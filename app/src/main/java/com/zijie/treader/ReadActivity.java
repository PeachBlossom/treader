package com.zijie.treader;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Slide;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.zijie.treader.base.BaseActivity;
import com.zijie.treader.db.BookMarks;
import com.zijie.treader.util.BookPageFactory;
import com.zijie.treader.util.CommonUtil;
import com.zijie.treader.view.PageWidget;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;

/**
 * Created by Administrator on 2016/1/3.
 */
public class ReadActivity extends BaseActivity implements OnClickListener,
        SeekBar.OnSeekBarChangeListener {
    @Bind(R.id.ll_read)
    private RelativeLayout rlayout;

    private static final String TAG = "ReadActivity";
    private LinearLayout layout;


    private static int begin = 0;// 记录的书籍开始位置
    private static int begin1;
    private static int jumpcencel_begin;
    public static Canvas mCurPageCanvas, mNextPageCanvas;
    private static String word = "";// 记录当前页面的文字
    private int a = 0, b = 0;// 记录toolpop的位置
    private TextView fontSize, readLight, bookMark, readJump,readSet;
    private static String bookPath,bookName;// 记录读入书的路径及书名
    private String ccc = null;// 记录是否为快捷方式调用
    protected long count = 1;
    public static SharedPreferences.Editor editor;
    private ImageButton listener_book,imageBtn_light,pop_return ;
    private TextView btn_mark_add,btn_mark_my,lightPlus,linghtDecrease;
    private TextView jumpOk, jumpCancel,fontBig,fontSmall;
    private Boolean isNight; // 亮度模式,白天和晚上
    protected int jumpPage;// 记录跳转进度条
    private int light; // 亮度值
    private WindowManager.LayoutParams lp;
    private TextView markEdit4;
    //这一页,下一页
    private static Bitmap mCurPageBitmap, mNextPageBitmap;

    private Context mContext = null;
    private PageWidget mPageWidget;
    private PopupWindow mPopupWindow, mToolpop, mToolpop1, mToolpop2,
            mToolpop3, mToolpop4, playpop,voicesetpop;
    private BookPageFactory pagefactory;
    private View popupwindwow, toolpop, toolpop1, toolpop2, toolpop3, toolpop4,
            playView,voiceSetView;  //加载popupwindow布局
    //屏幕高度
    int screenHeight;
    //屏幕宽度
    int screenWidth;
    // 电子书显示高度
    int readHeight;
    private SeekBar seekBar1, seekBar2, seekBar4;
    private Boolean show = false;// popwindow是否显示
    private Boolean voiceSetShow = false;//语音设置显示
    private Boolean voiceListining = false;//语音正在合成
    private int fontsize = 30; // 字体大小
    public static SharedPreferences sp;
    //默认字体大小
    private int defaultFontSize = 0;
    //最小字体大小
    private int minFontSize = 0;
    //最大字体大小
    private int maxFontSize = 0;
    public static String words;
    private boolean isStart;
    private Button button3;
    private  int scale;
    // 语音合成对象
    private SpeechSynthesizer mTts;
    // 默认发音人
    private String voicer = "xiaoyan";
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    // 缓冲进度
    private int mPercentForBuffering = 0;
    // 播放进度
    private int mPercentForPlaying = 0;
    private String[] mCloudVoicersEntries;
    private String[] mCloudVoicersValue ;
    private int selectedNum = 0;//选择导入书架的书本数量
    private AudioManager audio;
    private Typeface typeface;

    @Override
    public int getLayoutRes() {
        return R.layout.activity_read;
    }

    @Override
    protected void initData() {
        //保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hideSystemUI();//隐藏
        mContext = getBaseContext();
        typeface = Typeface.createFromAsset(getApplicationContext().getAssets(),"font/QH.ttf");
        scale = (int)mContext.getResources().getDisplayMetrics().density;

        initSpeech();
        initConfig();
        initPage();
        setPop();
        initVoiceSetPop();
        audio = (AudioManager) getSystemService(Service.AUDIO_SERVICE);

        // 提取记录在sharedpreferences的各种状态
        sp = getSharedPreferences("config", MODE_PRIVATE);
        editor = sp.edit();
        fontsize = getSize();// 获取配置文件中的size大小
        light = getLight();// 获取配置文件中的light值
        isNight = getDayOrNight();
        count = sp.getLong(bookPath + "count", 1);

    }

    private void initPage(){
        mCurPageBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.RGB_565);      //android:LargeHeap=true  use in  manifest application
        mNextPageBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.RGB_565);
        mCurPageCanvas = new Canvas(mCurPageBitmap);
        mNextPageCanvas = new Canvas(mNextPageBitmap);

        mPageWidget = new PageWidget(this, screenWidth, readHeight);// 页面
        rlayout.addView(mPageWidget);
    }
    private void initConfig(){
        //获取屏幕宽高
        WindowManager manage = getWindowManager();
        Display display = manage.getDefaultDisplay();
        Point displaysize = new Point();
        display.getSize(displaysize);
        screenWidth = displaysize.x;
        screenHeight = displaysize.y;
        readHeight = screenHeight - screenWidth / 320;
        //初始化字体大小
        defaultFontSize = (int) mContext.getResources().getDimension(R.dimen.reading_default_text_size);
        minFontSize = (int) mContext.getResources().getDimension(R.dimen.reading_min_text_size);
        maxFontSize = (int) mContext.getResources().getDimension(R.dimen.reading_max_text_size);
    }
    //初始化语音
    private void initSpeech(){
        SpeechUtility.createUtility(ReadActivity.this, SpeechConstant.APPID +"=5695a8b4");//创建语音配置对象
        mTts = SpeechSynthesizer.createSynthesizer(ReadActivity.this,mTtsInitListener);//初始化合成对象
        mCloudVoicersEntries = getResources().getStringArray(R.array.voicer_cloud_entries);
        mCloudVoicersValue = getResources().getStringArray(R.array.voicer_cloud_values);
        isStart = false;
    }

    @Override
    protected void initListener() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hideSystemUI();//隐藏
        mContext = getBaseContext();
        typeface = Typeface.createFromAsset(getApplicationContext().getAssets(),"font/QH.ttf");
        scale = (int)mContext.getResources().getDisplayMetrics().density;

        //初始化语音
        SpeechUtility.createUtility(ReadActivity.this, SpeechConstant.APPID +"=5695a8b4");//创建语音配置对象
        mTts = SpeechSynthesizer.createSynthesizer(ReadActivity.this,mTtsInitListener);//初始化合成对象
        mCloudVoicersEntries = getResources().getStringArray(R.array.voicer_cloud_entries);
        mCloudVoicersValue = getResources().getStringArray(R.array.voicer_cloud_values);
        isStart = false;
        //获取屏幕宽高
        WindowManager manage = getWindowManager();
        Display display = manage.getDefaultDisplay();
        Point displaysize = new Point();
        display.getSize(displaysize);
        screenWidth = displaysize.x;
        screenHeight = displaysize.y;
        readHeight = screenHeight - screenWidth / 320;

      defaultFontSize = (int) mContext.getResources().getDimension(R.dimen.reading_default_text_size) ;  //text size
        minFontSize = (int) mContext.getResources().getDimension(R.dimen.reading_min_text_size);
        maxFontSize = (int) mContext.getResources().getDimension(R.dimen.reading_max_text_size);


        mCurPageBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.RGB_565);      //android:LargeHeap=true  use in  manifest application
        mNextPageBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.RGB_565);
        mCurPageCanvas = new Canvas(mCurPageBitmap);
        mNextPageCanvas = new Canvas(mNextPageBitmap);

        mPageWidget = new PageWidget(this, screenWidth, readHeight);// 页面
        RelativeLayout rlayout = (RelativeLayout) findViewById(R.id.ll_read);
        rlayout.addView(mPageWidget);

        setPop(); //初始化POPUPWINDOW
        initVoiceSetPop();
        audio = (AudioManager) getSystemService(Service.AUDIO_SERVICE);

        // 提取记录在sharedpreferences的各种状态
        sp = getSharedPreferences("config", MODE_PRIVATE);
        editor = sp.edit();
        fontsize = getSize();// 获取配置文件中的size大小
        light = getLight();// 获取配置文件中的light值
        isNight = getDayOrNight();
        count = sp.getLong(bookPath + "count", 1);

        lp = getWindow().getAttributes();
        lp.screenBrightness = light / 10.0f < 0.01f ? 0.01f : light / 10.0f;
        getWindow().setAttributes(lp);
        //获取intent中的携带的信息
        Intent intent = getIntent();
        bookPath = intent.getStringExtra("bookpath");
        bookName = intent.getStringExtra("bookname");
        ccc = intent.getStringExtra("ccc");
        begin1 = intent.getIntExtra("bigin", 0);
        if(begin1 == 0) {
            begin = sp.getInt(bookPath + "begin", 0);
        }else {
            begin = begin1;
        }

        //日间或夜间模式设置
        pagefactory = new BookPageFactory(screenWidth, readHeight,this);// 书工厂
        if (isNight) {
            pagefactory.setBgBitmap(BookPageFactory.decodeSampledBitmapFromResource(
                    this.getResources(), R.drawable.main_bg, screenWidth, readHeight));
            pagefactory.setM_textColor(Color.rgb(128, 128, 128));
        } else {
        //  pagefactory.setBgBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.bg));
           // pagefactory.setBgBitmap(BookPageFactory.decodeSampledBitmapFromResource(
                 //   this.getResources(),R.drawable.bg,screenWidth,readHeight));
            Bitmap bmp = Bitmap.createBitmap(screenWidth,screenHeight, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bmp);
           // canvas.drawColor(Color.rgb(250, 249, 222));
            canvas.drawColor(getResources().getColor(R.color.read_background_paperYellow));
            pagefactory.setM_textColor(getResources().getColor(R.color.read_textColor));
            pagefactory.setBgBitmap(bmp);
         //   pagefactory.setM_textColor(Color.rgb(28, 28, 28));
        }
        // 从指定位置打开书籍，默认从开始打开
        try {
            pagefactory.openbook(bookPath, begin);
            pagefactory.setM_fontSize(fontsize);
            pagefactory.onDraw(mCurPageCanvas);
            // Log.d("ReadActivity", "sp中的size" + size);
            word = pagefactory.getFirstTwoLineText();// 获取当前阅读位置的前两行文字,用作书签
            editor.putInt(bookPath + "begin", begin).apply();
           // Log.d("ReadActivity", "第一页首两行文字是" + word);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    pagefactory.getBookInfo();  //获取章节目录
                }
            }).start();
        } catch (IOException e1) {
            Log.e(TAG, "打开电子书失败", e1);
            Toast.makeText(this, "打开电子书失败", Toast.LENGTH_SHORT).show();
        }
        //mPageWidget onTouch监听  翻页或显示菜单
        mPageWidget.setBitmaps(mCurPageBitmap, mCurPageBitmap);
        mPageWidget.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent e) {
                boolean ret = false;
                if (v == mPageWidget) {
                        if (e.getAction() == MotionEvent.ACTION_DOWN) {
                            mPageWidget.abortAnimation();
                            mPageWidget.calcCornerXY(e.getX(), e.getY());
                            pagefactory.onDraw(mCurPageCanvas);
                           // Log.d("ReadActivity","action_down");
                            int x = (int) e.getX();
                            int y = (int) e.getY();
                            //Action_Down时在中间位置显示菜单
                            if (x > screenWidth / 3 && x < screenWidth * 2 / 3 && y > screenHeight / 3 && y < screenHeight * 2 / 3) {
                                if(!voiceListining && !show) {
                                        showSystemUI();
                                        pop();
                                        show = true;
                                }else {
                                    if(!voiceSetShow){
                                        showSystemUI();
                                        setVoicesetpop();
                                        if (mTts.isSpeaking()) {
                                            mTts.pauseSpeaking();
                                        }
                                        voiceSetShow = true;
                                    }else {
                                         hideSystemUI();
                                         voicesetpop.dismiss();
                                         mTts.resumeSpeaking();
                                         voiceSetShow = false;
                                    }
                                }
                                return false;//停止向下分发事件
                            }
                            if (x < screenWidth / 2) {// 从左翻
                                if(voiceListining) {
                                    return false ;//如果正在语音朗读不翻页，停止向下分发事件
                                    }
                                    try {
                                        pagefactory.prePage();
                                        begin = pagefactory.getM_mbBufBegin();// 获取当前阅读位置
                                        word = pagefactory.getFirstTwoLineText();// 获取当前阅读位置的首行文字
                                    } catch (IOException e1) {
                                        Log.e(TAG, "onTouch->prePage error", e1);
                                    }
                                    if (pagefactory.isfirstPage()) {
                                        Toast.makeText(mContext, "当前是第一页", Toast.LENGTH_SHORT).show();
                                        return false;
                                    }
                                    pagefactory.onDraw(mNextPageCanvas);

                                } else if (x >= screenWidth / 2) {// 从右翻
                                    if(voiceListining) {
                                        return false ;
                                    }
                                    try {
                                        pagefactory.nextPage();
                                        begin = pagefactory.getM_mbBufBegin();// 获取当前阅读位置
                                        word = pagefactory.getFirstTwoLineText();// 获取当前阅读位置的首行文字
                                    } catch (IOException e1) {
                                        Log.e(TAG, "onTouch->nextPage error", e1);
                                    }
                                    if (pagefactory.islastPage()) {
                                        Toast.makeText(mContext, "已经是最后一页了", Toast.LENGTH_SHORT).show();
                                        return false;
                                    }
                                    pagefactory.onDraw(mNextPageCanvas);
                                }
                           mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);
                        }
                        editor.putInt(bookPath + "begin", begin).apply();
                        ret = mPageWidget.doTouchEvent(e);
                        return ret;
                }
                return false;
            }
        });
    }

    /**
     * 记录数据 并清空popupwindow
     */
    private void clear() {

        show = false;
        mPopupWindow.dismiss();
        popDismiss();
    }

    /**
     * 记录配置文件中字体大小
     */
    private void setSize(int fontsize) {
        try {
          //  fontsize = seekBar1.getProgress() + defaultFontSize;
            editor.putInt("size", fontsize);
            editor.apply();
        } catch (Exception e) {
            Log.e(TAG, "setSize-> Exception error", e);
        }
    }

    /**
     * 记录配置文件中亮度值和横竖屏
     */
    private void setLight() {
        try {
            light = seekBar2.getProgress();
            editor.putInt("light", light);

            editor.apply();
        } catch (Exception e) {
            Log.e(TAG, "setLight-> Exception error", e);
        }
    }

    /**
     * 读取配置文件中亮度值
     */
    private int getLight() {
        return sp.getInt("light", 3);
    }

   /**
   * 设置夜间还是白天阅读模式
   *
   * */
     private void setDayOrNight () {
         try {
             if (isNight) {
                 editor.putBoolean("night", true);
             } else {
                 editor.putBoolean("night", false);
             }
             editor.apply();
         }catch (Exception e) {
             Log.e(TAG, "setDayOrNight-> Exception error", e);
         }
     }

    /**
     * 获取夜间还是白天阅读模式
     */
    private boolean getDayOrNight() {
        return sp.getBoolean("night", false);
    }

    /**
     * 读取配置文件中字体大小
     */
    private int getSize() {
      return sp.getInt("size", defaultFontSize);
    }

    /**
     * 点击监听的处理
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 字体按钮
            case R.id.bookBtn_size:
                a = 1;
                setToolPop(a);
                break;
            // 亮度按钮
            case R.id.bookBtn_light:
                a = 2;
                setToolPop(a);
                break;
            // 书签按钮
            case R.id.bookBtn_mark:
                a = 3;
                setToolPop(a);
                break;
            // 跳转按钮
            case R.id.bookBtn_jump:
                a = 4;
                setToolPop(a);
                jumpcencel_begin = begin;
                break;
            case R.id.readSet:
                a = 5;
             //   setToolPop(a);
                break;
            // 夜间模式按钮
            case R.id.imageBtn_light:
                if (isNight) {
                    isNight = false;
                    layout.setBackgroundResource(R.drawable.tmall_bar_bg);
                  //  pagefactory.setM_textColor(Color.rgb(28, 28, 28));
                    pagefactory.setM_textColor(Color.rgb(50, 65, 78));
                    imageBtn_light.setImageResource(R.mipmap.menu_daynight_icon);

                  // pagefactory.setBgBitmap(BitmapFactory.decodeResource(
                   //         this.getResources(), R.drawable.bg));
                    Bitmap bmp=Bitmap.createBitmap(screenWidth,screenHeight, Bitmap.Config.RGB_565);
                    Canvas canvas=new Canvas(bmp);
                    canvas.drawColor(Color.rgb(250,249,222));
                    pagefactory.setBgBitmap(bmp);
                } else {
                    isNight = true;
                    layout.setBackgroundResource(R.drawable.tmall_bar_bg);
                    pagefactory.setM_textColor(Color.rgb(128, 128, 128));
                    imageBtn_light.setImageResource(R.mipmap.menu_light_icon2);
                    pagefactory.setBgBitmap(BitmapFactory.decodeResource(
                            this.getResources(), R.drawable.main_bg));
                }
                setDayOrNight();
                pagefactory.setM_mbBufBegin(begin);
                pagefactory.setM_mbBufEnd(begin);
                postInvalidateUI();
                break;
            // 添加书签按钮
            case R.id.Btn_mark_add:
              //  SQLiteDatabase db = markhelper.getWritableDatabase();
                word = word.trim();
                while (word.startsWith("　")) {
                    word = word.substring(1, word.length()).trim();
                }
                BookMarks bookMarks = new BookMarks();
                try {
                    SimpleDateFormat sf = new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm ss");
                    String time = sf.format(new Date());
                    bookMarks.setTime(time);
                    bookMarks.setBegin(begin);
                    bookMarks.setText(word);
                    bookMarks.setBookpath(bookPath);
                    bookMarks.save();

                    Toast.makeText(ReadActivity.this, "书签添加成功", Toast.LENGTH_SHORT).show();
                } catch (SQLException e) {
                    Toast.makeText(ReadActivity.this, "该书签已存在", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(ReadActivity.this, "添加书签失败", Toast.LENGTH_SHORT).show();
                }
                mToolpop.dismiss();
                mToolpop3.dismiss();
                break;
            // 我的书签按钮
           case R.id.Btn_mark_my:
                Intent intent = new Intent();
                intent.setClass(ReadActivity.this,MarkActivity.class);
                intent.putExtra("bookpath", bookPath);
                intent.putExtra("bookname", bookName);
                startActivity(intent);
                mPopupWindow.dismiss();
                popDismiss();

                break;
            //跳转确定按钮
            case R.id.jump_ok:
                clear();
                hideSystemUI();
                pagefactory.setM_mbBufBegin(begin);
                pagefactory.setM_mbBufEnd(begin);
                postInvalidateUI();
                break;
            //跳转取消按钮
            case R.id.jump_cancel:
                clear();
                hideSystemUI();
                pagefactory.setM_mbBufBegin(jumpcencel_begin);
                pagefactory.setM_mbBufEnd(jumpcencel_begin);
                postInvalidateUI();
                break;
            //暂停播放语音按钮
            case R.id.play_pause:
                mTts.pauseSpeaking();
                break;
            //在线合成发音人按钮
            case R.id.play_setting_people:

                new AlertDialog.Builder(this).setTitle("在线合成发音人选项")
                        .setSingleChoiceItems(mCloudVoicersEntries, // 单选框有几项,各是什么名字
                                selectedNum, // 默认的选项
                                new DialogInterface.OnClickListener() { // 点击单选框后的处理
                                    public void onClick(DialogInterface dialog,
                                                        int which) { // 点击了哪一项
                                        voicer = mCloudVoicersValue[which];
                                        selectedNum = which;
                                        dialog.dismiss();
                                    }
                                }).show();
                break;
            //开始语音朗读
            case R.id.listener_book:
                // 设置参数
                setParam();
                //Log.d("ReadActivity", "tts是否执行到这里");
                int code =  mTts.startSpeaking(words, mTtsListener);

                if (code != ErrorCode.SUCCESS) {
                    if(code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED){
                        //未安装则跳转到提示安装页面
                        //  mInstaller.install();
                    }else {
//                        showTip("语音合成失败,错误码: " + code);
                    }
                }
              //  voiceListining = true;
                hideSystemUI();
                mPopupWindow.dismiss();
                popDismiss();
                show = false;
                break;
            //pop window 中间空白view按钮
            case R.id.blank_view:
                if (show) {
                    show = false;
                    hideSystemUI();
                    mPopupWindow.dismiss();
                    popDismiss();
                }
                break;
            //pop window 返回按钮
            case R.id.pop_return:
                if (show) {
                    show = false;
                    hideSystemUI();
                    mPopupWindow.dismiss();
                    popDismiss();
                }

                KeyEvent newEvent = new KeyEvent(KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_BACK);
                onKeyDown(KeyEvent.KEYCODE_BACK, newEvent);
                break;
            //退出语音朗读
            case R.id.play_quit:

                mTts.stopSpeaking();
                mTts.destroy();
                hideSystemUI();
                voicesetpop.dismiss();
                voiceListining = false;
                show = false;
                break;
        }
    }

    /**
     * 判断是从哪个界面进入的READ
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
           case KeyEvent.KEYCODE_BACK :

               if (show) {// 如果popwindow正在显示
                   getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                   show = false;
                   mPopupWindow.dismiss();
                   popDismiss();
               } else {
                   ReadActivity.this.finish();
               }


                if (!mPopupWindow.isShowing()) {
                    hideSystemUI();
                } else {
                   // showSystemUI();
                }

                voiceListining = false;
                if (voicesetpop.isShowing()) {
                    voicesetpop.dismiss();
                }
            return true;
           //音量加大及减小事件
           case KeyEvent.KEYCODE_VOLUME_UP:
               if(!voiceListining && !show) {
                   prePage();
               }else
               audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,
                       AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
               return true;
           case KeyEvent.KEYCODE_VOLUME_DOWN:
               if(!voiceListining && !show) {
                   nextPage();
               }else
               audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER,
                       AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
               return true;
           default:
               break;
        }
        return super.onKeyDown(keyCode,event);
    }

    /**
     * 添加对menu按钮的监听
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (show) {
                show = false;
                mPopupWindow.dismiss();
                popDismiss();
            } else {
                show = true;
                pop();
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * 各种进度条变化设置
     * @param seekBar
     * @param progress
     * @param fromUser
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        switch (seekBar.getId()) {
            // 字体进度条
            case R.id.seekBar_size:
                fontsize = seekBar1.getProgress() + minFontSize;
                //Log.d("ReadActivity","size的大小"+fontsize);
                setSize(fontsize);
                pagefactory.setM_fontSize(fontsize);
                pagefactory.setM_mbBufBegin(begin);
                pagefactory.setM_mbBufEnd(begin);
                postInvalidateUI();
                break;
            // 亮度进度条
            case R.id.seekBar_light:
                light = seekBar2.getProgress();
                setLight();
                lp.screenBrightness = light / 10.0f < 0.01f ? 0.01f : light / 10.0f;
                getWindow().setAttributes(lp);
                break;
            // 跳转进度条
            case R.id.seekBar_jump:
                int s = seekBar4.getProgress();
                markEdit4.setText(s + "%");
                begin = (pagefactory.getM_mbBufLen() * s) / 100;
                editor.putInt(bookPath + "begin", begin).commit();
                pagefactory.setM_mbBufBegin(begin);
                pagefactory.setM_mbBufEnd(begin);
                try {
                    if (s == 100) {
                        pagefactory.prePage();
                        pagefactory.getM_mbBufBegin();
                        begin = pagefactory.getM_mbBufEnd();
                        pagefactory.setM_mbBufBegin(begin);
                        pagefactory.setM_mbBufBegin(begin);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "onProgressChanged seekBar4-> IOException error", e);
                }
                postInvalidateUI();
                break;
            case R.id.readSet:

                break;

        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    /**
     * popupwindow的弹出,工具栏
     */
    public void pop() {

        mPopupWindow.showAtLocation(mPageWidget, Gravity.NO_GRAVITY, 0, 0);
        fontSize = (TextView) popupwindwow.findViewById(R.id.bookBtn_size);
        readLight = (TextView) popupwindwow.findViewById(R.id.bookBtn_light);
        bookMark = (TextView) popupwindwow.findViewById(R.id.bookBtn_mark);
        readJump = (TextView) popupwindwow.findViewById(R.id.bookBtn_jump);
        readSet = (TextView) popupwindwow.findViewById(R.id.readSet);
        layout = (LinearLayout) popupwindwow.findViewById(R.id.bookpop_bottom);//主要为了夜间模式时设置背景
        fontSize.setTypeface(typeface);//设置字体
        readLight.setTypeface(typeface);
        bookMark.setTypeface(typeface);
        readJump.setTypeface(typeface);
        readSet.setTypeface(typeface);

        TextView blank_view = (TextView) popupwindwow.findViewById(R.id.blank_view);
        listener_book = (ImageButton) popupwindwow.findViewById(R.id.listener_book);
        pop_return = (ImageButton) popupwindwow.findViewById(R.id.pop_return);
        imageBtn_light = (ImageButton) popupwindwow.findViewById((R.id.imageBtn_light));
        getDayOrNight();
        if (isNight) {
            layout.setBackgroundResource(R.drawable.tmall_bar_bg);
            imageBtn_light.setImageResource(R.mipmap.menu_light_icon2);
        } else {
            layout.setBackgroundResource(R.drawable.tmall_bar_bg);
            imageBtn_light.setImageResource(R.mipmap.menu_daynight_icon);
        }
        fontSize.setOnClickListener(this);
        readLight.setOnClickListener(this);
        bookMark.setOnClickListener(this);
        readJump.setOnClickListener(this);
        readSet.setOnClickListener(this);
        blank_view.setOnClickListener(this);
        listener_book.setOnClickListener(this);
        pop_return.setOnClickListener(this);
        imageBtn_light.setOnClickListener(this);
    }

    /**
     * 关闭弹出pop
     */
    public void popDismiss() {
        mToolpop.dismiss();
        mToolpop1.dismiss();
        mToolpop2.dismiss();
        mToolpop3.dismiss();
        mToolpop4.dismiss();
        playpop.dismiss();
    }



    /**
     * 初始化所有POPUPWINDOW
     */
    private void setPop() {
        popupwindwow = this.getLayoutInflater().inflate(R.layout.bookpop, null);
        toolpop = this.getLayoutInflater().inflate(R.layout.toolpop, null);
        mPopupWindow = new PopupWindow(popupwindwow, LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
      //  mPopupWindow.setAnimationStyle(R.style.popwin_anim_style);
        mToolpop = new PopupWindow(toolpop, LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        toolpop1 = this.getLayoutInflater().inflate(R.layout.tool_size, null);
        mToolpop1 = new PopupWindow(toolpop1, LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        toolpop2 = this.getLayoutInflater().inflate(R.layout.tool_light, null);
        mToolpop2 = new PopupWindow(toolpop2, LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        toolpop3 = this.getLayoutInflater().inflate(R.layout.tool_mark, null);
        mToolpop3 = new PopupWindow(toolpop3, LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        toolpop4 = this.getLayoutInflater().inflate(R.layout.tool_jump, null);
        mToolpop4 = new PopupWindow(toolpop4, LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        playView = this.getLayoutInflater().inflate(R.layout.play_pop, null);
        playpop = new PopupWindow(playView, LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

    }

   /**
    * 初始化语音朗读菜单popupwindow
    */
    private void initVoiceSetPop(){
        voiceSetView = this.getLayoutInflater().inflate(R.layout.voicesetmenu,null);
        voicesetpop = new PopupWindow(voiceSetView,LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
    }
   /**
    * 设置语音朗读菜单的显示
    *
    */
    private void setVoicesetpop(){
        voicesetpop.showAtLocation(mPageWidget,Gravity.BOTTOM,0,0);
        TextView play_pause = (TextView)voiceSetView.findViewById(R.id.play_pause);
        TextView voice_people = (TextView)voiceSetView.findViewById(R.id.play_setting_people);
        ImageButton icon_quit = (ImageButton)voiceSetView.findViewById(R.id.IB_quit);
        TextView play_quit = (TextView)voiceSetView.findViewById(R.id.play_quit);
      //  play_pause.setOnClickListener(this);
      //  pay_continute.setOnClickListener(this);
        voice_people.setOnClickListener(this);
        play_quit.setOnClickListener(this);
    }

    /**
     * 设置popupwindow的显示与隐藏
     *
     * @param a
     */
    public void setToolPop(int a) {
        Log.i("hck", "setToolPop: " + "b:" + b + "  a:" + a);
        if (a == b && a != 0) {
            if (mToolpop.isShowing()) {
                popDismiss();
            } else {
                mToolpop.showAtLocation(mPageWidget, Gravity.BOTTOM, 0,
                        screenWidth * 45 / 320);
                // 当点击字体按钮
                if (a == 1) {
                    mToolpop1.setBackgroundDrawable(new ColorDrawable(0xb0000000));//设置背景为半透明色
                    if(CommonUtil.getBottomStatusHeight(mContext)!=0) {
                        int popofset = 70*scale+ CommonUtil.getBottomStatusHeight(mContext);
                        mToolpop1.showAtLocation(mPageWidget, Gravity.BOTTOM, 0, popofset);
                    }else
                        mToolpop1.showAtLocation(mPageWidget, Gravity.BOTTOM, 0, 70*scale);
                    seekBar1 = (SeekBar) toolpop1.findViewById(R.id.seekBar_size);
                    fontBig = (TextView) toolpop1.findViewById(R.id.size_plus);
                    fontSmall = (TextView) toolpop1.findViewById(R.id.size_decrease);
                    fontBig.setTypeface(typeface);
                    fontSmall.setTypeface(typeface);
                    fontsize = sp.getInt("size", defaultFontSize);
                    seekBar1.setProgress((fontsize-minFontSize));
                    seekBar1.setOnSeekBarChangeListener(this);
                    fontBig.setOnClickListener(this);
                    fontSmall.setOnClickListener(this);

                }
                // 当点击亮度按钮
                if (a == 2) {
                    mToolpop2.setBackgroundDrawable(new ColorDrawable(0xb0000000));
                    if(CommonUtil.getBottomStatusHeight(mContext)!=0) {
                        int popofset = 70*scale+CommonUtil.getBottomStatusHeight(mContext);
                        mToolpop2.showAtLocation(mPageWidget, Gravity.BOTTOM, 0, popofset);
                    }else
                        mToolpop2.showAtLocation(mPageWidget, Gravity.BOTTOM, 0, 70*scale);
                    seekBar2 = (SeekBar) toolpop2.findViewById(R.id.seekBar_light);
                    lightPlus = (TextView) toolpop2.findViewById(R.id.light_plus);
                    linghtDecrease = (TextView) toolpop2.findViewById(R.id.light_decrease);
                    lightPlus.setTypeface(typeface);
                    linghtDecrease.setTypeface(typeface);
                    getLight();
                    seekBar2.setProgress(light);
                    seekBar2.setOnSeekBarChangeListener(this);
                }
                // 当点击书签按钮
                if (a == 3) {
                    mToolpop3.setBackgroundDrawable(new ColorDrawable(0xb0000000));
                    if(CommonUtil.getBottomStatusHeight(mContext)!=0) {
                        int popofset = 70*scale+CommonUtil.getBottomStatusHeight(mContext);
                        mToolpop3.showAtLocation(mPageWidget, Gravity.BOTTOM, 0, popofset);
                    }else
                        mToolpop3.showAtLocation(mPageWidget, Gravity.BOTTOM, 0, 70*scale);
                    btn_mark_add = (TextView) toolpop3.findViewById(R.id.Btn_mark_add);
                    btn_mark_my = (TextView) toolpop3.findViewById(R.id.Btn_mark_my);
                    btn_mark_add.setTypeface(typeface);
                    btn_mark_my.setTypeface(typeface);
                    btn_mark_add.setOnClickListener(this);
                    btn_mark_my.setOnClickListener(this);
                }
                // 当点击跳转按钮
                if (a == 4) {
                   mToolpop4.setBackgroundDrawable(new ColorDrawable(0xb0000000));
                    if(CommonUtil.getBottomStatusHeight(mContext)!=0) {
                        int popofset = 70*scale+CommonUtil.getBottomStatusHeight(mContext);
                        mToolpop4.showAtLocation(mPageWidget, Gravity.BOTTOM, 0, popofset);
                    }else
                        mToolpop4.showAtLocation(mPageWidget, Gravity.BOTTOM, 0, 70*scale);
                    int bc = CommonUtil.getBottomStatusHeight(mContext);
                   // Log.d("ReadActivity","虚拟功能键栏高度是"+bc);
                    mToolpop4.showAtLocation(mPageWidget, Gravity.BOTTOM, 0, 500);
                    jumpOk = (TextView) toolpop4.findViewById(R.id.jump_ok);
                    jumpCancel = (TextView) toolpop4.findViewById(R.id.jump_cancel);
                    seekBar4 = (SeekBar) toolpop4.findViewById(R.id.seekBar_jump);
                    markEdit4 = (TextView) toolpop4.findViewById(R.id.markEdit4);
                    jumpOk.setTypeface(typeface);
                    jumpCancel.setTypeface(typeface);
                    markEdit4.setTypeface(typeface);
                    // begin = sp.getInt(bookPath + "begin", 1);
                    float fPercent = (float) (begin * 1.0 / pagefactory.getM_mbBufLen());
                    DecimalFormat df = new DecimalFormat("#0");
                    String strPercent = df.format(fPercent * 100) + "%";
                    markEdit4.setText(strPercent);
                    seekBar4.setProgress(Integer.parseInt(df.format(fPercent * 100)));
                    seekBar4.setOnSeekBarChangeListener(this);
                    jumpOk.setOnClickListener(this);
                    jumpCancel.setOnClickListener(this);
                }
                    if (a == 5) {
                    playpop.setBackgroundDrawable(new ColorDrawable(0xb0000000));
                        if(CommonUtil.getBottomStatusHeight(mContext)!=0) {
                            int popofset = 70*scale+CommonUtil.getBottomStatusHeight(mContext);
                            playpop.showAtLocation(mPageWidget, Gravity.BOTTOM, 0,popofset);
                        }else
                            playpop.showAtLocation(mPageWidget, Gravity.BOTTOM, 0,70*scale);
					Button button = (Button) playView.findViewById(R.id.play_set);
					button3 = (Button) playView.findViewById(R.id.play_voice);
                    Button tts_people_select = (Button)playView.findViewById(R.id.play_setting);
                    Button tts_resume = (Button)playView.findViewById(R.id.play_continute);
                    tts_people_select.setOnClickListener(this);
                    tts_resume.setOnClickListener(this);
					button.setOnClickListener(this);
					button3.setOnClickListener(this);
                }

            }
        } else {
            if (mToolpop.isShowing()) {
                // 对数据的记录
                popDismiss();
            }
            mToolpop.showAtLocation(mPageWidget, Gravity.BOTTOM, 0,
                    screenWidth * 45 / 320);
            // 点击字体按钮
            if (a == 1) {
                mToolpop1.setBackgroundDrawable(new ColorDrawable(0xb0000000));
                if(CommonUtil.getBottomStatusHeight(mContext)!=0) {
                    int popofset = 70*scale+CommonUtil.getBottomStatusHeight(mContext);
                    mToolpop1.showAtLocation(mPageWidget, Gravity.BOTTOM, 0,popofset);
                }else
                    mToolpop1.showAtLocation(mPageWidget, Gravity.BOTTOM, 0,70*scale);
                seekBar1 = (SeekBar) toolpop1.findViewById(R.id.seekBar_size);
                fontBig = (TextView) toolpop1.findViewById(R.id.size_plus);
                fontSmall = (TextView) toolpop1.findViewById(R.id.size_decrease);
                fontBig.setTypeface(typeface);
                fontSmall.setTypeface(typeface);
                fontsize = sp.getInt("size", defaultFontSize);
                seekBar1.setProgress(fontsize - minFontSize);
                seekBar1.setOnSeekBarChangeListener(this);
                fontBig.setOnClickListener(this);
                fontSmall.setOnClickListener(this);
            }
            // 点击亮度按钮
            if (a == 2) {
                mToolpop2.setBackgroundDrawable(new ColorDrawable(0xb0000000));
                if(CommonUtil.getBottomStatusHeight(mContext)!=0) {
                    int popofset = 70*scale+CommonUtil.getBottomStatusHeight(mContext);
                    mToolpop2.showAtLocation(mPageWidget, Gravity.BOTTOM, 0, popofset);
                }else
                    mToolpop2.showAtLocation(mPageWidget, Gravity.BOTTOM, 0, 70*scale);
                seekBar2 = (SeekBar) toolpop2.findViewById(R.id.seekBar_light);
                lightPlus = (TextView) toolpop2.findViewById(R.id.light_plus);
                linghtDecrease = (TextView) toolpop2.findViewById(R.id.light_decrease);
                lightPlus.setTypeface(typeface);
                linghtDecrease.setTypeface(typeface);
                getLight();
                seekBar2.setProgress(light);
                seekBar2.setOnSeekBarChangeListener(this);
            }
            // 点击书签按钮
            if (a == 3) {
                mToolpop3.setBackgroundDrawable(new ColorDrawable(0xb0000000));
                if(CommonUtil.getBottomStatusHeight(mContext)!=0) {
                    int popofset = 70*scale+CommonUtil.getBottomStatusHeight(mContext);
                    mToolpop3.showAtLocation(mPageWidget, Gravity.BOTTOM, 0, popofset);
                }else
                    mToolpop3.showAtLocation(mPageWidget, Gravity.BOTTOM, 0, 70*scale);
                btn_mark_add = (TextView) toolpop3.findViewById(R.id.Btn_mark_add);
                btn_mark_my = (TextView) toolpop3.findViewById(R.id.Btn_mark_my);
                btn_mark_add.setTypeface(typeface);
                btn_mark_my.setTypeface(typeface);
                btn_mark_add.setOnClickListener(this);
                btn_mark_my.setOnClickListener(this);
            }
            // 点击跳转按钮
            if (a == 4) {
                mToolpop4.setBackgroundDrawable(new ColorDrawable(0xb0000000));
                if(CommonUtil.getBottomStatusHeight(mContext)!=0) {
                    int popofset = 70*scale+CommonUtil.getBottomStatusHeight(mContext);
                    mToolpop4.showAtLocation(mPageWidget, Gravity.BOTTOM, 0, popofset);
                }else
                    mToolpop4.showAtLocation(mPageWidget, Gravity.BOTTOM, 0, 50*scale);
                int bc = CommonUtil.getBottomStatusHeight(mContext);
                jumpOk = (TextView) toolpop4.findViewById(R.id.jump_ok);
                jumpCancel = (TextView) toolpop4.findViewById(R.id.jump_cancel);
                seekBar4 = (SeekBar) toolpop4.findViewById(R.id.seekBar_jump);
                markEdit4 = (TextView) toolpop4.findViewById(R.id.markEdit4);
                jumpOk.setTypeface(typeface);
                jumpCancel.setTypeface(typeface);
                markEdit4.setTypeface(typeface);
                float fPercent = (float) (begin * 1.0 / pagefactory.getM_mbBufLen());
                DecimalFormat df = new DecimalFormat("#0");
                String strPercent = df.format(fPercent * 100) + "%";
                markEdit4.setText(strPercent);
                seekBar4.setProgress(Integer.parseInt(df.format(fPercent * 100)));
                seekBar4.setOnSeekBarChangeListener(this);
                jumpOk.setOnClickListener(this);
                jumpCancel.setOnClickListener(this);
            }
            if (a == 5) {
                playpop.setBackgroundDrawable(new ColorDrawable(0xb0000000));
                //根据是否有虚拟按键来设置显示位置
                if(CommonUtil.getBottomStatusHeight(mContext)!=0) {
                    int popofset = 70*scale+CommonUtil.getBottomStatusHeight(mContext);
                    playpop.showAtLocation(mPageWidget, Gravity.BOTTOM, 0,popofset);
                }else {
                    playpop.showAtLocation(mPageWidget, Gravity.BOTTOM, 0, 70 * scale);
                }
                Button button = (Button) playView.findViewById(R.id.play_set);
                button3 = (Button) playView.findViewById(R.id.play_voice);
                Button tts_people_select = (Button)playView.findViewById(R.id.play_setting);
                Button tts_resume = (Button)playView.findViewById(R.id.play_continute);
                tts_people_select.setOnClickListener(this);
                tts_resume.setOnClickListener(this);
                button.setOnClickListener(this);
                button3.setOnClickListener(this);
            }
        }
        // 记录上次点击的是哪一个
        b = a;
    }


    /**
     * 刷新界面
     */
    public void postInvalidateUI() {
        mPageWidget.abortAnimation();
        pagefactory.onDraw(mCurPageCanvas);
        try {
            pagefactory.currentPage();
            begin = pagefactory.getM_mbBufBegin();// 获取当前阅读位置
            word = pagefactory.getFirstTwoLineText();// 获取当前阅读位置的首两行文字
        } catch (IOException e1) {
            Log.e(TAG, "postInvalidateUI->IOException error", e1);
        }

        pagefactory.onDraw(mNextPageCanvas);
        mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);
        mPageWidget.postInvalidate();

    }
    /**
    *  合成参数设置
    */
    private void setParam(){

        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 设置在线合成发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
        //设置合成语速
        mTts.setParameter(SpeechConstant.SPEED,  "60");
        //设置合成音调
        mTts.setParameter(SpeechConstant.PITCH,  "50");
        //设置合成音量
        mTts.setParameter(SpeechConstant.VOLUME,  "50");

        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

    }

//    private void showTip(final String str) {
//        mToast.setText(str);
//        mToast.show();
//    }

    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            voiceListining = true;
//            showTip("开始播放");
        }

        @Override
        public void onSpeakPaused() {
//            showTip("暂停播放");
        }

        @Override
        public void onSpeakResumed() {
//            showTip("继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
            // 合成进度
            mPercentForBuffering = percent;
          //  showTip(String.format(getString(R.string.tts_toast_format),
                 //   mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
            mPercentForPlaying = percent;
          //  showTip(String.format(getString(R.string.tts_toast_format),
                 //   mPercentForBuffering, mPercentForPlaying));
            if (mPercentForPlaying == 100) {
                nextPage();
                mTts.startSpeaking(words, mTtsListener);//当前页面结束开始下一阅读页面的语音播放
            }
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                nextPage();
                mTts.startSpeaking(words, mTtsListener);//当前页面结束开始下一阅读页面的语音播放
              //  showTip("播放完成");
            } else if (error != null) {
//                showTip(error.getPlainDescription(true));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    /**
     * 初始化监听。
     */
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
//                showTip("初始化失败,错误码："+code);
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };

    /**
     * 隐藏菜单。沉浸式阅读
     */
    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        //  | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        );
    }

    private void showSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    public int getbegin() {
        return begin;
    }

    public static Bitmap getmCurPageBitmap() {
        return  mCurPageBitmap;
    }


    public static Bitmap getmNextPageBitmap() {
        return mNextPageBitmap;
    }

    @Override
    protected void onPause() {
        show = false;
        if(!mPopupWindow.isShowing()){
            hideSystemUI();
        }else showSystemUI();
        super.onPause();

    }

    @Override
    protected void onResume() {
        show = false;
        if(!mPopupWindow.isShowing()){
            hideSystemUI();
        }else showSystemUI();
        super.onResume();

    }

    @Override
    protected  void onStop() {

        show = false;
        if(!mPopupWindow.isShowing()){
            hideSystemUI();
        }else showSystemUI();

        super.onStop();

    }

    @Override
    protected void onDestroy() {
        mTts.stopSpeaking();
        // 退出时释放连接
        mTts.destroy();
        super.onDestroy();
        pagefactory = null;
        mPageWidget = null;

        finish();
    }

    /**
     * 翻到上一页
     */
    public void prePage() {
        mPageWidget.abortAnimation();
        mPageWidget.calcCornerXY(10, screenHeight-10);//从左翻页
        pagefactory.onDraw(mCurPageCanvas);
        try {
            pagefactory.prePage();
            begin = pagefactory.getM_mbBufBegin();// 获取当前阅读位置
            word = pagefactory.getFirstTwoLineText();// 获取当前阅读位置的首行文字
        } catch (IOException e1) {
            Log.e(TAG, "onTouch->prePage error", e1);
        }
        if (pagefactory.isfirstPage()) {
            Toast.makeText(mContext, "当前是第一页", Toast.LENGTH_SHORT).show();
        }else {
            pagefactory.onDraw(mNextPageCanvas);
            mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);
            editor.putInt(bookPath + "begin", begin).commit();
            MotionEvent e = MotionEvent.obtain(0, 0, MotionEvent.ACTION_MOVE,
                    20, screenHeight-20, 1);
            mPageWidget.doTouchEvent(e);
        }
    }
     /**
      * 翻下一页
      */
    public void nextPage() {
        mPageWidget.abortAnimation();
        mPageWidget.calcCornerXY(screenWidth-10, screenHeight-10);//从右翻页
        pagefactory.onDraw(mCurPageCanvas);
        try {
            pagefactory.nextPage();
            begin = pagefactory.getM_mbBufBegin();// 获取当前阅读位置
            word = pagefactory.getFirstTwoLineText();// 获取当前阅读位置的首行文字
        } catch (IOException e1) {
            Log.e(TAG, "onTouch->nextPage error", e1);
        }
        if (pagefactory.islastPage()) {
            Toast.makeText(mContext, "已经是最后一页了", Toast.LENGTH_SHORT).show();
        } else {
            pagefactory.onDraw(mNextPageCanvas);
            mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);
            editor.putInt(bookPath + "begin", begin).commit();
            MotionEvent e = MotionEvent.obtain(0, 0, MotionEvent.ACTION_MOVE,
                    screenWidth-20, screenHeight-20, 1);
            mPageWidget.doTouchEvent(e);
        }
     //   mTts.startSpeaking(words, mTtsListener);//当前页面结束开始下一阅读页面的语音播放
    }
    /**
    *   设置转换动画  在5.0系统自身已经带有这种效果
    */
    @TargetApi(21)
    private void setupWindowAnimations() {
        Fade fade = new Fade();
        fade.setDuration(500);
        getWindow().setEnterTransition(fade);

        Slide slide = new Slide();
        slide.setDuration(500);
        getWindow().setReturnTransition(fade);
    }

    /**
     * 获取书本存储路径
     * @return
     */
    public static String getBookPath() {
        return bookPath;
    }

}

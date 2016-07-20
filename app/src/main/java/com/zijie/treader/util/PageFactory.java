package com.zijie.treader.util;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Typeface;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.zijie.treader.Config;
import com.zijie.treader.R;
import com.zijie.treader.ReadActivity;
import com.zijie.treader.db.BookCatalogue;
import com.zijie.treader.view.BookPageWidget;

import org.litepal.crud.DataSupport;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by Administrator on 2016/1/3.
 */
public class PageFactory {
    private static final String TAG = "BookPageFactory";

    private StringBuilder word;
    private Context mcontext;
    private File book_file = null;
    private int m_backColor = 0xffff9e85; // 背景颜色
    private Bitmap m_book_bg = null;
    private int m_fontSize ;
//    private int lineSpace = 5;
    private boolean m_isfirstPage, m_islastPage;
    private Vector<String> m_lines = new Vector<String>();
    private MappedByteBuffer m_mbBuf = null;// 内存中的图书字符
    private int m_mbBufBegin = 0;// 当前页起始位置
    private int m_mbBufEnd = 0;// 当前页终点位置

    private static int m_mbBufLen = 0; // 图书总长度
    private static List<String> bookCatalogue = new ArrayList<>();
    private static List<Integer> bookCatalogueStartPos = new ArrayList<>();
    private String m_strCharsetName = "GBK";
    private Boolean isfirstTitle = true;
    int BufEnd = 0;
    private int mstartpos = 0;
    private int m_textColor = Color.rgb(50, 65, 78);
    private static Typeface typeface;
    private int marginHeight ; // 上下与边缘的距离
    private int marginWidth ; // 左右与边缘的距离
    //行间距
    private int lineSpace;
    private int mHeight;
    private int mLineCount; // 每页可以显示的行数
    private Paint mPaint;

    private SimpleDateFormat sdf;
    private String date;
    private DecimalFormat df ;

    private float mVisibleHeight; // 绘制内容的宽
    private float mVisibleWidth; // 绘制内容的宽
    private int mWidth;
    private Intent batteryInfoIntent;

    private Paint mBatterryPaint ;
    private float mBorderWidth;
    private float mBatteryPercentage;
    private RectF rect1 = new RectF();
    private RectF rect2 = new RectF();

    private static PageFactory pageFactory;
    private Config config;
    private BookPageWidget mBookPageWidget;

    private PageFactory(Context context) {
        config = Config.getInstance();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metric = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metric);
        mWidth = metric.widthPixels;
        mHeight = metric.heightPixels;
        mcontext = context.getApplicationContext();
        m_fontSize = (int) context.getResources().getDimension(R.dimen.reading_default_text_size);
        sdf = new SimpleDateFormat("HH:mm");//HH:mm为24小时制,hh:mm为12小时制
        date = sdf.format(new java.util.Date());
        df = new DecimalFormat("#0.0");
        mBorderWidth = context.getResources().getDimension(R.dimen.reading_board_battery_border_width);
        marginWidth = (int) context.getResources().getDimension(R.dimen.readingMarginWidth);
        marginHeight = (int) context.getResources().getDimension(R.dimen.readingMarginHeight);
        lineSpace = (int) context.getResources().getDimension(R.dimen.reading_line_spacing);
        typeface = Typeface.createFromAsset(context.getAssets(), "font/QH.ttf");
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);// 画笔
        mPaint.setTextAlign(Paint.Align.LEFT);// 左对齐
        mPaint.setTextSize(m_fontSize);// 字体大小
        mPaint.setColor(m_textColor);// 字体颜色
        mPaint.setTypeface(typeface);
        mPaint.setSubpixelText(true);// 设置该项为true，将有助于文本在LCD屏幕上的显示效果
        mVisibleWidth = mWidth - marginWidth * 2;
        mVisibleHeight = mHeight - marginHeight * 2;
        mLineCount = (int) (mVisibleHeight / (m_fontSize + lineSpace)); // 可显示的行数,-1是因为底部显示进度的位置容易被遮住
        batteryInfoIntent = context.getApplicationContext().registerReceiver(null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED)) ;//注册广播,随时获取到电池电量信息

        mBatterryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBatterryPaint.setTextSize(CommonUtil.sp2px(context, 12));
        mBatterryPaint.setTypeface(typeface);
        mBatterryPaint.setTextAlign(Paint.Align.LEFT);
        mBatterryPaint.setColor(m_textColor);
        initBg();
    }

    //初始化背景
    private void initBg(){
        if (config.getDayOrNight()) {
            //设置背景
            setBgBitmap(BookPageFactory.decodeSampledBitmapFromResource(
                    mcontext.getResources(), R.drawable.main_bg, mWidth, mHeight));
            //设置字体颜色
            setM_textColor(Color.rgb(128, 128, 128));
        } else {
            Bitmap bmp = Bitmap.createBitmap(mWidth,mHeight, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bmp);
            canvas.drawColor(mcontext.getResources().getColor(R.color.read_background_paperYellow));
            //设置字体颜色
            setM_textColor(mcontext.getResources().getColor(R.color.read_textColor));
            //设置背景
            setBgBitmap(bmp);
        }
    }

    public static synchronized PageFactory getInstance(){
        return pageFactory;
    }

    public static synchronized PageFactory createPageFactory(Context context){
        if (pageFactory == null){
            pageFactory = new PageFactory(context);
        }

        return pageFactory;
    }

    public void setPageWidget(BookPageWidget mBookPageWidget){
        this.mBookPageWidget = mBookPageWidget;

    }

    public void onDraw(Bitmap bitmap) {
        Canvas c = new Canvas(bitmap);
        word = new StringBuilder();
        int size = getM_fontSize();
        mPaint.setTextSize(size);
        // mLineCount =  (int) (mVisibleHeight / size) - 1;
        mPaint.setColor(m_textColor);
        if (m_lines.size() == 0)
            m_lines = pageDown();
        if (m_lines.size() > 0) {
            if (m_book_bg == null) {
                c.drawColor(m_backColor);
            } else {
                c.drawBitmap(m_book_bg, 0, 0, null);
            }

            int y = marginHeight;
            for (String strLine : m_lines) {
                y += m_fontSize + lineSpace;
                c.drawText(strLine, marginWidth, y, mPaint);
                word.append(strLine);
            }
//            ReadActivity.words=word.toString();
            word=null;
        }
        //画进度及时间
        int dateWith = (int) (mBatterryPaint.measureText(date)+mBorderWidth);
        float fPercent = (float) (m_mbBufBegin * 1.0 / m_mbBufLen);
        String strPercent = df.format(fPercent * 100) + "%";
        int nPercentWidth = (int) mBatterryPaint.measureText("999.9%") + 1;  //Paint.measureText直接返回參數字串所佔用的寬度
        c.drawText(strPercent, mWidth - nPercentWidth, mHeight - 10, mBatterryPaint);//x y为坐标值
        c.drawText(date, marginWidth ,mHeight-10, mBatterryPaint);

        // 画电池
        int level = batteryInfoIntent.getIntExtra( "level" , 0 );
        int scale = batteryInfoIntent.getIntExtra("scale", 100);
        mBatteryPercentage = (float) level / scale;
        int rect1Left = marginWidth+dateWith;//电池外框left位置

        //画电池外框
        float width = CommonUtil.convertDpToPixel(mcontext,20) - mBorderWidth;
        float height = CommonUtil.convertDpToPixel(mcontext,10);
        rect1.set(rect1Left, mHeight-height-10,rect1Left + width, mHeight - 10);
        rect2.set(rect1Left + mBorderWidth, mHeight - height + mBorderWidth - 10, rect1Left + width - mBorderWidth, mHeight - mBorderWidth - 10);
        c.save(Canvas.CLIP_SAVE_FLAG);
        c.clipRect(rect2, Region.Op.DIFFERENCE);
        c.drawRect(rect1, mBatterryPaint);
        c.restore();
        //画电量部分
        rect2.left += mBorderWidth;
        rect2.right -= mBorderWidth;
        rect2.right = rect2.left + rect2.width() * mBatteryPercentage;
        rect2.top += mBorderWidth;
        rect2.bottom -= mBorderWidth;
        c.drawRect(rect2, mBatterryPaint);
        //画电池头
        int poleHeight = (int) CommonUtil.convertDpToPixel(mcontext,10) / 2;
        rect2.left = rect1.right;
        rect2.top = rect2.top + poleHeight / 4;
        rect2.right = rect1.right + mBorderWidth;
        rect2.bottom = rect2.bottom - poleHeight/4;
        c.drawRect(rect2, mBatterryPaint);
    }

    /**
     *
     * @param strFilePath
     * @param begin
     *            表示书签记录的位置，读取书签时，将begin值给m_mbBufEnd，在读取nextpage，及成功读取到了书签
     *            记录时将m_mbBufBegin开始位置作为书签记录
     *
     * @throws IOException
     */
    @SuppressWarnings("resource")
    public void openbook(String strFilePath, int begin) throws IOException {
        m_lines.clear();

        m_strCharsetName = getCharset(strFilePath);
        if (m_strCharsetName == null){
            m_strCharsetName = "utf-8";
        }

        book_file = new File(strFilePath);
        long lLen = book_file.length();
        m_mbBufLen = (int) lLen;
        m_mbBuf = new RandomAccessFile(book_file, "r").getChannel().map(FileChannel.MapMode.READ_ONLY, 0, lLen);
        // Log.d(TAG, "total lenth：" + m_mbBufLen);
        // 设置已读进度
        if (begin >= 0) {
            m_mbBufBegin = begin;
            m_mbBufEnd = begin;
        } else {
        }
        setM_fontSize(config.getFontSize());

        new Thread(new Runnable() {
            @Override
            public void run() {
                getBookInfo();
            }
        }).start();

        if (mBookPageWidget != null){
            onDraw(mBookPageWidget.getCurPage());
        }
    }

    /**
     * 获取文件编码
     * @param fileName
     * @return
     * @throws IOException
     */
    public String getCharset(String fileName) throws IOException{
        String charset;
        FileInputStream fis = new FileInputStream(fileName);
        byte[] buf = new byte[4096];
        // (1)
        UniversalDetector detector = new UniversalDetector(null);
        // (2)
        int nread;
        while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
            detector.handleData(buf, 0, nread);
        }
        // (3)
        detector.dataEnd();
        // (4)
        charset = detector.getDetectedCharset();
        // (5)
        detector.reset();

        return charset;
    }

    /**
     * 画指定页的下一页
     *
     * @return 下一页的内容 Vector<String>
     */
    protected Vector<String> pageDown() {
        mPaint.setTextSize(m_fontSize);
        mPaint.setColor(m_textColor);
        String strParagraph = "";
        Vector<String> lines = new Vector<>();
        while (lines.size() < mLineCount && m_mbBufEnd < m_mbBufLen) {
            byte[] paraBuf = readParagraphForward(m_mbBufEnd);
            m_mbBufEnd += paraBuf.length;// 每次读取后，记录结束点位置，该位置是段落结束位置
            try {
                strParagraph = new String(paraBuf, m_strCharsetName);// 转换成制定GBK编码
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "pageDown->转换编码失败", e);
            }
            String strReturn = "";
            // 替换掉回车换行符,防止段落发生错乱
            if (strParagraph.indexOf("\r\n") != -1) {   //windows
                strReturn = "\r\n";
                strParagraph = strParagraph.replaceAll("\r\n","");
            } else if (strParagraph.indexOf("\n") != -1) {    //linux
                strReturn = "\n";
                strParagraph = strParagraph.replaceAll("\n", "");
            }

            if (strParagraph.length() == 0) {
                lines.add(strParagraph);
            }
            while (strParagraph.length() > 0) {
                // 画一行文字
                int nSize = mPaint.breakText(strParagraph, true, mVisibleWidth,
                        null);
                lines.add(strParagraph.substring(0, nSize));
                strParagraph = strParagraph.substring(nSize);// 得到剩余的文字
                // 超出最大行数则不再画
                if (lines.size() >= mLineCount) {
                    break;
                }
            }
            lines.add("\n\n");//段落间加一个空白行
            // 如果该页最后一段只显示了一部分，则重新定位结束点位置
            if (strParagraph.length() != 0) {
                try {
                    m_mbBufEnd -= (strParagraph + strReturn)
                            .getBytes(m_strCharsetName).length;
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "pageDown->记录结束点位置失败", e);
                }
            }
        }

        return lines;
    }

    /**
     *   提取章节目录及值
     */
    public void getBookInfo() {
        String strParagraph = "";
        while (mstartpos < m_mbBufLen-1) {
            byte[] paraBuf = readParagraphForward(mstartpos);
            mstartpos += paraBuf.length;// 每次读取后，记录结束点位置，该位置是段落结束位置
            try {
                strParagraph = new String(paraBuf, m_strCharsetName);// 转换成制定GBK编码
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "pageDown->转换编码失败", e);
            }
            EditText editText;
            String strReturn = "";
            // 替换掉回车换行符,防止段落发生错乱
            if (strParagraph.indexOf("\r\n") != -1) {   //windows
                strReturn = "\r\n";
                strParagraph = strParagraph.replaceAll("\r\n", "");
            } else if (strParagraph.indexOf("\n") != -1) {    //linux
                strReturn = "\n";
                strParagraph = strParagraph.replaceAll("\n", "");
            }

            if(strParagraph.contains("第") && strParagraph.contains("章")) {
                int m_mstartpos = mstartpos-paraBuf.length;//获得章节段落开始位置
                BookCatalogue bookCatalogue1 = new BookCatalogue();//每次保存后都要新建一个
                strParagraph = strParagraph.trim();//去除字符串前后空格
                //去除全角空格
                while (strParagraph.startsWith("　")) {
                    strParagraph = strParagraph.substring(1, strParagraph.length()).trim();
                }
                bookCatalogue.add(strParagraph);   //保存到数组
                bookCatalogueStartPos.add(m_mstartpos);
                bookCatalogue1.setBookCatalogue(strParagraph);  //保存到数据库
                bookCatalogue1.setBookCatalogueStartPos(m_mstartpos);
                bookCatalogue1.setBookpath(ReadActivity.getBookPath());
                String sql = "SELECT id FROM bookcatalogue WHERE bookcatalogue =? and bookCatalogueStartPos =?";
                Cursor cursor = DataSupport.findBySQL(sql,strParagraph,m_mstartpos +"");
                if(!cursor.moveToFirst()) {
                    bookCatalogue1.save();
                }
            }
        }
    }

    /**
     * 得到上上页的结束位置
     */
    protected void pageUp() {
        if (m_mbBufBegin < 0)
            m_mbBufBegin = 0;
        Vector<String> lines = new Vector<String>();
        String strParagraph = "";
        while (lines.size() < mLineCount && m_mbBufBegin > 0) {
            Vector<String> paraLines = new Vector<String>();
            byte[] paraBuf = readParagraphBack(m_mbBufBegin);
            m_mbBufBegin -= paraBuf.length;// 每次读取一段后,记录开始点位置,是段首开始的位置
            try {
                strParagraph = new String(paraBuf, m_strCharsetName);
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "pageUp->转换编码失败", e);
            }
            String strReturn = "";
            strParagraph = strParagraph.replaceAll("\r\n", "");
            strParagraph = strParagraph.replaceAll("\n", "");
            // 如果是空白行，直接添加
            if (strParagraph.length() == 0) {
                lines.add(strParagraph);
            }

            while (strParagraph.length() > 0) {
                // 画一行文字
                int nSize = mPaint.breakText(strParagraph, true, mVisibleWidth,
                        null);
                paraLines.add(strParagraph.substring(0, nSize));
                strParagraph = strParagraph.substring(nSize);

            }
            lines.addAll(0, paraLines);
            lines.add("\n\n");

//            if(lines.size() > mLineCount) {
//                  break;
//            }
        }

        while (lines.size() > mLineCount) {
            try {
                m_mbBufBegin += lines.get(0).getBytes(m_strCharsetName).length;
                lines.remove(0);
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "pageUp->记录起始点位置失败", e);
            }
        }
        m_mbBufEnd = m_mbBufBegin;// 上上一页的结束点等于上一页的起始点
        return;
    }

    /**
     * 向前翻页
     *
     * @throws IOException
     */
    public void prePage() throws IOException {
        if (m_mbBufBegin <= 0) {
            m_mbBufBegin = 0;
            m_isfirstPage = true;
            Toast.makeText(mcontext, "当前是第一页", Toast.LENGTH_SHORT).show();
            return;
        } else {
            m_isfirstPage = false;
        }
        onDraw(mBookPageWidget.getCurPage());

        m_lines.clear();
        pageUp();
        m_lines = pageDown();

        onDraw(mBookPageWidget.getNextPage());
    }

    /**
     * 向后翻页
     *
     * @throws IOException
     */
    public void nextPage() throws IOException {
        if (m_mbBufEnd >= m_mbBufLen) {
            m_islastPage = true;
            Toast.makeText(mcontext, "已经是最后一页了", Toast.LENGTH_SHORT).show();
            return;
        } else {
            m_islastPage = false;
        }
        onDraw(mBookPageWidget.getCurPage());

        m_lines.clear();
        m_mbBufBegin = m_mbBufEnd;// 当前页结束位置作为向前翻页的开始位置
        m_lines = pageDown();
        onDraw(mBookPageWidget.getNextPage());
    }

    public void currentPage() throws IOException {
        m_lines.clear();
        m_lines = pageDown();
    }

    /**
     * 读取指定位置的上一个段落
     *
     * @param nFromPos
     * @return byte[]
     */
    protected byte[] readParagraphBack(int nFromPos) {
        int nEnd = nFromPos;
        int i;
        byte b0, b1;
        if (m_strCharsetName.equals("UTF-16LE")) {
            i = nEnd - 2;
            while (i > 0) {
                b0 = m_mbBuf.get(i);
                b1 = m_mbBuf.get(i + 1);
                if (b0 == 0x0a && b1 == 0x00 && i != nEnd - 2) {
                    i += 2;
                    break;
                }
                i--;
            }

        } else if (m_strCharsetName.equals("UTF-16BE")) {
            i = nEnd - 2;
            while (i > 0) {
                b0 = m_mbBuf.get(i);
                b1 = m_mbBuf.get(i + 1);
                if (b0 == 0x00 && b1 == 0x0a && i != nEnd - 2) {
                    i += 2;
                    break;
                }
                i--;
            }
        } else {
            i = nEnd - 1;
            while (i > 0) {
                b0 = m_mbBuf.get(i);
                if (b0 == 0x0a && i != nEnd - 1) {// 0x0a表示换行符
                    i++;
                    break;
                }
                i--;
            }
        }
        if (i < 0)
            i = 0;
        int nParaSize = nEnd - i;
        int j;
        byte[] buf = new byte[nParaSize];
        for (j = 0; j < nParaSize; j++) {
            buf[j] = m_mbBuf.get(i + j);
        }
        return buf;
    }

    /**
     * 读取指定位置的下一个段落
     *
     * @param nFromPos
     * @return byte[]
     */
    protected byte[] readParagraphForward(int nFromPos) {
        int nStart = nFromPos;
        int i = nStart;
        byte b0, b1;
        // 根据编码格式判断换行
        if (m_strCharsetName.equals("UTF-16LE")) {
            while (i < m_mbBufLen - 1) {
                b0 = m_mbBuf.get(i++);
                b1 = m_mbBuf.get(i++);
                if (b0 == 0x0a && b1 == 0x00) {
                    break;
                }
            }
        } else if (m_strCharsetName.equals("UTF-16BE")) {
            while (i < m_mbBufLen - 1) {
                b0 = m_mbBuf.get(i++);
                b1 = m_mbBuf.get(i++);
                if (b0 == 0x00 && b1 == 0x0a) {
                    break;
                }
            }
        } else {
            while (i < m_mbBufLen) {
                b0 = m_mbBuf.get(i++);
                if (b0 == 0x0a) {
                    break;
                }
            }
        }
        int nParaSize = i - nStart; //段落长度
        byte[] buf = new byte[nParaSize];
        for (i = 0; i < nParaSize; i++) {
            buf[i] = m_mbBuf.get(nFromPos + i);
        }
        return buf;
    }

    /*
    *
    * 保存Bitmap图到sd卡
    * */
    public static String saveImageToGallery(String filename, Bitmap icon) {
        String fullfilepath = null;
        try {
            File path = new File(Environment.getExternalStorageDirectory()
                    +"");
            path.mkdirs();
            File file = new File(path, filename);
            fullfilepath = file.getAbsolutePath();
            Log.d("SF Wallpaper", "Saving image " + file.getAbsolutePath());
            // file.createNewFile();

            FileOutputStream os = new FileOutputStream(file);
            icon.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.close();
        } catch (Exception e) {
            Log.w("ExternalStorage", "Error writing file", e);
        }
        return fullfilepath;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            /**   final int halfHeight = height / 2;
             final int halfWidth = width / 2;

             // Calculate the largest inSampleSize value that is a power of 2 and keeps both
             // height and width larger than the requested height and width.
             while ((halfHeight / inSampleSize) > reqHeight
             && (halfWidth / inSampleSize) > reqWidth) {
             inSampleSize *= 2;
             } */
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都不会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap getResizedBitmap(int reqWidth, int reqHeight,  String imagePath) {

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        //inJustDecodeBounds = true <-- will not load the bitmap into memory
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/reqWidth, photoH/reqHeight);
        //  bmOptions.inSampleSize = calculateInSampleSize(bmOptions, reqWidth, reqHeight);
        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return(BitmapFactory.decodeFile(imagePath, bmOptions).copy(Bitmap.Config.ARGB_4444, true));
    }


    public void setBgBitmap(Bitmap BG) {
        m_book_bg = BG;
    }

    public void setM_fontSize(int m_fontSize) {
        this.m_fontSize = m_fontSize;
        mLineCount = (int) (mVisibleHeight / (m_fontSize + lineSpace));
    }
    public int getM_fontSize() {
        return this.m_fontSize; //2016.1.4
    }
    // 设置页面起始点
    public void setM_mbBufBegin(int m_mbBufBegin) {
        this.m_mbBufBegin = m_mbBufBegin;
    }

    // 设置页面结束点
    public void setM_mbBufEnd(int m_mbBufEnd) {
        this.m_mbBufEnd = m_mbBufEnd;
    }

    public int getM_mbBufBegin() {
        return m_mbBufBegin;
    }

    public String getFirstTwoLineText() {
        return m_lines.size() > 0 ? m_lines.get(0)+m_lines.get(1) : "";

    }

    public int getM_textColor() {
        return m_textColor;
    }

    public void setM_textColor(int m_textColor) {
        this.m_textColor = m_textColor;
    }

    public static int getM_mbBufLen() {
        return m_mbBufLen;
    }

    public int getM_mbBufEnd() {
        return m_mbBufEnd;
    }

    public int getmLineCount() {
        return mLineCount;
    }

    public boolean isfirstPage() {
        return m_isfirstPage;
    }

    public boolean islastPage() {
        return m_islastPage;
    }

    public static List<Integer> getBookCatalogueStartPos() {
        return bookCatalogueStartPos;
    }

    public static List<String> getBookCatalogue() {
        return bookCatalogue;
    }

}

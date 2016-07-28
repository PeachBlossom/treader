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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Created by Administrator on 2016/7/20 0020.
 */
public class PageFactory1 {
    private static final String TAG = "PageFactory";
    private static PageFactory1 pageFactory;

    private Context mContext;
    private Config config;
    //当前的书本
//    private File book_file = null;
    // 默认背景颜色
    private int m_backColor = 0xffff9e85;
    //页面宽
    private int mWidth;
    //页面高
    private int mHeight;
    //文字字体大小
    private float m_fontSize ;
    //时间格式
    private SimpleDateFormat sdf;
    //时间
    private String date;
    //进度格式
    private DecimalFormat df ;
    //电池边界宽度
    private float mBorderWidth;
    // 上下与边缘的距离
    private float marginHeight ;
    // 左右与边缘的距离
    private float marginWidth ;
    //状态栏距离底部高度
    private float statusMarginBottom;
    //行间距
    private float lineSpace;
    //字体
    private Typeface typeface;
    //文字画笔
    private Paint mPaint;
    //文字颜色
    private int m_textColor = Color.rgb(50, 65, 78);
    // 绘制内容的宽
    private float mVisibleHeight;
    // 绘制内容的宽
    private float mVisibleWidth;
    // 每页可以显示的行数
    private int mLineCount;
    //电池画笔
    private Paint mBatterryPaint ;
    //背景图片
    private Bitmap m_book_bg = null;
    //当前显示的文字
    private StringBuilder word = new StringBuilder();
    //当前总共的行
//    private Vector<String> m_lines = new Vector<>();
    // 当前页起始位置
    private int m_mbBufBegin = 0;
    // 当前页终点位置
    private int m_mbBufEnd = 0;
    // 图书总长度
    private long m_mbBufLen = 0;
    private Intent batteryInfoIntent;
    //电池电量百分比
    private float mBatteryPercentage;
    //电池外边框
    private RectF rect1 = new RectF();
    //电池内边框
    private RectF rect2 = new RectF();
    //文件编码
    private String m_strCharsetName = "GBK";
    //当前是否为第一页
    private boolean m_isfirstPage;
    //当前是否为最后一页
    private boolean m_islastPage;
    //书本widget
    private BookPageWidget mBookPageWidget;
    //书本所有段
    List<String> allParagraph;
    //书本所有行
    List<String> allLines;
    //现在的进度
    private float currentProgress;
    //目录
    private List<BookCatalogue> directoryList = new ArrayList<>();
    private String bookPath = "";
    private int currentCharter = 0;

    private PageEvent mPageEvent;

    public static synchronized PageFactory1 getInstance(){
        return pageFactory;
    }

    public static synchronized PageFactory1 createPageFactory(Context context){
        if (pageFactory == null){
            pageFactory = new PageFactory1(context);
        }
        return pageFactory;
    }

    private PageFactory1(Context context) {
        mContext = context.getApplicationContext();
        config = Config.getInstance();
        //获取屏幕宽高
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metric = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metric);
        mWidth = metric.widthPixels;
        mHeight = metric.heightPixels;

        sdf = new SimpleDateFormat("HH:mm");//HH:mm为24小时制,hh:mm为12小时制
        date = sdf.format(new java.util.Date());
        df = new DecimalFormat("#0.0");

        marginWidth = mContext.getResources().getDimension(R.dimen.readingMarginWidth);
        marginHeight = mContext.getResources().getDimension(R.dimen.readingMarginHeight);
        statusMarginBottom = mContext.getResources().getDimension(R.dimen.reading_status_margin_bottom);
        lineSpace = (int) context.getResources().getDimension(R.dimen.reading_line_spacing);
        mVisibleWidth = mWidth - marginWidth * 2;
        mVisibleHeight = mHeight - marginHeight * 2;

        typeface = config.getTypeface();
        m_fontSize = config.getFontSize();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);// 画笔
        mPaint.setTextAlign(Paint.Align.LEFT);// 左对齐
        mPaint.setTextSize(m_fontSize);// 字体大小
        mPaint.setColor(m_textColor);// 字体颜色
        mPaint.setTypeface(typeface);
        mPaint.setSubpixelText(true);// 设置该项为true，将有助于文本在LCD屏幕上的显示效果
        calculateLineCount();

        mBorderWidth = mContext.getResources().getDimension(R.dimen.reading_board_battery_border_width);
        mBatterryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBatterryPaint.setTextSize(CommonUtil.sp2px(context, 12));
        mBatterryPaint.setTypeface(typeface);
        mBatterryPaint.setTextAlign(Paint.Align.LEFT);
        mBatterryPaint.setColor(m_textColor);
        batteryInfoIntent = context.getApplicationContext().registerReceiver(null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED)) ;//注册广播,随时获取到电池电量信息

        initBg(config.getDayOrNight());
    }

    //初始化背景
    private void initBg(Boolean isNight){
        if (isNight) {
            //设置背景
            setBgBitmap(decodeSampledBitmapFromResource(
                    mContext.getResources(), R.drawable.main_bg, mWidth, mHeight));
            //设置字体颜色
            setM_textColor(Color.rgb(128, 128, 128));
        } else {
            //设置背景
            setBookBg(config.getBookBgType());
        }
    }

    private void calculateLineCount(){
        mLineCount = (int) (mVisibleHeight / (m_fontSize + lineSpace));// 可显示的行数
    }

    public void onDraw(Bitmap bitmap,List<String> m_lines) {
        if (getCurrentCharter() > 0) {
            currentCharter = getCurrentCharter();
        }

        Canvas c = new Canvas(bitmap);
        c.drawBitmap(getBgBitmap(), 0, 0, null);
        word.setLength(0);
        mPaint.setTextSize(getFontSize());
        mPaint.setColor(getTextColor());
        if (m_lines.size() == 0) {
            return;
        }

        if (m_lines.size() > 0) {
            float y = marginHeight;
            for (String strLine : m_lines) {
                y += m_fontSize + lineSpace;
                c.drawText(strLine, marginWidth, y, mPaint);
                word.append(strLine);
            }
        }

        //画进度及时间
        int dateWith = (int) (mBatterryPaint.measureText(date)+mBorderWidth);//时间宽度
        float fPercent = (float) (m_mbBufBegin * 1.0 / allLines.size());//进度
        currentProgress = fPercent;
        if (mPageEvent != null){
            mPageEvent.changeProgress(fPercent);
        }
        String strPercent = df.format(fPercent * 100) + "%";//进度文字
        int nPercentWidth = (int) mBatterryPaint.measureText("999.9%") + 1;  //Paint.measureText直接返回參數字串所佔用的寬度
        c.drawText(strPercent, mWidth - nPercentWidth, mHeight - statusMarginBottom, mBatterryPaint);//x y为坐标值
        c.drawText(date, marginWidth ,mHeight - statusMarginBottom, mBatterryPaint);
        // 画电池
        int level = batteryInfoIntent.getIntExtra( "level" , 0 );
        int scale = batteryInfoIntent.getIntExtra("scale", 100);
        mBatteryPercentage = (float) level / scale;
        float rect1Left = marginWidth + dateWith + statusMarginBottom;//电池外框left位置
        //画电池外框
        float width = CommonUtil.convertDpToPixel(mContext,20) - mBorderWidth;
        float height = CommonUtil.convertDpToPixel(mContext,10);
        rect1.set(rect1Left, mHeight - height - statusMarginBottom,rect1Left + width, mHeight - statusMarginBottom);
        rect2.set(rect1Left + mBorderWidth, mHeight - height + mBorderWidth - statusMarginBottom, rect1Left + width - mBorderWidth, mHeight - mBorderWidth - statusMarginBottom);
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
        int poleHeight = (int) CommonUtil.convertDpToPixel(mContext,10) / 2;
        rect2.left = rect1.right;
        rect2.top = rect2.top + poleHeight / 4;
        rect2.right = rect1.right + mBorderWidth;
        rect2.bottom = rect2.bottom - poleHeight/4;
        c.drawRect(rect2, mBatterryPaint);

        mBookPageWidget.postInvalidate();
    }

   //向前翻页
    public void prePage(){
        if (m_mbBufBegin <= 0) {
            m_mbBufBegin = 0;
            m_isfirstPage = true;
            Toast.makeText(mContext, "当前是第一页", Toast.LENGTH_SHORT).show();
            return;
        } else {
            m_isfirstPage = false;
        }
        onDraw(mBookPageWidget.getCurPage(),getPage(m_mbBufBegin,m_mbBufEnd));
        m_mbBufEnd = m_mbBufBegin - 1;
        m_mbBufBegin = m_mbBufBegin - mLineCount;
        onDraw(mBookPageWidget.getNextPage(),getPage(m_mbBufBegin,m_mbBufEnd));

    }

    //向后翻页
    public void nextPage(){
        if (m_mbBufEnd >= allLines.size()) {
            m_islastPage = true;
            Toast.makeText(mContext, "已经是最后一页了", Toast.LENGTH_SHORT).show();
            return;
        } else {
            m_islastPage = false;
        }
        onDraw(mBookPageWidget.getCurPage(),getPage(m_mbBufBegin,m_mbBufEnd));
        m_mbBufBegin = m_mbBufEnd + 1;
        m_mbBufEnd = getEndLine(m_mbBufBegin);
        onDraw(mBookPageWidget.getNextPage(),getPage(m_mbBufBegin,m_mbBufEnd));
        Log.e("nextPage","nextPagenextPagenextPagenextPagenextPagenextPage");
    }

    /**
     * 打开书本
     * @param strFilePath
     * @param begin
     * @throws IOException
     */
    public void openBook(String strFilePath, long begin) throws IOException {
        directoryList.clear();
        currentCharter = 0;
        bookPath = strFilePath;
        m_strCharsetName = getCharset(strFilePath);
        if (m_strCharsetName == null){
            m_strCharsetName = "utf-8";
        }

        String bookStr = transCoding(strFilePath);
        allParagraph = piecewise(bookStr);
        allLines = branch(allParagraph);
        m_mbBufLen = getBookLen();

        if (begin >= 0) {
            m_mbBufBegin = getBeginForLineNum(getLineNum(begin));
            m_mbBufEnd = getEndLine(m_mbBufBegin);
        } else {
            m_mbBufBegin = 0;
            m_mbBufEnd = getEndLine(m_mbBufEnd);
        }

        if (mBookPageWidget != null){
            currentPage();
        }
    }

    //根据开始位置和结束位置获取页面
    public List<String> getPage(int begin,int end){
        if (allLines == null){
            return null;
        }
        List<String> lines = new ArrayList<>();
        for (int i = begin; i <= end;i++ ){
            if (allLines.size() > i) {
                lines.add(allLines.get(i));
            }else{
                break;
            }
        }
        return lines;
    }

    //获取图书总长度
    public long getBookLen(){
        if (allLines == null){
            return 0;
        }
        long len = 0;
        for (String line : allLines){
            len += line.length();
        }

        return len;
    }

    //根据行数获取当前页面开始的行
    public int getBeginForLineNum(int num){
        int begin = 0;
        begin = num % mLineCount;
        return num - begin;
    }

    //根据页面开始行获取结束行
    public int getEndLine(int begin){
        int end = begin + mLineCount -1;
        return end;
    }

    //根据长度获取行数
    public int getLineNum(long len){
        if (allLines == null){
            return 0;
        }

        long lenth = 0;
        int num = 0;
        for (int i = 0;i <  allLines.size();i++){
            lenth += allLines.get(i).length();
            if (lenth >= len){
                num = i;
                break;
            }
        }
        return num;
    }

    //转码
    public String transCoding(String filePath) throws IOException {
        File book_file = new File(filePath);
        long lLen = book_file.length();
        MappedByteBuffer m_mbBuf = new RandomAccessFile(book_file, "r").getChannel().map(FileChannel.MapMode.READ_ONLY, 0, lLen);
        byte[] paraBuf = new byte[(int) lLen];
        ByteBuffer bb = m_mbBuf.get(paraBuf);
        Charset cs = Charset.forName(m_strCharsetName);
        bb.flip();
        CharBuffer cb = cs.decode(bb);
        return cb.toString();
    }

    //分段
    public List<String> piecewise(String data){
        if (data == null){
            return null;
        }
        ArrayList<String> list = new ArrayList<>();
        String pgStr = "";
        if (data.indexOf("\r\n") != -1){
            pgStr = "\r\n";
        }else if (data.indexOf("\n") != -1){
            pgStr = "\n";
        }else if (data.indexOf("\r") != -1){
            pgStr = "\r";
        }
        if (!pgStr.isEmpty()){
            String[] paragraphs = data.split(pgStr);
            for (String paragraph : paragraphs){
                paragraph = paragraph.trim();
                //每段缩进首行缩进两个汉字的距离
                if (paragraph.startsWith("\u3000")) {
                    list.add(paragraph);
                }else if (!paragraph.trim().isEmpty()){
                    list.add("\u3000\u3000" + paragraph);
                }
            }
        }else{
            list .add(data);
        }

        return list;
    }

    //分行
    public List<String> branch(List<String> list){
        if (list == null){
            return null;
        }
        List<String> allLines = new ArrayList<>();
        for (String paragraph : list){
            //分章
            if (paragraph.matches(".*第.{1,8}章.*")){
                Integer sizeL = new Integer(allLines.size() + 1);
                BookCatalogue bookCatalogue = new BookCatalogue();
                bookCatalogue.setBookCatalogueStartPos(sizeL);
                bookCatalogue.setBookCatalogue(paragraph);
                bookCatalogue.setBookpath(bookPath);
                directoryList.add(bookCatalogue);
            }

            List<String> lines = separateParagraphtoLines(paragraph);
            allLines.addAll(lines);
            //每段结尾加一个空行
            allLines.add("");
        }
        return allLines;
    }

    //每段分行
    public List<String> separateParagraphtoLines(String paragraphstr) {
        List<String> linesdata = new ArrayList<>();
        String str = paragraphstr;
        for (; str.length() > 0;) {
            int nums = mPaint.breakText(str, true, mVisibleWidth, null);
            if (nums <= str.length()) {
                String linnstr = str.substring(0, nums);
                linesdata.add(linnstr);
                str = str.substring(nums, str.length());
            } else {
                linesdata.add(str);
                str = "";
            }

        }
        return linesdata;
    }

    //上一章
    public void preChapter(){
        if (directoryList.size() > 0){
            int num = currentCharter;
            num --;
            if (num >= 0){
                m_mbBufBegin = getBeginForLineNum(directoryList.get(num).getBookCatalogueStartPos());
                m_mbBufEnd = getEndLine(m_mbBufBegin);
                currentPage();
                currentCharter = num;
            }
        }
    }

    //下一章
    public void nextChapter(){
        int num = currentCharter;
        num ++;
        if (num < directoryList.size()){
            m_mbBufBegin = getBeginForLineNum(directoryList.get(num).getBookCatalogueStartPos());
            m_mbBufEnd = getEndLine(m_mbBufBegin);
            currentPage();
            currentCharter = num;
        }
    }

    //获取现在的章
    public int getCurrentCharter(){
        int num = 0;
        for (int i = 0;directoryList.size() > i;i++){
            BookCatalogue bookCatalogue = directoryList.get(i);
            if (m_mbBufBegin <= bookCatalogue.getBookCatalogueStartPos() && m_mbBufEnd >= bookCatalogue.getBookCatalogueStartPos()){
                num = i;
                break;
            }
        }
        return num;
    }

    //绘制当前页面
    public void currentPage(){
        onDraw(mBookPageWidget.getCurPage(),getPage(m_mbBufBegin,m_mbBufEnd));
        onDraw(mBookPageWidget.getNextPage(),getPage(m_mbBufBegin,m_mbBufEnd));
    }

    //改变进度
    public void changeProgress(float progress){
        long begin = (long) (m_mbBufLen * progress);
        m_mbBufBegin = getBeginForLineNum(getLineNum(begin));
        m_mbBufEnd = getEndLine(m_mbBufBegin);
        currentPage();
    }

    //改变进度
    public void changeChapter(int lineNum){
        m_mbBufBegin = getBeginForLineNum(lineNum);
        m_mbBufEnd = getEndLine(m_mbBufBegin);
        currentPage();
    }

    //改变字体大小
    public void changeFontSize(int fontSize){
        this.m_fontSize = fontSize;
        mPaint.setTextSize(m_fontSize);
        calculateLineCount();
        allLines = null;
        allLines = branch(allParagraph);
        long begin = (long) (m_mbBufLen * currentProgress);
        m_mbBufBegin = getBeginForLineNum(getLineNum(begin));
        m_mbBufEnd = getEndLine(m_mbBufBegin);
        currentPage();
    }

    //改变字体
    public void changeTypeface(Typeface typeface){
        this.typeface = typeface;
        mPaint.setTypeface(typeface);
        allLines = null;
        allLines = branch(allParagraph);
        long begin = (long) (m_mbBufLen * currentProgress);
        m_mbBufBegin = getBeginForLineNum(getLineNum(begin));
        m_mbBufEnd = getEndLine(m_mbBufBegin);
        currentPage();
    }

    public void changeBookBg(int type){
        setBookBg(type);
        currentPage();
    }

    //设置页面的背景
    public void setBookBg(int type){
        Bitmap bitmap = Bitmap.createBitmap(mWidth,mHeight, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        int color = 0;
        switch (type){
            case Config.BOOK_BG_DEFAULT:
                bitmap = BookPageFactory.decodeSampledBitmapFromResource(
                        mContext.getResources(), R.drawable.paper, mWidth, mHeight);
                color = mContext.getResources().getColor(R.color.read_font_default);
                break;
            case Config.BOOK_BG_1:
                canvas.drawColor(mContext.getResources().getColor(R.color.read_bg_1));
                color = mContext.getResources().getColor(R.color.read_font_1);
                break;
            case Config.BOOK_BG_2:
                canvas.drawColor(mContext.getResources().getColor(R.color.read_bg_2));
                color = mContext.getResources().getColor(R.color.read_font_2);
                break;
            case Config.BOOK_BG_3:
                canvas.drawColor(mContext.getResources().getColor(R.color.read_bg_3));
                color = mContext.getResources().getColor(R.color.read_font_3);
                break;
            case Config.BOOK_BG_4:
                canvas.drawColor(mContext.getResources().getColor(R.color.read_bg_4));
                color = mContext.getResources().getColor(R.color.read_font_4);
                break;
        }
        setBgBitmap(bitmap);
        //设置字体颜色
        setM_textColor(color);
    }

    //设置日间或者夜间模式
    public void setDayOrNight(Boolean isNgiht){
        initBg(isNgiht);
        currentPage();
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

    public void clear(){
        directoryList.clear();
        currentCharter = 0;
        bookPath = "";
        mBookPageWidget = null;
        mPageEvent = null;
        if (allLines != null){
            allLines.clear();
        }
        if (allParagraph != null){
            allParagraph.clear();
        }
    }

    public List<BookCatalogue> getDirectoryList(){
        return directoryList;
    }

    public String getBookPath(){
        return bookPath;
    }
    //是否是第一页
    public boolean isfirstPage() {
        return m_isfirstPage;
    }
    //是否是最后一页
    public boolean islastPage() {
        return m_islastPage;
    }
    //设置页面背景
    public void setBgBitmap(Bitmap BG) {
        m_book_bg = BG;
    }
    //设置页面背景
    public Bitmap getBgBitmap() {
        return m_book_bg;
    }
    //设置文字颜色
    public void setM_textColor(int m_textColor) {
        this.m_textColor = m_textColor;
    }
    //获取文字颜色
    public int getTextColor() {
        return this.m_textColor;
    }
    //获取文字大小
    public float getFontSize() {
        return this.m_fontSize;
    }

    public void setPageWidget(BookPageWidget mBookPageWidget){
        this.mBookPageWidget = mBookPageWidget;
    }

    public void setPageEvent(PageEvent pageEvent){
        this.mPageEvent = pageEvent;
    }

    public interface PageEvent{
        void changeProgress(float progress);
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

}

package com.zijie.treader;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.example.jreader.adapter.MyPagerAdapter;
import com.example.jreader.database.BookCatalogue;
import com.example.jreader.util.CommonUtil;

/**
 * Created by Administrator on 2016/1/6.
 */
public class MarkActivity extends FragmentActivity implements View.OnClickListener {

    private PagerSlidingTabStrip pagerSlidingTabStrip;
    private DisplayMetrics dm;
    private ImageButton button_back;
    private TextView title;
    private static String bookpath_intent,bookname_intent;
    private Typeface typeface;

    @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.markactivity);
        dm = getResources().getDisplayMetrics();
        typeface = Typeface.createFromAsset(this.getAssets(),"font/QH.ttf");
        button_back = (ImageButton) findViewById(R.id.back);
        title = (TextView) findViewById(R.id.bookname);
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        pagerSlidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        setTabsValue();
        Intent intent = getIntent();
        bookpath_intent = intent.getStringExtra("bookpath");
        bookname_intent = intent.getStringExtra("bookname");
        viewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        pagerSlidingTabStrip.setViewPager(viewPager);
        button_back.setOnClickListener(this);
        title.setText(bookname_intent);
        title.setTypeface(typeface);
    }

    private void setTabsValue() {
        // 设置Tab是自动填充满屏幕的
        pagerSlidingTabStrip.setShouldExpand(true);//所有初始化要在setViewPager方法之前
        // 设置Tab的分割线是透明的
        pagerSlidingTabStrip.setDividerColor(Color.TRANSPARENT);
        // 设置Tab底部线的高度
        pagerSlidingTabStrip.setUnderlineHeight((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 1, dm));
        // 设置Tab Indicator的高度
        pagerSlidingTabStrip.setIndicatorHeight((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 4, dm));
        // 设置Tab标题文字的大小
        pagerSlidingTabStrip.setTextSize((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 16, dm));
        //设置Tab标题文字的字体
        pagerSlidingTabStrip.setTypeface(typeface,0);
        // 设置Tab Indicator的颜色
        pagerSlidingTabStrip.setIndicatorColor(Color.parseColor("#45c01a"));
        // 设置选中Tab文字的颜色 (这是我自定义的一个方法)
    //    pagerSlidingTabStrip.setSelectedTextColor(Color.parseColor("#45c01a"));
        // 取消点击Tab时的背景色
        pagerSlidingTabStrip.setTabBackground(0);

       // pagerSlidingTabStrip.setDividerPadding(18);
    }


    @Override
    public void onClick (View view) {
        Intent upIntent = NavUtils.getParentActivityIntent(this);
        if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
            TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent)
                    .startActivities();
        } else {
            upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            NavUtils.navigateUpTo(this, upIntent);
        }
        finish();

    }

    public static String getBookpath_intent(){
        return bookpath_intent;
    }

}

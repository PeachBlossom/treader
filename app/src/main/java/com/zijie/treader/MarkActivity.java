package com.zijie.treader;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import com.astuetz.PagerSlidingTabStrip;
import com.zijie.treader.adapter.MyPagerAdapter;
import com.zijie.treader.base.BaseActivity;
import com.zijie.treader.db.BookCatalogue;
import com.zijie.treader.util.FileUtils;
import com.zijie.treader.util.PageFactory;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/1/6.
 */
public class MarkActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.appbar)
    AppBarLayout appbar;
    @Bind(R.id.tabs)
    PagerSlidingTabStrip tabs;
    @Bind(R.id.pager)
    ViewPager pager;

//    @Bind(R.id.lv_catalogue)
//    ListView lv_catalogue;

    private PageFactory pageFactory;
    private Config config;
    private Typeface typeface;
    private ArrayList<BookCatalogue> catalogueList = new ArrayList<>();
    private DisplayMetrics dm;

    @Override
    public int getLayoutRes() {
        return R.layout.activity_mark;
    }

    @Override
    protected void initData() {
        pageFactory = PageFactory.getInstance();
        config = Config.getInstance();
        dm = getResources().getDisplayMetrics();
        typeface = config.getTypeface();

        setSupportActionBar(toolbar);
        //设置导航图标
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(FileUtils.getFileName(pageFactory.getBookPath()));
        }

        setTabsValue();
        pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager(),pageFactory.getBookPath()));
        tabs.setViewPager(pager);
    }

    private void setTabsValue() {
        // 设置Tab是自动填充满屏幕的
        tabs.setShouldExpand(true);//所有初始化要在setViewPager方法之前
        // 设置Tab的分割线是透明的
        tabs.setDividerColor(Color.TRANSPARENT);
        // 设置Tab底部线的高度
        tabs.setUnderlineHeight((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 1, dm));
        // 设置Tab Indicator的高度
        tabs.setIndicatorHeight((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 2, dm));
        // 设置Tab标题文字的大小
        tabs.setTextSize((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 16, dm));
        //设置Tab标题文字的字体
        tabs.setTypeface(typeface,0);
        // 设置Tab Indicator的颜色
        tabs.setIndicatorColor(getResources().getColor(R.color.colorAccent));
        // 取消点击Tab时的背景色
        tabs.setTabBackground(0);

        // pagerSlidingTabStrip.setDividerPadding(18);
    }

    @Override
    protected void initListener() {

    }

}

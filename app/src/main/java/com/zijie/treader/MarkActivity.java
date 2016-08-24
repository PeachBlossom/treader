package com.zijie.treader;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.zijie.treader.adapter.CatalogueAdapter;
import com.zijie.treader.base.BaseActivity;
import com.zijie.treader.db.BookCatalogue;
import com.zijie.treader.util.FileUtils;
import com.zijie.treader.util.PageFactory;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2016/1/6.
 */
public class MarkActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.appbar)
    AppBarLayout appbar;
    @Bind(R.id.lv_catalogue)
    ListView lv_catalogue;

    PageFactory pageFactory;
    ArrayList<BookCatalogue> catalogueList = new ArrayList<>();

    @Override
    public int getLayoutRes() {
        return R.layout.activity_mark;
    }

    @Override
    protected void initData() {
        pageFactory = PageFactory.getInstance();

        setSupportActionBar(toolbar);
        //设置导航图标
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(FileUtils.getFileName(pageFactory.getBookPath()));
        }

        catalogueList.addAll(pageFactory.getDirectoryList());
        CatalogueAdapter catalogueAdapter = new CatalogueAdapter(this, catalogueList);
        catalogueAdapter.setCharter(pageFactory.getCurrentCharter());
        lv_catalogue.setAdapter(catalogueAdapter);
        catalogueAdapter.notifyDataSetChanged();

//        tv_bookname.setText(FileUtils.getFileName(pageFactory.getBookPath()));
    }

    @Override
    protected void initListener() {
        lv_catalogue.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pageFactory.changeChapter(catalogueList.get(position).getBookCatalogueStartPos());
                MarkActivity.this.finish();
            }
        });
    }

}

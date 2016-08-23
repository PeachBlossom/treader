package com.zijie.treader;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.zijie.treader.adapter.CatalogueAdapter;
import com.zijie.treader.base.BaseActivity;
import com.zijie.treader.db.BookCatalogue;
import com.zijie.treader.util.FileUtils;
import com.zijie.treader.util.PageFactory;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2016/1/6.
 */
public class MarkActivity extends BaseActivity {

    @Bind(R.id.btn_back)
    ImageButton btn_back;
    @Bind(R.id.tv_bookname)
    TextView tv_bookname;
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
        catalogueList.addAll(pageFactory.getDirectoryList());
        CatalogueAdapter catalogueAdapter = new CatalogueAdapter(this,catalogueList);
        lv_catalogue.setAdapter(catalogueAdapter);
        catalogueAdapter.notifyDataSetChanged();

        tv_bookname.setText(FileUtils.getFileName(pageFactory.getBookPath()));
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

    @OnClick(R.id.btn_back)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_back:
                finish();
                break;
        }
    }

}

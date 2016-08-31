package com.zijie.treader.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.zijie.treader.R;
import com.zijie.treader.adapter.MarkAdapter;
import com.zijie.treader.base.BaseFragment;
import com.zijie.treader.db.BookMarks;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/8/31 0031.
 */
public class BookMarkFragment extends BaseFragment {
    public static final String ARGUMENT = "argument";

    @Bind(R.id.lv_catalogue)
    ListView lv_catalogue;

    private String bookpath;
    private String mArgument;
    private List<BookMarks> bookMarksList;
    private MarkAdapter markAdapter;

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_bookmark;
    }

    @Override
    protected void initData(View view) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            bookpath = bundle.getString(ARGUMENT);
        }
        bookMarksList = new ArrayList<>();
        bookMarksList = DataSupport.where("bookpath = ?", bookpath).find(BookMarks.class);
        markAdapter = new MarkAdapter(getActivity(), bookMarksList);
        lv_catalogue.setAdapter(markAdapter);
    }

    @Override
    protected void initListener() {

    }

    /**
     * 用于从Activity传递数据到Fragment
     * @param bookpath
     * @return
     */
    public static BookMarkFragment newInstance(String bookpath)
    {
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT, bookpath);
        BookMarkFragment bookMarkFragment = new BookMarkFragment();
        bookMarkFragment.setArguments(bundle);
        return bookMarkFragment;
    }

}

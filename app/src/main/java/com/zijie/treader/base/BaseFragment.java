package com.zijie.treader.base;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/8/31 0031.
 */
public abstract class BaseFragment extends Fragment {

    private View rootView;
    /**
     * 初始化布局
     */
    protected abstract int getLayoutRes();

    protected abstract void initData(View view);

    protected abstract void initListener();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutRes(), container, false);
        rootView = view;
        // 初始化View注入
        ButterKnife.bind(this,view);
        initData(view);
        initListener();
        return view;
    }

    public View getRootView(){
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}

package com.zijie.treader.adapter;




import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Administrator on 2016/1/12.
 */
public class MyPagerAdapter extends FragmentPagerAdapter {
//    private BookMarkFragment bookMarkFragment;
//    private CatalogueFragment catalogueFragment;
//    private NotesFragment notesFragment;
    private final String[] titles = { "目录", "书签", "笔记" };

    public MyPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public Fragment getItem(int position) {
//        switch (position) {
//            case 0:
//                if (catalogueFragment == null) {
//                    //  bookMarkFragment = new BookMarkFragment();
//                    //创建bookMarkFragment实例时同时把需要intent中的值传入
//                    catalogueFragment = CatalogueFragment.newInstance(MarkActivity.getBookpath_intent());
//                   // bookMarkFragment = BookMarkFragment.newInstance(MarkActivity.getBookpath_intent());
//                }
//                return catalogueFragment;
//
//            case 1:
//                if (bookMarkFragment == null) {
//                    //catalogueFragment = new CatalogueFragment();
//                  //  catalogueFragment = CatalogueFragment.newInstance(MarkActivity.getBookpath_intent());
//                    bookMarkFragment = BookMarkFragment.newInstance(MarkActivity.getBookpath_intent());
//                }
//                return bookMarkFragment;
//            case 2:
//                if (notesFragment == null) {
//                    notesFragment = new NotesFragment();
//                }
//                return notesFragment;
//            default:
//                return null;
//        }
        return null;
    }

}

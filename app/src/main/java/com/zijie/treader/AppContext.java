package com.zijie.treader;

import com.zijie.treader.db.BookList;

import org.litepal.LitePalApplication;
import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * Created by Administrator on 2016/7/8 0008.
 */
public class AppContext extends LitePalApplication {

    private List<BookList> books;

    @Override
    public void onCreate() {
        super.onCreate();
        LitePalApplication.initialize(this);
    }

    public List<BookList> getBookList(){
        if (books == null){
            books = DataSupport.findAll(BookList.class);
        }
        return books;
    }

    public boolean addBookList(){

        return true;
    }
}

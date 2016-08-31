package com.zijie.treader.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2015/12/27.
 */
public class BookMarks extends DataSupport {
    private int id ;
  //  private int page;
    private long begin; // 书签记录页面的结束点位置
  //  private int count;
    private String text;
    private String time;
    private String bookpath;
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTime() {
        return this.time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getBegin() {
        return this.begin;
    }

    public void setBegin(long begin) {
        this.begin = begin;
    }

    public String getBookpath() {
        return this.bookpath;
    }

    public void setBookpath(String bookpath) {
        this.bookpath = bookpath;
    }

}

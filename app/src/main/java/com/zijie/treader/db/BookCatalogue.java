package com.zijie.treader.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Lxq on 2016/4/9.
 */
public class BookCatalogue extends DataSupport {
    private int id;
    private String bookpath;
    private String bookCatalogue;
    private long bookCatalogueStartPos;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getBookCatalogue() {
        return this.bookCatalogue;
    }

    public void setBookCatalogue(String bookCatalogue) {
        this.bookCatalogue = bookCatalogue;
    }

    public String getBookpath() {
        return bookpath;
    }

    public void setBookpath(String ebookpath) {
        this.bookpath = ebookpath;
    }

    public long getBookCatalogueStartPos() {
        return bookCatalogueStartPos;
    }

    public void setBookCatalogueStartPos(long bookCatalogueStartPos) {
        this.bookCatalogueStartPos = bookCatalogueStartPos;
    }
}


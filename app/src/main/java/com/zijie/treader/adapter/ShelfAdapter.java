package com.zijie.treader.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zijie.treader.R;
import com.zijie.treader.db.BookList;
import com.zijie.treader.view.DragGridListener;
import com.zijie.treader.view.DragGridView;

import org.litepal.crud.DataSupport;
import org.litepal.exceptions.DataSupportException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2015/12/17.
 */
public class ShelfAdapter extends BaseAdapter implements DragGridListener {
    private Context mContex;
    private List<BookList> bilist;
    private static LayoutInflater inflater = null;
    private String booKpath,bookname;
    private int mHidePosition = -1;
    private Typeface typeface;
    protected List<AsyncTask<Void, Void, Boolean>> myAsyncTasks = new ArrayList<AsyncTask<Void, Void, Boolean>>();
    private int[] firstLocation;
    public ShelfAdapter(Context context, List<BookList> bilist){
        this.mContex = context;
        this.bilist = bilist;
        typeface = Typeface.createFromAsset(mContex.getAssets(),"font/QH.ttf");
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public ShelfAdapter(Context context){
        this.mContex = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub

        if(bilist.size()<10){
            return 10; //背景书架的draw需要用到item的高度
        }else{

            return bilist.size();
        }
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return bilist.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View contentView, ViewGroup arg2) {
        // TODO Auto-generated method stub
        final ViewHolder viewHolder;
        if (contentView == null) {
            contentView = inflater.inflate(R.layout.shelfitem, null);
            viewHolder = new ViewHolder();
            viewHolder.view = (TextView) contentView.findViewById(R.id.imageView1);
            viewHolder.view.setTypeface(typeface);
            viewHolder.deleteItem_IB = (ImageButton) contentView.findViewById(R.id.item_close_Im);
            contentView.setTag(viewHolder);

            }
             else {
            viewHolder = (ViewHolder) contentView.getTag();
            }

           if (bilist.size() == 0) {
           //   viewHolder.view.setBackgroundResource(R.drawable.cover_default_new);
              viewHolder.view.setClickable(false);
              viewHolder.view.setVisibility(View.INVISIBLE);
              viewHolder.deleteItem_IB.setVisibility(View.INVISIBLE);

            } else {
                if(bilist.size()>position){

                 //   viewHolder.view.setBackgroundResource(R.drawable.cover_default_new);
                    final String fileName = bilist.get(position).getBookname();
                    final String filePath = bilist.get(position).getBookpath();
                    viewHolder.view.setText(fileName);

                    if (DragGridView.getShowDeleteButton()) {
                        viewHolder.deleteItem_IB.setVisibility(View.VISIBLE);
                    }else {
                        viewHolder.deleteItem_IB.setVisibility(View.INVISIBLE);
                    }
                    bookname = fileName;
                    booKpath = filePath;

                    if(position == mHidePosition){
                        contentView.setVisibility(View.INVISIBLE);
                    }else {
                        contentView.setVisibility(View.VISIBLE);//DragGridView  解决复用问题

                    }

                }else {
                 //   viewHolder.view.setBackgroundResource(R.drawable.cover_default_new);
                    viewHolder.view.setClickable(false);
                    viewHolder.deleteItem_IB.setClickable(false);
                    viewHolder.view.setVisibility(View.GONE);
                    viewHolder.deleteItem_IB.setVisibility(View.GONE);

                }

            }

        return contentView;
    }

    class ViewHolder {
        ImageButton deleteItem_IB;
        TextView view;
    }

    /**
     * Drag移动时item交换数据,并在数据库中更新交换后的位置数据
     * @param oldPosition
     * @param newPosition
     */
    @Override
    public void reorderItems(int oldPosition, int newPosition) {

        BookList temp = bilist.get(oldPosition);
        List<BookList> bookLists1 = new ArrayList<>();
        bookLists1 = DataSupport.findAll(BookList.class);

        int tempId = bookLists1.get(newPosition).getId();
       // Log.d("oldposotion is",oldPosition+"");
       // Log.d("newposotion is", newPosition + "");
        if(oldPosition < newPosition){
            for(int i=oldPosition; i<newPosition; i++){
                //获得交换前的ID,必须是数据库的真正的ID，如果使用bilist获取id是错误的，因为bilist交换后id是跟着交换的
                List<BookList> bookLists = new ArrayList<>();
                bookLists = DataSupport.findAll(BookList.class);
                int dataBasesId = bookLists.get(i).getId();
                Collections.swap(bilist, i, i + 1);

                updateBookPosition(i,dataBasesId, bilist);

            }
        }else if(oldPosition > newPosition){
            for(int i=oldPosition; i>newPosition; i--) {
                List<BookList> bookLists = new ArrayList<>();
                bookLists = DataSupport.findAll(BookList.class);
                int dataBasesId = bookLists.get(i).getId();

                Collections.swap(bilist, i, i - 1);

                updateBookPosition(i,dataBasesId,bilist);

            }
        }

        bilist.set(newPosition, temp);
        updateBookPosition(newPosition, tempId, bilist);

    }

    /**
     * 两个item数据交换结束后，把不需要再交换的item更新到数据库中
     * @param position
     * @param bookLists
     */
    public void updateBookPosition (int position,int databaseId,List<BookList> bookLists) {
        BookList bookList = new BookList();
        BookList bookList1 = new BookList();
        String bookpath = bookLists.get(position).getBookpath();
        String bookname = bookLists.get(position).getBookname();
        bookList.setBookpath(bookpath);
        bookList1.setBookname(bookname);
        //开线程保存改动的数据到数据库
        //使用litepal数据库框架update时每次只能update一个id中的一条信息，如果相同则不更新。
        upDateBookToSqlite3(databaseId , bookList, bookList1);
    }

    /**
     * 隐藏item
     * @param hidePosition
     */
    @Override
    public void setHideItem(int hidePosition) {
        this.mHidePosition = hidePosition;
        notifyDataSetChanged();
    }

    /**
     * 删除书本
     * @param deletePosition
     */
    @Override
    public void removeItem(int deletePosition) {

        String bookpath = bilist.get(deletePosition).getBookpath();
        DataSupport.deleteAll(BookList.class, "bookpath = ?", bookpath);
        bilist.remove(deletePosition);
       // Log.d("删除的书本是", bookpath);

        notifyDataSetChanged();

    }

    /**
     * Book打开后位置移动到第一位
     * @param openPosition
     */
    @Override
    public void setItemToFirst(int openPosition) {

        List<BookList> bookLists1 = new ArrayList<>();
        bookLists1 = DataSupport.findAll(BookList.class);
        int tempId = bookLists1.get(0).getId();
        BookList temp = bookLists1.get(openPosition);
       // Log.d("setitem adapter ",""+openPosition);
        if(openPosition!=0) {
            for (int i = openPosition; i > 0 ; i--) {
                List<BookList> bookListsList = new ArrayList<>();
                bookListsList = DataSupport.findAll(BookList.class);
                int dataBasesId = bookListsList.get(i).getId();

               Collections.swap(bookLists1, i, i - 1);
               updateBookPosition(i, dataBasesId, bookLists1);
            }

            bookLists1.set(0, temp);
            updateBookPosition(0, tempId, bookLists1);
            for (int j = 0 ;j<bookLists1.size();j++) {
                String bookpath = bookLists1.get(j).getBookpath();
              //  Log.d("移动到第一位",bookpath);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void nitifyDataRefresh() {
        notifyDataSetChanged();
    }

    public void putAsyncTask(AsyncTask<Void, Void, Boolean> asyncTask) {
        myAsyncTasks.add(asyncTask.execute());
    }

    /**
     * 数据库书本信息更新
     * @param databaseId  要更新的数据库的书本ID
     * @param bookList
     * @param bookList1
     */
    public void upDateBookToSqlite3(final int databaseId,final BookList bookList,final BookList bookList1) {

        putAsyncTask(new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {

            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    bookList.update(databaseId);
                    bookList1.update(databaseId);

                } catch (DataSupportException e) {
                    return false;
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (result) {

                } else {
                    Log.d("保存到数据库结果-->", "失败");
                }
            }
        });
    }

}

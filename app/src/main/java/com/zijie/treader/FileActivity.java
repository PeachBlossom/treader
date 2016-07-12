package com.zijie.treader;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zijie.treader.adapter.FileAdapter;
import com.zijie.treader.base.BaseActivity;
import com.zijie.treader.db.BookList;
import com.zijie.treader.util.FileUtils;
import com.zijie.treader.util.Fileutil;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/7/11 0011.
 */
public class FileActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.btn_choose_all)
    Button btnChooseAll;
    @Bind(R.id.btn_delete)
    Button btnDelete;
    @Bind(R.id.btn_add_file)
    Button btnAddFile;
    @Bind(R.id.lv_file_drawer)
    ListView lvFileDrawer;

    public static final int EXTERNAL_STORAGE_REQ_CODE = 10 ;

    //文件根目录
    private File root;
    private List<File> listFile = new ArrayList<>();
    private static FileAdapter adapter;

    @Override
    public int getLayoutRes() {
        return R.layout.activity_file;
    }

    @Override
    protected void initData() {
        getWindow().setBackgroundDrawable(null);
        setSupportActionBar(toolbar);
        //设置导航图标
        toolbar.setNavigationIcon(R.mipmap.return_button);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle("导入图书");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission(FileActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, EXTERNAL_STORAGE_REQ_CODE,"添加图书需要此权限，请允许");
        }
        root = Environment.getExternalStorageDirectory();

        adapter = new FileAdapter(this, listFile);
        lvFileDrawer.setAdapter(adapter);
        searchFile1();
    }

    @Override
    protected void initListener() {
        lvFileDrawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                adapter.setSelectedPosition(position);
            }
        });

        lvFileDrawer.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return true;
            }
        });

        adapter.setCheckedChangeListener(new FileAdapter.CheckedChangeListener() {
            @Override
            public void onCheckedChanged(int position, CompoundButton buttonView, boolean isChecked) {
                setAddFileText(adapter.getCheckNum());
            }
        });
        //全选
        btnChooseAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.checkAll();
            }
        });
        //取消选择
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.cancel();
            }
        });
        //把已经选择的书加入书架
        btnAddFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBookList();
            }
        });
    }

    //保存选择的txt文件
    private void saveBookList(){
        List<File> files = adapter.getCheckFiles();
        if (files.size() > 0) {
            List<BookList> bookLists = new ArrayList<BookList>();
            for (File file : files) {
                BookList bookList = new BookList();
                String bookName = Fileutil.getFileNameNoEx(file.getName());
                bookList.setBookname(bookName);
                bookList.setBookpath(file.getAbsolutePath());
                bookLists.add(bookList);
            }
            new saveBookToSqlLiteTask().execute(bookLists);
        }
    }

    private class saveBookToSqlLiteTask extends AsyncTask<List<BookList>,Void,Integer>{
        private static final int FAIL = 0;
        private static final int SUCCESS = 1;
        private static final int REPEAT = 2;
        private BookList repeatBookList;

        @Override
        protected Integer doInBackground(List<BookList>... params) {
            List<BookList> bookLists = params[0];
            for (BookList bookList : bookLists){
                List<BookList> books = DataSupport.where("bookpath = ?", bookList.getBookpath()).find(BookList.class);
                if (books.size() > 0){
                    repeatBookList = bookList;
                    return REPEAT;
                }
            }

            try {
                DataSupport.saveAll(bookLists);
            } catch (Exception e){
                e.printStackTrace();
                return FAIL;
            }
            return SUCCESS;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            String msg = "";
            switch (result){
                case FAIL:
                    msg = "由于一些原因添加书本失败";
                    break;
                case SUCCESS:
                    msg = "添加书本成功";
                    break;
                case REPEAT:
                    msg = "书本" + repeatBookList.getBookname() + "重复了";
                    break;
            }

            Toast.makeText(FileActivity.this, msg, Toast.LENGTH_SHORT).show();
        }
    }

    //设置添加按钮text
    protected void setAddFileText(final int num){
        btnAddFile.post(new Runnable() {
            @Override
            public void run() {
                btnAddFile.setText("加入书架(" + num + ")项");
            }
        });
    }
    protected void searchFile1(){
        startTime = System.currentTimeMillis();
        new SearchTextFileTask().execute();
    }

    private long startTime;
    private long endTime;
    //获取所有的本地text文件
    protected void searchFile(){
        startTime = System.currentTimeMillis();
        showProgress(true,"正在扫描txt文件");

        final File file = new File(root.getAbsolutePath());
        File[] subFiles = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        int fileLength = subFiles.length;
        List<File> files = new ArrayList<>();
        int num = 1;
        for (int i = 0;i < fileLength;i++){
            if (i < fileLength*num/24 - 1){
                files.add(subFiles[i]);
                continue;
            }
            if (i == fileLength*num/24 - 1){
                num++;
                FileThread fileThread = new FileThread(files);
                fileThread.start();
                files = new ArrayList<>();
            }
        }

    }

    private class FileThread extends Thread{
        private List<File> subFiles;
        public FileThread(List<File> subFiles){
            this.subFiles = subFiles;
        }

        @Override
        public void run() {
            super.run();
            List<File> files = new ArrayList<>();
            for (File file:subFiles){
                files = FileUtils.getSuffixFile(files, file,".txt");
            }
            Message message = new Message();
            message.obj = files;
            searchHandler.sendMessage(message);
        }
    }

    private Handler searchHandler = new Handler(){
        private int successNum = 0;
        private final static int TOTALNUM = 24;
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            successNum++;
            if (msg.obj != null) {
                listFile.addAll((List<File>)msg.obj);
            }
            if (successNum == TOTALNUM){
                hideProgress();
                successNum = 0;
                adapter.setFiles(listFile);  //list值传到adapter
                setAddFileText(0);
                endTime = System.currentTimeMillis();
                Log.e("time",endTime - startTime + "");
            }
        }
    };

    private class SearchTextFileTask extends AsyncTask<Void,Void,Boolean>{
        @Override
        protected void onPreExecute() {
            showProgress(true,"正在扫描txt文件");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            listFile = FileUtils.getSuffixFile(root.getAbsolutePath(),".txt");
            if (listFile == null || listFile.isEmpty()){
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            hideProgress();
            if (result) {
                adapter.setFiles(listFile);  //list值传到adapter
                setAddFileText(0);
                endTime = System.currentTimeMillis();
                Log.e("time",endTime - startTime + "");
            } else {
                Toast.makeText(FileActivity.this, "本机查不到txt文件", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case EXTERNAL_STORAGE_REQ_CODE: {
                // 如果请求被拒绝，那么通常grantResults数组为空
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //申请成功，进行相应操作
                    root = Environment.getExternalStorageDirectory();
                    searchFile();
                } else {
                    //申请失败，可以继续向用户解释。
                }
                return;
            }
        }
    }

}

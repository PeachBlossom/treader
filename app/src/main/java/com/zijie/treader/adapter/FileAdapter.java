package com.zijie.treader.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zijie.treader.FileActivity;
import com.zijie.treader.R;
import com.zijie.treader.db.BookList;
import com.zijie.treader.util.Fileutil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2015/12/19.
 */
public  class FileAdapter extends BaseAdapter {
    private List<File> files;
    private HashMap<File,Boolean> checkMap = new HashMap<>();

    private Context context;
    private CheckedChangeListener mCheckedChangeListener;

    public FileAdapter(Context context) {
        this.context = context;
    }

    public FileAdapter(Context context, List<File> files) {
        this.context = context;
        this.files = files;
        initCheckMap();
    }

    private void initCheckMap(){
        if (files != null) {
            for (File file : files) {
                checkMap.put(file, false);
            }
        }
    }

    @Override
    public int getCount() {
        if (files == null) {
            return 0;
        }
        return files.size();
    }

    @Override
    public Object getItem(int position) {
        return files.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //全选
    public void checkAll(){
        Iterator iter = checkMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            checkMap.put((File) entry.getKey(),true);
        }
        notifyDataSetChanged();
    }

    //取消
    public void cancel(){
        Iterator iter = checkMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            checkMap.put((File) entry.getKey(),false);
        }
        notifyDataSetChanged();
    }

    //选择的数目
    public int getCheckNum(){
        int num = 0;
        Iterator iter = checkMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            if ((Boolean) entry.getValue()){
                num++;
            }
        }
        return num;
    }

    public List<File> getCheckFiles(){
        List<File> files = new ArrayList<>();
        Iterator iter = checkMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            if ((Boolean) entry.getValue()){
                files.add((File) entry.getKey());
            }
        }
        return files;
    }

    public HashMap<File,Boolean> getCheckMap(){
        return checkMap;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
        initCheckMap();
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        final File file = files.get(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.adapter_file_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //CheckBox状态变化监听
        viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkMap.put(file,isChecked);
                if (mCheckedChangeListener != null){
                    mCheckedChangeListener.onCheckedChanged(position,buttonView,isChecked);
                }
            }
        });
        initFileData(file,viewHolder);
        initCheckBox(file,viewHolder);
        return convertView;
    }

    private void initCheckBox(File file,ViewHolder viewHolder){
        if (checkMap.get(file) != null) {
            viewHolder.checkBox.setChecked(checkMap.get(file));
        }
    }

    private void initFileData(File file,ViewHolder viewHolder){
        //设置文件名
        viewHolder.textView.setText(file.getName());
        //文件夹和文件逻辑判断
        if (file.isDirectory()) {
            viewHolder.fileIcon.setImageResource(R.mipmap.folder);
            viewHolder.checkBox.setVisibility(View.INVISIBLE);
            viewHolder.textSize.setText("项");
        } else {
            viewHolder.fileIcon.setImageResource(R.mipmap.file_type_txt);
            viewHolder.checkBox.setVisibility(View.VISIBLE);
            viewHolder.textSize.setText(Fileutil.formatFileSize(file.length()));
        }
    }

    static class ViewHolder {
        @Bind(R.id.tv_file_text)
        TextView textView;
        @Bind(R.id.tv_file_text_size)
        TextView textSize;
        @Bind(R.id.iv_file_icon)
        ImageView fileIcon;
        @Bind(R.id.cb_file_image)
        CheckBox checkBox;
        @Bind(R.id.ll_file_lin)
        LinearLayout linearLayout;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public void setCheckedChangeListener(CheckedChangeListener checkedChangeListener){
        mCheckedChangeListener = checkedChangeListener;
    }

    public interface CheckedChangeListener{
        void onCheckedChanged(int position,CompoundButton buttonView, boolean isChecked);
    }
}

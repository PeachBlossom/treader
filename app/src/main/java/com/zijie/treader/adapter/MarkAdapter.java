package com.zijie.treader.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zijie.treader.Config;
import com.zijie.treader.R;
import com.zijie.treader.db.BookMarks;
import com.zijie.treader.util.PageFactory;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by Administrator on 2016/1/3.
 */
public class MarkAdapter extends BaseAdapter {
    private Context mContext;
    private List<BookMarks> list ;
    private Config config;
    private Typeface typeface;
    private PageFactory pageFactory;

    public MarkAdapter(Context context, List<BookMarks> list) {
        mContext = context;
        this.list = list;
        pageFactory = PageFactory.getInstance();
        config = config.getInstance();
        typeface = config.getTypeface();
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    public Object getItem(int position) {
        return list.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_bookmark,null);
            viewHolder.text_mark = (TextView) convertView.findViewById(R.id.text_mark);
            viewHolder.progress1 = (TextView) convertView.findViewById(R.id.progress1);
            viewHolder.mark_time = (TextView) convertView.findViewById(R.id.mark_time);
            viewHolder.text_mark.setTypeface(typeface);
            viewHolder.progress1.setTypeface(typeface);
            viewHolder.mark_time.setTypeface(typeface);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.text_mark.setText(list.get(position).getText());
        long begin = list.get(position).getBegin();
        float fPercent = (float) (begin * 1.0 / pageFactory.getBookLen());
        DecimalFormat df = new DecimalFormat("#0.0");
        String strPercent = df.format(fPercent * 100) + "%";
        viewHolder.progress1.setText(strPercent);
        viewHolder.mark_time.setText(list.get(position).getTime().substring(0, 16));
        return convertView;
    }

    class ViewHolder {

        TextView text_mark,progress1,mark_time;
    }

}

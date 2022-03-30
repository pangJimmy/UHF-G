package com.pda.uhf_g.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.pda.uhf_g.R;
import com.pda.uhf_g.entity.TagInfo;

import java.util.List;

public class EPCListViewAdapter extends BaseAdapter {

    private Context mContext ;
    private List<TagInfo> list ;

    public EPCListViewAdapter(Context context, List<TagInfo> list) {
        this.mContext = context ;
        this.list = list ;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder ;
        if (convertView == null) {
            viewHolder = new ViewHolder() ;
            convertView = LayoutInflater.from(mContext).inflate(R.layout.recycle_item, null);
            viewHolder.index = (TextView) convertView.findViewById(R.id.index);
            viewHolder.type = (TextView) convertView.findViewById(R.id.type);
            viewHolder.sensor_data = (TextView) convertView.findViewById(R.id.sensor_data);
            viewHolder.epc = (TextView) convertView.findViewById(R.id.epc);
            viewHolder.tid = (TextView) convertView.findViewById(R.id.tid);
            viewHolder.rssi = (TextView) convertView.findViewById(R.id.rssi);
            viewHolder.count = (TextView) convertView.findViewById(R.id.count);
            viewHolder.layoutTid = (LinearLayout) convertView.findViewById(R.id.layout_tid);
            viewHolder.tvTid = (TextView) convertView.findViewById(R.id.tv_tid) ;
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (list != null && !list.isEmpty()) {
            TagInfo tag = list.get(position);
            viewHolder.index.setText(tag.getIndex().toString());
            viewHolder.type.setText(tag.getType());
            viewHolder.epc.setText(tag.getEpc());
            viewHolder.tid.setText(tag.getTid());
            viewHolder.rssi.setText(tag.getRssi());
            viewHolder.count.setText(tag.getCount().toString());
            if (tag.getIsShowTid()) {
                viewHolder.layoutTid.setVisibility(View.VISIBLE);
                viewHolder.tvTid.setText(tag.getTid());
            }else{
                viewHolder.layoutTid.setVisibility(View.GONE);
            }
        }
        return convertView;
    }

    class ViewHolder{
        TextView index;
        TextView type;
        TextView sensor_data;
        TextView epc;
        TextView tid;
        TextView rssi;
        TextView count;
        LinearLayout layoutTid ;
        TextView tvTid ;
    }
}

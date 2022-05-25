package com.pda.uhf_g.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.handheld.uhfr.Reader;
import com.pda.uhf_g.R;

import java.util.List;

import cn.pda.serialport.Tools;

public class TempTagListViewAdapter extends BaseAdapter {

    private Context mContext ;
    private List<Reader.TEMPTAGINFO> list ;

    public TempTagListViewAdapter(Context mContext, List<Reader.TEMPTAGINFO> list) {
        this.mContext = mContext ;
        this.list = list ;
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder ;
        if (convertView == null) {
            viewHolder = new ViewHolder() ;
            convertView = LayoutInflater.from(mContext).inflate(R.layout.tag_item, null);
            viewHolder.index = convertView.findViewById(R.id.tv_sn) ;
            viewHolder.epc = convertView.findViewById(R.id.tv_epc) ;
            viewHolder.temp = convertView.findViewById(R.id.tv_temp) ;
            viewHolder.count = convertView.findViewById(R.id.tv_count) ;
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (list != null && !list.isEmpty()) {
            Reader.TEMPTAGINFO info = list.get(position);
            viewHolder.index.setText(info.index + "");
            viewHolder.epc.setText(Tools.Bytes2HexString(info.EpcId, info.Epclen));
            viewHolder.temp.setText(Double.toString(info.Temperature));
            viewHolder.count.setText(info.count + "");
        }
        return convertView;
    }

    class ViewHolder{
        TextView index;
        TextView epc;
        TextView temp;
        TextView count;
    }
}

package com.pda.uhf_g.adapter;


import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.pda.uhf_g.R;
import com.pda.uhf_g.entity.TagInfo;
import com.pda.uhf_g.util.LogUtil;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;


public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> {
    private List<TagInfo> mTagList;
    private Integer thisPosition = null;
    private boolean isTid = true;
    private TextView showText;
    private NumberFormat nf = NumberFormat.getNumberInstance();

    public Integer getThisPosition() {
        return thisPosition;
    }

    public void setThisPosition(Integer thisPosition) {
        this.thisPosition = thisPosition;
    }

    public boolean isTid() {
        return isTid;
    }

    public void setTid(boolean tid) {
        isTid = tid;
    }

    public TextView getShowText() {
        return showText;
    }

    public void setShowText(TextView showText) {
        this.showText = showText;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView index;
        TextView type;
        TextView sensor_data;
        TextView epc;
        TextView tid;
        TextView rssi;
        TextView count;
        TextView ctesius_data;
        LinearLayout layoutTid ;
        TextView tvTid ;

        public ViewHolder(final View view) {
            super(view);
            index = (TextView) view.findViewById(R.id.index);
            type = (TextView) view.findViewById(R.id.type);
            sensor_data = (TextView) view.findViewById(R.id.sensor_data);
            epc = (TextView) view.findViewById(R.id.epc);
            tid = (TextView) view.findViewById(R.id.tid);
            rssi = (TextView) view.findViewById(R.id.rssi);
            count = (TextView) view.findViewById(R.id.count);
//            moisture_data = (TextView) view.findViewById(R.id.moisture_data);
//            ltu27_data = (TextView) view.findViewById(R.id.ltu27_data);
//            ltu31_data = (TextView) view.findViewById(R.id.ltu31_data);
//            epcData = (TextView) view.findViewById(R.id.epcBank_data);
            ctesius_data = (TextView) view.findViewById(R.id.ctesius_data);

            layoutTid = (LinearLayout) view.findViewById(R.id.layout_tid);
            tvTid = (TextView) view.findViewById(R.id.tv_tid) ;
        }
    }

    public RecycleViewAdapter(List<TagInfo> list) {
        mTagList = list;
        nf.setMaximumFractionDigits(2);
        //
        nf.setRoundingMode(RoundingMode.DOWN);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_item, parent, false);
        final RecycleViewAdapter.ViewHolder holder = new RecycleViewAdapter.ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TagInfo tag = mTagList.get(holder.getAdapterPosition());
                setThisPosition(holder.getAdapterPosition());
                notifyDataSetChanged();
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TagInfo tag = mTagList.get(position);
        holder.index.setText(tag.getIndex().toString());
        holder.type.setText(tag.getType());
        holder.ctesius_data.setVisibility(View.GONE);
        holder.epc.setText(tag.getEpc());
        holder.tid.setText(tag.getTid());
        holder.rssi.setText(tag.getRssi());
        holder.count.setText(tag.getCount().toString());
        if (tag.getIsShowTid()) {
            holder.layoutTid.setVisibility(View.VISIBLE);
            holder.tvTid.setText(tag.getTid());
        }else{
            holder.layoutTid.setVisibility(View.GONE);
        }

        if (getThisPosition() != null && position == getThisPosition()) {
            holder.itemView.setBackgroundColor(Color.rgb(135, 206, 235));
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        return mTagList.size();
    }

    public void notifyData(List<TagInfo> poiItemList) {
        LogUtil.e("RecycleViewAdapter notifyData()");
        if (poiItemList != null) {
            mTagList = poiItemList;
//            mTagList.clear();
//            mTagList.addAll(poiItemList) ;
            notifyDataSetChanged();
        }
    }


}

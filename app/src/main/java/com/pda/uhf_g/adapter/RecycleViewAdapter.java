package com.pda.uhf_g.adapter;


import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.pda.uhf_g.R;
import com.pda.uhf_g.entity.TagInfo;

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
        TextView userData;
        TextView reserveData;
        TextView moisture_data;
        TextView ltu27_data;
        TextView ltu31_data;
        TextView epcData;
        TextView ctesius_data;

        public ViewHolder(final View view) {
            super(view);
            index = (TextView) view.findViewById(R.id.index);
            type = (TextView) view.findViewById(R.id.type);
            sensor_data = (TextView) view.findViewById(R.id.sensor_data);
            epc = (TextView) view.findViewById(R.id.epc);
            tid = (TextView) view.findViewById(R.id.tid);
            rssi = (TextView) view.findViewById(R.id.rssi);
            count = (TextView) view.findViewById(R.id.count);
            userData = (TextView) view.findViewById(R.id.userData);
            reserveData = (TextView) view.findViewById(R.id.reserveData);
//            moisture_data = (TextView) view.findViewById(R.id.moisture_data);
//            ltu27_data = (TextView) view.findViewById(R.id.ltu27_data);
//            ltu31_data = (TextView) view.findViewById(R.id.ltu31_data);
//            epcData = (TextView) view.findViewById(R.id.epcBank_data);
            ctesius_data = (TextView) view.findViewById(R.id.ctesius_data);
        }
    }

    public RecycleViewAdapter(List<TagInfo> list) {
        mTagList = list;
        nf.setMaximumFractionDigits(2);
        // 如果不需要四舍五入，可以使用RoundingMode.DOWN
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
//        if (showText != null && showText.getId() == R.id.ctesius_title) {
//            holder.ctesius_data.setVisibility(View.VISIBLE);
//            holder.ctesius_data.setText(nf.format(tag.getCtesius() * 0.01) + "°");
//        } else {
            holder.ctesius_data.setVisibility(View.GONE);
//        }
//        if (!isTid) {
//            holder.epc.setVisibility(View.VISIBLE);
//            holder.tid.setVisibility(View.GONE);
//        } else {
//            holder.epc.setVisibility(View.GONE);
//            holder.tid.setVisibility(View.VISIBLE);
//        }
//        if (showText != null) {
////            if (showText.getId() == R.id.sensor_title) {
////                holder.sensor_data.setVisibility(View.VISIBLE);
////                holder.sensor_data.setText(tag.getNmv2d());
////            }
//            if (showText.getId() == R.id.moisture_title) {
//                holder.moisture_data.setText(tag.getMoisture());
//                holder.moisture_data.setVisibility(View.VISIBLE);
//                holder.ltu27_data.setVisibility(View.GONE);
//                holder.ltu31_data.setVisibility(View.GONE);
//                holder.epcData.setVisibility(View.GONE);
//            }
//
//            if (showText.getId() == R.id.ltu27_title) {
//                holder.ltu27_data.setText(tag.getLtu27());
//                holder.ltu27_data.setVisibility(View.VISIBLE);
//                holder.moisture_data.setVisibility(View.GONE);
//                holder.ltu31_data.setVisibility(View.GONE);
//                holder.epcData.setVisibility(View.GONE);
//            }
//
//            if (showText.getId() == R.id.ltu31_title) {
//                holder.ltu31_data.setText(tag.getLtu31());
//                holder.ltu31_data.setVisibility(View.VISIBLE);
//                holder.moisture_data.setVisibility(View.GONE);
//                holder.ltu27_data.setVisibility(View.GONE);
//                holder.epcData.setVisibility(View.GONE);
//            }
//
//            if (showText.getId() == R.id.epcBank_title) {
//                holder.epcData.setText(tag.getEpcData());
//                holder.epcData.setVisibility(View.VISIBLE);
//                holder.moisture_data.setVisibility(View.GONE);
//                holder.ltu27_data.setVisibility(View.GONE);
//                holder.ltu31_data.setVisibility(View.GONE);
//            }
//
//        } else {
////            holder.sensor_data.setVisibility(View.GONE);
//            holder.moisture_data.setVisibility(View.GONE);
//            holder.ltu27_data.setVisibility(View.GONE);
//            holder.ltu31_data.setVisibility(View.GONE);
//            holder.epcData.setVisibility(View.GONE);
//        }

        holder.userData.setText(tag.getUserData());
        holder.reserveData.setText(tag.getReservedData());
        holder.epc.setText(tag.getEpc());
        holder.tid.setText(tag.getTid());
        holder.rssi.setText(tag.getRssi());
        holder.count.setText(tag.getCount().toString());

        if (getThisPosition() != null && position == getThisPosition()) {
            holder.itemView.setBackgroundColor(Color.rgb(135, 206, 235));
//            holder.index.setBackgroundColor(Color.rgb(135, 206, 235));
//            holder.type.setBackgroundColor(Color.rgb(135, 206, 235));
//            holder.tid.setBackgroundColor(Color.rgb(135, 206, 235));
//            holder.epc.setBackgroundColor(Color.rgb(135, 206, 235));
//            holder.count.setBackgroundColor(Color.rgb(135, 206, 235));
//            holder.rssi.setBackgroundColor(Color.rgb(135, 206, 235));
//            holder.userData.setBackgroundColor(Color.rgb(135, 206, 235));
//            holder.reserveData.setBackgroundColor(Color.rgb(135, 206, 235));
//            holder.sensor_data.setBackgroundColor(Color.rgb(135, 206, 235));
//            holder.epcData.setBackgroundColor(Color.rgb(135, 206, 235));
//            holder.moisture_data.setBackgroundColor(Color.rgb(135, 206, 235));
//            holder.ltu27_data.setBackgroundColor(Color.rgb(135, 206, 235));
//            holder.ltu31_data.setBackgroundColor(Color.rgb(135, 206, 235));
        } else {
//            holder.index.setBackgroundColor(Color.WHITE);
//            holder.type.setBackgroundColor(Color.WHITE);
//            holder.tid.setBackgroundColor(Color.WHITE);
//            holder.epc.setBackgroundColor(Color.WHITE);
//            holder.count.setBackgroundColor(Color.WHITE);
//            holder.rssi.setBackgroundColor(Color.WHITE);
//            holder.userData.setBackgroundColor(Color.WHITE);
//            holder.reserveData.setBackgroundColor(Color.WHITE);
//            holder.sensor_data.setBackgroundColor(Color.WHITE);
//            holder.epcData.setBackgroundColor(Color.WHITE);
//            holder.moisture_data.setBackgroundColor(Color.WHITE);
//            holder.ltu27_data.setBackgroundColor(Color.WHITE);
//            holder.ltu31_data.setBackgroundColor(Color.WHITE);
            holder.itemView.setBackgroundColor(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        return mTagList.size();
    }

    public void notifyData(List<TagInfo> poiItemList) {
        if (poiItemList != null) {
            mTagList = poiItemList;
            notifyDataSetChanged();
        }
    }


}

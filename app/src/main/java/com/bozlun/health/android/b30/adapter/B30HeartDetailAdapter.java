package com.bozlun.health.android.b30.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.bozlun.health.android.R;
import com.veepoo.protocol.model.datas.HalfHourRateData;
import com.veepoo.protocol.model.datas.HalfHourSportData;

import java.util.List;

/**
 * Created by Administrator on 2018/8/15.
 */

public class B30HeartDetailAdapter extends RecyclerView.Adapter<B30HeartDetailAdapter.B30ViewHolder> {

    private List<HalfHourRateData> list;
    private Context mContext;
    private List<HalfHourSportData> halfHourSportData;

    public B30HeartDetailAdapter(List<HalfHourRateData> list, List<HalfHourSportData> halfHourSportData, Context mContext) {
        this.list = list;
        this.halfHourSportData = halfHourSportData;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public B30ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_b30_heart_detail_layout, parent, false);
        B30ViewHolder b30ViewHolder = new B30ViewHolder(view);
        return b30ViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull B30ViewHolder holder, int position) {
        if (position < halfHourSportData.size()) {
            HalfHourRateData halfHourRateData = list.get(position);
            String colck = halfHourRateData.getTime().getColck();
            int rateValue = halfHourRateData.getRateValue();
            holder.dateTv.setText(colck);
            holder.valueTv.setText(rateValue==0?"--":rateValue+"");
            HalfHourSportData halfHourSportDatas = halfHourSportData.get(position);
            int sportValue = halfHourSportDatas.getSportValue();

            if (sportValue <= 220) {//静止
                holder.img.setImageResource(R.drawable.history_heart_rest);
            } else if (sportValue > 220 && sportValue <= 700) {//少量
                holder.img.setImageResource(R.drawable.history_heart_move);
            } else if (sportValue > 700 && sportValue <= 1400) {//中量
                holder.img.setImageResource(R.drawable.history_heart_moderrate);
            } else if (sportValue > 1400 && sportValue <= 3200) {//大量
                holder.img.setImageResource(R.drawable.history_heart_mass);
            } else if (sportValue > 3200) {//剧烈
                holder.img.setImageResource(R.drawable.history_heart_strenuous);
            }
        }else{
            HalfHourRateData halfHourRateData = list.get(position);
            String colck = halfHourRateData.getTime().getColck();
            int rateValue = halfHourRateData.getRateValue();
            holder.dateTv.setText(colck);
            holder.valueTv.setText(rateValue==0?"--":rateValue+"");
            holder.img.setImageResource(R.drawable.history_heart_move);
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class B30ViewHolder extends RecyclerView.ViewHolder {

        TextView dateTv, valueTv;
        ImageView img;

        public B30ViewHolder(View itemView) {
            super(itemView);
            dateTv = itemView.findViewById(R.id.itemHeartDetailDateTv);
            valueTv = itemView.findViewById(R.id.itemHeartDetailValueTv);
            img = itemView.findViewById(R.id.itemHeartDetailImg);
        }
    }
}

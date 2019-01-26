package com.bozlun.health.android.adpter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bozlun.health.android.R;

import java.util.List;

/**
 * Created by An on 2018/9/11.
 */

public class PhoneAdapter extends BaseAdapter {
    List<Integer>phoneHeadList;
    Context context;

    public PhoneAdapter(List<Integer> phoneHeadList, Context context) {
        this.phoneHeadList = phoneHeadList;
        this.context = context;
    }
    @Override
    public int getCount() {
        return phoneHeadList.size();
    }

    @Override
    public Object getItem(int position) {
        return phoneHeadList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_phone_head_layout,parent,false);
        TextView tv = view.findViewById(R.id.tv_phone_head);
        tv.setText("+"+phoneHeadList.get(position));
        return view;
    }
}

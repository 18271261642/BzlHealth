
package com.bozlun.health.android.friend.mutilbind;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bozlun.health.android.R;
import com.bozlun.health.android.friend.bean.TextItem;

import me.drakeet.multitype.ItemViewBinder;

public class TextItemViewBinder extends ItemViewBinder<TextItem, TextItemViewBinder.TextHolder> {

    static class TextHolder extends RecyclerView.ViewHolder {

        private @NonNull
        final TextView text;

        TextHolder(@NonNull View itemView) {
            super(itemView);
            this.text = (TextView) itemView.findViewById(R.id.text_frend);
        }
    }

    @NonNull
    @Override
    protected TextHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        View root = inflater.inflate(R.layout.item_text, parent, false);
        return new TextHolder(root);
    }

    @Override
    protected void onBindViewHolder(@NonNull TextHolder holder, @NonNull TextItem textItem) {
        holder.text.setText(textItem.text);

    }
}
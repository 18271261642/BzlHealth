package com.bozlun.health.android.friend.mutilbind;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.R;
import com.bozlun.health.android.bzlmaps.CommomDialog;
import com.bozlun.health.android.friend.bean.MyFrendListBean;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FrendAdapter extends RecyclerView.Adapter<FrendAdapter.ViewHodler> {
    private Context context;
    List<MyFrendListBean.MyfriendsBean> myfriends;

    public FrendAdapter(Context context, List<MyFrendListBean.MyfriendsBean> myfriends) {
        this.context = context;
        this.myfriends = myfriends;
    }

    @NonNull
    @Override
    public ViewHodler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.frend_rankings_list_item, parent, false);
        return new ViewHodler(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHodler holder, final int position) {
        try {
            final MyFrendListBean.MyfriendsBean myfriendsBean = myfriends.get(position);
            if (myfriendsBean != null) {
                if (position != 0) {
                    String nickName = myfriendsBean.getNickName().trim();
                    String phone = myfriendsBean.getPhone().trim();
                    //昵称
                    if (!TextUtils.isEmpty(nickName) && holder.userNames != null) {
                        holder.userNames.setText(nickName);
                    } else if (!TextUtils.isEmpty(phone) && holder.userNames != null) {
                        holder.userNames.setText(phone);
                    }
                    //头像
                    if (!TextUtils.isEmpty((String) myfriendsBean.getImage()) && holder.circleImageView != null) {
                        Glide.with(MyApp.getInstance()).load((String) myfriendsBean.getImage())
                                .into(holder.circleImageView);
                    } else {
                        Glide.with(MyApp.getInstance()).load(R.mipmap.bg_img).into(holder.circleImageView);
                    }
                    holder.frendSteps.setText(context.getResources().getString(R.string.step) + ":" + String.valueOf(myfriendsBean.getStepNumber()));//步数
                    holder.rankNuber.setText(String.valueOf(position));//排名
                    holder.zan_count.setText(String.valueOf(myfriendsBean.getTodayThumbs()));//被赞次数

                    int isThumbs = myfriendsBean.getIsThumbs();
                    if (isThumbs == 0) {
                        holder.zanOclick.setEnabled(true);
                        holder.image_tautas.setBackgroundResource(R.mipmap.ic_on_like);
                    } else {
                        holder.zanOclick.setEnabled(false);
                        holder.image_tautas.setBackgroundResource(R.mipmap.ic_un_like);
                    }
                    holder.viewst_view.setVisibility(View.GONE);
                    holder.zanOclick.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //
                            mOnItemListenter.ItemLoveOnClick(view, myfriendsBean.getUserId());
                        }
                    });
                    //item点击
                    holder.line_onclick.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mOnItemListenter.ItemOnClick(view, myfriendsBean.getUserId(), myfriendsBean.getStepNumber(), myfriendsBean.getHeight(), position);
                        }
                    });
                    //item长按
                    holder.line_onclick.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(final View view) {

                            final String userName = TextUtils.isEmpty(myfriendsBean.getNickName()) ? myfriendsBean.getPhone() : myfriendsBean.getNickName();
                            new CommomDialog(context, R.style.dialog,
                                    MyApp.getInstance().getResources().getString(R.string.string_ok_delete_frend)
                                    + userName + "？", new CommomDialog.OnCloseListener() {
                                @Override
                                public void onClick(Dialog dialog, boolean confirm) {
                                    if (confirm) {
                                        mOnItemListenter.ItemOnLongClick(view, myfriendsBean.getUserId());
                                        myfriends.remove(position);
                                        notifyDataSetChanged();
                                    }
                                    dialog.dismiss();
                                }
                            }).setTitle(MyApp.getInstance().getResources().getString(R.string.string_delete_frend)).show();

                            return false;
                        }
                    });
                }else {
                    String nickName = myfriendsBean.getNickName().trim();
                    String phone = myfriendsBean.getPhone().trim();
                    //昵称
                    if (!TextUtils.isEmpty(nickName) && holder.userNames != null) {
                        holder.userNames.setText(nickName);
                    } else if (!TextUtils.isEmpty(phone) && holder.userNames != null) {
                        holder.userNames.setText(phone);
                    }
                    //头像
                    if (!TextUtils.isEmpty((String) myfriendsBean.getImage()) && holder.circleImageView != null) {
                        Glide.with(MyApp.getInstance()).load((String) myfriendsBean.getImage())
                                .into(holder.circleImageView);
                    } else {
                        Glide.with(MyApp.getInstance()).load(R.mipmap.bg_img).into(holder.circleImageView);
                    }
                    holder.viewst_view.setVisibility(View.VISIBLE);
                    holder.frendSteps.setText(context.getResources().getString(R.string.step) + ":" + String.valueOf(myfriendsBean.getStepNumber()));//步数
                    holder.rankNuber.setText(MyApp.getInstance().getResources().getString(R.string.string_mine));//排名
                    holder.zan_count.setText(String.valueOf(myfriendsBean.getTodayThumbs()));//被赞次数

                    //item点击
                    holder.line_views.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mOnItemListenter.ItemOnClickMine(position);
                        }
                    });
                }


            }


        } catch (Error e) {
        }

    }

    @Override
    public int getItemCount() {
        return myfriends.size();
    }

    public class ViewHodler extends RecyclerView.ViewHolder {
        // ---- 用户名---排名---好友步数---点赞以及点赞计数
        TextView userNames, rankNuber, frendSteps, zan_count;
        CircleImageView circleImageView;
        ImageView image_tautas;
        LinearLayout line_onclick, zanOclick,line_views;
        View viewst_view;

        ViewHodler(View itemView) {
            super(itemView);

            line_views= itemView.findViewById(R.id.line_views);
            viewst_view = itemView.findViewById(R.id.st_view);
            userNames = itemView.findViewById(R.id.user_names);
            rankNuber = itemView.findViewById(R.id.text_rank_nuber);
            frendSteps = itemView.findViewById(R.id.frend_steps);
            zan_count = itemView.findViewById(R.id.zan_cont);
            zanOclick = itemView.findViewById(R.id.love_zan);
            image_tautas = itemView.findViewById(R.id.image_tautas);
            circleImageView = itemView.findViewById(R.id.imahe_list_heard);
            line_onclick = itemView.findViewById(R.id.line_onclick);

        }
    }


    private OnItemListenter mOnItemListenter;

    public void setmOnItemListenter(OnItemListenter mOnItemListenter) {
        this.mOnItemListenter = mOnItemListenter;
    }

    public interface OnItemListenter {
        void ItemLoveOnClick(View view, String applicant);

        void ItemOnClick(View view, String applicant, int stepNumber, String frendHeight, int postion);

        void ItemOnClickMine(int postion);

        void ItemOnLongClick(View view, String applicant);
    }
}

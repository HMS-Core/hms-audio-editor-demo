/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.huawei.hms.audioeditor.ui.api.DraftInfo;
import com.huawei.hms.audioeditor.ui.common.listener.OnClickRepeatedListener;
import com.huawei.hms.audioeditor.demo.util.SizeUtils;
import com.huawei.hms.audioeditor.demo.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

/**
 * me page draft recyclerView adapter
 *
 */
public class MainRecyclerViewAdapter extends RecyclerView.Adapter<MainRecyclerViewAdapter.ViewHolder> {
    private Context mContext;
    private List<DraftInfo> mRecords;
    private SimpleDateFormat mSimpleDateFormat;
    private OnItemClickListener mOnItemClickListener;

    public MainRecyclerViewAdapter(Context context, List<DraftInfo> records) {
        mContext = context;
        mRecords = records;
        mSimpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.CHINA);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recycler_view_layout_home_item, parent, false);
        return new ViewHolder(view, mOnItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DraftInfo item = mRecords.get(position);
        ViewGroup.LayoutParams itemParams = holder.constraintLayoutRecyclerViewLayoutHomeItem.getLayoutParams();
        ConstraintLayout.LayoutParams imageParams =
                new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, SizeUtils.dp2Px(mContext, 36));

        imageParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        imageParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        imageParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        holder.constraintLayoutRecyclerViewLayoutHomeItem.setLayoutParams(itemParams);
        holder.nameRecyclerViewLayoutHomeItem.setText(item.getDraftName());
        String size = SizeUtils.bytes2kb(item.getDraftSize());
        String lastUpdate = mSimpleDateFormat.format(item.getDraftUpdateTime());
        holder.sizeRecyclerViewLayoutHomeItem.setText(size + " " + lastUpdate);
    }

    @Override
    public int getItemCount() {
        return mRecords.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onMoreClick(View view, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout constraintLayoutRecyclerViewLayoutHomeItem;
        TextView nameRecyclerViewLayoutHomeItem;
        TextView sizeRecyclerViewLayoutHomeItem;
        ImageView moreRecyclerViewLayoutHomeItem;
        OnItemClickListener mOnItemClickListener;

        public ViewHolder(@NonNull View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);
            constraintLayoutRecyclerViewLayoutHomeItem =
                    itemView.findViewById(R.id.constraintLayout_recycler_view_layout_home_item);
            nameRecyclerViewLayoutHomeItem = itemView.findViewById(R.id.name_recycler_view_layout_home_item);
            sizeRecyclerViewLayoutHomeItem = itemView.findViewById(R.id.size_recycler_view_layout_home_item);
            moreRecyclerViewLayoutHomeItem = itemView.findViewById(R.id.more_recycler_view_layout_home_item);
            mOnItemClickListener = onItemClickListener;
            initListener();
        }

        private void initListener() {
            constraintLayoutRecyclerViewLayoutHomeItem.setOnClickListener(
                    new OnClickRepeatedListener(
                            v -> {
                                if (mOnItemClickListener != null) {
                                    mOnItemClickListener.onItemClick(getAdapterPosition());
                                }
                            }));

            moreRecyclerViewLayoutHomeItem.setOnClickListener(
                    new OnClickRepeatedListener(
                            v -> {
                                if (mOnItemClickListener != null) {
                                    mOnItemClickListener.onMoreClick(v, getAdapterPosition());
                                }
                            }));
        }
    }
}

/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.widget.comment;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

/**
 * RecyclerView 通用 ViewHolder
 *
 * @since 20200202
 */
public class RViewHolder extends RecyclerView.ViewHolder {
    private SparseArray<View> mViews;
    private View mConvertView;

    public RViewHolder(Context context, View itemView) {
        super(itemView);
        mConvertView = itemView;
        mViews = new SparseArray<>();
    }

    public static com.huawei.hms.audioeditor.ui.common.adapter.comment.RViewHolder get(Context context, View itemView) {
        return new com.huawei.hms.audioeditor.ui.common.adapter.comment.RViewHolder(context, itemView);
    }

    public static com.huawei.hms.audioeditor.ui.common.adapter.comment.RViewHolder get(Context context, ViewGroup parent, int layoutId) {
        View itemView = LayoutInflater.from(context).inflate(layoutId, parent, false);
        return new com.huawei.hms.audioeditor.ui.common.adapter.comment.RViewHolder(context, itemView);
    }

    public View getCovertView() {
        return mConvertView;
    }

    @SuppressWarnings("unchecked")
    public <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);
        if (view == null) {
            view = mConvertView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }

    /**
     * 设置TextView
     *
     * @param viewId ID
     * @param text   值
     * @return holder
     */
    public RViewHolder setText(int viewId, String text) {
        TextView tv = getView(viewId);
        tv.setText(text);
        return this;
    }

    public RViewHolder setImageResource(int viewId, int res) {
        ImageView imageView = getView(viewId);
        imageView.setImageResource(res);
        return this;
    }
}

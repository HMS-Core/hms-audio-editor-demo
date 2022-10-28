/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.widget.comment;

import android.content.Context;

import com.huawei.hms.audioeditor.ui.common.adapter.comment.RViewHolder;

import java.util.List;

/**
 * Single View RecyclerView Adapter
 *
 * @since 2020/02/03
 * @param <T> generics
 *
 */
public abstract class RCommandAdapter<T> extends RMCommandAdapter<T> {
    public RCommandAdapter(Context context, List<T> list, final int layoutId) {
        super(context, list);

        addItemViewDelegate(
                new ItemViewDelegate<T>() {
                    @Override
                    public int getItemViewLayoutId() {
                        return layoutId;
                    }

                    @Override
                    public boolean isForViewType(T item, int position) {
                        return true;
                    }

                    @Override
                    public void convert(RViewHolder holder, T t, int dataPosition, int position) {
                        RCommandAdapter.this.rcConvert(holder, t, dataPosition, position);
                    }
                });
    }

    /**
     * convert
     *
     * @param holder ViewHolder
     * @param t generics
     * @param dataPosition dataPosition
     * @param position position
     */
    protected abstract void rcConvert(RViewHolder holder, T t, int dataPosition, int position);
}

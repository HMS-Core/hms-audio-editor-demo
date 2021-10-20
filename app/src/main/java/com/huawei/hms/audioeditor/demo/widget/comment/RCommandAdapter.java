/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.widget.comment;

import android.content.Context;

import com.huawei.hms.audioeditor.ui.common.adapter.comment.RViewHolder;

import java.util.List;

/**
 * 单视图 RecyclerView 适配器
 *
 * @since 2020/02/03
 * @param <T> a
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
                        RCommandAdapter.this.convert(holder, t, dataPosition, position);
                    }
                });
    }

    /**
     * convert
     *
     * @param holder ViewHolder
     * @param t 泛型
     * @param dataPosition 数据索引
     * @param position 索引
     */
    protected abstract void convert(RViewHolder holder, T t, int dataPosition, int position);
}

/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.widget.comment;

import com.huawei.hms.audioeditor.ui.common.adapter.comment.RViewHolder;

public interface ItemViewDelegate<T> {
    /**
     * ss
     *
     * @return a
     */
    int getItemViewLayoutId();

    /**
     * a
     *
     * @param item a
     * @param position a
     * @return a
     */
    boolean isForViewType(T item, int position);

    /**
     * a
     *
     * @param holder a
     * @param t a
     * @param dataPosition a
     * @param position a
     */
    void convert(RViewHolder holder, T t, int dataPosition, int position);
}

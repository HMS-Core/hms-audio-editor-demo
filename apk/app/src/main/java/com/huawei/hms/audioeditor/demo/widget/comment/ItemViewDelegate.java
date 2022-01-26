/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.widget.comment;

import com.huawei.hms.audioeditor.ui.common.adapter.comment.RViewHolder;

public interface ItemViewDelegate<T> {
    int getItemViewLayoutId();

    boolean isForViewType(T item, int position);

    void convert(RViewHolder holder, T t, int dataPosition, int position);
}

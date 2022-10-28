/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.widget.comment;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.huawei.hms.audioeditor.ui.common.adapter.comment.RViewHolder;
import com.huawei.hms.audioeditor.ui.common.listener.OnClickRepeatedListener;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

/**
 *  RecyclerView adapter
 *
 * @since 20200202
 */
public class RMCommandAdapter<T> extends RecyclerView.Adapter<RViewHolder> {
    private SparseArray<View> mHeaders = new SparseArray<>();
    private SparseArray<View> mFooters = new SparseArray<>();

    protected List<T> mList;
    protected Context mContext;
    public ItemViewDelegateManager mItemViewDelegateManager;
    private OnItemClickListener mOnItemClickListener;

    public RMCommandAdapter(Context context, List<T> list) {
        mContext = context;
        mList = list;
        mItemViewDelegateManager = new ItemViewDelegateManager();
    }

    @Override
    public RViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mHeaders.indexOfKey(viewType) >= 0) {
            View view = mHeaders.get(viewType);
            return RViewHolder.get(mContext, view);
        }

        if (mFooters.indexOfKey(viewType) >= 0) {
            View view = mFooters.get(viewType);
            return RViewHolder.get(mContext, view);
        }

        ItemViewDelegate itemViewDelegate = mItemViewDelegateManager.getItemViewDelegate(viewType);
        int layoutId = itemViewDelegate.getItemViewLayoutId();
        RViewHolder holder = RViewHolder.get(mContext, parent, layoutId);
        setListener(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(RViewHolder holder, int position) {
        if (isHeaderPosition(position) || isFooterPosition(position)) {
            return;
        }
        position = position - mHeaders.size();
        convert(holder, mList.get(position), position);
    }

    @SuppressWarnings("unchecked")
    @Override
    public int getItemViewType(int position) {
        if (isHeaderPosition(position)) {
            return mHeaders.keyAt(position);
        }

        if (isFooterPosition(position)) {
            position = position - getOriginalItemCount() - mHeaders.size();
            return mFooters.keyAt(position);
        }
        position = position - mHeaders.size();
        if (!useItemViewDelegateManager()) {
            return super.getItemViewType(position);
        }
        return mItemViewDelegateManager.getItemViewType(mList.get(position), position);
    }

    private boolean isFooterPosition(int position) {
        return position >= getOriginalItemCount() + mHeaders.size();
    }

    private boolean isHeaderPosition(int position) {
        return position < mHeaders.size();
    }

    private void setListener(final RViewHolder viewHolder) {
        viewHolder
                .getCovertView()
                .setOnClickListener(
                        new OnClickRepeatedListener(
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (mOnItemClickListener != null) {
                                            int position = viewHolder.getAdapterPosition() - mHeaders.size();
                                            mOnItemClickListener.onItemClick(
                                                    v, viewHolder, position, viewHolder.getAdapterPosition());
                                        }
                                    }
                                }));

        viewHolder
                .getCovertView()
                .setOnLongClickListener(
                        new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                if (mOnItemClickListener != null) {
                                    int position = viewHolder.getAdapterPosition() - mHeaders.size();
                                    return mOnItemClickListener.onItemLongClick(
                                            v, viewHolder, position, viewHolder.getAdapterPosition());
                                }
                                return false;
                            }
                        });
    }

    @Override
    public int getItemCount() {
        return mList.size() + mHeaders.size() + mFooters.size();
    }

    public int getOriginalItemCount() {
        return getItemCount() - mHeaders.size() - mFooters.size();
    }

    @SuppressWarnings("unchecked")
    private void convert(RViewHolder holder, T t, int dataPosition) {
        mItemViewDelegateManager.convert(holder, t, dataPosition, holder.getAdapterPosition());
    }

    @SuppressWarnings("unchecked")
    protected RMCommandAdapter addItemViewDelegate(ItemViewDelegate<T> itemViewDelegate) {
        mItemViewDelegateManager.addDelegate(itemViewDelegate);
        return this;
    }

    private boolean useItemViewDelegateManager() {
        return mItemViewDelegateManager.getItemViewDelegateCount() > 0;
    }

    public interface OnItemClickListener {
        /**
         * click
         *
         * @param view view
         * @param holder holder
         * @param dataPosition dataPosition
         * @param position position
         */
        void onItemClick(View view, RecyclerView.ViewHolder holder, int dataPosition, int position);

        /**
         * long click
         * @param view view
         * @param holder holder
         * @param dataPosition dataPosition
         * @param position position
         * @return true: success; false:failed
         */
        boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int dataPosition, int position);
    }

    /**
     * ClickListener
     *
     * @param onItemClickListener click
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }
}

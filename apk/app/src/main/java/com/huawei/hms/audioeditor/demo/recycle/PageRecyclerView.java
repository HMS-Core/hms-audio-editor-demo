/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.recycle;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

/**
 * GridView effect of horizontal paging
 * The default value is 1 row and each page has three columns. To customize the number of rows and columns,
 * call the PageRecyclerView#setPageSize(int, int) method to customize the number of rows
 * before calling the PageRecyclerView#setAdapter(RecyclerView.Adapter) method.
 *
 */
public class PageRecyclerView extends RecyclerView {

    private Context mContext = null;

    private PageAdapter myAdapter = null;

    // Superminimum effective slip distance
    private int shortestDistance;

    // 手指按下的X轴坐标
    private float downX = 0f;

    // X-axis coordinate pressed by the finger
    private float slideDis = 0f;

    private int totalPage = 0;

    private int currentPage = 1;

    // Current X-axis position
    private float scrollXpos = 0f;

    // Number of rows
    private int spanRow = 1;

    // Columns per page
    private int spanColumn = 3;

    // Page Spacing
    private int pageMargin = 0;

    // Indicator Layout
    private PageIndicatorView mIndicatorView = null;

    public PageRecyclerView(Context context) {
        this(context, null);
    }

    public PageRecyclerView(Context context, AttributeSet attributSet) {
        this(context, attributSet, 0);
    }

    public PageRecyclerView(Context context, AttributeSet attributSet, int style) {
        super(context, attributSet, style);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;

        setLayoutManager(new AutoGridLayoutManager(
                mContext, spanRow, AutoGridLayoutManager.HORIZONTAL, false));
        setOverScrollMode(OVER_SCROLL_NEVER);
    }

    public void setIndicator(PageIndicatorView indicatorView) {
        this.mIndicatorView = indicatorView;
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        shortestDistance = getMeasuredWidth() / 4;
    }

    /**
     * Update Page Number Indicators and Related Data.
     */
    private void update() {
        int tempAllPages = ((int) Math.ceil(myAdapter.dataList.size() / (double) (spanRow * spanColumn))); // Calculate the total number of pages
        if (tempAllPages != totalPage) {
            mIndicatorView.initIndicator(tempAllPages);
            // The page number decreases and the current page is the last page.
            if (tempAllPages < totalPage && currentPage == totalPage) {
                currentPage = tempAllPages;
                smoothScrollBy(-getWidth(), 0); // Performing a Rollover
            }
            mIndicatorView.setSelectedPage(currentPage - 1);
            totalPage = tempAllPages;
        }
    }

    /**
     * Set the number of rows and columns per page
     *
     * @param row    Number of rows. < = 0 indicates that the default number of rows is used.
     * @param column Number of columns on each page. < = 0 indicates that the default number of columns on each page is used.
     */
    public void setPageSize(int row, int column) {
        this.spanRow = row <= 0 ? this.spanRow : row;
        this.spanColumn = column <= 0 ? this.spanColumn : column;
        setLayoutManager(new AutoGridLayoutManager(
                mContext, this.spanRow, AutoGridLayoutManager.HORIZONTAL, false));
    }

    @Override
    public void setAdapter(Adapter tmpAdapter) {
        super.setAdapter(tmpAdapter);
        this.myAdapter = (PageAdapter) tmpAdapter;
        update();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (downX == 0) {
                    downX = event.getX();
                }
                if (currentPage == totalPage && downX - event.getX() >= 0) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                slideDis = event.getX() - downX;
                if (Math.abs(slideDis) > shortestDistance) {
                    // Sliding distance is sufficient, and page turning is performed.
                    if (slideDis > 0) {
                        // Previous Page
                        currentPage = currentPage == 1 ? 1 : currentPage - 1;
                    } else {
                        // Next Page
                        currentPage = currentPage == totalPage ? totalPage : currentPage + 1;
                    }
                    // Modify Indicator Selection
                    mIndicatorView.setSelectedPage(currentPage - 1);
                }
                // Performing a Rollover
                smoothScrollBy((int) ((currentPage - 1) * getWidth() - scrollXpos), 0);
                downX = 0;
                return true;
            default:
                break;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void onScrolled(int dx, int dy) {
        scrollXpos += dx;
        super.onScrolled(dx, dy);
    }

    public class PageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<?> dataList = null;
        private CallBack mCallBack = null;
        private int itemWidth = 0;
        private int itemCount = 0;

        public PageAdapter(List<?> data, CallBack callBack) {
            this.dataList = data;
            this.mCallBack = callBack;
            itemCount = dataList.size() + spanRow * spanColumn;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (itemWidth <= 0) {
                itemWidth = (parent.getWidth() - pageMargin * 2) / spanColumn;
            }

            RecyclerView.ViewHolder holder = mCallBack.onCreateViewHolder(parent, viewType);

            holder.itemView.measure(0, 0);
            holder.itemView.getLayoutParams().width = itemWidth;
            holder.itemView.getLayoutParams().height = holder.itemView.getMeasuredHeight();

            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (spanColumn == 1) {
                // Each item is separated from the left and right pages of each pageMargin.
                holder.itemView.getLayoutParams().width = itemWidth + pageMargin * 2;
                holder.itemView.setPadding(pageMargin, 0, pageMargin, 0);
            } else {
                int m = position % (spanRow * spanColumn);
                if (m < spanRow) {
                    // The distance between the items on the left of each page and the left of the pageMargin
                    holder.itemView.getLayoutParams().width = itemWidth + pageMargin;
                    holder.itemView.setPadding(pageMargin, 0, 0, 0);
                } else if (m >= spanRow * spanColumn - spanRow) {
                    // The distance between the items on the right of each page and the right pageMargin
                    holder.itemView.getLayoutParams().width = itemWidth + pageMargin;
                    holder.itemView.setPadding(0, 0, pageMargin, 0);
                } else {
                    // Normal display in the middle
                    holder.itemView.getLayoutParams().width = itemWidth;
                    holder.itemView.setPadding(0, 0, 0, 0);
                }
            }

            if (position < dataList.size()) {
                holder.itemView.setVisibility(View.VISIBLE);
                mCallBack.onBindViewHolder(holder, position);
            } else {
                holder.itemView.setVisibility(View.INVISIBLE);
            }

        }

        @Override
        public int getItemCount() {
            return itemCount;
        }

        public void remove(int position) {
            if (position < dataList.size()) {
                dataList.remove(position);
                itemCount--;
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, currentPage * spanRow * spanColumn);
                update();
            }
        }

    }

    public interface CallBack {

        /**
         * Create vieHolder.
         *
         * @param parent parent container
         * @param viewType viewType
         * @return RecyclerView.ViewHolder
         */
        RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType);

        /**
         * Bind data to viewHolder.
         *
         * @param holder parent container
         * @param position position
         */
        void onBindViewHolder(RecyclerView.ViewHolder holder, int position);

    }
}

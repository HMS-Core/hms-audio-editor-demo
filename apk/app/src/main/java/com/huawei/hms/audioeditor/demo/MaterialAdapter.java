/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2016-2019. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hms.audioeditor.common.network.http.ability.util.network.NetworkStartup;
import com.huawei.hms.audioeditor.demo.widget.comment.RCommandAdapter;
import com.huawei.hms.audioeditor.sdk.materials.network.response.MaterialsCutContent;
import com.huawei.hms.audioeditor.sdk.util.SmartLog;
import com.huawei.hms.audioeditor.ui.R;
import com.huawei.hms.audioeditor.ui.common.adapter.comment.RViewHolder;
import com.huawei.hms.audioeditor.ui.common.listener.OnClickRepeatedListener;
import com.huawei.hms.audioeditor.demo.util.SizeUtils;
import com.huawei.hms.audioeditor.demo.util.StringUtil;
import com.huawei.hms.audioeditor.demo.util.TimeUtils;

import java.util.List;


import static com.huawei.hms.audioeditor.sdk.materials.network.response.MaterialsCutContent.DOWNLOADED;
import static com.huawei.hms.audioeditor.sdk.materials.network.response.MaterialsCutContent.DOWNLOADING;
import static com.huawei.hms.audioeditor.sdk.materials.network.response.MaterialsCutContent.UNDOWNLOAD;

/**
 * @since 2021/1/16
 */
public class MaterialAdapter extends RCommandAdapter<MaterialsCutContent> {
    private static final String TAG = "SoundEffectItemAdapter";

    private OnItemClickListener onClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        onClickListener = listener;
    }

    public MaterialAdapter(Context context, List<MaterialsCutContent> list, int layoutId) {
        super(context, list, layoutId);
    }

    @Override
    protected void convert(
            RViewHolder holder, MaterialsCutContent materialsCutContent, int dataPosition, int position) {
        ImageView mMusicPictureIv = holder.getView(R.id.music_icon);
        TextView mNameTv = holder.getView(R.id.music_name_tv);
        TextView mDurationTv = holder.getView(R.id.local_duration_tv);
        ImageView mMusicDownloadIv = holder.getView(R.id.music_download_icon);
        ProgressBar progressBar = holder.itemView.findViewById(R.id.progress_bar);
        TextView mDownloadPbTv = holder.itemView.findViewById(R.id.progress_value_text);
        TextView mUseTv = holder.getView(R.id.music_use_tv);
        FrameLayout mProgressLayout = holder.getView(R.id.music_download_progress_layout);
        mNameTv.setMaxWidth(SizeUtils.screenWidth(mContext) - SizeUtils.dp2Px(mContext, 195));
        MaterialsCutContent item = mList.get(dataPosition);

        mNameTv.setText(item.getContentName());

        try {
            float duration = Float.parseFloat(item.getMaterialDuration()) * 1000;
            mDurationTv.setText(TimeUtils.makeTimeString(mContext, (long) duration));
        } catch (NumberFormatException e) {
            SmartLog.e(TAG, "mDurationTv NumberFormatException!");
        }

        if (!StringUtil.isEmpty(item.getLocalPath())) {
            mProgressLayout.setVisibility(View.GONE);
            mMusicDownloadIv.setVisibility(View.GONE);
            mUseTv.setVisibility(View.VISIBLE);
            mDownloadPbTv.setText("");
            item.setStatus(DOWNLOADED);
        }

        if (item.getStatus() == UNDOWNLOAD) {
            mMusicDownloadIv.setVisibility(View.VISIBLE);
            mProgressLayout.setVisibility(View.GONE);
            mUseTv.setVisibility(View.GONE);
            mDownloadPbTv.setText("");
        }

        if (item.getStatus() == DOWNLOADING) {
            mMusicDownloadIv.setVisibility(View.GONE);
            mProgressLayout.setVisibility(View.VISIBLE);
            mUseTv.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(
                new OnClickRepeatedListener(
                        v -> {
                            if (onClickListener != null) {
                                if (!StringUtil.isEmpty(item.getLocalPath())) {
                                    onClickListener.onItemClick(position, dataPosition);
                                }
                            }
                        }));
        mMusicDownloadIv.setOnClickListener(
                new OnClickRepeatedListener(
                        v -> {
                            if (onClickListener != null) {
                                if (!NetworkStartup.isNetworkConn()) {
                                    String msg = holder.itemView.getResources().getString(R.string.text_to_audio_error_2);
                                    Toast.makeText(holder.itemView.getContext(), msg, Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                mProgressLayout.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.VISIBLE);
                                mMusicDownloadIv.setVisibility(View.GONE);
                                item.setStatus(DOWNLOADING);
                                onClickListener.onDownloadClick(position, dataPosition);
                            }
                        }));
        mUseTv.setOnClickListener(
                new OnClickRepeatedListener(
                        v -> {
                            if (onClickListener != null) {
                                onClickListener.onPlayClick(position, dataPosition);
                            }
                        }));
    }


    public interface OnItemClickListener {
        /**
         * click
         *
         * @param aPosition     UI Location
         * @param aDataPosition Data Location
         */
        void onItemClick(int aPosition, int aDataPosition);

        /**
         * 下载
         *
         * @param aPosition     UI Location
         * @param aDataPosition Data Location
         */
        void onDownloadClick(int aPosition, int aDataPosition);

        /**
         * 使用点击
         *
         * @param aPosition     UI Location
         * @param aDataPosition Data Location
         */
        void onPlayClick(int aPosition, int aDataPosition);
    }
}

/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.restful;

import android.media.AudioFormat;
import android.text.TextUtils;
import android.util.Log;

import com.huawei.hms.audioeditor.common.network.http.ability.util.AppContext;
import com.huawei.hms.audioeditor.demo.util.PCMToWav;
import com.huawei.hms.audioeditor.sdk.ChangeSoundCallback;
import com.huawei.hms.audioeditor.sdk.materials.network.MaterialsDownloadCallBack;
import com.huawei.hms.audioeditor.sdk.util.FileUtil;

import java.io.File;

public class TtsingCloudManager {
    private static final String TAG = "TtsingCloudManager";

    private boolean isFinished = false;

    private String currentTaskId;

    public void postTtsingAsync(String accompanimentId, String lyric, String outputDir, String outputName, int singType, int timbre, TtsingTaskListener callback) {
        isFinished = false;
        if (singType == TtsingConstant.SING_COMPOSE_TYPE_PRESET) {
            TtsingRestfulApi.getInstance().createPresetAsyncTask(accompanimentId, lyric, timbre, new TtsingTaskListener<String>() {
                @Override
                public void onResult(String taskId) {
                    currentTaskId = taskId;
                    queryTaskStatus(taskId, outputDir, outputName, callback);
                }

                @Override
                public void onFail(String taskId, String errorCode) {
                    callback.onFail(taskId, errorCode);
                }
            });
        } else {
            TtsingRestfulApi.getInstance().createXmlAsyncTask(timbre, AppContext.getContext(), new TtsingTaskListener<String>() {
                @Override
                public void onResult(String taskId) {
                    currentTaskId = taskId;
                    queryTaskStatus(taskId, outputDir, outputName, callback);
                }

                @Override
                public void onFail(String taskId, String errorCode) {
                    callback.onFail(taskId, errorCode);
                }
            });
        }
    }

    private void queryTaskStatus(String taskId, String saveDir, String saveName, TtsingTaskListener callback) {
        new Thread(() -> {
            while (!isFinished) {
                TtsingRestfulApi.getInstance().queryTaskStatus(AppContext.getContext(), taskId, new TtsingTaskListener<TtsingQueryResp>() {
                    @Override
                    public void onResult(TtsingQueryResp result) {
                        if (result == null) {
                            callback.onFail(taskId, "-1");
                            isFinished = true;
                            return;
                        }

                        // completed
                        if ("0".equals(result.getRetData().getTaskStatus())) {
                            isFinished = true;
                            TtsingQueryResultBean resultBean = new TtsingQueryResultBean();
                            resultBean.setFileId(result.getRetData().getTaskId());
                            resultBean.setUrl(result.getRetData().getResultUrl());
                            downloadResource(taskId, resultBean, saveDir, saveName, callback);
                        }
                    }

                    @Override
                    public void onFail(String taskId, String errorCode) {
                        isFinished = true;
                        callback.onFail(taskId, errorCode);
                    }

                });
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }).start();
    }

    public void postTtsingCancle(TtsingTaskListener listener) {
        isFinished = true;
        if (TextUtils.isEmpty(currentTaskId)) {
            return;
        }
        TtsingRestfulApi.getInstance().postTtsingCancle(currentTaskId, listener);
    }

    public void downloadResource(String taskId, TtsingQueryResultBean resultBean, String saveDir, String saveName, TtsingTaskListener callback) {
        String downloadUrl = resultBean.getUrl();
        String pcmName = String.valueOf(System.currentTimeMillis());
        TtsingRestfulApi.getInstance().downloadResource(AppContext.getContext(), downloadUrl, saveDir, pcmName, new MaterialsDownloadCallBack() {
            @Override
            public void onDownloadSuccess(File file) {
                String convertWaveFile =
                    PCMToWav.convertWaveFile(
                        file.getAbsolutePath(),
                        saveDir + File.separator + saveName,
                        48000,
                        AudioFormat.CHANNEL_IN_FRONT,
                        AudioFormat.ENCODING_PCM_16BIT);
                callback.onResult(convertWaveFile);
            }

            @Override
            public void onDownloading(int progress) {}

            @Override
            public void onDownloadFailed(int errorCode) {
                callback.onFail(taskId, String.valueOf(errorCode));
            }
        });
    }
}

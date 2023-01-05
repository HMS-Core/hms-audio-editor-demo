/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.restful;

public interface TtsingTaskListener<T> {
    /**
     * task success
     *
     * @param result result
     */
    void onResult(T result);

    /**
     * task failed
     *
     * @param taskId task id
     * @param errorCode error code
     */
    void onFail(String taskId, String errorCode);
}

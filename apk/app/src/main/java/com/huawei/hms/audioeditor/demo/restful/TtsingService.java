/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.restful;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface TtsingService {

    // Invoking the Synchronous Interface (Streaming)
    @POST("/v1/audioeditor/gateway/ai/ttsing/sync")
    Call<ResponseBody> postTtsingSyncTask(@Body Object object);

    // Interface for Creating an Asynchronous Task
    @POST("/v1/audioeditor/gateway/ai/ttsing/async/task/create")
    Call<TtsingCreateResultBean> createAsyncTask(@Body Object object);

    // Querying the Status of an Asynchronous Song Synthesis Task
    @POST("/v1/audioeditor/gateway/ai/ttsing/async/task/status")
    Call<TtsingQueryResp> queryAsyncTask(@Body Object object);

    // Canceling a Specified Asynchronous Task
    @POST("/v1/audioeditor/gateway/ai/ttsing/async/task/cancel")
    Call<ResponseBody> cancelTtsingAsyncTask(@Body Object object);
}

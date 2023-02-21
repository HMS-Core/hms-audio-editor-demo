/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.restful;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import com.huawei.hms.audioeditor.HAEEditorLibraryApplication;
import com.huawei.hms.audioeditor.common.agc.HAEApplication;
import com.huawei.hms.audioeditor.common.agc.HAEApplicationSetting;
import com.huawei.hms.audioeditor.common.grs.GrsUtils;
import com.huawei.hms.audioeditor.common.network.http.ability.util.AppContext;
import com.huawei.hms.audioeditor.common.utils.CloseUtils;
import com.huawei.hms.audioeditor.common.utils.GsonUtils;
import com.huawei.hms.audioeditor.demo.restful.bean.Accompaniment;
import com.huawei.hms.audioeditor.demo.restful.bean.SongBean;
import com.huawei.hms.audioeditor.demo.restful.bean.SongConfigBean;
import com.huawei.hms.audioeditor.sdk.HAEErrorCode;
import com.huawei.hms.audioeditor.sdk.HAEMaterialsManageFile;
import com.huawei.hms.audioeditor.sdk.materials.network.MaterialsDownloadCallBack;
import com.huawei.hms.audioeditor.sdk.materials.network.inner.resp.base.RequestParamsIn;
import com.huawei.hms.audioeditor.sdk.materials.network.utils.CountryResolver;
import com.huawei.hms.audioeditor.sdk.util.SmartLog;
import com.huawei.hms.framework.common.NetworkUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TtsingRestfulApi {
    private static final String TAG = "TtsingRestfulApi";

    private static TtsingRestfulApi instance;

    private static SongConfigBean songConfigBean = null;

    private static final String SONG_JSON_NAME = "song_config.json";

    private TtsingService ttsingService;

    private TtsingRestfulApi() {
        initHttpClient();
        initSongConfig();
    }

    private String taskId = "";


    public static TtsingRestfulApi getInstance() {
        if (instance == null) {
            instance = new TtsingRestfulApi();
        }
        return instance;
    }

    private void initSongConfig() {
        String songJson = getStringJson(AppContext.getContext(), SONG_JSON_NAME);
        songConfigBean = GsonUtils.fromJson(songJson, SongConfigBean.class);
    }

    private void initHttpClient() {
        String demain = GrsUtils.getBusinessUrl(HAEEditorLibraryApplication.getContext());  // 域名
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> Log.i("okhttp request ", message));
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.connectTimeout(40, TimeUnit.SECONDS);
        httpClient.writeTimeout(40, TimeUnit.SECONDS);
        httpClient.readTimeout(40, TimeUnit.SECONDS);
        httpClient.addInterceptor(logging);
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {

                final HAEApplicationSetting applicationOptions;
                if (HAEApplication.getInstance().getAppSetting() != null) {
                    applicationOptions = HAEApplication.getInstance().getAppSetting();
                } else {
                    applicationOptions = HAEApplicationSetting.fromResource(HAEEditorLibraryApplication.getContext());
                }
                UUID uuid = UUID.randomUUID();

                Request original = chain.request();
                Request request = original.newBuilder()
                        .header(RequestParamsIn.X_REQUEST_ID, String.valueOf(uuid))
                        .header(RequestParamsIn.X_PACKAGE_NAME,  HAEEditorLibraryApplication.getContext().getPackageName())
                        .header(RequestParamsIn.X_COUNTRY_CODE, new CountryResolver(HAEEditorLibraryApplication.getContext(), false).getCountryCode())
                        .header(RequestParamsIn.HMS_APPLICATION_ID, applicationOptions.getAppId())
                        .header(RequestParamsIn.CERT_FINGER_PRINT, applicationOptions.getCertFingerprint())
                        .header(RequestParamsIn.X_AUTHORIZATION, "Bearer " + HAEApplication.getInstance().getAuthorizationToken())
                        .method(original.method(), original.body())
                        .build();
                return chain.proceed(request);
            }
        });


        OkHttpClient client = httpClient.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(demain)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ttsingService = retrofit.create(TtsingService.class);
    }

    private static String getStringJson(Context context, String jsonName) {
        if (TextUtils.isEmpty(jsonName)) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        InputStreamReader inputStreamReader = null;
        BufferedReader bf = null;
        try {
            AssetManager manager = context.getAssets();
            inputStreamReader = new InputStreamReader(manager.open(jsonName), StandardCharsets.UTF_8);
            bf = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bf.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            SmartLog.e(TAG, e.getMessage());
        } finally {
            CloseUtils.close(inputStreamReader);
            CloseUtils.close(bf);
        }
        return builder.toString();
    }

    private String[] convertSongLyricToLyrics(String accompanimentId, String input) {
        String lyric = new String(input);
        if (TextUtils.isEmpty(lyric)) {
            return null;
        }
        List<Accompaniment> accompaniments = songConfigBean.getAccompaniments();
        int index = -1;
        for (int i = 0; i < accompaniments.size(); i++) {
            if (accompanimentId.equals(accompaniments.get(i).getId())) {
                index = i;
            }
        }
        if (index == -1) {
            Log.e(TAG, "no accompaniment id pare");
            return null;
        }

        SongBean songBean = songConfigBean.getEditSong().get(index);
        List<Integer> numbers = songBean.getNumber();
        String[] lyrics = new String[numbers.size()];
        lyric.replace(",", "");
        if(lyric.length() <= numbers.get(0)) {
            for (int i = 0; i < lyrics.length; i++) {
                lyrics[i] = lyric;
            }
            return lyrics;
        }
        for (int i = 0; i < numbers.size(); i++) {
            while (lyric.length() < numbers.get(i)) {
                lyric += input;
            }
            lyrics[i] = lyric.substring(0, numbers.get(i));
            lyric = lyric.substring(numbers.get(i));
        }
        return lyrics;
    }

    private PostTtsingPresetText initPresetRequestBody(String accompanimentId, String songLyric, int timbre) {
        PostTtsingPresetText shortText = new PostTtsingPresetText();
        PostTtsingPresetText.ConfigBean configBean = new PostTtsingPresetText.ConfigBean();
        configBean.setType(timbre);
        configBean.setOutputEncoderFormat(0);  // Audio encoding format 0: pcm
        shortText.setConfig(configBean);
        String[] lyric = convertSongLyricToLyrics(accompanimentId, songLyric);

        PostTtsingPresetText.DataBean dataBean = new PostTtsingPresetText.DataBean();
        dataBean.setLyrics(lyric);
        dataBean.setAccompanimentId(accompanimentId);
        dataBean.setIsAutoFill("true");
        dataBean.setLanguage("chinese");
        shortText.setData(dataBean);
        return shortText;
    }

    private PostTtsingText initRequestBody(int timbre, Context context) {
        PostTtsingText shortText = new PostTtsingText();
        PostTtsingText.ConfigBean configBean = new PostTtsingText.ConfigBean();
        configBean.setType(timbre);
        configBean.setOutputEncoderFormat(0);  // Audio encoding format 0: pcm
        shortText.setConfig(configBean);
        InputStreamReader inputStreamReader = null;
        BufferedReader bf = null;
        StringBuilder builder = new StringBuilder();
        try {
            AssetManager manager = context.getAssets();
            inputStreamReader = new InputStreamReader(manager.open("tts_sing_test.musicxml"), StandardCharsets.UTF_8);
            bf = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bf.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            Log.e(TAG, "open musicxml failed");
        } finally {
            CloseUtils.close(inputStreamReader);
            CloseUtils.close(bf);
        }

        String lyric = builder.toString();

        PostTtsingText.DataBean dataBean = new PostTtsingText.DataBean();
        dataBean.setLyric(lyric);
        dataBean.setLanguage("chinese");
        shortText.setData(dataBean);
        return shortText;
    }

    public void queryTaskStatus(Context context, String taskId, TtsingTaskListener queryListener) {
        Map request = new HashMap<>();
        request.put("taskId", taskId);
        Call<TtsingQueryResp> call = ttsingService.queryAsyncTask(request);
        call.enqueue(new Callback<TtsingQueryResp>(){

            @Override
            public void onResponse(Call<TtsingQueryResp> call, Response<TtsingQueryResp> response) {
                if (response == null) {
                    return;
                }
                if (response.errorBody() != null){
                    try {
                        queryListener.onFail(null, response.errorBody().string());
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }
                    return;
                }
                queryListener.onResult(response.body());
            }

            @Override
            public void onFailure(Call<TtsingQueryResp> call, Throwable t) {
                Log.e(TAG, "query async task fail: " + t.getMessage());
                queryListener.onFail(taskId, "-1");
            }
        });
    }

    public void createPresetAsyncTask(String accompanimentId, String songLyric, int timbre, TtsingTaskListener taskListener) {
        PostTtsingPresetText shortText = initPresetRequestBody(accompanimentId, songLyric, timbre);
        Call<TtsingCreateResultBean> call = ttsingService.createAsyncTask(shortText);
        call.enqueue(new Callback<TtsingCreateResultBean>() {
            @Override
            public void onResponse(Call<TtsingCreateResultBean> call, Response<TtsingCreateResultBean> response) {
                if (response == null) {
                    return;
                }
                if(response.errorBody() != null) {
                    try {
                        taskListener.onFail(null, response.errorBody().string());
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }
                    return;
                }
                if (response.body().getRetData() == null) {
                    taskListener.onFail(null, String.valueOf(response.code()));
                    return;
                }
                taskId = response.body().getRetData().getTaskId();
                taskListener.onResult(taskId);
            }

            @Override
            public void onFailure(Call<TtsingCreateResultBean> call, Throwable t) {
                Log.e(TAG, "create async task fail: " + t.getMessage());
                taskListener.onFail(taskId, "-1");
            }
        });
    }

    public void createXmlAsyncTask(int timbre, Context context, TtsingTaskListener taskListener) {
        PostTtsingText shortText = initRequestBody(timbre, context);
        Call<TtsingCreateResultBean> call = ttsingService.createAsyncTask(shortText);
        call.enqueue(new Callback<TtsingCreateResultBean>() {
            @Override
            public void onResponse(Call<TtsingCreateResultBean> call, Response<TtsingCreateResultBean> response) {
                if (response == null) {
                    return;
                }
                if(response.errorBody() != null) {
                    try {
                        taskListener.onFail(null, response.errorBody().string());
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }
                    return;
                }
                if (response.body() == null) {
                    taskListener.onFail(null, String.valueOf(response.code()));
                    return;
                }
                if (response.body().getRetData() == null) {
                    taskListener.onFail(null, String.valueOf(response.code()));
                    return;
                }
                taskId = response.body().getRetData().getTaskId();
                taskListener.onResult(taskId);
            }

            @Override
            public void onFailure(Call<TtsingCreateResultBean> call, Throwable t) {
                Log.e(TAG, "create async xml task fail: " + t.getMessage());
                taskListener.onFail(taskId, "-1");
            }
        });
    }

    public void postTtsingCancle(String taskId, TtsingTaskListener listener) {
        Map request = new HashMap();
        request.put("taskId", taskId);
        Call<ResponseBody> call = ttsingService.cancelTtsingAsyncTask(request);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    listener.onFail(taskId, "-1");
                    return;
                }
                if(response.errorBody() != null) {
                    try {
                        listener.onFail(taskId, response.errorBody().string());
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }
                    return;
                }
                listener.onResult(taskId);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "cancel async task fail: " + t.getMessage());
                listener.onFail(taskId, "-1");
            }
        });
    }

    public void downloadResource(Context context, String downloadUrl, String saveDir, String saveName, final MaterialsDownloadCallBack callBack) {
        if (!NetworkUtil.isNetworkAvailable(context)) {
            if (callBack != null) {
                callBack.onDownloadFailed(HAEErrorCode.FAIL_NO_NETWORK);
            }
            return;
        }
        HAEMaterialsManageFile fileManage = new HAEMaterialsManageFile();
        fileManage.downloadResource(downloadUrl, saveDir, saveName, callBack);
    }
}

/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.viewmodel;

import java.util.List;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.huawei.hms.audioeditor.ui.api.DraftInfo;
import com.huawei.hms.audioeditor.ui.api.HAEUIManager;

public class MainViewModel extends AndroidViewModel {
    private MutableLiveData<List<DraftInfo>> mDraftProjects = new MutableLiveData<>();

    public MainViewModel(@NonNull Application application) {
        super(application);
    }

    public void initDraftProjects() {
        new Thread(() -> {
            List<DraftInfo> draftProjects = HAEUIManager.getInstance().getDraftList();
            if (draftProjects != null) {
                mDraftProjects.postValue(draftProjects);
            }
        }).start();
    }

    public MutableLiveData<List<DraftInfo>> getDraftProjects() {
        return mDraftProjects;
    }
}

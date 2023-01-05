/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.restful.bean;

import com.huawei.hms.audioeditor.ui.common.utils.KeepOriginal;

import androidx.annotation.NonNull;

@KeepOriginal
public class Accompaniment implements Cloneable {
    private String id;

    private String materialId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMaterialId() {
        return materialId;
    }

    public void setMaterialId(String materialId) {
        this.materialId = materialId;
    }

    @NonNull
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "Accompaniment{" +
                "id='" + id + '\'' +
                ", materialId='" + materialId + '\'' +
                '}';
    }
}

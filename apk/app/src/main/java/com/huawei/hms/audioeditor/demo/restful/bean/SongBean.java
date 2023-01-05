/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.restful.bean;

import com.huawei.hms.audioeditor.ui.common.utils.KeepOriginal;

import java.util.List;

import androidx.annotation.NonNull;

@KeepOriginal
public class SongBean implements Cloneable{
    private String name;
    private List<String> lyrics;
    private List<Integer> number;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getLyrics() {
        return lyrics;
    }

    public void setLyrics(List<String> lyrics) {
        this.lyrics = lyrics;
    }

    public List<Integer> getNumber() {
        return number;
    }

    public void setNumber(List<Integer> number) {
        this.number = number;
    }

    @NonNull
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "SongBean{" +
                "name='" + name + '\'' +
                ", lyrics=" + lyrics +
                ", number=" + number +
                '}';
    }
}

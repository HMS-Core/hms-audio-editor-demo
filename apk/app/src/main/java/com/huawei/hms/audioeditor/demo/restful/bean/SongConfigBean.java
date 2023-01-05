/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.restful.bean;

import com.huawei.hms.audioeditor.ui.common.utils.KeepOriginal;

import java.util.ArrayList;
import java.util.List;

@KeepOriginal
public class SongConfigBean {
    private List<SongBean> song;
    private List<Accompaniment> accompaniments;

    public List<SongBean> getEditSong() {
        List<SongBean> songs = new ArrayList<>();
        if (song != null) {
            songs.addAll(song);
        }
        return songs;
    }

    public List<Accompaniment> getAccompaniments() {
        List<Accompaniment> accompanimentList = new ArrayList<>();
        if (accompaniments != null) {
            accompanimentList.addAll(accompaniments);
        }
        return accompanimentList;
    }

    public void setSong(List<SongBean> song) {
        this.song = song;
    }

    public void setAccompaniments(List<Accompaniment> accompaniments) {
        this.accompaniments = accompaniments;
    }
}

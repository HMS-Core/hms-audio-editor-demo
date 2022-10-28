/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

/**
 * Save the draft name and path.
 */
public class MediaData implements Parcelable {
    /**
     * draft name
     */
    private String name;

    /**
     * draft path
     */
    private String path;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MediaData data = (MediaData) o;
        return Objects.equals(name, data.name)
            && Objects.equals(path, data.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, path);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.path);
    }

    public MediaData() {}

    protected MediaData(Parcel in) {
        this.name = in.readString();
        this.path = in.readString();
    }

    public static final Creator<MediaData> CREATOR =
            new Creator<MediaData>() {
                @Override
                public MediaData createFromParcel(Parcel source) {
                    return new MediaData(source);
                }

                @Override
                public MediaData[] newArray(int size) {
                    return new MediaData[size];
                }
            };
}

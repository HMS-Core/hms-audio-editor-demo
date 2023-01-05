/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.restful;

public class TtsingQueryResultBean {
    private String url;

    private String fileId;

    public String getUrl() {
        return url;
    }

    public String getFileId() {
        return fileId;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    @Override
    public String toString() {
        return "DivideResultBean{" +
                "url='" + url + '\'' +
                ", fileId='" + fileId + '\'' +
                '}';
    }

}

/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.restful;

import java.io.Serializable;

public class PostTtsingPresetText implements Serializable {
    private DataBean data;
    private ConfigBean config;

    public static class DataBean implements Serializable {

        private String[] lyrics;
        private String language;
        private String accompanimentId;
        private String isAutoFill;

        public void setLyrics(String[] lyrics) {
            this.lyrics = lyrics;
        }

        public void setAccompanimentId(String accompanimentId) {
            this.accompanimentId = accompanimentId;
        }

        public void setIsAutoFill(String isAutoFill) {
            this.isAutoFill = isAutoFill;
        }

        public void setLanguage(String language) {
            this.language = language;
        }
    }

    public static class ConfigBean implements Serializable {

        private int type;
        private int outputEncoderFormat;

        public void setType(int type) {
            this.type = type;
        }

        public void setOutputEncoderFormat(int outputEncoderFormat) {
            this.outputEncoderFormat = outputEncoderFormat;
        }
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public void setConfig(ConfigBean config) {
        this.config = config;
    }
}

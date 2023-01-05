/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.restful;

public class TtsingCreateResultBean {
    String retCode;
    String retMsg;
    private RetData retData;
    public RetData getRetData() {
        return retData;
    }
    public static class RetData {
        String taskStatusMsg;
        String taskId;
        String taskStatus;

        public String getTaskStatusMsg() {
            return taskStatusMsg;
        }

        public void setTaskStatusMsg(String taskStatusMsg) {
            this.taskStatusMsg = taskStatusMsg;
        }

        public String getTaskId() {
            return taskId;
        }

        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }

        public String getTaskStatus() {
            return taskStatus;
        }

        public void setTaskStatus(String taskStatus) {
            this.taskStatus = taskStatus;
        }
    }

}

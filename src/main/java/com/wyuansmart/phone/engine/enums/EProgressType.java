package com.wyuansmart.phone.engine.enums;


import java.text.SimpleDateFormat;
import java.util.Date;

/****************************
 * 任务进度定义
 * by chaser 2019/05/22
 * **************************/
public enum EProgressType {
    /**********************************************************************
     *   1：下载获取文件   2.下载完成,解压中    3： 解压完成，解析运行中
     *   4. 任务成功完成
     *   5：任务因为某种原因失败, 6：用户停止
     *****************************************************************/
    none(0,"none"),
    download(1,"download"),
    unzip(2,"unzip"),
    running(3,"running"),
    succeed(4,"succeed"),
    failed(5,"failed"),
    stopped(6,"stopped");

    private int value;
    private String message;

    EProgressType(final int value, final String str) {
        this.value = value;
        this.message = str;
    }

    public int getValue() {
        return value;
    }
    public String getMessage() {
        return message;
    }

}

package com.wyuansmart.phone.engine.manager.diffusion;

import com.wyuansmart.phone.engine.communication.http.diffusion.vo.Txt2imgResponse;

import java.util.Calendar;

/**
 * 绘画状态
 */
public class DrawStatus {
    /**
     * 等待执行，排队中
     */
    public final static int WAIT_RUN_STATUS = -1;

    /**
     * 正在初始化
     */
    public final static int INIT_STATUS = 0;
    /**
     * 正在初始化
     */
    public final static int RUN_STATUS = 1;
    /**
     * 完成
     */
    public final static int END_STATUS = 2;

    /**
     * 状态 -1
     */
    private int status = WAIT_RUN_STATUS;

    private int queueIndex = -1;

    private Txt2imgResponse response;

    private Calendar createTime = Calendar.getInstance();

    private Calendar endTime;

    private Float lastProgress = null;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        if (status == END_STATUS){
            endTime = Calendar.getInstance();
        }
        this.status = status;
    }

    public int getQueueIndex() {
        return queueIndex;
    }

    public void setQueueIndex(int queueIndex) {
        this.queueIndex = queueIndex;
    }

    public Txt2imgResponse getResponse() {
        return response;
    }

    public void setResponse(Txt2imgResponse response) {
        this.response = response;
    }

    public Calendar getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Calendar createTime) {
        this.createTime = createTime;
    }

    public Calendar getEndTime() {
        return endTime;
    }

    public void setEndTime(Calendar endTime) {
        this.endTime = endTime;
    }

    public Float getLastProgress() {
        return lastProgress;
    }

    public void setLastProgress(Float lastProgress) {
        this.lastProgress = lastProgress;
    }
}

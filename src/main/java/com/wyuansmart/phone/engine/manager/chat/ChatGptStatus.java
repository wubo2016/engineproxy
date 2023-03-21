package com.wyuansmart.phone.engine.manager.chat;

import com.wyuansmart.phone.engine.communication.http.chat.vo.ChatResponse;

import java.util.Calendar;

public class ChatGptStatus {

    /**
     * 等待执行，排队中
     */
    public final static int WAIT_RUN_STATUS = -1;
    /**
     * 正在请求
     */
    public final static int RUN_STATUS = 0;
    /**
     * 完成
     */
    public final static int END_STATUS = 1;


    /**
     * 状态 -1
     */
    private int status = WAIT_RUN_STATUS;

    private int queueIndex = -1;

    private ChatResponse chatResponse;

    private Calendar startTime = Calendar.getInstance();

    /**
     * 预计需要运行多少秒
     */
    private long expectedTime = 20000;

    private Calendar endTime;

    private String user;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public ChatResponse getChatResponse() {
        return chatResponse;
    }

    public void setChatResponse(ChatResponse chatResponse) {
        this.chatResponse = chatResponse;
    }

    public Calendar getStartTime() {
        return startTime;
    }

    public void setStartTime(Calendar startTime) {
        this.startTime = startTime;
    }

    public Calendar getEndTime() {
        return endTime;
    }

    public void setEndTime(Calendar endTime) {
        this.endTime = endTime;
    }

    public long getExpectedTime() {
        return expectedTime;
    }

    public void setExpectedTime(long expectedTime) {
        this.expectedTime = expectedTime;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getQueueIndex() {
        return queueIndex;
    }

    public void setQueueIndex(int queueIndex) {
        this.queueIndex = queueIndex;
    }

    /**
     * 获取进度
     * @return
     */
    public float getProgress(){
        if (status < RUN_STATUS){
            return 0.0f;
        }else if(startTime == null){
            return 0.0f;
        }else if(status == END_STATUS){
            return 1.0f;
        }else {
            Calendar now = Calendar.getInstance();
            long t = now.getTimeInMillis() - startTime.getTimeInMillis();
            if (t < 0){
                startTime = now;
                return 0.0f;
            }

            float progress = (float) t / (float) expectedTime;
            if (progress > 1.0f){
                return 0.99f;
            }else {
                return progress;
            }
        }
    }
}

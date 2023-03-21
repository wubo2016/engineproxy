package com.wyuansmart.phone.engine.manager.diffusion;

import java.util.Calendar;

public class ImageCache {

    /**
     * 图片唯一访问地址
     */
    private String url;

    /**
     * 图片二进制数据
     */
    private byte[] imageData;

    /**
     * 创建时间
     */
    private Calendar calendar = Calendar.getInstance();

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }
}

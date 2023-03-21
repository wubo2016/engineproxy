package com.wyuansmart.phone.engine.communication.http.diffusion.vo;

import com.wyuansmart.phone.common.core.entity.cloud.AuditingResponse;
import com.wyuansmart.phone.engine.communication.http.diffusion.vo.ProgressResponse;

import java.util.ArrayList;
import java.util.Calendar;

public class Txt2imgResponse extends ProgressResponse {
    /**
     * 生的图片地址
     */
    private String imageUrl;
    private String info;
    private Boolean is_generating;
    private Float duration;
    private Float average_duration;

    private AuditingResponse auditingResponse;

    private Calendar createTime = Calendar.getInstance();

    private ArrayList<String> imageUrls = new ArrayList<>();

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void addImageUrl(String imageUrl) {
        if (imageUrls == null){
            imageUrls = new ArrayList<>();
        }
        if(this.imageUrl == null){
            this.imageUrl = imageUrl;
        }
        imageUrls.add(imageUrl);
    }

    public void setImageUrls(ArrayList<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public ArrayList<String> getImageUrls(){
        return imageUrls;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Boolean getIs_generating() {
        return is_generating;
    }

    public void setIs_generating(Boolean is_generating) {
        this.is_generating = is_generating;
    }

    public Float getDuration() {
        return duration;
    }

    public void setDuration(Float duration) {
        this.duration = duration;
    }

    public Float getAverage_duration() {
        return average_duration;
    }

    public void setAverage_duration(Float average_duration) {
        this.average_duration = average_duration;
    }

    public Calendar getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Calendar createTime) {
        this.createTime = createTime;
    }

    public AuditingResponse getAuditingResponse() {
        return auditingResponse;
    }

    public void setAuditingResponse(AuditingResponse auditingResponse) {
        this.auditingResponse = auditingResponse;
    }

    @Override
    public String toString() {
        return "Txt2imgResponse{" +
                "imageUrl='" + imageUrl + '\'' +
                ", info='" + info + '\'' +
                ", is_generating=" + is_generating +
                ", duration=" + duration +
                ", average_duration=" + average_duration +
                ", auditingResponse=" + auditingResponse +
                ", createTime=" + createTime +
                ", imageUrls=" + imageUrls +
                '}';
    }
}

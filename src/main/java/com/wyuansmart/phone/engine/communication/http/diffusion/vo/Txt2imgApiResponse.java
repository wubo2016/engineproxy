package com.wyuansmart.phone.engine.communication.http.diffusion.vo;

import com.wyuansmart.phone.common.core.entity.cloud.AuditingResponse;
import com.wyuansmart.phone.engine.communication.http.diffusion.vo.ProgressResponse;

import java.util.ArrayList;

public class Txt2imgApiResponse extends ProgressResponse {
    private ArrayList<String> images;
    private String info;
    private String parameters;
    private AuditingResponse auditingResponse;


    public ArrayList<String> getImages() {
        return images;
    }

    public void setImages(ArrayList<String> images) {
        this.images = images;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "Txt2imgApiResponse{" +
                ", info='" + info + '\'' +
                ", parameters='" + parameters + '\'' +
                ", auditingResponse=" + auditingResponse +
                '}';
    }
}

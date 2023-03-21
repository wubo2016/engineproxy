package com.wyuansmart.phone.engine.vo;

import io.swagger.v3.oas.annotations.media.Schema;

public class ImageAuditingRequest {
    @Schema(description = "审核的图片地址")
    private String url;

    @Schema(description = "检测的类型 porn,ads",defaultValue = "porn")
    private String detectType;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDetectType() {
        return detectType;
    }

    public void setDetectType(String detectType) {
        this.detectType = detectType;
    }
}

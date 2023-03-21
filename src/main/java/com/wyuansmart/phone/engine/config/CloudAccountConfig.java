package com.wyuansmart.phone.engine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

public class CloudAccountConfig {
    private String type;

    private boolean enable;

    private boolean supportStorage = true;

    private boolean supportAuditing = false;

    private String endpoint;

    private String accessKeyId;

    private String accessKeySecret;

    private String bucketName;

    private int port = 0;

    private int downloadPort = 0;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getDownloadPort() {
        return downloadPort;
    }

    public void setDownloadPort(int downloadPort) {
        this.downloadPort = downloadPort;
    }

    public boolean isSupportStorage() {
        return supportStorage;
    }

    public void setSupportStorage(boolean supportStorage) {
        this.supportStorage = supportStorage;
    }

    public boolean isSupportAuditing() {
        return supportAuditing;
    }

    public void setSupportAuditing(boolean supportAuditing) {
        this.supportAuditing = supportAuditing;
    }
}

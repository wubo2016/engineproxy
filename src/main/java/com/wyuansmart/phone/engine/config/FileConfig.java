package com.wyuansmart.phone.engine.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileConfig {

    /**
     * 本地存储路径
     */
    @Value("${service.img.localPath:/data/mars/picdata/}")
    private String localPath = "/data/mars/picdata/";

    /**
     * 解压路径
     */
    @Value("${service.img.unzipPath:/data/mars/picdata/unzip/}")
    private String unzipPath = "/data/mars/picdata/unzip";

    /**
     *  远程存储路径
     */
    @Value("${service.img.urlPath:/picdata/}")
    private String urlPath = "/picdata/";

    @Value("${spring.application.name:sersync}")
    private String appName;


    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getUrlPath() {
        return urlPath;
    }

    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getUnzipPath() {
        return unzipPath;
    }

    public void setUnzipPath(String unzipPath) {
        this.unzipPath = unzipPath;
    }
}

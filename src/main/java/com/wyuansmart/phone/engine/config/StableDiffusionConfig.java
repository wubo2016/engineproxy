package com.wyuansmart.phone.engine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "server.stable.diffusion")
public class StableDiffusionConfig {

    /**
     * 图片本地缓存时长 多少分钟
     */
    private long imageCacheTime = 10;

    /**
     * 绘画引擎
     */
    private List<DiffusionEngineConfig> engines;

    public List<DiffusionEngineConfig> getEngines() {
        return engines;
    }

    public void setEngines(List<DiffusionEngineConfig> engines) {
        this.engines = engines;
    }

    public long getImageCacheTime() {
        return imageCacheTime;
    }

    public void setImageCacheTime(long imageCacheTime) {
        this.imageCacheTime = imageCacheTime;
    }
}

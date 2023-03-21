package com.wyuansmart.phone.engine.communication.http.diffusion.vo;

/**
 * 查询绘画进度的应答
 */
public class ProgressResponse {
    private boolean active;
    private boolean queued;
    private boolean completed;
    private Float progress;
    private Float eta;
    private Float aestheticScore;
    private String live_preview;
    private Integer id_live_preview;
    private String textinfo;
    private Integer queueIndex = -1;

    public Integer getQueueIndex() {
        return queueIndex;
    }

    public void setQueueIndex(Integer queueIndex) {
        this.queueIndex = queueIndex;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isQueued() {
        return queued;
    }

    public void setQueued(boolean queued) {
        this.queued = queued;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Float getProgress() {
        return progress;
    }

    public void setProgress(Float progress) {
        this.progress = progress;
    }

    public Float getEta() {
        return eta;
    }

    public void setEta(Float eta) {
        this.eta = eta;
    }

    public Float getAestheticScore() {
        return aestheticScore;
    }

    public void setAestheticScore(Float aestheticScore) {
        this.aestheticScore = aestheticScore;
    }

    public String getLive_preview() {
        return live_preview;
    }

    public void setLive_preview(String live_preview) {
        this.live_preview = live_preview;
    }

    public Integer getId_live_preview() {
        return id_live_preview;
    }

    public void setId_live_preview(Integer id_live_preview) {
        this.id_live_preview = id_live_preview;
    }

    public String getTextinfo() {
        return textinfo;
    }

    public void setTextinfo(String textinfo) {
        this.textinfo = textinfo;
    }

    @Override
    public String toString() {
        return "ProgressResponse{" +
                "active=" + active +
                ", queued=" + queued +
                ", completed=" + completed +
                ", progress=" + progress +
                ", eta=" + eta +
                ", aestheticScore=" + aestheticScore +
                ", id_live_preview=" + id_live_preview +
                ", textinfo='" + textinfo + '\'' +
                '}';
    }
}

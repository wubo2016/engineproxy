package com.wyuansmart.phone.engine.config;

public class DiffusionEngineConfig {
    /**
     * 服务地址跟端口
     */
    private String serviceAddress;

    /**
     * 设置默认开启的模型
     */
    private String checkpoint;

    private boolean enable;

    private ControlNetConfig controlNet;


    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getCheckpoint() {
        return checkpoint;
    }

    public void setCheckpoint(String checkpoint) {
        this.checkpoint = checkpoint;
    }

    public ControlNetConfig getControlNet() {
        return controlNet;
    }

    public void setControlNet(ControlNetConfig controlNet) {
        this.controlNet = controlNet;
    }

    @Override
    public String toString() {
        return "DiffusionEngineConfig{" +
                "serviceAddress='" + serviceAddress + '\'' +
                ", checkpoint='" + checkpoint + '\'' +
                ", enable=" + enable +
                '}';
    }
}

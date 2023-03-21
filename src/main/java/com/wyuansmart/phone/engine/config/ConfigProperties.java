package com.wyuansmart.phone.engine.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConfigProperties {
    @Value("${server.engine.proxy.management.ip: 127.0.0.1}")
    private String managementServerIp;

    @Value("${server.engine.proxy.management.port: 1226}")
    private int managementServerPort;

    @Value("${server.engine.proxy.management.moduleId: 0}")
    private int moduleId;

    @Value("${server.engine.proxy.management.enable: false}")
    private boolean enable;

    /**
     * 节点或设备序列号
     */
    @Value("${server.engine.proxy.management.serial: }")
    private String serial;
    /*
    *软件版本
     */
    @Value("${server.engine.proxy.management.version: v1.0.0}")
    private String version;


    public String getManagementServerIp() {
        return managementServerIp;
    }

    public int getManagementServerPort() {
        return managementServerPort;
    }

    public int getModuleId() {
        return moduleId;
    }

    public void setManagementServerIp(String managementServerIp) {
        this.managementServerIp = managementServerIp;
    }

    public void setManagementServerPort(int managementServerPort) {
        this.managementServerPort = managementServerPort;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getVersion() {
        return version;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}

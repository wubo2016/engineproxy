package com.wyuansmart.phone.engine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@ConfigurationProperties(prefix = "server.cloud")
public class CloudConfig {

    private ArrayList<CloudAccountConfig> accounts;

    public ArrayList<CloudAccountConfig> getAccounts() {
        return accounts;
    }

    public void setAccounts(ArrayList<CloudAccountConfig> accounts) {
        this.accounts = accounts;
    }

    /**
     * 获取云存储账号
     * @return
     */
    public CloudAccountConfig getStorageAccount(){
        if(this.accounts == null){
            return null;
        }

        for (CloudAccountConfig config : accounts){
            if (config.isEnable() && config.isSupportStorage()){
                return config;
            }
        }
        return null;
    }

    /**
     * 获取内容审核账户配置信息
     * @return
     */
    public CloudAccountConfig getAuditingAccount(){
        if(this.accounts == null){
            return null;
        }

        for (CloudAccountConfig config : accounts){
            if (config.isEnable() && config.isSupportAuditing()){
                return config;
            }
        }
        return null;
    }
}

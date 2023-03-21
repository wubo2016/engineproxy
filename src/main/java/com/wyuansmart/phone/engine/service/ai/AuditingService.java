package com.wyuansmart.phone.engine.service.ai;

import com.wyuansmart.phone.common.core.cloud.AbsCloud;
import com.wyuansmart.phone.common.core.cloud.CloudFactory;
import com.wyuansmart.phone.common.core.entity.cloud.AuditingResponse;
import com.wyuansmart.phone.engine.config.CloudAccountConfig;
import com.wyuansmart.phone.engine.config.CloudConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * 内容审核服务
 */
@Service
public class AuditingService {
    @Autowired
    private CloudConfig cloudConfig;

    /**
     * 云服务对象
     */
    private AbsCloud absCloud;

    @PostConstruct
    public void init() {
        CloudAccountConfig accountConfig = cloudConfig.getAuditingAccount();
        if (accountConfig == null){
            return;
        }

        absCloud = CloudFactory.createCloudStorage(accountConfig.getType());
        absCloud.setServerAddress(accountConfig.getEndpoint());
        absCloud.setAccessKey(accountConfig.getAccessKeyId());
        absCloud.setSecretKey(accountConfig.getAccessKeySecret());
        absCloud.setBucketName(accountConfig.getBucketName());
        absCloud.setPort(accountConfig.getPort());
        absCloud.setDownloadPort(accountConfig.getDownloadPort());
        int ret = absCloud.initClient();
        return;
    }

    /**
     * 图片内容审核
     * @param url
     * @return
     */
    public AuditingResponse imageAuditing(String url){
        if (absCloud == null){
            return null;
        }
        return absCloud.imageAuditing(url);
    }

}

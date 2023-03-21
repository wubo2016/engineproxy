package com.wyuansmart.phone.engine.service.ai;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.wyuansmart.phone.common.core.cloud.CloudFactory;
import com.wyuansmart.phone.common.core.cloud.translate.AbsCloudTranslate;
import com.wyuansmart.phone.common.core.cloud.translate.TencentCloudTranslate;
import com.wyuansmart.phone.engine.config.CloudAccountConfig;
import com.wyuansmart.phone.engine.config.TranslateConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class TranslateService {

    @Autowired
    private TranslateConfig config;

    private AbsCloudTranslate absCloudTranslate;

    @PostConstruct
    public void init() {
        absCloudTranslate = CloudFactory.createCloudTranslate(config.getType());
        absCloudTranslate.setServerAddress(config.getEndpoint());
        absCloudTranslate.setAccessKey(config.getAccessKeyId());
        absCloudTranslate.setRegion(config.getRegion());
        absCloudTranslate.setAppId(config.getAppId());
        absCloudTranslate.setSecretKey(config.getAccessKeySecret());
        absCloudTranslate.initClient();
    }

    /**
     * 文本翻译
     * @param source 源语言类型。auto：自动识别 zh：简体中文
     *                         zh-TW：繁体中文 en：英语
     *                         ja：日语， ko：韩语
     *
     * @param sourceText 待翻译的文本，文本统一使用utf-8格式编码 低于2000字符
     * @param target 目标语言类型 zh：简体中文
     *                         zh-TW：繁体中文 en：英语
     *                         ja：日语， ko：韩语
     * @return 翻译结果
     */
    public String textTranslate(String source,String sourceText,String target) throws TencentCloudSDKException {
        if(StringUtils.isEmpty(source)){
            source = "auto";
        }

        if(StringUtils.isEmpty(target)){
            target = "en";
        }

        return absCloudTranslate.textTranslate(source,sourceText,target);
    }
}

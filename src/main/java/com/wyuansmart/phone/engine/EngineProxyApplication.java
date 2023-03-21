package com.wyuansmart.phone.engine;

import com.wyuansmart.phone.common.client.AppNodeClient;
import com.wyuansmart.phone.common.protobuf.NodeRegisterProto;
import com.wyuansmart.phone.engine.config.ConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;


import java.util.Properties;

@SpringBootApplication
public class EngineProxyApplication extends SpringBootServletInitializer {

    @Autowired
    private ConfigProperties properties;

    private static Logger LOG = LoggerFactory.getLogger(EngineProxyApplication.class);
    private static Class<EngineProxyApplication> applicationClass = EngineProxyApplication.class;

    public static void main(String[] args) {
        Properties props = new Properties();
        new SpringApplicationBuilder(EngineProxyApplication.class).properties(props).run(args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(applicationClass);
    }

    @Bean
    public AppNodeClient appNodeClient() {

        LOG.info("engine Application start connect "+ properties.getManagementServerIp() +":" +properties.getManagementServerPort());
        AppNodeClient appNodeClient = new AppNodeClient();
        appNodeClient.setSerial(properties.getSerial());
        appNodeClient.setVersion(properties.getVersion());
        if (!properties.isEnable()){
            return appNodeClient;
        }
        appNodeClient.init(NodeRegisterProto.NodeRegister.NodeType.ENGINE_PROXY_VALUE,
                "" + properties.getModuleId());

        boolean connected = false;
        int retryTime = 0;
        while (!connected && retryTime < 10) {
            try {
                LOG.info("Trying to connect to manage server, time " + retryTime);
                connected = appNodeClient.connect(properties.getManagementServerIp(), properties.getManagementServerPort());
                retryTime++;
                if (!connected) {
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                LOG.error("", e);
                retryTime++;
            }
        }
        if (!connected){
            LOG.error("Connect to  manage server failed,start exit service.");
            System.exit(-1);
        }

        return appNodeClient;
    }


}

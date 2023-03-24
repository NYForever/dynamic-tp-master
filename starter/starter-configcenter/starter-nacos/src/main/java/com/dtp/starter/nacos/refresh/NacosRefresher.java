package com.dtp.starter.nacos.refresh;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.em.ConfigFileTypeEnum;
import com.dtp.common.util.NacosUtil;
import com.dtp.core.refresh.AbstractRefresher;
import com.dtp.core.support.ThreadPoolCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * NacosRefresher related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public class NacosRefresher extends AbstractRefresher implements InitializingBean, Listener {

    private static final ThreadPoolExecutor EXECUTOR = ThreadPoolCreator.createCommonFast("nacos-listener");

    private ConfigFileTypeEnum configFileType;

    @NacosInjected
    private ConfigService configService;

    @Resource
    private Environment environment;

    /**
     * 初始化该类时，通过配置获取要连接的nacos地址
     * 并使用configService监听，即所监听的nacos配置如果有变化，就会触发监听
     */
    @Override
    public void afterPropertiesSet() {

        DtpProperties.Nacos nacos = dtpProperties.getNacos();
        configFileType = NacosUtil.getConfigType(dtpProperties, ConfigFileTypeEnum.PROPERTIES);
        String dataId = NacosUtil.deduceDataId(nacos, environment, configFileType);
        String group = NacosUtil.getGroup(nacos, "DEFAULT_GROUP");

        try {
            configService.addListener(dataId, group, this);
            log.info("DynamicTp refresher, add listener success, dataId: {}, group: {}", dataId, group);
        } catch (NacosException e) {
            log.error("DynamicTp refresher, add listener error, dataId: {}, group: {}", dataId, group, e);
        }
    }

    /**
     * 专门定义单独的线程池用于监听
     *
     * @return
     */
    @Override
    public Executor getExecutor() {
        return EXECUTOR;
    }

    /**
     * 配置变化，接受最新的配置
     *
     * @param content
     */
    @Override
    public void receiveConfigInfo(String content) {
        //调用父类的刷新方法
        refresh(content, configFileType);
    }

}

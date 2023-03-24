package com.dtp.core.refresh;

import cn.hutool.core.map.MapUtil;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.em.ConfigFileTypeEnum;
import com.dtp.common.event.RefreshEvent;
import com.dtp.core.DtpRegistry;
import com.dtp.core.handler.ConfigHandler;
import com.dtp.core.support.PropertiesBinder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * AbstractRefresher related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public abstract class AbstractRefresher implements Refresher {

    @Resource
    protected DtpProperties dtpProperties;

    /**
     *
     * @param content content
     * @param fileType file type
     */
    @Override
    public void refresh(String content, ConfigFileTypeEnum fileType) {

        if (StringUtils.isBlank(content) || Objects.isNull(fileType)) {
            log.warn("DynamicTp refresh, empty content or null fileType.");
            return;
        }

        try {
            //获取confighandler对象，解析配置文件
            ConfigHandler configHandler = ConfigHandler.getInstance();
            Map<Object, Object> properties = configHandler.parseConfig(content, fileType);
            doRefresh(properties);
        } catch (IOException e) {
            log.error("DynamicTp refresh error, content: {}, fileType: {}", content, fileType, e);
        }
    }

    protected void doRefresh(Map<Object, Object> properties) {
        if (MapUtil.isEmpty(properties)) {
            log.warn("DynamicTp refresh, empty properties.");
            return;
        }
        PropertiesBinder.bindDtpProperties(properties, dtpProperties);
        doRefresh(dtpProperties);
    }

    protected void doRefresh(DtpProperties dtpProperties) {
        //根据最新的配置刷新线程池对象
        DtpRegistry.refresh(dtpProperties);
        //发布事件
        publishEvent(dtpProperties);
    }

    private void publishEvent(DtpProperties dtpProperties) {
        RefreshEvent event = new RefreshEvent(this, dtpProperties);
        ApplicationContextHolder.publishEvent(event);
    }
}

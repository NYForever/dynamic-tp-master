package com.dtp.adapter.dubbo.alibaba;

import cn.hutool.core.map.MapUtil;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.store.DataStore;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.dto.ExecutorWrapper;
import com.dtp.core.adapter.AbstractDtpAdapter;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import static com.alibaba.dubbo.common.Constants.EXECUTOR_SERVICE_COMPONENT_KEY;


/**
 * AlibabaDubboDtpAdapter related
 *
 * @author yanhom
 * @since 1.0.6
 */
@Slf4j
@SuppressWarnings("all")
public class AlibabaDubboDtpAdapter extends AbstractDtpAdapter {

    private static final String NAME = "dubboTp";

    private static final Map<String, ExecutorWrapper> DUBBO_EXECUTORS = Maps.newHashMap();

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(NAME, dtpProperties.getDubboTp(), dtpProperties.getPlatforms());
    }

    @Override
    public Map<String, ExecutorWrapper> getExecutorWrappers() {
        return DUBBO_EXECUTORS;
    }

    @Override
    protected void initialize() {
        super.initialize();
        DataStore dataStore = ExtensionLoader.getExtensionLoader(DataStore.class).getDefaultExtension();
        Map<String, Object> executors = dataStore.get(EXECUTOR_SERVICE_COMPONENT_KEY);
        if (MapUtil.isNotEmpty(executors)) {
            executors.forEach((k, v) -> {
                val name = genTpName(k);
                val executorWrapper = new ExecutorWrapper(name, (ThreadPoolExecutor) v);
                initNotifyItems(name, executorWrapper);
                DUBBO_EXECUTORS.put(name, executorWrapper);
            });
        }
        log.info("DynamicTp adapter, alibaba dubbo executors init end, executors: {}", DUBBO_EXECUTORS);
    }

    private String genTpName(String port) {
        return NAME + "#" + port;
    }
}

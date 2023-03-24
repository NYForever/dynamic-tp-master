package com.dtp.starter.cloud.nacos.refresh;

import com.dtp.core.refresh.AbstractRefresher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.lang.NonNull;

/**
 * CloudNacosRefresher related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public class CloudNacosRefresher extends AbstractRefresher implements SmartApplicationListener {

    //监听RefreshScopeRefreshedEvent事件
    @Override
    public boolean supportsEventType(@NonNull Class<? extends ApplicationEvent> eventType) {
        return RefreshScopeRefreshedEvent.class.isAssignableFrom(eventType);
    }

    /**
     * springcloud-nacos，配置变更，只需要监听该事件，直接拿着最新的配置更新线程池对象即可
     *
     * @param event
     */
    @Override
    public void onApplicationEvent(@NonNull ApplicationEvent event) {
        if (event instanceof RefreshScopeRefreshedEvent) {
            doRefresh(dtpProperties);
        }
    }
}

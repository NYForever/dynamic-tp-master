package com.dtp.core.support;

import com.dtp.core.thread.DtpExecutor;
import com.dtp.core.thread.EagerDtpExecutor;
import lombok.Getter;

/**
 * ExecutorType related
 *
 * @author yanhom
 * @since 1.0.4
 **/
@Getter
public enum ExecutorType {

    /**
     * Executor type.
     *
     * eager类型表示处理IO密集型的线程池，类似tomcat的线程池，即当核心线程数使用完之后，后续的任务会直接创建新的线程执行，而不是放到队列中
     * 原理应该是重写了ThreadPoolExecutor的一些方法，可以重点看一下
     */
    COMMON("common", DtpExecutor.class),
    EAGER("eager", EagerDtpExecutor.class);

    private final String name;

    private final Class<?> clazz;

    ExecutorType(String name, Class<?> clazz) {
        this.name = name;
        this.clazz = clazz;
    }

    public static Class<?> getClass(String name) {
        for (ExecutorType type : ExecutorType.values()) {
            if (type.name.equals(name)) {
                return type.getClazz();
            }
        }
        return COMMON.getClazz();
    }
}

package com.dtp.core.support.runnable;

import cn.hutool.core.map.MapUtil;
import org.slf4j.MDC;

import java.util.Map;

/**
 * MdcRunnable related
 *
 * 可以在线程间传递信息
 *
 * @author yanhom
 * @since 1.0.8
 **/
public class MdcRunnable implements Runnable{

    private final Runnable runnable;

    /**
     * Saves the MDC value of the current thread
     */
    private final Map<String, String> parentMdc;

    public MdcRunnable(Runnable runnable) {
        this.runnable = runnable;
        this.parentMdc = MDC.getCopyOfContextMap();
    }

    public static MdcRunnable get(Runnable runnable) {
        return new MdcRunnable(runnable);
    }

    @Override
    public void run() {

        if (MapUtil.isEmpty(parentMdc)) {
            runnable.run();
            return;
        }

        // Assign the MDC value of the parent thread to the child thread
        for (Map.Entry<String, String> entry : parentMdc.entrySet()) {
            MDC.put(entry.getKey(), entry.getValue());
        }
        try {
            // Execute the decorated thread run method
            runnable.run();
        } finally {
            // Remove MDC value at the end of execution
            for (Map.Entry<String, String> entry : parentMdc.entrySet()) {
                MDC.remove(entry.getKey());
            }
        }
    }
}
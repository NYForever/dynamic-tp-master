package com.dtp.extension.notify.email.base;

import cn.hutool.core.date.DateTime;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.dto.*;
import com.dtp.common.em.NotifyPlatformEnum;
import com.dtp.common.em.NotifyItemEnum;
import com.dtp.common.util.CommonUtil;
import com.dtp.core.context.AlarmCtx;
import com.dtp.core.context.BaseNotifyCtx;
import com.dtp.core.context.DtpNotifyCtxHolder;
import com.dtp.core.notify.AbstractDtpNotifier;
import com.dtp.core.notify.alarm.AlarmCounter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.thymeleaf.context.Context;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.dtp.common.constant.DynamicTpConst.UNKNOWN;
import static com.dtp.core.notify.manager.NotifyItemManager.getAlarmKeys;

/**
 * DtpEmailNotifier related
 *
 * @author ljinfeng
 * @since 1.0.8
 */
@Slf4j
public class DtpEmailNotifier extends AbstractDtpNotifier {

    private static EmailNotifier emailNotifier;

    public DtpEmailNotifier() {
        super(emailNotifier = ApplicationContextHolder.getBean(EmailNotifier.class));
    }

    @Override
    public String platform() {
        return NotifyPlatformEnum.EMAIL.name().toLowerCase();
    }

    @Override
    protected String getNoticeTemplate() {
        return null;
    }

    @Override
    protected String getAlarmTemplate() {
        return null;
    }

    @Override
    protected Pair<String, String> getColors() {
        return null;
    }

    @Override
    protected String buildAlarmContent(NotifyPlatform platform, NotifyItemEnum notifyItemEnum, String template) {
        AlarmCtx alarmCtx = (AlarmCtx) DtpNotifyCtxHolder.get();
        ExecutorWrapper executorWrapper = alarmCtx.getExecutorWrapper();
        val executor = (ThreadPoolExecutor) alarmCtx.getExecutorWrapper().getExecutor();
        NotifyItem notifyItem = alarmCtx.getNotifyItem();
        AlarmInfo alarmInfo = alarmCtx.getAlarmInfo();

        String threadPoolName = alarmCtx.getExecutorWrapper().getThreadPoolName();
        val alarmCounter = AlarmCounter.countStrRrq(threadPoolName, executor);

        Context context = new Context();
        context.setVariable("serviceName", CommonUtil.getInstance().getServiceName());
        context.setVariable("serviceAddress", CommonUtil.getInstance().getIp() + ":" + CommonUtil.getInstance().getPort());
        context.setVariable("serviceEnv", CommonUtil.getInstance().getEnv());
        context.setVariable("poolName", populatePoolName(threadPoolName, executorWrapper));
        context.setVariable("alarmType", notifyItemEnum.getValue());
        context.setVariable("threshold", notifyItem.getThreshold());
        context.setVariable("corePoolSize", executor.getCorePoolSize());
        context.setVariable("maximumPoolSize", executor.getMaximumPoolSize());
        context.setVariable("poolSize", executor.getPoolSize());
        context.setVariable("activeCount", executor.getActiveCount());
        context.setVariable("largestPoolSize", executor.getLargestPoolSize());
        context.setVariable("taskCount", executor.getTaskCount());
        context.setVariable("completedTaskCount", executor.getCompletedTaskCount());
        context.setVariable("waitingTaskCount", executor.getQueue().size());
        context.setVariable("queueType", executor.getQueue().getClass().getSimpleName());
        context.setVariable("queueCapacity", getQueueCapacity(executor));
        context.setVariable("queueSize", executor.getQueue().size());
        context.setVariable("queueRemaining", executor.getQueue().remainingCapacity());
        context.setVariable("rejectType", getRejectHandlerName(executor));
        context.setVariable("rejectCount", alarmCounter.getLeft());
        context.setVariable("runTimeoutCount", alarmCounter.getMiddle());
        context.setVariable("queueTimeoutCount", alarmCounter.getRight());
        context.setVariable("lastAlarmTime", alarmInfo.getLastAlarmTime() == null ? UNKNOWN : alarmInfo.getLastAlarmTime());
        context.setVariable("alarmTime", DateTime.now());
        context.setVariable("alarmInterval", notifyItem.getInterval());
        context.setVariable("highlightVariables", getAlarmKeys(notifyItemEnum));
        return emailNotifier.processTemplateContent("alarm", context);
    }

    @Override
    protected String buildNoticeContent(NotifyPlatform platform, String template, DtpMainProp oldProp, List<String> diffs) {
        String threadPoolName = oldProp.getThreadPoolName();
        BaseNotifyCtx notifyCtx = DtpNotifyCtxHolder.get();
        ExecutorWrapper executorWrapper = notifyCtx.getExecutorWrapper();
        val executor = (ThreadPoolExecutor) executorWrapper.getExecutor();

        Context context = new Context();
        context.setVariable("serviceName", CommonUtil.getInstance().getServiceName());
        context.setVariable("serviceAddress", CommonUtil.getInstance().getIp() + ":" + CommonUtil.getInstance().getPort());
        context.setVariable("serviceEnv", CommonUtil.getInstance().getEnv());
        context.setVariable("poolName", populatePoolName(threadPoolName, executorWrapper));
        context.setVariable("oldCorePoolSize", oldProp.getCorePoolSize());
        context.setVariable("newCorePoolSize", executor.getCorePoolSize());
        context.setVariable("oldMaxPoolSize", oldProp.getMaxPoolSize());
        context.setVariable("newMaxPoolSize", executor.getMaximumPoolSize());
        context.setVariable("oldIsAllowCoreThreadTimeOut", oldProp.isAllowCoreThreadTimeOut());
        context.setVariable("newIsAllowCoreThreadTimeOut", executor.allowsCoreThreadTimeOut());
        context.setVariable("oldKeepAliveTime", oldProp.getKeepAliveTime());
        context.setVariable("newKeepAliveTime", executor.getKeepAliveTime(TimeUnit.SECONDS));
        context.setVariable("queueType", executor.getQueue().getClass().getSimpleName());
        context.setVariable("oldQueueCapacity", oldProp.getQueueCapacity());
        context.setVariable("newQueueCapacity", getQueueCapacity(executor));
        context.setVariable("oldRejectType", oldProp.getRejectType());
        context.setVariable("newRejectType", getRejectHandlerName(executor));
        context.setVariable("notifyTime", DateTime.now());
        context.setVariable("diffs", diffs != null ? diffs : Collections.emptySet());
        return emailNotifier.processTemplateContent("notice", context);
    }

}

package com.dtp.core.notify;

import com.dtp.common.constant.LarkNotifyConst;
import com.dtp.common.em.NotifyPlatformEnum;
import com.dtp.core.notify.base.Notifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import static com.dtp.common.constant.LarkNotifyConst.LARK_ALARM_JSON_STR;
import static com.dtp.common.constant.LarkNotifyConst.LARK_CHANGE_NOTICE_JSON_STR;

/**
 * DtpLarkNotifier
 *
 * @author fxbin
 * @version v1.0
 * @since 2022/4/28 23:25
 */
@Slf4j
public class DtpLarkNotifier extends AbstractDtpNotifier {

    public DtpLarkNotifier(Notifier notifier) {
        super(notifier);
    }

    @Override
    public String platform() {
        return NotifyPlatformEnum.LARK.name().toLowerCase();
    }

    @Override
    protected String getNoticeTemplate() {
        return LARK_CHANGE_NOTICE_JSON_STR;
    }

    @Override
    protected String getAlarmTemplate() {
        return LARK_ALARM_JSON_STR;
    }

    @Override
    protected Pair<String, String> getColors() {
        return new ImmutablePair<>(LarkNotifyConst.WARNING_COLOR, LarkNotifyConst.COMMENT_COLOR);
    }
}

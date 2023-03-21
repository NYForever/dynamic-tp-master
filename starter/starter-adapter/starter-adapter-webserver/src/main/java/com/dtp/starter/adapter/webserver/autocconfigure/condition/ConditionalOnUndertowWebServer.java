package com.dtp.starter.adapter.webserver.autocconfigure.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * {@link Conditional @Conditional} that checks if the application is a Undertow WebServer
 *
 * @author liu.guorong
 * @since 1.0.8
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnUndertowWebServerCondition.class)
public @interface ConditionalOnUndertowWebServer {
}

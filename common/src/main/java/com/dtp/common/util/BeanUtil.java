package com.dtp.common.util;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.util.Map;

import static com.dtp.common.em.QueueTypeEnum.buildLbq;

/**
 * BeanUtil related
 *
 * @author yanhom
 * @since 1.0.4
 **/
@Slf4j
public final class BeanUtil {

    private BeanUtil() {
    }

    public static void registerIfAbsent(BeanDefinitionRegistry registry,
                                        String beanName,
                                        Class<?> clazz,
                                        Map<String, Object> properties,
                                        Object... constructorArgs) {
        if (ifPresent(registry, beanName, clazz) || registry.containsBeanDefinition(beanName)) {
            log.warn("DynamicTp registrar, bean definition already exists, overrides with remote config, beanName: {}",
                    beanName);
            registry.removeBeanDefinition(beanName);
        }
        doRegister(registry, beanName, clazz, properties, constructorArgs);
    }

    public static boolean ifPresent(BeanDefinitionRegistry registry, String beanName, Class<?> clazz) {
        String[] beanNames = getBeanNames((ListableBeanFactory) registry, clazz);
        return ArrayUtils.contains(beanNames, beanName);
    }

    public static String[] getBeanNames(ListableBeanFactory beanFactory, Class<?> clazz) {
        return beanFactory.getBeanNamesForType(clazz, true, false);
    }

    public static void doRegister(BeanDefinitionRegistry registry,
                                  String beanName,
                                  Class<?> clazz,
                                  Map<String, Object> properties,
                                  Object... constructorArgs) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        //设置bean的参数
        for (Object constructorArg : constructorArgs) {
            builder.addConstructorArgValue(constructorArg);
        }
        //设置bean的属性
        if (CollUtil.isNotEmpty(properties)) {
            properties.forEach(builder::addPropertyValue);
        }

        //registry注入bean，需要bean的名字和BeanDefinition对象
        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
    }
}

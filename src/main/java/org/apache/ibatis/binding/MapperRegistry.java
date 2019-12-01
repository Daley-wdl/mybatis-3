/**
 * Copyright 2009-2018 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.binding;

import org.apache.ibatis.builder.annotation.MapperAnnotationBuilder;
import org.apache.ibatis.io.ResolverUtil;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;

import java.util.*;

/**
 * Mapper 注册表
 *
 * 1、在Mybatis提供的编程接口中，开发人员只需要定义好Mapper接口(如：UserDao)，开发人员无需去实现。Mybatis会利用JDK的动态代理实现 Mapper接口。
 * 2、在Mybatis中，每个Mapper接口都会对应一个MapperProxyFactory对象实例，这个对应关系在Configuration.mapperRegistry.knownMappers中。
 * 3、当getMapper()方法被调用时，Mybatis会找到相对应的MapperProxyFactory对象实例，利用这个工厂来创建一个jdk动态代理对象，是这个Mapper接口的实现类,当Mapper定义的方法被调用时，会调用MapperProxy来处理。
 * 4、MapperProxy会根据方法找到对应的MapperMethod对象来实现这次调用。
 * 5、MapperMethod对应会读取方法中的注解，从Configuration中找到相对应的MappedStatement对象，再执行
 *
 * @author Clinton Begin
 * @author Eduardo Macarron
 * @author Lasse Voss
 */
public class MapperRegistry {

    /**
     * MyBatis Configuration 对象
     */
    private final Configuration config;
    /**
     * MapperProxyFactory 的映射
     *
     * KEY：Mapper 接口, value: 代理工厂
     */
    private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<>();

    public MapperRegistry(Configuration config) {
        this.config = config;
    }

    @SuppressWarnings("unchecked")
    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        // 获得 MapperProxyFactory 对象
        final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
        // 不存在，则抛出 BindingException 异常
        if (mapperProxyFactory == null) {
            throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
        }
        // 创建 Mapper Proxy 对象
        try {
            return mapperProxyFactory.newInstance(sqlSession);
        } catch (Exception e) {
            throw new BindingException("Error getting mapper instance. Cause: " + e, e);
        }
    }

    public <T> boolean hasMapper(Class<T> type) {
        return knownMappers.containsKey(type);
    }

    public <T> void addMapper(Class<T> type) {
        // 判断，必须是接口。
        if (type.isInterface()) {
            // 已经添加过，则抛出 BindingException 异常
            if (hasMapper(type)) {
                throw new BindingException("Type " + type + " is already known to the MapperRegistry.");
            }
            boolean loadCompleted = false;
            try {
                // 添加到 knownMappers 中
                knownMappers.put(type, new MapperProxyFactory<>(type));
                // It's important that the type is added before the parser is run
                // otherwise the binding may automatically be attempted by the
                // mapper parser. If the type is already known, it won't try.
                // 解析 Mapper 的注解配置
                MapperAnnotationBuilder parser = new MapperAnnotationBuilder(config, type);
                parser.parse();
                // 标记加载完成
                loadCompleted = true;
            } finally {
                // 若加载未完成，从 knownMappers 中移除
                if (!loadCompleted) {
                    knownMappers.remove(type);
                }
            }
        }
    }

    /**
     * @since 3.2.2
     */
    public Collection<Class<?>> getMappers() {
        return Collections.unmodifiableCollection(knownMappers.keySet());
    }

    /**
     * 扫描指定包，并将符合的类，添加到 {@link #knownMappers} 中
     *
     * @since 3.2.2
     */
    public void addMappers(String packageName, Class<?> superType) {
        // 扫描指定包下的指定类
        ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<>();
        resolverUtil.find(new ResolverUtil.IsA(superType), packageName);
        Set<Class<? extends Class<?>>> mapperSet = resolverUtil.getClasses();
        // 遍历，添加到 knownMappers 中
        for (Class<?> mapperClass : mapperSet) {
            addMapper(mapperClass);
        }
    }

    /**
     * @since 3.2.2
     */
    public void addMappers(String packageName) {
        addMappers(packageName, Object.class);
    }

}

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
package org.apache.ibatis.executor;

/**
 * 错误上下文
 *
 * @author Clinton Begin
 */
public class ErrorContext {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");
    private static final ThreadLocal<ErrorContext> LOCAL = new ThreadLocal<>();

    private org.apache.ibatis.executor.ErrorContext stored;
    private String resource;
    private String activity;
    private String object;
    private String message;
    private String sql;
    private Throwable cause;

    private ErrorContext() {
    }

    public static org.apache.ibatis.executor.ErrorContext instance() {
        org.apache.ibatis.executor.ErrorContext context = LOCAL.get();
        if (context == null) {
            context = new org.apache.ibatis.executor.ErrorContext();
            LOCAL.set(context);
        }
        return context;
    }

    public org.apache.ibatis.executor.ErrorContext store() {
        org.apache.ibatis.executor.ErrorContext newContext = new org.apache.ibatis.executor.ErrorContext();
        newContext.stored = this;
        LOCAL.set(newContext);
        return LOCAL.get();
    }

    public org.apache.ibatis.executor.ErrorContext recall() {
        if (stored != null) {
            LOCAL.set(stored);
            stored = null;
        }
        return LOCAL.get();
    }

    public org.apache.ibatis.executor.ErrorContext resource(String resource) {
        this.resource = resource;
        return this;
    }

    public org.apache.ibatis.executor.ErrorContext activity(String activity) {
        this.activity = activity;
        return this;
    }

    public org.apache.ibatis.executor.ErrorContext object(String object) {
        this.object = object;
        return this;
    }

    public org.apache.ibatis.executor.ErrorContext message(String message) {
        this.message = message;
        return this;
    }

    public org.apache.ibatis.executor.ErrorContext sql(String sql) {
        this.sql = sql;
        return this;
    }

    public org.apache.ibatis.executor.ErrorContext cause(Throwable cause) {
        this.cause = cause;
        return this;
    }

    public org.apache.ibatis.executor.ErrorContext reset() {
        resource = null;
        activity = null;
        object = null;
        message = null;
        sql = null;
        cause = null;
        LOCAL.remove();
        return this;
    }

    @Override
    public String toString() {
        StringBuilder description = new StringBuilder();

        // message
        if (this.message != null) {
            description.append(LINE_SEPARATOR);
            description.append("### ");
            description.append(this.message);
        }

        // resource
        if (resource != null) {
            description.append(LINE_SEPARATOR);
            description.append("### The error may exist in ");
            description.append(resource);
        }

        // object
        if (object != null) {
            description.append(LINE_SEPARATOR);
            description.append("### The error may involve ");
            description.append(object);
        }

        // activity
        if (activity != null) {
            description.append(LINE_SEPARATOR);
            description.append("### The error occurred while ");
            description.append(activity);
        }

        // activity
        if (sql != null) {
            description.append(LINE_SEPARATOR);
            description.append("### SQL: ");
            description.append(sql.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ').trim());
        }

        // cause
        if (cause != null) {
            description.append(LINE_SEPARATOR);
            description.append("### Cause: ");
            description.append(cause.toString());
        }

        return description.toString();
    }

}

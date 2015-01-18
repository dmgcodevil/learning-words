package com.github.learningwords.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by dmgcodevil on 1/17/2015.
 */
public class RequestParameters {

    private final List<NameValuePair> pathParams = new ArrayList<>();
    private final List<NameValuePair> queryParams = new ArrayList<>();
    private final static RequestParameters EMPTY = new EmptyRequestParameters();

    public static RequestParameters empty() {
        return EMPTY;
    }

    public static RequestParameters create() {
        return new RequestParameters();
    }

    public RequestParameters addPathParam(String name, Object val) {
        pathParams.add(new NameValuePair(name, val.toString()));
        return this;
    }

    public RequestParameters addQueryParam(String name, Object val) {
        queryParams.add(new NameValuePair(name, val.toString()));
        return this;
    }

    public List<NameValuePair> getPathParams() {
        return Collections.unmodifiableList(pathParams);
    }

    public List<NameValuePair> getQueryParams() {
        return Collections.unmodifiableList(queryParams);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("pathParams=").append('[').append(toString(pathParams)).append(']');
        sb.append(", queryParams=").append('[').append(toString(queryParams)).append(']');
        return sb.toString();
    }

    public static class NameValuePair {
        private final String name;
        private final String value;

        private NameValuePair(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return new StringBuilder()
                    .append("name='").append(name).append('\'')
                    .append(", value='").append(value).append('\'').toString();
        }
    }

    private String toString(List<NameValuePair> pairs) {
        if (pairs == null || pairs.isEmpty()) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (NameValuePair pair : pairs) {
            stringBuilder.append(pair.toString()).append(",");
        }
        String result = stringBuilder.toString();
        return result.substring(0, result.length() - 1);
    }

    private static class EmptyRequestParameters extends RequestParameters {

        private static final String MESSAGE =
                "this is EmptyRequestParameters implementation and it's prohibited be modified. Create new instance of RequestParameters";

        @Override
        public RequestParameters addPathParam(String name, Object val) {
            throw new UnsupportedOperationException(MESSAGE);
        }

        @Override
        public RequestParameters addQueryParam(String name, Object val) {
            throw new UnsupportedOperationException(MESSAGE);
        }
    }
}
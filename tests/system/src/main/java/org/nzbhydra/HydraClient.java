/*
 *  (C) Copyright 2023 TheOtherP (theotherp@posteo.net)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.nzbhydra;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.PostConstruct;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class HydraClient {

    private static final Logger logger = LoggerFactory.getLogger(HydraClient.class);

    @Value("${nzbhydra.host}")
    private String nzbhydraHost;
    @Value("${nzbhydra.port}")
    private int nzbhydraPort;

    @PostConstruct
    public void logData() {
        logger.info(() -> "Using NZBHydra host " + nzbhydraHost + " and port " + nzbhydraPort);
    }

    private OkHttpClient getClient() {
        return new OkHttpClient.Builder().readTimeout(20, TimeUnit.SECONDS).build();
    }

    public HydraResponse call(String method, String endpoint, Map<String, String> headers, Object requestBody, String... parameters) {

        final HttpUrl.Builder urlBuilder = new HttpUrl.Builder().scheme("http")
            .host(nzbhydraHost)
            .port(nzbhydraPort)
            .addPathSegments(StringUtils.removeStart(endpoint, "/"));

        for (String parameter : parameters) {
            final String[] split = parameter.split("=");
            urlBuilder.addQueryParameter(split[0], split[1]);
        }
        if (endpoint.contains("internalapi") && Arrays.stream(parameters).noneMatch(x -> x.startsWith("internalApiKey"))) {
            //Must be provided to instance in docker container
            urlBuilder.addQueryParameter("internalApiKey", "internalApiKey");
        }
        final RequestBody body = createRequestBody(requestBody);
        try (Response response = getClient().newCall(new Request.Builder()
            .headers(Headers.of(headers))
            .method(method, body)
            .url(urlBuilder.build())
            .build()).execute()) {
            try (ResponseBody responseBody = response.body()) {
                return new HydraResponse(responseBody.string(), response.code());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    private static RequestBody createRequestBody(Object requestBody) {
        final RequestBody body;
        if (requestBody == null) {
            body = null;
        } else {
            String jsonRequestBody;
            if (requestBody instanceof String) {
                jsonRequestBody = (String) requestBody;
            } else {
                try {
                    jsonRequestBody = Jackson.JSON_MAPPER.writeValueAsString(requestBody);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
            body = RequestBody.create(jsonRequestBody, MediaType.parse("application/json"));
        }
        return body;
    }

    public HydraResponse get(String endpoint, Map<String, String> headers, String... parameters) {
        return call("GET", endpoint, headers, null, parameters);
    }

    public HydraResponse get(String endpoint, String... parameters) {
        return call("GET", endpoint, Collections.emptyMap(), null, parameters);
    }

    public HydraResponse put(String endpoint, Object body, String... parameters) {
        return call("PUT", endpoint, Collections.emptyMap(), body, parameters);
    }

    public HydraResponse put(String endpoint, Object body, Map<String, String> headers, String... parameters) {
        return call("PUT", endpoint, headers, body, parameters);
    }

    public HydraResponse post(String endpoint, Object body, String... parameters) {
        return call("POST", endpoint, Collections.emptyMap(), body, parameters);
    }


}

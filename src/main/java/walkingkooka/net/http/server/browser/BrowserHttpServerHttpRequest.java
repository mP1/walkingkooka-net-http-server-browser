/*
 * Copyright 2020 Miroslav Pokorny (github.com/mP1)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package walkingkooka.net.http.server.browser;

import walkingkooka.collect.list.Lists;
import walkingkooka.net.RelativeUrl;
import walkingkooka.net.Url;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.HttpProtocolVersion;
import walkingkooka.net.http.HttpTransport;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestParameterName;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonObject;
import walkingkooka.tree.json.JsonPropertyName;
import walkingkooka.tree.json.JsonString;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A {@link HttpRequest} sourced from json. Some limitations exist, including headers can only have a single value,
 * and only the body text and not binary body is supported.
 * <pre>
 * {
 *    "method": "GET",
 *    "url": "/path/to/something",
 *    "version: "HTTP/1.0",
 *    "headers": {
 *        "Content-type": "application/json",
 *        "Content-length": "9",
 *    }
 *    "bodyText": "body-text";
 * }
 * </pre>
 */
final class BrowserHttpServerHttpRequest implements HttpRequest {

    static BrowserHttpServerHttpRequest with(final JsonObject json) {
        Objects.requireNonNull(json, "json");
        return new BrowserHttpServerHttpRequest(json);
    }

    private BrowserHttpServerHttpRequest(final JsonObject json) {
        super();
        this.json = json;
    }

    @Override
    public HttpTransport transport() {
        return HttpTransport.UNSECURED;
    }

    @Override
    public HttpProtocolVersion protocolVersion() {
        return HttpProtocolVersion.with(this.json.getOrFail(VERSION).stringValueOrFail());
    }

    private final static JsonPropertyName VERSION = JsonPropertyName.with("version");

    @Override
    public RelativeUrl url() {
        return Url.parseRelative(this.json.getOrFail(URL).stringValueOrFail());
    }

    private final static JsonPropertyName URL = JsonPropertyName.with("url");

    @Override
    public HttpMethod method() {
        return HttpMethod.with(this.json.get(METHOD).orElse(GET).stringValueOrFail());
    }

    private final JsonPropertyName METHOD = JsonPropertyName.with("method");
    private final JsonString GET = JsonNode.string(HttpMethod.GET.value());

    @Override
    public Map<HttpHeaderName<?>, List<?>> headers() {
        if (null == this.headers) {
            this.headers = BrowserHttpServerHttpRequestHeadersMap.with(this.json.get(HEADERS)
                    .orElse(JsonNode.object())
                    .objectOrFail());
        }
        return this.headers;
    }

    private final static JsonPropertyName HEADERS = JsonPropertyName.with("headers");
    private BrowserHttpServerHttpRequestHeadersMap headers;

    /**
     * Getting the body as bytes is not supported and throws {@link UnsupportedOperationException}.
     */
    @Override
    public byte[] body() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long bodyLength() {
        return this.bodyText().getBytes(this.charset(HttpEntity.DEFAULT_BODY_CHARSET)).length;
    }

    @Override
    public String bodyText() {
        return this.json.get(BODY)
                .map(JsonNode::stringValueOrFail)
                .orElse("");
    }

    private final static JsonPropertyName BODY = JsonPropertyName.with("body");

    @Override
    public Map<HttpRequestParameterName, List<String>> parameters() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> parameterValues(final HttpRequestParameterName parameterName) {
        return this.parameters().getOrDefault(parameterName, Lists.empty());
    }

    private final JsonObject json;

    @Override
    public String toString() {
        return this.json.toString();
    }
}

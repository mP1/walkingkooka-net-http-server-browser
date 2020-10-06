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

import walkingkooka.Cast;
import walkingkooka.collect.list.Lists;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpProtocolVersion;
import walkingkooka.net.http.HttpStatus;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonObject;
import walkingkooka.tree.json.JsonPropertyName;

import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

/**
 * A {@link HttpResponse} that builds a JSON object with some restrictions. Only a single {@link HttpEntity} can be set,
 * and headers may only have a single value.
 */
final class BrowserHttpServerHttpResponse implements HttpResponse {

    static BrowserHttpServerHttpResponse empty() {
        return new BrowserHttpServerHttpResponse();
    }

    private BrowserHttpServerHttpResponse() {
        super();
    }

    @Override
    public void setVersion(final HttpProtocolVersion version) {
        Objects.requireNonNull(version, "version");

        this.object = this.object.set(VERSION, JsonNode.string(version.value()));
    }

    @Override
    public Optional<HttpProtocolVersion> version() {
        return this.object.get(VERSION).map(BrowserHttpServerHttpResponse::version0);
    }

    private static HttpProtocolVersion version0(final JsonNode node) {
        return HttpProtocolVersion.with(node.stringValueOrFail());
    }

    private final static JsonPropertyName VERSION = JsonPropertyName.with("version");

    @Override
    public void setStatus(final HttpStatus status) {
        Objects.requireNonNull(status, "status");

        this.object = this.object
                .set(STATUS_CODE, JsonNode.number(status.value().code()))
                .set(STATUS_MESSAGE, JsonNode.string(status.message()));
    }

    @Override
    public Optional<HttpStatus> status() {
        return this.object.get(STATUS_CODE)
                .map(BrowserHttpServerHttpResponse::status0);
    }

    private static HttpStatus status0(final JsonNode node) {
        return HttpStatusCode.withCode(node.numberValueOrFail().intValue())
                .setMessage(node.parentOrFail().objectOrFail().getOrFail(STATUS_MESSAGE).stringValueOrFail());
    }

    private final static JsonPropertyName STATUS_CODE = JsonPropertyName.with("status-code");
    private final static JsonPropertyName STATUS_MESSAGE = JsonPropertyName.with("status-message");

    @Override
    public void addEntity(final HttpEntity entity) {
        Objects.requireNonNull(entity, "entity");

        JsonObject object = this.object;
        if (object.get(HEADERS).isPresent() || object.get(BODY).isPresent()) {
            throw new IllegalArgumentException("Only 1 entity supported=" + entity);
        }

        final List<JsonNode> headers = Lists.array();

        for (final Entry<HttpHeaderName<?>, List<?>> headerAndValues : entity.headers().entrySet()) {
            final HttpHeaderName<?> header = headerAndValues.getKey();
            final List<?> values = headerAndValues.getValue();

            final int valueCount = values.size();
            switch (valueCount) {
                case 0:
                    break;
                case 1:
                    final Object value = values.get(0);
                    final JsonNode valueJsonNode = value instanceof Number ?
                            JsonNode.number(((Number) value).doubleValue()) :
                            JsonNode.string(header.headerText(Cast.to(value)));

                    headers.add(valueJsonNode.setName(JsonPropertyName.with(header.value())));
                    break;
                default:
                    throw new IllegalArgumentException("Header " + header + " contains " + valueCount + " values only 1 supported=" + values);
            }
        }

        if (!headers.isEmpty()) {
            object = object.set(HEADERS, JsonNode.object().setChildren(headers));
        }

        final String bodyText = entity.bodyText();
        if (!bodyText.isEmpty()) {
            object = object.set(BODY, JsonNode.string(bodyText));
        }

        this.object = object;
    }

    @Override
    public List<HttpEntity> entities() {
        HttpEntity entity = HttpEntity.EMPTY;

        final JsonObject object = this.object;
        final Optional<JsonNode> headers = object.get(HEADERS);
        if (headers.isPresent()) {
            for (final JsonNode headerAndValue : headers.get().children()) {
                final HttpHeaderName<?> header = HttpHeaderName.with(headerAndValue.name().value());
                final String value = headerAndValue.text();
                entity = entity.addHeader(header, Cast.to(header.parse(value)));
            }
        }

        final Optional<JsonNode> bodyText = object.get(BODY);
        if (bodyText.isPresent()) {
            entity = entity.setBodyText(bodyText.get().stringValueOrFail());
        }

        return entity.isEmpty() ?
                Lists.empty() :
                Lists.of(entity);
    }

    private final static JsonPropertyName HEADERS = JsonPropertyName.with("headers");
    private final static JsonPropertyName BODY = JsonPropertyName.with("body");

    JsonObject object = JsonNode.object();

    @Override
    public String toString() {
        return this.object.toString();
    }
}

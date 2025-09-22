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

import org.junit.jupiter.api.Test;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.MapTesting;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonPropertyName;

import java.util.List;

public final class BrowserHttpServerHttpRequestHeadersMapTest extends BrowserHttpServerTestCase<BrowserHttpServerHttpRequestHeadersMap> implements MapTesting<BrowserHttpServerHttpRequestHeadersMap, HttpHeaderName<?>, List<?>> {

    @Test
    public void testGetSameCase() {
        this.getAndCheck(BrowserHttpServerHttpRequestHeadersMap.with(JsonNode.object()
                        .set(JsonPropertyName.with(HttpHeaderName.CONTENT_LENGTH.value()), JsonNode.number(1))),
                HttpHeaderName.CONTENT_LENGTH, Lists.of(1L));
    }

    @Test
    public void testGetDifferentCase() {
        this.getAndCheck(BrowserHttpServerHttpRequestHeadersMap.with(JsonNode.object()
                        .set(JsonPropertyName.with("content-LENGTH"), JsonNode.number(1))),
                HttpHeaderName.CONTENT_LENGTH, Lists.of(1L));
    }

    @Override
    public BrowserHttpServerHttpRequestHeadersMap createMap() {
        return BrowserHttpServerHttpRequestHeadersMap.with(JsonNode.object()
                .set(JsonPropertyName.with("Content-Length"), JsonNode.number(1))
                .set(JsonPropertyName.with("Content-Type"), "text/plain")
        );
    }

    @Override
    public Class<BrowserHttpServerHttpRequestHeadersMap> type() {
        return BrowserHttpServerHttpRequestHeadersMap.class;
    }
}

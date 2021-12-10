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
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonObject;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides the {@link Map} view of headers. The get and contains methods support case insensitive access honouring {@link HttpHeaderName}.
 */
final class BrowserHttpServerHttpRequestHeadersMap extends AbstractMap<HttpHeaderName<?>, List<?>> {

    static BrowserHttpServerHttpRequestHeadersMap with(final JsonObject headers) {
        return new BrowserHttpServerHttpRequestHeadersMap(headers);
    }

    private BrowserHttpServerHttpRequestHeadersMap(final JsonObject headers) {
        super();
        this.headers = headers;
    }

    @Override
    public Set<Entry<HttpHeaderName<?>, List<?>>> entrySet() {
        return BrowserHttpServerHttpRequestHeadersMapEntrySet.with(this.headers);
    }

    @Override
    public int size() {
        return this.headers.children().size();
    }

    @Override
    public boolean containsKey(final Object key) {
        return this.get(key).size() > 0;
    }

    @Override
    public List<?> get(final Object key) {
        return key instanceof HttpHeaderName ?
                this.get0((HttpHeaderName<?>) key) :
                Lists.empty();
    }

    private List<?> get0(final HttpHeaderName<?> header) {
        return this.headers.children().stream()
                .filter(hv -> hv.name().value().equalsIgnoreCase(header.value()))
                .map(hv -> Lists.of(header.parse(hv.text())))
                .findFirst()
                .orElse(Lists.empty());
    }

    /**
     * Parses the header value which should be a string into a typed value.
     */
    private static List<?> get1(final HttpHeaderName<?> header,
                                final JsonNode node) {
        return Lists.of(header.parse(node.text()));
    }

    private final JsonObject headers;

    @Override
    public String toString() {
        return this.headers.toString();
    }
}

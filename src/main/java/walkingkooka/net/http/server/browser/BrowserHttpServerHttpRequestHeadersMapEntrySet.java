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

import walkingkooka.collect.iterator.Iterators;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonObject;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

/**
 * A {@link java.util.Set} view of all the headers in {@link BrowserHttpServerHttpRequest}.
 */
final class BrowserHttpServerHttpRequestHeadersMapEntrySet extends AbstractSet<Entry<HttpHeaderName<?>, List<?>>> {

    static BrowserHttpServerHttpRequestHeadersMapEntrySet with(final JsonObject headers) {
        return new BrowserHttpServerHttpRequestHeadersMapEntrySet(headers);
    }

    private BrowserHttpServerHttpRequestHeadersMapEntrySet(final JsonObject headers) {
        super();
        this.headers = headers;
    }

    @Override
    public Iterator<Entry<HttpHeaderName<?>, List<?>>> iterator() {
        return Iterators.mapping(this.headers.children().iterator(), BrowserHttpServerHttpRequestHeadersMapEntrySet::mapper);
    }

    private static Entry<HttpHeaderName<?>, List<?>> mapper(final JsonNode node) {
        final HttpHeaderName<?> headerName = HttpHeaderName.with(node.name().value());
        return Maps.entry(
                headerName,
                Lists.of(
                        headerName.parseValue(node.text())
                )
        );
    }

    @Override
    public int size() {
        return this.headers.children().size();
    }

    private final JsonObject headers;

    @Override
    public String toString() {
        return this.headers.toString();
    }
}

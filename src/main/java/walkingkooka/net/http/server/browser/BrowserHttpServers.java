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

import elemental2.dom.MessageEvent;
import elemental2.dom.MessagePort;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.HttpServer;
import walkingkooka.reflect.PublicStaticHelper;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Provides a factory for several {@link HttpServer} implementations that receive messages holding the request and
 * postMessage back another message holding the response.
 */
public final class BrowserHttpServers implements PublicStaticHelper {

    public static HttpServer messagePort(final BiConsumer<HttpRequest, HttpResponse> processor,
                                         final MessagePort port,
                                         final Predicate<MessageEvent<String>> messageFilter) {
        return BrowserHttpServer.with(processor, port, messageFilter);
    }

    /**
     * Stop creation
     */
    private BrowserHttpServers() {
        throw new UnsupportedOperationException();
    }
}

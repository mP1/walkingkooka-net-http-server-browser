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
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpServer;
import walkingkooka.reflect.PublicStaticHelper;

import java.util.function.Predicate;

/**
 * Provides a factory for several {@link HttpServer} implementations that receive messages holding the request and
 * postMessage back another message holding the response.
 */
public final class BrowserHttpServers implements PublicStaticHelper {

    /**
     * {@see BrowserHttpServer}
     */
    public static HttpServer messagePort(final HttpHandler httpHandler,
                                         final MessagePort port,
                                         final Predicate<MessageEvent<String>> messageFilter,
                                         final String postMessageTargetOrigin) {
        return BrowserHttpServer.with(
            httpHandler,
            port,
            messageFilter,
            postMessageTargetOrigin
        );
    }

    /**
     * Stop creation
     */
    private BrowserHttpServers() {
        throw new UnsupportedOperationException();
    }
}

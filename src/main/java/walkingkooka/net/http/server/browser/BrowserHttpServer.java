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

import elemental2.core.Transferable;
import elemental2.dom.Event;
import elemental2.dom.EventListener;
import elemental2.dom.MessageEvent;
import elemental2.dom.MessagePort;
import jsinterop.base.Js;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.HttpServer;
import walkingkooka.text.CharSequences;
import walkingkooka.tree.json.JsonNode;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * A {@link HttpServer} that accepts messages from a {@link MessagePort}, processing the request and then calls {@link MessagePort#postMessage}
 * back the response. The {@link MessagePort} can be a {@link elemental2.dom.Window} or {@link elemental2.dom.Worker}.
 */
final class BrowserHttpServer implements HttpServer {

    /**
     * Creates a new {@link BrowserHttpServer}.
     */
    static BrowserHttpServer with(final BiConsumer<HttpRequest, HttpResponse> processor,
                                  final MessagePort port,
                                  final Predicate<MessageEvent<String>> messageFilter,
                                  final String postMessageTargetOrigin) {
        Objects.requireNonNull(processor, "processor");
        Objects.requireNonNull(port, "port");
        Objects.requireNonNull(messageFilter, "messageFilter");
        CharSequences.failIfNullOrEmpty(postMessageTargetOrigin, "postMessageTargetOrigin");

        return new BrowserHttpServer(processor, port, messageFilter, postMessageTargetOrigin);
    }

    /**
     * Use factory
     */
    private BrowserHttpServer(final BiConsumer<HttpRequest, HttpResponse> processor,
                              final MessagePort port,
                              final Predicate<MessageEvent<String>> messageFilter,
                              final String postMessageTargetOrigin) {
        super();
        this.processor = processor;
        this.port = port;
        this.messageFilter = messageFilter;
        this.postMessageTargetOrigin = postMessageTargetOrigin;
    }

    // HttpServer.......................................................................................................

    /**
     * Starts the server by adding the message event listener.
     */
    @Override
    public final void start() {
        if (this.running) {
            throw new IllegalStateException("Server already running");
        }
        this.port.addEventListener(MESSAGE, this.eventListener, false);
        this.running = true;
    }

    /**
     * Stops the server by removing the message event listener.
     */
    @Override
    public final void stop() {
        if (false == this.running) {
            throw new IllegalStateException("Server not running");
        }
        this.port.removeEventListener(MESSAGE, this.eventListener);
        this.running = false;
    }

    private final static String MESSAGE = "message";
    private final MessagePort port;
    private final EventListener eventListener = this::handleEvent;
    private boolean running;

    private void handleEvent(final Event event) {
        this.handleMessageEvent(Js.cast(event));
    }

    /**
     * Before handling the message as a {@link }HttpRequest} the message filter predicate is used to test the message.
     * This allows the message.origin to be tested and more.
     */
    // @VisibleForTesting
    void handleMessageEvent(final MessageEvent<String> event) {
        if (this.messageFilter.test(event)) {
            // inputs
            final HttpRequest request = BrowserHttpServerHttpRequest.with(JsonNode.parse(event.data).objectOrFail());
            final HttpResponse response = BrowserHttpServerHttpResponse.empty();

            // process
            this.processor.accept(request, response);

            // outputs
            event.source.postMessage(response.toString(), this.postMessageTargetOrigin);
        }
    }

    private final Predicate<MessageEvent<String>> messageFilter;

    /**
     * Handles the request and produces a response.
     */
    private final BiConsumer<HttpRequest, HttpResponse> processor;

    /**
     * The {@link MessagePort#postMessage(Object, Transferable[])}
     */
    private final String postMessageTargetOrigin;
    
    @Override
    public String toString() {
        return this.processor.toString();
    }
}

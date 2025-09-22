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

import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import elemental2.dom.EventListener;
import elemental2.dom.MessageEvent;
import elemental2.dom.MessagePort;
import elemental2.dom.Window;
import elemental2.promise.Promise;
import jsinterop.base.Js;
import org.junit.jupiter.api.Test;
import walkingkooka.ToStringTesting;
import walkingkooka.collect.list.Lists;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpProtocolVersion;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpServer;
import walkingkooka.predicate.Predicates;
import walkingkooka.reflect.ClassTesting2;
import walkingkooka.reflect.JavaVisibility;

import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class BrowserHttpServerTest implements ClassTesting2<BrowserHttpServer>, ToStringTesting<BrowserHttpServer> {

    private final static HttpHandler HANDLER = (request, response) -> {
        throw new UnsupportedOperationException();
    };

    private final static Predicate<MessageEvent<String>> MESSAGE_FILTER = Predicates.always();
    private final static String TARGET_ORIGIN = "*";

    @Test
    public void testWithNullProcessorFails() {
        assertThrows(NullPointerException.class, () -> BrowserHttpServer.with(null, new TestMessagePort(), MESSAGE_FILTER, TARGET_ORIGIN));
    }

    @Test
    public void testWithNullMessagePortFails() {
        assertThrows(NullPointerException.class, () -> BrowserHttpServer.with(HANDLER, null, MESSAGE_FILTER, TARGET_ORIGIN));
    }

    @Test
    public void testWithNullMessageFilterFails() {
        assertThrows(NullPointerException.class, () -> BrowserHttpServer.with(HANDLER, new TestMessagePort(), null, TARGET_ORIGIN));
    }

    @Test
    public void testWithNullTargetOriginFails() {
        assertThrows(NullPointerException.class, () -> BrowserHttpServer.with(HANDLER, new TestMessagePort(), MESSAGE_FILTER, null));
    }

    @Test
    public void testWithEmptyTargetOriginFails() {
        assertThrows(IllegalArgumentException.class, () -> BrowserHttpServer.with(HANDLER, new TestMessagePort(), MESSAGE_FILTER, ""));
    }

    @Test
    public void testStart() {
        final TestMessagePort port = new TestMessagePort();
        final BrowserHttpServer server = BrowserHttpServer.with(HANDLER, port, MESSAGE_FILTER, TARGET_ORIGIN);
        server.start();
    }

    @Test
    public void testStartTwiceFails() {
        final TestMessagePort port = new TestMessagePort();
        final BrowserHttpServer server = BrowserHttpServer.with(HANDLER, port, MESSAGE_FILTER, TARGET_ORIGIN);
        server.start();
        assertThrows(IllegalStateException.class, () -> server.start());
    }

    @Test
    public void testStopWithStartFails() {
        final TestMessagePort port = new TestMessagePort();
        final BrowserHttpServer server = BrowserHttpServer.with(HANDLER, port, MESSAGE_FILTER, TARGET_ORIGIN);
        assertThrows(IllegalStateException.class, () -> server.stop());
    }

    @Test
    public void testStop() {
        final TestMessagePort port = new TestMessagePort();
        final BrowserHttpServer server = BrowserHttpServer.with(HANDLER, port, MESSAGE_FILTER, TARGET_ORIGIN);
        server.start();
        server.stop();
    }

    @Test
    public void testStopTwiceFails() {
        final TestMessagePort port = new TestMessagePort();
        final BrowserHttpServer server = BrowserHttpServer.with(HANDLER, port, MESSAGE_FILTER, TARGET_ORIGIN);
        server.start();
        server.stop();
        assertThrows(IllegalStateException.class, () -> server.stop());
    }

    @Test
    public void testHandleMessageEvent() {
        final TestMessagePort port = new TestMessagePort();
        final BrowserHttpServer server = BrowserHttpServer.with((request, response) -> {
            response.setStatus(HttpStatusCode.CREATED.setMessage("Custom CREATED Message 123"));
            response.setEntity(
                HttpEntity.EMPTY.setBodyText("Response-" + request.bodyText())
            );
        }, port, MESSAGE_FILTER, TARGET_ORIGIN);
        server.start();

        final List<String> postedMessage = Lists.array();

        final MessageEvent<String> event = new MessageEvent<>("message");
        event.source = new Window() {
            @Override
            public void postMessage(final Object message,
                                    final String targetOrigin) {
                postedMessage.add(message.toString());
            }
        };
        event.data = "{\"body\": \"body-text-123\"}";

        server.handleMessageEvent(event);
        server.stop();

        this.checkEquals(Lists.of("{\n" +
            "  \"status-code\": 201,\n" +
            "  \"status-message\": \"Custom CREATED Message 123\",\n" +
            "  \"body\": \"Response-body-text-123\"\n" +
            "}"), postedMessage);
    }

    /**
     * Override the key methods that are native to make things work in a JVM.
     */
    private static class TestMessagePort extends MessagePort {
        @Override
        public void addEventListener(final String type,
                                     final EventListener listener,
                                     final boolean capture) {
            this.eventListener = listener;
        }

        @Override
        public void removeEventListener(final String type,
                                        final EventListener listener) {
            assertSame(this.eventListener, listener, "removed from EventListener");
            this.eventListener = null;
        }

        @Override
        public boolean dispatchEvent(final Event event) {
            this.eventListener.handleEvent(event);
            return true;
        }

        @Override
        public void postMessage(final Object message) {
            this.messages.add(message);
        }

        EventListener eventListener;
        List<Object> messages;
    }

    //@Test(timeout = 1000)
    public Promise<Void> testWindow() {
        final MessagePort window = Js.cast(DomGlobal.window);

        final HttpServer server = BrowserHttpServers.messagePort((req, resp) -> {
                resp.setVersion(HttpProtocolVersion.VERSION_1_0);
                resp.setStatus(HttpStatusCode.withCode(999).setMessage("Custom Status Message"));
                resp.setEntity(
                    HttpEntity.EMPTY.addHeader(
                        HttpHeaderName.SERVER,
                        "TestMessageServer"
                    ).setBodyText("Response-" + req.bodyText()
                    )
                );
            }, window,
            new Predicate<>() {

                public boolean test(final MessageEvent<String> event) {
                    DomGlobal.console.log("Message filter data: " + event.data);
                    return this.counter++ == 0;
                }

                // the first message will be a HttpRequest, the next will be the HttpResponse sent back which we want to ignore.
                int counter;
            },
            "*");

        server.start();

        final List<String> messages = Lists.array();

        // will capture ALL messages, including the request and the response posted back
        window.addEventListener("message", new EventListener() {
            @Override
            public void handleEvent(final Event event) {
                final MessageEvent<String> messageEvent = Js.cast(event);
                DomGlobal.console.log("window message handleEvent: " + messageEvent.data);
                messages.add(messageEvent.data);
            }
        }, false);

        final String request = "GET /path1/file2 HTTP/1.0\r\nContent-Length: 1\r\nContent-Type: text/plain\r\n\r\nBody1234";
        DomGlobal.postMessage(request, "*");

        return new Promise<>(
            (resolve, reject) -> {
                DomGlobal.setTimeout((ignored) -> {
                        final String response = "HTTP/1.0 999 Custom Status Message\r\nServer: TestMessageServer\r\n\r\nResponse-Body1234";
                        this.checkEquals(Lists.of(request, response), messages);
                        server.stop();
                        resolve.onInvoke((Void) null);
                    },
                    500);
            });
    }

    @Test
    public void testToString() {
        this.toStringAndCheck(BrowserHttpServer.with(HANDLER, new TestMessagePort(), MESSAGE_FILTER, TARGET_ORIGIN), HANDLER.toString());
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<BrowserHttpServer> type() {
        return BrowserHttpServer.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}

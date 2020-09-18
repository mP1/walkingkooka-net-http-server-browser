/*
 * Copyright © 2020 Miroslav Pokorny
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
 */
package test;


import com.google.j2cl.junit.apt.J2clTestInput;
import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import elemental2.dom.EventListener;
import elemental2.dom.MessageEvent;
import elemental2.dom.MessagePort;
import elemental2.promise.Promise;
import jsinterop.base.Js;
import org.junit.Assert;
import org.junit.Test;
import walkingkooka.collect.list.Lists;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpProtocolVersion;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.HttpServer;
import walkingkooka.net.http.server.browser.BrowserHttpServers;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

@J2clTestInput(JunitTest.class)
public final class JunitTest {

    // Mostly copied from BrowserHttpServerTest
    @Test(timeout = 1000)
    public Promise<Void> testWindow() {
        final MessagePort window = Js.cast(DomGlobal.window);

        final HttpServer server = BrowserHttpServers.messagePort((req, resp) -> {
                    resp.setVersion(HttpProtocolVersion.VERSION_1_0);
                    resp.setStatus(HttpStatusCode.withCode(999).setMessage("Custom Status Message"));
                    resp.addEntity(HttpEntity.EMPTY.addHeader(HttpHeaderName.SERVER, "TestMessageServer").setBodyText("Response-" + req.bodyText()));
                }, window,
                new Predicate<MessageEvent<String>>() {

                    public boolean test(final MessageEvent<String> event) {
                        DomGlobal.console.log("Message filter data: " + event.data);
                        return this.counter++ == 0;
                    }

                    // the first message will be a HttpRequest, the next will be the HttpResponse sent back which we want to ignore.
                    int counter;
                });

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
        final String response = "HTTP/1.0 999 Custom Status Message\r\nServer: TestMessageServer\r\n\r\nResponse-Body1234";

        window.postMessage(request);

        return new Promise<Void>(
                (resolve, reject) -> {
                    DomGlobal.setTimeout((ignored) -> {
                                Assert.assertEquals(Lists.of(request, response), messages);
                                server.stop();
                                resolve.onInvoke((Void) null);
                            },
                            500);
                });
    }
}

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
import walkingkooka.tree.json.JsonNode;

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
                resp.setStatus(
                    HttpStatusCode.withCode(999)
                        .setMessage("Custom Status Message")
                );
                resp.setEntity(
                    HttpEntity.EMPTY.addHeader(
                        HttpHeaderName.SERVER,
                        "TestMessageServer"
                    ).setBodyText("Response-" + req.bodyText()));
            }, window,
            new Predicate<MessageEvent<String>>() {

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
                messages.add(pretty(messageEvent.data));
            }
        }, false);

        final String request = "{\n" +
            "  \"version\": \"HTTP/1.0\",\n" +
            "  \"method\": \"POST\",\n" +
            "  \"headers\": {\n" +
            "    \"Content-Type\": \"text/plain\",\n" +
            "    \"Content-Length\": 123\n" +
            "  },\n" +
            "  \"body\": \"Body123\"\n" +
            "}";
        final String response = "{\n" +
            "  \"version\": \"HTTP/1.0\",\n" +
            "  \"status-code\": 999,\n" +
            "  \"status-message\": \"Custom Status Message\",\n" +
            "  \"headers\": {\n" +
            "    \"Server\": \"TestMessageServer\"\n" +
            "  },\n" +
            "  \"body\": \"Response-Body123\"\n" +
            "}";

        DomGlobal.postMessage(request, "*");

        return new Promise<Void>(
            (resolve, reject) -> {
                DomGlobal.setTimeout((ignored) -> {
                        Assert.assertEquals(Lists.of(pretty(request), pretty(response)), messages);
                        server.stop();
                        resolve.onInvoke((Void) null);
                    },
                    500);
            });
    }

    private static String pretty(final String json) {
        return JsonNode.parse(json).toString();
    }
}

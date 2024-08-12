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
import walkingkooka.ToStringTesting;
import walkingkooka.collect.list.Lists;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpProtocolVersion;
import walkingkooka.net.http.HttpStatus;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.tree.json.JsonNode;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class BrowserHttpServerHttpResponseTest extends BrowserHttpServerTestCase<BrowserHttpServerHttpResponse>
        implements ToStringTesting<BrowserHttpServerHttpResponse> {

    @Test
    public void testEmpty() {
        final BrowserHttpServerHttpResponse response = BrowserHttpServerHttpResponse.empty();
        this.checkEquals(Optional.empty(), response.version(), "version");

        this.check(BrowserHttpServerHttpResponse.empty(),
                "{}");
    }

    // version..........................................................................................................

    @Test
    public void testSetVersionAll() {
        final BrowserHttpServerHttpResponse response = BrowserHttpServerHttpResponse.empty();

        for (final HttpProtocolVersion version : HttpProtocolVersion.values()) {
            response.setVersion(version);
            this.checkEquals(Optional.of(version), response.version(), "version");
        }
    }

    @Test
    public void testSetVersion() {
        final BrowserHttpServerHttpResponse response = BrowserHttpServerHttpResponse.empty();

        final HttpProtocolVersion version = HttpProtocolVersion.VERSION_1_0;
        response.setVersion(version);
        this.checkEquals(Optional.of(version), response.version(), "version");

        this.check(response,
                "{ \"version\": \"HTTP/1.0\"}");
    }

    @Test
    public void testSetVersionTwice() {
        final BrowserHttpServerHttpResponse response = BrowserHttpServerHttpResponse.empty();

        response.setVersion(HttpProtocolVersion.VERSION_1_0);
        response.setVersion(HttpProtocolVersion.VERSION_1_1);

        this.check(response,
                "{ \"version\": \"HTTP/1.1\"}");
    }

    // status..........................................................................................................

    @Test
    public void testSetStatusAll() {
        final BrowserHttpServerHttpResponse response = BrowserHttpServerHttpResponse.empty();

        for (final HttpStatusCode statusCode : HttpStatusCode.values()) {
            final HttpStatus status = statusCode.status();

            response.setStatus(status);
            this.checkEquals(Optional.of(status), response.status(), "status");
        }
    }

    @Test
    public void testSetStatus() {
        final BrowserHttpServerHttpResponse response = BrowserHttpServerHttpResponse.empty();

        final HttpStatus status = HttpStatusCode.FOUND.setMessage("Custom message 123");
        response.setStatus(status);
        this.checkEquals(Optional.of(status), response.status(), "status");

        this.check(response,
                "{ \"status-code\": 302, \"status-message\": \"Custom message 123\"}");
    }

    @Test
    public void testSetStatusTwice() {
        final BrowserHttpServerHttpResponse response = BrowserHttpServerHttpResponse.empty();

        response.setStatus(HttpStatusCode.OK.setMessage("Lost456"));

        final HttpStatus status = HttpStatusCode.FOUND.setMessage("Custom message 123");
        response.setStatus(status);
        this.checkEquals(Optional.of(status), response.status(), "status");

        this.check(response,
                "{ \"status-code\": 302, \"status-message\": \"Custom message 123\"}");
    }

    // setEntity........................................................................................................

    @Test
    public void testSetEntityEmpty() {
        final BrowserHttpServerHttpResponse response = BrowserHttpServerHttpResponse.empty();
        response.setEntity(HttpEntity.EMPTY);

        this.check(response, "{}");
    }

    @Test
    public void testSetEntityHeader() {
        final BrowserHttpServerHttpResponse response = BrowserHttpServerHttpResponse.empty();
        response.setEntity(HttpEntity.EMPTY.addHeader(HttpHeaderName.CONTENT_LENGTH, 1L));

        this.check(response, "{\n" +
                "  \"headers\": {\n" +
                "    \"Content-Length\": 1\n" +
                "  }\n" +
                "}");
    }

    @Test
    public void testSetEntityHeader2() {
        final BrowserHttpServerHttpResponse response = BrowserHttpServerHttpResponse.empty();
        response.setEntity(HttpEntity.EMPTY
                .addHeader(HttpHeaderName.CONTENT_TYPE, MediaType.TEXT_PLAIN)
                .addHeader(HttpHeaderName.CONTENT_LENGTH, 1L));

        this.check(response, "{\n" +
                "  \"headers\": {\n" +
                "    \"Content-Type\": \"text/plain\",\n" +
                "    \"Content-Length\": 1\n" +
                "  }\n" +
                "}");
    }

    @Test
    public void testSetEntityHeader3() {
        final BrowserHttpServerHttpResponse response = BrowserHttpServerHttpResponse.empty();
        response.setEntity(HttpEntity.EMPTY
                .addHeader(HttpHeaderName.CONTENT_LENGTH, 1L)
                .addHeader(HttpHeaderName.CONTENT_TYPE, MediaType.TEXT_PLAIN));

        this.check(response, "{\n" +
                "  \"headers\": {\n" +
                "    \"Content-Length\": 1,\n" +
                "    \"Content-Type\": \"text/plain\"\n" +
                "  }\n" +
                "}");
    }

    private void check(final BrowserHttpServerHttpResponse response,
                       final String json) {
        this.checkEquals(JsonNode.parse(json),
                response.object);
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        final BrowserHttpServerHttpResponse response = BrowserHttpServerHttpResponse.empty();
        response.setStatus(HttpStatusCode.BAD_REQUEST.setMessage("Bad request 123"));
        response.setEntity(HttpEntity.EMPTY
                .addHeader(HttpHeaderName.CONTENT_LENGTH, 1L)
                .addHeader(HttpHeaderName.CONTENT_TYPE, MediaType.parse("text/plain123"))
                .setBodyText("Body123"));

        this.toStringAndCheck(response, "{\n" +
                "  \"status-code\": 400,\n" +
                "  \"status-message\": \"Bad request 123\",\n" +
                "  \"headers\": {\n" +
                "    \"Content-Length\": 1,\n" +
                "    \"Content-Type\": \"text/plain123\"\n" +
                "  },\n" +
                "  \"body\": \"Body123\"\n" +
                "}");
    }


    @Override
    public Class<BrowserHttpServerHttpResponse> type() {
        return BrowserHttpServerHttpResponse.class;
    }
}

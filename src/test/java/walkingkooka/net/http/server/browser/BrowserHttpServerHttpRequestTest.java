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
import walkingkooka.net.Url;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.HttpProtocolVersion;
import walkingkooka.tree.json.JsonNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class BrowserHttpServerHttpRequestTest extends BrowserHttpServerTestCase<BrowserHttpServerHttpRequest>
        implements ToStringTesting<BrowserHttpServerHttpRequest> {

    @Test
    public void testGetMethod() {
        methodAndCheck(HttpMethod.GET);
    }

    @Test
    public void testPostMethod() {
        methodAndCheck(HttpMethod.POST);
    }

    @Test
    public void testCustomMethod() {
        methodAndCheck(HttpMethod.with("CustomMethod"));
    }

    private void methodAndCheck(final HttpMethod method) {
        this.methodAndCheck("{ \"method\": \"" + method + "\" }", method);
    }

    @Test
    public void testMethodMissingDefaultsGET() {
        this.methodAndCheck("{}", HttpMethod.GET);
    }

    private void methodAndCheck(final String request,
                                final HttpMethod method) {
        final BrowserHttpServerHttpRequest httpRequest = this.parse(request);
        assertEquals(method, httpRequest.method(), () -> request);
    }

    @Test
    public void testUrl() {
        urlAndCheck("/path/to/file");
    }

    @Test
    public void testUrlWithQueryParameters() {
        urlAndCheck("/path/with/query?query1=value1");
    }

    private void urlAndCheck(final String url) {
        final BrowserHttpServerHttpRequest request = this.parse("{ \"url\": \"" + url + "\" }");
        assertEquals(Url.parseRelative(url), request.url(), () -> request.toString());
    }

    @Test
    public void testVersion10() {
        versionAndCheck(HttpProtocolVersion.VERSION_1_0);
    }

    @Test
    public void testVersion11() {
        versionAndCheck(HttpProtocolVersion.VERSION_1_1);
    }

    private void versionAndCheck(final HttpProtocolVersion version) {
        final BrowserHttpServerHttpRequest request = this.parse("{ \"version\": \"" + version + "\" }");
        assertEquals(version,
                request.protocolVersion(),
                () -> request.toString());
    }

    @Test
    public void testHeadersMissing() {
        final BrowserHttpServerHttpRequest request = this.parse("{}");
        assertEquals(Lists.empty(),
                request.headers().get(HttpHeaderName.CONTENT_TYPE),
                () -> request.toString());
    }

    @Test
    public void testHeaderAbsent() {
        final BrowserHttpServerHttpRequest request = this.parse("{ \"headers\": {\"Content-Length\": 123}}");
        assertEquals(Lists.empty(),
                request.headers().get(HttpHeaderName.CONTENT_TYPE),
                () -> request.toString());
    }

    @Test
    public void testHeaderNumericValue() {
        final BrowserHttpServerHttpRequest request = this.parse("{ \"headers\": {\"Content-Length\": 123}}");
        assertEquals(Lists.of(123L),
                request.headers().get(HttpHeaderName.CONTENT_LENGTH),
                () -> request.toString());
    }

    @Test
    public void testHeaderStringValue() {
        final BrowserHttpServerHttpRequest request = this.parse("{ \"headers\": {\"Content-Type\": \"text/plain\"}}");
        assertEquals(Lists.of(MediaType.TEXT_PLAIN),
                request.headers().get(HttpHeaderName.CONTENT_TYPE),
                () -> request.toString());
    }

    @Test
    public void testHeaders() {
        final BrowserHttpServerHttpRequest request = this.parse("{ \"method\": \"POST\", \"headers\": {\"Content-Type\": \"text/plain\", \"Content-Length\": 123}}");
        assertEquals(HttpMethod.POST,
                request.method(),
                () -> request.toString());
        assertEquals(Lists.of(MediaType.TEXT_PLAIN),
                request.headers().get(HttpHeaderName.CONTENT_TYPE),
                () -> request.toString());
        assertEquals(Lists.of(123L),
                request.headers().get(HttpHeaderName.CONTENT_LENGTH),
                () -> request.toString());
    }

    @Test
    public void testBodyText() {
        final BrowserHttpServerHttpRequest request = this.parse("{ \"body\": \"abc123\"}");
        assertEquals("abc123", request.bodyText());
    }

    @Test
    public void testBodyFails() {
        assertThrows(UnsupportedOperationException.class, () -> this.parse("{ \"body\": \"abc123\"}").body());
    }

    @Test
    public void testBodyLength() {
        assertEquals(6L, this.parse("{\"headers\": {}, \"body\": \"abc123\"}").bodyLength());
    }

    @Test
    public void testBodyLengthContentTypeUtf8() {
        assertEquals(6L, this.parse("{\"headers\": {\"Content-Type\": \"text/plain;charset=UTF8\"}, \"body\": \"abc123\"}").bodyLength());
    }

    @Test
    public void testBodyLengthContentTypeUtf16() {
        assertEquals(14L, this.parse("{\"headers\": {\"Content-Type\": \"text/plain;charset=UTF16\"}, \"body\": \"abc123\"}").bodyLength());
    }

    @Test
    public void testParametersFails() {
        assertThrows(UnsupportedOperationException.class, () -> this.parse("{}").parameters());
    }

    private BrowserHttpServerHttpRequest parse(final String json) {
        return BrowserHttpServerHttpRequest.with(JsonNode.parse(json).objectOrFail());
    }

    @Test
    public void testToString() {
        this.toStringAndCheck(this.parse("{ \"method\": \"POST\", \"headers\": {\"Content-Type\": \"text/plain\", \"Content-Length\": 123}, \"body\": \"abc123\"}"),
                "{\n" +
                        "  \"method\": \"POST\",\n" +
                        "  \"headers\": {\n" +
                        "    \"Content-Type\": \"text/plain\",\n" +
                        "    \"Content-Length\": 123\n" +
                        "  },\n" +
                        "  \"body\": \"abc123\"\n" +
                        "}");
    }

    @Override
    public Class<BrowserHttpServerHttpRequest> type() {
        return BrowserHttpServerHttpRequest.class;
    }
}

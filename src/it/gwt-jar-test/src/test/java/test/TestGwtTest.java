package test;

import com.google.gwt.junit.client.GWTTestCase;

import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonPropertyName;
import walkingkooka.tree.patch.NodePatch;
import walkingkooka.tree.pointer.NodePointer;

public class TestGwtTest extends GWTTestCase {

    @Override
    public String getModuleName() {
        return "test.Test";
    }

    public void testAssertEquals() {
        assertEquals(
                1,
                1
        );
    }

    public void testPatch() {
        final JsonNode before = JsonNode.parse("{\"hello\": { \"colour\": \"replaced-1\", \"tree\": \"removed-3\"}}");

        final NodePatch<JsonNode, JsonPropertyName> patch = emptyPatch()
                .add(pointer("/added-1"), JsonNode.number(1))
                .add(pointer("/hello/colour"), JsonNode.string("green-2"))
                .remove(pointer("/hello/tree"));

        final JsonNode after = patch.apply(before);

        final JsonNode expected = JsonNode.parse("{\n" +
                "  \"hello\": {\n" +
                "    \"colour\": \"green-2\"\n" +
                "  },\n" +
                "  \"added-1\": 1\n" +
                "}");

        assertEquals(
                expected,
                after
        );
    }

    private static NodePatch<JsonNode, JsonPropertyName> emptyPatch() {
        return NodePatch.empty(JsonNode.class);
    }

    private static NodePointer<JsonNode, JsonPropertyName> pointer(final String path) {
        return NodePointer.parse(path, JsonPropertyName::with, JsonNode.class);
    }
}

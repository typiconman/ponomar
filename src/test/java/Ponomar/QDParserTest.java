package Ponomar;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;
import java.util.*;
import org.junit.jupiter.api.Test;

class QDParserTest {
    @Test void reportsDocumentStructureAndAttributes() throws Exception {
        RecordingHandler handler = parse(
            "<?xml version=\"1.0\"?><root alpha=\"one &amp; two\"><child id='7'/><!--ignored--></root>");
        assertEquals(List.of("document:start", "element:start:root", "element:start:child",
            "element:end:child", "element:end:root", "document:end"), handler.events);
        assertEquals("one & two", handler.attributes.get("root").get("alpha"));
        assertEquals("7", handler.attributes.get("child").get("id"));
    }

    @Test void expandsEntitiesAndPreservesCdata() throws Exception {
        RecordingHandler handler = parse("<root>left&lt;right&#33;<![CDATA[raw <xml> & text]]></root>");
        assertEquals(List.of("left<right!", "raw <xml> & text"), handler.text);
    }

    @Test void expandsAllBuiltInEntities() throws Exception {
        assertEquals(List.of("<>&\"'"), parse("<root>&lt;&gt;&amp;&quot;&apos;</root>").text);
    }

    @Test void supportsNestedAndEmptyElements() throws Exception {
        RecordingHandler handler = parse("<a><b><c/></b><d/></a>");
        assertEquals(List.of("document:start", "element:start:a", "element:start:b", "element:start:c",
            "element:end:c", "element:end:b", "element:start:d", "element:end:d", "element:end:a",
            "document:end"), handler.events);
    }

    @Test void normalizesLineEndings() throws Exception {
        assertEquals(List.of("one\ntwo\nthree\nfour"), parse("<root>one\r\ntwo\rthree\nfour</root>").text);
    }

    @Test void ignoresXmlDeclarationsCommentsAndDoctype() throws Exception {
        RecordingHandler handler = parse("<?xml version='1.0'?><!DOCTYPE root><!--x--><root/>");
        assertEquals(List.of("document:start", "element:start:root", "element:end:root", "document:end"),
            handler.events);
    }

    @Test void rejectsUnknownEntities() {
        Exception failure = assertThrows(Exception.class, () -> parse("<root>&unknown;</root>"));
        assertTrue(failure.getMessage().contains("Unknown entity: &unknown;"));
        assertTrue(failure.getMessage().contains("line 1"));
    }

    @Test void rejectsUnclosedAndMalformedDocuments() {
        assertTrue(assertThrows(Exception.class, () -> parse("<root><child></child>")).getMessage()
            .contains("missing end tag"));
        assertThrows(Exception.class, () -> parse("<root attr=nope/>"));
        assertThrows(Exception.class, () -> parse("<root/ junk>"));
    }

    @Test void rejectsMismatchedClosingTags() {
        assertThrows(Exception.class, () -> parse("<root><child></root></child>"));
    }

    private static RecordingHandler parse(String xml) throws Exception {
        RecordingHandler handler = new RecordingHandler();
        QDParser.parse(handler, new StringReader(xml));
        return handler;
    }

    private static final class RecordingHandler implements DocHandler {
        final List<String> events = new ArrayList<>();
        final List<String> text = new ArrayList<>();
        final Hashtable<String, Hashtable> attributes = new Hashtable<>();
        public void startDocument() { events.add("document:start"); }
        public void endDocument() { events.add("document:end"); }
        public void startElement(String tag, Hashtable attrs) {
            events.add("element:start:" + tag); attributes.put(tag, new Hashtable(attrs));
        }
        public void endElement(String tag) { events.add("element:end:" + tag); }
        public void text(String value) { text.add(value); }
    }
}

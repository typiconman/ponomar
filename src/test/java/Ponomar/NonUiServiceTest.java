package Ponomar;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NonUiServiceTest {
    @Test void assemblesUsualBeginningForPriestAndReaderServices() {
        for (int priest : List.of(0, 1)) {
            OrderedHashtable info = TestData.englishStringDayInfo(new JDate(6, 1, 2024));
            info.put("PS", Integer.toString(priest));
            Service service = new Service(info);
            String html = service.startService("xml/Services/UsualBeginning.xml");
            assertAll(() -> assertNotNull(html), () -> assertTrue(html.startsWith("<html>")),
                () -> assertTrue(html.contains("</body></html>")),
                () -> assertTrue(html.length() > 500),
                () -> assertEquals("</p>", service.closeService()));
        }
    }

    @Test void usualBeginningConvenienceApiShouldReturnTheServiceText() {
        OrderedHashtable info = TestData.englishStringDayInfo(new JDate(6, 1, 2024));
        UsualBeginning beginning = new UsualBeginning(info);
        assertTrue(beginning.getUsualBeginning().length() > 500);
    }

    @Test void readsLocalizedServiceTextAndHeaders() {
        OrderedHashtable info = TestData.englishDayInfo();
        ReadText reader = new ReadText(info);
        String text = reader.readText("xml/Services/CommonPrayers/Amen.xml");
        assertNotNull(text);
        assertFalse(text.isEmpty());
        assertEquals("Prayer of the Third Hour",
            reader.readHeader("xml/Services/CommonPrayers/PrayerTerce.xml"));
    }

    @Test void oldCommemorationReaderAndQuasiCommemorationExposeMetadata() {
        OrderedHashtable greek = TestData.englishDayInfo(); greek.put("LS", "el/");
        Commemoration commemoration = new Commemoration("B_163", greek);
        assertEquals("B_163", commemoration.getID());
        OrderedHashtable troparion = commemoration.getService("/TROPARION", "1");
        assertEquals("4", troparion.get("Tone"));
        assertTrue(troparion.get("text").toString().length() > 50);
        assertNotNull(commemoration.getRH("missing", "missing"));

        OrderedHashtable grammar = new OrderedHashtable(); grammar.put("Nominative", "Synthetic saint");
        Commemoration synthetic = new Commemoration("Synthetic saint", grammar, new OrderedHashtable());
        assertEquals("Synthetic saint", synthetic.getGrammar(""));
        assertEquals("-1", synthetic.getRank());
        assertEquals("-1", synthetic.getID());
        assertEquals("-1", synthetic.getCycle());
    }

    @Test void legacyCommemorationShouldAcceptNonNumericIds() {
        OrderedHashtable greek = TestData.englishDayInfo(); greek.put("LS", "el/");
        Commemoration commemoration = new Commemoration("B_163", greek);
        assertDoesNotThrow(() -> assertNotNull(commemoration.getGrammar("")));
    }

    @Test void reporterEmitsParserEvents() throws Exception {
        PrintStream former = System.out;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(bytes, true, StandardCharsets.UTF_8));
            Reporter.reportOnFile("Ponomar/languages/xml/lives/9135.xml");
        } finally {
            System.setOut(former);
        }
        String report = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(report.contains("start document"));
        assertTrue(report.contains("start elem: SCRIPTURE"));
        assertTrue(report.contains("end document"));
    }

    @Test void convertedTableAndTypiconUtilitiesWriteUtf8Artifacts(@TempDir Path directory) throws Exception {
        Path table = directory.resolve("saints.tsv");
        CreateTables.main(new String[] {table.toString(), "1"});
        List<String> rows = Files.readAllLines(table, StandardCharsets.UTF_8);
        assertEquals("ID\tTag\tKey\tValue", rows.get(0));
        assertTrue(rows.stream().anyMatch(line -> line.contains("Nominative")));

        Path typicon = directory.resolve("menaion.tex");
        TypiconMenologion.main(new String[] {"9", "1", typicon.toString()});
        String tex = Files.readString(typicon, StandardCharsets.UTF_8);
        assertTrue(tex.contains("\\begin{document}"));
        assertTrue(tex.contains("Beginning of the Indiction"));
        assertTrue(tex.endsWith("\\end{document}\n"));
    }

    @Test void createIndexRendererGroupsNamesAndDates() {
        Map<String, CreateIndex.Entry> entries = new LinkedHashMap<>();
        entries.put("1", new CreateIndex.Entry("Nicholas", "Saint Nicholas",
            new ArrayList<>(List.of(new JDate(12, 6, 2024)))));
        entries.put("2", new CreateIndex.Entry("Nicholas", "Another Nicholas",
            new ArrayList<>(List.of(new JDate(5, 9, 2024)))));
        String html = CreateIndex.render(entries, 2024);
        assertTrue(html.contains("ColSpan=\"2\">Nicholas"));
        assertTrue(html.contains("December 6"));
        assertTrue(html.contains("May 9"));
    }
}

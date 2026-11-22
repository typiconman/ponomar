package Ponomar;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SupportingLogicTest {
    @Test void astronomyProducesDeterministicBoundedCoordinates() {
        double[] sun = Astronomy.sunpos(0.0);
        assertEquals(0.983, sun[0], 0.002);
        assertEquals(278.9, sun[1], 0.5);
        Astronomy astronomy = new Astronomy();
        long day = new JDate(1, 1, 2024).getJulianDay();
        assertAll(
            () -> assertBetween(astronomy.lunarlong(day), 0, 360),
            () -> assertBetween(astronomy.solarlong(day), 0, 360),
            () -> assertBetween(astronomy.lunarage(day), 0, 360),
            () -> assertEquals(astronomy.lunarage(day), astronomy.lunarage(day), 0.0));
    }

    @Test void languagePackLoadsEnglishPhrasesAndSplitsValues() {
        LanguagePack language = new LanguagePack(DayInfoFixtures.englishDayInfo());
        assertEquals("Copyright ^YY by ^AA", language.Phrases.get("Copyright"));
        assertArrayEquals(new String[] {"one", "two", "three"}, language.obtainValues("one/,two/,three"));
        assertEquals(8, language.obtainValues(language.Phrases.get("Phases").toString()).length);
    }

    @Test void languagePackSupportsExplicitPathsAndGracefulMissingFileFallback() {
        OrderedHashtable info = DayInfoFixtures.englishDayInfo();
        LanguagePack explicit = new LanguagePack(
            "Ponomar/languages/en/xml/Commands/LanguagePacks.xml", info);
        assertEquals("Copyright ^YY by ^AA", explicit.Phrases.get("Copyright"));

        PrintStream former = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        LanguagePack missing;
        try {
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            missing = new LanguagePack("Ponomar/languages/does-not-exist.xml", info);
        } finally {
            System.setOut(former);
        }
        assertTrue(missing.Phrases.isEmpty());
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("Unable to find"));
    }

    @Test void helpersFindFallbackFilesAndCopyTables() {
        Helpers helpers = new Helpers(DayInfoFixtures.englishDayInfo());
        assertEquals("Ponomar/languages/en/xml/Commands/LanguagePacks.xml",
            helpers.langFileFind("en/", "xml/Commands/LanguagePacks.xml"));
        assertEquals("Ponomar/languages/en/xml/lives/9135.xml",
            helpers.langFileFind("en/", "xml/lives/9135.xml"));
        Hashtable original = new Hashtable(); original.put("year", 2024);
        Hashtable copy = helpers.deepCopy(original);
        assertEquals("2024", copy.get("year"));
        copy.put("year", "changed");
        assertEquals(2024, original.get("year"));
        JavaFileFilter filter = new JavaFileFilter();
        assertTrue(filter.accept(new File("report.html")));
        assertFalse(filter.accept(new File("report.txt")));
    }

    @Test void ruleBasedNumberUsesEnglishRomanRules() {
        RuleBasedNumber numbers = new RuleBasedNumber(DayInfoFixtures.englishDayInfo());
        assertEquals("zero", numbers.getFormattedNumber(0));
        assertEquals("I", numbers.getFormattedNumber(1));
        assertEquals("IV", numbers.getFormattedNumber(4));
        assertEquals("IX", numbers.getFormattedNumber(9));
        assertEquals("XLII", numbers.getFormattedNumber(42));
        assertEquals("MCMXCIX", numbers.getFormattedNumber(1999));
        assertEquals("5000.0", numbers.getFormattedNumber(5000));
        assertEquals(-1, numbers.ConvertToInteger("X"));
    }

    @Test void kahunaFormatsRangesAndXmlAttributes() {
        assertEquals("(doy >= 1 && doy <= 3) || (doy >= 5 && doy <= 6)",
            Kahuna.stringify(List.of(6, 2, 1, 5, 3, 3)));
        assertEquals("doy == 7", Kahuna.stringify(List.of(7)));
        assertEquals("", Kahuna.stringify(List.of()));
        assertEquals("a&amp;b&lt;c&gt;d&quot;", Kahuna.xmlEscape("a&b<c>d\""));
    }

    @Test void kahunaEntryPointRequiresExplicitInputAndOutputPaths() {
        IllegalArgumentException missingBoth = assertThrows(
            IllegalArgumentException.class, () -> Kahuna.main(new String[0]));
        IllegalArgumentException missingOutput = assertThrows(
            IllegalArgumentException.class, () -> Kahuna.main(new String[] {"input"}));
        assertAll(
            () -> assertTrue(missingBoth.getMessage().startsWith("Usage: Kahuna")),
            () -> assertEquals(missingBoth.getMessage(), missingOutput.getMessage()));
    }

    @Test void kahunaReplacesLiturgyInASeparateOutputTree(@TempDir Path temporary) throws Exception {
        Path input = Files.createDirectory(temporary.resolve("data"));
        Path output = Files.createDirectory(temporary.resolve("lives"));
        Files.writeString(input.resolve("1.tsv"),
            ";; fixture\n8\t3\t214\t134\t251\t13\t13\n", StandardCharsets.UTF_8);
        for (int file = 2; file <= 65; file++) Files.writeString(input.resolve(file + ".tsv"), ";; empty\n");
        Files.writeString(input.resolve("lectionary.tsv"), "13\t1\tEpistle\t100\tGospel\t200\n");
        Path xml = output.resolve("9135.xml");
        Files.writeString(xml, "<SAINT>\n<SERVICE>\n<LITURGY>\n<OLD/>\n</LITURGY>\n</SERVICE>\n</SAINT>\n");

        Kahuna.update(input, output);

        String updated = Files.readString(xml);
        assertFalse(updated.contains("<OLD/>"));
        assertTrue(updated.contains("Type=\"apostol\" Reading=\"Epistle\""));
        assertTrue(updated.contains("Type=\"gospel\" Reading=\"Gospel\""));
        assertTrue(updated.contains("ndayF == -251 &amp;&amp; (doy == 214)"));
    }

    @Test void kahunaUsesTheLastRowForDuplicateLegacyMatrixKeys(@TempDir Path temporary) throws Exception {
        Path input = Files.createDirectory(temporary.resolve("data"));
        Path output = Files.createDirectory(temporary.resolve("lives"));
        Files.writeString(input.resolve("1.tsv"),
            "8\t3\t214\t134\t251\t12\t12\n", StandardCharsets.UTF_8);
        Files.writeString(input.resolve("2.tsv"),
            "8\t3\t214\t134\t251\t13\t13\n", StandardCharsets.UTF_8);
        for (int file = 3; file <= 65; file++) Files.writeString(input.resolve(file + ".tsv"), "");
        Files.writeString(input.resolve("lectionary.tsv"),
            "12\t1\tOld Epistle\t99\tOld Gospel\t199\n"
                + "13\t1\tNew Epistle\t100\tNew Gospel\t200\n", StandardCharsets.UTF_8);
        Path xml = output.resolve("9135.xml");
        Files.writeString(xml, "<SAINT>\n<LITURGY>\n</LITURGY>\n</SAINT>\n");

        Kahuna.update(input, output);

        String updated = Files.readString(xml);
        assertFalse(updated.contains("Old Epistle"));
        assertFalse(updated.contains("Old Gospel"));
        assertTrue(updated.contains("New Epistle"));
        assertTrue(updated.contains("New Gospel"));
    }

    @Test void createTablesRecursivelyExpandsCompositeSaints() throws Exception {
        MenologionReader.Saint synthetic = new MenologionReader.Saint(
            "middle", "root", "", Map.of("Nominative", "Synthetic composite"), Map.of(), 0);

        List<MenologionReader.Saint> expanded = CreateTables.expanded(List.of(synthetic), id -> switch (id) {
            case "middle" -> new MenologionReader.Saint("leaf", "middle", "", Map.of(), Map.of(), 0);
            case "leaf" -> new MenologionReader.Saint("leaf", "leaf", "", Map.of(), Map.of(), 0);
            default -> throw new IllegalArgumentException(id);
        });

        assertTrue(expanded.stream().anyMatch(saint -> saint.cId().equals("middle")));
        assertTrue(expanded.stream().anyMatch(saint -> saint.cId().equals("leaf")));
    }

    @Test void kahunaRealFixtureRecreatesEveryCommittedLiturgyRule(@TempDir Path temporary) throws Exception {
        Path output = Files.createDirectory(temporary.resolve("lives"));
        Path source = Path.of("Ponomar", "languages", "xml", "lives");
        try (var files = Files.list(source)) {
            for (Path file : files.filter(path -> path.getFileName().toString().matches("9\\d+\\.xml")).toList()) {
                Files.copy(file, output.resolve(file.getFileName()));
            }
        }

        Kahuna.update(Path.of("Ponomar", "scripts", "Perl", "data"), output);

        try (var files = Files.list(output)) {
            for (Path actual : files.toList()) {
                Path expected = source.resolve(actual.getFileName());
                assertEquals(liturgyRules(expected), liturgyRules(actual), actual.getFileName().toString());
            }
        }
    }

    @Test void menologionReaderLoadsLayeredEnglishData() throws Exception {
        List<MenologionReader.Saint> saints = MenologionReader.saints(new JDate(9, 1, 2015));
        assertFalse(saints.isEmpty());
        MenologionReader.Saint indiction = saints.stream().filter(saint -> saint.cId().equals("09426"))
            .findFirst().orElseThrow();
        assertEquals("Beginning of the Indiction (that is, the New Year)",
            indiction.name().get("Nominative"));
        assertEquals(4, indiction.rank());
    }

    private static void assertBetween(double value, double minimum, double maximum) {
        assertTrue(value >= minimum && value < maximum, () -> value + " not in [" + minimum + ", " + maximum + ")");
    }

    private static List<String> liturgyRules(Path file) throws IOException {
        boolean[] inside = {false};
        List<String> rules = new ArrayList<>();
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (line.contains("<LITURGY>")) inside[0] = true;
            else if (line.contains("</LITURGY>")) inside[0] = false;
            else if (inside[0] && line.contains("<SCRIPTURE")) rules.add(line.trim());
        }
        return rules.stream().sorted().toList();
    }
}

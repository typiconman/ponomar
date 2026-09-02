package Ponomar;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class LucanJumpGoldenTest {
    private static final int[] YEARS = {
        2010,2037,2143,2075,2018,2034,2162,2061,2094,2083,2099,2064,2031,2338,2058,2085,2096,
        2028,2118,2023,2123,2039,2055,2088,2077,2104,2071,2020,2267,2036,2025,2047,2041,2074,
        2063,2090,2079,2033,2022,2060,2038,2155,2065,2098,2087,2030,2103,2057,2035,2084,2062,
        2179,2089,2027,2021,2054,2043,2070,2059,2024,2097,2040,2287,2051,2078
    };
    private static final String[] LETTERS =
        "А Б В Г Д Е Ж Ѕ З И І К Л М Н О П Р С Т Ꙋ Ф Х Ѿ Ц Ч Ш Щ Ъ Ы Ь Ѣ Ю Ѫ Ѧ".split(" ");
    private static final Map<Integer, List<ReadingRule>> RULE_CACHE = new HashMap<>();

    @TestFactory Stream<DynamicTest> matchesAll65LucanJumpGoldenFiles() {
        return java.util.stream.IntStream.range(0, YEARS.length).mapToObj(index ->
            DynamicTest.dynamicTest((index + 1) + ".tsv — " + YEARS[index], () -> {
                List<String> expected = resourceLines("/regression/" + (index + 1) + ".tsv");
                List<String> actual = generate(YEARS[index]);
                assertEquals(expected.size(), actual.size(), "line count");
                for (int line = 0; line < expected.size(); line++) {
                    assertEquals(expected.get(line), actual.get(line), "line " + (line + 1));
                }
            }));
    }

    private static List<String> generate(int year) throws Exception {
        JDate pascha = Paschalion.getPascha(year);
        JDate nextPascha = Paschalion.getPascha(year + 1);
        JDate date = (JDate) pascha.clone();
        date.addDays(134);
        JDate end = (JDate) nextPascha.clone();
        end.subtractDays(70);

        List<String> lines = new ArrayList<>();
        lines.add(";; This is a kahuna data file for the year " + year);
        lines.add(";; Comments are indicated by the two semicolons");
        lines.add(";; The key of boundaries is " + boundaryLetter(year)
            + " (next year will be " + boundaryLetter(year + 1) + ")");
        lines.add(";; The columns below are:");
        lines.add(";; month\tday\tdoy\tnday\tndayF\tapostol\tgospel");
        lines.add(";; The first two columns are useless for the kahuna; they are only for debugging");
        lines.add(";; Since you have to be a kahuna youself to use these data, there is no documentation");

        while (date.compareTo(end) < 0) {
            int nday = (int) JDate.difference(date, pascha);
            int untilNext = (int) JDate.difference(nextPascha, date);
            OrderedHashtable dayInfo = TestData.englishDayInfo();
            dayInfo.put("doy", Integer.toString(date.getDoy()));
            dayInfo.put("dow", Integer.toString(date.getDayOfWeek()));
            int calendarYear = date.getYear();
            dayInfo.put("nday", Long.toString(JDate.difference(date, Paschalion.getPascha(calendarYear))));
            dayInfo.put("ndayP", Long.toString(JDate.difference(date, Paschalion.getPascha(calendarYear - 1))));
            dayInfo.put("ndayF", Long.toString(JDate.difference(date, Paschalion.getPascha(calendarYear + 1))));

            List<ReadingRule> rules = rules(nday + 1);
            String apostol = effectiveWeek(rules, "apostol", dayInfo);
            String gospel = effectiveWeek(rules, "gospel", dayInfo);
            String row = String.join("\t", Integer.toString(date.getMonth()), Integer.toString(date.getDay()),
                Integer.toString(date.getDoy()), Integer.toString(nday), Integer.toString(untilNext)) + "\t"
                + apostol + (apostol.isEmpty() ? "" : "\t") + gospel;
            lines.add(row);
            date.addDays(1);
        }
        return lines;
    }

    private static String effectiveWeek(List<ReadingRule> rules, String type, OrderedHashtable dayInfo) {
        StringOp evaluator = new StringOp();
        evaluator.dayInfo = dayInfo;
        for (ReadingRule rule : rules) {
            if (type.equals(rule.type) && (rule.command == null || evaluator.evalbool(rule.command))) {
                return rule.effectiveWeek == null ? "" : rule.effectiveWeek;
            }
        }
        return "";
    }

    private static List<ReadingRule> rules(int cycleDay) throws Exception {
        List<ReadingRule> cached = RULE_CACHE.get(cycleDay);
        if (cached != null) return cached;
        Path file = Path.of("Ponomar", "languages", "xml", "lives", "9" + cycleDay + ".xml");
        List<ReadingRule> parsed = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            QDParser.parse(new DocHandler() {
                public void startDocument() {}
                public void endDocument() {}
                public void endElement(String tag) {}
                public void text(String text) {}
                public void startElement(String tag, Hashtable attrs) {
                    if ("SCRIPTURE".equals(tag)) parsed.add(new ReadingRule(
                        string(attrs.get("Type")), string(attrs.get("EffWeek")), string(attrs.get("Cmd"))));
                }
            }, reader);
        }
        RULE_CACHE.put(cycleDay, List.copyOf(parsed));
        return RULE_CACHE.get(cycleDay);
    }

    private static String string(Object value) { return value == null ? null : value.toString(); }

    private static String boundaryLetter(int year) {
        return LETTERS[Paschalion.getKeyOfBoundaries(year) - 1];
    }

    private static List<String> resourceLines(String name) throws IOException {
        try (InputStream stream = LucanJumpGoldenTest.class.getResourceAsStream(name)) {
            assertNotNull(stream, name);
            return new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).lines().toList();
        }
    }

    private record ReadingRule(String type, String effectiveWeek, String command) {}
}

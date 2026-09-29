package Ponomar;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
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
    // Saturday/Sunday overrides around Elevation, Nativity, and Theophany. The
    // production XML models these fixed-feast cycles; the Kahuna TSVs do not.
    private static final Set<String> FIXED_FEAST_OVERRIDES = Set.of(
        "SAE", "SAN", "SAT", "SBE", "SBN", "SBT",
        "SatAE", "SatAN", "SatAT", "SatBE", "SatBT");
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

    @Test void productionSelectionMatchesGoldenAcrossEveryLucanDate() throws Exception {
        int datesVisited = 0;
        int datesCompared = 0;
        int comparisons = 0;
        int discriminatingDates = 0;
        Set<Integer> cycleFiles = new HashSet<>();
        Set<Integer> discriminatingFiles = new HashSet<>();
        Set<String> encounteredOverrides = new HashSet<>();
        List<String> unknownOverrides = new ArrayList<>();
        List<String> mismatches = new ArrayList<>();

        for (int year : YEARS) {
            JDate pascha = Paschalion.getPascha(year);
            JDate nextPascha = Paschalion.getPascha(year + 1);
            JDate date = (JDate) pascha.clone();
            date.addDays(134);
            JDate end = (JDate) nextPascha.clone();
            end.subtractDays(70);

            while (date.compareTo(end) < 0) {
                int cycleDay = (int) JDate.difference(date, pascha) + 1;
                OrderedHashtable dayInfo = DayInfoFixtures.englishDayInfo(date);
                List<ReadingRule> expectedRules = rules(cycleDay);
                boolean discriminating = hasMultiplePossibleWeeks(expectedRules);
                Map<String, String> productionWeeks = productionEffectiveWeeks(
                    new Day("xml/pentecostarion/" + cycleDay, dayInfo));
                boolean skippedForFixedFeast = false;

                for (String type : List.of("apostol", "gospel")) {
                    String expected = effectiveWeek(expectedRules, type, dayInfo);
                    String actual = productionWeeks.getOrDefault(type, "");
                    String context = "year=" + year + ", date=" + date.getMonth() + "/" + date.getDay()
                        + ", cycleDay=" + cycleDay + ", type=" + type;
                    if (!actual.isEmpty() && !actual.matches("\\d+")) {
                        skippedForFixedFeast = true;
                        if (FIXED_FEAST_OVERRIDES.contains(actual)) encounteredOverrides.add(actual);
                        else if (unknownOverrides.size() < 25) unknownOverrides.add(actual + " (" + context + ")");
                        continue;
                    }
                    comparisons++;
                    if (!Objects.equals(expected, actual) && mismatches.size() < 25) {
                        mismatches.add(context + ", expected=" + expected + ", actual=" + actual);
                    }
                }

                datesVisited++;
                cycleFiles.add(cycleDay);
                if (discriminating) discriminatingFiles.add(cycleDay);
                if (!skippedForFixedFeast) {
                    datesCompared++;
                    if (discriminating) discriminatingDates++;
                }
                date.addDays(1);
            }
        }

        assertTrue(datesVisited > 10_000, "the full 65-year range must be exercised");
        assertTrue(datesCompared > 9_000, "too many production dates were skipped");
        assertTrue(comparisons > 18_000, "both reading types must be compared across the range");
        assertTrue(cycleFiles.size() >= 180, "too few distinct cycle files were loaded");
        assertTrue(discriminatingFiles.size() >= 150, "too few files offered multiple possible weeks");
        assertTrue(discriminatingDates > 8_000, "too few comparisons could detect wrong rule selection");
        assertEquals(FIXED_FEAST_OVERRIDES, encounteredOverrides,
            "the fixed-feast override inventory changed");
        assertTrue(unknownOverrides.isEmpty(), () -> "unknown overrides: " + unknownOverrides);
        assertTrue(mismatches.isEmpty(), () -> "production selection mismatches: " + mismatches);
    }

    private static Map<String, String> productionEffectiveWeeks(Day day) {
        Map<String, String> selected = new HashMap<>();
        for (OrderedHashtable wrapper : day.getReadings()) {
            OrderedHashtable information = (OrderedHashtable) wrapper.get("Readings");
            OrderedHashtable readings = (OrderedHashtable) information.get("Readings");
            OrderedHashtable liturgy = (OrderedHashtable) readings.get("LITURGY");
            if (liturgy == null) continue;
            for (String type : List.of("apostol", "gospel")) {
                if (liturgy.get(type) != null) selected.putIfAbsent(type,
                    ((OrderedHashtable) liturgy.get(type)).get("EffWeek").toString());
            }
        }
        return selected;
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
            OrderedHashtable dayInfo = DayInfoFixtures.englishDayInfo();
            dayInfo.put("doy", Integer.toString(date.getDoy()));
            dayInfo.put("dow", Integer.toString(date.getDayOfWeek()));
            int calendarYear = date.getYear();
            dayInfo.put("nday", Long.toString(JDate.difference(date, Paschalion.getPascha(calendarYear))));
            dayInfo.put("ndayP", Long.toString(JDate.difference(date, Paschalion.getPascha(calendarYear - 1))));
            dayInfo.put("ndayF", Long.toString(JDate.difference(date, Paschalion.getPascha(calendarYear + 1))));

            // The legacy fixture's Pascha-relative day zero maps to Pentecostarion life 91.xml.
            int cycleDay = nday + 1;
            List<ReadingRule> rules = rules(cycleDay);
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

    private static boolean hasMultiplePossibleWeeks(List<ReadingRule> rules) {
        for (String type : List.of("apostol", "gospel")) {
            long count = rules.stream().filter(rule -> type.equals(rule.type))
                .map(ReadingRule::effectiveWeek).filter(Objects::nonNull).distinct().count();
            if (count > 1) return true;
        }
        return false;
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
                private final Deque<String> parents = new ArrayDeque<>();
                public void startDocument() {}
                public void endDocument() {}
                public void endElement(String tag) { parents.pop(); }
                public void text(String text) {}
                public void startElement(String tag, Hashtable attrs) {
                    if ("SCRIPTURE".equals(tag) && "LITURGY".equals(parents.peek())) parsed.add(new ReadingRule(
                        string(attrs.get("Type")), string(attrs.get("EffWeek")), string(attrs.get("Cmd"))));
                    parents.push(tag);
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

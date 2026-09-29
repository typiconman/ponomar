package Ponomar;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NonUiCoverageTest {
    @AfterEach void restoreSunriseLanguageState() {
        new Sunrise(DayInfoFixtures.englishDayInfo());
    }

    @Test void formatsJulianAndGregorianDatesInLocalizedText() {
        JDate julian = new JDate(1, 1, 2024);
        OrderedHashtable english = DayInfoFixtures.englishDayInfo(julian);
        assertEquals("Sunday, 1 January A.D. 2024", julian.toString(english));
        assertEquals("Sunday, 14 January A.D. 2024", julian.getGregorianDateS(english));

        Date civil = julian.getGregorianDate();
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.setTime(civil);
        assertAll(() -> assertEquals(2024, utc.get(Calendar.YEAR)),
            () -> assertEquals(Calendar.JANUARY, utc.get(Calendar.MONTH)),
            () -> assertEquals(14, utc.get(Calendar.DAY_OF_MONTH)));

        OrderedHashtable chinese = DayInfoFixtures.englishDayInfo(julian);
        chinese.put("LS", "zh/Hans/"); chinese.put("Ideographic", "1");
        assertAll(() -> assertTrue(julian.toString(chinese).contains("二〇二四")),
            () -> assertTrue(julian.getGregorianDateS(chinese).contains("十四")));
    }

    @Test void convertsHistoricalCalendarDatesAndAnnoMundiBoundaries() {
        OrderedHashtable info = DayInfoFixtures.englishStringDayInfo(new JDate(10, 4, 1582));
        PCalendar beforeReform = new PCalendar(new JDate(10, 4, 1582), PCalendar.gregorian, info);
        assertEquals(beforeReform.getJulianDay(),
            new PCalendar(new JDate(10, 4, 1582), PCalendar.julian, info).getJulianDay());

        PCalendar afterReform = new PCalendar(new JDate(10, 15, 1582), PCalendar.gregorian, info);
        assertEquals(10.0, new PCalendar(new JDate(10, 15, 1582), PCalendar.julian, info).getJulianDay()
            - afterReform.getJulianDay());
        assertEquals(7532, new PCalendar(new JDate(8, 31, 2024), PCalendar.julian, info).getAM());
        assertEquals(7533, new PCalendar(new JDate(9, 1, 2024), PCalendar.julian, info).getAM());
    }

    @Test void exercisesAllAstronomicalMoonPhaseLabelsAndPolarSunBranches() {
        OrderedHashtable info = DayInfoFixtures.englishDayInfo();
        Astronomy astronomy = new Astronomy();
        Set<String> expected = new HashSet<>(Arrays.asList(
            new LanguagePack(info).obtainValues(new LanguagePack(info).Phrases.get("Phases").toString())));
        Set<String> actual = new HashSet<>();
        JDate date = new JDate(1, 1, 2024);
        for (int day = 0; day < 400; day++) {
            actual.add(astronomy.lunarphase(date.getJulianDay() + day, info));
            actual.add(Paschalion.getLunarPhaseString(new JDate(date.getJulianDay() + day), info));
        }
        assertEquals(expected, actual);

        double[] midnightSun = Sunrise.getSunriseSunset(new JDate(6, 21, 2024), 0, 89, 0);
        double[] polarNight = Sunrise.getSunriseSunset(new JDate(12, 21, 2024), 0, 89, 0);
        assertEquals(midnightSun[0], midnightSun[1], 0.001);
        assertEquals(0.0, circularDuration(polarNight), 0.001);
        assertNotEquals(midnightSun[0], polarNight[0]);

        info.put("Ideographic", "1"); info.put("LS", "zh/Hans/");
        new Sunrise(info);
        String[] ideographic = Sunrise.getSunriseSunsetString(new JDate(6, 1, 2024), 116.4, 39.9, 8);
        assertAll(() -> assertFalse(ideographic[0].matches(".*[0-9].*")),
            () -> assertFalse(ideographic[1].matches(".*[0-9].*")));
    }

    @Test void appliesRomanNumberRulesAcrossSubtractiveAndRepeatCases() {
        RuleBasedNumber roman = new RuleBasedNumber(DayInfoFixtures.englishDayInfo());
        Map<Integer, String> romans = Map.of(3, "III", 8, "VIII", 14, "XIV", 49, "XLIX",
            944, "CMXLIV", 3999, "MMMCMXCIX", 4999, "MMMMCMXCIX");
        romans.forEach((number, expected) -> assertEquals(expected, roman.getFormattedNumber(number)));
    }

    @Test void appliesChineseConditionalAndRecursiveNumberRules() {
        OrderedHashtable chineseInfo = DayInfoFixtures.englishDayInfo(); chineseInfo.put("LS", "zh/Hans/");
        RuleBasedNumber chinese = new RuleBasedNumber(chineseInfo);
        assertAll(() -> assertEquals("〇", chinese.getFormattedNumber(0)),
            () -> assertEquals("十", chinese.getFormattedNumber(10)),
            () -> assertEquals("二十", chinese.getFormattedNumber(20)),
            () -> assertEquals("一百〇七", chinese.getFormattedNumber(107)),
            () -> assertEquals("一百一十", chinese.getFormattedNumber(110)));
    }

    @Test void appliesGreekAndChurchSlavonicFinalFormattingRules() {
        OrderedHashtable greekInfo = DayInfoFixtures.englishDayInfo(); greekInfo.put("LS", "el/");
        assertEquals("μβʹ", new RuleBasedNumber(greekInfo).getFormattedNumber(42));
        OrderedHashtable slavonicInfo = DayInfoFixtures.englishDayInfo(); slavonicInfo.put("LS", "cu/");
        String twelve = new RuleBasedNumber(slavonicInfo).getFormattedNumber(12);
        assertAll(() -> assertTrue(twelve.contains("в")), () -> assertTrue(twelve.contains("і")),
            () -> assertTrue(twelve.contains("҃")));
    }

    @Test void convertedIndexEntryPointScansTheFullMenologion(@TempDir Path directory) throws Exception {
        Path output = directory.resolve("index.html");
        CreateIndex.main(new String[] {"2015", output.toString()});
        String html = Files.readString(output, StandardCharsets.UTF_8);
        assertTrue(html.startsWith("\n<HTML>"));
        assertTrue(html.contains("Index of Saints"));
        assertTrue(html.contains("Beginning of the Indiction"));
        assertFalse(html.contains("lives.cgi?id=1428\""), "composite commemorations are expanded, not indexed");
        assertTrue(html.contains("lives.cgi?id=14280\""), "expanded sub-saints are indexed");
        assertTrue(html.endsWith("</HTML>"));
    }

    @Test void typiconRendererHandlesRanksSourcesAndReposeMetadata() {
        List<MenologionReader.Saint> saints = List.of(
            saint("1", "General saint", 4, "", Map.of("ReposeDate", "1900", "ReposePlace", "Kyiv")),
            saint("2", "Greek saint", -2, "el", Map.of("Year", "900")),
            saint("3", "Russian saint", 2, "ru", Map.of("ReposeCentury", "12th century")),
            saint("4", "Roman saint", 9, "la", Map.of("Note", "martyr")));
        String tex = TypiconMenologion.render(saints);
        assertAll(() -> assertTrue(tex.contains("\\textcolor{red}{General saint}")),
            () -> assertTrue(tex.contains("Greek sources")),
            () -> assertTrue(tex.contains("Greek saint (900)")),
            () -> assertTrue(tex.contains("Russian sources")),
            () -> assertTrue(tex.contains("†12th century")),
            () -> assertTrue(tex.contains("\\footnote{Martyr}")));
    }

    private static MenologionReader.Saint saint(String id, String name, int rank, String source,
            Map<String, String> info) {
        return new MenologionReader.Saint(id, id, source, Map.of("Nominative", name), info, rank);
    }

    private static double circularDuration(double[] riseSet) {
        double duration = riseSet[1] - riseSet[0];
        return duration < 0 ? duration + 24 : duration;
    }
}

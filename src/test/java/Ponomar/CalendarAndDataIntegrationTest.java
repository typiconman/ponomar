package Ponomar;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Test;

class CalendarAndDataIntegrationTest {
    @Test void convertsBetweenJulianAndGregorianCalendars() {
        OrderedHashtable info = DayInfoFixtures.englishDayInfo();
        PCalendar julian = new PCalendar(new JDate(1, 14, 2012), PCalendar.julian, info);
        PCalendar gregorian = new PCalendar(new JDate(1, 14, 2012), PCalendar.gregorian, info);
        assertEquals(13.0, julian.getJulianDay() - gregorian.getJulianDay());
        assertEquals(2012, julian.getYearJ());
        assertEquals(1, julian.getMonthJ());
        assertEquals(14, julian.getDayJ());
        assertEquals(2012, julian.getYearG());
        assertEquals(1, julian.getMonthG());
        assertEquals(27, julian.getDayG());
        assertEquals(7520, julian.getAM());
        PCalendar clone = (PCalendar) julian.clone();
        assertEquals(julian.getJulianDay(), clone.getJulianDay());
    }

    @Test void computesSunriseSunsetAndLunarPhaseStrings() {
        JDate date = new JDate(6, 1, 2024);
        OrderedHashtable info = DayInfoFixtures.englishDayInfo(date);
        new Sunrise(info);
        double[] standard = Sunrise.getSunriseSunset(date, -74.0, 40.7, -5);
        double[] daylight = Sunrise.getSunriseSunset(date, -74.0, 40.7, -5, true);
        double[] civil = Sunrise.getSunriseSunset(date, -74.0, 40.7, -5, false, Sunrise.CIVIL);
        assertAll(() -> assertEquals(2, standard.length), () -> assertEquals(2, daylight.length),
            () -> assertEquals(2, civil.length), () -> assertEquals(1.0, daylight[0] - standard[0], 0.001));
        String[] formatted = Sunrise.getSunriseSunsetString(date, "-74", "40.7", "-5");
        assertEquals(2, formatted.length);
        assertTrue(formatted[0].matches(".*\\d.*"));
        String phase = new Astronomy().lunarphase(date.getJulianDay(), info);
        assertTrue(Arrays.asList(new LanguagePack(info).obtainValues(
            new LanguagePack(info).Phrases.get("Phases").toString())).contains(phase));
        assertEquals("Error", new Astronomy().lunarphaseJulian(date));
    }

    @Test void loadsACompleteMenaionDayAndItsCommemorations() {
        JDate date = new JDate(9, 1, 2015);
        OrderedHashtable info = DayInfoFixtures.englishDayInfo(date);
        Day day = new Day("xml/09/01", info);
        assertTrue(day.getDayRank() >= 0);
        assertEquals(-1, day.getTone());
        assertTrue(day.getCommsHyper().contains("Indiction"));
        assertNotNull(day.getIcon().get("Images"));
        assertNotNull(day.getIcon().get("Names"));
        assertNotNull(day.getReadings());
    }

    @Test void loadsCommemorationMetadataServicesLifeAndIcons() {
        OrderedHashtable info = DayInfoFixtures.englishDayInfo(new JDate(9, 1, 2015));
        Commemoration1 commemoration = new Commemoration1("09426", "09426", info);
        assertEquals("09426", commemoration.getSId());
        assertEquals("09426", commemoration.getCId());
        assertTrue(commemoration.getName().contains("Indiction"));
        assertEquals(4, commemoration.getRank());
        assertFalse(commemoration.checkLife());
        assertFalse(commemoration.checkPropers());
        assertNotNull(commemoration.getReadings());
        assertNotNull(commemoration.getServiceNode("/LITURGY/SCRIPTURE"));
        assertNotNull(commemoration.getService("/LITURGY/SCRIPTURE", "apostol"));
        assertNull(commemoration.getRH("missing", "missing"));
        assertNotNull(commemoration.getDisplayIcons());

        Commemoration1 life = new Commemoration1("85", "85", info);
        assertTrue(life.checkLife() || !life.getName().isEmpty());
    }

    @Test void formatsEveryPredefinedAndGenericFastingRule() {
        Fasting fasting = new Fasting(DayInfoFixtures.englishDayInfo(new JDate(3, 4, 2024)));
        for (String rule : List.of("0000000", "0000001", "0000011", "0000111", "0001111",
                "0011111", "0111111", "1111111", "0000010", "1010101")) {
            assertNotNull(fasting.convert(rule));
            assertFalse(fasting.convert(rule).isEmpty());
        }
        assertNotNull(fasting.FastRules());
    }

    @Test void configurationAndHandlerCallbacksPopulateState() {
        OrderedHashtable originalDefaults = ConfigurationFiles.Defaults;
        try {
            ConfigurationFiles.Defaults = new OrderedHashtable();
            ConfigurationFiles.ReadFile();
            assertFalse(ConfigurationFiles.Defaults.isEmpty());
            ConfigurationFiles config = new ConfigurationFiles();
            Hashtable attrs = new Hashtable(); attrs.put("Example", "value");
            config.startDocument(); config.startElement("DEFAULT", attrs); config.endElement("DEFAULT"); config.text(""); config.endDocument();
            assertEquals("value", ConfigurationFiles.Defaults.get("Example"));
        } finally {
            ConfigurationFiles.Defaults = originalDefaults;
        }

        LanguagePack pack = new LanguagePack(DayInfoFixtures.englishDayInfo());
        Hashtable phrase = new Hashtable(); phrase.put("Key", "Synthetic"); phrase.put("Value", "yes");
        pack.startDocument(); pack.startElement("PHRASE", phrase); pack.endElement("LANGUAGE"); pack.text(""); pack.endDocument();
        assertEquals("yes", pack.Phrases.get("Synthetic"));
    }

    @Test void serviceRuleHandlersSelectMatchingRules() {
        OrderedHashtable info = DayInfoFixtures.englishDayInfo(new JDate(4, 22, 2024));
        ServiceInfo service = new ServiceInfo("LITURGY", info);
        service.ServiceRules();
        service.startDocument();
        Hashtable period = new Hashtable(); service.startElement("PERIOD", period);
        Hashtable rule = new Hashtable(); rule.put("Order", "normal"); rule.put("Cmd", "dow == 0");
        service.startElement("LITURGY", rule); service.endElement("PERIOD"); service.endDocument(); service.text("");

        Fasting fasting = new Fasting(info);
        fasting.startDocument(); fasting.startElement("PERIOD", new Hashtable());
        Hashtable fastRule = new Hashtable(); fastRule.put("Case", "1111111");
        fasting.startElement("RULE", fastRule); fasting.endElement("PERIOD"); fasting.endDocument(); fasting.text("");
        assertNotNull(fasting.convert("1111111"));
    }

    @Test void serviceRuleLoaderShouldReturnRules() {
        ServiceInfo service = new ServiceInfo("LITURGY", DayInfoFixtures.englishDayInfo(new JDate(4, 22, 2024)));
        assertNotNull(service.ServiceRules());
    }
}

package Ponomar;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Hashtable;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PaschalionTest {
    static Stream<Arguments> knownPaschas() {
        return Stream.of(
            Arguments.of(2023, 4, 3), Arguments.of(2024, 4, 22),
            Arguments.of(2025, 4, 7), Arguments.of(2026, 3, 30));
    }

    @ParameterizedTest
    @MethodSource("knownPaschas")
    void returnsKnownOrthodoxPaschaDates(int year, int month, int day) {
        assertDate(month, day, year, Paschalion.getPascha(year));
    }

    @Test void paschaIsSunday() {
        assertEquals(0, Paschalion.getPascha(2024).getDayOfWeek());
    }

    @Test void derivesMovableObservances() {
        JDate pascha = Paschalion.getPascha(2024);
        assertEquals(48L, JDate.difference(pascha, Paschalion.getLentStart(2024)));
        assertEquals(49L, JDate.difference(Paschalion.getPentecost(2024), pascha));
        assertEquals(57L, JDate.difference(Paschalion.getApostlesFastStart(2024), pascha));
        assertEquals(28, Paschalion.getSolarCycle(2024));
        assertEquals(2, Paschalion.getIndiction(2024));
        assertEquals(Paschalion.getKeyOfBoundaries(2024), 32);
        assertEquals(8, Paschalion.getLunarCycle(2024));
    }

    @Test void calculatesApostlesFastLength() {
        assertEquals(JDate.difference(new JDate(6, 29, 2024),
            Paschalion.getApostlesFastStart(2024)), Paschalion.getApostlesFastLength(2024));
    }

    @Test void classifiesRepresentativeFastingDays() {
        int[] fasts = Paschalion.getFasts(2024);
        assertEquals(366, fasts.length);
        assertEquals(1, fasts[indexOf(1, 5, 2024)]);
        assertEquals(0, fasts[indexOf(1, 6, 2024)]);
        assertEquals(2, fasts[indexOf(3, 4, 2024)]);
        assertEquals(1, fasts[indexOf(3, 5, 2024)]);
        assertEquals(0, fasts[indexOf(4, 23, 2024)]);
        assertEquals(1, fasts[indexOf(8, 1, 2024)]);
    }

    @Test void calculatesLunarHelpers() {
        JDate date = new JDate(4, 22, 2024);
        double phase = Paschalion.getLunarPhase(date);
        assertTrue(phase >= 0.0 && phase < 1.0);
        assertTrue(Paschalion.getNextNewMoon(date).compareTo(date) >= 0);
        assertTrue(Paschalion.getNextFullMoon(date).compareTo(date) >= 0);
    }

    @Test void buildsFeastTable() {
        OrderedHashtable info = DayInfoFixtures.englishDayInfo();
        Hashtable feasts = Paschalion.getFeasts(2024, info);
        assertFalse(feasts.isEmpty());
        assertTrue(feasts.containsKey(Paschalion.getPascha(2024).getJulianDay()));
    }

    @Test void combinesAnnunciationAndPaschaForKyriopascha() {
        OrderedHashtable info = DayInfoFixtures.englishDayInfo();
        Hashtable feasts = Paschalion.getFeasts(1912, info);
        JDate kyriopascha = new JDate(3, 25, 1912);
        assertEquals(kyriopascha.getJulianDay(), Paschalion.getPascha(1912).getJulianDay());
        assertEquals("Kyriopascha: Pascha and Annunciation", feasts.get(kyriopascha.getJulianDay()));
    }

    @Test void transfersMeetingOfTheLordBeforeLentWhenNecessary() {
        OrderedHashtable info = DayInfoFixtures.englishDayInfo();
        Hashtable feasts = Paschalion.getFeasts(2010, info);
        assertEquals(48L, JDate.difference(Paschalion.getPascha(2010), new JDate(2, 2, 2010)));
        assertEquals("Meeting of the Lord", feasts.get(new JDate(2, 1, 2010).getJulianDay()));
        assertFalse(feasts.containsKey(new JDate(2, 2, 2010).getJulianDay()));
    }

    @Test void classifiesEveryAnnualFastingSeason() {
        int year = 2024;
        int[] fasts = Paschalion.getFasts(year);
        JDate pascha = Paschalion.getPascha(year);
        assertAll(
            () -> assertEquals(0, fasts[indexOf(12, 26, year)], "Sviatki"),
            () -> assertEquals(0, fasts[indexFromPascha(pascha, -66, year)], "Publican and Pharisee"),
            () -> assertEquals(2, fasts[indexFromPascha(pascha, -52, year)], "Cheesefare"),
            () -> assertEquals(1, fasts[indexFromPascha(pascha, -20, year)], "Lent"),
            () -> assertEquals(0, fasts[indexFromPascha(pascha, 3, year)], "Bright Week"),
            () -> assertEquals(0, fasts[indexFromPascha(pascha, 52, year)], "Pentecost week"),
            () -> assertEquals(1, fasts[indexFromPascha(pascha, 60, year)], "Apostles' Fast"),
            () -> assertEquals(1, fasts[indexOf(8, 7, year)], "Dormition Fast"),
            () -> assertEquals(1, fasts[indexOf(12, 18, year)], "Advent"),
            () -> assertEquals(1, fasts[indexOf(7, 4, year)], "ordinary Wednesday"),
            () -> assertEquals(0, fasts[indexOf(7, 5, year)], "ordinary Thursday"));
    }

    @Test void rejectsYearsBeforeAd33() {
        IllegalArgumentException failure = assertThrows(
            IllegalArgumentException.class, () -> Paschalion.getPascha(32));
        assertEquals("Invalid year", failure.getMessage());
        assertAll(
            () -> assertThrows(IllegalArgumentException.class, () -> Paschalion.getPentecost(32)),
            () -> assertThrows(IllegalArgumentException.class, () -> Paschalion.getLentStart(32)),
            () -> assertThrows(IllegalArgumentException.class, () -> Paschalion.getApostlesFastStart(32)),
            () -> assertThrows(IllegalArgumentException.class, () -> Paschalion.getFasts(32)));
    }

    private static void assertDate(int month, int day, int year, JDate actual) {
        assertAll(() -> assertEquals(month, actual.getMonth()),
            () -> assertEquals(day, actual.getDay()), () -> assertEquals(year, actual.getYear()));
    }

    private static int indexOf(int month, int day, int year) {
        return (int) JDate.difference(new JDate(month, day, year), new JDate(1, 1, year));
    }

    private static int indexFromPascha(JDate pascha, int offset, int year) {
        JDate date = (JDate) pascha.clone();
        date.addDays(offset);
        return (int) JDate.difference(date, new JDate(1, 1, year));
    }
}

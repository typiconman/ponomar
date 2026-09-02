package Ponomar;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JDateTest {
    @BeforeAll static void initializeLocalizedErrors() {
        new JDate(1, 1, 2024).getGregorianDateS(TestData.englishDayInfo());
    }

    static Stream<Arguments> roundTripDates() {
        return Stream.of(
            Arguments.of(1, 1, 1900), Arguments.of(2, 29, 1900),
            Arguments.of(3, 1, 2000), Arguments.of(12, 31, 2023),
            Arguments.of(4, 22, 2024));
    }

    @ParameterizedTest
    @MethodSource("roundTripDates")
    void roundTripsJulianCalendarDates(int month, int day, int year) {
        assertDate(month, day, year, new JDate(month, day, year));
    }

    @Test void exposesJulianDay() {
        assertEquals(2440601L, new JDate(1, 1, 1970).getJulianDay());
        assertDate(1, 1, 1970, new JDate(2440601L));
    }

    @Test void handlesLeapDayArithmetic() {
        JDate date = new JDate(2, 28, 2024);
        date.addDays(1);
        assertDate(2, 29, 2024, date);
        date.addDays(1);
        assertDate(3, 1, 2024, date);
        date.subtractDays(2);
        assertDate(2, 28, 2024, date);
    }

    @Test void crossesMonthAndYearBoundaries() {
        JDate date = new JDate(12, 31, 2023);
        date.addDays(1);
        assertDate(1, 1, 2024, date);
        date.subtractDays(1);
        assertDate(12, 31, 2023, date);
        date.addMonths();
        assertDate(1, 31, 2024, date);
        date.subtactMonths();
        assertDate(12, 31, 2023, date);
    }

    @Test void calculatesDayOfWeek() {
        assertEquals(0, new JDate(1, 1, 2024).getDayOfWeek());
        assertEquals(3, new JDate(1, 1, 1970).getDayOfWeek());
    }

    @Test void calculatesCommonYearDayOfYear() {
        assertEquals(0, new JDate(1, 1, 2023).getDoy());
        assertEquals(58, new JDate(2, 28, 2023).getDoy());
        assertEquals(59, new JDate(3, 1, 2023).getDoy());
        assertEquals(364, new JDate(12, 31, 2023).getDoy());
    }

    @Test void calculatesLeapYearDayOfYear() {
        assertEquals(0, new JDate(1, 1, 2024).getDoy());
        assertEquals(58, new JDate(2, 28, 2024).getDoy());
        assertEquals(59, new JDate(2, 29, 2024).getDoy());
        assertEquals(60, new JDate(3, 1, 2024).getDoy());
        assertEquals(365, new JDate(12, 31, 2024).getDoy());
    }

    @Test void rejectsDayZero() {
        assertThrows(IllegalArgumentException.class, () -> new JDate(1, 0, 2024));
    }

    @Test void rejectsOtherInvalidInputs() {
        assertAll(
            () -> assertThrows(IllegalArgumentException.class, () -> new JDate(0, 1, 2024)),
            () -> assertThrows(IllegalArgumentException.class, () -> new JDate(13, 1, 2024)),
            () -> assertThrows(IllegalArgumentException.class, () -> new JDate(2, 30, 2024)),
            () -> assertThrows(IllegalArgumentException.class, () -> new JDate(1, -1, 2024)),
            () -> assertThrows(IllegalArgumentException.class, () -> new JDate(-1L)),
            () -> assertThrows(IllegalArgumentException.class, () -> JDate.getMaxDaysInMonth(0, 2024)));
    }

    @Test void reportsMaximumDaysForCommonAndLeapYears() {
        assertEquals(28, JDate.getMaxDaysInMonth(2, 2023));
        assertEquals(29, JDate.getMaxDaysInMonth(2, 2024));
        assertEquals(30, JDate.getMaxDaysInMonth(4, 2024));
        assertEquals(31, JDate.getMaxDaysInMonth(12, 2024));
    }

    @Test void comparesClonesAndCalculatesDifferences() {
        JDate earlier = new JDate(12, 31, 2023);
        JDate later = new JDate(1, 10, 2024);
        assertTrue(earlier.compareTo(later) < 0);
        assertTrue(later.compareTo(earlier) > 0);
        assertEquals(0, later.compareTo(later));
        assertEquals(10L, JDate.difference(later, earlier));
        assertTrue(later.equals((JDate) later.clone()));

        JDate clone = (JDate) later.clone();
        clone.addDays(1);
        assertDate(1, 10, 2024, later);
        assertDate(1, 11, 2024, clone);
    }

    private static void assertDate(int month, int day, int year, JDate actual) {
        assertAll(
            () -> assertEquals(month, actual.getMonth()),
            () -> assertEquals(day, actual.getDay()),
            () -> assertEquals(year, actual.getYear()));
    }
}

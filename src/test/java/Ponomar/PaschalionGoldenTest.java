package Ponomar;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

class PaschalionGoldenTest {
    private static final int START = 1941;
    private static final String[] LETTERS =
        "А Б В Г Д Е Ж Ѕ З И І К Л М Н О П Р С Т Ꙋ Ф Х Ѿ Ц Ч Ш Щ Ъ Ы Ь Ѣ Ю Ѫ Ѧ".split(" ");
    private static final String[] MONTHS =
        "January February March April May June July August September October November December".split(" ");

    @TestFactory Stream<DynamicTest> matchesAll532GoldenRows() throws IOException {
        List<String> expected = resourceLines("/regression/paschalion_baseline.tsv");
        assertEquals("Paschalion for 532 years beginning with 1941", expected.get(0));
        assertEquals(533, expected.size());
        return java.util.stream.IntStream.range(0, 532).mapToObj(offset -> {
            int year = START + offset;
            return DynamicTest.dynamicTest("Paschalion golden year " + year,
                () -> assertEquals(expected.get(offset + 1), row(year)));
        });
    }

    @Test void perlComputusHelpersRemainInternallyConsistent() {
        for (int year = START; year < START + 532; year++) {
            int foundation = foundation(year);
            int epacta = epacta(year);
            assertTrue(epacta + foundation == 21 || epacta + foundation == 51, "year " + year);
            assertEquals(Paschalion.getPascha(year).getJulianDay(),
                computedPascha(year, concurrent(year), foundation).getJulianDay());
        }
    }

    private static String row(int year) {
        JDate pascha = Paschalion.getPascha(year);
        int key = Paschalion.getKeyOfBoundaries(year);
        return String.join("\t", Integer.toString(year), Integer.toString(year + 5508),
            Integer.toString(Paschalion.getIndiction(year)), Integer.toString(Paschalion.getSolarCycle(year)),
            Integer.toString(concurrent(year)), Integer.toString(Paschalion.getLunarCycle(year)),
            Integer.toString(foundation(year)), Integer.toString(epacta(year)),
            MONTHS[pascha.getMonth() - 1] + " " + pascha.getDay(), LETTERS[key - 1]);
    }

    private static int concurrent(int year) {
        int cycle = (year + 20) % 28;
        int value = cycle + cycle / 4;
        while (value > 7) value -= 7;
        return value == 0 ? 7 : value;
    }

    private static int foundation(int year) {
        int value = ((year + 1) % 19) * 11;
        while (value > 30) value -= 30;
        return value == 0 ? 29 : value;
    }

    private static int epacta(int year) { return (51 - foundation(year)) % 30; }

    private static JDate computedPascha(int year, int concurrent, int foundation) {
        JDate firstSunday = new JDate(3, 1, year);
        firstSunday.addDays((10 - concurrent) % 7);
        JDate boundary = new JDate(3, 1, year);
        boundary.addDays(47 - foundation);
        if (JDate.difference(boundary, new JDate(3, 21, year)) < 0) boundary.addDays(30);
        while (JDate.difference(firstSunday, boundary) < 0) firstSunday.addDays(7);
        return firstSunday;
    }

    private static List<String> resourceLines(String name) throws IOException {
        try (InputStream stream = PaschalionGoldenTest.class.getResourceAsStream(name)) {
            assertNotNull(stream, name);
            return new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).lines().toList();
        }
    }
}

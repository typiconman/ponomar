package Ponomar;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/** Java replacement for the former kahuna.pl Lucan-jump table generator. */
public final class Kahuna {
    private Kahuna() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException(
                "Usage: Kahuna <input-data-directory> <output-lives-directory>");
        }
        update(Path.of(args[0]), Path.of(args[1]));
    }

    public static void update(Path input, Path output) throws IOException {
        Map<Integer, List<Cell>> matrix = readMatrix(input);
        Map<String, Map<Integer, Lection>> lectionary = readLectionary(input.resolve("lectionary.tsv"));
        for (Map.Entry<Integer, List<Cell>> entry : matrix.entrySet()) {
            int cycleDay = entry.getKey();
            Path xml = output.resolve("9" + cycleDay + ".xml");
            List<String> replacement = scripture(entry.getValue(), (cycleDay - 1) % 7, lectionary);
            replaceLiturgy(xml, replacement);
        }
    }

    static String stringify(Collection<Integer> input) {
        List<Integer> values = input.stream().distinct().sorted().toList();
        if (values.isEmpty()) return "";
        List<String> parts = new ArrayList<>();
        int start = values.get(0), stop = start;
        for (int index = 1; index <= values.size(); index++) {
            if (index < values.size() && values.get(index) == stop + 1) {
                stop = values.get(index);
                continue;
            }
            parts.add(start == stop ? "doy == " + start : "(doy >= " + start + " && doy <= " + stop + ")");
            if (index < values.size()) start = stop = values.get(index);
        }
        return String.join(" || ", parts);
    }

    static String xmlEscape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static Map<Integer, List<Cell>> readMatrix(Path input) throws IOException {
        Map<Integer, List<Cell>> result = new TreeMap<>();
        for (int file = 1; file <= 65; file++) {
            for (String line : Files.readAllLines(input.resolve(file + ".tsv"), StandardCharsets.UTF_8)) {
                if (line.startsWith(";;")) continue;
                String[] values = line.split("\t", -1);
                if (values.length < 7 || values[5].isEmpty() || values[6].isEmpty()) continue;
                int cycleDay = Integer.parseInt(values[3]) + 1;
                Cell cell = new Cell(Integer.parseInt(values[2]), Integer.parseInt(values[4]), values[5], values[6]);
                List<Cell> cells = result.computeIfAbsent(cycleDay, ignored -> new ArrayList<>());
                // The Perl hash used (cycle day, day of year, distance to next Pascha) as its key.
                cells.removeIf(existing -> existing.doy == cell.doy && existing.ndayF == cell.ndayF);
                cells.add(cell);
            }
        }
        return result;
    }

    private static Map<String, Map<Integer, Lection>> readLectionary(Path file) throws IOException {
        Map<String, Map<Integer, Lection>> result = new TreeMap<>();
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (line.startsWith(";;")) continue;
            String[] values = line.split("\t", -1);
            if (values.length < 6 || values[2].isEmpty() || values[4].isEmpty()) continue;
            result.computeIfAbsent(values[0], ignored -> new TreeMap<>()).put(Integer.parseInt(values[1]),
                new Lection(values[2], values[3], values[4], values[5]));
        }
        return result;
    }

    private static List<String> scripture(List<Cell> cells, int dow,
            Map<String, Map<Integer, Lection>> lectionary) {
        List<String> result = new ArrayList<>();
        appendType(result, cells, dow, lectionary, true);
        appendType(result, cells, dow, lectionary, false);
        return result;
    }

    private static void appendType(List<String> output, List<Cell> cells, int dow,
            Map<String, Map<Integer, Lection>> lectionary, boolean apostol) {
        Map<String, List<Cell>> byWeek = cells.stream().collect(Collectors.groupingBy(
            cell -> apostol ? cell.apostol : cell.gospel, TreeMap::new, Collectors.toList()));
        for (Map.Entry<String, List<Cell>> weekEntry : byWeek.entrySet()) {
            String week = weekEntry.getKey();
            Lection lection = Optional.ofNullable(lectionary.get(week)).map(map -> map.get(dow))
                .orElseThrow(() -> new IllegalArgumentException("No lectionary row for week " + week + ", day " + dow));
            Map<Integer, List<Cell>> byFutureDistance = weekEntry.getValue().stream().collect(
                Collectors.groupingBy(Cell::ndayF, TreeMap::new, Collectors.toList()));
            for (Map.Entry<Integer, List<Cell>> distance : byFutureDistance.entrySet()) {
                addRule(output, week, lection, apostol, distance.getKey(), distance.getValue(), true);
                addRule(output, week, lection, apostol, distance.getKey(), distance.getValue(), false);
            }
        }
    }

    private static void addRule(List<String> output, String week, Lection lection, boolean apostol,
            int ndayF, List<Cell> cells, boolean earlyYear) {
        List<Integer> days = cells.stream().map(Cell::doy)
            .filter(day -> earlyYear ? day < 100 : day > 100).toList();
        if (days.isEmpty()) return;
        String variable = earlyYear ? "nday" : "ndayF";
        String command = xmlEscape(variable + " == " + -ndayF + " && (" + stringify(days) + ")");
        String type = apostol ? "apostol" : "gospel";
        String reading = apostol ? lection.epistleReading : lection.gospelReading;
        String pericope = apostol ? lection.epistlePericope : lection.gospelPericope;
        output.add("<SCRIPTURE Type=\"" + type + "\" Reading=\"" + reading + "\" Pericope=\""
            + pericope + "\" EffWeek=\"" + week + "\" Cmd=\"" + command + "\"/>");
    }

    private static void replaceLiturgy(Path file, List<String> scripture) throws IOException {
        List<String> former = Files.readAllLines(file, StandardCharsets.UTF_8);
        List<String> updated = new ArrayList<>();
        boolean inside = false;
        for (String line : former) {
            if (line.contains("<LITURGY>")) {
                inside = true;
                updated.add("<LITURGY>");
                updated.addAll(scripture);
            } else if (line.contains("</LITURGY>")) {
                inside = false;
                updated.add("</LITURGY>");
            } else if (!inside) {
                updated.add(line);
            }
        }
        Path temporary = Files.createTempFile(file.getParent(), file.getFileName().toString(), ".tmp");
        Files.write(temporary, updated, StandardCharsets.UTF_8);
        try {
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException unsupported) {
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private record Cell(int doy, int ndayF, String apostol, String gospel) {}
    private record Lection(String epistleReading, String epistlePericope,
                           String gospelReading, String gospelPericope) {}
}

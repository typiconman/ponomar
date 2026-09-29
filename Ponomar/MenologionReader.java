package Ponomar;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Shared Java data reader used by the former Perl menologion utilities. */
final class MenologionReader {
    private MenologionReader() {}

    static List<Saint> saints(JDate date) throws Exception {
        OrderedHashtable dayInfo = dayInfo(date);
        StringOp evaluator = new StringOp();
        evaluator.dayInfo = dayInfo;
        Path dayFile = Path.of("Ponomar", "languages", "en", "xml",
            String.format(Locale.ROOT, "%02d", date.getMonth()),
            String.format(Locale.ROOT, "%02d.xml", date.getDay()));
        List<DayEntry> entries = new ArrayList<>();
        parse(dayFile, (tag, attrs) -> {
            if (!"SAINT".equals(tag) || attrs.get("SId") == null || attrs.get("CId") == null) return;
            Object command = attrs.get("Cmd");
            if (command == null || evaluator.evalbool(command.toString())) entries.add(new DayEntry(
                attrs.get("SId").toString().trim(), attrs.get("CId").toString().trim(),
                attrs.get("Src") == null ? "" : attrs.get("Src").toString()));
        });
        List<Saint> result = new ArrayList<>();
        for (DayEntry entry : entries) result.add(readLife(entry.sIds, entry.cId, entry.source));
        return result;
    }

    static Saint readLife(String sIds, String cId, String source) throws Exception {
        Map<String, String> name = new TreeMap<>();
        Map<String, String> info = new TreeMap<>();
        int[] rank = {0};
        for (Path file : List.of(
                Path.of("Ponomar", "languages", "xml", "lives", cId + ".xml"),
                Path.of("Ponomar", "languages", "en", "xml", "lives", cId + ".xml"))) {
            if (!Files.isRegularFile(file)) continue;
            parse(file, (tag, attrs) -> {
                if ("NAME".equals(tag)) copy(attrs, name);
                if ("INFO".equals(tag)) copy(attrs, info);
                if ("SERVICE".equals(tag) && attrs.get("Type") != null) {
                    rank[0] = Integer.parseInt(attrs.get("Type").toString());
                }
            });
        }
        if (rank[0] < 2 && cId.length() == 4 && numeric(cId) >= 9000 && numeric(cId) < 9900) rank[0] = -2;
        return new Saint(sIds, cId, source, Map.copyOf(name), Map.copyOf(info), rank[0]);
    }

    static OrderedHashtable dayInfo(JDate date) {
        OrderedHashtable info = new OrderedHashtable();
        info.put("LS", "en/"); info.put("GS", "0");
        info.put("dow", date.getDayOfWeek()); info.put("doy", date.getDoy());
        int year = date.getYear();
        info.put("nday", JDate.difference(date, Paschalion.getPascha(year)));
        info.put("ndayP", JDate.difference(date, Paschalion.getPascha(year - 1)));
        info.put("ndayF", JDate.difference(date, Paschalion.getPascha(year + 1)));
        return info;
    }

    private static void copy(Hashtable attrs, Map<String, String> target) {
        for (Object key : attrs.keySet()) target.put(key.toString(), attrs.get(key).toString());
    }

    private static int numeric(String value) {
        try { return Integer.parseInt(value); } catch (NumberFormatException ignored) { return -1; }
    }

    private static void parse(Path file, StartElement consumer) throws Exception {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            QDParser.parse(new DocHandler() {
                public void startDocument() {}
                public void endDocument() {}
                public void endElement(String tag) {}
                public void text(String text) {}
                public void startElement(String tag, Hashtable attrs) { consumer.accept(tag, attrs); }
            }, reader);
        }
    }

    interface StartElement { void accept(String tag, Hashtable attrs); }
    private record DayEntry(String sIds, String cId, String source) {}
    record Saint(String sIds, String cId, String source, Map<String, String> name,
                 Map<String, String> info, int rank) {}
}

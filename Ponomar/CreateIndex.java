package Ponomar;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Java replacement for create_index.pl. */
public final class CreateIndex {
    private static final String[] MONTHS =
        "January February March April May June July August September October November December".split(" ");
    private CreateIndex() {}

    public static void main(String[] args) throws Exception {
        int year = args.length > 0 ? Integer.parseInt(args[0]) : new JDate().getYear();
        Path output = args.length > 1 ? Path.of(args[1]) : Path.of("Ponomar/regtests/index.html");
        Map<String, Entry> entries = new LinkedHashMap<>();
        JDate date = new JDate(9, 1, year);
        for (int offset = 0; offset < 365; offset++, date.addDays(1)) {
            for (MenologionReader.Saint saint : MenologionReader.saints(date)) {
                if (!saint.sIds().equals(saint.cId())) {
                    if (saint.sIds().matches("\\d+") && Integer.parseInt(saint.sIds()) < 10) continue;
                    for (String id : saint.sIds().split(","))
                        add(entries, MenologionReader.readLife(id.trim(), id.trim(), saint.source()), date);
                } else {
                    add(entries, saint, date);
                }
            }
        }
        String html = render(entries, year);
        if (output.getParent() != null) Files.createDirectories(output.getParent());
        Files.writeString(output, html, StandardCharsets.UTF_8);
    }

    private static void add(Map<String, Entry> entries, MenologionReader.Saint saint, JDate date) {
        if (!saint.name().containsKey("Nominative")) return;
        Entry entry = entries.computeIfAbsent(saint.cId(), ignored -> new Entry(
            saint.name().getOrDefault("Short", saint.name().get("Nominative")),
            saint.name().get("Nominative"), new ArrayList<>()));
        entry.dates.add((JDate) date.clone());
    }

    static String render(Map<String, Entry> entries, int year) {
        StringBuilder html = new StringBuilder("\n<HTML>\n<HEAD>\n<TITLE>Index of Saints</TITLE>\n"
            + "<META Http-equiv=\"content-type\" Content=\"text/html; charset=utf-8\">\n</HEAD>\n<BODY>\n<TABLE>\n");
        Map<String, List<Map.Entry<String, Entry>>> byName = new TreeMap<>();
        entries.entrySet().forEach(entry -> byName.computeIfAbsent(entry.getValue().shortName,
            ignored -> new ArrayList<>()).add(entry));
        byName.forEach((name, saints) -> {
            html.append("<TR><TD ColSpan=\"2\">").append(name).append("</TD></TR>\n");
            for (Map.Entry<String, Entry> saint : saints) {
                html.append("<TR><TD><A Href=\"http://www.ponomar.net/cgi-bin/lives.cgi?id=")
                    .append(saint.getKey()).append("\">").append(saint.getValue().fullName)
                    .append("</A></TD><TD>");
                for (JDate date : saint.getValue().dates) html.append("<A Href=\"http://www.ponomar.net/cgi-bin/menologion.cgi?month=")
                    .append(date.getMonth()).append("&day=").append(date.getDay()).append("&year=").append(year)
                    .append("\">").append(MONTHS[date.getMonth() - 1]).append(' ').append(date.getDay()).append("</A> ");
                // Deliberately close each saint row here; the Perl generator closed once per
                // name group and produced malformed HTML when multiple saints shared a name.
                html.append("</TD></TR>\n");
            }
        });
        return html.append("</TABLE>\n</BODY>\n</HTML>").toString();
    }

    static final class Entry {
        final String shortName, fullName; final List<JDate> dates;
        Entry(String shortName, String fullName, List<JDate> dates) {
            this.shortName = shortName; this.fullName = fullName; this.dates = dates;
        }
    }
}

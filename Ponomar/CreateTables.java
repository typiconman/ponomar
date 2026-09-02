package Ponomar;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Java replacement for create_tables.pl. */
public final class CreateTables {
    private CreateTables() {}

    public static void main(String[] args) throws Exception {
        Path output = args.length > 0 ? Path.of(args[0]) : Path.of("Ponomar/regtests/saintdata.tsv");
        int days = args.length > 1 ? Integer.parseInt(args[1]) : 1;
        List<String> lines = new ArrayList<>(List.of("ID\tTag\tKey\tValue"));
        JDate date = new JDate(9, 1, 2015);
        for (int day = 0; day < days; day++, date.addDays(1)) {
            for (MenologionReader.Saint saint : expanded(MenologionReader.saints(date))) {
                append(lines, saint.cId(), "NAME", saint.name());
                append(lines, saint.cId(), "INFO", saint.info());
            }
        }
        if (output.getParent() != null) Files.createDirectories(output.getParent());
        Files.write(output, lines, StandardCharsets.UTF_8);
    }

    private static List<MenologionReader.Saint> expanded(List<MenologionReader.Saint> saints) throws Exception {
        List<MenologionReader.Saint> result = new ArrayList<>(saints);
        for (MenologionReader.Saint saint : saints) {
            if (!saint.sIds().equals(saint.cId()) && !(saint.sIds().matches("\\d+")
                    && Integer.parseInt(saint.sIds()) < 10)) {
                for (String id : saint.sIds().split(",")) result.add(MenologionReader.readLife(id.trim(), id.trim(), ""));
            }
        }
        return result;
    }

    private static void append(List<String> output, String id, String tag, Map<String, String> values) {
        values.forEach((key, value) -> output.add(String.join("\t", id, tag, key, value)));
    }
}

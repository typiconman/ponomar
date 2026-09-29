package Ponomar;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Java replacement for typicon_menologion.pl. */
public final class TypiconMenologion {
    private static final LinkedHashMap<String, String> SOURCES = new LinkedHashMap<>();
    private static final String[] TYPICON = {"", "⹇", "🕃", "🕃", "🕂", "🕁", "🕀", "🕀", "🕀"};
    static { SOURCES.put("", "General"); SOURCES.put("el", "Greek sources");
        SOURCES.put("ru", "Russian sources"); SOURCES.put("la", "Roman sources"); }
    private TypiconMenologion() {}

    public static void main(String[] args) throws Exception {
        if (args.length < 2) throw new IllegalArgumentException("Usage: TypiconMenologion month day [output]");
        int month = Integer.parseInt(args[0]), day = Integer.parseInt(args[1]);
        if (month < 1 || month > 12 || day < 1 || day > JDate.getMaxDaysInMonth(month, 2015))
            throw new IllegalArgumentException("Invalid month or day");
        Path output = args.length > 2 ? Path.of(args[2]) : Path.of("Ponomar/regtests/menaion_entry.tex");
        String tex = render(MenologionReader.saints(new JDate(month, day, 2015)));
        if (output.getParent() != null) Files.createDirectories(output.getParent());
        Files.writeString(output, tex, StandardCharsets.UTF_8);
    }

    static String render(List<MenologionReader.Saint> saints) {
        StringBuilder out = new StringBuilder("\\documentclass[12pt]{article}\n\\usepackage{xltxtra,xcolor}\n"
            + "\\newfontfamily{\\slv}[Scale=1.0]{Ponomar Unicode}\n"
            + "\\setmainfont[Mapping=tex-text]{Liberation Serif}\n\\begin{document}\n\n");
        SOURCES.forEach((source, title) -> {
            if (!source.isEmpty()) out.append("\\textbf{").append(title).append("}: ");
            int index = 0;
            for (MenologionReader.Saint saint : saints) {
                if (!source.equals(saint.source()) || !saint.name().containsKey("Nominative")) continue;
                if (index++ > 0) out.append("; ");
                int rank = saint.rank();
                String symbol = rank == -2 ? "⧟" : rank >= 0 && rank < TYPICON.length ? TYPICON[rank] : "";
                if (!symbol.isEmpty()) out.append(rank >= 3 ? "\\textcolor{red}{\\slv " + symbol + "} " : "{\\slv " + symbol + "} ");
                String name = saint.name().get("Nominative");
                out.append(rank >= 4 ? "\\textcolor{red}{" + name + "}" : name);
                String note = saint.info().getOrDefault("Note", "");
                if (!note.isEmpty()) out.append("\\footnote{").append(StringOp.capitalize(note)).append('}');
                String repose = repose(saint.info()), place = saint.info().getOrDefault("ReposePlace", "");
                if (!place.isEmpty() && !repose.isEmpty()) out.append(" (").append(place).append(", ").append(repose).append(')');
                else if (!repose.isEmpty()) out.append(" (").append(repose).append(')');
            }
            out.append("\n\n");
        });
        return out.append("\\end{document}\n").toString();
    }

    private static String repose(Map<String, String> info) {
        String value = info.getOrDefault("ReposeDate", info.getOrDefault("ReposeCentury", ""));
        if (value.length() > 1) return "†" + value;
        return info.getOrDefault("Year", "");
    }
}

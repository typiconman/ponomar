package Ponomar;

/** Fresh per-test day-information maps for headless production code. */
final class DayInfoFixtures {
    private DayInfoFixtures() {}

    static OrderedHashtable englishDayInfo() {
        OrderedHashtable info = new OrderedHashtable();
        info.put("LS", "en/");
        info.put("GS", "0");
        info.put("dow", "0");
        info.put("doy", "0");
        info.put("nday", "0");
        info.put("ndayP", "0");
        info.put("ndayF", "0");
        info.put("dRank", "0");
        info.put("Tone", "1");
        info.put("Ideographic", "0");
        info.put("PS", "1");
        info.put("PFlag", "0");
        info.put("PFlag1", "0");
        info.put("PFlag2", "0");
        info.put("PFlag3", "0");
        info.put("FontFaceM", "Serif");
        info.put("FontSizeM", "12");
        info.put("FontFaceL", "Serif");
        info.put("FontSizeL", "12");
        return info;
    }

    static OrderedHashtable englishDayInfo(JDate date) {
        OrderedHashtable info = englishDayInfo();
        int year = date.getYear();
        info.put("Year", year);
        info.put("dow", date.getDayOfWeek());
        info.put("doy", date.getDoy());
        info.put("nday", JDate.difference(date, Paschalion.getPascha(year)));
        info.put("ndayP", JDate.difference(date, Paschalion.getPascha(year - 1)));
        info.put("ndayF", JDate.difference(date, Paschalion.getPascha(year + 1)));
        return info;
    }

    static OrderedHashtable englishStringDayInfo(JDate date) {
        OrderedHashtable source = englishDayInfo(date);
        OrderedHashtable strings = new OrderedHashtable();
        for (java.util.Iterator keys = source.iterateKeys(); keys.hasNext();) {
            Object key = keys.next();
            strings.put(key, source.get(key).toString());
        }
        return strings;
    }
}

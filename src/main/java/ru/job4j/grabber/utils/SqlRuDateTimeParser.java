package ru.job4j.grabber.utils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static java.util.Map.entry;

public class SqlRuDateTimeParser implements DateTimeParser {
    private static final Map<String, Integer> MONTHS = Map.ofEntries(
            entry("янв", 1),
            entry("фев", 2),
            entry("мар", 3),
            entry("апр", 4),
            entry("май", 5),
            entry("июн", 6),
            entry("июл", 7),
            entry("авг", 8),
            entry("сен", 9),
            entry("окт", 10),
            entry("ноя", 11),
            entry("дек", 12)
    );

    /**
     * 1. Сначала разобьем на дату и время
     * 2. Дату разбиваем на день, месяц, год
     * 2.1. месяц меняем по мапе
     * 3. Собираем в кучу, конверт в LocalDateTime
     *
     * @param parse - строка с неправильной датой
     * @return - LocalDateTime
     */
    @Override
    public LocalDateTime parse(String parse) {
        if (!parse.matches("^[^,]+,\\s+\\d+:\\d+$")) {
            return null;
        }
        String[] part = parse.split(",\\s+");
        String[] dmy = part[0].split(" ");
        LocalDateTime date = LocalDateTime.now();
        int day = date.getDayOfMonth();
        int month = date.getMonthValue();
        int year = date.getYear();
        String[] hm = part[1].split(":");
        int hh = Integer.parseInt(hm[0]);
        int mm = Integer.parseInt(hm[1]);
        if (dmy.length == 3) {
            if (MONTHS.containsKey(dmy[1])) {
                day = Integer.parseInt(dmy[0]);
                month = MONTHS.get(dmy[1]);
                year = Integer.parseInt("20" + dmy[2]);
            }
        } else if ("вчера".equals(part[1])) {
            date = date.minus(1, ChronoUnit.DAYS);
            day = date.getDayOfMonth();
            month = date.getMonthValue();
            year = date.getYear();
        }
        return LocalDateTime.of(year, month, day, hh, mm);
    }
}

package ru.job4j.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.SqlRuDateTimeParser;

public class SqlRuParse {
    /**
     * Слишком простой вариант. Лане не понравится. Но имеет место на существование.
     * 1.
     * String url = "https://www.sql.ru/forum/job-offers/" + (i == 0 ? "" : i);
     * 2.
     * Elements hrefs = doc.select(".sort_options").get(1).getElementsByTag("a");
     * for (Element href : hrefs) {
     * if (Integer.valueOf(i + 1).toString().equals(href.text())) {
     * url = href.attr("href");
     * break;
     * }
     * }
     */
    public static void main(String[] args) throws Exception {
        SqlRuParse sqlRuParse = new SqlRuParse();
        String url = "https://www.sql.ru/forum/job-offers/";
        for (int i = 1; i <= 5; i++) {
            System.out.println(System.lineSeparator() + "Page: " + i);
            Document doc = sqlRuParse.parsePage(url + (i == 0 ? "" : i));
            Thread.sleep(3000);
        }
    }

    private Document parsePage(String url) throws Exception {
        Document doc = Jsoup.connect(url).get();
        Elements row = doc.select(".postslisttopic");
        for (Element td : row) {
            Element href = td.child(0);
            System.out.println(href.attr("href"));
            System.out.println(href.text());
            Element parent = td.parent();
            SqlRuDateTimeParser sqlRuDateTimeParser = new SqlRuDateTimeParser();
            System.out.println(parent.child(5).text() + " => "
                    + sqlRuDateTimeParser.parse(parent.child(5).text()));
        }
        return doc;
    }
}

package ru.job4j.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.Post;
import ru.job4j.grabber.utils.SqlRuDateTimeParser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SqlRuParse {
    private List<Post> posts;

    /**
     *
     */
    public static void main(String[] args) throws Exception {
        SqlRuParse sqlRuParse = new SqlRuParse();
        sqlRuParse.posts = new ArrayList<>();
        String url = "https://www.sql.ru/forum/job-offers/";
        for (int i = 0; i < 5; i++) {
            System.out.println(System.lineSeparator() + "Page: " + i);
            sqlRuParse.parsePage(url + (i == 0 ? "" : i));
            Thread.sleep(3000);
        }
    }

    /**
     * Вытаскиваем Пост с работой
     *
     * @param postLink - ulr поста
     * @return - тескс поста
     */
    private String getPost(String postLink) throws Exception {
        Document doc = Jsoup.connect(postLink).get();
        String text = "";
        Elements textBody = doc.select(".msgBody");
        if (textBody.size() > 1) {
            text = textBody.get(1).text();
        }
        return text;
    }

    /**
     * Вытаскиваем все линки на посты.
     */
    private void parsePage(String url) throws Exception {
        Document doc = Jsoup.connect(url).get();
        SqlRuDateTimeParser sqlRuDateTimeParser = new SqlRuDateTimeParser();
        Elements rowTr = doc.getElementsByTag("tr");
        int count = 0;
        for (Element row : rowTr) {
            Elements cols = row.getElementsByTag("td");
            Elements dates = cols.select(".altCol");
            if (!dates.isEmpty()) {
                Elements postLinks = cols.select(".postslisttopic");
                if (!postLinks.isEmpty() && !postLinks.get(0).toString().contains("closedTopic")) {
                    String date = dates.get(1).text();
                    Element href = postLinks.get(0).getElementsByTag("a").get(0);
                    String postLink = href.attr("href");
                    String postText = href.text();
                    String postDescription = getPost(postLink);
                    System.out.println(date + postLink + ": " + postText);
                    System.out.println(postDescription);
                    LocalDateTime postDate = sqlRuDateTimeParser.parse(date);
                    posts.add(new Post(count++, postText, postLink, postDescription, postDate));
                    Thread.sleep(2000);
                }
            }
        }
    }
}

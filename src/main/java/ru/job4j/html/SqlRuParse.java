package ru.job4j.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.Parse;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.Post;
import ru.job4j.grabber.utils.SqlRuDateTimeParser;

import java.util.ArrayList;
import java.util.List;

public class SqlRuParse implements Parse {
    private List<Post> posts;
    private final DateTimeParser dateTimeParser;

    public SqlRuParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) throws Exception {
        SqlRuParse sqlRuParse = new SqlRuParse(new SqlRuDateTimeParser());
        sqlRuParse.posts = new ArrayList<>();
        String url = "https://www.sql.ru/forum/job-offers/";
        for (int i = 0; i < 5; i++) {
            System.out.println(System.lineSeparator() + "Page: " + (i + 1));
            sqlRuParse.posts.addAll(sqlRuParse.list(url + (i == 0 ? "" : i)));
            Thread.sleep(3000);
        }
    }

    /**
     * Вытаскиваем все линки на посты.
     */
    @Override
    public List<Post> list(String link) {
        List<Post> posts = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(link).get();
            Elements rowTr = doc.getElementsByTag("tr");
            int count = 0;
            for (Element row : rowTr) {
                Elements postLinks = row.getElementsByTag("td").select(".postslisttopic");
                if (!postLinks.isEmpty() && !postLinks.get(0).toString().contains("closedTopic")) {
                    Post post = detail(postLinks.get(0).getElementsByTag("a").get(0).attr("href"));
                    if (post != null) {
                        posts.add(post);
                    }
                    Thread.sleep(2000);
                }
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
        return posts;
    }

    /**
     * Вытаскиваем Пост с работой
     *
     * @param link - ulr поста
     * @return - тескс поста
     */
    @Override
    public Post detail(String link) {
        Post result = null;
        try {
            Document doc = Jsoup.connect(link).get();
            String postTitle = "";
            String postDescription = "";
            String postDate = "";
            Elements textBody = doc.select(".msgBody");
            if (textBody.size() > 1) {
                postDescription = textBody.get(1).text();
            }
            textBody = doc.select(".messageHeader");
            if (textBody.size() > 0) {
                postTitle = textBody.get(0).text().replaceAll("\\[new\\]", "");
            }
            textBody = doc.select(".msgFooter");
            if (textBody.size() > 0) {
                postDate = textBody.get(0).text();
                postDate = postDate.substring(0, postDate.indexOf(" ["));
            }
            System.out.println(postDate + ": " + link + ": " + postTitle);
            System.out.println(postDescription + System.lineSeparator());
            result = new Post(postTitle, link, postDescription, dateTimeParser.parse(postDate));
        } catch (Exception err) {
            err.printStackTrace();
        }
        return result;
    }
}

package ru.job4j.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import ru.job4j.grabber.Grab;
import ru.job4j.grabber.Parse;
import ru.job4j.grabber.Store;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.Post;
import ru.job4j.grabber.utils.SqlRuDateTimeParser;
import ru.job4j.quartz.AlertRabbit;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class SqlRuParse implements Parse, Store, Grab {
    private List<Post> posts;
    private final DateTimeParser dateTimeParser;
    private Connection cn;

    public SqlRuParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) throws Exception {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        SqlRuParse sqlRuParse = new SqlRuParse(new SqlRuDateTimeParser());
        sqlRuParse.init(sqlRuParse, sqlRuParse, scheduler);
    }

    /**
     * подгружаем файл конфигурации
     */
    private static Properties initProperties(String configFile) {
        Properties config = new Properties();
        try (InputStream in = AlertRabbit.class.getClassLoader()
                .getResourceAsStream(configFile)) {
            config.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }

    /**
     * Вытаскиваем все линки на посты.
     */
    @Override
    public List<Post> list(String link) {
        List<Post> posts = new ArrayList<>();
        try {
            for (int i = 0; i < 5; i++) {
                System.out.println(System.lineSeparator() + "Page: " + (i + 1));
                Document doc = Jsoup.connect(link + (i == 0 ? "" : i)).get();
                Elements rowTr = doc.getElementsByTag("tr");
                int count = 0;
                for (Element row : rowTr) {
                    Elements postLinks = row.getElementsByTag("td").select(".postslisttopic");
                    if (!postLinks.isEmpty() && !postLinks.get(0).toString().contains("closedTopic")) {
                        Post post = detail(postLinks.get(0).getElementsByTag("a").get(0).attr("href"));
                        if (checkPost(post)) {
                            System.out.println("Post - added to base.");
                            posts.add(post);
                        }
                        Thread.sleep(2000);
                    }
                }
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
        return posts;
    }

    /**
     * проверяем, соответствует ли пост нашим требованиям.
     *
     * @param post - проверяемый объект
     * @return - соответствие критериям
     */
    private boolean checkPost(Post post) {
        return post != null
                && post.getTitle().toUpperCase().contains("JAVA")
                && !post.getTitle().toUpperCase().contains("JAVASCRIPT");
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
                postTitle = textBody.get(0).ownText();
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

    @Override
    public void save(Post post) {

    }

    @Override
    public List<Post> getAll() {
        return null;
    }

    @Override
    public Post findById(int id) {
        return null;
    }

    @Override
    public void init(Parse parse, Store store, Scheduler scheduler) throws SchedulerException {
        try {
            Properties config = initProperties("app.properties");
            int parserInterval = Integer.parseInt(config.getProperty("parser.interval"));
            scheduler.start();
            JobDataMap jdata = new JobDataMap();
            jdata.put("parser", parse);
            jdata.put("store", store);
            jdata.put("url", "https://www.sql.ru/forum/job-offers/");
            JobDetail job = newJob(SqlRuParse.Quartz.class)
                    .usingJobData(jdata)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(parserInterval)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    /**
     * класс для запуска задачи по расписанию, бред! всеже это надо делать средствами ОС
     */
    public static class Quartz implements Job {
        public Quartz() {
            System.out.println(hashCode());
        }

        @Override
        public void execute(JobExecutionContext context) {
            String url = (String) context.getJobDetail().getJobDataMap().get("url");
            Parse parser = (Parse) context.getJobDetail().getJobDataMap().get("parser");
            Store store = (Store) context.getJobDetail().getJobDataMap().get("store");
            System.out.println("Quardz: " + url);
            List<Post> posts = parser.list(url);
        }
    }


}

package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class AlertRabbit {
    public static void main(String[] args) {
        AlertRabbit rabbit = new AlertRabbit();
        try (Connection connection = rabbit.connectBase("rabbit.properties")) {
            int rabbitInteval = rabbit.getRabbitInteval("rabbit.properties", "rabbit.interval");
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap jdata = new JobDataMap();
            jdata.put("connection", connection);
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(jdata)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(rabbitInteval)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
            connection.close();
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    /**
     * получить из файла конфигурации ключ
     *
     * @param configFile - файл
     * @param nameProp   - имя ключа
     * @return - значение ключа
     * @throws IOException - выкинуть на верх
     */
    private int getRabbitInteval(String configFile, String nameProp) throws IOException {
        int rabbitInteval = 10;
        try (InputStream in = AlertRabbit.class.getClassLoader()
                .getResourceAsStream(configFile)) {
            Properties config = new Properties();
            config.load(in);
            rabbitInteval = Integer.parseInt(config.getProperty(nameProp));
        }
        return rabbitInteval;
    }

    /**
     * Производит подключение к базе
     *
     * @param configFile - файл с настройками
     * @return - конект к базе
     * @throws Exception - все что есть выкидываем на верх
     */
    private Connection connectBase(String configFile) throws Exception {
        Connection cn;
        try (InputStream in = AlertRabbit.class.getClassLoader()
                .getResourceAsStream(configFile)) {
            Properties config = new Properties();
            config.load(in);
            Class.forName(config.getProperty("driver-class-name"));
            cn = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password")
            );
        }
        createTable(cn);
        return cn;
    }

    /**
     * проверяет наличие базы если ее нет, то сздает
     */
    private void createTable(Connection cn) {
        try (PreparedStatement statement = cn.prepareStatement(
                "CREATE TABLE IF NOT EXISTS rabbit "
                        + "(id SERIAL PRIMARY KEY, created_date TIMESTAMP);")) {
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * nak, nak, Neo, wake Up... fallow to White Rabbit.
     */
    public static class Rabbit implements Job {
        public Rabbit() {
            System.out.println(hashCode());
        }

        /**
         * сохраняет в базу запись. Текущее время
         *
         * @param cn - конектион к базе
         */
        public void saveToSQL(Connection cn) {
            try (PreparedStatement statement = cn.prepareStatement(
                    "INSERT INTO rabbit (created_date) VALUES(?)")) {
                statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void execute(JobExecutionContext context) {
            System.out.println("Rabbit runs here ...");
            Connection cn = (Connection) context.getJobDetail().getJobDataMap().get("connection");
            saveToSQL(cn);
        }
    }

}

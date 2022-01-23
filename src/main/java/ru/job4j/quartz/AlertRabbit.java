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
        Properties config = initProperties("rabbit.properties");
        try (Connection connection = rabbit.connectBase(config)) {
            int rabbitInteval = Integer.parseInt(config.getProperty("rabbit.interval"));
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
     * Производит подключение к базе
     *
     * @param config - контейнер с настройками
     * @return - конект к базе
     * @throws Exception - все что есть выкидываем на верх
     */
    private Connection connectBase(Properties config) throws SQLException, ClassNotFoundException {
        Connection cn;
        Class.forName(config.getProperty("driver-class-name"));
        cn = DriverManager.getConnection(
                config.getProperty("url"),
                config.getProperty("username"),
                config.getProperty("password")
        );
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

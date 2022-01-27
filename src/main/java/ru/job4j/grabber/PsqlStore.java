package ru.job4j.grabber;

import ru.job4j.grabber.utils.Post;
import ru.job4j.quartz.AlertRabbit;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {
    private Connection connection;

    public static void main(String[] args) {
        Properties config = initProperties("app.properties");
        PsqlStore psqlStore = new PsqlStore(config);
        Post post = new Post("Test job", "http://url2", "Description", LocalDateTime.now());
        psqlStore.save(post);
        System.out.println(psqlStore.findById(post.getId()));
        System.out.println(psqlStore.getAll());
    }

    public PsqlStore(Properties config) {
        try {
            Class.forName(config.getProperty("jdbc.driver"));
            connection = DriverManager.getConnection(
                    config.getProperty("jdbc.url"),
                    config.getProperty("jdbc.username"),
                    config.getProperty("jdbc.password")
            );
            createTable();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void save(Post post) {
        if (findByLink(post.getLink()) == null) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO post (name, link, text, created) VALUES(?, ?, ?, ?);",
                    Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, post.getTitle());
                statement.setString(2, post.getLink());
                statement.setString(3, post.getDescription());
                statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
                statement.execute();
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        post.setId(generatedKeys.getInt("id"));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement statement =
                     connection.prepareStatement("SELECT * FROM post;")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    posts.add(getNewPost(resultSet));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return posts;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement statement =
                     connection.prepareStatement("SELECT * FROM post WHERE id = ?;")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    post = getNewPost(resultSet);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return post;
    }

    public Post findByLink(String link) {
        Post post = null;
        try (PreparedStatement statement =
                     connection.prepareStatement("SELECT * FROM post WHERE link = '?';")) {
            statement.setString(1, link);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    post = getNewPost(resultSet);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    /**
     * пока не нужно, но гдето там это явно будет нужно
     */
    public void createTable() {
        try (PreparedStatement statement = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS post "
                        + "("
                        + " id SERIAL PRIMARY KEY,"
                        + " name VARCHAR(255),"
                        + " link VARCHAR(255) UNIQUE,"
                        + " text TEXT,"
                        + " created TIMESTAMP"
                        + ");")) {
            statement.execute();
        } catch (
                SQLException e) {
            e.printStackTrace();
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
     * Собираем все данные в одно целое
     *
     * @param resultSet - SQL запрос
     * @return - объект post
     * @throws SQLException - бывает и такое
     */
    private Post getNewPost(ResultSet resultSet) throws SQLException {
        return new Post(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("link"),
                resultSet.getString("text"),
                resultSet.getTimestamp("created").toLocalDateTime()
        );
    }

}
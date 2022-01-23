package ru.job4j.grabber.utils;

import java.time.LocalDateTime;

/**
 * - id типа int - идентификатор вакансии (берется из нашей базы данных);
 * - title типа String - название вакансии;
 * - link типа String - ссылка на описание вакансии;
 * - description типа String - описание вакансии;
 * - created типа LocalDateTime - дата создания вакансии.
 */
public class Post {
    private int id;
    private String title;
    private String link;
    private String description;
    private LocalDateTime created;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Post post = (Post) o;

        if (title != null ? !title.equals(post.title) : post.title != null) {
            return false;
        }
        if (link != null ? !link.equals(post.link) : post.link != null) {
            return false;
        }
        if (description != null ? !description.equals(post.description) : post.description != null) {
            return false;
        }
        return created != null ? created.equals(post.created) : post.created == null;
    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (link != null ? link.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (created != null ? created.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Post{"
                + "id=" + id
                + ", title='" + title + '\''
                + ", link='" + link + '\''
                + ", description='" + description + '\''
                + ", created=" + created
                + '}';
    }
}

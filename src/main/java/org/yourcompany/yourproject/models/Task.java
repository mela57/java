package org.yourcompany.yourproject.models;
import java.util.UUID;

public class Task {
    private final String id;
    private String title;
    private String description;
    private boolean done;
    private User user;

    public Task(String title, String description, User user) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.done = false;
        this.user = user;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public User getUser() {
        return user;
    }

        public void setUser(User user) {
        this.user = user;
    }
}


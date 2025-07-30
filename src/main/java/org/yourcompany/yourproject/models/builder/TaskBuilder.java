package org.yourcompany.yourproject.models.builder;

import org.yourcompany.yourproject.models.Task;
import org.yourcompany.yourproject.models.User;

public class TaskBuilder {
    
    private String title;
    private String description;
    private User user;

    public TaskBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public TaskBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public TaskBuilder setUser(User user) {
        this.user = user;
        return this;
    }

    public Task build() {
        return new Task(title, description, user);
    }
}

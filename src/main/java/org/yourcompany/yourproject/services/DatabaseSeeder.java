package org.yourcompany.yourproject.services;

import org.yourcompany.yourproject.models.Task;
import org.yourcompany.yourproject.models.User;
import org.yourcompany.yourproject.models.builder.TaskBuilder;

public class DatabaseSeeder {
    private final DatabaseAccess db = DatabaseAccess.getInstance();

    public void seed(){
        User user = new User("Mélanie");
        db.addUser(user);

        Task t1 = new TaskBuilder().setTitle("Faire une to-do list").setDescription("AHAHAHAHAH").setUser(user).build();
        db.addTask(t1);
    }
}

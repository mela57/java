// DatabaseAccess.java
package org.yourcompany.yourproject.services;

import java.util.ArrayList;
import java.util.List;

import org.yourcompany.yourproject.models.Task;
import org.yourcompany.yourproject.models.User;

public class DatabaseAccess {

    private static DatabaseAccess instance;
    
    private final List<User> users;
    private final List<Task> tasks;

    private DatabaseAccess() {
        users = new ArrayList<>();
        tasks = new ArrayList<>();
    }

    public static DatabaseAccess getInstance() {
        if (instance == null) {
            instance = new DatabaseAccess();
        }
        return instance;
    }

    // CRUD Users
    public void addUser(User user) {
        users.add(user);
    }

    public User findUserById(int id) throws NotFoundException {
        return users.stream().filter(u -> u.getId() == id).findFirst().orElseThrow(() ->
            new NotFoundException("Utilisateur non trouvé")
        );
    }

    public void removeUser(int id) throws NotFoundException {
        User user = findUserById(id);
        users.remove(user);
    }

    // CRUD Tasks
    public void addTask(Task task) {
        tasks.add(task);
    }

    public Task findTaskById(int id) throws NotFoundException {
        return tasks.stream().filter(t -> t.getId() == id).findFirst().orElseThrow(() ->
            new NotFoundException("Tache non trouvée")
        );
    }

    public void removeTask(int id) throws NotFoundException {
        Task task = findTaskById(id);
        tasks.remove(task);
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Task> getTasks() {
        return tasks;
    }
}

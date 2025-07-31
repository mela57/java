package org.yourcompany.yourproject.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.yourcompany.yourproject.models.Task;
import org.yourcompany.yourproject.models.User;

public class DatabaseAccess {

    private static DatabaseAccess instance;
    
    private final List<User> users;
    private final List<Task> tasks;
    private final Connection connection;

    private final String createUsersTable =
    "CREATE TABLE IF NOT EXISTS users (" +
    "id INT PRIMARY KEY AUTO_INCREMENT," +
    "firstName VARCHAR(255) NOT NULL" +
    ")";

    private final String createUsers = "INSERT INTO users (firstName) VALUES (?)";

    private final String createTasksTable =
    "CREATE TABLE IF NOT EXISTS tasks (" +
    "id INT PRIMARY KEY AUTO_INCREMENT," +
    "title VARCHAR(255) NOT NULL," +
    "description TEXT," +
    "done BOOLEAN," +
    "user_id INT," +
    "FOREIGN KEY (user_id) REFERENCES users(id)" +
    ")";

    private final String createTasks = "INSERT INTO tasks (title, description, done, user_id) VALUES (?, ?, ?, ?)";


    private DatabaseAccess() {
        users = new ArrayList<>();
        tasks = new ArrayList<>();
        try {
            connection = DriverManager.getConnection ("jdbc:h2:mem:db1");
            createTables();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createTables() throws SQLException {
        connection.createStatement().executeUpdate(createUsersTable);
        connection.createStatement().executeUpdate(createTasksTable);
        
    }

    public static DatabaseAccess getInstance() {
        if (instance == null) {
            instance = new DatabaseAccess();
        }
        return instance;
    }

    // CRUD Users
    public void addUser(User user) {
        // users.add(user);
        System.out.println("Ajout User" + user.getId());
        try {
            PreparedStatement stmt = connection.prepareStatement(createUsers);
            stmt.setString(1, user.getFirstName());
            stmt.executeUpdate();
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
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
        System.out.println("Ajout Tache" + task.getId());
        try {
            PreparedStatement stmt = connection.prepareStatement(createTasks);
            stmt.setString(1, task.getTitle());
            stmt.setString(2, task.getDescription());
            stmt.setBoolean(3, task.isDone());
            stmt.setInt(4, task.getUser().getId());
            stmt.executeUpdate();
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
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

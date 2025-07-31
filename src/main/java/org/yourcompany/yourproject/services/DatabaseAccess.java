package org.yourcompany.yourproject.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
    private final String selectUserById = "SELECT id, firstName FROM users WHERE id = ?";
    private final String updateUserById = "UPDATE users SET firstName = ? WHERE id = ?";
    private final String deleteUserById = "DELETE FROM users WHERE id = ?";
    private final String selectAllUsers = "SELECT id, firstName FROM users";

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
    private final String selectTaskById = "SELECT id, title, description, done, user_id FROM tasks WHERE id = ?";
    private final String updateTaskById = "UPDATE tasks SET title = ?, description = ?, done = ?, user_id = ? WHERE id = ?";
    private final String deleteTaskById = "DELETE FROM tasks WHERE id = ?";
    private final String selectAllTasks = "SELECT id, title, description, done, user_id FROM tasks";


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
            try {
        PreparedStatement stmt = connection.prepareStatement(selectUserById);
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            User user = new User(rs.getString("firstName"));
            // Mettre l'id à jour si besoin (à adapter selon ta classe User)
            // user.setId(rs.getInt("id")); // à faire si l'id n'est pas final
            return user;
        } else {
            throw new NotFoundException("Utilisateur non trouvé");
        }
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
        // return users.stream().filter(u -> u.getId() == id).findFirst().orElseThrow(() ->
        //     new NotFoundException("Utilisateur non trouvé")
        // );
    }

    public void removeUser(int id) throws NotFoundException {
        try {
            PreparedStatement stmt = connection.prepareStatement(deleteUserById);
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
        // User user = findUserById(id);
        // users.remove(user);
    }

    public void updateUser(User user) {
        try {
            PreparedStatement stmt = connection.prepareStatement(updateUserById);
            stmt.setString(1, user.getFirstName());
            stmt.setInt(2, user.getId());
            stmt.executeUpdate();
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
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
            try {
        PreparedStatement stmt = connection.prepareStatement(selectTaskById);
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            // Il te faut d'abord retrouver le User associé
            User user = findUserById(rs.getInt("user_id"));
            Task task = new Task(
                rs.getString("title"),
                rs.getString("description"),
                user
            );
            // task.setId(rs.getInt("id")); // à adapter selon ta classe
            task.setDone(rs.getBoolean("done"));
            return task;
        } else {
            throw new NotFoundException("Tache non trouvée");
        }
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
        // return tasks.stream().filter(t -> t.getId() == id).findFirst().orElseThrow(() ->
        //     new NotFoundException("Tache non trouvée")
        // );
    }

    
    public void updateTask(Task task) {
        try {
            PreparedStatement stmt = connection.prepareStatement(updateTaskById);
            stmt.setString(1, task.getTitle());
            stmt.setString(2, task.getDescription());
            stmt.setBoolean(3, task.isDone());
            stmt.setInt(4, task.getUser().getId());
            stmt.setInt(5, task.getId());
            stmt.executeUpdate();
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    public void removeTask(int id) throws NotFoundException {
        try {
            PreparedStatement stmt = connection.prepareStatement(deleteTaskById);
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
        // Task task = findTaskById(id);
        // tasks.remove(task);
    }

    public List<User> getUsers() {
            List<User> result = new ArrayList<>();
    try {
        PreparedStatement stmt = connection.prepareStatement(selectAllUsers);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            User user = new User(rs.getString("firstName"));
            // user.setId(rs.getInt("id")); // Idem, à faire si l'id n'est pas final
            result.add(user);
        }
    } catch (SQLException e){
        throw new RuntimeException(e);
    }
    return result;
        // return users;
    }

    public List<Task> getTasks() throws NotFoundException {
            List<Task> result = new ArrayList<>();
    try {
        PreparedStatement stmt = connection.prepareStatement(selectAllTasks);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            User user = findUserById(rs.getInt("user_id"));
            Task task = new Task(
                rs.getString("title"),
                rs.getString("description"),
                user
            );
            // task.setId(rs.getInt("id"));
            task.setDone(rs.getBoolean("done"));
            result.add(task);
        }
    } catch (SQLException e){
        throw new RuntimeException(e);
    }
    return result;
        // return tasks;
    }
}

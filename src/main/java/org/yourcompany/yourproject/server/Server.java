package org.yourcompany.yourproject.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import org.yourcompany.yourproject.models.Task;
import org.yourcompany.yourproject.models.User;
import org.yourcompany.yourproject.services.DatabaseAccess;
import org.yourcompany.yourproject.services.NotFoundException;

public class Server {
    private final int port;
    private final DatabaseAccess db;

    public Server(int port) {
        this.port = port;
        this.db = DatabaseAccess.getInstance();
    }

    public void start() {
        System.out.println("🚀 Serveur démarré sur le port " + port);
        System.out.println("📱 Interface web : http://localhost:" + port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream(), "UTF-8")
                );

                String line = in.readLine();
                String url = "/";
                String method = "GET";
                
                if (line != null && !line.isEmpty()) {
                    System.out.println("Requête : " + line);
                    String[] parts = line.split(" ");
                    if (parts.length >= 2) {
                        method = parts[0];
                        url = parts[1];
                    }
                }

                // Lire les headers
                StringBuilder requestBody = new StringBuilder();
                int contentLength = 0;
                while ((line = in.readLine()) != null && !line.isEmpty()) {
                    if (line.startsWith("Content-Length:")) {
                        contentLength = Integer.parseInt(line.split(":")[1].trim());
                    }
                }

                // Lire le body si présent
                if (contentLength > 0) {
                    char[] buffer = new char[contentLength];
                    in.read(buffer, 0, contentLength);
                    requestBody.append(buffer);
                }

                PrintWriter out = new PrintWriter(clientSocket.getOutputStream());

                // Router les requêtes
                handleRequest(method, url, requestBody.toString(), out);

                out.flush();
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Erreur serveur : " + e.getMessage());
        }
    }

    private void handleRequest(String method, String url, String body, PrintWriter out) {
        // Headers CORS pour éviter les problèmes avec JavaScript
            String corsHeaders = "Access-Control-Allow-Origin: *\r\n" +
                            "Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS\r\n" +
                            "Access-Control-Allow-Headers: Content-Type\r\n";

        switch (url) {
            case "/", "" -> {
                String html = getMainPageHtml();
                out.print("HTTP/1.1 200 OK\r\n");
                out.print("Content-Type: text/html; charset=utf-8\r\n");
                out.print("Content-Length: " + html.getBytes().length + "\r\n");
                out.print(corsHeaders);
                out.print("\r\n");
                out.print(html);
            }
            
            case "/api/users" -> {
                if ("GET".equals(method)) {
                    handleGetUsers(out, corsHeaders);
                } else if ("POST".equals(method)) {
                    handleCreateUser(body, out, corsHeaders);
                }
            }
            
            case "/api/tasks" -> {
                if ("GET".equals(method)) {
                    handleGetTasks(out, corsHeaders);
                } else if ("POST".equals(method)) {
                    handleCreateTask(body, out, corsHeaders);
                }
            }
            
            default -> {
                // Gérer les URLs avec ID (ex: /api/users/1, /api/tasks/1)
                if (url.startsWith("/api/users/")) {
                    String idStr = url.substring("/api/users/".length());
                    try {
                        int id = Integer.parseInt(idStr);
                        if ("DELETE".equals(method)) {
                            handleDeleteUser(id, out, corsHeaders);
                        }
                    } catch (NumberFormatException e) {
                        send404(out, corsHeaders);
                    }
                } else if (url.startsWith("/api/tasks/")) {
                    String[] parts = url.split("/");
                    if (parts.length >= 4) {
                        try {
                            int id = Integer.parseInt(parts[3]);
                            if ("DELETE".equals(method)) {
                                handleDeleteTask(id, out, corsHeaders);
                            } else if ("PUT".equals(method)) {
                                handleUpdateTask(id, body, out, corsHeaders);
                            }
                        } catch (NumberFormatException e) {
                            send404(out, corsHeaders);
                        }
                    }
                } else {
                    send404(out, corsHeaders);
                }
            }
        }
    }

    private void handleGetUsers(PrintWriter out, String corsHeaders) {
        try {
            List<User> users = db.getUsers();
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                json.append("{\"id\":").append(user.getId())
                    .append(",\"firstName\":\"").append(user.getFirstName()).append("\"}");
                if (i < users.size() - 1) json.append(",");
            }
            json.append("]");

            out.print("HTTP/1.1 200 OK\r\n");
            out.print("Content-Type: application/json\r\n");
            out.print("Content-Length: " + json.toString().getBytes().length + "\r\n");
            out.print(corsHeaders);
            out.print("\r\n");
            out.print(json.toString());
        } catch (Exception e) {
            sendError(out, corsHeaders, "Erreur lors de la récupération des utilisateurs");
        }
    }

    private void handleCreateUser(String body, PrintWriter out, String corsHeaders) {
        try {
            // Parse simple du JSON (firstName:"value")
            String firstName = extractJsonValue(body, "firstName");
            if (firstName != null) {
                User user = new User(firstName);
                db.addUser(user);
                
                String response = "{\"success\":true,\"message\":\"Utilisateur créé\"}";
                out.print("HTTP/1.1 201 Created\r\n");
                out.print("Content-Type: application/json\r\n");
                out.print("Content-Length: " + response.getBytes().length + "\r\n");
                out.print(corsHeaders);
                out.print("\r\n");
                out.print(response);
            } else {
                sendError(out, corsHeaders, "Nom requis");
            }
        } catch (Exception e) {
            sendError(out, corsHeaders, "Erreur lors de la création de l'utilisateur");
        }
    }

    private void handleGetTasks(PrintWriter out, String corsHeaders) {
        try {
            List<Task> tasks = db.getTasks();
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < tasks.size(); i++) {
                Task task = tasks.get(i);
                json.append("{\"id\":").append(task.getId())
                    .append(",\"title\":\"").append(task.getTitle()).append("\"")
                    .append(",\"description\":\"").append(task.getDescription() != null ? task.getDescription() : "").append("\"")
                    .append(",\"done\":").append(task.isDone())
                    .append(",\"user\":{\"id\":").append(task.getUser().getId())
                    .append(",\"firstName\":\"").append(task.getUser().getFirstName()).append("\"}}");
                if (i < tasks.size() - 1) json.append(",");
            }
            json.append("]");

            out.print("HTTP/1.1 200 OK\r\n");
            out.print("Content-Type: application/json\r\n");
            out.print("Content-Length: " + json.toString().getBytes().length + "\r\n");
            out.print(corsHeaders);
            out.print("\r\n");
            out.print(json.toString());
        } catch (Exception e) {
            sendError(out, corsHeaders, "Erreur lors de la récupération des tâches");
        }
    }

    private void handleCreateTask(String body, PrintWriter out, String corsHeaders) {
        try {
            String title = extractJsonValue(body, "title");
            String description = extractJsonValue(body, "description");
            String userIdStr = extractJsonValue(body, "userId");
            
            if (title != null && userIdStr != null) {
                int userId = Integer.parseInt(userIdStr);
                User user = db.findUserById(userId);
                
                Task task = new Task(title, description, user);
                db.addTask(task);
                
                String response = "{\"success\":true,\"message\":\"Tâche créée\"}";
                out.print("HTTP/1.1 201 Created\r\n");
                out.print("Content-Type: application/json\r\n");
                out.print("Content-Length: " + response.getBytes().length + "\r\n");
                out.print(corsHeaders);
                out.print("\r\n");
                out.print(response);
            } else {
                sendError(out, corsHeaders, "Titre et utilisateur requis");
            }
        } catch (NotFoundException e) {
            sendError(out, corsHeaders, "Utilisateur non trouvé");
        } catch (Exception e) {
            sendError(out, corsHeaders, "Erreur lors de la création de la tâche");
        }
    }

    private void handleDeleteUser(int id, PrintWriter out, String corsHeaders) {
        try {
            db.removeUser(id);
            String response = "{\"success\":true,\"message\":\"Utilisateur supprimé\"}";
            out.print("HTTP/1.1 200 OK\r\n");
            out.print("Content-Type: application/json\r\n");
            out.print("Content-Length: " + response.getBytes().length + "\r\n");
            out.print(corsHeaders);
            out.print("\r\n");
            out.print(response);
        } catch (Exception e) {
            sendError(out, corsHeaders, "Erreur lors de la suppression");
        }
    }

    private void handleDeleteTask(int id, PrintWriter out, String corsHeaders) {
        try {
            db.removeTask(id);
            String response = "{\"success\":true,\"message\":\"Tâche supprimée\"}";
            out.print("HTTP/1.1 200 OK\r\n");
            out.print("Content-Type: application/json\r\n");
            out.print("Content-Length: " + response.getBytes().length + "\r\n");
            out.print(corsHeaders);
            out.print("\r\n");
            out.print(response);
        } catch (Exception e) {
            sendError(out, corsHeaders, "Erreur lors de la suppression");
        }
    }

    private void handleUpdateTask(int id, String body, PrintWriter out, String corsHeaders) {
        try {
            Task task = db.findTaskById(id);
            
            String doneStr = extractJsonValue(body, "done");
            if (doneStr != null) {
                task.setDone(Boolean.parseBoolean(doneStr));
                db.updateTask(task);
            }
            
            String response = "{\"success\":true,\"message\":\"Tâche mise à jour\"}";
            out.print("HTTP/1.1 200 OK\r\n");
            out.print("Content-Type: application/json\r\n");
            out.print("Content-Length: " + response.getBytes().length + "\r\n");
            out.print(corsHeaders);
            out.print("\r\n");
            out.print(response);
        } catch (Exception e) {
            sendError(out, corsHeaders, "Erreur lors de la mise à jour");
        }
    }

    private void sendError(PrintWriter out, String corsHeaders, String message) {
        String response = "{\"success\":false,\"message\":\"" + message + "\"}";
        out.print("HTTP/1.1 500 Internal Server Error\r\n");
        out.print("Content-Type: application/json\r\n");
        out.print("Content-Length: " + response.getBytes().length + "\r\n");
        out.print(corsHeaders);
        out.print("\r\n");
        out.print(response);
    }

    private void send404(PrintWriter out, String corsHeaders) {
        String response = "{\"success\":false,\"message\":\"Page non trouvée\"}";
        out.print("HTTP/1.1 404 Not Found\r\n");
        out.print("Content-Type: application/json\r\n");
        out.print("Content-Length: " + response.getBytes().length + "\r\n");
        out.print(corsHeaders);
        out.print("\r\n");
        out.print(response);
    }

    private String extractJsonValue(String json, String key) {
        try {
            String searchFor = "\"" + key + "\":";
            int startIndex = json.indexOf(searchFor);
            if (startIndex == -1) return null;
            
            startIndex += searchFor.length();
            while (startIndex < json.length() && json.charAt(startIndex) == ' ') startIndex++;
            
            if (startIndex >= json.length()) return null;
            
            if (json.charAt(startIndex) == '"') {
                startIndex++;
                int endIndex = json.indexOf('"', startIndex);
                if (endIndex == -1) return null;
                return json.substring(startIndex, endIndex);
            } else {
                int endIndex = startIndex;
                while (endIndex < json.length() && 
                       json.charAt(endIndex) != ',' && 
                       json.charAt(endIndex) != '}' && 
                       json.charAt(endIndex) != ' ') {
                    endIndex++;
                }
                return json.substring(startIndex, endIndex);
            }
        } catch (Exception e) {
            return null;
        }
    }

    private String getMainPageHtml() {
        return """
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Task Manager - Interface Java</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            color: #333;
        }

        .container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 20px;
        }

        header {
            text-align: center;
            margin-bottom: 2rem;
        }

        h1 {
            color: white;
            font-size: 2.5rem;
            text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
            margin-bottom: 0.5rem;
        }

        .subtitle {
            color: rgba(255,255,255,0.9);
            font-size: 1.1rem;
        }

        .main-content {
            display: grid;
            grid-template-columns: 1fr 2fr;
            gap: 2rem;
            margin-top: 2rem;
        }

        .card {
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(10px);
            border-radius: 15px;
            padding: 1.5rem;
            box-shadow: 0 8px 32px rgba(0,0,0,0.1);
            border: 1px solid rgba(255,255,255,0.2);
        }

        .card h2 {
            color: #4a5568;
            margin-bottom: 1rem;
            font-size: 1.3rem;
            border-bottom: 2px solid #667eea;
            padding-bottom: 0.5rem;
        }

        .form-group {
            margin-bottom: 1rem;
        }

        label {
            display: block;
            margin-bottom: 0.5rem;
            font-weight: 600;
            color: #4a5568;
        }

        input, textarea, select {
            width: 100%;
            padding: 0.75rem;
            border: 2px solid #e2e8f0;
            border-radius: 8px;
            font-size: 1rem;
            transition: all 0.3s ease;
        }

        input:focus, textarea:focus, select:focus {
            outline: none;
            border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
        }

        textarea {
            resize: vertical;
            min-height: 80px;
        }

        .btn {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            padding: 0.75rem 1.5rem;
            border-radius: 8px;
            cursor: pointer;
            font-size: 1rem;
            font-weight: 600;
            transition: all 0.3s ease;
            box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
        }

        .btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
        }

        .btn-secondary {
            background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
            box-shadow: 0 4px 15px rgba(72, 187, 120, 0.3);
        }

        .btn-danger {
            background: linear-gradient(135deg, #f56565 0%, #e53e3e 100%);
            box-shadow: 0 4px 15px rgba(245, 101, 101, 0.3);
        }

        .task-list {
            max-height: 600px;
            overflow-y: auto;
        }

        .task-item {
            background: white;
            border: 1px solid #e2e8f0;
            border-radius: 10px;
            padding: 1rem;
            margin-bottom: 1rem;
            transition: all 0.3s ease;
        }

        .task-item:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 20px rgba(0,0,0,0.1);
        }

        .task-item.completed {
            opacity: 0.7;
            background: #f0fff4;
            border-color: #68d391;
        }

        .task-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 0.5rem;
        }

        .task-title {
            font-weight: 600;
            color: #2d3748;
            flex-grow: 1;
            margin-right: 1rem;
        }

        .task-title.completed {
            text-decoration: line-through;
            color: #68d391;
        }

        .task-status {
            background: #667eea;
            color: white;
            padding: 0.2rem 0.5rem;
            border-radius: 20px;
            font-size: 0.8rem;
            font-weight: 600;
        }

        .task-status.completed {
            background: #48bb78;
        }

        .task-description {
            color: #718096;
            margin-bottom: 0.5rem;
            font-style: italic;
        }

        .task-user {
            color: #4a5568;
            font-size: 0.9rem;
            margin-bottom: 0.5rem;
        }

        .task-actions {
            display: flex;
            gap: 0.5rem;
        }

        .btn-small {
            padding: 0.4rem 0.8rem;
            font-size: 0.8rem;
        }

        .users-list {
            max-height: 300px;
            overflow-y: auto;
        }

        .user-item {
            background: white;
            border: 1px solid #e2e8f0;
            border-radius: 8px;
            padding: 0.75rem;
            margin-bottom: 0.5rem;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .user-name {
            font-weight: 600;
            color: #2d3748;
        }

        .user-id {
            color: #718096;
            font-size: 0.8rem;
        }

        .stats {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
            gap: 1rem;
            margin-bottom: 2rem;
        }

        .stat-card {
            background: rgba(255, 255, 255, 0.9);
            padding: 1rem;
            border-radius: 10px;
            text-align: center;
            box-shadow: 0 4px 15px rgba(0,0,0,0.1);
        }

        .stat-number {
            font-size: 2rem;
            font-weight: bold;
            color: #667eea;
        }

        .stat-label {
            color: #718096;
            font-size: 0.9rem;
            margin-top: 0.25rem;
        }

        .loading {
            text-align: center;
            padding: 2rem;
            color: #718096;
        }

        .error {
            background: #fed7d7;
            color: #c53030;
            padding: 1rem;
            border-radius: 8px;
            margin-bottom: 1rem;
            border-left: 4px solid #f56565;
        }

        .success {
            background: #c6f6d5;
            color: #22543d;
            padding: 1rem;
            border-radius: 8px;
            margin-bottom: 1rem;
            border-left: 4px solid #48bb78;
        }

        @media (max-width: 768px) {
            .main-content {
                grid-template-columns: 1fr;
            }
            
            h1 {
                font-size: 2rem;
            }
            
            .stats {
                grid-template-columns: repeat(2, 1fr);
            }
        }

        .fade-in {
            animation: fadeIn 0.5s ease-in;
        }

        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(20px); }
            to { opacity: 1; transform: translateY(0); }
        }
    </style>
</head>
<body>
    <div class="container">
        <header>
            <h1>Task Manager</h1>
            <p class="subtitle">Gestionnaire de tâches Java avec H2 Database</p>
        </header>

        <div class="stats">
            <div class="stat-card">
                <div class="stat-number" id="totalTasks">0</div>
                <div class="stat-label">Tâches totales</div>
            </div>
            <div class="stat-card">
                <div class="stat-number" id="completedTasks">0</div>
                <div class="stat-label">Terminées</div>
            </div>
            <div class="stat-card">
                <div class="stat-number" id="pendingTasks">0</div>
                <div class="stat-label">En cours</div>
            </div>
            <div class="stat-card">
                <div class="stat-number" id="totalUsers">0</div>
                <div class="stat-label">Utilisateurs</div>
            </div>
        </div>

        <div class="main-content">
            <div>
                <div class="card">
                    <h2>👤 Nouvel Utilisateur</h2>
                    <form id="userForm">
                        <div class="form-group">
                            <label for="firstName">Prénom</label>
                            <input type="text" id="firstName" name="firstName" required>
                        </div>
                        <button type="submit" class="btn">Ajouter Utilisateur</button>
                    </form>
                </div>

                <div class="card" style="margin-top: 1rem;">
                    <h2>👥 Utilisateurs</h2>
                    <div id="usersList" class="users-list">
                        <div class="loading">Chargement...</div>
                    </div>
                </div>

                <div class="card" style="margin-top: 1rem;">
                    <h2>📝 Nouvelle Tâche</h2>
                    <form id="taskForm">
                        <div class="form-group">
                            <label for="taskTitle">Titre</label>
                            <input type="text" id="taskTitle" name="title" required>
                        </div>
                        <div class="form-group">
                            <label for="taskDescription">Description</label>
                            <textarea id="taskDescription" name="description"></textarea>
                        </div>
                        <div class="form-group">
                            <label for="taskUser">Utilisateur</label>
                            <select id="taskUser" name="userId" required>
                                <option value="">Sélectionner un utilisateur</option>
                            </select>
                        </div>
                        <button type="submit" class="btn btn-secondary">Créer Tâche</button>
                    </form>
                </div>
            </div>

            <div class="card">
                <h2>📋 Liste des Tâches</h2>
                <div id="tasksList" class="task-list">
                    <div class="loading">Chargement des tâches...</div>
                </div>
            </div>
        </div>
    </div>

    <script>
        const API_BASE_URL = '';

        let users = [];
        let tasks = [];

        const userForm = document.getElementById('userForm');
        const taskForm = document.getElementById('taskForm');
        const usersList = document.getElementById('usersList');
        const tasksList = document.getElementById('tasksList');
        const taskUserSelect = document.getElementById('taskUser');

        document.addEventListener('DOMContentLoaded', function() {
            loadUsers();
            loadTasks();
            
            userForm.addEventListener('submit', handleUserSubmit);
            taskForm.addEventListener('submit', handleTaskSubmit);
        });

        async function handleUserSubmit(e) {
            e.preventDefault();
            const formData = new FormData(e.target);
            const firstName = formData.get('firstName');
            
            try {
                const response = await fetch('/api/users', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({ firstName: firstName })
                });
                
                if (response.ok) {
                    loadUsers();
                    updateUserSelect();
                    e.target.reset();
                    showMessage('Utilisateur ajouté avec succès!', 'success');
                } else {
                    showMessage('Erreur lors de l\\'ajout de l\\'utilisateur', 'error');
                }
            } catch (error) {
                showMessage('Erreur de connexion', 'error');
            }
        }

        async function handleTaskSubmit(e) {
            e.preventDefault();
            const formData = new FormData(e.target);
            
            const title = formData.get('title');
            const description = formData.get('description');
            const userId = parseInt(formData.get('userId'));
            
            try {
                const response = await fetch('/api/tasks', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({ 
                        title: title, 
                        description: description, 
                        userId: userId 
                    })
                });
                
                if (response.ok) {
                    loadTasks();
                    e.target.reset();
                    showMessage('Tâche créée avec succès!', 'success');
                } else {
                    showMessage('Erreur lors de la création de la tâche', 'error');
                }
            } catch (error) {
                showMessage('Erreur de connexion', 'error');
            }
        }

        async function loadUsers() {
            try {
                const response = await fetch('/api/users');
                if (response.ok) {
                    users = await response.json();
                    displayUsers();
                    updateUserSelect();
                    updateStats();
                }
            } catch (error) {
                console.error('Erreur lors du chargement des utilisateurs:', error);
            }
        }

        async function loadTasks() {
            try {
                const response = await fetch('/api/tasks');
                if (response.ok) {
                    tasks = await response.json();
                    displayTasks();
                    updateStats();
                }
            } catch (error) {
                console.error('Erreur lors du chargement des tâches:', error);
            }
        }

        function displayUsers() {
            if (users.length === 0) {
                usersList.innerHTML = '<div class="loading">Aucun utilisateur</div>';
                return;
            }

            usersList.innerHTML = users.map(user => `
                <div class="user-item fade-in">
                    <div>
                        <div class="user-name">${user.firstName}</div>
                        <div class="user-id">ID: ${user.id}</div>
                    </div>
                    <button class="btn btn-danger btn-small" onclick="deleteUser(${user.id})">
                        Supprimer
                    </button>
                </div>
            `).join('');
        }

        function displayTasks() {
            if (tasks.length === 0) {
                tasksList.innerHTML = '<div class="loading">Aucune tâche</div>';
                return;
            }

            tasksList.innerHTML = tasks.map(task => `
                <div class="task-item fade-in ${task.done ? 'completed' : ''}">
                    <div class="task-header">
                        <div class="task-title ${task.done ? 'completed' : ''}">${task.title}</div>
                        <div class="task-status ${task.done ? 'completed' : ''}">
                            ${task.done ? 'Terminée' : 'En cours'}
                        </div>
                    </div>
                    <div class="task-description">${task.description || 'Pas de description'}</div>
                    <div class="task-user">👤 ${task.user.firstName}</div>
                    <div class="task-actions">
                        <button class="btn btn-small ${task.done ? 'btn-secondary' : ''}" 
                                onclick="toggleTask(${task.id})">
                            ${task.done ? 'Réactiver' : 'Terminer'}
                        </button>
                        <button class="btn btn-danger btn-small" onclick="deleteTask(${task.id})">
                            Supprimer
                        </button>
                    </div>
                </div>
            `).join('');
        }

        function updateUserSelect() {
            const options = users.map(user => 
                `<option value="${user.id}">${user.firstName}</option>`
            ).join('');
            
            taskUserSelect.innerHTML = `
                <option value="">Sélectionner un utilisateur</option>
                ${options}
            `;
        }

        function updateStats() {
            const totalTasks = tasks.length;
            const completedTasks = tasks.filter(t => t.done).length;
            const pendingTasks = totalTasks - completedTasks;
            const totalUsers = users.length;

            document.getElementById('totalTasks').textContent = totalTasks;
            document.getElementById('completedTasks').textContent = completedTasks;
            document.getElementById('pendingTasks').textContent = pendingTasks;
            document.getElementById('totalUsers').textContent = totalUsers;
        }

        async function toggleTask(taskId) {
            const task = tasks.find(t => t.id === taskId);
            if (task) {
                try {
                    const response = await fetch(`/api/tasks/${taskId}`, {
                        method: 'PUT',
                        headers: {
                            'Content-Type': 'application/json',
                        },
                        body: JSON.stringify({ done: !task.done })
                    });
                    
                    if (response.ok) {
                        loadTasks();
                        showMessage(`Tâche ${!task.done ? 'terminée' : 'réactivée'}!`, 'success');
                    } else {
                        showMessage('Erreur lors de la mise à jour', 'error');
                    }
                } catch (error) {
                    showMessage('Erreur de connexion', 'error');
                }
            }
        }

        async function deleteTask(taskId) {
            if (confirm('Êtes-vous sûr de vouloir supprimer cette tâche ?')) {
                try {
                    const response = await fetch(`/api/tasks/${taskId}`, {
                        method: 'DELETE'
                    });
                    
                    if (response.ok) {
                        loadTasks();
                        showMessage('Tâche supprimée!', 'success');
                    } else {
                        showMessage('Erreur lors de la suppression', 'error');
                    }
                } catch (error) {
                    showMessage('Erreur de connexion', 'error');
                }
            }
        }

        async function deleteUser(userId) {
            const userTasks = tasks.filter(t => t.user.id === userId);
            if (userTasks.length > 0) {
                if (!confirm(`Cet utilisateur a ${userTasks.length} tâche(s). Voulez-vous vraiment le supprimer ?`)) {
                    return;
                }
            }
            
            try {
                const response = await fetch(`/api/users/${userId}`, {
                    method: 'DELETE'
                });
                
                if (response.ok) {
                    loadUsers();
                    loadTasks();
                    showMessage('Utilisateur supprimé!', 'success');
                } else {
                    showMessage('Erreur lors de la suppression', 'error');
                }
            } catch (error) {
                showMessage('Erreur de connexion', 'error');
            }
        }

        function showMessage(message, type) {
            const messageDiv = document.createElement('div');
            messageDiv.className = type;
            messageDiv.textContent = message;
            
            document.querySelector('.container').insertBefore(messageDiv, document.querySelector('.stats'));
            
            setTimeout(() => {
                messageDiv.remove();
            }, 3000);
        }
    </script>
</body>
</html>
        """;
    }
}


// package org.yourcompany.yourproject.server;

// import java.io.BufferedReader;
// import java.io.IOException;
// import java.io.InputStreamReader;
// import java.io.PrintWriter;
// import java.net.ServerSocket;
// import java.net.Socket;
// import java.util.ArrayList;
// import java.util.List;

// public class Server {
//     private final int port;
//     private final List<String> users;
//     private final List<String> tasks;

//     public Server(int port) {
//         this.port = port;
//         this.users = new ArrayList<>();
//         this.tasks = new ArrayList<>();
//         // Init de base
//         users.add("Mel");
//         users.add("Greg");
//         users.add("Laura");
//         tasks.add("Cours de Java");
//         tasks.add("Rendre TP serveur");
//         tasks.add("Faire les couillions !");
//     }

//     public void start() {
//         System.out.println("Serveur démarré sur le port " + port);

//         try (ServerSocket serverSocket = new ServerSocket(port)) {
//             while (true) {
//                 Socket clientSocket = serverSocket.accept();

//                 BufferedReader in = new BufferedReader(
//                         new InputStreamReader(clientSocket.getInputStream(), "UTF-8")
//                 );

//                 String line = in.readLine();
//                 String url = "/";
//                 if (line != null && !line.isEmpty()) {
//                     System.out.println("Requête brute : " + line);
//                     String[] parts = line.split(" ");
//                     if (parts.length >= 2) {
//                         url = parts[1];
//                     }
//                 }

                
//                 while ((line = in.readLine()) != null && !line.isEmpty()) {}

//                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
//                 String hello = "<html><body><h1>Hello world!</h1></body></html>";

//                 switch (url) {
//                     case "/","" -> {
//                         out.print("HTTP/1.1 200 OK\r\n");
//                         out.print("Content-Type: text/html\r\n");
//                         out.print("Content-Length: " + hello.length() + "\r\n");
//                         out.print("\r\n");
//                         out.print(hello);
//                         break;
//                         }
//                     case "/users" -> {
//                             String responseBody = String.join("\n", users);
//                             out.print("HTTP/1.1 200 OK\r\n");
//                             out.print("Content-Type: text/plain\r\n");
//                             out.print("Content-Length: " + responseBody.length() + "\r\n");
//                             out.print("\r\n");
//                             out.print(responseBody);
//                         }
//                     case "/tasks" -> {
//                             String responseBody = String.join("\n", tasks);
//                             out.print("HTTP/1.1 200 OK\r\n");
//                             out.print("Content-Type: text/plain\r\n");
//                             out.print("Content-Length: " + responseBody.length() + "\r\n");
//                             out.print("\r\n");
//                             out.print(responseBody);
//                         }
//                     default -> {
//                         String responseBody = "Erreur 404 !";
//                         out.print("HTTP/1.1 404 Not Found\r\n");
//                         out.print("Content-Type: text/plain\r\n");
//                         out.print("Content-Length: " + responseBody.length() + "\r\n");
//                         out.print("\r\n");
//                         out.print(responseBody);
//                     }
//                 }

//                 out.flush();
//                 clientSocket.close();
//             }
//         } catch (IOException e) {
//             System.err.println("Erreur serveur : " + e.getMessage());
//         }
//     }
// }

package org.yourcompany.yourproject.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private final int port;
    private final List<String> users;
    private final List<String> tasks;

    public Server(int port) {
        this.port = port;
        this.users = new ArrayList<>();
        this.tasks = new ArrayList<>();
        // Init de base
        users.add("Mel");
        users.add("Greg");
        users.add("Laura");
        tasks.add("Cours de Java");
        tasks.add("Rendre TP serveur");
        tasks.add("Faire les couillions !");
    }

    public void start() {
        System.out.println("Serveur démarré sur le port " + port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream(), "UTF-8")
                );

                String line = in.readLine();
                String url = "/";
                if (line != null && !line.isEmpty()) {
                    System.out.println("Requête brute : " + line);
                    String[] parts = line.split(" ");
                    if (parts.length >= 2) {
                        url = parts[1];
                    }
                }

                
                while ((line = in.readLine()) != null && !line.isEmpty()) {}

                PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
                String hello = "<html><body><h1>Hello world!</h1></body></html>";

                switch (url) {
                    case "/","" -> {
                        out.print("HTTP/1.1 200 OK\r\n");
                        out.print("Content-Type: text/html\r\n");
                        out.print("Content-Length: " + hello.length() + "\r\n");
                        out.print("\r\n");
                        out.print(hello);
                        break;
                        }
                    case "/users" -> {
                            String responseBody = String.join("\n", users);
                            out.print("HTTP/1.1 200 OK\r\n");
                            out.print("Content-Type: text/plain\r\n");
                            out.print("Content-Length: " + responseBody.length() + "\r\n");
                            out.print("\r\n");
                            out.print(responseBody);
                        }
                    case "/tasks" -> {
                            String responseBody = String.join("\n", tasks);
                            out.print("HTTP/1.1 200 OK\r\n");
                            out.print("Content-Type: text/plain\r\n");
                            out.print("Content-Length: " + responseBody.length() + "\r\n");
                            out.print("\r\n");
                            out.print(responseBody);
                        }
                    default -> {
                        String responseBody = "Erreur 404 !";
                        out.print("HTTP/1.1 404 Not Found\r\n");
                        out.print("Content-Type: text/plain\r\n");
                        out.print("Content-Length: " + responseBody.length() + "\r\n");
                        out.print("\r\n");
                        out.print(responseBody);
                    }
                }

                out.flush();
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Erreur serveur : " + e.getMessage());
        }
    }
}

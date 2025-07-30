/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package org.yourcompany.yourproject;

import org.yourcompany.yourproject.server.Server;

/**
 *
 * @author melanie
 */
public class Tp_java {
    public static void main(String[] args) {

        Server server = new Server(8080);
        server.start();

        // int port = 8080;
        // System.out.println("Serveur démarré sur le port " + port);

        // try (ServerSocket serverSocket = new ServerSocket(port)) {
        //     while (true) {
                
        //         Socket clientSocket = serverSocket.accept();
        //         System.out.println("Nouvelle connexion : " + clientSocket.getInetAddress());

        //         BufferedReader in = new BufferedReader(
        //             new InputStreamReader(clientSocket.getInputStream(), "UTF-8")
        //         );

        //         String line;
        //         StringBuilder request = new StringBuilder();
        //         while ((line = in.readLine()) != null && !line.isEmpty()) {
        //             request.append(line).append("\n");
        //         }
        //         System.out.println("Requête reçue :\n" + request);

        //         PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
        //         out.print("HTTP/1.1 200 OK\r\n");
        //         out.print("Content-Type: text/html\r\n");
        //         out.print("Content-Length: 12\r\n");
        //         out.print("\r\n");
        //         out.print("<html><body><h1>Hello world!</h1></body></html>");

        //         out.flush();

        //         // Ferme la connexion
        //         clientSocket.close();
        //     }
        // } catch (IOException e) {
        //     System.err.println("Erreur serveur : " + e.getMessage());
        // }

        // DatabaseAccess db = DatabaseAccess.getInstance();

        // new DatabaseSeeder().seed();

        // System.out.println("Utilisateurs :");
        // for (User u : db.getUsers()) {
        //     System.out.println(u.getId() + " - " + u.getFirstName());
        // }
        // System.out.println("Tâches :");
        // for (Task t : db.getTasks()) {
        //     System.out.println(t.getId() + " - " + t.getTitle());
        // }

        // System.out.println("Description :");
        // for (Task t : db.getTasks()) {
        //     System.out.println(t.getId() + " - " + t.getDescription());
        // }
        // System.out.println("What is your name ?");
        // Scanner scanner = new Scanner(System.in);
        // String name = scanner.nextLine();
        // User user = new User(name);
        // System.out.println("Hello" + user.getFirstName());

        // List<Task> tasks = new ArrayList<>();

        // for (int i=0; i <10; i++) {
        //     Task task = new Task("task_"+i, "Lorem ipsum", user);
        //     tasks.add(task);
        // }

        // for (int i=0; i <5; i++) {
        //     DatedTask task = new DatedTask("task_"+i, "Lorem ipsum", user, new Date());
        //     tasks.add(task);
        // }

        // System.out.println("here is your list of task");

        // scanner.close();

    }

    
}

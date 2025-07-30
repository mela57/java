/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package org.yourcompany.yourproject;

import org.yourcompany.yourproject.models.Task;
import org.yourcompany.yourproject.models.User;
import org.yourcompany.yourproject.services.DatabaseAccess;
import org.yourcompany.yourproject.services.DatabaseSeeder;

/**
 *
 * @author melanie
 */
public class Tp_java {
    public static void main(String[] args) {

        DatabaseAccess db = DatabaseAccess.getInstance();

        new DatabaseSeeder().seed();

        System.out.println("Utilisateurs :");
        for (User u : db.getUsers()) {
            System.out.println(u.getId() + " - " + u.getFirstName());
        }
        System.out.println("Tâches :");
        for (Task t : db.getTasks()) {
            System.out.println(t.getId() + " - " + t.getTitle());
        }

        System.out.println("Description :");
        for (Task t : db.getTasks()) {
            System.out.println(t.getId() + " - " + t.getDescription());
        }
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

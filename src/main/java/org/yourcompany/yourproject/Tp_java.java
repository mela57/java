/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package org.yourcompany.yourproject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.yourcompany.yourproject.models.DatedTask;
import org.yourcompany.yourproject.models.Task;
import org.yourcompany.yourproject.models.User;

/**
 *
 * @author melanie
 */
public class Tp_java {
    public static void main(String[] args) {
        System.out.println("What is your name ?");
        Scanner scanner = new Scanner(System.in);
        String name = scanner.nextLine();
        User user = new User(name);
        System.out.println("Hello" + user.getFirstName());

        List<Task> tasks = new ArrayList<>();

        for (int i=0; i <10; i++) {
            Task task = new Task("task_"+i, "Lorem ipsum", user);
            tasks.add(task);
        }

        for (int i=0; i <5; i++) {
            DatedTask task = new DatedTask("task_"+i, "Lorem ipsum", user, new Date());
            tasks.add(task);
        }

        System.out.println("here is your list of task");

        scanner.close();
        // User u = new User("Mel");
        // Task t = new Task("Apprendre Java", "Faire les exercices", u);
        // DatedTask dt = new DatedTask("Rendre TP", "TP Java à rendre", u, java.time.LocalDate.of(2025, 8, 1));

        // System.out.println("User: " + u.getFirstName() + " (" + u.getId() + ")");
        // System.out.println("Task: " + t.getTitle() + ", done? " + t.isDone());
        // System.out.println("DatedTask: " + dt.getTitle() + ", Due: " + dt.getDueDate());
    }
    // public static void main(String[] args) {
    //     System.out.println("What is your name ?");
    //     Scanner scanner = new Scanner(System.in);
    //     String name = scanner.nextLine();
    //     System.out.println("Hello" + name);
    //     // String[] tasks = new String[4];
    //     // tasks[0] = "Test0";
    //     // tasks[1] = "Test1";
    //     // tasks[2] = "Test2";
    //     // tasks[3] = "Test3";

    //     // for (int i = 0; i < tasks.length i++){
    //     //     System.out.println(tasks[i]);
    //     // }

    //     String[] tasks2 = new String[]{ "test", "test1", "test2", "test3"};
        
    //     for (String task: tasks2){
    //         System.out.println(task);
    //     }
    //     String[] tasks3 = new String[tasks2.length+1];
    //     System.arraycopy(tasks2, 0, tasks3, 0, tasks2.length);
    //     System.out.println("Ajouter une tache");
    //     String task = scanner.nextLine();
    //     tasks3[tasks3.length-1] = task;
    //     for (String t:tasks3){
    //         System.out.println(t);
    //     }
    //     scanner.close();
    // }
    
}

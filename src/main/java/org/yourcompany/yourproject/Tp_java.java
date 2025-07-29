/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package org.yourcompany.yourproject;

import java.util.Scanner;

/**
 *
 * @author melanie
 */
public class Tp_java {

    public static void main(String[] args) {
        System.out.println("What is your name ?");
        Scanner scanner = new Scanner(System.in);
        String name = scanner.nextLine();
        System.out.println("Hello" + name);
        // String[] tasks = new String[4];
        // tasks[0] = "Test0";
        // tasks[1] = "Test1";
        // tasks[2] = "Test2";
        // tasks[3] = "Test3";

        // for (int i = 0; i < tasks.length i++){
        //     System.out.println(tasks[i]);
        // }

        String[] tasks2 = new String[]{ "test", "test1", "test2", "test3"};
        
        for (String task: tasks2){
            System.out.println(task);
        }
        String[] tasks3 = new String[tasks2.length+1];
        System.arraycopy(tasks2, 0, tasks3, 0, tasks2.length);
        System.out.println("Ajouter une tache");
        String task = scanner.nextLine();
        tasks3[tasks3.length-1] = task;
        for (String t:tasks3){
            System.out.println(t);
        }
        scanner.close();
    }
    
}

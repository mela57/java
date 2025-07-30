package org.yourcompany.yourproject.models;

public class User {
    private static int counter;
    private final int id;
    private String firstName;

    static {
        counter = 1;
    }

    {
        id = counter++;
    }

    public User(String firstName) {
        this.firstName = firstName;
    }

    public int getId() { return id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
}
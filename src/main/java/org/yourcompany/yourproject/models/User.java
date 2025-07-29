package org.yourcompany.yourproject.models;
import java.util.UUID;

public class User {
    
    private final String id;
    private String firstName;

    public User(String firstName) {
        this.id = UUID.randomUUID().toString();
        this.firstName = firstName;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
}
package org.yourcompany.yourproject.models;
import java.util.Date;

public class DatedTask extends Task {
    private Date dueDate;

    public DatedTask(String title, String description, User user, Date dueDate) {
        super(title, description, user);
        this.dueDate = dueDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }
}


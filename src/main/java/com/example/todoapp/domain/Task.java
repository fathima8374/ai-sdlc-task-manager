package com.example.todoapp.domain;

import org.springframework.format.annotation.DateTimeFormat;
import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.Date;

@Entity
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String description;

    @Column
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;

    @Column
    @Enumerated(EnumType.STRING)
    private Priority priority;

    public Task(){ }

    public Task(int id, String description, Date date) {
        this.id = id;
        this.description = description;
        this.date = date;
    }

    public Task(int id, String description, Date date, Priority priority) {
        this.id = id;
        this.description = description;
        this.date = date;
        this.priority = priority;
    }

    public Task(String description, Date date) {
        this.description = description;
        this.date = date;
    }

    public Task(String description, Date date, Priority priority) {
        this.description = description;
        this.date = date;
        this.priority = priority;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String details) {
        this.description = details;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }
}

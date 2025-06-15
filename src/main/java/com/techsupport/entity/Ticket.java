package com.techsupport.entity;

import jakarta.persistence.*;

/**
 * Minimal Ticket entity for Week 1 smoke testing.
 * This is a placeholder to make the TicketRepository functional.
 * Will be fully implemented with all fields and relationships in Week 2.
 */
@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    // Default constructor (required by JPA)
    public Ticket() {}

    // Constructor for testing
    public Ticket(String title) {
        this.title = title;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "id=" + id +
                ", title='" + title + '\'' +
                '}';
    }
} 
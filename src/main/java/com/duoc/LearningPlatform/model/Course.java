package com.duoc.LearningPlatform.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(name = "professor_id", nullable = false)
    private Long professorId;

    @Column(nullable = false)
    private boolean active;

    protected Course() {}

    public Course(String title, String description, Long professorId) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Course title cannot be empty");
        }
        if (professorId == null) {
            throw new IllegalArgumentException("Professor ID cannot be null");
        }
        this.title = title;
        this.description = description;
        this.professorId = professorId;
        this.active = true;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public void updateDetails(String title, String description, Long professorId) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Course title cannot be empty");
        }
        if (professorId == null) {
            throw new IllegalArgumentException("Professor ID cannot be null");
        }
        this.title = title;
        this.description = description;
        this.professorId = professorId;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Long getProfessorId() {
        return professorId;
    }

    public boolean isActive() {
        return active;
    }
}

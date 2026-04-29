package com.duoc.LearningPlatform.dto;

import com.duoc.LearningPlatform.model.Course;

public class CourseResponse {

    private Long id;
    private String title;
    private String description;
    private Long professorId;
    private boolean active;

    public static CourseResponse fromEntity(Course course) {
        CourseResponse response = new CourseResponse();
        response.id = course.getId();
        response.title = course.getTitle();
        response.description = course.getDescription();
        response.professorId = course.getProfessorId();
        response.active = course.isActive();
        return response;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getProfessorId() {
        return professorId;
    }

    public void setProfessorId(Long professorId) {
        this.professorId = professorId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

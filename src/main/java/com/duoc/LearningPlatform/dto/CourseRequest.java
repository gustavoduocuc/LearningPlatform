package com.duoc.LearningPlatform.dto;

import jakarta.validation.constraints.NotBlank;

public class CourseRequest {

    @NotBlank(message = "title is required")
    private String title;

    private String description;

    @NotBlank(message = "instructor is required")
    private String instructor;

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

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }
}

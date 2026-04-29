package com.duoc.LearningPlatform.dto;

import com.duoc.LearningPlatform.model.Registration;

import java.time.LocalDateTime;

public class RegistrationResponse {

    private Long id;
    private Long courseId;
    private Long studentId;
    private LocalDateTime registrationDate;
    private String courseTitle;
    private String studentName;

    public static RegistrationResponse fromEntity(Registration registration) {
        RegistrationResponse response = new RegistrationResponse();
        response.id = registration.getId();
        response.courseId = registration.getCourseId();
        response.studentId = registration.getStudentId();
        response.registrationDate = registration.getRegistrationDate();
        return response;
    }

    public static RegistrationResponse fromEntity(Registration registration, String courseTitle, String studentName) {
        RegistrationResponse response = fromEntity(registration);
        response.courseTitle = courseTitle;
        response.studentName = studentName;
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
}

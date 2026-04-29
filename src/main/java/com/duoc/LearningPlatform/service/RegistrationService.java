package com.duoc.LearningPlatform.service;

import com.duoc.LearningPlatform.model.Course;
import com.duoc.LearningPlatform.model.Registration;
import com.duoc.LearningPlatform.model.Role;
import com.duoc.LearningPlatform.model.User;
import com.duoc.LearningPlatform.repository.CourseRepository;
import com.duoc.LearningPlatform.repository.RegistrationRepository;
import com.duoc.LearningPlatform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public RegistrationService(RegistrationRepository registrationRepository,
                               UserRepository userRepository,
                               CourseRepository courseRepository) {
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public Registration registerStudent(Long courseId, Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));

        if (student.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("User with id " + studentId + " is not a student");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + courseId));

        if (registrationRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
            throw new IllegalStateException("Student is already registered for this course");
        }

        Registration registration = new Registration(courseId, studentId);
        return registrationRepository.save(registration);
    }

    @Transactional
    public void unregisterStudent(Long registrationId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found with id: " + registrationId));

        registrationRepository.deleteById(registrationId);
    }

    public List<Registration> findByCourseId(Long courseId) {
        return registrationRepository.findByCourseId(courseId);
    }

    public List<Registration> findByStudentId(Long studentId) {
        return registrationRepository.findByStudentId(studentId);
    }

    public List<Registration> findAll() {
        return registrationRepository.findAll();
    }

    public boolean isRegistered(Long courseId, Long studentId) {
        return registrationRepository.existsByCourseIdAndStudentId(courseId, studentId);
    }

    public Registration findById(Long id) {
        return registrationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found with id: " + id));
    }
}

package com.duoc.LearningPlatform.service;

import com.duoc.LearningPlatform.exception.ResourceNotFoundException;
import com.duoc.LearningPlatform.model.Course;
import com.duoc.LearningPlatform.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public List<Course> findActiveCourses() {
        List<Course> allCourses = courseRepository.findAll();
        ArrayList<Course> activeCourses = new ArrayList<>();
        for (Course course : allCourses) {
            if (course.isActive()) {
                activeCourses.add(course);
            }
        }
        activeCourses.sort(Comparator.comparing(Course::getTitle));
        return activeCourses;
    }

    public List<Course> findAllCourses() {
        return courseRepository.findAll();
    }

    public Course getCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
    }

    @Transactional
    public Course createCourse(String title, String description, String instructor) {
        Course course = new Course(title, description, instructor);
        return courseRepository.save(course);
    }

    @Transactional
    public Course updateCourse(Long id, String title, String description, String instructor) {
        Course course = getCourse(id);
        course.updateDetails(title, description, instructor);
        return courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course", id);
        }
        courseRepository.deleteById(id);
    }

    @Transactional
    public Course activateCourse(Long id) {
        Course course = getCourse(id);
        course.activate();
        return courseRepository.save(course);
    }

    @Transactional
    public Course deactivateCourse(Long id) {
        Course course = getCourse(id);
        course.deactivate();
        return courseRepository.save(course);
    }
}

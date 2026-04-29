package com.duoc.LearningPlatform.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CourseTest {

    @Test
    void createsCourseWithValidData() {
        Course course = new Course("Java 101", "Introduction to Java", 1L);

        assertEquals("Java 101", course.getTitle());
        assertEquals("Introduction to Java", course.getDescription());
        assertEquals(1L, course.getProfessorId());
        assertTrue(course.isActive());
    }

    @Test
    void rejectsNullTitle() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Course(null, "Description", 1L);
        });
    }

    @Test
    void rejectsBlankTitle() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Course("   ", "Description", 1L);
        });
    }

    @Test
    void rejectsNullProfessorId() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Course("Java 101", "Description", null);
        });
    }

    @Test
    void activatesCourse() {
        Course course = new Course("Java 101", "Description", 1L);
        course.deactivate();

        course.activate();

        assertTrue(course.isActive());
    }

    @Test
    void deactivatesCourse() {
        Course course = new Course("Java 101", "Description", 1L);

        course.deactivate();

        assertFalse(course.isActive());
    }

    @Test
    void updatesDetailsSuccessfully() {
        Course course = new Course("Java 101", "Description", 1L);

        course.updateDetails("Advanced Java", "Advanced concepts", 2L);

        assertEquals("Advanced Java", course.getTitle());
        assertEquals("Advanced concepts", course.getDescription());
        assertEquals(2L, course.getProfessorId());
    }

    @Test
    void rejectsNullTitleWhenUpdating() {
        Course course = new Course("Java 101", "Description", 1L);

        assertThrows(IllegalArgumentException.class, () -> {
            course.updateDetails(null, "Description", 1L);
        });
    }

    @Test
    void rejectsNullProfessorIdWhenUpdating() {
        Course course = new Course("Java 101", "Description", 1L);

        assertThrows(IllegalArgumentException.class, () -> {
            course.updateDetails("Java 101", "Description", null);
        });
    }

    @Test
    void allowsNullDescription() {
        Course course = new Course("Java 101", null, 1L);

        assertNull(course.getDescription());
    }

    @Test
    void allowsNullDescriptionWhenUpdating() {
        Course course = new Course("Java 101", "Description", 1L);

        course.updateDetails("Advanced Java", null, 2L);

        assertNull(course.getDescription());
    }
}

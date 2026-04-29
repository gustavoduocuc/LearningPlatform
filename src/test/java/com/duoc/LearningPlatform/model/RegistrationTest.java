package com.duoc.LearningPlatform.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationTest {

    @Test
    void createsRegistrationWithValidData() {
        Registration registration = new Registration(1L, 2L);

        assertNull(registration.getId());
        assertEquals(1L, registration.getCourseId());
        assertEquals(2L, registration.getStudentId());
        assertNull(registration.getRegistrationDate());
    }

    @Test
    void rejectsNullCourseId() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Registration(null, 2L);
        });
    }

    @Test
    void rejectsNullStudentId() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Registration(1L, null);
        });
    }

    @Test
    void validatesCourseIdIsPositive() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Registration(0L, 2L);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Registration(-1L, 2L);
        });
    }

    @Test
    void validatesStudentIdIsPositive() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Registration(1L, 0L);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Registration(1L, -1L);
        });
    }

    @Test
    void setsRegistrationDateOnCreation() {
        Registration registration = new Registration(1L, 2L);
        
        registration.onCreate();
        
        assertNotNull(registration.getRegistrationDate());
        assertTrue(registration.getRegistrationDate().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(registration.getRegistrationDate().isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    @Test
    void twoRegistrationsWithSameCourseAndStudentAreEqual() {
        Registration reg1 = new Registration(1L, 2L);
        Registration reg2 = new Registration(1L, 2L);

        assertEquals(reg1, reg2);
        assertEquals(reg1.hashCode(), reg2.hashCode());
    }

    @Test
    void twoRegistrationsWithDifferentCourseAreNotEqual() {
        Registration reg1 = new Registration(1L, 2L);
        Registration reg2 = new Registration(2L, 2L);

        assertNotEquals(reg1, reg2);
    }

    @Test
    void twoRegistrationsWithDifferentStudentAreNotEqual() {
        Registration reg1 = new Registration(1L, 2L);
        Registration reg2 = new Registration(1L, 3L);

        assertNotEquals(reg1, reg2);
    }
}

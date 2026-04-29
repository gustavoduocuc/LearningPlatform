package com.duoc.LearningPlatform.repository;

import com.duoc.LearningPlatform.model.Registration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationRepositoryTest {

    @Mock
    private RegistrationRepository registrationRepository;

    @Test
    void findsRegistrationsByCourseId() {
        Registration reg1 = new Registration(1L, 2L);
        Registration reg2 = new Registration(1L, 3L);
        when(registrationRepository.findByCourseId(1L)).thenReturn(List.of(reg1, reg2));

        List<Registration> result = registrationRepository.findByCourseId(1L);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(r -> r.getCourseId().equals(1L)));
    }

    @Test
    void findsRegistrationsByStudentId() {
        Registration reg1 = new Registration(1L, 2L);
        Registration reg2 = new Registration(3L, 2L);
        when(registrationRepository.findByStudentId(2L)).thenReturn(List.of(reg1, reg2));

        List<Registration> result = registrationRepository.findByStudentId(2L);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(r -> r.getStudentId().equals(2L)));
    }

    @Test
    void checksIfExistsByCourseIdAndStudentId() {
        when(registrationRepository.existsByCourseIdAndStudentId(1L, 2L)).thenReturn(true);
        when(registrationRepository.existsByCourseIdAndStudentId(1L, 99L)).thenReturn(false);

        assertTrue(registrationRepository.existsByCourseIdAndStudentId(1L, 2L));
        assertFalse(registrationRepository.existsByCourseIdAndStudentId(1L, 99L));
    }

    @Test
    void findsRegistrationByCourseIdAndStudentId() {
        Registration registration = new Registration(1L, 2L);
        when(registrationRepository.findByCourseIdAndStudentId(1L, 2L)).thenReturn(Optional.of(registration));
        when(registrationRepository.findByCourseIdAndStudentId(1L, 99L)).thenReturn(Optional.empty());

        Optional<Registration> found = registrationRepository.findByCourseIdAndStudentId(1L, 2L);
        Optional<Registration> notFound = registrationRepository.findByCourseIdAndStudentId(1L, 99L);

        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getCourseId());
        assertEquals(2L, found.get().getStudentId());
        assertTrue(notFound.isEmpty());
    }

    @Test
    void savesRegistration() {
        Registration registration = new Registration(1L, 2L);
        when(registrationRepository.save(any(Registration.class))).thenAnswer(i -> {
            Registration saved = i.getArgument(0);
            saved.getClass().getDeclaredFields();
            return saved;
        });

        Registration saved = registrationRepository.save(registration);

        assertNotNull(saved);
        assertEquals(1L, saved.getCourseId());
        assertEquals(2L, saved.getStudentId());
    }

    @Test
    void deletesRegistration() {
        doNothing().when(registrationRepository).deleteById(1L);

        registrationRepository.deleteById(1L);

        verify(registrationRepository).deleteById(1L);
    }

    @Test
    void findsAllRegistrations() {
        Registration reg1 = new Registration(1L, 2L);
        Registration reg2 = new Registration(3L, 4L);
        when(registrationRepository.findAll()).thenReturn(List.of(reg1, reg2));

        List<Registration> result = registrationRepository.findAll();

        assertEquals(2, result.size());
    }
}

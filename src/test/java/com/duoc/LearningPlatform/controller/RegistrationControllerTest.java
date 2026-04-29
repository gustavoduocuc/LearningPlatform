package com.duoc.LearningPlatform.controller;

import com.duoc.LearningPlatform.model.Registration;
import com.duoc.LearningPlatform.service.RegistrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationControllerTest {

    @Mock
    private RegistrationService registrationService;

    @InjectMocks
    private RegistrationController registrationController;

    @Test
    void listsRegistrationsByCourseId() {
        Registration reg1 = new Registration(1L, 2L);
        Registration reg2 = new Registration(1L, 3L);
        when(registrationService.findByCourseId(1L)).thenReturn(List.of(reg1, reg2));

        ResponseEntity<List<com.duoc.LearningPlatform.dto.RegistrationResponse>> response = 
                registrationController.listRegistrations(1L, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void listsRegistrationsByStudentId() {
        Registration reg1 = new Registration(1L, 2L);
        Registration reg2 = new Registration(3L, 2L);
        when(registrationService.findByStudentId(2L)).thenReturn(List.of(reg1, reg2));

        ResponseEntity<List<com.duoc.LearningPlatform.dto.RegistrationResponse>> response = 
                registrationController.listRegistrations(null, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void listsAllRegistrationsWhenNoFilter() {
        Registration reg1 = new Registration(1L, 2L);
        Registration reg2 = new Registration(3L, 4L);
        when(registrationService.findAll()).thenReturn(List.of(reg1, reg2));

        ResponseEntity<List<com.duoc.LearningPlatform.dto.RegistrationResponse>> response = 
                registrationController.listRegistrations(null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void createsRegistrationSuccessfully() {
        Registration registration = new Registration(1L, 2L);
        registration.onCreate();
        when(registrationService.registerStudent(eq(1L), eq(2L))).thenReturn(registration);

        com.duoc.LearningPlatform.dto.RegistrationRequest request = new com.duoc.LearningPlatform.dto.RegistrationRequest();
        request.setCourseId(1L);
        request.setStudentId(2L);

        ResponseEntity<com.duoc.LearningPlatform.dto.RegistrationResponse> response = 
                registrationController.createRegistration(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getCourseId());
        assertEquals(2L, response.getBody().getStudentId());
    }

    @Test
    void deletesRegistrationSuccessfully() {
        doNothing().when(registrationService).unregisterStudent(1L);

        ResponseEntity<Void> response = registrationController.deleteRegistration(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(registrationService).unregisterStudent(1L);
    }

    @Test
    void getsRegistrationById() {
        Registration registration = new Registration(1L, 2L);
        registration.onCreate();
        when(registrationService.findById(1L)).thenReturn(registration);

        ResponseEntity<com.duoc.LearningPlatform.dto.RegistrationResponse> response = 
                registrationController.getRegistration(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getCourseId());
    }
}

package com.duoc.LearningPlatform.service;

import com.duoc.LearningPlatform.model.Course;
import com.duoc.LearningPlatform.model.Evaluation;
import com.duoc.LearningPlatform.model.Role;
import com.duoc.LearningPlatform.model.StudentEvaluation;
import com.duoc.LearningPlatform.model.StudentSubmission;
import com.duoc.LearningPlatform.model.User;
import com.duoc.LearningPlatform.repository.CourseRepository;
import com.duoc.LearningPlatform.repository.EvaluationRepository;
import com.duoc.LearningPlatform.repository.StudentEvaluationRepository;
import com.duoc.LearningPlatform.repository.StudentSubmissionRepository;
import com.duoc.LearningPlatform.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock
    private StudentSubmissionRepository submissionRepository;

    @Mock
    private EvaluationRepository evaluationRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RegistrationService registrationService;

    @Mock
    private StudentEvaluationRepository studentEvaluationRepository;

    @InjectMocks
    private SubmissionService submissionService;

    @Test
    void allowsRegisteredStudentToSubmitPdf() {
        Long studentId = 7L;
        Long evaluationId = 1L;
        Long courseId = 1L;
        String description = "My assignment submission";
        String fileName = "assignment.pdf";
        String contentType = "application/pdf";
        byte[] fileContent = createPdfContent();

        User student = createStudent();
        Evaluation evaluation = createEvaluation(courseId, "Midterm");
        Course course = createCourse(2L);
        StudentSubmission savedSubmission = new StudentSubmission(evaluationId, studentId, description, fileName, contentType, fileContent);

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(evaluationRepository.findById(evaluationId)).thenReturn(Optional.of(evaluation));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(registrationService.isRegistered(courseId, studentId)).thenReturn(true);
        when(submissionRepository.existsByStudentIdAndEvaluationId(studentId, evaluationId)).thenReturn(false);
        when(submissionRepository.save(any(StudentSubmission.class))).thenReturn(savedSubmission);

        StudentSubmission result = submissionService.submitAssignment(studentId, evaluationId, description, fileName, contentType, fileContent);

        assertNotNull(result);
        assertEquals(studentId, result.getStudentId());
        assertEquals(evaluationId, result.getEvaluationId());
        assertEquals(description, result.getDescription());
        assertEquals(fileName, result.getFileName());
        assertEquals(contentType, result.getContentType());
        verify(submissionRepository).save(any(StudentSubmission.class));
    }

    @Test
    void allowsRegisteredStudentToSubmitZip() {
        Long studentId = 7L;
        Long evaluationId = 1L;
        Long courseId = 1L;
        String description = "My project files";
        String fileName = "project.zip";
        String contentType = "application/zip";
        byte[] fileContent = createZipContent();

        User student = createStudent();
        Evaluation evaluation = createEvaluation(courseId, "Project");
        Course course = createCourse(2L);
        StudentSubmission savedSubmission = new StudentSubmission(evaluationId, studentId, description, fileName, contentType, fileContent);

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(evaluationRepository.findById(evaluationId)).thenReturn(Optional.of(evaluation));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(registrationService.isRegistered(courseId, studentId)).thenReturn(true);
        when(submissionRepository.existsByStudentIdAndEvaluationId(studentId, evaluationId)).thenReturn(false);
        when(submissionRepository.save(any(StudentSubmission.class))).thenReturn(savedSubmission);

        StudentSubmission result = submissionService.submitAssignment(studentId, evaluationId, description, fileName, contentType, fileContent);

        assertNotNull(result);
        assertEquals("application/zip", result.getContentType());
    }

    @Test
    void allowsRegisteredStudentToSubmitDocx() {
        Long studentId = 7L;
        Long evaluationId = 1L;
        Long courseId = 1L;
        String description = "My essay";
        String fileName = "essay.docx";
        String contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        byte[] fileContent = createDocxContent();

        User student = createStudent();
        Evaluation evaluation = createEvaluation(courseId, "Essay");
        Course course = createCourse(2L);
        StudentSubmission savedSubmission = new StudentSubmission(evaluationId, studentId, description, fileName, contentType, fileContent);

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(evaluationRepository.findById(evaluationId)).thenReturn(Optional.of(evaluation));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(registrationService.isRegistered(courseId, studentId)).thenReturn(true);
        when(submissionRepository.existsByStudentIdAndEvaluationId(studentId, evaluationId)).thenReturn(false);
        when(submissionRepository.save(any(StudentSubmission.class))).thenReturn(savedSubmission);

        StudentSubmission result = submissionService.submitAssignment(studentId, evaluationId, description, fileName, contentType, fileContent);

        assertNotNull(result);
    }

    @Test
    void allowsProfessorToListSubmissionsForTheirCourse() {
        Long evaluationId = 1L;
        Long courseId = 1L;
        Long professorId = 2L;
        Long studentId1 = 7L;
        Long studentId2 = 8L;

        User professor = createProfessor();
        Evaluation evaluation = createEvaluation(courseId, "Midterm");
        Course course = new Course("Java 101", "Description", professorId);

        StudentSubmission submission1 = new StudentSubmission(evaluationId, studentId1, "Submission 1", "file1.pdf", "application/pdf", new byte[]{1});
        StudentSubmission submission2 = new StudentSubmission(evaluationId, studentId2, "Submission 2", "file2.pdf", "application/pdf", new byte[]{2});

        when(userRepository.findById(professorId)).thenReturn(Optional.of(professor));
        when(evaluationRepository.findById(evaluationId)).thenReturn(Optional.of(evaluation));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(submissionRepository.findByEvaluationId(evaluationId)).thenReturn(List.of(submission1, submission2));

        List<StudentSubmission> result = submissionService.findSubmissionsByEvaluationId(evaluationId, professorId);

        assertEquals(2, result.size());
    }

    @Test
    void rejectsSubmissionWhenStudentNotRegistered() {
        Long studentId = 7L;
        Long evaluationId = 1L;
        Long courseId = 1L;
        String description = "My assignment";
        String fileName = "assignment.pdf";
        String contentType = "application/pdf";
        byte[] fileContent = createSampleContent();

        User student = createStudent();
        Course course = createCourse(2L);
        Evaluation evaluation = createEvaluation(courseId, "Midterm");

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(evaluationRepository.findById(evaluationId)).thenReturn(Optional.of(evaluation));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(registrationService.isRegistered(courseId, studentId)).thenReturn(false);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            submissionService.submitAssignment(studentId, evaluationId, description, fileName, contentType, fileContent);
        });

        assertEquals("Student is not registered for this course", exception.getMessage());
        verify(submissionRepository, never()).save(any());
    }

    @Test
    void rejectsSubmissionWithInvalidFileType() {
        Long studentId = 7L;
        Long evaluationId = 1L;
        String description = "My assignment";
        String fileName = "assignment.exe";
        String contentType = "application/x-msdownload";
        byte[] fileContent = createSampleContent();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            submissionService.submitAssignment(studentId, evaluationId, description, fileName, contentType, fileContent);
        });

        assertEquals("Invalid file type. Allowed types: PDF, DOC, DOCX, ZIP", exception.getMessage());
        verifyNoInteractions(userRepository, evaluationRepository, submissionRepository);
    }

    @Test
    void rejectsDuplicateSubmission() {
        Long studentId = 7L;
        Long evaluationId = 1L;
        Long courseId = 1L;
        String description = "My assignment";
        String fileName = "assignment.pdf";
        String contentType = "application/pdf";
        byte[] fileContent = createSampleContent();

        User student = createStudent();
        Course course = createCourse(2L);
        Evaluation evaluation = createEvaluation(courseId, "Midterm");

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(evaluationRepository.findById(evaluationId)).thenReturn(Optional.of(evaluation));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(registrationService.isRegistered(courseId, studentId)).thenReturn(true);
        when(submissionRepository.existsByStudentIdAndEvaluationId(studentId, evaluationId)).thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            submissionService.submitAssignment(studentId, evaluationId, description, fileName, contentType, fileContent);
        });

        assertEquals("Student already submitted for this evaluation", exception.getMessage());
        verify(submissionRepository, never()).save(any());
    }

    @Test
    void preventsProfessorFromViewingOtherCoursesSubmissions() {
        Long evaluationId = 1L;
        Long courseId = 1L;
        Long professorId = 99L;
        Long actualProfessorId = 2L;

        User professor = createProfessor();
        Evaluation evaluation = createEvaluation(courseId, "Midterm");
        Course course = new Course("Java 101", "Description", actualProfessorId);

        when(userRepository.findById(professorId)).thenReturn(Optional.of(professor));
        when(evaluationRepository.findById(evaluationId)).thenReturn(Optional.of(evaluation));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            submissionService.findSubmissionsByEvaluationId(evaluationId, professorId);
        });

        assertEquals("Professor can only view submissions for their own courses", exception.getMessage());
    }

    @Test
    void rejectsSubmissionWhenEvaluationNotFound() {
        Long studentId = 7L;
        Long evaluationId = 99L;
        String description = "My assignment";
        String fileName = "assignment.pdf";
        String contentType = "application/pdf";
        byte[] fileContent = createSampleContent();

        User student = createStudent();

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(evaluationRepository.findById(evaluationId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            submissionService.submitAssignment(studentId, evaluationId, description, fileName, contentType, fileContent);
        });

        assertEquals("Evaluation not found with id: 99", exception.getMessage());
    }

    @Test
    void rejectsSubmissionWithEmptyFile() {
        Long studentId = 7L;
        Long evaluationId = 1L;
        String description = "My assignment";
        String fileName = "empty.pdf";
        String contentType = "application/pdf";
        byte[] fileContent = new byte[0];

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            submissionService.submitAssignment(studentId, evaluationId, description, fileName, contentType, fileContent);
        });

        assertEquals("File content cannot be empty", exception.getMessage());
        verifyNoInteractions(userRepository, evaluationRepository, submissionRepository);
    }

    @Test
    void rejectsSubmissionFromNonStudent() {
        Long professorId = 2L;
        Long evaluationId = 1L;
        String description = "My assignment";
        String fileName = "assignment.pdf";
        String contentType = "application/pdf";
        byte[] fileContent = createSampleContent();

        User professor = createProfessor();

        when(userRepository.findById(professorId)).thenReturn(Optional.of(professor));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            submissionService.submitAssignment(professorId, evaluationId, description, fileName, contentType, fileContent);
        });

        assertEquals("User with id 2 is not a student", exception.getMessage());
    }

    @Test
    void allowsStudentToViewTheirGrade() {
        Long studentId = 7L;
        Long evaluationId = 1L;

        User student = createStudent();
        StudentEvaluation grade = new StudentEvaluation(studentId, evaluationId, 85);

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studentEvaluationRepository.findByStudentIdAndEvaluationId(studentId, evaluationId)).thenReturn(Optional.of(grade));

        StudentEvaluation result = submissionService.findStudentGradeForEvaluation(studentId, evaluationId);

        assertNotNull(result);
        assertEquals(85, result.getScore());
        assertEquals(studentId, result.getStudentId());
        assertEquals(evaluationId, result.getEvaluationId());
    }

    @Test
    void throwsWhenStudentViewsGradeForUnevaluatedSubmission() {
        Long studentId = 7L;
        Long evaluationId = 1L;

        User student = createStudent();

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studentEvaluationRepository.findByStudentIdAndEvaluationId(studentId, evaluationId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            submissionService.findStudentGradeForEvaluation(studentId, evaluationId);
        });

        assertEquals("No grade assigned for this evaluation", exception.getMessage());
    }

    private User createStudent() {
        return new User("Ana", "ana@duoc.cl", "password", Role.STUDENT);
    }

    private User createProfessor() {
        return new User("Prof", "prof@duoc.cl", "password", Role.PROFESSOR);
    }

    private Course createCourse(Long professorId) {
        return new Course("Java 101", "Description", professorId);
    }

    private Evaluation createEvaluation(Long courseId, String name) {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        return new Evaluation(courseId, name, 100, futureDate);
    }

    private byte[] createPdfContent() {
        return new byte[]{0x25, 0x50, 0x44, 0x46};
    }

    private byte[] createZipContent() {
        return new byte[]{0x50, 0x4B, 0x03, 0x04};
    }

    private byte[] createDocxContent() {
        return new byte[]{0x50, 0x4B};
    }

    private byte[] createSampleContent() {
        return new byte[]{1, 2, 3};
    }
}

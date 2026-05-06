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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubmissionService {

    private final StudentSubmissionRepository submissionRepository;
    private final EvaluationRepository evaluationRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final RegistrationService registrationService;
    private final StudentEvaluationRepository studentEvaluationRepository;

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/zip",
            "application/x-zip-compressed"
    );

    public SubmissionService(StudentSubmissionRepository submissionRepository,
                            EvaluationRepository evaluationRepository,
                            CourseRepository courseRepository,
                            UserRepository userRepository,
                            RegistrationService registrationService,
                            StudentEvaluationRepository studentEvaluationRepository) {
        this.submissionRepository = submissionRepository;
        this.evaluationRepository = evaluationRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.registrationService = registrationService;
        this.studentEvaluationRepository = studentEvaluationRepository;
    }

    @Transactional
    public StudentSubmission submitAssignment(Long studentId, Long evaluationId, String description,
                                               String fileName, String contentType, byte[] fileContent) {
        validateFileType(contentType);
        validateFileNotEmpty(fileContent);

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));

        if (student.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("User with id " + studentId + " is not a student");
        }

        Evaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new IllegalArgumentException("Evaluation not found with id: " + evaluationId));

        Long courseId = evaluation.getCourseId();
        courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + courseId));

        if (!registrationService.isRegistered(courseId, studentId)) {
            throw new IllegalStateException("Student is not registered for this course");
        }

        if (submissionRepository.existsByStudentIdAndEvaluationId(studentId, evaluationId)) {
            throw new IllegalStateException("Student already submitted for this evaluation");
        }

        StudentSubmission submission = new StudentSubmission(evaluationId, studentId, description, fileName, contentType, fileContent);
        return submissionRepository.save(submission);
    }

    private void validateFileType(String contentType) {
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Invalid file type. Allowed types: PDF, DOC, DOCX, ZIP");
        }
    }

    private void validateFileNotEmpty(byte[] fileContent) {
        if (fileContent == null || fileContent.length == 0) {
            throw new IllegalArgumentException("File content cannot be empty");
        }
    }

    public List<StudentSubmission> findSubmissionsByEvaluationId(Long evaluationId, Long requestingUserId) {
        User requestingUser = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + requestingUserId));

        Evaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new IllegalArgumentException("Evaluation not found with id: " + evaluationId));

        // Admin can view all
        if (requestingUser.getRole() == Role.ADMIN) {
            return submissionRepository.findByEvaluationId(evaluationId);
        }

        // Professor can only view their own course evaluations
        if (requestingUser.getRole() == Role.PROFESSOR) {
            Course course = courseRepository.findById(evaluation.getCourseId())
                    .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + evaluation.getCourseId()));

            if (!requestingUserId.equals(course.getProfessorId())) {
                throw new IllegalStateException("Professor can only view submissions for their own courses");
            }

            return submissionRepository.findByEvaluationId(evaluationId);
        }

        // Students cannot use this method (they have a separate endpoint)
        throw new IllegalStateException("Students cannot list all submissions for an evaluation");
    }

    public List<Evaluation> findEvaluationsForStudent(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));

        if (student.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("User with id " + studentId + " is not a student");
        }

        // Get all registrations for this student
        List<com.duoc.LearningPlatform.model.Registration> registrations = registrationService.findByStudentId(studentId);

        // Get evaluations for each registered course
        return registrations.stream()
                .flatMap(registration -> evaluationRepository.findByCourseId(registration.getCourseId()).stream())
                .distinct()
                .toList();
    }

    public StudentSubmission findOwnSubmissionForEvaluation(Long studentId, Long evaluationId) {
        return submissionRepository.findByStudentIdAndEvaluationId(studentId, evaluationId)
                .orElseThrow(() -> new IllegalArgumentException("No submission found for this evaluation"));
    }

    public StudentEvaluation findStudentGradeForEvaluation(Long studentId, Long evaluationId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));

        if (student.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("User with id " + studentId + " is not a student");
        }

        return studentEvaluationRepository.findByStudentIdAndEvaluationId(studentId, evaluationId)
                .orElseThrow(() -> new IllegalArgumentException("No grade assigned for this evaluation"));
    }
}

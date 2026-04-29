package com.duoc.LearningPlatform.service;

import com.duoc.LearningPlatform.model.Course;
import com.duoc.LearningPlatform.model.Evaluation;
import com.duoc.LearningPlatform.model.Role;
import com.duoc.LearningPlatform.model.StudentEvaluation;
import com.duoc.LearningPlatform.model.User;
import com.duoc.LearningPlatform.repository.CourseRepository;
import com.duoc.LearningPlatform.repository.EvaluationRepository;
import com.duoc.LearningPlatform.repository.StudentEvaluationRepository;
import com.duoc.LearningPlatform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final StudentEvaluationRepository studentEvaluationRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public EvaluationService(EvaluationRepository evaluationRepository,
                             StudentEvaluationRepository studentEvaluationRepository,
                             CourseRepository courseRepository,
                             UserRepository userRepository) {
        this.evaluationRepository = evaluationRepository;
        this.studentEvaluationRepository = studentEvaluationRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Evaluation createEvaluation(Long courseId, String name, int maximumScore, LocalDateTime applicationDate) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + courseId));

        Evaluation evaluation = new Evaluation(courseId, name, maximumScore, applicationDate);
        return evaluationRepository.save(evaluation);
    }

    @Transactional
    public Evaluation updateEvaluation(Long id, String name, int maximumScore, LocalDateTime applicationDate) {
        Evaluation evaluation = evaluationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evaluation not found with id: " + id));

        evaluation.updateDetails(name, maximumScore, applicationDate);
        return evaluationRepository.save(evaluation);
    }

    public List<Evaluation> findByCourseId(Long courseId) {
        return evaluationRepository.findByCourseId(courseId);
    }

    public List<Evaluation> findAll() {
        return evaluationRepository.findAll();
    }

    public Evaluation findById(Long id) {
        return evaluationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evaluation not found with id: " + id));
    }

    @Transactional
    public void deleteEvaluation(Long id) {
        Evaluation evaluation = evaluationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evaluation not found with id: " + id));

        evaluationRepository.deleteById(id);
    }

    @Transactional
    public StudentEvaluation assignGrade(Long evaluationId, Long studentId, int score) {
        Evaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new IllegalArgumentException("Evaluation not found with id: " + evaluationId));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));

        if (student.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("User with id " + studentId + " is not a student");
        }

        if (score > evaluation.getMaximumScore()) {
            throw new IllegalArgumentException("Score cannot exceed maximum score of " + evaluation.getMaximumScore());
        }

        if (studentEvaluationRepository.existsByStudentIdAndEvaluationId(studentId, evaluationId)) {
            throw new IllegalStateException("Grade already assigned to student for this evaluation");
        }

        StudentEvaluation grade = new StudentEvaluation(studentId, evaluationId, score);
        return studentEvaluationRepository.save(grade);
    }

    @Transactional
    public StudentEvaluation updateGrade(Long gradeId, int newScore) {
        StudentEvaluation grade = studentEvaluationRepository.findById(gradeId)
                .orElseThrow(() -> new IllegalArgumentException("Grade not found with id: " + gradeId));

        Evaluation evaluation = evaluationRepository.findById(grade.getEvaluationId())
                .orElseThrow(() -> new IllegalArgumentException("Evaluation not found with id: " + grade.getEvaluationId()));

        if (newScore > evaluation.getMaximumScore()) {
            throw new IllegalArgumentException("Score cannot exceed maximum score of " + evaluation.getMaximumScore());
        }

        grade.updateScore(newScore);
        return studentEvaluationRepository.save(grade);
    }

    public List<StudentEvaluation> findGradesByEvaluationId(Long evaluationId) {
        return studentEvaluationRepository.findByEvaluationId(evaluationId);
    }

    public List<StudentEvaluation> findGradesByStudentId(Long studentId) {
        return studentEvaluationRepository.findByStudentId(studentId);
    }

    public StudentEvaluation findGradeById(Long id) {
        return studentEvaluationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Grade not found with id: " + id));
    }

    @Transactional
    public void deleteGrade(Long id) {
        StudentEvaluation grade = studentEvaluationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Grade not found with id: " + id));

        studentEvaluationRepository.deleteById(id);
    }
}

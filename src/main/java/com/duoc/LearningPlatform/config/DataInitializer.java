package com.duoc.LearningPlatform.config;

import com.duoc.LearningPlatform.model.Course;
import com.duoc.LearningPlatform.model.Evaluation;
import com.duoc.LearningPlatform.model.Registration;
import com.duoc.LearningPlatform.model.Role;
import com.duoc.LearningPlatform.model.StudentEvaluation;
import com.duoc.LearningPlatform.model.User;
import com.duoc.LearningPlatform.repository.CourseRepository;
import com.duoc.LearningPlatform.repository.EvaluationRepository;
import com.duoc.LearningPlatform.repository.RegistrationRepository;
import com.duoc.LearningPlatform.repository.StudentEvaluationRepository;
import com.duoc.LearningPlatform.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@ConditionalOnProperty(name = "app.data.initializer.enabled", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final RegistrationRepository registrationRepository;
    private final EvaluationRepository evaluationRepository;
    private final StudentEvaluationRepository studentEvaluationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(CourseRepository courseRepository,
                           UserRepository userRepository,
                           RegistrationRepository registrationRepository,
                           EvaluationRepository evaluationRepository,
                           StudentEvaluationRepository studentEvaluationRepository,
                           PasswordEncoder passwordEncoder) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.registrationRepository = registrationRepository;
        this.evaluationRepository = evaluationRepository;
        this.studentEvaluationRepository = studentEvaluationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            System.out.println("Datos ya inicializados, omitiendo...");
            return;
        }

        User admin = new User("Administrador", "admin@duoc.cl", passwordEncoder.encode("admin123"), Role.ADMIN);

        User professor1 = new User("Juan Pérez", "juan.perez@duoc.cl", passwordEncoder.encode("profesor123"), Role.PROFESSOR);
        User professor2 = new User("María López", "maria.lopez@duoc.cl", passwordEncoder.encode("profesor123"), Role.PROFESSOR);
        User professor3 = new User("Carlos García", "carlos.garcia@duoc.cl", passwordEncoder.encode("profesor123"), Role.PROFESSOR);
        User professor4 = new User("María Rodríguez", "maria.rodriguez@duoc.cl", passwordEncoder.encode("profesor123"), Role.PROFESSOR);
        User professor5 = new User("Roberto Vega", "roberto.vega@duoc.cl", passwordEncoder.encode("profesor123"), Role.PROFESSOR);

        User student1 = new User("Ana Student", "ana@duoc.cl", passwordEncoder.encode("estudiante123"), Role.STUDENT);
        User student2 = new User("Pedro Student", "pedro@duoc.cl", passwordEncoder.encode("estudiante123"), Role.STUDENT);

        User savedAdmin = userRepository.save(admin);
        User savedProf1 = userRepository.save(professor1);
        User savedProf2 = userRepository.save(professor2);
        User savedProf3 = userRepository.save(professor3);
        User savedProf4 = userRepository.save(professor4);
        User savedProf5 = userRepository.save(professor5);
        User savedStudent1 = userRepository.save(student1);
        User savedStudent2 = userRepository.save(student2);

        Course java = new Course(
                "Introducción a Java",
                "Aprende los fundamentos de programación en Java, incluyendo conceptos de POO",
                savedProf1.getId()
        );

        Course python = new Course(
                "Python para ciencia de datos",
                "Domina Python para análisis de datos y aprendizaje automático",
                savedProf2.getId()
        );

        Course web = new Course(
                "Desarrollo web con Spring Boot",
                "Construye APIs REST y aplicaciones web con Spring Boot",
                savedProf3.getId()
        );

        Course database = new Course(
                "Diseño de bases de datos y SQL",
                "Aprende diseño de bases de datos relacionales y consultas SQL",
                savedProf4.getId()
        );

        Course inactive = new Course(
                "Mantenimiento de sistemas legados",
                "Mantenimiento de sistemas COBOL heredados",
                savedProf5.getId()
        );
        inactive.deactivate();

        Course savedJava = courseRepository.save(java);
        Course savedPython = courseRepository.save(python);
        Course savedWeb = courseRepository.save(web);
        Course savedDatabase = courseRepository.save(database);
        courseRepository.save(inactive);

        // Create sample registrations
        Registration reg1 = new Registration(savedJava.getId(), savedStudent1.getId());
        Registration reg2 = new Registration(savedPython.getId(), savedStudent1.getId());
        Registration reg3 = new Registration(savedJava.getId(), savedStudent2.getId());
        registrationRepository.save(reg1);
        registrationRepository.save(reg2);
        registrationRepository.save(reg3);

        // Create sample evaluations
        LocalDateTime futureDate1 = LocalDateTime.now().plusDays(14);
        LocalDateTime futureDate2 = LocalDateTime.now().plusDays(30);
        Evaluation eval1 = new Evaluation(savedJava.getId(), "Parcial 1 - Java", 100, futureDate1);
        Evaluation eval2 = new Evaluation(savedJava.getId(), "Proyecto Final", 100, futureDate2);
        Evaluation eval3 = new Evaluation(savedPython.getId(), "Tarea 1 - Python", 50, futureDate1.plusDays(7));

        Evaluation savedEval1 = evaluationRepository.save(eval1);
        Evaluation savedEval2 = evaluationRepository.save(eval2);
        Evaluation savedEval3 = evaluationRepository.save(eval3);

        // Create sample grades
        StudentEvaluation grade1 = new StudentEvaluation(savedStudent1.getId(), savedEval1.getId(), 85);
        StudentEvaluation grade2 = new StudentEvaluation(savedStudent1.getId(), savedEval3.getId(), 90);
        StudentEvaluation grade3 = new StudentEvaluation(savedStudent2.getId(), savedEval1.getId(), 78);
        studentEvaluationRepository.save(grade1);
        studentEvaluationRepository.save(grade2);
        studentEvaluationRepository.save(grade3);

        System.out.println("=== Datos de ejemplo cargados ===");
        System.out.println("Usuarios: " + userRepository.count());
        System.out.println("  - Admin: admin@duoc.cl / admin123");
        System.out.println("  - Profesores: " + professor1.getEmail() + ", " + professor2.getEmail() + ", etc. / profesor123");
        System.out.println("  - Estudiantes: " + student1.getEmail() + ", etc. / estudiante123");
        System.out.println("Cursos: " + courseRepository.count());
        System.out.println("Inscripciones: " + registrationRepository.count());
        System.out.println("Evaluaciones: " + evaluationRepository.count());
        System.out.println("Calificaciones: " + studentEvaluationRepository.count());
        System.out.println("=================================");
    }
}

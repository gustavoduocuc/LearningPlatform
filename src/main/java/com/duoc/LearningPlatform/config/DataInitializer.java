package com.duoc.LearningPlatform.config;

import com.duoc.LearningPlatform.model.Course;
import com.duoc.LearningPlatform.repository.CourseRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CourseRepository courseRepository;

    public DataInitializer(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public void run(String... args) {
        Course java = new Course(
                "Introducción a Java",
                "Aprende los fundamentos de programación en Java, incluyendo conceptos de POO",
                "Juan Pérez"
        );

        Course python = new Course(
                "Python para ciencia de datos",
                "Domina Python para análisis de datos y aprendizaje automático",
                "María López"
        );

        Course web = new Course(
                "Desarrollo web con Spring Boot",
                "Construye APIs REST y aplicaciones web con Spring Boot",
                "Carlos García"
        );

        Course database = new Course(
                "Diseño de bases de datos y SQL",
                "Aprende diseño de bases de datos relacionales y consultas SQL",
                "María Rodríguez"
        );

        Course inactive = new Course(
                "Mantenimiento de sistemas legados",
                "Mantenimiento de sistemas COBOL heredados",
                "Roberto Vega"
        );
        inactive.deactivate();

        courseRepository.save(java);
        courseRepository.save(python);
        courseRepository.save(web);
        courseRepository.save(database);
        courseRepository.save(inactive);

        System.out.println("Cursos de ejemplo cargados: " + courseRepository.count() + " cursos");
    }
}

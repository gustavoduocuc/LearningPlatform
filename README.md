# LearningPlatform

Plataforma de aprendizaje en línea — proyecto de arquitectura de microservicios

## Requisitos previos

- Java 21
- Maven 3.9+

## Ejecutar la aplicación

```bash
./mvnw spring-boot:run
```

La aplicación queda disponible en `http://localhost:8080`

## Endpoints disponibles

### API de cursos

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/courses` | Lista todos los cursos activos |
| GET | `/api/courses/{id}` | Obtiene un curso por ID |
| POST | `/api/courses` | Crea un curso |
| PUT | `/api/courses/{id}` | Actualiza un curso |
| DELETE | `/api/courses/{id}` | Elimina un curso |
| POST | `/api/courses/{id}/activate` | Activa un curso |
| POST | `/api/courses/{id}/deactivate` | Desactiva un curso |

### Salud y monitoreo

| Endpoint | Descripción |
|----------|-------------|
| `/actuator/health` | Estado de salud de la aplicación |
| `/actuator/info` | Información de la aplicación |

### Consola H2 (desarrollo)

Consola de la base H2: `http://localhost:8080/h2-console`

Datos de conexión:
- JDBC URL: `jdbc:h2:mem:learningplatformdb`
- Usuario: `sa`
- Contraseña: (vacía)

## Project Structure

```
src/main/java/com/duoc/LearningPlatform/
├── LearningPlatformApplication.java
├── config/
│   └── DataInitializer.java      # Loads sample courses on startup
├── controller/
│   └── CourseController.java
├── service/
│   └── CourseService.java
├── repository/
│   └── CourseRepository.java
├── model/
│   └── Course.java
├── dto/
│   ├── CourseRequest.java
│   └── CourseResponse.java
└── exception/
    ├── ResourceNotFoundException.java
    └── GlobalExceptionHandler.java
```

## Paso 3: Listado de cursos

La aplicación incluye un método personalizado para listar cursos activos con orden definido:

- Usa `ArrayList` como estructura temporal
- Filtra solo cursos activos (`active = true`)
- Ordena alfabéticamente por título con `Comparator`
- Devuelve una lista bien formateada (no la estructura cruda)

```java
// CourseService.findActiveCourses()
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
```

### Datos de ejemplo

Al iniciar se cargan 5 cursos de ejemplo (4 activos, 1 inactivo):

- Diseño de bases de datos y SQL
- Introducción a Java
- Python para ciencia de datos
- Desarrollo web con Spring Boot
- Mantenimiento de sistemas legados (inactivo)

## Ejemplos de uso de la API

### Crear un curso

```bash
curl -X POST http://localhost:8080/api/courses \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Introducción a Java",
    "description": "Aprende los fundamentos de programación en Java",
    "instructor": "Juan Pérez"
  }'
```

### Listar cursos activos

```bash
curl http://localhost:8080/api/courses
```

### Comprobar el estado de la aplicación

```bash
curl http://localhost:8080/actuator/health
```

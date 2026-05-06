# LearningPlatform

Plataforma de aprendizaje en línea — proyecto de arquitectura de microservicios

## Requisitos previos

- Java 21
- Maven 3.9+

## Configuración de base de datos

La aplicación soporta múltiples perfiles de base de datos:

### Perfil local (desarrollo) - H2 Database

Por defecto, la aplicación usa H2 en memoria para desarrollo local:

```bash
# Perfil local (implícito)
./mvnw spring-boot:run

# O explícitamente con perfil
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

**Características del perfil local:**
- Base de datos H2 en memoria
- Consola H2 disponible en `http://localhost:8080/h2-console`
- Datos de ejemplo cargados automáticamente
- Ideal para desarrollo y testing

**Datos de conexión H2:**
- JDBC URL: `jdbc:h2:mem:learningplatformdb`
- Usuario: `sa`
- Contraseña: (vacía)

### Perfil de producción - Oracle Database

Para producción, la aplicación se conecta a Oracle Database.

**1. Configurar variables de entorno:**

```bash
export ORACLE_DB_URL=jdbc:oracle:thin:@//host:1521/serviceName
export ORACLE_DB_USERNAME=tu_usuario
export ORACLE_DB_PASSWORD=tu_password
```

**2. Ejecutar build de producción:**

```bash
./scripts/build-prod.sh
```

**3. Ejecutar con perfil prod:**

```bash
java -jar -Dspring.profiles.active=prod target/LearningPlatform-0.0.1-SNAPSHOT.jar
```

**Características del perfil prod:**
- Conexión a Oracle Database
- Datos de ejemplo NO se cargan automáticamente
- H2 Console deshabilitada
- Configuración de connection pool optimizada

## Ejecutar la aplicación

### Modo desarrollo (H2)

```bash
./mvnw spring-boot:run
```

La aplicación queda disponible en `http://localhost:8080`

### Archivos de configuración

- `application.properties` - Configuración común
- `application-local.properties` - Configuración H2 (desarrollo)
- `application-prod.properties` - Configuración Oracle (producción)

## Autenticación

La API utiliza JWT (JSON Web Tokens) para autenticación. Todos los endpoints (excepto login) requieren un token válido en el header:

```
Authorization: Bearer <token>
```

### Roles de usuario

- **ADMIN**: Acceso completo a todos los endpoints
- **PROFESSOR**: Puede gestionar cursos y ver usuarios
- **STUDENT**: Solo lectura de cursos activos

### Usuarios de prueba

Al iniciar la aplicación se crean los siguientes usuarios:

| Rol | Email | Contraseña |
|-----|-------|------------|
| Admin | `admin@duoc.cl` | `admin123` |
| Profesor | `juan.perez@duoc.cl` | `profesor123` |
| Estudiante | `ana@duoc.cl` | `estudiante123` |

## Endpoints disponibles

### Autenticación (Público)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/auth/login` | Iniciar sesión, retorna JWT token |

**Ejemplo de login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@duoc.cl", "password": "admin123"}'
```

**Recuperación de contraseña (OTP):**
```bash
# Solicitar código OTP
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email": "usuario@duoc.cl"}'
# Resetear contraseña con OTP
curl -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{"email": "usuario@duoc.cl", "otp": "123456", "newPassword": "nuevaPass123"}'
```

### API de Usuarios (Requiere autenticación)

| Método | Endpoint | Acceso | Descripción |
|--------|----------|--------|-------------|
| GET | `/api/users` | ADMIN, PROFESSOR | Lista todos los usuarios |
| GET | `/api/users/{id}` | ADMIN, PROFESSOR | Obtiene un usuario por ID |
| POST | `/api/users` | ADMIN | Crea un nuevo usuario |
| PUT | `/api/users/{id}` | ADMIN (completo), PROFESSOR (solo email propio) | Actualiza un usuario |
| PUT | `/api/users/{id}/full` | ADMIN | Actualización completa (nombre, email, rol) |
| DELETE | `/api/users/{id}` | ADMIN | Elimina un usuario |

### API de Cursos (Requiere autenticación)

| Método | Endpoint | Acceso | Descripción |
|--------|----------|--------|-------------|
| GET | `/api/courses` | Todos los roles autenticados | Lista cursos activos |
| GET | `/api/courses/{id}` | Todos los roles autenticados | Obtiene un curso por ID |
| POST | `/api/courses` | ADMIN | Crea un curso |
| PUT | `/api/courses/{id}` | ADMIN, PROFESSOR | Actualiza un curso |
| DELETE | `/api/courses/{id}` | ADMIN | Elimina un curso |
| POST | `/api/courses/{id}/activate` | ADMIN, PROFESSOR | Activa un curso |
| POST | `/api/courses/{id}/deactivate` | ADMIN, PROFESSOR | Desactiva un curso |

**Nota:** Los cursos ahora usan `professorId` (ID del profesor) en lugar del nombre del instructor.

### API de Inscripciones/Registrations (Requiere autenticación)

| Método | Endpoint | Acceso | Descripción |
|--------|----------|--------|-------------|
| GET | `/api/registrations` | Todos los roles autenticados | Lista inscripciones (filtradas por rol) |
| GET | `/api/registrations?courseId={id}` | Todos los roles autenticados | Lista inscripciones de un curso |
| GET | `/api/registrations?studentId={id}` | Todos los roles autenticados | Lista inscripciones de un estudiante |
| GET | `/api/registrations/{id}` | Todos los roles autenticados | Obtiene una inscripción por ID |
| POST | `/api/registrations` | STUDENT | Inscribe un estudiante a un curso |
| DELETE | `/api/registrations/{id}` | ADMIN | Elimina una inscripción |

**Notas:**
- Estudiantes solo pueden inscribirse a sí mismos
- No se permite inscribirse dos veces al mismo curso
- El estudiante debe tener rol STUDENT

### API de Evaluaciones (Requiere autenticación)

| Método | Endpoint | Acceso | Descripción |
|--------|----------|--------|-------------|
| GET | `/api/evaluations` | ADMIN, PROFESSOR | Lista todas las evaluaciones |
| GET | `/api/evaluations?courseId={id}` | ADMIN, PROFESSOR | Lista evaluaciones de un curso |
| GET | `/api/evaluations/{id}` | ADMIN, PROFESSOR | Obtiene una evaluación por ID |
| POST | `/api/evaluations` | ADMIN, PROFESSOR | Crea una nueva evaluación |
| PUT | `/api/evaluations/{id}` | ADMIN, PROFESSOR | Modifica una evaluación |
| DELETE | `/api/evaluations/{id}` | ADMIN | Elimina una evaluación |
| GET | `/api/evaluations/{id}/grades` | ADMIN, PROFESSOR | Lista calificaciones de una evaluación |
| POST | `/api/evaluations/{id}/grades` | ADMIN, PROFESSOR | Asigna una calificación a un estudiante |

**Notas:**
- La calificación debe estar entre 0 y el `maximumScore` de la evaluación
- Solo estudiantes pueden recibir calificaciones
- La fecha de aplicación debe ser futura

### Salud y monitoreo

| Endpoint | Descripción |
|----------|-------------|
| `/actuator/health` | Estado de salud de la aplicación |
| `/actuator/info` | Información de la aplicación |

### Consola H2 (desarrollo - solo perfil local)

La consola H2 está disponible solo en el perfil `local`:

URL: `http://localhost:8080/h2-console`

Datos de conexión:
- JDBC URL: `jdbc:h2:mem:learningplatformdb`
- Usuario: `sa`
- Contraseña: (vacía)

## Project Structure

```
src/main/java/com/duoc/LearningPlatform/
├── LearningPlatformApplication.java
├── config/
│   ├── DataInitializer.java         # Carga datos de ejemplo
│   └── SecurityConfig.java          # Configuración de seguridad JWT
├── controller/
│   ├── AuthController.java          # Login, autenticación y recuperación de contraseña
│   ├── CourseController.java
│   ├── EvaluationController.java    # Gestión de evaluaciones y notas
│   ├── RegistrationController.java   # Inscripciones de estudiantes
│   └── UserController.java          # Gestión de usuarios
├── service/
│   ├── ConsolePasswordResetOtpNotifier.java  # Envío OTP a consola (logs)
│   ├── CourseService.java
│   ├── EvaluationService.java       # Evaluaciones y calificaciones
│   ├── RegistrationService.java     # Inscripciones
│   └── UserService.java             # Lógica de usuarios y auth
├── repository/
│   ├── CourseRepository.java
│   ├── EvaluationRepository.java
│   ├── PasswordResetCodeRepository.java  # Códigos OTP de recuperación
│   ├── RegistrationRepository.java
│   ├── StudentEvaluationRepository.java
│   └── UserRepository.java
├── model/
│   ├── Course.java
│   ├── Evaluation.java              # Evaluaciones de cursos
│   ├── PasswordResetCode.java       # Códigos OTP para recuperación de contraseña
│   ├── Registration.java            # Inscripciones estudiante-curso
│   ├── Role.java                    # Enum: ADMIN, PROFESSOR, STUDENT
│   ├── StudentEvaluation.java        # Calificaciones de estudiantes
│   └── User.java                    # Entidad de usuario con password
├── dto/
│   ├── CourseRequest.java           # Ahora usa professorId
│   ├── CourseResponse.java
│   ├── CreateUserRequest.java
│   ├── EvaluationRequest.java
│   ├── EvaluationResponse.java
│   ├── ForgotPasswordRequest.java   # Solicitud de recuperación de contraseña
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── RegistrationRequest.java
│   ├── RegistrationResponse.java
│   ├── StudentEvaluationRequest.java
│   ├── StudentEvaluationResponse.java
│   ├── UpdateEmailRequest.java
│   ├── UpdateUserRequest.java
│   └── UserResponse.java
├── security/
│   ├── EvaluationSecurity.java      # Helper para permisos de evaluaciones
│   ├── JwtAuthenticationFilter.java # Filtro de autenticación JWT
│   ├── JwtUtil.java                 # Generación/validación de tokens
│   ├── RegistrationSecurity.java    # Helper para permisos de inscripciones
│   └── UserSecurity.java            # Helper para verificación de usuario
└── exception/
    ├── ResourceNotFoundException.java
    └── GlobalExceptionHandler.java  # Manejo de errores auth
```

## Ejemplos de uso de la API

### 1. Login y obtener token

```bash
# Login como admin
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@duoc.cl", "password": "admin123"}' | jq -r '.token')

echo "Token: $TOKEN"
```

### 2. Crear un curso (requiere ADMIN)

```bash
curl -X POST http://localhost:8080/api/courses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "Introducción a Java",
    "description": "Aprende los fundamentos de programación en Java",
    "professorId": 2
  }'
```

### 3. Listar cursos activos (cualquier rol autenticado)

```bash
curl http://localhost:8080/api/courses \
  -H "Authorization: Bearer $TOKEN"
```

### 4. Crear un usuario (solo ADMIN)

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Nuevo Profesor",
    "email": "nuevo@duoc.cl",
    "password": "password123",
    "role": "PROFESSOR"
  }'
```

### 5. Listar usuarios (ADMIN o PROFESSOR)

```bash
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer $TOKEN"
```

### 6. Inscribir estudiante a curso (STUDENT)

```bash
# Login como estudiante primero
STUDENT_TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "ana@duoc.cl", "password": "estudiante123"}' | jq -r '.token')

# Inscribirse a un curso
curl -X POST http://localhost:8080/api/registrations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -d '{
    "courseId": 1,
    "studentId": 7
  }'
```

### 7. Listar inscripciones (cualquier rol autenticado)

```bash
curl http://localhost:8080/api/registrations \
  -H "Authorization: Bearer $TOKEN"

# Filtrar por curso
curl http://localhost:8080/api/registrations?courseId=1 \
  -H "Authorization: Bearer $TOKEN"
```

### 8. Crear evaluación (ADMIN o PROFESSOR)

```bash
curl -X POST http://localhost:8080/api/evaluations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "courseId": 1,
    "name": "Examen Parcial 1",
    "maximumScore": 100,
    "applicationDate": "2025-06-15T10:00:00"
  }'
```

### 9. Asignar calificación (ADMIN o PROFESSOR)

```bash
curl -X POST http://localhost:8080/api/evaluations/1/grades \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "studentId": 7,
    "score": 85
  }'
```

### 10. Ver calificaciones de una evaluación (ADMIN o PROFESSOR)

```bash
curl http://localhost:8080/api/evaluations/1/grades \
  -H "Authorization: Bearer $TOKEN"
```

## Códigos de respuesta

- `200 OK` - Petición exitosa (GET, PUT)
- `201 Created` - Recurso creado exitosamente (POST)
- `204 No Content` - Recurso eliminado exitosamente (DELETE)
- `400 Bad Request` - Datos inválidos o error de negocio
- `401 Unauthorized` - No autenticado o token inválido
- `403 Forbidden` - Autenticado pero sin permisos suficientes
- `404 Not Found` - Recurso no encontrado

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

Al iniciar se cargan:
- **5 cursos** (4 activos, 1 inactivo) vinculados a profesores
- **5 profesores** con credenciales de acceso
- **2 estudiantes** con acceso de solo lectura
- **1 administrador** con acceso total
- **3 inscripciones** de estudiantes a cursos
- **3 evaluaciones** creadas para los cursos
- **3 calificaciones** asignadas a estudiantes

## Testing

La aplicación incluye tests siguiendo TDD:

```bash
# Ejecutar todos los tests
./mvnw test
```

**Nota:** Los tests usan H2 (perfil de test) para garantizar consistencia y no requieren conexión a Oracle.

Estructura de tests:
- `model/` - Tests unitarios de entidades
- `repository/` - Tests de integración con `@DataJpaTest`
- `service/` - Tests unitarios con Mockito
- `controller/` - Tests de integración con `@WebMvcTest`
- `AuthenticationE2ETest` - Tests end-to-end de autenticación

## Build para producción

### Script de build automatizado

```bash
# Configurar variables de entorno primero
export ORACLE_DB_URL=jdbc:oracle:thin:@//tu-host:1521/tu-servicio
export ORACLE_DB_USERNAME=tu_usuario
export ORACLE_DB_PASSWORD=tu_password

# Ejecutar build de producción
./scripts/build-prod.sh
```

El script valida que todas las variables requeridas estén configuradas antes de compilar.

### Variables de entorno requeridas para producción

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `ORACLE_DB_URL` | URL JDBC de conexión Oracle | `jdbc:oracle:thin:@//host:1521/SERVICE` |
| `ORACLE_DB_USERNAME` | Usuario de base de datos | `learning_user` |
| `ORACLE_DB_PASSWORD` | Contraseña de base de datos | (secreto) |

### Ejemplo de archivo .env

Copia `.env.example` a `.env` y configura tus valores:

```bash
cp .env.example .env
# Edita .env con tus credenciales reales
```

**Importante:** Nunca incluyas el archivo `.env` en el control de versiones.

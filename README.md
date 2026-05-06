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

- **ADMIN**: Acceso completo a todos los endpoints; puede crear, listar y eliminar notificaciones de cualquier usuario
- **PROFESSOR**: Puede gestionar cursos y ver usuarios; puede listar, consultar, marcar como leídas y eliminar **sus propias** notificaciones
- **STUDENT**: Lectura de cursos activos; puede crear y consultar sus propios pagos simulados (tras inscribirse en el curso); puede listar, consultar, marcar como leídas y eliminar **sus propias** notificaciones

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
| DELETE | `/api/courses/{id}` | ADMIN, PROFESSOR | Elimina un curso |
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

### API de Pagos / Payments (simulado, requiere autenticación)

Este módulo representa el flujo de pago por matrícula en un curso. El procesamiento se simula: al crear un pago válido, el registro queda normalmente en estado `APPROVED` con una `transactionReference` generada (prefijo `SIM-`). En una arquitectura de microservicios real, esta lógica viviría en un **Payment Service** y se conectaría a un proveedor externo (PSP).

| Método | Endpoint | Acceso | Descripción |
|--------|----------|--------|-------------|
| POST | `/api/payments` | STUDENT | Crea un pago simulado para un curso (el estudiante es siempre el del JWT) |
| GET | `/api/payments` | ADMIN, STUDENT | ADMIN lista todos los pagos; STUDENT solo los propios |
| GET | `/api/payments/{id}` | ADMIN, STUDENT | ADMIN ve cualquier pago; STUDENT solo el suyo |
| PUT | `/api/payments/{id}/status` | ADMIN | Actualiza el estado con transiciones válidas |
| PUT | `/api/payments/{id}/cancel` | ADMIN | Cancela el pago (`CANCELLED`) cuando corresponde |

**Notas:**

- Debe existir una **inscripción** (`Registration`) para el par estudiante-curso antes de pagar.
- El curso debe existir; el monto debe ser mayor que cero; `paymentMethod` obligatorio (`CREDIT_CARD` o `BANK_TRANSFER`).
- No puede haber más de un pago **APPROVED** por el mismo estudiante y curso.
- Transiciones de estado (ADMIN): desde `PENDING` → `APPROVED`, `REJECTED`, `CANCELLED`; desde `APPROVED` → `CANCELLED`; estados finales no se reactivan arbitrariamente.
- **PROFESSOR** no utiliza estos endpoints.

### API de Notificaciones (simulado, requiere autenticación)

Este módulo persiste avisos para estudiantes y profesores (nuevos cursos, inscripciones, evaluaciones, pagos, etc.). **No hay envío real** por correo, SMS ni push: solo se guarda el registro y se expone por REST. En una arquitectura de microservicios real, un **Notification Service** recibiría eventos de otros servicios (cursos, matrículas, evaluaciones, pagos) y se integraría con proveedores externos o un broker de mensajes.

| Método | Endpoint | Acceso | Descripción |
|--------|----------|--------|-------------|
| POST | `/api/notifications` | ADMIN | Crea una notificación para el `recipientId` indicado (demostración / pruebas) |
| GET | `/api/notifications` | Autenticado | ADMIN lista todas; STUDENT y PROFESSOR solo las dirigidas a su usuario |
| GET | `/api/notifications/me` | Autenticado | Lista solo las notificaciones del usuario del JWT (incluye ADMIN respecto a su propio usuario) |
| GET | `/api/notifications/{id}` | Autenticado | ADMIN ve cualquier notificación; otros solo si son el destinatario |
| PUT | `/api/notifications/{id}/read` | Autenticado | Marca como leída (idempotente si ya estaba leída) |
| DELETE | `/api/notifications/{id}` | Autenticado | ADMIN elimina cualquiera; STUDENT y PROFESSOR solo las propias |

**Reglas de negocio:**

- Debe existir el usuario destinatario (`recipientId`).
- `title` y `message` no pueden estar vacíos; `notificationType` es obligatorio.
- Tipos admitidos: `NEW_COURSE`, `COURSE_ENROLLMENT`, `PENDING_ASSIGNMENT`, `EVALUATION_CREATED`, `GRADE_ASSIGNED`, `PAYMENT_APPROVED`, `PAYMENT_REJECTED`, `GENERAL`.
- Si se envían `relatedCourseId`, `relatedEvaluationId` o `relatedPaymentId`, la entidad referenciada debe existir.
- Un usuario no puede leer, marcar como leída ni borrar notificaciones de otro usuario (salvo ADMIN).

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
| POST | `/api/evaluations/{id}/submissions` | STUDENT | Estudiante envía tarea/examen (multipart/form-data) |
| GET | `/api/evaluations/{id}/submissions` | ADMIN, PROFESSOR | Lista envíos de estudiantes para una evaluación |
| GET | `/api/evaluations/my-evaluations` | STUDENT | Lista evaluaciones de cursos donde está inscrito |
| GET | `/api/evaluations/{id}/my-submission` | STUDENT | Ver el envío propio para una evaluación |
| GET | `/api/evaluations/{id}/my-grade` | STUDENT | Ver la calificación asignada para una evaluación |

**Notas:**
- La calificación debe estar entre 0 y el `maximumScore` de la evaluación
- Solo estudiantes pueden recibir calificaciones
- La fecha de aplicación debe ser futura

**Notas sobre envíos (submissions):**
- Estudiantes solo pueden enviar si están inscritos en el curso de la evaluación
- Solo se permite un envío por estudiante por evaluación
- Tipos de archivo permitidos: PDF, DOC, DOCX, ZIP
- El archivo se almacena como BLOB en la base de datos
- Los profesores solo pueden ver envíos de evaluaciones de sus propios cursos

**Flujo para estudiantes:**
1. El estudiante usa `GET /api/evaluations/my-evaluations` para ver evaluaciones disponibles
2. Con el `evaluationId`, envía la tarea con `POST /api/evaluations/{id}/submissions`
3. Puede verificar su envío con `GET /api/evaluations/{id}/my-submission`

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
│   ├── NotificationController.java # Notificaciones persistidas (entrega simulada)
│   ├── PaymentController.java       # Pagos simulados por matrícula
│   ├── RegistrationController.java   # Inscripciones de estudiantes
│   └── UserController.java          # Gestión de usuarios
├── service/
│   ├── ConsolePasswordResetOtpNotifier.java  # Envío OTP a consola (logs)
│   ├── CourseService.java
│   ├── EvaluationService.java       # Evaluaciones y calificaciones
│   ├── NotificationService.java     # Notificaciones y permisos por rol
│   ├── PaymentService.java          # Lógica de pagos simulados
│   ├── RegistrationService.java     # Inscripciones
│   └── UserService.java             # Lógica de usuarios y auth
├── repository/
│   ├── CourseRepository.java
│   ├── EvaluationRepository.java
│   ├── PasswordResetCodeRepository.java  # Códigos OTP de recuperación
│   ├── NotificationRepository.java
│   ├── PaymentRepository.java
│   ├── RegistrationRepository.java
│   ├── StudentEvaluationRepository.java
│   └── UserRepository.java
├── model/
│   ├── Course.java
│   ├── Evaluation.java              # Evaluaciones de cursos
│   ├── Notification.java            # Notificaciones a usuarios (FKs opcionales por ID)
│   ├── NotificationType.java        # Enum de tipo de notificación
│   ├── PasswordResetCode.java       # Códigos OTP para recuperación de contraseña
│   ├── Payment.java                 # Pagos simulados estudiante-curso
│   ├── PaymentMethod.java           # Enum: CREDIT_CARD, BANK_TRANSFER
│   ├── PaymentStatus.java           # Enum: PENDING, APPROVED, REJECTED, CANCELLED
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
│   ├── NotificationRequest.java
│   ├── NotificationResponse.java
│   ├── PaymentRequest.java
│   ├── PaymentResponse.java
│   ├── RegistrationRequest.java
│   ├── RegistrationResponse.java
│   ├── UpdatePaymentStatusRequest.java
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

### 11. Estudiante envía tarea/examen (STUDENT)

**Usando Postman (recomendado):**
1. Selecciona el endpoint `POST /api/evaluations/{id}/submissions`
2. En el body, selecciona **form-data**
3. Agrega un campo `description` (texto, opcional)
4. Agrega un campo `file` y haz clic en **Select File** para adjuntar tu archivo
5. Envía la petición

**Usando curl con multipart/form-data:**
```bash
# Login como estudiante primero
STUDENT_TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "ana@duoc.cl", "password": "estudiante123"}' | jq -r '.token')

# Enviar tarea con archivo adjunto
curl -X POST http://localhost:8080/api/evaluations/1/submissions \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -F "description=Mi tarea de Java" \
  -F "file=@/path/to/tarea.pdf"
```

**Tipos de archivo permitidos:**
- `application/pdf` - Archivos PDF
- `application/msword` - Archivos DOC
- `application/vnd.openxmlformats-officedocument.wordprocessingml.document` - Archivos DOCX
- `application/zip` o `application/x-zip-compressed` - Archivos ZIP

**Notas:**
- El estudiante debe estar inscrito en el curso de la evaluación
- Solo se permite un envío por estudiante por evaluación
- El archivo se envía como multipart/form-data, no como JSON

### 12. Profesor revisa envíos de estudiantes (ADMIN o PROFESSOR)

```bash
# Como profesor, ver los envíos para una evaluación
curl http://localhost:8080/api/evaluations/1/submissions \
  -H "Authorization: Bearer $TOKEN"
```

**Respuesta:**
```json
[
  {
    "id": 1,
    "evaluationId": 1,
    "studentId": 7,
    "studentName": "Ana García",
    "description": "Mi tarea de Java",
    "fileName": "tarea.pdf",
    "contentType": "application/pdf",
    "submittedAt": "2025-06-10T14:30:00"
  }
]
```

**Notas:**
- El profesor solo puede ver envíos de evaluaciones de cursos que imparte
- El ADMIN puede ver todos los envíos
- Después de revisar, el profesor puede asignar calificación usando el endpoint de grades

### 13. Estudiante ve sus evaluaciones disponibles (STUDENT)

```bash
# Como estudiante, ver evaluaciones de cursos donde está inscrito
curl http://localhost:8080/api/evaluations/my-evaluations \
  -H "Authorization: Bearer $STUDENT_TOKEN"
```

**Respuesta:**
```json
[
  {
    "id": 1,
    "courseId": 1,
    "name": "Examen Parcial 1",
    "maximumScore": 100,
    "applicationDate": "2025-06-15T10:00:00",
    "courseTitle": "Introducción a Java"
  },
  {
    "id": 2,
    "courseId": 2,
    "name": "Proyecto Final",
    "maximumScore": 100,
    "applicationDate": "2025-07-01T10:00:00",
    "courseTitle": "Base de Datos"
  }
]
```

**Notas:**
- Solo muestra evaluaciones de cursos donde el estudiante está inscrito
- El estudiante usa estos IDs para enviar sus tareas/exámenes

### 14. Estudiante ve su propio envío (STUDENT)

```bash
# Como estudiante, ver el envío que hizo para una evaluación específica
curl http://localhost:8080/api/evaluations/1/my-submission \
  -H "Authorization: Bearer $STUDENT_TOKEN"
```

**Respuesta:**
```json
{
  "id": 1,
  "evaluationId": 1,
  "studentId": 7,
  "studentName": "Ana García",
  "description": "Mi tarea de Java",
  "fileName": "tarea.pdf",
  "contentType": "application/pdf",
  "submittedAt": "2025-06-10T14:30:00"
}
```

**Notas:**
- Retorna error 404 si el estudiante no ha enviado nada para esta evaluación
- Solo muestra el envío propio, no los de otros estudiantes

### 15. Estudiante ve su calificación (STUDENT)

```bash
# Como estudiante, ver la calificación asignada para una evaluación
curl http://localhost:8080/api/evaluations/1/my-grade \
  -H "Authorization: Bearer $STUDENT_TOKEN"
```

**Respuesta:**
```json
{
  "id": 1,
  "studentId": 7,
  "evaluationId": 1,
  "score": 85,
  "evaluatedAt": "2025-06-20T10:30:00",
  "studentName": "Ana García",
  "evaluationName": "Examen Parcial 1"
}
```

**Notas:**
- Retorna error 404 si el profesor aún no ha asignado una calificación
- La nota está entre 0 y el `maximumScore` de la evaluación

### Notificaciones (ADMIN crea; destinatario consulta)

```bash
# Login admin y crear notificación para un estudiante (ajusta recipientId según DataInitializer, p. ej. 7)
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@duoc.cl", "password": "admin123"}' | jq -r '.token')

curl -s -X POST http://localhost:8080/api/notifications \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "recipientId": 7,
    "title": "Nueva evaluación",
    "message": "Tienes una evaluación pendiente en tu curso.",
    "notificationType": "PENDING_ASSIGNMENT",
    "relatedCourseId": 1
  }' | jq .

# Estudiante: listar solo sus notificaciones
STUDENT_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "ana@duoc.cl", "password": "estudiante123"}' | jq -r '.token')

curl -s http://localhost:8080/api/notifications/me \
  -H "Authorization: Bearer $STUDENT_TOKEN" | jq .

# Marcar como leída (sustituye NOTIFICATION_ID por el id devuelto)
curl -s -X PUT http://localhost:8080/api/notifications/NOTIFICATION_ID/read \
  -H "Authorization: Bearer $STUDENT_TOKEN" | jq .
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
- `repository/` - Tests de repositorio (en este proyecto, muchos usan Mockito sobre la interfaz del repositorio)
- `service/` - Tests unitarios con Mockito (incluye `NotificationServiceTest`, `PaymentServiceTest`, etc.)
- `controller/` - Tests de controlador con Mockito (`NotificationControllerTest`, `PaymentControllerTest`, etc.)
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

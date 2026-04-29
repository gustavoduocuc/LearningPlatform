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
│   ├── DataInitializer.java      # Carga datos de ejemplo
│   └── SecurityConfig.java         # Configuración de seguridad JWT
├── controller/
│   ├── AuthController.java        # Login y autenticación
│   ├── CourseController.java
│   └── UserController.java         # Gestión de usuarios
├── service/
│   ├── CourseService.java
│   └── UserService.java            # Lógica de usuarios y auth
├── repository/
│   ├── CourseRepository.java
│   └── UserRepository.java
├── model/
│   ├── Course.java
│   ├── Role.java                   # Enum: ADMIN, PROFESSOR, STUDENT
│   └── User.java                   # Entidad de usuario con password
├── dto/
│   ├── CourseRequest.java          # Ahora usa professorId
│   ├── CourseResponse.java
│   ├── CreateUserRequest.java
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── UpdateEmailRequest.java
│   ├── UpdateUserRequest.java
│   └── UserResponse.java
├── security/
│   ├── JwtAuthenticationFilter.java # Filtro de autenticación JWT
│   ├── JwtUtil.java                # Generación/validación de tokens
│   └── UserSecurity.java           # Helper para verificación de usuario
└── exception/
    ├── ResourceNotFoundException.java
    └── GlobalExceptionHandler.java # Manejo de errores auth
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

## Testing

La aplicación incluye tests siguiendo TDD:

```bash
# Ejecutar todos los tests
./mvnw test
```

Estructura de tests:
- `model/` - Tests unitarios de entidades
- `repository/` - Tests de integración con `@DataJpaTest`
- `service/` - Tests unitarios con Mockito
- `controller/` - Tests de integración con `@WebMvcTest`
- `AuthenticationE2ETest` - Tests end-to-end de autenticación

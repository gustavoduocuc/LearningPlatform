#!/bin/bash

# Script de build para producción
# Compila y empaqueta la aplicación con el perfil 'prod' activo

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "======================================"
echo "Build de Producción - LearningPlatform"
echo "======================================"
echo ""

# Validar variables de entorno requeridas
echo "Validando variables de entorno de Oracle..."

MISSING_VARS=()

if [ -z "$ORACLE_DB_URL" ]; then
    MISSING_VARS+=("ORACLE_DB_URL")
fi

if [ -z "$ORACLE_DB_USERNAME" ]; then
    MISSING_VARS+=("ORACLE_DB_USERNAME")
fi

if [ -z "$ORACLE_DB_PASSWORD" ]; then
    MISSING_VARS+=("ORACLE_DB_PASSWORD")
fi

if [ ${#MISSING_VARS[@]} -ne 0 ]; then
    echo ""
    echo "ERROR: Faltan las siguientes variables de entorno obligatorias:"
    for var in "${MISSING_VARS[@]}"; do
        echo "  - $var"
    done
    echo ""
    echo "Ejemplo de configuración:"
    echo "  export ORACLE_DB_URL=jdbc:oracle:thin:@//host:1521/serviceName"
    echo "  export ORACLE_DB_USERNAME=username"
    echo "  export ORACLE_DB_PASSWORD=password"
    echo ""
    exit 1
fi

echo "Variables de entorno configuradas correctamente."
echo ""

# Navegar al directorio del proyecto
cd "$PROJECT_DIR"

# Limpiar compilaciones anteriores
echo "Limpiando compilaciones anteriores..."
./mvnw clean -q

# Compilar y empaquetar con perfil de producción
echo "Compilando aplicación con perfil 'prod'..."
./mvnw package -DskipTests -q

# Verificar que el JAR se generó correctamente
JAR_FILE="$PROJECT_DIR/target/LearningPlatform-0.0.1-SNAPSHOT.jar"

if [ -f "$JAR_FILE" ]; then
    echo ""
    echo "======================================"
    echo "Build exitoso"
    echo "======================================"
    echo "Archivo generado: $JAR_FILE"
    echo ""
    echo "Para ejecutar en producción:"
    echo "  java -jar -Dspring.profiles.active=prod $JAR_FILE"
    echo ""
else
    echo "ERROR: No se encontró el archivo JAR generado"
    exit 1
fi

# Stock Simulator - Backend Spring Boot

Aplicación backend para un simulador de acciones desarrollada con Spring Boot 3.3.5.

## 📋 Requisitos Previos

Para ejecutar el proyecto localmente (sin Docker), necesitas:

- **Java 17** o superior
- **Maven 3.6+**
- **PostgreSQL 15+**
- **Git**

## 🚀 Ejecución Local (Sin Docker)

### 1. Configurar PostgreSQL

Crea una base de datos PostgreSQL:

```sql
CREATE DATABASE postgres;
```

O usa las credenciales por defecto en `application.properties`.

### 2. Configurar Variables de Entorno (Opcional)

Puedes crear un archivo `.env` o exportar las variables:

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=postgres
export DB_USERNAME=postgres
export DB_PASSWORD=tu_password
export MAIL_USERNAME=tu-email@gmail.com
export MAIL_PASSWORD=tu-app-password
```

### 3. Compilar y Ejecutar

```bash
# Compilar el proyecto
mvn clean package

# Ejecutar la aplicación
mvn spring-boot:run
```

O ejecutar el JAR directamente:

```bash
java -jar target/stock-simulator-0.0.1-SNAPSHOT.jar
```

La aplicación estará disponible en: `http://localhost:8080`

## 🐳 Ejecución con Docker

### Opción 1: Docker Compose (Recomendado)

Esta es la forma más sencilla de ejecutar todo el stack (PostgreSQL + Backend):

```powershell
# Construir y levantar los servicios
docker-compose up --build

# O en modo detached (segundo plano)
docker-compose up -d --build
```

Para detener los servicios:

```powershell
docker-compose down

# Para eliminar también los volúmenes (datos de la BD)
docker-compose down -v
```

### Opción 2: Docker Individual

#### Construir la imagen:

```powershell
docker build -t stock-simulator-backend .
```

#### Ejecutar el contenedor:

```powershell
docker run -d `
  --name stock-simulator-backend `
  -p 8080:8080 `
  -e DB_HOST=host.docker.internal `
  -e DB_PORT=5432 `
  -e DB_NAME=postgres `
  -e DB_USERNAME=postgres `
  -e DB_PASSWORD=postgres `
  stock-simulator-backend
```

## 🔧 Configuración

### Variables de Entorno

Puedes configurar el proyecto mediante variables de entorno. Crea un archivo `.env` en la raíz del proyecto:

```env
# Base de Datos
DB_NAME=postgres
DB_USERNAME=postgres
DB_PASSWORD=postgres
DB_PORT=5432

# Servidor
SERVER_PORT=8080

# Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=tu-email@gmail.com
MAIL_PASSWORD=tu-app-password

# CORS (orígenes permitidos separados por coma)
CORS_ALLOWED_ORIGINS=http://localhost:4200,http://localhost:3000
```

### Configuración de Email

Para usar Gmail, necesitas generar una "App Password":

1. Ve a tu cuenta de Google
2. Seguridad → Verificación en 2 pasos (debe estar activada)
3. Contraseñas de aplicaciones → Generar nueva
4. Usa esa contraseña en `MAIL_PASSWORD`

## 🌐 Conexión con Frontend

El backend está configurado para aceptar peticiones desde:

- `http://localhost:4200` (por defecto, Angular)

Para permitir otros orígenes, configura la variable `CORS_ALLOWED_ORIGINS`:

```env
CORS_ALLOWED_ORIGINS=http://localhost:4200,http://localhost:3000,http://localhost:5173
```

### Ejemplo de configuración en el frontend:

```typescript
// Angular - environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080'
};

// React/Vue - .env
VITE_API_URL=http://localhost:8080
REACT_APP_API_URL=http://localhost:8080
```

## 📦 Estructura del Proyecto

```
stock-simulator-spring/
├── src/
│   ├── main/
│   │   ├── java/ucab/edu/ve/stocksimulator/
│   │   │   ├── controller/     # Controladores REST
│   │   │   ├── dto/            # Data Transfer Objects
│   │   │   ├── model/          # Entidades JPA
│   │   │   ├── repository/     # Repositorios JPA
│   │   │   ├── service/        # Lógica de negocio
│   │   │   └── security/       # Configuración de seguridad
│   │   └── resources/
│   │       └── application.properties
│   └── test/                   # Tests
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

## 🧪 Testing

```powershell
# Ejecutar todos los tests
mvn test

# Ejecutar tests específicos
mvn test -Dtest=StockControllerTest
```

## 📝 Endpoints Principales

- **Usuarios**: `/api/user`
- **Stocks**: `/api/stock`
- **Transacciones**: `/api/transaction`
- **Formulario de contacto**: `/api/contact`

## 🔍 Verificar que Funciona

```powershell
# Verificar que el backend está corriendo (ejemplo con endpoint de stocks)
curl http://localhost:8080/api/stock

# O desde el navegador
http://localhost:8080/api/stock
```

## 🐛 Troubleshooting

### Error de conexión a la base de datos

- Verifica que PostgreSQL esté corriendo
- Revisa las credenciales en `application.properties` o variables de entorno
- Si usas Docker, verifica que el contenedor de PostgreSQL esté saludable: `docker-compose ps`

### Error de CORS

- Verifica que el origen del frontend esté en `CORS_ALLOWED_ORIGINS`
- Revisa la consola del navegador para ver el error específico

### Puerto ya en uso

```powershell
# Windows - encontrar proceso usando el puerto 8080
netstat -ano | findstr :8080

# Cambiar el puerto en application.properties o variable SERVER_PORT
```

## 📚 Tecnologías Utilizadas

- Spring Boot 3.3.5
- Spring Data JPA
- Spring Security
- PostgreSQL
- Maven
- Java 17

## 👥 Contribuir

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📄 Licencia

Este proyecto es parte de un proyecto académico de la UCAB.


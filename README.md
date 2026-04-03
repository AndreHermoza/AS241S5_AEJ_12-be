# ✨ AI Services API - Spring Boot Reactive

Una API reactiva moderna construida con Spring Boot que integra poderosos modelos de Inteligencia Artificial procedentes de **Rapid API**, utilizando un stack completamente no bloqueante (WebFlux + R2DBC).

---

## 🚀 Modelos IA Integrados

### 1️⃣ AI Text-to-Image Generator Flux
Transforma texto en imágenes al instante usando inteligencia artificial.
* **Modelo:** Basado en el algoritmo Flux.
* **Flujo:** Envías un `prompt` (texto descriptivo) ➡️ la IA genera una imagen acorde ➡️ la aplicación guarda la URL y sus atributos en la base de datos.
* **API Externa:** `ai-text-to-image-generator-flux-free-api.p.rapidapi.com`

### 2️⃣ AI Background Remover
Herramienta de visión artificial para quitar fondos de imágenes automáticamente.
* **Flujo:** Envías la URL de origen (`source_image_url`) ➡️ detecta el sujeto principal en la imagen ➡️ remueve el fondo por completo ➡️ descarga los *bytes* y guarda la imagen con fondo transparente en el almacenamiento local del sistema.
* **API Externa:** `ai-background-remover.p.rapidapi.com`

---

## 🛠 Tecnologías y Herramientas

<p>
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Boot-4.0.5-6DB33F?style=for-the-badge&logo=spring&logoColor=white" />
  <img src="https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white" />
  <img src="https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" />
</p>

* **Paradigma:** Programación Reactiva (`Project Reactor`, WebFlux).
* **Persistencia Reactiva:** Spring Data R2DBC para conectividad asíncrona a bases de datos relacionales sin bloqueos.
* **IDEs Recomendados:** IntelliJ IDEA | Visual Studio Code.
* **Librerías Adicionales:** Lombok (para reducción de boilerplate code).

---

## 📦 Dependencias Core (Maven)
Fragmento principal del `pom.xml` con el ecosistema de base de datos asíncrona:

```xml
<!-- WebFlux para programación reactiva (Non-blocking I/O) -->
<dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

<!-- R2DBC para persistencia de datos relacional de manera asíncrona -->
<dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-r2dbc</artifactId>
</dependency>

<!-- Drivers para PostgreSQL R2DBC (y estándar en tiempo de ejecución) -->
<dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>r2dbc-postgresql</artifactId>
      <scope>runtime</scope>
</dependency>
<dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
      <scope>runtime</scope>
</dependency>
```

---

## 🌐 Endpoints Principales

| Dominio | Método | URL | Body Ejemplo (JSON) | Descripción |
|---|---|---|---|---|
| **Text to Image** | `POST` | `/api/v1/tti/generate` | `{"prompt": "a futuristic city"}` | Genera una nueva imagen a partir de un prompt. |
| | `GET` | `/api/v1/tti` | - | Lista el historial de creaciones. |
| | `GET` | `/api/v1/tti/{id}` | - | Busca una generación por su identificador. |
| | `PATCH` | `/api/v1/tti/deactivate/{id}`| - | Cambia el estado (boolean) del registro a inactivo. |
| **B.G. Remover** | `POST` | `/api/v1/bgremover/process`| `{"source_image_url": "http://img"}` | Procesa url asíncronamente y guarda img local. |
| | `GET` | `/api/v1/bgremover` | - | Obtiene el listado de imágenes a las que se quitó fondo. |

---

## ⚙️ Configuración y Despliegue Local

**1. Credenciales y DB**
Asegúrate de inyectar las credenciales correspondientes o listarlas en tu `application.properties`/`application.yml`:
```properties
# Base de datos Asíncrona (Postgres)
spring.r2dbc.url=r2dbc:postgresql://localhost:5432/mi_base_de_datos
spring.r2dbc.username=mi_usuario_db
spring.r2dbc.password=mi_password_db

# Rapid API Key
api.rapidapi.key=INGRESA_TU_API_KEY_DE_RAPID_API
```

**2. Ejecución**
Desde la raíz del proyecto (donde convive el archivo `pom.xml`):
```bash
mvn clean install
mvn spring-boot:run
```

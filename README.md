# WaterTankCenter

## Descripción

WaterTankCenter es una plataforma para la simulación, monitorización y administración de tanques de agua. El proyecto está compuesto por dos módulos principales:

- `WaterTankCenter-Backend`: API REST y lógica de negocio implementada en Java con Spring Boot (Maven). Provee endpoints para obtener datos de sensores, controlar actuadores y gestionar configuraciones del sistema.
- `WaterTankCenter-Frontend`: interfaz web construida con Angular que consume la API del backend y muestra gráficos, niveles de agua y paneles de control para usuarios y administradores.

## Objetivo

Permitir la visualización en tiempo real del nivel de agua en uno o varios tanques, la configuración y el control de actuadores (válvulas, bombas) y la simulación de entradas/salidas. El proyecto es útil para entornos educativos, experimentación con controladores y demostraciones de integración entre frontend y backend.

## Estructura del repositorio

Raíz

- `WaterTankCenter-Backend/` - Aplicación Spring Boot (Maven).
  - `src/main/java/...` - Código fuente Java.
  - `src/main/resources/application.yaml` - Configuración principal.
  - `HELP.md` - Notas y ayudas específicas del backend.
- `WaterTankCenter-Frontend/` - Aplicación Angular.
  - `src/app/` - Componentes, servicios y modelos.
  - `package.json` - Dependencias y scripts de desarrollo.

## Requisitos previos

- Java 11+ (ver `pom.xml` para la versión exacta requerida).
- Maven 3.6+.
- Node.js 14+ y npm o yarn.
- Angular CLI (opcional, para desarrollo del frontend).

## Instalación y ejecución

Backend (desarrollo)

1. Entra al directorio del backend:

   cd WaterTankCenter-Backend

2. Construye y ejecuta con Maven:

   mvn clean package
   mvn spring-boot:run

Alternativa: ejecutar el JAR generado:

    java -jar target/*.jar

Por defecto la aplicación se levanta en http://localhost:8080, revisa `application.yaml` para confirmar puertos y propiedades.

Frontend (desarrollo)

1. Entra al directorio del frontend:

   cd WaterTankCenter-Frontend

2. Instala dependencias y ejecuta en modo desarrollo:

   npm install
   npm start

   o usando Angular CLI:

   ng serve --open

3. La aplicación por defecto queda en http://localhost:4200. Asegúrate de que el backend esté corriendo y, si es necesario, ajusta la URL base de la API en `src/app/app.config.ts`.

## Configuración

- Backend: `WaterTankCenter-Backend/src/main/resources/application.yaml` contiene puertos, parámetros de simulación y propiedades de conexión. Ajusta según tu entorno (p. ej. base de datos o broker si aplica).
- Frontend: `WaterTankCenter-Frontend/src/app/app.config.ts` define la URL base de la API y otras constantes de configuración de la interfaz.

## API - Endpoints principales (ejemplos)

Revisa los controladores en `WaterTankCenter-Backend/src/main/java/com/miumg/wtcenter/controller` para la lista completa de rutas.

- GET /api/tanks - Listar tanques disponibles.
- GET /api/tanks/{id}/level - Obtener nivel actual del tanque.
- POST /api/tanks/{id}/control - Enviar comando de control (abrir/cerrar válvula, ajustar bomba).
- GET /api/metrics - Obtener métricas y estadísticas históricas.

## Desarrollo y pruebas

- Backend: los tests están en `WaterTankCenter-Backend/src/test/java`. Ejecuta:

  mvn test

- Frontend: los tests y specs se encuentran en `WaterTankCenter-Frontend/src/app/**/*.spec.ts`. Ejecuta:

  npm test

## Buenas prácticas

- Levanta primero el backend y confirma que la API responde (curl, Postman o similar).
- Si trabajas con frontend en `ng serve`, asegúrate de habilitar CORS temporalmente en el backend o usar proxy de Angular para evitar problemas de origen cruzado.
- Añade datos de ejemplo o fixtures si necesitas poblar el sistema para demostraciones; busca recursos en `src/test/resources` o crea scripts de carga.

## Contribuciones

Si quieres contribuir:

1. Haz fork del repositorio y crea una rama descriptiva: `feat/nombre-cambio` o `fix/nombre-bug`.
2. Añade pruebas para cambios relevantes.
3. Ejecuta los tests y linters antes de abrir el Pull Request.
4. Describe claramente en el PR el objetivo del cambio y cómo probarlo.

## Licencia

Incluye un archivo `LICENSE` si deseas especificar la licencia (por ejemplo MIT, Apache 2.0, etc.). Si no existe, agrega uno antes de publicar.

## Contacto y soporte

Para dudas, mejoras o reportes, contacta al autor o crea un issue en el repositorio con la etiqueta correspondiente.

## Próximos pasos sugeridos

- Documentar la API con ejemplos curl y/o generar una colección Postman.
- Añadir un `CONTRIBUTING.md` con líneas guía para desarrolladores.
- Proveer un `docker-compose.yml` para ejecutar backend y frontend en contenedores.

---

Este README ofrece una visión general para usar y desarrollar el proyecto. Si quieres que añada secciones concretas (ejemplos de uso de la API, Docker, CI/CD o diagramas de arquitectura), dime cuáles y las agrego.

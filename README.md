# Sistema de Inventario Distribuido

## 1. Título y Descripción General
**Nombre proyecto:** Sistema de Gestión de Inventario Distribuido (Inventory System)

**Resumen:** Es una solución robusta para la gestión de inventario distribuido, compras, ventas y logística entre múltiples sedes.
**Propósito:** El sistema garantiza coherencia de datos en tiempo real y trazabilidad auditable de cada movimiento.

## 2. Stack Tecnológico (Justificación Técnica)
* **Lenguaje y Framework:** Java 21 con Spring Boot 3.4.1.
* **Base de Datos:** MySQL 8.0 (Elegida por su naturaleza relacional, ideal para transacciones seguras y consistencia de datos).
* **Autenticación:** Implementación de Seguridad con Spring Security y JWT (jjwt 0.12.6), utilizando cifrado de contraseñas con BCrypt.
* **Mensajería:** Apache Kafka para procesamiento de eventos asíncronos y comunicación desacoplada.
* **Herramientas de IA:** El diseño de la arquitectura, la lógica de negocio y la documentación técnica fueron potenciados por herramientas de IA como **Gemini** y **Antigravity**, asegurando mejores prácticas y eficiencia.

## 3. Arquitectura del Sistema
* **Patrón de Diseño:** Arquitectura en Capas (Controller-Service-Repository), siguiendo principios de **Clean Architecture** para mantener el desacoplamiento.
* **Principios:** Uso estricto de principios **SOLID** y separación de responsabilidades.
* **Diagramas:**
    * **Diagrama de Arquitectura:**
      ![Diagrama de arquitectura Sistema Inventario](./Diagrama%20de%20arquitectura%20Sistema%20Inventario.png)
    * **Diagrama Entidad-Relación (E-R):** Disponible en el script [modelo_er.sql](./modelo_er.sql).

## 4. Requisitos Previos e Instalación
### Software necesario:
* **Docker** y **Docker Compose** (Versiones recientes).
* **Java Development Kit (JDK) 21** (Para ejecución local sin Docker).

### Pasos:
1. **Clonar el repositorio:**
   ```bash
   git clone <url-del-repositorio>
   cd Back-SistemaInventario
   ```
2. **Configurar variables de entorno:**
   Cree un archivo `.env` o revise la configuración en `docker-compose.yml`. Las credenciales por defecto están configuradas para desarrollo.
3. **Comando de ejecución:**
   ```bash
   docker compose up --build
   ```

## 5. Módulos y Funcionalidades Principales
* **Gestión de Inventario:** CRUD completo con trazabilidad de movimientos de productos en tiempo real a través de múltiples sucursales.
* **Transferencias entre Sucursales:** Flujo controlado desde la solicitud original hasta la confirmación de recepción, asegurando que el stock sea consistente.
* **Módulo de Logística:** Seguimiento detallado del transporte de mercancías, cálculo de tiempos estimados vs. reales y clasificación inteligente de rutas.
* **Gestión de Proveedores:** Registro centralizado, catálogo de productos por proveedor y evaluación de cumplimiento en tiempos de entrega.
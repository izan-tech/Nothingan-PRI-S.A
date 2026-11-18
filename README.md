# Nothingan-PRI-S.A

Plataforma de gestión web full-stack desarrollada como parte del proyecto intermodular
**Desarrollo de Aplicaciones Multiplataforma (DAM)** (Grupo 3 — Institut Nicolau Copèrnic, Terrassa). 

## 📋 Resumen del Proyecto 

**Nothingan Pri S.A.** es una empresa de alquiler de vehículos que opera en Terrassa desde hace más de 36 años. Esta plataforma reemplaza los procesos manuales antiguos con un sistema digital centralizado de ERP y CRM que cubre todo el ciclo de alquiler:

- **Portal del Cliente:** Registro de usuarios con subida de documento de identificación/licencia de conducir, filtrado del catálogo de vehículos por fecha/hora, bloqueo automático del vehículo al reservar, historial de reservas y reseñas de vehículos con 5 estrellas.

-  **Panel de Control del Agente:** Gestión de la flota (CRUD, activación/desactivación), registro de clientes y actualización a estado premium, listas de verificación de entrega de vehículos con evidencia fotográfica e inspecciones de devolución de vehículos con cálculo automático de deducción de depósitos. -

- **Panel de Administrador:** Flujo de trabajo de validación de clientes pendiente, control de acceso basado en roles, registros de errores del sistema, y reportes de reservas/ingresos de clientes.

## 🛠️ Stack Tecnológico
- **Backend:** Java, Spring Boot, Spring Data JPA, Spring Security
- **Frontend:** Thymeleaf, HTML5, CSS3, JavaScript
- **Bases de Datos:** MySQL (datos de negocio relacionales) + MongoDB (documentos, fotos de inspección, notificaciones, registros)
- **Testing y Gestión:** JUnit 5, Jira (Scrum Sprints), Figma (prototipos UI/UX), Git / GitLab / GitHub

## 📄 Documentación y Manual de Usuario
- 📘 **[Leer Memoria Técnica Completa & Manual de Usuario (PDF)](./Memoria%20Nothingan%20Pri%20S.A.pdf)** *(Incluye memoria del proyecto, documentación técnica, manual de usuario, propuestas de mejora y conclusiones).* 

## 👥 Equipo de Desarrollo (Grupo 3 — 2º DAM)
- **[Hugo López Soldado](https://github.com/hlopezsoldado)** — *Módulo de Gestión de Agentes, Seguridad Basada en Roles, Panel en Tiempo Real & Pruebas JUnit*
- **[Izan Lázaro Egea](https://github.com/izan-tech)** — *Arquitectura Full-Stack y Backend (Spring Boot & MongoDB), Módulos de Clientes y Reservas, Validación del Sistema & Corrección de Errores*
- **[Brais Anfruns Rodríguez](https://github.com/banfruns-ban)** — *Módulo de Gestión de Flota y Vehículos (CRUD Completo & Control de Disponibilidad), QA Funcional & Pruebas Unitarias/Integración con JUnit*
- **[Yuri Andrei Martinello](https://github.com/yuri-martinello)** — *Desarrollo Full-Stack, Autenticación, Seguridad y Permisos de Acceso, Mejoras en la UX de Navegación & Depuración de Reservas*

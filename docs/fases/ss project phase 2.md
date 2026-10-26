**Universidad San Carlos de Guatemala Centro Universitario de Occidente**  
**División de Ingeniería**  
**Curso: Seminario de sistemas**  
**Ingeniero: Mario Ramirez**

	

**Proyecto SS1: NetPay Manager**   
Fase 2: Análisis, Modelado y Planificación

**Dónavin O'brian Gabriel Tzunún López 201930708**

[**1\. Historias de Usuario	4**](#1.-historias-de-usuario)

[HU01 – Inicio de sesión	4](#hu01-–-inicio-de-sesión)

[HU02 – Administración de empleados	4](#hu02-–-administración-de-empleados)

[HU03 – Restablecimiento de contraseña	4](#hu03-–-restablecimiento-de-contraseña)

[HU04 – Administración de clientes	5](#hu04-–-administración-de-clientes)

[HU05 – Administración de servicios	5](#hu05-–-administración-de-servicios)

[HU06 – Asignación de clientes a cobradores	5](#hu06-–-asignación-de-clientes-a-cobradores)

[HU07 – Consulta de clientes asignados	6](#hu07-–-consulta-de-clientes-asignados)

[HU08 – Registro de pagos	6](#hu08-–-registro-de-pagos)

[HU09 – Registro de pagos parciales	6](#hu09-–-registro-de-pagos-parciales)

[HU10 – Registro de pagos adelantados	6](#hu10-–-registro-de-pagos-adelantados)

[HU11 – Consulta del historial de pagos	7](#hu11-–-consulta-del-historial-de-pagos)

[HU12 – Generación de recibos	7](#hu12-–-generación-de-recibos)

[HU13 – Consulta de auditoría	7](#hu13-–-consulta-de-auditoría)

[HU14 – Generación de reportes	8](#hu14-–-generación-de-reportes)

[HU15 – Exportación de reportes	8](#hu15-–-exportación-de-reportes)

[HU16 – Importación de información histórica	8](#hu16-–-importación-de-información-histórica)

[HU17 – Respaldo y recuperación de información	8](#hu17-–-respaldo-y-recuperación-de-información)

[HU18 – Despliegue del frontend	9](#hu18-–-despliegue-del-frontend)

[HU19 – Despliegue del backend	9](#hu19-–-despliegue-del-backend)

[HU20 – Integración y despliegue continuo	9](#hu20-–-integración-y-despliegue-continuo)

[**2\. Casos de uso	11**](#2.-casos-de-uso)

[**Módulo 1: Autenticación	11**](#módulo-1:-autenticación)

[CU-01 — Iniciar sesión	11](#cu-01-—-iniciar-sesión)

[CU-02 — Cerrar sesión	11](#cu-02-—-cerrar-sesión)

[**Módulo 2: Dashboard	12**](#módulo-2:-dashboard)

[CU-03 — Ver dashboard	12](#cu-03-—-ver-dashboard)

[**Módulo 3: Clientes	12**](#módulo-3:-clientes)

[CU-04 — Consultar clientes	12](#cu-04-—-consultar-clientes)

[CU-05 — Gestionar cliente	13](#cu-05-—-gestionar-cliente)

[**Módulo 4: Servicios	14**](#módulo-4:-servicios)

[CU-06 — Consultar servicios	14](#cu-06-—-consultar-servicios)

[CU-07 — Gestionar servicio	14](#cu-07-—-gestionar-servicio)

[**Módulo 5: Pagos	15**](#módulo-5:-pagos)

[CU-08 — Consultar pagos	15](#cu-08-—-consultar-pagos)

[CU-09 — Registrar pago	15](#cu-09-—-registrar-pago)

[**Módulo 6: Recibos	16**](#módulo-6:-recibos)

[CU-10 — Consultar recibos	16](#cu-10-—-consultar-recibos)

[**Módulo 7: Reportes	16**](#módulo-7:-reportes)

[CU-11 — Generar y exportar reporte de cobros	17](#cu-11-—-generar-y-exportar-reporte-de-cobros)

[**Módulo 8: Auditoría	17**](#módulo-8:-auditoría)

[CU-12 — Consultar bitácora de auditoría	17](#cu-12-—-consultar-bitácora-de-auditoría)

[**Módulo 9: Usuarios	18**](#módulo-9:-usuarios)

[CU-13 — Consultar usuarios	18](#cu-13-—-consultar-usuarios)

[CU-14 — Gestionar usuario	18](#cu-14-—-gestionar-usuario)

[**Módulo 10: Casos de soporte (uso interno)	19**](#módulo-10:-casos-de-soporte-\(uso-interno\))

[CU-15 — Validar sesión activa	19](#cu-15-—-validar-sesión-activa)

[CU-16 — Registrar evento de auditoría	19](#cu-16-—-registrar-evento-de-auditoría)

[**3\. Arquitectura de Software y Estructura de Paqueterías	21**](#3.-arquitectura-de-software-y-estructura-de-paqueterías)

[3.1 Estructura del Backend (Spring Boot \+ Java)	21](#heading=h.8vqrz5by6udl)

[3.2 Estructura del Frontend (Angular)	21](#heading=h.2u257gtuax9g)

[**4\. Modelado Técnico del Sistema	21**](#4.-modelado-técnico-del-sistema)

[**4.1 Diagrama de Clases	21**](#4.1-diagramas)

[Componentes principales representados	22](#componentes-principales-representados)

[**4.2 Modelo Entidad-Relación (E/R) Finalizado	23**](#4.2-modelo-entidad-relación-\(e/r\)-finalizado)

[Consideraciones de integridad	23](#consideraciones-de-integridad)

[**4.3 Modelos de Procesos Técnicos Completos	24**](#4.3-modelos-de-procesos-técnicos-completos)

[Proceso Técnico A: Autenticación de Usuarios y Control de Roles	24](#proceso-técnico-a:-autenticación-de-usuarios-y-control-de-roles)

[**5\. Gestión Evolutiva de Datos (Migraciones)	25**](#5.-gestión-evolutiva-de-datos-\(migraciones\))

[**1\. Configuración de Flyway	25**](#1.-configuración-de-flyway)

[**2\. Convenciones y numeración (V\<n\>\_\_\<descripcion\>.sql)	25**](#2.-convenciones-y-numeración-\(v\<n\>__\<descripcion\>.sql\))

[**3\. Migraciones actuales	25**](#3.-migraciones-actuales)

[V1 — Tablas base	26](#v1-—-tablas-base)

[V2 — Datos maestros de tipologías	28](#v2-—-datos-maestros-de-tipologías)

[V3 — Usuario raíz (dato semilla)	28](#v3-—-usuario-raíz-\(dato-semilla\))

[V4 — Tabla app\_service (planes/servicios de internet)	29](#v4-—-tabla-app_service-\(planes/servicios-de-internet\))

[**6\. Documentación de Infraestructura y Entorno de IA	30**](#6.-documentación-de-infraestructura-y-entorno-de-ia)

[**6.1 Contenedores Locales (Docker)	30**](#6.1-contenedores-locales-\(docker\))

[Consideraciones del entorno	30](#consideraciones-del-entorno)

[**6.2 Uso de Agentes de IA en la Fase de Programación	30**](#6.2-uso-de-agentes-de-ia-en-la-fase-de-programación)

[Flujo de trabajo utilizado	31](#flujo-de-trabajo-utilizado)

[Herramientas utilizadas	31](#herramientas-utilizadas)

[**7\. Secuencia de Pantallas	32**](#7.-secuencia-de-pantallas)

[**8\. Anexos	39**](#8.-anexos)

## 

## 1\. Historias de Usuario {#1.-historias-de-usuario}

Durante la Fase 2, las historias de usuario fueron revisadas y refinadas tomando como referencia las funcionalidades implementadas y las necesidades identificadas durante el desarrollo. Estas historias permiten relacionar las funcionalidades del sistema con los usuarios que interactúan con ellas y establecer criterios verificables para validar su implementación.

Cada historia se identifica mediante un código único y contiene una descripción de la necesidad, junto con sus respectivos criterios de aceptación. Los criterios permiten comprobar que la funcionalidad desarrollada cumple con el comportamiento esperado.

Las historias contempladas para esta fase abarcan los módulos de autenticación, administración de usuarios, clientes, servicios, asignaciones, pagos, recibos, auditoría, reportes, importación de información y los componentes relacionados con infraestructura y despliegue.

### **HU01 – Inicio de sesión** {#hu01-–-inicio-de-sesión}

**Como** empleado del sistema, **quiero** autenticarme mediante mis credenciales, **para** ingresar únicamente a las funcionalidades que corresponden a mi rol.

**Criterios de aceptación:**

* El sistema debe presentar un formulario para ingresar las credenciales.  
* Las credenciales deben ser verificadas antes de permitir el acceso.  
* El sistema debe identificar el rol del usuario autenticado.  
* Las credenciales incorrectas deben impedir el inicio de sesión.  
* Las opciones disponibles deben depender del rol asignado al usuario.  
* Una sesión válida debe permitir al usuario utilizar el sistema mientras permanezca autenticado.


### **HU02 – Administración de empleados** {#hu02-–-administración-de-empleados}

**Como** administrador, **quiero** administrar las cuentas de los empleados, **para** mantener control sobre los usuarios que tienen acceso al sistema.

**Criterios de aceptación:**

* Debe ser posible registrar un nuevo empleado.  
* Debe ser posible consultar la información registrada.  
* Debe ser posible actualizar los datos de un empleado.  
* Debe ser posible activar o desactivar una cuenta.  
* Cada empleado debe tener un rol definido.  
* Los roles disponibles deben corresponder a los establecidos por el sistema.

### **HU03 – Restablecimiento de contraseña** {#hu03-–-restablecimiento-de-contraseña}

**Como** administrador, **quiero** cambiar la contraseña de un empleado, **para** permitirle recuperar el acceso cuando sea necesario.

**Criterios de aceptación:**

* El administrador debe poder seleccionar la cuenta correspondiente.  
* Debe existir una opción para iniciar el cambio de contraseña.  
* La nueva contraseña debe almacenarse utilizando un mecanismo seguro.  
* La contraseña no debe almacenarse directamente en texto plano.  
* El empleado debe poder autenticarse utilizando la nueva contraseña.

### **HU04 – Administración de clientes** {#hu04-–-administración-de-clientes}

**Como** administrador, **quiero** registrar y mantener los datos de los clientes, **para** disponer de información actualizada sobre las personas que poseen servicios contratados.

**Criterios de aceptación:**

* Se debe poder registrar un nuevo cliente.  
* Se debe poder consultar la información registrada.  
* Se debe poder modificar la información del cliente.  
* La información del cliente debe conservar sus relaciones con los servicios contratados.  
* Un cliente debe poder existir sin disponer de una cuenta de acceso al sistema.

### **HU05 – Administración de servicios** {#hu05-–-administración-de-servicios}

**Como** administrador, **quiero** administrar los servicios contratados por los clientes, **para** mantener actualizados los planes y sus condiciones de cobro.

**Criterios de aceptación:**

* Debe ser posible asociar un servicio con un cliente.  
* Debe ser posible consultar el servicio asociado.  
* Debe ser posible modificar la información correspondiente al servicio.  
* Debe ser posible establecer o actualizar el precio vigente.  
* Los cambios posteriores de precio no deben alterar los valores almacenados en pagos anteriores.

### **HU06 – Asignación de clientes a cobradores** {#hu06-–-asignación-de-clientes-a-cobradores}

**Como** administrador, **quiero** relacionar clientes con cobradores, **para** distribuir las responsabilidades de cobro entre los empleados.

**Criterios de aceptación:**

* El administrador debe poder seleccionar un cliente.  
* Debe poder seleccionar un cobrador.  
* Debe poder registrar la asignación.  
* Debe poder consultar las asignaciones existentes.  
* El cobrador debe visualizar solamente los clientes que le correspondan.

### **HU07 – Consulta de clientes asignados** {#hu07-–-consulta-de-clientes-asignados}

**Como** cobrador, **quiero** consultar los clientes que tengo bajo mi responsabilidad, **para** contar con la información necesaria para gestionar sus pagos.

**Criterios de aceptación:**

* El cobrador debe visualizar su listado de clientes asignados.  
* Debe poder consultar la información básica de cada cliente.  
* Debe poder consultar los servicios relacionados con cada cliente.  
* No debe poder acceder mediante este módulo a clientes asignados a otros cobradores.

### **HU08 – Registro de pagos** {#hu08-–-registro-de-pagos}

**Como** cobrador, **quiero** registrar los pagos efectuados por los clientes, **para** mantener actualizado el estado de sus obligaciones.

**Criterios de aceptación:**

* El cobrador debe poder seleccionar un cliente que tenga asignado.  
* Debe poder seleccionar el servicio correspondiente.  
* Debe poder ingresar el monto recibido.  
* Debe poder indicar la fecha en que se efectuó el pago.  
* Debe poder indicar el período al que corresponde el pago.  
* Debe poder agregar observaciones cuando sea necesario.  
* El sistema debe determinar el estado correspondiente según la información registrada.  
* La operación debe conservar el usuario que realizó el registro.  
* La operación debe conservar la fecha de registro.  
* El sistema debe generar la información correspondiente para auditoría.

### **HU09 – Registro de pagos parciales** {#hu09-–-registro-de-pagos-parciales}

**Como** cobrador, **quiero** registrar pagos cuyo monto sea inferior al valor total del período, **para** conservar correctamente los abonos efectuados por los clientes.

**Criterios de aceptación:**

* El sistema debe aceptar montos inferiores al valor total esperado.  
* Debe conservarse el monto efectivamente recibido.  
* El pago debe quedar relacionado con el período correspondiente.  
* El sistema debe reflejar que el período aún no ha sido cubierto completamente.  
* El registro debe conservar el valor del servicio correspondiente al período.

### **HU10 – Registro de pagos adelantados** {#hu10-–-registro-de-pagos-adelantados}

**Como** cobrador, **quiero** registrar pagos correspondientes a períodos posteriores, **para** mantener correctamente la información de los pagos realizados anticipadamente.

**Criterios de aceptación:**

* Debe ser posible seleccionar un período futuro.  
* Debe ser posible indicar el monto recibido.  
* El pago debe quedar asociado al período seleccionado.  
* El historial debe reflejar que dicho período fue cancelado anticipadamente.  
* El registro debe conservar el valor correspondiente al servicio y período.

### **HU11 – Consulta del historial de pagos** {#hu11-–-consulta-del-historial-de-pagos}

**Como** cobrador, **quiero** consultar los pagos registrados de mis clientes, **para** verificar su historial y conocer el estado de sus períodos.

**Criterios de aceptación:**

* El cobrador debe poder consultar el historial de sus clientes asignados.  
* Cada registro debe mostrar la fecha efectiva del pago.  
* Debe mostrar el período relacionado.  
* Debe mostrar el monto registrado.  
* Debe mostrar la información necesaria del servicio.  
* Debe identificar al usuario que realizó el registro cuando corresponda.

### **HU12 – Generación de recibos** {#hu12-–-generación-de-recibos}

**Como** cobrador, **quiero** generar un recibo a partir de un pago registrado, **para** entregar al cliente un comprobante de la transacción.

**Criterios de aceptación:**

* El recibo debe estar relacionado con un pago existente.  
* Debe contener los datos principales del cliente.  
* Debe incluir la información del servicio.  
* Debe indicar el período correspondiente.  
* Debe mostrar el monto pagado.  
* Debe incluir la fecha del pago.  
* El sistema debe generar el recibo inicialmente en formato PDF.

### **HU13 – Consulta de auditoría** {#hu13-–-consulta-de-auditoría}

**Como** administrador, **quiero** revisar las operaciones realizadas dentro del sistema, **para** disponer de trazabilidad sobre las acciones ejecutadas por los usuarios.

**Criterios de aceptación:**

* El administrador debe poder consultar los registros de auditoría.  
* Cada registro debe identificar al usuario que realizó la acción.  
* Debe registrarse la fecha y hora de la operación.  
* Debe identificarse la acción ejecutada.  
* Debe poder relacionarse la operación con el registro afectado cuando corresponda.  
* Los registros de auditoría no deben poder ser modificados por los cobradores.

### **HU14 – Generación de reportes** {#hu14-–-generación-de-reportes}

**Como** administrador, **quiero** consultar información mediante reportes, **para** obtener una visión organizada de los datos registrados en el sistema.

**Criterios de aceptación:**

* El administrador debe poder seleccionar el reporte que desea consultar.  
* Debe poder utilizar los filtros disponibles.  
* El sistema debe presentar los resultados correspondientes.  
* La información mostrada debe provenir de los datos actualizados del sistema.  
* Los resultados deben respetar los criterios de filtrado seleccionados.


### **HU15 – Exportación de reportes** {#hu15-–-exportación-de-reportes}

**Como** administrador, **quiero** exportar la información obtenida mediante los reportes, **para** disponer de los datos en formatos que puedan ser almacenados o utilizados externamente.

**Criterios de aceptación:**

* El administrador debe poder solicitar la exportación de un reporte.  
* Debe estar disponible la generación de archivos PDF.  
* Debe estar disponible la generación de archivos Excel (.xlsx).  
* El archivo generado debe contener la información resultante de los filtros aplicados.  
* Los datos exportados deben corresponder a la consulta realizada.

### **HU16 – Importación de información histórica** {#hu16-–-importación-de-información-histórica}

**Como** administrador, **quiero** cargar información histórica mediante archivos Excel, **para** incorporar al sistema los datos existentes de la empresa.

**Criterios de aceptación:**

* El administrador debe poder seleccionar un archivo Excel.  
* El sistema debe verificar que el archivo tenga la estructura esperada.  
* Los datos deben validarse antes de ser almacenados.  
* Los registros que cumplan las validaciones deben poder incorporarse.  
* Los registros que presenten errores deben ser identificados.  
* La importación no debe generar información incompleta o inconsistente.

### **HU17 – Respaldo y recuperación de información** {#hu17-–-respaldo-y-recuperación-de-información}

**Como** administrador, **quiero** disponer de mecanismos de respaldo y recuperación, **para** reducir el impacto de posibles fallos o pérdidas de información.

**Criterios de aceptación:**

* La información de la base de datos debe contar con mecanismos de respaldo.  
* Los respaldos deben contener la información necesaria para recuperar el sistema.  
* Debe existir un procedimiento definido para restaurar la información.  
* El procedimiento de recuperación debe ser validado durante el desarrollo.

### **HU18 – Despliegue del frontend** {#hu18-–-despliegue-del-frontend}

**Como** desarrollador, **quiero** publicar el frontend Angular en la infraestructura definida, **para** disponer de una versión accesible del sistema desde un ambiente de pruebas.

**Criterios de aceptación:**

* El proyecto Angular debe compilar sin errores.  
* Los archivos generados deben poder ser publicados en Amazon S3.  
* El frontend debe estar disponible mediante HTTPS.  
* La aplicación debe poder comunicarse correctamente con la API desplegada.

### **HU19 – Despliegue del backend** {#hu19-–-despliegue-del-backend}

**Como** desarrollador, **quiero** desplegar la API Spring Boot en la infraestructura Cloud, **para** proporcionar los servicios requeridos por el frontend.

**Criterios de aceptación:**

* El proyecto backend debe compilar correctamente.  
* La aplicación Spring Boot debe ejecutarse correctamente en Amazon EC2.  
* La API debe aceptar solicitudes provenientes del frontend.  
* La comunicación externa debe realizarse mediante HTTPS.  
* El backend debe poder conectarse con la base de datos PostgreSQL administrada mediante Amazon RDS.

### **HU20 – Integración y despliegue continuo** {#hu20-–-integración-y-despliegue-continuo}

**Como** desarrollador, **quiero** automatizar la validación, construcción y despliegue del proyecto mediante GitHub Actions, **para** hacer más consistente el proceso de integración de cambios.

**Criterios de aceptación:**

* Los cambios enviados al repositorio deben activar el flujo correspondiente.  
* El pipeline debe ejecutar las etapas de construcción configuradas.  
* Los errores encontrados durante el proceso deben quedar identificados.  
* La ejecución del pipeline debe conservar su resultado en GitHub Actions.  
* El despliegue debe ejecutarse únicamente cuando se cumplan las condiciones establecidas para el ambiente correspondiente.

## 

## 2\. Casos de uso {#2.-casos-de-uso}

## Módulo 1: Autenticación {#módulo-1:-autenticación}

### CU-01 — Iniciar sesión {#cu-01-—-iniciar-sesión}

| Campo | Detalle |
| ----- | ----- |
| Actor(es) | Administrador, Cobrador |
| Precondición | El usuario tiene una cuenta con tp\_status \= Activo en app\_user |
| Postcondición | Se genera un token\_credential válido y se redirige al Dashboard |

**Flujo principal**

1. El usuario ingresa correo y contraseña.  
2. El sistema valida las credenciales contra app\_user y verifica tp\_status \= Activo.  
3. El sistema genera un token\_credential y actualiza last\_login\_at.  
4. Se redirige al Dashboard.

**Variantes:** credenciales inválidas o usuario inactivo → se rechaza el acceso con el mensaje correspondiente, sin generar token.

### CU-02 — Cerrar sesión {#cu-02-—-cerrar-sesión}

| Campo | Detalle |
| ----- | ----- |
| Actor(es) | Administrador, Cobrador |
| Precondición | El usuario tiene una sesión activa |
| Postcondición | El token\_credential vigente queda invalidado |

**Flujo principal**

1. El usuario selecciona "Cerrar sesión".  
2. El sistema invalida el token\_credential y redirige al login.

## 

## Módulo 2: Dashboard {#módulo-2:-dashboard}

### CU-03 — Ver dashboard {#cu-03-—-ver-dashboard}

| Campo | Detalle |
| ----- | ----- |
| Actor(es) | Administrador, Cobrador |
| Incluye | CU-15, CU-08, CU-12 |
| Precondición | Sesión activa |
| Postcondición | Se muestran KPIs y los últimos eventos de auditoría |

**Flujo principal**

1. El usuario accede al Dashboard.  
2. El sistema valida la sesión (CU-15).  
3. El sistema calcula KPIs a partir de app\_payment y muestra el resumen de pagos recientes (CU-08).  
4. El sistema muestra los últimos 5 eventos de auditoría (CU-12).

## Módulo 3: Clientes {#módulo-3:-clientes}

### CU-04 — Consultar clientes {#cu-04-—-consultar-clientes}

| Campo | Detalle |
| ----- | ----- |
| Actor(es) | Administrador, Cobrador |
| Incluye | CU-15 |
| Precondición | Sesión activa |
| Postcondición | Se muestra el listado de app\_customer con plan, estado y cobrador asignado |

**Flujo principal**

1. El usuario accede a la vista "Clientes".  
2. El sistema valida la sesión (CU-15) y lista los clientes con su plan vigente (app\_subscription).

**Variantes:** el usuario puede acotar el listado escribiendo un término de búsqueda o aplicando un filtro (estado, plan, cobrador); es una interacción sobre el mismo listado, no un caso de uso distinto.

### CU-05 — Gestionar cliente {#cu-05-—-gestionar-cliente}

| Campo | Detalle |
| ----- | ----- |
| Actor(es) | Administrador |
| Incluye | CU-16 |
| Precondición | Sesión activa como Administrador |
| Postcondición | El cliente queda creado, editado, con nuevo cobrador asignado, o con su estado cambiado; queda un evento de auditoría |

**Descripción.** Cubre todo el ciclo de mantenimiento de un cliente: alta, edición de datos, reasignación de cobrador y cambio de estado (suspender/reactivar). Se agrupan porque comparten el mismo formulario y el mismo registro de auditoría — no justifican ser cuatro casos de uso separados.

**Flujo principal (alta de cliente)**

1. El Administrador abre "Nuevo cliente" e ingresa nombre, contacto, email, teléfono y plan.  
2. El sistema valida que el email no esté en uso por otro cliente activo.  
3. El sistema crea el registro en app\_customer (tp\_status \= Activo) junto con su app\_subscription inicial.  
4. El sistema registra el evento de auditoría (CU-16).

**Flujos alternos**

* **Editar datos:** el Administrador modifica nombre/contacto/email/teléfono de un cliente existente → se actualiza app\_customer y se registra auditoría.  
* **Reasignar cobrador:** dentro de la edición, se cambia collector\_user\_id a otro app\_user con rol Cobrador.  
* **Cambiar estado:** el Administrador suspende (tp\_status \= Suspendido) o reactiva (tp\_status \= Activo) al cliente → se registra auditoría con la acción correspondiente ("Suspendió cliente" / "Reactivó cliente").  
* **Email duplicado** (en alta): se rechaza con error, no se crea el registro.

## 

## 

## Módulo 4: Servicios {#módulo-4:-servicios}

### CU-06 — Consultar servicios {#cu-06-—-consultar-servicios}

| Campo | Detalle |
| ----- | ----- |
| Actor(es) | Administrador, Cobrador |
| Incluye | CU-15 |
| Precondición | Sesión activa |
| Postcondición | Se muestra el listado de planes (app\_service) con velocidad, precio y cantidad de clientes |

**Flujo principal**

1. El usuario accede a la vista "Servicios".  
2. El sistema valida la sesión (CU-15) y lista los planes con su tipo, estado y conteo de suscripciones activas.

### CU-07 — Gestionar servicio {#cu-07-—-gestionar-servicio}

| Campo | Detalle |
| ----- | ----- |
| Actor(es) | Administrador |
| Incluye | CU-16 |
| Precondición | Sesión activa como Administrador |
| Postcondición | El plan queda creado, editado o descontinuado; queda un evento de auditoría |

**Flujo principal (alta de servicio)**

1. El Administrador ingresa nombre, velocidad, precio mensual y tipo de plan.  
2. El sistema crea el registro en app\_service con tp\_status \= Activo.  
3. El sistema registra el evento de auditoría (CU-16).

**Flujos alternos**

* **Editar:** se modifica velocidad, precio o tipo de un plan existente.  
* **Descontinuar:** se cambia tp\_status a Descontinuado; los clientes con suscripción activa a ese plan no se ven afectados retroactivamente.

## Módulo 5: Pagos {#módulo-5:-pagos}

### CU-08 — Consultar pagos {#cu-08-—-consultar-pagos}

| Campo | Detalle |
| ----- | ----- |
| Actor(es) | Administrador, Cobrador |
| Incluye | CU-15 |
| Precondición | Sesión activa |
| Postcondición | Se muestra el listado de app\_payment con cliente, plan, monto, método y estado |

**Flujo principal**

1. El usuario accede a la vista "Pagos".  
2. El sistema valida la sesión (CU-15) y lista los pagos uniendo app\_customer, app\_subscription y app\_service.

### CU-09 — Registrar pago {#cu-09-—-registrar-pago}

| Campo | Detalle |
| ----- | ----- |
| Actor(es) | Cobrador, Administrador, Sistema |
| Incluye | CU-16 |
| Precondición | El cliente tiene una app\_subscription activa |
| Postcondición | Se crea un registro en app\_payment; si fue exitoso, se emite un recibo; si falló, queda registrado como error |

**Descripción.** Cubre tanto el registro manual (Cobrador recibe efectivo/transferencia/tarjeta) como el automático (Sistema, ej. débito o pasarela).

**Flujo principal**

1. El actor inicia el registro de un pago para un cliente, con base en su app\_subscription vigente.  
2. Se ingresa/recibe el monto y método (Efectivo, Transferencia, Tarjeta).  
3. El sistema crea el registro en app\_payment.  
4. El sistema registra el evento de auditoría (CU-16): "Registró pago" o "Pago fallido" según el resultado.

**Flujos alternos**

* **Pago exitoso:** tp\_status \= Completado → el sistema genera automáticamente un app\_receipt asociado (1:1 con el pago), disponible de inmediato en "Recibos" (CU-10).  
* **Pago fallido:** tp\_status \= Fallido (fondos insuficientes, tarjeta declinada, etc.) → no se genera recibo; el evento de auditoría queda con tp\_level \= error.

## Módulo 6: Recibos {#módulo-6:-recibos}

### CU-10 — Consultar recibos {#cu-10-—-consultar-recibos}

| Campo | Detalle |
| ----- | ----- |
| Actor(es) | Administrador, Cobrador |
| Incluye | CU-15 |
| Precondición | Sesión activa |
| Postcondición | Se muestra el listado de app\_receipt con pago de referencia, cliente, monto y período |

**Flujo principal**

1. El usuario accede a la vista "Recibos".  
2. El sistema valida la sesión (CU-15) y lista los recibos uniendo app\_payment y app\_customer.

**Variantes:** el usuario puede descargar o imprimir cualquier recibo del listado; es una acción sobre un elemento ya consultado, no un caso de uso aparte.

## Módulo 7: Reportes {#módulo-7:-reportes}

### CU-11 — Generar y exportar reporte de cobros {#cu-11-—-generar-y-exportar-reporte-de-cobros}

| Campo | Detalle |
| ----- | ----- |
| Actor(es) | Administrador |
| Incluye | CU-15, CU-16 (al exportar) |
| Precondición | Sesión activa como Administrador |
| Postcondición | Se muestra un reporte agregado; si se exporta, se genera un PDF y queda un evento de auditoría |

**Flujo principal**

1. El Administrador selecciona un rango de fechas en la vista "Reportes".  
2. El sistema valida la sesión (CU-15) y agrega los datos de app\_payment (totales, por plan, por cobrador).  
3. El Administrador puede exportar el reporte a PDF → el sistema registra el evento de auditoría (CU-16): "Exportó reporte".

## Módulo 8: Auditoría {#módulo-8:-auditoría}

### CU-12 — Consultar bitácora de auditoría {#cu-12-—-consultar-bitácora-de-auditoría}

| Campo | Detalle |
| ----- | ----- |
| Actor(es) | Administrador |
| Incluye | CU-15 |
| Precondición | Sesión activa como Administrador |
| Postcondición | Se muestra el listado de app\_audit\_log con usuario, acción, detalle, IP y nivel |

**Flujo principal**

1. El Administrador accede a la vista "Auditoría".  
2. El sistema valida la sesión (CU-15) y lista los eventos, más reciente primero.

**Variantes:** el Administrador puede filtrar el listado por usuario (o "sistema"); es una interacción sobre el mismo listado.

## Módulo 9: Usuarios {#módulo-9:-usuarios}

### CU-13 — Consultar usuarios {#cu-13-—-consultar-usuarios}

| Campo | Detalle |
| ----- | ----- |
| Actor(es) | Administrador |
| Incluye | CU-15, CU-12 (al ver historial de un usuario) |
| Precondición | Sesión activa como Administrador |
| Postcondición | Se muestra el listado de app\_user con rol, estado y último acceso |

**Flujo principal**

1. El Administrador accede a la vista "Usuarios".  
2. El sistema valida la sesión (CU-15) y lista los usuarios con su rol y estado.  
3. Al seleccionar un usuario, el sistema incluye la consulta de auditoría (CU-12) filtrada por ese usuario, mostrando su historial de actividad (equivalente al auditoriaPorUsuario del prototipo).

### CU-14 — Gestionar usuario {#cu-14-—-gestionar-usuario}

| Campo | Detalle |
| ----- | ----- |
| Actor(es) | Administrador |
| Incluye | CU-16 |
| Precondición | Sesión activa como Administrador |
| Postcondición | El usuario queda creado, editado o desactivado; queda un evento de auditoría |

**Flujo principal (alta de usuario)**

1. El Administrador ingresa nombre, email y rol (Administrador/Cobrador).  
2. El sistema valida que el email no esté en uso por otro usuario activo.  
3. El sistema crea el registro en app\_user (tp\_status \= Activo) y registra el evento de auditoría (CU-16): "Creó usuario".

**Flujos alternos**

* **Editar:** se modifica nombre, email o rol de un usuario existente.  
* **Desactivar:** tp\_status \= Inactivo e invalida cualquier token\_credential vigente de ese usuario.

## Módulo 10: Casos de soporte (uso interno) {#módulo-10:-casos-de-soporte-(uso-interno)}

### CU-15 — Validar sesión activa {#cu-15-—-validar-sesión-activa}

| Campo | Detalle |
| ----- | ----- |
| Incluido por | CU-03, CU-04, CU-06, CU-08, CU-10, CU-11, CU-12, CU-13 |
| Precondición | Existe un token en la solicitud |
| Postcondición | Se confirma o rechaza el acceso |

**Flujo principal**

1. El sistema recibe el token y verifica en token\_credential que expiration\_date \> CURRENT\_TIMESTAMP.  
2. Si es válido, continúa el caso de uso que lo incluyó; si no, redirige a CU-01.

### CU-16 — Registrar evento de auditoría {#cu-16-—-registrar-evento-de-auditoría}

| Campo | Detalle |
| ----- | ----- |
| Incluido por | CU-05, CU-07, CU-09, CU-11, CU-14 |
| Precondición | Ocurrió una acción mutable en el sistema |
| Postcondición | Se crea un registro en app\_audit\_log |

**Flujo principal**

1. El caso de uso que lo incluye envía: user\_id (o NULL si es del Sistema), action, detail, ip\_address y tp\_level.  
2. El sistema inserta el registro en app\_audit\_log.

## 

## 3\. Arquitectura de Software y Estructura de Paqueterías {#3.-arquitectura-de-software-y-estructura-de-paqueterías}

## **3.1 Arquitectura General**

NetPay Manager está compuesto por una arquitectura cliente-servidor dividida principalmente en tres componentes: **Frontend**, **Backend** y **Base de Datos**.

El frontend está desarrollado con **Angular 19**, utilizando componentes standalone y TypeScript estricto. Su responsabilidad principal es proporcionar la interfaz de usuario, gestionar la navegación, capturar las acciones realizadas por los usuarios y comunicarse con el backend mediante servicios HTTP.

El backend está desarrollado con **Java 17 y Spring Boot 4.1.1** y concentra la lógica de negocio, los casos de uso, la autenticación, la autorización, el acceso a datos y la exposición de los servicios REST.

La persistencia utiliza **PostgreSQL**, mientras que **Flyway** se encarga del control de versiones y evolución del esquema de la base de datos.

## **3.2 Arquitectura del Backend**

El backend de NetPay Manager utiliza una **arquitectura por capas basada en principios de Clean Architecture**.

La arquitectura establece una separación clara entre la lógica de negocio y los detalles relacionados con frameworks, persistencia y comunicación HTTP.

La regla de dependencia establece que la infraestructura depende de las capas internas, mientras que el dominio permanece independiente de la aplicación y de la infraestructura.

| Capa | Paquete | Clases |
| ----- | ----- | ----- |
| domain | `domain/entity` | `AppUser`, `AppService`, `AdmTypology`, `TokenCredential` |
| domain | `domain/repository` | `AppUserRepository`, `AppServiceRepository`, `AdmTypologyRepository`, `TokenCredentialRepository` |
| domain | `domain/service` | `PasswordHashService`, `TokenService`, `SecurityContext` |
| domain | `domain/service/model` | `TokenPayload`, `SecurityPrincipal` |
| domain | `domain/dto` | `QueryDto`, `AppUserQueryDto`, `AppServiceQueryDto`, `PaginatedResult<T>` |
| domain | `domain/exception` | `DomainException`, `EntityNotFoundException`, `EntityAlreadyExistsException`, `InvalidCredentialsException`, `UnauthorizeException` |
| domain | `domain/enums` | `TypologyEnum` |
| application | `application/use_case` | `LoginUseCase`, `TokenCredentialUseCase`, `AppUserUseCase`, `AppServiceUseCase`, `AdmTypologyUseCase` |
| application | `application/model` | `LoginRequest`, `LoginResponse` |
| infrastructure | `infrastructure/db/entity` | `AppUserEntity`, `AppServiceEntity`, `AdmTypologyEntity`, `TokenCredentialEntity` |
| infrastructure | `infrastructure/db` | `AppUserJpaRepository`, `AppServiceJpaRepository`, `AdmTypologyJpaRepository`, `TokenCredentialJpaRepository`, `DbAppUserRepository`, `DbAppServiceRepository`, `DbAdmTypologyRepository`, `DbTokenCredentialRepository` |
| infrastructure | `infrastructure/http/controller` | `AuthController`, `AppUserController`, `AppServiceController`, `AdmTypologyController` |
| infrastructure | `infrastructure/http/dto` | `AppUserResponseDto`, `AppServiceResponseDto` |
| infrastructure | `infrastructure/http/mapper` | `HttpMapper` |
| infrastructure | `infrastructure/http/middleware` | `GlobalExceptionMiddleware` |
| infrastructure | `infrastructure/http/filter` | `JwtAuthFilter` (huérfano, ver [§4](https://docs.google.com/document/d/1frQt0skwTRl1SjrMjjWg769B4f9edFeEKCQfZrtLwjU/edit#4-estado-actual-pendientes-wip)) |
| infrastructure | `infrastructure/security` | `SecurityConfig`, `JwtAuthenticationFilter`, `SpringSecurityContext` |
| infrastructure | `infrastructure/service` | `JwtTokenService`, `BcryptHashService` |
| infrastructure | `infrastructure/config` | `BeanConfig` |
| main | raíz | `NetPayBackendApplication` |

## **3.3 Arquitectura del Frontend**

El frontend de NetPay Manager está desarrollado con **Angular 19**, utilizando componentes standalone, TypeScript estricto, Angular CLI y SCSS.

La aplicación no utiliza `NgModules`. Los componentes son standalone y declaran sus dependencias mediante `imports`.

## 4\. Modelado Técnico del Sistema {#4.-modelado-técnico-del-sistema}

## 4.1 Diagramas {#4.1-diagramas}

El backend de **NetPay Manager** está organizado mediante una arquitectura por capas basada en principios de Clean Architecture. La estructura separa las responsabilidades del dominio, la aplicación y la infraestructura, manteniendo al dominio independiente de frameworks y tecnologías externas.

La capa **Domain** concentra las entidades y reglas propias del negocio, incluyendo entidades como AppUser y AdmTypology, contratos de repositorio, servicios de dominio, DTOs, enumeraciones y excepciones. La capa **Application** se encarga de la orquestación de los casos de uso mediante clases \*UseCase, además de los modelos de entrada y salida utilizados por dichos casos de uso. Finalmente, la capa **Infrastructure** contiene los adaptadores necesarios para exponer la API HTTP, acceder a PostgreSQL mediante JPA/Hibernate, implementar la seguridad con Spring Security y JWT, y realizar la configuración de la aplicación.

La comunicación principal de una operación sigue el flujo:

Controller → UseCase → Repository de dominio → Db\*Repository → PostgreSQL

Las implementaciones de persistencia (\*Entity, \*JpaRepository y Db\*Repository) pertenecen a infraestructura, mientras que las entidades de dominio permanecen libres de anotaciones de Spring y JPA. Esta separación permite mantener las reglas de negocio independientes de la tecnología utilizada para persistir o exponer la información.

En el frontend, la aplicación utiliza Angular 19 con componentes standalone y una organización por funcionalidades. Las funcionalidades se cargan mediante lazy loading y utilizan servicios de API para comunicarse con el backend. Los guards funcionales controlan la navegación y los interceptores HTTP gestionan aspectos transversales como el estado global de carga y las respuestas no autorizadas.

### Componentes principales representados {#componentes-principales-representados}

* **Backend**  
  * Entidades de dominio.  
  * Interfaces de repositorio.  
  * Servicios de dominio.  
  * Casos de uso.  
  * Modelos de aplicación.  
  * Controladores REST.  
  * DTOs HTTP.  
  * Mappers.  
  * Repositorios JPA e implementaciones de infraestructura.  
  * Componentes de seguridad y autenticación JWT.  
  * Middleware global para el manejo de excepciones.  
* **Frontend**  
  * Componentes standalone.  
  * Páginas agrupadas por feature.  
  * Rutas con lazy loading.  
  * Servicios de API.  
  * Guards funcionales.  
  * Interceptores HTTP.  
  * Modelos y DTOs.  
  * Servicios reutilizables.  
  * Layout y componentes comunes.


## 4.2 Modelo Entidad-Relación (E/R) Finalizado {#4.2-modelo-entidad-relación-(e/r)-finalizado}

El sistema utiliza **PostgreSQL** como motor de persistencia y **Spring Data JPA / Hibernate** como mecanismo de acceso desde el backend. La estructura persistente se administra mediante migraciones de Flyway, mientras que Hibernate utiliza ddl-auto=validate para comprobar que las entidades JPA sean compatibles con el esquema existente.

El modelo E/R debe representar las entidades persistentes del sistema, sus relaciones, llaves primarias y foráneas, tipos de datos y restricciones de integridad. La separación entre las entidades de dominio y las entidades JPA permite que la representación utilizada por la base de datos permanezca dentro de la capa de infraestructura.

Un aspecto importante de la persistencia es el manejo de estados mediante tipologías. Los registros no se eliminan físicamente: las operaciones de eliminación se realizan mediante un cambio de estado hacia DELETED. Los repositorios y casos de uso deben excluir los registros que se encuentren en dicho estado al realizar las consultas normales.

Además, los identificadores internos de base de datos no se exponen directamente a través de la API. Las operaciones HTTP utilizan hashId como identificador público en URLs y respuestas, manteniendo separados los identificadores internos de persistencia de los identificadores utilizados por los consumidores de la API.

### Consideraciones de integridad {#consideraciones-de-integridad}

* Las llaves primarias identifican de forma única cada registro persistente.  
* Las llaves foráneas mantienen la integridad entre entidades relacionadas.  
* Los estados permiten implementar eliminación lógica mediante DELETED.  
* Las migraciones de Flyway constituyen la fuente de control de versiones del esquema.  
* Una migración aplicada no debe modificarse ni reutilizarse; cualquier cambio posterior debe implementarse mediante una nueva versión.  
* Hibernate valida el esquema existente en lugar de modificarlo automáticamente.


## 4.3 Modelos de Procesos Técnicos Completos {#4.3-modelos-de-procesos-técnicos-completos}

Los procesos técnicos del sistema describen la interacción entre frontend, API, casos de uso, persistencia y mecanismos de seguridad. La arquitectura busca mantener los controladores delgados, delegando la lógica de negocio a los casos de uso y el acceso a datos a los contratos de repositorio y sus implementaciones.

### Proceso Técnico A: Autenticación de Usuarios y Control de Roles {#proceso-técnico-a:-autenticación-de-usuarios-y-control-de-roles}

El proceso de autenticación comienza en el frontend Angular, donde AuthService prepara la solicitud de autenticación y obtiene el fingerprint del dispositivo. El frontend utiliza el endpoint correspondiente del backend para enviar las credenciales y el fingerprint dentro del modelo de autenticación.

En el backend, la solicitud es recibida por el controlador HTTP y delegada al caso de uso de autenticación. El caso de uso utiliza los contratos del dominio y sus implementaciones de infraestructura para consultar la información del usuario y validar las credenciales.

La autenticación utiliza **JWT** y Spring Security. Una vez validada la autenticación, el backend genera el token correspondiente y devuelve la respuesta al frontend. El cliente almacena el token utilizando la clave definida por AppStorageKeys.AUTH y mantiene el perfil del usuario mediante AppStorageKeys.PROFILE.

El control de acceso y navegación del frontend se apoya en guards funcionales. Las rutas protegidas quedan detrás de los mecanismos de navegación definidos por la aplicación, mientras que las solicitudes HTTP utilizan el token almacenado para acceder a los recursos protegidos.

### Proceso Técnico B: Gestión de pagos y conservación del historial

El flujo de pagos debe documentar la operación desde la captura de los datos en el frontend hasta la persistencia de la transacción en PostgreSQL. El frontend utiliza servicios de API para enviar la información al backend, mientras que el backend delega la operación a un caso de uso encargado de aplicar las reglas correspondientes.

Para el escenario de pagos parciales, completos y adelantados, el proceso debe considerar el valor del servicio correspondiente al período que se está registrando. De esta forma, una modificación posterior del precio no debe reinterpretar automáticamente operaciones históricas que ya fueron registradas.

El proceso técnico puede dividirse en las siguientes etapas:

1. El usuario autorizado registra desde el frontend la información correspondiente al pago y al período.  
2. El frontend valida la información disponible y envía la solicitud HTTP al backend.  
3. El controlador recibe la solicitud y la delega al caso de uso correspondiente.  
4. El caso de uso obtiene la información necesaria para determinar el valor aplicable al período de la operación.  
5. Se determina el estado o resultado del pago de acuerdo con las reglas de negocio implementadas.  
6. La operación se persiste mediante el repositorio de dominio y su implementación en infraestructura.  
7. PostgreSQL conserva la información resultante para mantener la trazabilidad de la operación.  
8. El backend devuelve al frontend el resultado de la operación mediante el DTO HTTP correspondiente.  
9. El frontend actualiza la interfaz y presenta el resultado al usuario.

## 

## 5\. Gestión Evolutiva de Datos (Migraciones) {#5.-gestión-evolutiva-de-datos-(migraciones)}

## 1\. Configuración de Flyway {#1.-configuración-de-flyway}

Definida en src/main/resources/application.properties:

| Propiedad | Valor | Efecto |
| ----- | ----- | ----- |
| spring.flyway.enabled=true | Habilitado | Las migraciones corren al arrancar Spring Boot |
| spring.flyway.clean-disabled=true | Borrado deshabilitado | **Prohibido** hacer drop del esquema desde la app |
| spring.flyway.locations=classpath:db/migration | Ubicación | Scripts en src/main/resources/db/migration/ |
| spring.jpa.hibernate.ddl-auto=validate | Validación | Hibernate valida que las entidades JPA (\*Entity) coincidan con el esquema real |

**Consecuencia:** toda entidad JPA añadida/modificada debe ir **acompañada** de su migración. Si no coinciden, la app no arranca (validate falla).

## 2\. Convenciones y numeración (V\<n\>\_\_\<descripcion\>.sql) {#2.-convenciones-y-numeración-(v<n>__<descripcion>.sql)}

* Los scripts viven en src/main/resources/db/migration/.  
* Formato de nombre: V\<n\>\_\_\<description\>.sql, donde \<n\> es **monotónico** y nunca se reutiliza. Ejemplos: V1\_\_initial\_tables.sql, V4\_\_app\_service\_table.sql.  
* **Nunca modificar ni reescribir una migración ya aplicada** (una DB que ya la ejecutó vería checksum distinto y Flyway fallaría). Si hay que cambiar algo, se crea V\<n+1\>.  
* Los scripts deben ser **idempotentes en intención**: pensados para ejecutarse una sola vez en orden.  
* Antes de un commit: ./mvnw clean compile para verificar que las entidades siguen siendo coherentes con el esquema.

## 3\. Migraciones actuales {#3.-migraciones-actuales}

| Versión | Archivo | Contenido |
| ----- | ----- | ----- |
| **V1** | V1\_\_initial\_tables.sql | Crea las tablas base del sistema |
| **V2** | V2\_\_typologies.sql | Puebla adm\_typology con las tipologías del sistema (datos maestros) |
| **V3** | V3\_\_add\_root\_user.sql | Inserta el usuario administrador raíz (dato semilla) |
| **V4** | V4\_\_app\_service\_table.sql | Crea la tabla app\_service (planes/servicios de internet) |

### V1 — Tablas base {#v1-—-tablas-base}

**adm\_typology** (tipologías reutilizables: estados, tipos, roles)

| Columna | Tipo | Restricciones |
| ----- | ----- | ----- |
| typology\_id | BIGINT | PRIMARY KEY — id real de la fila |
| internal\_id | BIGINT NOT NULL | UNIQUE — id lógico/público usado en la app (uq\_adm\_typology\_internal\_id) |
| parent\_typology\_id | BIGINT | FK → adm\_typology(typology\_id) — agrupación jerárquica (ej. "Status", "Service\_Types") |
| description | VARCHAR(150) NOT NULL | Nombre legible |
| value\_1, value\_2 | VARCHAR(255) | Valores auxiliares (ej. código ADMIN) |

**app\_user** (usuarios del sistema)

| Columna | Tipo | Restricciones |
| ----- | ----- | ----- |
| id | BIGSERIAL | PRIMARY KEY |
| hash\_id | VARCHAR(50) NOT NULL | UNIQUE — identificador público en URLs/respuestas |
| full\_name | VARCHAR(150) NOT NULL |  |
| email | VARCHAR(150) NOT NULL | UNIQUE |
| password\_hash | VARCHAR(255) NOT NULL | Hash BCrypt |
| tp\_role | BIGINT NOT NULL | FK → adm\_typology(typology\_id) |
| tp\_status | BIGINT NOT NULL | FK → adm\_typology(typology\_id) |
| last\_login\_at | TIMESTAMP | Último acceso |
| created\_at, updated\_at | TIMESTAMP NOT NULL | DEFAULT CURRENT\_TIMESTAMP |
| deleted\_at | TIMESTAMP | Soft-delete |

**token\_credential** (sesiones/tokens emitidos)

| Columna | Tipo | Restricciones |
| ----- | ----- | ----- |
| id | BIGSERIAL | PRIMARY KEY |
| user\_id | BIGINT NOT NULL | FK → app\_user(id) ON DELETE CASCADE |
| token | VARCHAR(750) NOT NULL | UNIQUE |
| effective\_date | TIMESTAMP NOT NULL |  |
| expiration\_date | TIMESTAMP NOT NULL | CHECK (expiration\_date \> effective\_date) |
| user\_agent | VARCHAR(250) |  |
| fingerprint | VARCHAR(500) |  |
| created\_at | TIMESTAMP NOT NULL | DEFAULT CURRENT\_TIMESTAMP |

Índices: idx\_token\_credential\_user (user\_id), idx\_token\_credential\_expiration (expiration\_date).

### V2 — Datos maestros de tipologías {#v2-—-datos-maestros-de-tipologías}

Puebla adm\_typology. Estructura jerárquica (parent\_typology\_id apunta al typology\_id del grupo padre). Valores relevantes para la aplicación:

| Grupo | typology\_id | internal\_id | Hijos (typology\_id / internal\_id) |
| ----- | ----- | ----- | ----- |
| Global (raíz) | −30 | −30 | — |
| **Status** | 30048 | 48 | Active 30049/49 · Inactive 30050/50 · Deleted 30051/51 · Locked 30052/52 · Suspended 30053/53 |
| **User\_Roles** | 30060 | 60 | Administrator 30061/61 (value\_1=ADMIN) · Collector 30062/62 (value\_1=COLLECTOR) |
| **Service\_Types** | 30070 | 70 | Residential 30071/71 · Business 30072/72 · Corporate 30073/73 |
| **Payment\_Methods** | 30080 | 80 | Cash 30081/81 · Transfer 30082/82 · Card 30083/83 |
| **Payment\_Status** | 30090 | 90 | Completed 30091/91 · Pending 30092/92 · Failed 30093/93 |
| **Audit\_Levels** | 30100 | 100 | Info 30101/101 · Warning 30102/102 · Error 30103/103 |

### V3 — Usuario raíz (dato semilla) {#v3-—-usuario-raíz-(dato-semilla)}

INSERT INTO app\_user (hash\_id, full\_name, email, password\_hash, tp\_role, tp\_status, last\_login\_at)

VALUES ('usr\_8f3b9a2c-...-4c5d', 'Administrador Root', 'admin@netpay.com', '\$2a\$12\$...', 30061, 30049, NULL);

* Rol: Administrador (tp\_role=30061), estado: Active (tp\_status=30049).

### V4 — Tabla app\_service (planes/servicios de internet) {#v4-—-tabla-app_service-(planes/servicios-de-internet)}

| Columna | Tipo | Restricciones |
| ----- | ----- | ----- |
| id | BIGSERIAL | PRIMARY KEY |
| hash\_id | VARCHAR(50) NOT NULL | UNIQUE — identificador público |
| name | VARCHAR(150) NOT NULL | Nombre del plan/servicio |
| speed | VARCHAR(50) NOT NULL | Velocidad (cadena, ej. 100 Mbps) |
| monthly\_price | NUMERIC(10,2) NOT NULL | Precio mensual (decimal) |
| tp\_type | BIGINT NOT NULL | FK → adm\_typology(typology\_id) — tipo de servicio (Service\_Types) |
| tp\_status | BIGINT NOT NULL | FK → adm\_typology(typology\_id) — estado |
| created\_at, updated\_at | TIMESTAMP NOT NULL | DEFAULT CURRENT\_TIMESTAMP |
| deleted\_at | TIMESTAMP | Soft-delete |

## 

## 6\. Documentación de Infraestructura y Entorno de IA {#6.-documentación-de-infraestructura-y-entorno-de-ia}

## 6.1 Contenedores Locales (Docker) {#6.1-contenedores-locales-(docker)}

El entorno de desarrollo de NetPay Manager utiliza PostgreSQL como sistema gestor de base de datos. La ejecución mediante contenedores permite disponer de un entorno local controlado y reproducible para los componentes que dependen de la base de datos.

La configuración exacta de Docker debe mantenerse separada de la configuración propia de Spring Boot. El backend se conecta a PostgreSQL mediante las propiedades de datasource configuradas para el entorno correspondiente, mientras que Flyway administra la evolución del esquema.

\[Insertar aquí el docker-compose.yml utilizado realmente durante la Fase 2.\]

### Consideraciones del entorno {#consideraciones-del-entorno}

* PostgreSQL debe ejecutarse con una configuración reproducible.  
* Las credenciales y secretos no deben quedar hardcodeados en el repositorio.  
* La configuración de conexión debe gestionarse mediante variables de entorno o configuración externa.  
* Flyway debe ejecutar las migraciones versionadas del proyecto.  
* El entorno local debe permitir que el backend Spring Boot se conecte al PostgreSQL utilizado para desarrollo.

> El archivo de referencia de arquitectura no contiene un docker-compose.yml ni la definición exacta de los servicios Docker utilizados. Por ello, el bloque de configuración debe tomarse directamente del archivo real del proyecto.

## 6.2 Uso de Agentes de IA en la Fase de Programación {#6.2-uso-de-agentes-de-ia-en-la-fase-de-programación}

Durante la Fase 2, las herramientas de Inteligencia Artificial pueden utilizarse como apoyo al proceso de desarrollo, principalmente para analizar código existente, proponer implementaciones, identificar errores y acelerar tareas repetitivas.

En el backend, este apoyo puede aplicarse a la revisión de la arquitectura por capas, implementación de casos de uso, construcción de repositorios, análisis de errores de compilación y revisión de consultas o migraciones. En el frontend, puede utilizarse para analizar componentes standalone, servicios HTTP, guards, interceptores, modelos y estilos, manteniendo siempre las convenciones establecidas por el proyecto.

El uso de agentes de IA no sustituye la validación del código. Las propuestas generadas deben revisarse contra la arquitectura, los requisitos de la Fase 2 y el código existente antes de incorporarse al proyecto.

### Flujo de trabajo utilizado {#flujo-de-trabajo-utilizado}

1. **Análisis del código existente:** se proporciona al agente el contexto necesario de la funcionalidad, arquitectura y archivos relacionados.  
2. **Identificación del problema:** se describe el error o funcionalidad requerida y se solicita una solución compatible con las convenciones actuales.  
3. **Generación o modificación:** el agente propone código, consultas, migraciones, pruebas o documentación.  
4. **Revisión manual:** se verifica que la propuesta respete las capas, nombres, dependencias y reglas del proyecto.  
5. **Compilación y pruebas:** el cambio se valida mediante las herramientas del proyecto.  
6. **Corrección iterativa:** cuando aparecen errores de compilación o comportamiento, se proporciona el resultado al agente para analizar la causa y ajustar únicamente las partes necesarias.  
7. **Integración:** una vez validado el cambio, se incorpora al código fuente y se documenta cuando corresponde.

### Herramientas utilizadas {#herramientas-utilizadas}

* **ChatGPT:** análisis de arquitectura, explicación de errores, generación y revisión de código, diseño de soluciones y documentación.  
* **OpenCode:** asistencia durante la navegación y modificación del código del proyecto.

## 

## 7\. Secuencia de Pantallas {#7.-secuencia-de-pantallas}

Durante la Fase 2 se implementó la interfaz web de NetPay Manager, tomando como referencia los requerimientos funcionales definidos y los flujos de trabajo de los diferentes tipos de usuario. La secuencia de pantallas permite representar el recorrido que realiza cada usuario dentro del sistema desde su autenticación hasta la ejecución de las principales operaciones disponibles.

La navegación se encuentra organizada de acuerdo con los permisos y responsabilidades asociados al usuario autenticado. El administrador dispone de las funcionalidades relacionadas con la configuración y gestión general del sistema, mientras que el cobrador cuenta con las opciones necesarias para consultar sus clientes asignados, revisar sus servicios, registrar pagos y generar los comprobantes correspondientes.

Las pantallas implementadas mantienen una estructura visual consistente entre los diferentes módulos, utilizando componentes reutilizables y elementos comunes de navegación, formularios, tablas, filtros, indicadores de estado y acciones. Esto permite mantener una experiencia uniforme a medida que el usuario se desplaza entre las distintas funcionalidades.

Como parte del desarrollo frontend, las interfaces fueron implementadas utilizando Angular y se integraron con los servicios proporcionados por el backend. La definición visual realizada durante el proceso de diseño sirvió como referencia para la construcción de las vistas, realizando los ajustes necesarios conforme se validaron las funcionalidades y los requerimientos del sistema.

La secuencia documentada contempla las principales pantallas y recorridos funcionales del sistema:

* Inicio de sesión.  
* Dashboard.  
* Gestión de usuarios y empleados.  
* Gestión de clientes.  
* Gestión de servicios.  
* Asignación de clientes.  
* Consulta de clientes asignados.  
* Registro de pagos.  
* Consulta del historial de pagos.  
* Generación de recibos.  
* Reportes y exportación.  
* Auditoría.

Cada secuencia muestra las pantallas involucradas y las transiciones principales entre ellas, permitiendo identificar el flujo que debe seguir el usuario para completar una determinada operación. Las diferencias de navegación entre administrador y cobrador se documentan según las funcionalidades habilitadas para cada rol.

La documentación visual de esta sección se complementa con las capturas de las interfaces implementadas y, cuando corresponde, con los diseños utilizados como referencia durante el desarrollo. De esta manera, la secuencia de pantallas permite relacionar los requerimientos funcionales con la implementación concreta del frontend y facilita la validación de los flujos principales del sistema.


## 8\. Anexos {#8.-anexos}

backend: [https\://github.com/NaviM02/net-pay-backend.git](https://github.com/NaviM02/net-pay-backend.git)  
frontend: [https\://github.com/NaviM02/net-pay-frontend.git](https://github.com/NaviM02/net-pay-frontend.git)  
jira: [https\://cunoc-dtzunun.atlassian.net/jira/software/projects/NPM/](https://cunoc-dtzunun.atlassian.net/jira/software/projects/NPM/boards/1?filter=&groupBy=none)  
figma: [https\://www\.figma.com/proto/T1pgbs7zV4gP9ob19ZaBDA/Proyecto-SS?node-id=1-3\&p=f\&t=BfhAvfocYlBXHDFn-1\&scaling=min-zoom\&content-scaling=fixed\&page-id=0%3A1](https://www.figma.com/proto/T1pgbs7zV4gP9ob19ZaBDA/Proyecto-SS?node-id=1-3&p=f&t=BfhAvfocYlBXHDFn-1&scaling=min-zoom&content-scaling=fixed&page-id=0%3A1)  
diagramas/imágenes:[seminario](https://drive.google.com/drive/folders/1KEoUFWaYdh4EoOKz6vdB6GjiKagXLFXL?usp=sharing)  


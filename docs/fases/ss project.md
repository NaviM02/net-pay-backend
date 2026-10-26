**Universidad San Carlos de Guatemala Centro Universitario de Occidente**  
**División de Ingeniería**  
**Curso: Seminario de sistemas**  
**Ingeniero: Mario Ramirez**

	

**Proyecto SS1: NetPay Manager**   
Fase 1: Análisis, Modelado y Planificación

**Dónavin O'brian Gabriel Tzunún López 201930708**

**Índice:**

**[1\. Definición del problema	4](#1.-definición-del-problema)**

[**2\. Descripción del proyecto y de la empresa	5**](#2.-descripción-del-proyecto-y-de-la-empresa)

[2.1 Empresa	5](#2.1-empresa)

[2.2 Descripción del sistema	6](#2.2-descripción-del-sistema)

[**3\. Objetivos	7**](#3.-objetivos)

[3.1 Objetivo general	7](#3.1-objetivo-general)

[3.2 Objetivos específicos	7](#3.2-objetivos-específicos)

[**4\. Alcance del proyecto	8**](#4.-alcance-del-proyecto)

[4.1 Funcionalidades incluidas	8](#4.1-funcionalidades-incluidas)

[4.2 Funcionalidades fuera del alcance inicial	9](#4.2-funcionalidades-fuera-del-alcance-inicial)

[**5\. Requisitos funcionales	10**](#5.-requisitos-funcionales)

[RF01 – Autenticación	10](#rf01-–-autenticación)

[RF02 – Recuperación de contraseña	10](#rf02-–-recuperación-de-contraseña)

[RF03 – Gestión de usuarios	10](#rf03-–-gestión-de-usuarios)

[RF04 – Gestión de clientes	10](#rf04-–-gestión-de-clientes)

[RF05 – Gestión de servicios	10](#rf05-–-gestión-de-servicios)

[RF06 – Asignación de clientes	10](#rf06-–-asignación-de-clientes)

[RF07 – Registro de pagos	10](#rf07-–-registro-de-pagos)

[RF08 – Pagos parciales	10](#rf08-–-pagos-parciales)

[RF9 – Pagos adelantados	11](#rf9-–-pagos-adelantados)

[RF10 – Historial de pagos	11](#rf10-–-historial-de-pagos)

[RF11 – Trazabilidad de operaciones	11](#rf11-–-trazabilidad-de-operaciones)

[RF12 – Generación de recibos	11](#rf12-–-generación-de-recibos)

[RF13 – Reportes	11](#rf13-–-reportes)

[RF14 – Exportación de reportes	11](#rf14-–-exportación-de-reportes)

[RF15 – Importación de información histórica	11](#rf15-–-importación-de-información-histórica)

[RF16 – Notificaciones	11](#rf16-–-notificaciones)

[**6\. Requisitos no funcionales	12**](#6.-requisitos-no-funcionales)

[RNF01 – Seguridad	12](#rnf01-–-seguridad)

[RNF02 – Disponibilidad	12](#rnf02-–-disponibilidad)

[RNF03 – Escalabilidad	12](#rnf03-–-escalabilidad)

[RNF04 – Mantenibilidad	12](#rnf04-–-mantenibilidad)

[RNF05 – Integridad de datos	12](#rnf05-–-integridad-de-datos)

[RNF06 – Trazabilidad	12](#rnf06-–-trazabilidad)

[RNF07 – Recuperación	12](#rnf07-–-recuperación)

[**7\. Procesos del negocio	13**](#7.-procesos-del-negocio)

[**8\. Casos de uso	17**](#8.-casos-de-uso)

[Administrador	17](#administrador)

[Cobrador	17](#cobrador)

[**9\. Arquitectura propuesta	19**](#9.-arquitectura-propuesta)

[Arquitectura conceptual	19](#arquitectura-conceptual)

[**10\. Tecnologías seleccionadas	20**](#10.-tecnologías-seleccionadas)

[**11\. Arquitectura e infraestructura Cloud	22**](#11.-arquitectura-e-infraestructura-cloud)

[Diseño conceptual	23](#diseño-conceptual)

[**12\. Seguridad	24**](#12.-seguridad)

[**13\. Gestión y migración de datos	24**](#13.-gestión-y-migración-de-datos)

[**14\. Estrategia de respaldo	26**](#14.-estrategia-de-respaldo)

[**15\. Análisis económico	27**](#15.-análisis-económico)

[**16\. Análisis de riesgos	28**](#16.-análisis-de-riesgos)

[**17\. Metodología Scrum	29**](#17.-metodología-scrum)

[**18\. Configuración de Jira y Product Backlog	30**](#18.-configuración-de-jira-y-product-backlog)

[Historias de usuario iniciales	30](#historias-de-usuario-iniciales)

[**19\. Estrategia GitFlow	36**](#19.-estrategia-gitflow)

[**20\. Diseño inicial de interfaz y mockups	38**](#20.-diseño-inicial-de-interfaz-y-mockups)

# 

# **1\. Definición del problema** {#1.-definición-del-problema}

Actualmente, la empresa cliente administra la información relacionada con sus clientes, servicios de internet y pagos principalmente mediante archivos de Excel. Este método permite llevar un control básico de la información y consultar los datos registrados, pero conforme aumenta la cantidad de clientes y movimientos, mantener la información de forma manual puede resultar cada vez más complicado. Los archivos dependen de una estructura que puede ser modificada directamente, lo que dificulta mantener un control uniforme de los datos, consultar información específica y conservar un historial organizado de las operaciones realizadas. Además, la información de clientes, servicios y pagos se encuentra relacionada entre sí, por lo que resulta necesario contar con una forma más estructurada de administrar estas relaciones.

Uno de los principales problemas identificados se encuentra en el control de los pagos de los servicios de internet. El proceso no se limita a registrar si un cliente pagó o no un determinado mes, ya que pueden presentarse diferentes situaciones durante el registro. Un cliente puede realizar un pago parcial, pagar completamente el período correspondiente o incluso adelantar el pago de uno o varios períodos futuros. De igual manera, la fecha en que un empleado registra el pago en el sistema puede ser diferente a la fecha en que el cliente realizó realmente el pago. Por esta razón, el sistema debe permitir registrar la fecha real del pago y relacionarlo con el período al que corresponde, evitando que estas diferencias generen información incorrecta sobre el estado de los servicios.

Otro aspecto importante es el cambio de precio de los servicios. Un cliente puede cambiar de plan de internet y, como consecuencia, el precio que debe pagar puede variar con el tiempo. Sin embargo, este cambio no debe modificar la información de los pagos que ya fueron realizados bajo un precio anterior. Por lo tanto, es necesario conservar el valor que correspondía al servicio en cada período, de manera que el historial pueda consultarse posteriormente sin que los cambios de precio actuales afecten los registros históricos. Esta información también permitirá determinar de mejor manera si un pago fue completo, parcial o si existe un monto que corresponde a períodos posteriores.

La empresa también requiere organizar el trabajo de los empleados encargados de realizar los cobros. Para ello, se necesita contar con usuarios internos que puedan acceder al sistema de acuerdo con su función y con una administración de los clientes asignados a cada cobrador. De esta manera, el administrador podrá gestionar los empleados y sus asignaciones, mientras que los cobradores podrán consultar la información necesaria para realizar su trabajo y registrar los pagos correspondientes. Los clientes, por su parte, serán administrados como información dentro del sistema y no necesitarán contar con una cuenta de usuario propia.

Además del registro de pagos, existe la necesidad de generar comprobantes que permitan respaldar las operaciones realizadas. Inicialmente, el sistema deberá permitir generar un recibo en formato PDF, dejando prevista la posibilidad de integrarlo posteriormente con una impresora de recibos si la empresa adquiere el equipo necesario. De igual manera, la información registrada deberá poder utilizarse para generar reportes administrativos que permitan consultar los pagos y otros datos relevantes de la operación de la empresa, incluyendo su exportación a formatos como PDF y Excel.

Finalmente, se identifica la necesidad de mantener una trazabilidad de las operaciones realizadas dentro del sistema. Las acciones importantes, especialmente aquellas relacionadas con la creación, modificación o registro de pagos, deben permitir identificar qué usuario realizó la operación y cuándo fue realizada. Esto permitirá mantener un historial de las acciones efectuadas y facilitar la revisión de la información cuando sea necesario. También se busca que el acceso al sistema sea controlado mediante usuarios y roles, de forma que las funciones disponibles dependan de las responsabilidades de cada empleado.

A partir de estas necesidades, se plantea desarrollar una aplicación web que centralice la administración de clientes, servicios, pagos, empleados, recibos y reportes en un mismo sistema. La solución busca sustituir progresivamente el manejo manual de la información mediante archivos de Excel por una plataforma que permita organizar los datos, conservar su historial y facilitar las operaciones diarias relacionadas con el cobro de los servicios de internet. De esta manera, el proyecto se enfocará principalmente en mejorar el control de los pagos y en proporcionar a la empresa una herramienta centralizada para administrar la información de sus operaciones.

# **2\. Descripción del proyecto y de la empresa** {#2.-descripción-del-proyecto-y-de-la-empresa}

## **2.1 Empresa** {#2.1-empresa}

La empresa propuesta para el desarrollo del proyecto será **Gravity**, una empresa dedicada a la prestación de servicios de internet para clientes residenciales y pequeños negocios. Para efectos del proyecto, la empresa contará con una estructura administrativa encargada de gestionar la información general del negocio y con empleados responsables de realizar el cobro y registro de los pagos de los clientes que les sean asignados.

La operación de la empresa requiere mantener información relacionada con sus clientes, los servicios de internet contratados, los precios establecidos para cada servicio y los pagos realizados durante los diferentes períodos. Debido a que un mismo cliente puede mantener un servicio durante un período prolongado y realizar diferentes tipos de pagos a lo largo del tiempo, resulta importante conservar un historial organizado que permita consultar la información de manera clara y mantener la relación entre los clientes, sus servicios y los pagos correspondientes.

Actualmente, una parte importante de esta información administrativa se encuentra almacenada en hojas de cálculo. Aunque este mecanismo permite registrar y consultar los datos, la empresa requiere una solución que permita centralizar la información y establecer una estructura más organizada para su manejo. La necesidad aumenta especialmente en el control de los pagos, debido a que estos pueden corresponder a diferentes períodos, ser realizados de manera parcial o cubrir períodos futuros.

A partir de esta necesidad surge la propuesta de desarrollar una solución tecnológica orientada específicamente a la administración de los pagos de los servicios de internet. El proyecto busca proporcionar a Gravity una herramienta que permita llevar un mayor control sobre la información que actualmente administra de manera manual, facilitando las tareas de los empleados y proporcionando al personal administrativo una visión más organizada de las operaciones relacionadas con los pagos.

## **2.2 Descripción del sistema** {#2.2-descripción-del-sistema}

**NetPay Manager** será una aplicación web desarrollada para centralizar y facilitar la gestión de los pagos de los servicios de internet de Gravity. El sistema estará orientado principalmente al personal interno de la empresa, por lo que los clientes serán administrados como registros dentro de la aplicación y no necesitarán disponer de una cuenta de usuario para utilizar el sistema.

La aplicación permitirá administrar la información de los clientes, empleados cobradores, servicios contratados y precios asociados a cada servicio. También permitirá establecer qué clientes se encuentran asignados a cada cobrador, facilitando la organización del trabajo y el acceso a la información necesaria para realizar los cobros.

Uno de los componentes principales será la gestión de pagos. El sistema permitirá registrar pagos asociados a períodos pasados, actuales o futuros, incluyendo situaciones en las que el cliente realice un pago parcial o adelante el pago de uno o más períodos. Para cada pago se podrá registrar información como la fecha real en que fue realizado, el monto recibido y comentarios relacionados con la operación. Además, se conservará el valor correspondiente al servicio en el período del pago, permitiendo mantener un historial correcto incluso cuando posteriormente cambie el precio o el plan contratado por el cliente.

El sistema también contará con mecanismos de trazabilidad para las operaciones importantes. De esta manera, los movimientos realizados dentro de la aplicación podrán relacionarse con el usuario que los efectuó y con la fecha correspondiente. Esta información permitirá mantener un historial de las operaciones y facilitar la revisión de los registros cuando sea necesario.

Como parte de la gestión de los pagos, NetPay Manager contemplará la generación de recibos digitales en formato PDF. Inicialmente, esta será la alternativa principal para generar los comprobantes de pago. Adicionalmente, se investigará la posibilidad de integrar el sistema con una impresora de recibos; sin embargo, esta integración dependerá de que la empresa disponga de un dispositivo compatible. Por esta razón, el proyecto dejará planteada la posibilidad de realizar dicha integración posteriormente sin que la generación del recibo digital dependa de la existencia de la impresora.

Finalmente, el sistema contará con un módulo de reportes dirigido al administrador. Este módulo permitirá consultar información relacionada con los pagos y obtener métricas básicas para apoyar la revisión de las operaciones de la empresa. Los reportes podrán ser exportados en formatos **PDF y Excel (.xlsx)**, cumpliendo con la necesidad de disponer de información que pueda ser consultada, almacenada o utilizada fuera de la aplicación.

En conjunto, NetPay Manager buscará proporcionar a Gravity una plataforma centralizada para sustituir progresivamente el manejo de la información mediante hojas de cálculo y facilitar la administración de los clientes, servicios y pagos. El sistema estará diseñado de manera que sus funcionalidades puedan evolucionar posteriormente de acuerdo con nuevas necesidades de la empresa, manteniendo como objetivo principal el control organizado y trazable de los pagos de los servicios de internet.

# **3\. Objetivos** {#3.-objetivos}

## **3.1 Objetivo general** {#3.1-objetivo-general}

Diseñar y desarrollar una aplicación web empresarial para la gestión centralizada de clientes, servicios y pagos de internet de la empresa Gravity, permitiendo organizar y controlar la información relacionada con los cobros, mantener un historial de las operaciones realizadas y facilitar la generación de recibos y reportes administrativos. El sistema será desarrollado mediante una arquitectura modular, segura y preparada para su despliegue en la nube, buscando facilitar su mantenimiento y permitir su evolución de acuerdo con las necesidades futuras de la empresa.

## **3.2 Objetivos específicos** {#3.2-objetivos-específicos}

* Centralizar la información de clientes, servicios, empleados cobradores y pagos que actualmente es administrada mediante archivos de Excel, proporcionando una estructura organizada para su consulta y gestión.  
* Implementar un sistema de autenticación y autorización para controlar el acceso de los usuarios internos y diferenciar las funciones disponibles para los roles de administrador y cobrador.  
* Permitir el registro y consulta de pagos correspondientes a períodos pasados, actuales o futuros, contemplando situaciones como pagos parciales, pagos completos y pagos adelantados.  
* Conservar la información histórica relacionada con el precio de los servicios, de manera que los cambios de plan o de precio no modifiquen los datos correspondientes a pagos realizados anteriormente.  
* Registrar información de trazabilidad sobre las operaciones relevantes realizadas dentro del sistema, permitiendo identificar el usuario responsable y la fecha en que se efectuó cada movimiento.  
* Facilitar la generación de recibos digitales asociados a los pagos registrados, utilizando inicialmente el formato PDF y dejando prevista una posible integración futura con una impresora de recibos.  
* Proporcionar un módulo de reportes para el administrador que permita consultar información relacionada con los pagos y exportar los resultados en formatos PDF y Excel (.xlsx).  
* Diseñar una arquitectura de software modular y organizada que facilite el mantenimiento del sistema, la incorporación de nuevas funcionalidades y su evolución conforme surjan nuevas necesidades.  
* Diseñar una infraestructura inicial en AWS que contemple los componentes necesarios para el posterior despliegue de la aplicación, considerando aspectos de seguridad, disponibilidad y posibilidad de crecimiento.

# **4\. Alcance del proyecto** {#4.-alcance-del-proyecto}

El proyecto comprenderá el diseño y desarrollo de una aplicación web orientada a la administración y control de los pagos relacionados con los servicios de internet ofrecidos por la empresa Gravity. La aplicación estará dirigida al personal interno de la empresa y tendrá como propósito centralizar la información que actualmente se administra mediante archivos de Excel, proporcionando una estructura organizada para gestionar clientes, servicios, pagos y las operaciones relacionadas con estos.

Dentro del alcance se contempla la administración de usuarios internos, clientes, servicios de internet, precios históricos, asignación de clientes a cobradores, registro y consulta de pagos, generación de recibos, trazabilidad de las operaciones y elaboración de reportes administrativos. El sistema deberá contemplar las diferentes situaciones que pueden presentarse durante el registro de un pago, incluyendo pagos parciales, pagos completos y pagos adelantados, así como la posibilidad de registrar pagos correspondientes a períodos anteriores o futuros.

También se contempla la conservación de la información histórica relacionada con los servicios y sus precios. Esto permitirá que los pagos realizados anteriormente mantengan el valor que correspondía al momento de su registro, incluso cuando posteriormente el cliente cambie de plan o se modifique el precio del servicio.

Debido a que la empresa utiliza actualmente archivos de Excel como fuente de información, se contempla dentro del alcance una funcionalidad para realizar una **importación inicial de datos** desde estos archivos. Esta funcionalidad tendrá como propósito facilitar la incorporación de la información existente al nuevo sistema, evitando que todos los registros deban ingresarse manualmente desde cero.

El proyecto también contempla la generación de recibos digitales en formato PDF y la investigación de una posible integración con una impresora física de recibos. Esta última dependerá de la disponibilidad de un dispositivo compatible por parte del cliente y, por lo tanto, se considera una extensión del sistema y no una condición necesaria para que la funcionalidad de recibos pueda ser utilizada.

## **4.1 Funcionalidades incluidas** {#4.1-funcionalidades-incluidas}

Las funcionalidades consideradas dentro del alcance inicial del proyecto son las siguientes:

* Autenticación de usuarios internos, permitiendo el acceso controlado al sistema.  
* Gestión de empleados, incluyendo la información necesaria para los usuarios internos del sistema.  
* Recuperación y restablecimiento de contraseñas mediante intervención del administrador, evitando que los cobradores puedan realizar por sí mismos procesos de recuperación externos al control de la empresa.  
* Gestión de clientes, permitiendo registrar, consultar, modificar y administrar la información de los clientes.  
* Gestión de servicios de internet, incluyendo la información relacionada con el servicio contratado.  
* Asignación de clientes a cobradores, permitiendo organizar los clientes que estarán bajo responsabilidad de cada cobrador.  
* Registro de pagos, incluyendo fecha real del pago, monto y comentarios relacionados con la operación.  
* Registro de pagos parciales, permitiendo representar situaciones en las que el monto recibido no cubre completamente el período correspondiente.  
* Registro de pagos adelantados, permitiendo relacionar pagos con períodos futuros.  
* Consulta del historial de pagos, permitiendo revisar los movimientos realizados por cada cliente.  
* Trazabilidad de operaciones, registrando información relacionada con los movimientos importantes realizados dentro del sistema.  
* Generación de recibos digitales, inicialmente mediante documentos PDF.  
* Reportes administrativos, proporcionando información y métricas relacionadas con los pagos registrados.  
* Exportación de reportes, permitiendo generar archivos en formatos PDF y Excel (.xlsx).  
* Importación inicial de información desde Excel, para facilitar la incorporación de los datos existentes de la empresa.  
* Notificaciones relacionadas con procesos importantes, de acuerdo con las necesidades definidas para la autenticación y otras operaciones relevantes del sistema.

## **4.2 Funcionalidades fuera del alcance inicial** {#4.2-funcionalidades-fuera-del-alcance-inicial}

Para mantener un alcance acorde con los objetivos y recursos disponibles para el proyecto, inicialmente no se contempla la integración con bancos, tarjetas de crédito, plataformas externas de pago en línea o sistemas de facturación electrónica. El sistema estará enfocado en **registrar y administrar los pagos realizados a la empresa**, no en procesar directamente transacciones financieras.

Tampoco se contempla la administración de la infraestructura física utilizada para proporcionar el servicio de internet, incluyendo routers, antenas, equipos de red, configuración de conexiones o monitoreo técnico de la red. El sistema se limitará a la gestión administrativa relacionada con los clientes, servicios y pagos.

De igual manera, no se contempla dentro del alcance inicial el desarrollo de una aplicación móvil independiente. El sistema será desarrollado como una aplicación web accesible mediante un navegador, priorizando las funcionalidades necesarias para la administración de los pagos y la operación interna de la empresa.

La integración directa con una impresora física de recibos se considera una funcionalidad de investigación y posible extensión. El proyecto investigará las alternativas técnicas disponibles para realizar esta integración, pero su implementación dependerá de que el cliente disponga de una impresora compatible. En caso de que el equipo no esté disponible al finalizar el proyecto, la generación de recibos en formato PDF será la solución funcional contemplada dentro del sistema.

Finalmente, el proyecto no contempla inicialmente que los clientes tengan acceso directo a la plataforma mediante cuentas propias. Los usuarios del sistema serán empleados internos, principalmente administradores y cobradores, mientras que los clientes serán administrados como información dentro de la aplicación.

# **5\. Requisitos funcionales** {#5.-requisitos-funcionales}

### **RF01 – Autenticación** {#rf01-–-autenticación}

El sistema deberá permitir que los empleados registrados puedan iniciar sesión utilizando sus credenciales y acceder únicamente a las funcionalidades correspondientes a su tipo de usuario.

### **RF02 – Recuperación de contraseña** {#rf02-–-recuperación-de-contraseña}

El sistema deberá permitir el restablecimiento de contraseñas de los empleados mediante la intervención del administrador. El proceso deberá mantener el control de la empresa sobre las cuentas internas y evitar que un usuario pueda modificar su contraseña mediante un proceso de recuperación externo no autorizado.

### **RF03 – Gestión de usuarios** {#rf03-–-gestión-de-usuarios}

El administrador podrá crear, consultar, modificar y desactivar las cuentas de los empleados que utilizarán el sistema. Las cuentas estarán asociadas a uno de los dos tipos de usuario definidos para la aplicación: Administrador o Cobrador.

### **RF04 – Gestión de clientes** {#rf04-–-gestión-de-clientes}

El sistema permitirá registrar, consultar, modificar y administrar la información de los clientes que poseen servicios de internet. Los clientes serán administrados como registros dentro del sistema y no dispondrán de cuentas de usuario propias.

### **RF05 – Gestión de servicios** {#rf05-–-gestión-de-servicios}

El sistema permitirá asociar uno o más servicios de internet a un cliente y mantener la información correspondiente al servicio contratado, incluyendo el plan y el precio utilizado para realizar los registros de pago.

### **RF06 – Asignación de clientes** {#rf06-–-asignación-de-clientes}

El administrador podrá asignar clientes a empleados que desempeñen funciones de cobrador. Estas asignaciones permitirán organizar la responsabilidad de los clientes y facilitar el trabajo de los empleados encargados de registrar los pagos.

### **RF07 – Registro de pagos** {#rf07-–-registro-de-pagos}

Los cobradores podrán registrar pagos indicando la fecha real en que fueron realizados, el período al que corresponden, el monto recibido y comentarios adicionales cuando sea necesario.

### **RF08 – Pagos parciales** {#rf08-–-pagos-parciales}

El sistema permitirá registrar pagos cuyo monto sea inferior al valor correspondiente al período registrado, conservando el monto efectivamente recibido y permitiendo agregar comentarios que expliquen la situación cuando sea necesario.

### **RF9 – Pagos adelantados** {#rf9-–-pagos-adelantados}

El sistema permitirá registrar pagos realizados anticipadamente y asociarlos con períodos futuros, permitiendo mantener el registro de los pagos que hayan sido realizados por adelantado.

### **RF10 – Historial de pagos** {#rf10-–-historial-de-pagos}

El sistema permitirá consultar los pagos realizados por cada cliente y servicio, incluyendo la fecha del pago, período correspondiente, monto registrado, comentarios y usuario responsable de la operación cuando corresponda.

### **RF11 – Trazabilidad de operaciones** {#rf11-–-trazabilidad-de-operaciones}

El sistema deberá registrar las operaciones relevantes realizadas por los usuarios, incluyendo información sobre el usuario responsable, la fecha, la acción realizada y el registro afectado. Esta información permitirá consultar posteriormente los movimientos efectuados dentro del sistema.

### **RF12 – Generación de recibos** {#rf12-–-generación-de-recibos}

El sistema permitirá generar un recibo digital asociado a un pago registrado, inicialmente en formato PDF.

### **RF13 – Reportes** {#rf13-–-reportes}

El administrador podrá consultar métricas y reportes relacionados con los clientes, servicios y pagos registrados en el sistema.

### **RF14 – Exportación de reportes** {#rf14-–-exportación-de-reportes}

El sistema permitirá exportar los reportes generados por el administrador en formatos PDF y Excel (.xlsx).

### **RF15 – Importación de información histórica** {#rf15-–-importación-de-información-histórica}

El sistema permitirá importar información previamente almacenada en archivos Excel, realizando las validaciones necesarias antes de incorporar los registros a la base de datos.

### **RF16 – Notificaciones** {#rf16-–-notificaciones}

El sistema deberá utilizar un mecanismo de notificación para procesos importantes relacionados con la seguridad de las cuentas, incluyendo otras operaciones que requieran comunicación con los empleados.

# **6\. Requisitos no funcionales** {#6.-requisitos-no-funcionales}

### **RNF01 – Seguridad** {#rnf01-–-seguridad}

La aplicación deberá proteger la información mediante autenticación de usuarios, control de acceso según el tipo de usuario, almacenamiento seguro de contraseñas y comunicación mediante HTTPS en los ambientes desplegados.

### **RNF02 – Disponibilidad** {#rnf02-–-disponibilidad}

La arquitectura deberá estar diseñada de manera que permita mantener el funcionamiento del sistema ante fallos individuales de componentes y que pueda evolucionar posteriormente hacia una infraestructura con mayores niveles de disponibilidad.

### **RNF03 – Escalabilidad** {#rnf03-–-escalabilidad}

La aplicación deberá utilizar una arquitectura modular que permita aumentar los recursos de sus componentes conforme aumente la cantidad de clientes, pagos y usuarios del sistema.

### **RNF04 – Mantenibilidad** {#rnf04-–-mantenibilidad}

El backend será desarrollado utilizando una arquitectura organizada basada en principios de Clean Architecture, separando las responsabilidades relacionadas con el dominio, la lógica de aplicación, la infraestructura y la presentación. Esta separación deberá facilitar el mantenimiento y la incorporación de nuevas funcionalidades.

### **RNF05 – Integridad de datos** {#rnf05-–-integridad-de-datos}

Los registros de pagos deberán conservar la información correspondiente al momento en que fueron registrados, incluyendo el monto recibido y el período al que fueron asociados, evitando que modificaciones posteriores de la información del servicio alteren los registros de pagos existentes.

### **RNF06 – Trazabilidad** {#rnf06-–-trazabilidad}

Las operaciones críticas realizadas dentro del sistema deberán quedar registradas de manera que sea posible identificar al usuario que realizó la operación, la fecha en que fue realizada, la acción ejecutada y el registro relacionado.

### **RNF07 – Recuperación** {#rnf07-–-recuperación}

La información almacenada deberá contar con mecanismos de respaldo que permitan recuperar los datos ante una eventual pérdida, daño o fallo de la infraestructura.

# **7\. Procesos del negocio** {#7.-procesos-del-negocio}

Para el modelado inicial se identificaron los siguientes procesos:

1. Autenticación y acceso al sistema  
2. Gestión de clientes y servicios de internet  
3. Asignación de clientes a cobradores  
4. Registro y gestión de pagos  
5. Generación de recibos  
6. Generación y exportación de reportes  
7. Importación histórica desde Excel

Link a draw.io: [https\://drive.google.com/file/d/1Hte1KoqwdHoeM\_ua\_AHUxVjbxuB6Bh1a/view](https://drive.google.com/file/d/1Hte1KoqwdHoeM_ua_AHUxVjbxuB6Bh1a/view)

![][image1]

# 

# 

# 

# 

# 

# 

# 

# 

# 

# **8\. Casos de uso** {#8.-casos-de-uso}

Los principales actores identificados para el sistema son Administrador y Cobrador, ambos correspondientes a usuarios internos de la empresa.

El Administrador tendrá acceso a las funciones de administración general del sistema, incluyendo la gestión de empleados, clientes y servicios, la asignación de clientes a cobradores, la consulta de pagos, la revisión de la trazabilidad de las operaciones, la generación de reportes y la importación de información histórica.

El Cobrador tendrá acceso principalmente a la información de los clientes que le hayan sido asignados y a las operaciones relacionadas con la consulta y registro de pagos. Las acciones disponibles para este actor estarán limitadas a las funciones necesarias para realizar sus actividades dentro de la empresa.

### **Administrador** {#administrador}

* Iniciar sesión.  
* Gestionar empleados.  
* Gestionar clientes.  
* Gestionar servicios.  
* Asignar clientes a cobradores.  
* Consultar pagos.  
* Consultar auditoría.  
* Generar reportes.  
* Exportar reportes.  
* Importar información desde Excel.


  ### **Cobrador** {#cobrador}

* Iniciar sesión.  
* Consultar clientes asignados.  
* Consultar servicios.  
* Registrar pagos.  
* Consultar historial de pagos.  
* Generar recibos.

Link a draw.io: [https\://drive.google.com/file/d/1pu90B3kIf6vDoL9GPpX27KS708UliRp9/view](https://drive.google.com/file/d/1pu90B3kIf6vDoL9GPpX27KS708UliRp9/view)

# 

# 

# **9\. Arquitectura propuesta** {#9.-arquitectura-propuesta}

El sistema será desarrollado utilizando una arquitectura web cliente-servidor. El frontend será desarrollado con Angular y será responsable de la interacción con los usuarios, la presentación de la información y el consumo de los servicios proporcionados por el backend. La comunicación entre ambas partes se realizará mediante una API REST sobre HTTPS.

El backend será desarrollado con Spring Boot y estará encargado de exponer los endpoints de la aplicación, procesar las solicitudes provenientes del frontend y ejecutar las reglas de negocio correspondientes. Para su organización interna se utilizarán principios de Clean Architecture, separando las responsabilidades relacionadas con la presentación, aplicación, dominio e infraestructura. Esta separación permitirá mantener desacoplada la lógica de negocio de elementos externos como la base de datos, servicios de comunicación y mecanismos específicos de despliegue.

La capa de presentación será responsable de recibir las solicitudes HTTP y exponer los endpoints de la API REST. La capa de aplicación coordinará los casos de uso y las operaciones que pueden realizar los usuarios del sistema. La capa de dominio contendrá las entidades, reglas y lógica de negocio principales, procurando que estas no dependan de tecnologías o componentes externos. Finalmente, la capa de infraestructura proporcionará las implementaciones necesarias para interactuar con elementos externos, como la persistencia de datos y otros servicios utilizados por la aplicación.

La persistencia de la información se realizará mediante PostgreSQL. El frontend no tendrá acceso directo a la base de datos, sino que todas las operaciones relacionadas con la información serán gestionadas por el backend mediante la API REST. Las modificaciones de la estructura de la base de datos serán administradas mediante Flyway, permitiendo mantener las migraciones versionadas junto con el código fuente y facilitar la evolución controlada del esquema de la base de datos.

### **Arquitectura conceptual** {#arquitectura-conceptual}

                   Usuario  
                       \|  
                       \| HTTPS  
                       v  
                Angular Frontend  
                       \|  
                       \| REST API  
                       v  
                Spring Boot Backend  
                       \|  
          \+------------+------------+  
          \|            \|            \|  
       Domain     Application   Infrastructure  
                                   \|  
                                   v  
                              PostgreSQL

# **10\. Tecnologías seleccionadas** {#10.-tecnologías-seleccionadas}

| Componente | Tecnología |
| :---- | :---- |
| Frontend | Angular |
| Backend | Spring Boot |
| Lenguaje Backend | Java |
| Base de datos | PostgreSQL |
| Migraciones | Flyway |
| Contenedores locales | Docker |
| Control de versiones | Git / GitHub |
| CI/CD | GitHub Actions |
| Cloud | Amazon Web Services (AWS)  |
| Frontend Cloud | Amazon S3 \+ Amazon CloudFront   |
| Backend Cloud | Amazon EC2 |
| Base de datos Cloud | Amazon RDS for PostgreSQL  |
| Gestión de permisos | Amazon VPC    |

La solución utilizará Angular para el desarrollo de la interfaz web y Spring Boot con Java para la implementación de la API REST y la lógica de negocio. PostgreSQL será utilizado como sistema gestor de base de datos y Flyway permitirá administrar de manera versionada las modificaciones realizadas sobre su estructura.

Durante el desarrollo local se utilizará Docker para facilitar la configuración y reproducción del entorno, principalmente para ejecutar PostgreSQL y los demás componentes que requieran una configuración independiente. El código fuente será administrado mediante Git y GitHub, mientras que GitHub Actions será utilizado para automatizar los procesos de integración y construcción del proyecto.

Para el ambiente Cloud se seleccionará Amazon Web Services (AWS), debido a que la arquitectura propuesta en el enunciado contempla específicamente servicios como S3, EC2 y RDS. La infraestructura inicial utilizará Amazon S3 para almacenar los archivos estáticos generados por la compilación de Angular y Amazon CloudFront para distribuir el frontend mediante HTTPS. Amazon EC2 será utilizado para ejecutar el backend desarrollado con Spring Boot, mientras que Amazon RDS for PostgreSQL proporcionará una base de datos administrada.

La infraestructura utilizará Amazon VPC para definir la red de los componentes desplegados y AWS IAM para controlar los permisos de acceso bajo el principio de mínimo privilegio. Para almacenar parámetros de configuración y valores sensibles necesarios por la aplicación se contempla utilizar AWS Systems Manager Parameter Store, priorizando parámetros de tipo Standard para evitar servicios adicionales que no sean necesarios para el alcance inicial. Los parámetros Standard de Parameter Store no tienen un cargo adicional por almacenamiento.

La selección de estos servicios busca mantener una infraestructura adecuada para el proyecto académico sin incorporar inicialmente componentes de mayor complejidad o costo que no sean necesarios. AWS mantiene actualmente un programa Free Tier que permite a nuevos usuarios utilizar créditos y determinados servicios sin costo dentro de los límites establecidos, aunque el uso que exceda dichos límites puede generar cargos. Por esta razón, durante las pruebas se utilizarán recursos de bajo consumo y se controlará periódicamente el uso y la facturación de la cuenta.

Durante la etapa inicial se priorizará una arquitectura sencilla que pueda evolucionar posteriormente hacia componentes adicionales de disponibilidad, escalabilidad, monitoreo y seguridad conforme las necesidades del sistema lo requieran.

# 

# **11\. Arquitectura e infraestructura Cloud** {#11.-arquitectura-e-infraestructura-cloud}

La infraestructura inicial se plantea utilizando Amazon Web Services (AWS) como proveedor de servicios Cloud. La arquitectura estará organizada mediante una red virtual en Amazon VPC, permitiendo establecer una separación entre los componentes públicos y los componentes que no deben estar directamente expuestos a Internet.

El frontend desarrollado con Angular será compilado y almacenado como archivos estáticos en Amazon S3. Para proporcionar acceso mediante HTTPS y distribuir el contenido de forma segura, se utilizará Amazon CloudFront como capa de distribución frente al bucket de S3. Esta combinación permite servir el contenido estático mediante una infraestructura administrada y preparada para incorporar posteriormente mecanismos adicionales de seguridad y distribución. AWS recomienda utilizar CloudFront junto con S3 para la distribución y protección del contenido estático.

El backend desarrollado con Spring Boot será ejecutado inicialmente sobre una instancia de Amazon EC2. La instancia formará parte de la VPC y contará con reglas de seguridad que limitarán los puertos y servicios accesibles desde el exterior. La API será la única responsable de comunicarse con la base de datos, evitando que el frontend tenga acceso directo a PostgreSQL.

La información persistente será almacenada en Amazon RDS for PostgreSQL. La instancia de RDS se ubicará en una sección de la red destinada a componentes de datos y no se expondrá directamente a Internet. El acceso a la base de datos será permitido únicamente desde los componentes autorizados, principalmente la instancia EC2 donde se ejecutará el backend.

La seguridad de la infraestructura se apoyará en Amazon VPC, grupos de seguridad, AWS IAM y comunicación mediante HTTPS. Los permisos de IAM se configurarán siguiendo el principio de mínimo privilegio, otorgando a cada usuario, servicio o componente únicamente los permisos necesarios para realizar sus funciones.

Los valores de configuración y credenciales que no deban formar parte del código fuente serán administrados mediante AWS Systems Manager Parameter Store. De esta manera, las credenciales de la base de datos y otros valores sensibles podrán mantenerse separados del repositorio del proyecto.

Para el acceso al frontend se utilizará HTTPS mediante CloudFront. En el caso del backend, se establecerá igualmente HTTPS como requisito de comunicación externa, definiendo durante la etapa de implementación el mecanismo de certificado TLS más adecuado para la instancia EC2. De esta manera, las comunicaciones entre los usuarios y los componentes expuestos de la aplicación estarán protegidas mediante cifrado.

La arquitectura inicial estará orientada a un entorno académico y de pruebas, por lo que se priorizará mantener una cantidad reducida de servicios y recursos. Amazon RDS proporciona mecanismos administrados de respaldo y recuperación que pueden utilizarse para implementar posteriormente la estrategia de recuperación de la base de datos definida para el proyecto. AWS permite realizar recuperación a un punto específico en el tiempo mediante los respaldos automatizados y los registros de transacciones administrados por RDS.

Conforme avance el proyecto, la infraestructura podrá evolucionar incorporando componentes adicionales para mejorar la disponibilidad, escalabilidad, monitoreo y seguridad sin modificar la separación principal entre frontend, backend y base de datos.

### **Diseño conceptual** {#diseño-conceptual}

                        
   


La arquitectura propuesta separará el frontend, el procesamiento de la aplicación y la persistencia de los datos, evitando que los usuarios o el frontend accedan directamente a la base de datos. Esta organización permitirá mantener un control adecuado sobre las comunicaciones y permisos y proporcionará una base para incorporar posteriormente mecanismos adicionales de alta disponibilidad, escalabilidad y monitoreo.

Para el primer entregable, esta arquitectura se considera un diseño inicial y no implica que todos los componentes deban encontrarse desplegados de forma definitiva. Su propósito es establecer la topología Cloud propuesta, las responsabilidades de cada servicio, la comunicación entre componentes y los mecanismos principales de seguridad que serán considerados durante la implementación.

# 

# **12\. Seguridad** {#12.-seguridad}

La seguridad será considerada desde las primeras etapas del diseño y se aplicará tanto a la aplicación como a la infraestructura utilizada para su despliegue.

El sistema implementará mecanismos de autenticación y autorización basados en roles, permitiendo diferenciar las operaciones disponibles para los usuarios con rol de Administrador y Cobrador. Cada usuario podrá acceder únicamente a las funcionalidades y recursos que correspondan a sus responsabilidades dentro de la empresa.

Las contraseñas no serán almacenadas directamente en la base de datos, sino mediante mecanismos de hash seguros y adecuados para el almacenamiento de credenciales. Las sesiones y credenciales utilizadas para acceder a la API serán gestionadas mediante mecanismos de autenticación que permitan validar la identidad del usuario antes de autorizar las operaciones solicitadas.

La comunicación entre los usuarios y los servicios publicados deberá realizarse mediante HTTPS, evitando la transmisión de información sensible mediante conexiones sin cifrado. La comunicación entre los diferentes componentes de la infraestructura también será restringida mediante las reglas de red correspondientes.

Las credenciales, claves, tokens y demás información sensible no deberán almacenarse directamente en el código fuente ni en el repositorio del proyecto. Durante el desarrollo local se utilizarán variables de entorno y archivos de configuración excluidos del control de versiones. En el ambiente Cloud se contempla utilizar AWS Systems Manager Parameter Store para administrar parámetros y valores sensibles que necesite la aplicación.

En AWS se utilizará IAM para controlar el acceso a los recursos de la infraestructura siguiendo el principio de mínimo privilegio. Los permisos serán asignados de acuerdo con las responsabilidades de cada usuario o componente, evitando otorgar permisos administrativos cuando no sean necesarios.

La infraestructura utilizará Amazon VPC y grupos de seguridad para controlar la comunicación entre los componentes. La base de datos PostgreSQL administrada mediante Amazon RDS no deberá estar expuesta directamente a Internet, y su acceso será restringido a los componentes autorizados del backend.

Como medida adicional, los registros de auditoría del sistema permitirán identificar las operaciones relevantes realizadas por los usuarios, incluyendo el usuario responsable y la fecha de ejecución. Esto permitirá mantener trazabilidad sobre las operaciones relacionadas con la información de la empresa.

# **13\. Gestión y migración de datos** {#13.-gestión-y-migración-de-datos}

PostgreSQL será utilizado como sistema gestor de base de datos debido a sus capacidades transaccionales, confiabilidad, soporte para relaciones entre entidades y compatibilidad con Spring Boot. La base de datos será responsable de almacenar la información relacionada con clientes, servicios, pagos, empleados, asignaciones y registros de auditoría.

La evolución de la estructura de la base de datos será controlada mediante Flyway. Cada modificación al esquema será registrada mediante una migración versionada, permitiendo mantener un historial de los cambios realizados y reproducir de forma controlada la estructura de la base de datos en los diferentes ambientes del proyecto.

La migración inicial de información existente será realizada a partir de los archivos de Excel utilizados actualmente por la empresa. Antes de incorporar los registros a PostgreSQL, la información deberá pasar por procesos de validación y transformación para verificar que cumpla con la estructura y las reglas definidas por el sistema. Los registros que presenten errores deberán ser identificados para permitir su revisión antes de completar la importación.

Un aspecto importante será la conservación de la información histórica relacionada con los servicios y sus precios. Los pagos registrados deberán conservar el valor correspondiente al servicio y período al que fueron asociados, evitando depender exclusivamente del precio actual del servicio. De esta manera, cuando un cliente cambie de plan o se modifique el precio de un servicio, los pagos realizados anteriormente conservarán la información que les correspondía en el momento de la operación.

La gestión de datos también deberá mantener la relación entre clientes, servicios y pagos, permitiendo consultar posteriormente el historial completo de las operaciones realizadas. Las modificaciones de información que puedan afectar estos registros deberán realizarse de manera controlada para preservar la integridad de los datos históricos.

# 

# **14\. Estrategia de respaldo** {#14.-estrategia-de-respaldo}

La estrategia de respaldo estará orientada principalmente a proteger la información de clientes, servicios, pagos, empleados y registros de auditoría, garantizando que los datos puedan recuperarse ante errores humanos, fallos de infraestructura o pérdida accidental de información.

En el ambiente Cloud se utilizarán las capacidades de respaldo administrado proporcionadas por Amazon RDS para PostgreSQL. Estos mecanismos permitirán conservar respaldos de la base de datos y facilitar su recuperación en caso de incidentes.

Como parte del diseño inicial se contempla una estrategia basada en respaldos completos periódicos y mecanismos que permitan conservar los cambios realizados posteriormente. Para el ambiente Cloud, la configuración definitiva de la frecuencia de respaldos, período de retención y recuperación a un punto específico en el tiempo se establecerá durante la etapa de implementación de acuerdo con las capacidades disponibles en Amazon RDS y las necesidades del proyecto.

La estrategia buscará proporcionar una capacidad de recuperación adecuada para la información histórica de los pagos. Se dará especial importancia a los datos de pagos y auditoría, debido a que su pérdida podría afectar la trazabilidad de las operaciones y la capacidad de la empresa para consultar su historial.

Además de los respaldos administrados por el servicio Cloud, se considerará la realización periódica de respaldos independientes de la información cuando sea necesario para contar con una alternativa adicional de recuperación. Los procedimientos de restauración deberán ser probados durante el desarrollo para verificar que los respaldos disponibles puedan utilizarse correctamente ante una eventual pérdida de información.

La estrategia de respaldo será revisada durante las siguientes etapas del proyecto y podrá ajustarse conforme se definan los requisitos definitivos de disponibilidad, retención y recuperación de datos.

# 

# **15\. Análisis económico** {#15.-análisis-económico}

El proyecto será desarrollado inicialmente utilizando herramientas y tecnologías que no requieren licencias comerciales para el desarrollo local, incluyendo Java, Spring Boot, Angular, PostgreSQL, Flyway, Docker y Git. Esto permitirá mantener bajos los costos durante las etapas de desarrollo y pruebas.

Para el despliegue se utilizarán servicios de Amazon Web Services (AWS). Debido a que el proyecto corresponde a un desarrollo académico, se priorizará el uso de recursos de bajo consumo y de los beneficios gratuitos o créditos que se encuentren disponibles para la cuenta utilizada. AWS dispone actualmente de un programa Free Tier con beneficios que dependen del tipo de cuenta, servicio y fecha de creación de la cuenta, por lo que no se asumirá que todos los recursos utilizados serán permanentemente gratuitos. [AWS Free Tier](https://aws.amazon.com/free/?utm_source=chatgpt.com)

La infraestructura propuesta contempla inicialmente Amazon S3, Amazon CloudFront, Amazon EC2 y Amazon RDS for PostgreSQL. Estos servicios pueden generar costos cuando se superan los límites gratuitos o cuando las condiciones del Free Tier aplicable a la cuenta no cubren el consumo realizado. Por esta razón, durante el desarrollo se mantendrán los recursos dimensionados para un entorno académico y se monitoreará periódicamente el consumo y los cargos de la cuenta.

No se establecerá un costo mensual fijo para AWS en esta etapa, debido a que el precio depende de factores como región, tipo de instancia, almacenamiento, transferencia de datos, horas de ejecución y características de cada servicio. Además, los precios publicados por AWS se expresan principalmente en dólares estadounidenses, mientras que los gastos del proyecto para el contexto de Guatemala deberán considerarse en función del tipo de cambio vigente al momento de realizar el despliegue.

El uso de PostgreSQL permite evitar costos de licenciamiento del sistema gestor de base de datos, mientras que Docker facilita la creación de un entorno local reproducible sin necesidad de contratar infraestructura Cloud durante el desarrollo.

Como medida de control económico, antes de utilizar recursos que puedan generar cargos se verificará la configuración de facturación de la cuenta, los límites de uso y los beneficios disponibles. También se utilizarán herramientas de monitoreo y alertas de costos de AWS para detectar consumos inesperados y evitar que los recursos permanezcan activos innecesariamente.

En caso de que alguna funcionalidad Cloud genere un costo que no sea justificable para el proyecto académico, se priorizará mantenerla como parte del diseño de infraestructura y realizar su implementación únicamente cuando sea necesario para las pruebas o demostraciones. El objetivo será contar con una arquitectura técnicamente viable sin asumir costos innecesarios para el desarrollo del proyecto.

# 

# **16\. Análisis de riesgos** {#16.-análisis-de-riesgos}

Para el desarrollo del proyecto se identificaron los principales riesgos que podrían afectar el cumplimiento de los objetivos, el funcionamiento del sistema, la información almacenada o los recursos disponibles para su implementación.

| Riesgo | Probabilidad | Impacto | Estrategia de mitigación |
| ----- | ----- | ----- | ----- |
| Pérdida de información | Media | Alto | Implementar respaldos periódicos y procedimientos de recuperación y restauración de la base de datos. |
| Errores durante la importación de Excel | Alta | Alto | Validar los datos antes de importarlos, identificar registros inválidos y generar información sobre los errores encontrados. |
| Cambios en los requerimientos | Alta | Medio | Mantener un backlog actualizado, priorizar los cambios y gestionar las modificaciones mediante la metodología Scrum. |
| Incremento inesperado del costo Cloud | Media | Alto | Utilizar recursos de bajo consumo, monitorear el uso de AWS, establecer alertas de costos y eliminar recursos que no estén siendo utilizados. |
| Errores en el cálculo o aplicación de pagos | Media | Alto | Implementar pruebas automatizadas, validaciones de negocio y pruebas específicas para pagos completos, parciales y adelantados. |
| Pérdida de trazabilidad | Baja | Alto | Registrar las operaciones críticas junto con el usuario, fecha, acción y registro afectado, evitando modificaciones que eliminen información de auditoría. |
| Problemas con la impresora física de recibos | Media | Medio | Mantener la generación de recibos en PDF como funcionalidad principal y considerar la impresión física como una extensión del sistema. |
| Cambios en los precios de los servicios | Alta | Medio | Conservar la información correspondiente al precio aplicado en cada período y evitar que los cambios posteriores modifiquen los registros históricos. |
| Fallos durante el despliegue | Media | Alto | Utilizar CI/CD mediante GitHub Actions, mantener ambientes separados y validar los cambios antes de desplegarlos en producción. |
| Dificultades asociadas al desarrollo individual | Alta | Medio | Mantener una planificación de tareas, documentación técnica, control de versiones y seguimiento periódico del avance del proyecto. |
| Exposición accidental de información sensible | Media | Alto | Utilizar HTTPS, control de acceso mediante IAM, variables de entorno o Parameter Store y evitar almacenar credenciales en el repositorio. |
| Fallos o indisponibilidad de servicios Cloud | Baja | Alto | Mantener respaldos, procedimientos de recuperación y una arquitectura que permita sustituir o restaurar componentes afectados cuando sea necesario. |
| Errores en migraciones de base de datos | Media | Alto | Utilizar Flyway para versionar las migraciones, probar los cambios previamente y realizar respaldos antes de modificaciones importantes del esquema. |

# **17\. Metodología Scrum** {#17.-metodología-scrum}

El proyecto será gestionado utilizando Scrum como marco de trabajo ágil para organizar, priorizar y dar seguimiento al desarrollo de la aplicación. Debido a que el desarrollo será realizado por una sola persona, las responsabilidades asociadas a Product Owner, Scrum Master y Developer serán asumidas de manera individual.

El trabajo será dividido en sprints de tres semanas, de acuerdo con la periodicidad establecida para el proyecto académico. Al inicio de cada sprint se realizará la planificación de las actividades y se seleccionarán los elementos del Product Backlog que serán desarrollados. Durante el sprint se dará seguimiento al avance de las tareas y se registrarán los cambios o incidencias que puedan surgir.

Al finalizar cada sprint se revisará el incremento desarrollado para verificar el cumplimiento de los objetivos establecidos y posteriormente se realizará una retrospectiva para identificar aspectos que puedan mejorarse durante los siguientes sprints.

Jira será utilizado como herramienta principal para la gestión del proyecto, permitiendo administrar el Product Backlog, historias de usuario, tareas técnicas, bugs y avances de cada sprint. La información registrada en Jira permitirá mantener una trazabilidad entre los requerimientos definidos, las funcionalidades implementadas y las actividades realizadas durante el desarrollo.

# **18\. Configuración de Jira y Product Backlog** {#18.-configuración-de-jira-y-product-backlog}

Jira será utilizado como herramienta central para organizar y dar seguimiento al desarrollo de NetPay Manager. El Product Backlog estará compuesto principalmente por historias de usuario, tareas técnicas, tareas de infraestructura y bugs identificados durante las etapas de desarrollo y pruebas.

Las historias de usuario seguirán una estructura basada en el formato:

**Como \[tipo de usuario\], quiero \[funcionalidad\], para \[beneficio o propósito\].**

Cada historia contará con criterios de aceptación que permitan determinar de forma objetiva cuándo una funcionalidad puede considerarse completada. Las historias serán priorizadas de acuerdo con su importancia para la operación del sistema y posteriormente serán distribuidas entre los diferentes sprints.

Los principales módulos considerados para la organización inicial del Product Backlog son:

* Autenticación y seguridad.  
* Gestión de usuarios y empleados.  
* Gestión de clientes.  
* Gestión de servicios.  
* Gestión de asignaciones.  
* Gestión de pagos.  
* Gestión de recibos.  
* Auditoría y trazabilidad.  
* Reportes y exportación.  
* Importación de datos históricos.  
* Infraestructura Cloud.  
* CI/CD.  
* Respaldo y recuperación.

  ### **Historias de usuario iniciales** {#historias-de-usuario-iniciales}

**HU01 – Inicio de sesión**

Como empleado, quiero iniciar sesión utilizando mis credenciales, para acceder al sistema de acuerdo con mi rol y realizar las operaciones que me correspondan.

**Criterios de aceptación:**

* El usuario debe poder ingresar sus credenciales.  
* El sistema debe validar las credenciales proporcionadas.  
* El sistema debe identificar el rol asociado al usuario.  
* Un usuario con credenciales inválidas no debe poder acceder al sistema.  
* El usuario debe acceder únicamente a las funcionalidades permitidas para su rol.

**HU02 – Gestión de empleados**

Como administrador, quiero gestionar las cuentas de los empleados, para controlar quiénes pueden utilizar el sistema y qué rol tienen asignado.

**Criterios de aceptación:**

* El administrador debe poder crear empleados.  
* Debe poder consultar la información de los empleados.  
* Debe poder modificar la información de un empleado.  
* Debe poder activar o desactivar una cuenta.  
* Cada empleado debe tener asignado un rol de Administrador o Cobrador.

**HU03 – Restablecimiento de contraseña**

Como administrador, quiero restablecer la contraseña de un empleado, para recuperar el acceso a una cuenta cuando el usuario no pueda ingresar al sistema.

**Criterios de aceptación:**

* El administrador debe poder seleccionar un empleado.  
* Debe poder iniciar el proceso de restablecimiento.  
* El sistema debe permitir establecer una nueva contraseña de forma segura.  
* La contraseña no debe almacenarse en texto plano.  
* El usuario debe poder utilizar la nueva contraseña para iniciar sesión.

**HU04 – Gestión de clientes**

Como administrador, quiero gestionar la información de los clientes, para mantener actualizados los registros de las personas que poseen servicios de internet.

**Criterios de aceptación:**

* El administrador debe poder registrar un cliente.  
* Debe poder consultar la información de un cliente.  
* Debe poder modificar la información de un cliente.  
* El sistema debe conservar la relación entre el cliente y sus servicios.  
* Los clientes no deben requerir una cuenta de usuario para existir en el sistema.

**HU05 – Gestión de servicios**

Como administrador, quiero gestionar los servicios contratados por los clientes, para mantener actualizada la información de los planes y precios disponibles.

**Criterios de aceptación:**

* El administrador debe poder asociar un servicio a un cliente.  
* Debe poder consultar el servicio contratado.  
* Debe poder modificar la información correspondiente al servicio.  
* Debe poder establecer o actualizar el precio del servicio.  
* Los cambios posteriores del precio no deben modificar los registros históricos de pagos.

**HU06 – Asignación de clientes**

Como administrador, quiero asignar clientes a cobradores, para organizar la responsabilidad de cada empleado sobre los clientes que debe atender.

**Criterios de aceptación:**

* El administrador debe poder seleccionar un cliente.  
* Debe poder seleccionar un cobrador.  
* Debe poder crear la asignación.  
* Debe poder consultar las asignaciones existentes.  
* El cobrador debe poder consultar únicamente los clientes que le hayan sido asignados.

**HU07 – Consulta de clientes asignados**

Como cobrador, quiero consultar los clientes que tengo asignados, para disponer de la información necesaria para realizar el proceso de cobro.

**Criterios de aceptación:**

* El cobrador debe poder visualizar sus clientes asignados.  
* Debe poder consultar la información básica de cada cliente.  
* Debe poder consultar los servicios asociados al cliente.  
* No debe poder acceder a clientes que no estén bajo su responsabilidad.

**HU08 – Registro de pago**

Como cobrador, quiero registrar el pago realizado por un cliente indicando la fecha, período, monto y comentario, para mantener actualizado el historial de pagos.

**Criterios de aceptación:**

* El cobrador debe poder seleccionar un cliente asignado.  
* Debe poder seleccionar el servicio correspondiente.  
* Debe poder ingresar el monto pagado.  
* Debe poder indicar la fecha real en que se realizó el pago.  
* Debe poder indicar el período al que corresponde el pago.  
* Debe poder agregar un comentario cuando sea necesario.  
* El sistema debe determinar la situación del pago de acuerdo con el monto y período registrado.  
* El sistema debe registrar el usuario que realizó la operación.  
* El sistema debe registrar la fecha en que la operación fue registrada.  
* El sistema debe generar el registro correspondiente en la auditoría.

**HU09 – Registro de pagos parciales**

Como cobrador, quiero registrar un monto inferior al valor correspondiente a un período, para conservar correctamente los pagos parciales realizados por los clientes.

**Criterios de aceptación:**

* El sistema debe permitir registrar un monto inferior al valor esperado del período.  
* Debe conservarse el monto efectivamente recibido.  
* El pago debe quedar asociado al período correspondiente.  
* El historial debe reflejar que el período no ha sido cubierto completamente.  
* El registro debe conservar el precio correspondiente al período.

**HU10 – Registro de pagos adelantados**

Como cobrador, quiero registrar pagos correspondientes a períodos futuros, para conservar correctamente los pagos que un cliente realiza por adelantado.

**Criterios de aceptación:**

* El sistema debe permitir seleccionar un período futuro.  
* Debe permitir registrar el monto recibido.  
* El pago debe quedar asociado al período futuro correspondiente.  
* El historial del cliente debe reflejar que el período fue pagado anticipadamente.  
* El registro debe conservar el valor correspondiente al servicio y período.

**HU11 – Consulta de historial de pagos**

Como cobrador, quiero consultar el historial de pagos de un cliente asignado, para verificar los pagos realizados y conocer el estado de sus períodos.

**Criterios de aceptación:**

* El cobrador debe poder consultar el historial de los clientes asignados.  
* El historial debe mostrar la fecha real del pago.  
* Debe mostrar el período al que corresponde.  
* Debe mostrar el monto registrado.  
* Debe mostrar información relevante del servicio.  
* Debe mostrar el usuario responsable cuando corresponda.

**HU12 – Generación de recibo**

Como cobrador, quiero generar un recibo asociado a un pago, para proporcionar un comprobante de la operación realizada.

**Criterios de aceptación:**

* El recibo debe estar asociado a un pago registrado.  
* Debe incluir información del cliente.  
* Debe incluir información del servicio y período.  
* Debe incluir el monto pagado.  
* Debe incluir la fecha correspondiente al pago.  
* El sistema debe generar inicialmente el recibo en formato PDF.


**HU13 – Consulta de auditoría**

Como administrador, quiero consultar la trazabilidad de las operaciones realizadas, para conocer quién realizó cada operación y cuándo fue ejecutada.

**Criterios de aceptación:**

* El administrador debe poder consultar los registros de auditoría.  
* Cada registro debe identificar al usuario responsable.  
* Debe registrar la fecha y hora de la operación.  
* Debe identificar la acción realizada.  
* Debe permitir relacionar la operación con el registro afectado cuando corresponda.  
* Los cobradores no deben poder modificar los registros de auditoría.

**HU14 – Generación de reportes**

Como administrador, quiero generar reportes sobre la información registrada, para consultar y analizar las operaciones relacionadas con clientes, servicios y pagos.

**Criterios de aceptación:**

* El administrador debe poder seleccionar el tipo de información que desea consultar.  
* Debe poder aplicar filtros según los criterios disponibles.  
* El sistema debe mostrar los resultados obtenidos.  
* Los resultados deben utilizar información actualizada de la base de datos.

**HU15 – Exportación de reportes**

Como administrador, quiero exportar los reportes generados, para poder almacenar, compartir o utilizar la información fuera del sistema.

**Criterios de aceptación:**

* El administrador debe poder exportar los resultados de un reporte.  
* El sistema debe permitir generar archivos PDF.  
* El sistema debe permitir generar archivos Excel (.xlsx).  
* La información exportada debe corresponder a los filtros y resultados seleccionados.

**HU16 – Importación de información desde Excel**

Como administrador, quiero importar información histórica desde archivos Excel, para incorporar los datos actuales de la empresa al nuevo sistema sin tener que registrarlos manualmente.

**Criterios de aceptación:**

* El administrador debe poder seleccionar un archivo Excel.  
* El sistema debe validar la estructura del archivo.  
* El sistema debe validar los datos antes de incorporarlos.  
* Los registros válidos deben poder ser incorporados a la base de datos.  
* Los registros inválidos deben ser identificados y reportados.  
* La importación no debe generar registros incompletos o inconsistentes.

**HU17 – Respaldo de información**

Como administrador, quiero contar con respaldos de la información del sistema, para poder recuperar los datos ante una pérdida o fallo de infraestructura.

**Criterios de aceptación:**

* La base de datos debe contar con mecanismos de respaldo.  
* Los respaldos deben conservar información suficiente para recuperar los datos.  
* Debe existir un procedimiento de restauración.  
* El proceso de restauración debe ser probado durante el desarrollo.

**HU18 – Despliegue del frontend**

Como desarrollador, quiero desplegar el frontend Angular en la infraestructura Cloud definida, para disponer de una versión accesible del sistema en un ambiente de pruebas.

**Criterios de aceptación:**

* El proyecto Angular debe poder compilarse correctamente.  
* Los archivos generados deben poder almacenarse en Amazon S3.  
* El frontend debe poder accederse mediante HTTPS.  
* El frontend debe poder comunicarse con la API desplegada.

**HU19 – Despliegue del backend**

Como desarrollador, quiero desplegar el backend Spring Boot en Amazon EC2, para disponer de una API accesible desde el frontend.

**Criterios de aceptación:**

* El backend debe poder compilarse correctamente.  
* La aplicación Spring Boot debe ejecutarse correctamente en EC2.  
* La API debe poder recibir solicitudes desde el frontend.  
* La comunicación externa debe utilizar HTTPS.  
* El backend debe poder comunicarse con la base de datos PostgreSQL administrada mediante RDS.

**HU20 – Integración y despliegue continuo**

Como desarrollador, quiero automatizar el proceso de construcción y despliegue mediante GitHub Actions, para reducir errores manuales y verificar los cambios realizados en el código.

**Criterios de aceptación:**

* Los cambios enviados al repositorio deben activar el pipeline correspondiente.  
* El pipeline debe ejecutar el proceso de build.  
* Los errores del proceso deben ser identificados.  
* El resultado del pipeline debe quedar registrado en GitHub Actions.  
* Los despliegues deberán realizarse únicamente cuando se cumplan las condiciones definidas para el ambiente correspondiente.

# **19\. Estrategia GitFlow** {#19.-estrategia-gitflow}

El proyecto utilizará una estrategia basada en GitFlow para organizar el desarrollo y mantener una separación clara entre las versiones estables y los cambios que se encuentren en desarrollo.

La rama main contendrá las versiones estables del sistema destinadas a producción. La rama develop será utilizada como rama principal de integración, donde se incorporarán las funcionalidades antes de ser consideradas para una versión estable.

Las nuevas funcionalidades serán desarrolladas mediante ramas feature/, creadas a partir de develop. Una vez finalizado el desarrollo y verificadas las pruebas correspondientes, los cambios serán integrados nuevamente mediante un Pull Request.

Los errores críticos encontrados en una versión estable serán atendidos mediante ramas hotfix/, creadas a partir de main. Una vez corregido el problema, el cambio deberá integrarse tanto en main como en develop para evitar que la corrección se pierda en versiones posteriores.

La estructura general de ramas será:

main  
 \|  
 \+---- hotfix/error-pago  
 \|  
develop  
 \|  
\+---- feature/auth  
 \|  
 \+---- feature/clientes  
 \|  
 \+---- feature/pagos  
 \|  
 \+---- feature/reportes

Para mantener una nomenclatura uniforme, las ramas seguirán una estructura descriptiva de acuerdo con el tipo de cambio realizado. Por ejemplo:

* feature/auth  
* feature/clientes  
* feature/servicios  
* feature/pagos  
* feature/reportes  
* feature/importacion-excel  
* feature/infraestructura-cloud  
* hotfix/error-pago

Aunque el proyecto será desarrollado individualmente, se mantendrá el flujo de trabajo mediante Pull Requests para conservar la trazabilidad de los cambios y facilitar la revisión antes de integrar modificaciones importantes. En caso de que el proyecto académico requiera una revisión por otro integrante, esta se realizará de acuerdo con las posibilidades y lineamientos establecidos por el docente para proyectos individuales.

Los commits deberán utilizar mensajes descriptivos que permitan identificar de manera sencilla el propósito de cada cambio y facilitar la relación entre las modificaciones realizadas, las historias de usuario y las tareas registradas en Jira.

# 

# **20\. Diseño inicial de interfaz y mockups** {#20.-diseño-inicial-de-interfaz-y-mockups}

El sistema contará con una interfaz web orientada a facilitar las actividades administrativas y de cobro realizadas por el personal de Gravity. El diseño buscará mantener una navegación clara y consistente, diferenciando las funcionalidades disponibles según el rol del usuario autenticado.

El administrador contará con acceso a las funciones generales de gestión y supervisión del sistema, mientras que el cobrador dispondrá principalmente de las opciones relacionadas con sus clientes asignados, consulta de servicios, registro de pagos y generación de recibos.

Para el diseño inicial de las interfaces se utilizará Figma como herramienta de apoyo para elaborar los mockups y definir la estructura visual de las principales vistas antes de su implementación.

La propuesta visual utilizará como color primario el tono `#0B1D3A`, acompañado por una paleta de colores complementarios orientada a mantener una apariencia consistente y facilitar la diferenciación de elementos de la interfaz.

| Uso | Color |
| :---- | :---- |
| Color primario | `#0B1D3A` |
| Azul secundario | `#1E3A6E` |
| Azul claro | `#60A5FA` |
| Azul de interacción | `#3B7AF0` |
| Azul de énfasis | `#1E5CE6` |
| Gris para elementos secundarios | `#94A3B8` |

La selección de colores podrá ajustarse durante el proceso de diseño e implementación de acuerdo con criterios de legibilidad, contraste y consistencia visual. La paleta definida servirá como referencia inicial para componentes como botones, encabezados, navegación, indicadores de estado y elementos interactivos.

Las principales vistas previstas para el sistema son:

* Inicio de sesión.  
* Dashboard.  
* Gestión de empleados.  
* Gestión de clientes.  
* Gestión de servicios.  
* Asignación de clientes a cobradores.  
* Historial de pagos.  
* Registro de pago.  
* Generación de recibos.  
* Reportes.  
* Auditoría.

Los mockups serán elaborados progresivamente en Figma y se incorporarán al documento como representación visual de las principales pantallas y flujos del sistema. Estos diseños servirán como referencia para la posterior implementación del frontend en Angular y podrán evolucionar conforme se validen los requerimientos y se identifiquen nuevas necesidades durante el desarrollo.

Enlace a figma:

[https\://www\.figma.com/proto/T1pgbs7zV4gP9ob19ZaBDA/Proyecto-SS?node-id=1-3\&p=f\&t=BfhAvfocYlBXHDFn-1\&scaling=min-zoom\&content-scaling=fixed\&page-id=0%3A1](https://www.figma.com/proto/T1pgbs7zV4gP9ob19ZaBDA/Proyecto-SS?node-id=1-3&p=f&t=BfhAvfocYlBXHDFn-1&scaling=min-zoom&content-scaling=fixed&page-id=0%3A1)

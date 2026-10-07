# Sistema de Gestión de Biblioteca Escolar

Aplicación de escritorio hecha en **Java** con interfaz gráfica en **Swing (JFrame)** y base de datos **MySQL** (JDBC).
Permite registrar estudiantes, controlar el inventario de libros y administrar los préstamos y devoluciones de una biblioteca escolar.

---

## Funcionalidades

- **Inicio de sesión:** valida el correo y la contraseña contra la tabla `usuarios`.
- **Gestión de estudiantes:** agregar, modificar y eliminar estudiantes desde la interfaz (con validación de campos).
- **Inventario de libros:** tabla con todos los libros y su stock actual.
- **Préstamos y devoluciones:**
  - Registro de préstamos con descuento automático de stock.
  - La fecha de vencimiento se calcula sola (7 días desde el préstamo).
  - Reglas de negocio: máximo **2 libros activos por estudiante** y no se puede pedir **2 ejemplares del mismo libro**.
  - Registro de devoluciones con reposición de stock.
  - **Detección de atraso:** si un préstamo pasó su fecha límite se marca como `ATRASADO` y al devolverlo el sistema avisa cuántos días de atraso tiene.
- **Historial y reportes:**
  - Historial de todos los préstamos.
  - Libros más prestados.
  - Libros actualmente en préstamo.
  - Historial por estudiante.

Todas las pestañas se actualizan solas cuando se registra un cambio.

---

## Arquitectura y patrones de diseño

El proyecto está organizado en paquetes siguiendo el patrón **MVC**:

```
src/
├── config/
│   └── DatabaseConnection.java      → Singleton de la conexión a MySQL
├── model/                           → MODELO: clases con los datos
│   ├── Persona.java                 (clase abstracta)
│   ├── Usuario.java                 (hereda de Persona)
│   ├── Estudiante.java              (hereda de Persona)
│   ├── Libro.java
│   └── Prestamo.java
├── dao/                             → DAO: acceso a la base de datos
│   ├── CrudDAO.java                 (interfaz con guardar, actualizar, eliminar)
│   ├── UsuarioDAO.java
│   ├── EstudianteDAO.java
│   ├── LibroDAO.java
│   ├── PrestamoDAO.java
│   └── ReporteDAO.java
├── controller/                      → CONTROLADOR: lógica y reglas del negocio
│   ├── UsuarioController.java
│   ├── EstudianteController.java
│   ├── LibroController.java
│   ├── PrestamoController.java
│   └── ReporteController.java
└── ui/                              → VISTA: ventanas y pestañas
    ├── Main.java
    ├── PanelEstudiantes.java
    ├── PanelInventario.java
    ├── PanelPrestamos.java
    └── PanelReportes.java
```

### Patrones aplicados

- **MVC (Modelo – Vista – Controlador):** las vistas (`ui`) no hablan directo con la base de datos; le piden todo a los controladores, y estos usan los DAO y los modelos.
- **DAO (Data Access Object):** cada DAO se encarga solo de interactuar con la base de datos (consultas SQL con `PreparedStatement`).
- **Singleton:** `DatabaseConnection` tiene un constructor privado y el método estático `getInstance()`, de modo que toda la aplicación comparte una única conexión.

### Programación orientada a objetos

- **Herencia y clase abstracta:** `Usuario` y `Estudiante` heredan de la clase abstracta `Persona`.
- **Polimorfismo:** el método abstracto `getDescripcion()` de `Persona` se implementa de forma distinta en cada clase hija.
- **Interfaz:** `CrudDAO` define las operaciones que deben tener los DAO; `LibroDAO` y `EstudianteDAO` la implementan.
- **Encapsulamiento:** atributos privados con getters y setters.
- Las pestañas de la interfaz heredan de `JPanel`.

---

## Concurrencia y consistencia del stock

- **Hilo independiente:** el registro de un préstamo (validaciones, descuento de stock e inserción) se ejecuta en un `Thread` aparte, por lo que la interfaz **no se congela**. Los mensajes se muestran con `SwingUtilities.invokeLater`.
- **Sincronización:**
  - El bloque de préstamo usa `synchronized (LOCK)`, así solo un préstamo a la vez valida y descuenta stock.
  - Los métodos `reducirStock` y `aumentarStock` de `LibroDAO` son `synchronized`.
  - La consulta usa `WHERE stock > 0` para que el stock nunca quede negativo.
  - Si el préstamo no se puede registrar, el stock descontado se devuelve.

---

## Base de datos

Tablas del sistema:

```
usuarios    (id, nombre, rut, correo, contrasena, rol)
estudiantes (id, nombre, rut, curso, correo)
libros      (id, titulo, autor, isbn, editorial, stock, id_categoria)
categorias  (id, nombre)
prestamos   (id, id_estudiante, id_libro, fecha_prestamo, fecha_devolucion, devuelto)
```

## Cómo ejecutar el proyecto

1. **Clonar** el repositorio:
   ```bash
   git clone [URL-de-tu-repositorio]
   ```
2. **Crear la base de datos** en MySQL: crear una base llamada `biblioteca` y ejecutar los dos scripts de la carpeta `sql/`, en el orden indicado arriba.
3. **Abrir el proyecto** en IntelliJ IDEA.
4. **Agregar MySQL Connector/J** al proyecto (`File > Project Structure > Libraries > + > Java` y seleccionar el `.jar` del conector).
5. **Configurar la conexión** en `src/config/DatabaseConnection.java`, cambiando el usuario y la contraseña de MySQL por los tuyos:
   ```java
   private static final String USER = "root";
   private static final String PASS = "tu_contraseña";
   ```
6. **Ejecutar** la clase `ui.Main`.
7. Ingresar con un usuario del script de datos de ejemplo (tabla `usuarios`).

---


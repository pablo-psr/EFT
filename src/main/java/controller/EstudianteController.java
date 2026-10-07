package controller;

import dao.EstudianteDAO;
import model.Estudiante;
import java.util.List;

/**
 * Controlador (la C de MVC) de la gestión de estudiantes.
 * <p>
 * Recibe los datos que escribe el usuario en la pantalla, los revisa (validaciones)
 * y, si están bien, le pide al DAO que los guarde en la base de datos.
 * <p>
 * Los métodos guardar, actualizar y eliminar devuelven un String:
 * null significa que todo salió bien; si no, devuelven el mensaje de error para mostrar.
 */
public class EstudianteController {
    // El controlador usa el DAO para hablar con la base de datos
    private EstudianteDAO dao = new EstudianteDAO();

    /**
     * Entrega todos los estudiantes registrados.
     *
     * @return lista de estudiantes
     */
    public List<Estudiante> listar() {
        return dao.listarTodos();
    }

    /**
     * Registra un estudiante nuevo.
     *
     * @return null si se guardó bien, o el mensaje de error
     */
    public String guardar(String nombre, String rut, String curso, String correo) {
        String error = validar(nombre, rut, curso, correo);
        if (error != null) {
            return error;
        }
        // El id es 0 porque lo asigna MySQL automáticamente al insertar
        Estudiante est = new Estudiante(0, nombre.trim(), rut.trim(), curso.trim(), correo.trim());
        if (dao.guardar(est)) {
            return null;
        }
        return "No se pudo guardar el estudiante. Revise los datos (por ejemplo, un RUT o correo repetido).";
    }

    /**
     * Modifica los datos de un estudiante que ya existe.
     *
     * @param id id del estudiante a modificar
     * @return null si se actualizó bien, o el mensaje de error
     */
    public String actualizar(int id, String nombre, String rut, String curso, String correo) {
        String error = validar(nombre, rut, curso, correo);
        if (error != null) {
            return error;
        }
        Estudiante est = new Estudiante(id, nombre.trim(), rut.trim(), curso.trim(), correo.trim());
        if (dao.actualizar(est)) {
            return null;
        }
        return "No se pudo actualizar el estudiante. Revise los datos.";
    }

    /**
     * Elimina un estudiante.
     *
     * @param id id del estudiante a eliminar
     * @return null si se eliminó bien, o el mensaje de error
     */
    public String eliminar(int id) {
        if (dao.eliminar(id)) {
            return null;
        }
        return "No se pudo eliminar. Es posible que el estudiante tenga préstamos registrados.";
    }

    // Revisa que no haya campos vacíos y que el correo tenga forma de correo
    private String validar(String nombre, String rut, String curso, String correo) {
        if (nombre.trim().isEmpty() || rut.trim().isEmpty() || curso.trim().isEmpty() || correo.trim().isEmpty()) {
            return "Todos los campos son obligatorios.";
        }
        if (!correo.contains("@")) {
            return "El correo no es válido (debe contener @).";
        }
        return null;
    }
}

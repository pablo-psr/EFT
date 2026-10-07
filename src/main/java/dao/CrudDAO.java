package dao;

/**
 * Interfaz que define las operaciones de escritura que debe tener un DAO.
 * <p>
 * Una interfaz es como una lista de tareas obligatorias: cada DAO que diga
 * "implements CrudDAO" está obligado a escribir estos métodos (guardar, actualizar
 * y eliminar). Cada DAO los hace a su manera: LibroDAO trabaja con la tabla libros
 * y EstudianteDAO con la tabla estudiantes.
 * <p>
 * Los métodos reciben un Object porque sirven para cualquier entidad; dentro de cada
 * DAO se convierte (cast) al tipo que corresponde. El método listarTodos() no está aquí
 * porque cada DAO devuelve una lista de objetos distintos.
 */
public interface CrudDAO {

    /**
     * Inserta un registro nuevo en la tabla.
     *
     * @param objeto el objeto a guardar
     * @return true si se guardó correctamente
     */
    boolean guardar(Object objeto);

    /**
     * Modifica un registro que ya existe (se identifica por su id).
     *
     * @param objeto el objeto con los datos nuevos
     * @return true si se actualizó correctamente
     */
    boolean actualizar(Object objeto);

    /**
     * Borra un registro de la tabla.
     *
     * @param id id del registro a eliminar
     * @return true si se eliminó correctamente
     */
    boolean eliminar(int id);
}

package dao;

import config.DatabaseConnection;
import model.Estudiante;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de la tabla "estudiantes": todo el acceso a la base de datos relacionado con estudiantes.
 * Implementa {@link CrudDAO} (guardar, actualizar y eliminar) y tiene su propio listarTodos().
 * <p>
 * Nota: la conexión NO se cierra en los métodos, porque es la conexión única
 * del Singleton y se reutiliza. Solo se cierran el PreparedStatement y el ResultSet.
 */
public class EstudianteDAO implements CrudDAO {

    /**
     * Lee todos los estudiantes de la tabla.
     *
     * @return lista de estudiantes (vacía si hay un error o no hay estudiantes)
     */
    public List<Estudiante> listarTodos() {
        List<Estudiante> lista = new ArrayList<>();
        String sql = "SELECT * FROM estudiantes";
        // Pedimos la conexión única al Singleton
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            // Cada vuelta del while es una fila de la tabla, y se convierte en un objeto Estudiante
            while (rs.next()) {
                lista.add(new Estudiante(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("rut"),
                        rs.getString("curso"),
                        rs.getString("correo")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Inserta un estudiante nuevo.
     *
     * @param objeto estudiante con los datos a guardar
     * @return true si se guardó correctamente
     */
    @Override
    public boolean guardar(Object objeto) {
        // La interfaz recibe un Object; aquí sabemos que es un Estudiante, por eso se convierte (cast)
        Estudiante est = (Estudiante) objeto;
        String sql = "INSERT INTO estudiantes (nombre, rut, curso, correo) VALUES (?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Cada número es la posición del ? en el SQL, en orden
            ps.setString(1, est.getNombre());
            ps.setString(2, est.getRut());
            ps.setString(3, est.getCurso());
            ps.setString(4, est.getCorreo());
            // executeUpdate devuelve cuántas filas cambió; si es mayor que 0, funcionó
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Modifica los datos de un estudiante que ya existe.
     *
     * @param objeto estudiante con los datos nuevos (se busca por su id)
     * @return true si se actualizó correctamente
     */
    @Override
    public boolean actualizar(Object objeto) {
        // La interfaz recibe un Object; aquí sabemos que es un Estudiante, por eso se convierte (cast)
        Estudiante est = (Estudiante) objeto;
        String sql = "UPDATE estudiantes SET nombre = ?, rut = ?, curso = ?, correo = ? WHERE id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, est.getNombre());
            ps.setString(2, est.getRut());
            ps.setString(3, est.getCurso());
            ps.setString(4, est.getCorreo());
            // El 5.° ? es el id del WHERE: indica cuál estudiante se modifica
            ps.setInt(5, est.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina un estudiante.
     * Si el estudiante ya tiene préstamos, MySQL no deja borrarlo (llave foránea)
     * y este método devuelve false.
     *
     * @param id id del estudiante a eliminar
     * @return true si se eliminó correctamente
     */
    @Override
    public boolean eliminar(int id) {
        String sql = "DELETE FROM estudiantes WHERE id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

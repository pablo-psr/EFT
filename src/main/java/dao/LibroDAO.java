package dao;

import config.DatabaseConnection;
import model.Libro;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de la tabla "libros": todo el acceso a la base de datos relacionado con libros.
 * Implementa {@link CrudDAO} (guardar, actualizar y eliminar), tiene su propio
 * listarTodos() y además maneja el stock con métodos sincronizados.
 * <p>
 * Nota: la conexión NO se cierra en los métodos, porque es la conexión única
 * del Singleton y se reutiliza. Solo se cierran el PreparedStatement y el ResultSet.
 */
public class LibroDAO implements CrudDAO {

    /**
     * Lee todos los libros de la tabla.
     *
     * @return lista de libros (vacía si hay un error o no hay libros)
     */
    public List<Libro> listarTodos() {
        List<Libro> lista = new ArrayList<>();
        String sql = "SELECT * FROM libros";
        // Pedimos la conexión única al Singleton
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            // Cada vuelta del while es una fila de la tabla, y se convierte en un objeto Libro
            while (rs.next()) {
                lista.add(new Libro(
                        rs.getInt("id"),
                        rs.getString("titulo"),
                        rs.getString("autor"),
                        rs.getString("isbn"),
                        rs.getString("editorial"),
                        rs.getInt("stock"),
                        rs.getInt("id_categoria")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Inserta un libro nuevo.
     *
     * @param objeto libro con los datos a guardar
     * @return true si se guardó correctamente
     */
    @Override
    public boolean guardar(Object objeto) {
        // La interfaz recibe un Object; aquí sabemos que es un Libro, por eso se convierte (cast)
        Libro libro = (Libro) objeto;
        String sql = "INSERT INTO libros (titulo, autor, isbn, editorial, stock, id_categoria) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Cada número es la posición del ? en el SQL, en orden
            ps.setString(1, libro.getTitulo());
            ps.setString(2, libro.getAutor());
            ps.setString(3, libro.getIsbn());
            ps.setString(4, libro.getEditorial());
            ps.setInt(5, libro.getStock());
            ps.setInt(6, libro.getIdCategoria());
            // executeUpdate devuelve cuántas filas cambió; si es mayor que 0, funcionó
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Modifica los datos de un libro que ya existe.
     *
     * @param objeto libro con los datos nuevos (se busca por su id)
     * @return true si se actualizó correctamente
     */
    @Override
    public boolean actualizar(Object objeto) {
        // La interfaz recibe un Object; aquí sabemos que es un Libro, por eso se convierte (cast)
        Libro libro = (Libro) objeto;
        String sql = "UPDATE libros SET titulo = ?, autor = ?, isbn = ?, editorial = ?, stock = ?, id_categoria = ? WHERE id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, libro.getTitulo());
            ps.setString(2, libro.getAutor());
            ps.setString(3, libro.getIsbn());
            ps.setString(4, libro.getEditorial());
            ps.setInt(5, libro.getStock());
            ps.setInt(6, libro.getIdCategoria());
            // El 7.° ? es el id del WHERE: indica cuál libro se modifica
            ps.setInt(7, libro.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina un libro.
     * Si el libro ya tiene préstamos, MySQL no deja borrarlo (llave foránea)
     * y este método devuelve false.
     *
     * @param id id del libro a eliminar
     * @return true si se eliminó correctamente
     */
    @Override
    public boolean eliminar(int id) {
        String sql = "DELETE FROM libros WHERE id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Descuenta 1 ejemplar del stock de un libro.
     * <p>
     * Es synchronized: si dos hilos intentan prestar el mismo libro al mismo tiempo,
     * uno espera al otro. Así se evitan condiciones de carrera (que el stock quede mal).
     * La condición "stock > 0" impide que el stock quede negativo.
     *
     * @param idLibro id del libro
     * @return true si se descontó; false si no había stock o hubo un error
     */
    public synchronized boolean reducirStock(int idLibro) {
        String sql = "UPDATE libros SET stock = stock - 1 WHERE id = ? AND stock > 0";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idLibro);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Suma 1 ejemplar al stock de un libro (se usa al devolverlo).
     * Es synchronized por la misma razón que {@link #reducirStock(int)}.
     *
     * @param idLibro id del libro
     * @return true si se actualizó correctamente
     */
    public synchronized boolean aumentarStock(int idLibro) {
        String sql = "UPDATE libros SET stock = stock + 1 WHERE id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idLibro);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

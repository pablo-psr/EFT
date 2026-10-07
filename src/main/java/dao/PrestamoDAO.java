package dao;

import config.DatabaseConnection;
import model.Prestamo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de la tabla "prestamos": registra préstamos y devoluciones, y hace consultas
 * como cuántos libros tiene un estudiante en este momento.
 * <p>
 * Nota: la conexión NO se cierra en los métodos, porque es la conexión única
 * del Singleton y se reutiliza.
 */
public class PrestamoDAO {

    /**
     * Clase DTO (Data Transfer Object): solo sirve para transportar datos.
     * Guarda un préstamo pendiente junto con el nombre del estudiante y el título
     * del libro, para mostrarlo en la pestaña de devoluciones.
     * Es "static" porque se puede usar sin necesidad de crear un PrestamoDAO.
     */
    public static class PrestamoDetalle {
        private int idPrestamo;
        private int idLibro;
        private String estudianteNombre;
        private String libroTitulo;
        // Días que lleva de atraso (0 o menos significa que está en plazo)
        private int diasAtraso;

        public PrestamoDetalle(int idPrestamo, int idLibro, String estudianteNombre, String libroTitulo, int diasAtraso) {
            this.idPrestamo = idPrestamo;
            this.idLibro = idLibro;
            this.estudianteNombre = estudianteNombre;
            this.libroTitulo = libroTitulo;
            this.diasAtraso = diasAtraso;
        }

        public int getIdPrestamo() { return idPrestamo; }
        public int getIdLibro() { return idLibro; }
        public int getDiasAtraso() { return diasAtraso; }

        // toString define cómo se ve este objeto dentro del JComboBox de devoluciones
        @Override
        public String toString() {
            String texto = "Préstamo #" + idPrestamo + " - " + estudianteNombre + " | Libro: " + libroTitulo;
            // Si pasó la fecha límite, se avisa en el texto del combo
            if (diasAtraso > 0) {
                texto += " | ATRASADO " + diasAtraso + " día(s)";
            }
            return texto;
        }
    }

    /**
     * Obtiene los préstamos que todavía no se devuelven, con el nombre del
     * estudiante y el título del libro.
     *
     * @return lista de préstamos pendientes
     */
    public List<PrestamoDetalle> listarPendientesConDetalle() {
        List<PrestamoDetalle> lista = new ArrayList<>();
        // INNER JOIN une tablas: así traemos el nombre del estudiante (tabla estudiantes)
        // y el título del libro (tabla libros) de cada préstamo no devuelto (devuelto = FALSE)
        // DATEDIFF(hoy, fecha límite) da los días de diferencia: si es mayor que 0, está atrasado
        String sql = "SELECT p.id AS id_prestamo, p.id_libro, e.nombre AS estudiante, l.titulo AS libro, " +
                "DATEDIFF(CURRENT_DATE(), p.fecha_devolucion) AS dias_atraso " +
                "FROM prestamos p " +
                "INNER JOIN estudiantes e ON p.id_estudiante = e.id " +
                "INNER JOIN libros l ON p.id_libro = l.id " +
                "WHERE p.devuelto = FALSE";

        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new PrestamoDetalle(
                        rs.getInt("id_prestamo"),
                        rs.getInt("id_libro"),
                        rs.getString("estudiante"),
                        rs.getString("libro"),
                        rs.getInt("dias_atraso")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Lee todos los préstamos (se usa para el historial).
     *
     * @return lista con todos los préstamos
     */
    public List<Prestamo> listarTodos() {
        List<Prestamo> lista = new ArrayList<>();
        String sql = "SELECT * FROM prestamos";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new Prestamo(
                        rs.getInt("id"),
                        rs.getInt("id_estudiante"),
                        rs.getInt("id_libro"),
                        rs.getDate("fecha_prestamo"),
                        rs.getDate("fecha_devolucion"),
                        rs.getBoolean("devuelto")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Cuenta cuántos libros tiene prestados un estudiante sin devolver.
     * Sirve para la regla de máximo 2 libros por estudiante.
     *
     * @param idEstudiante id del estudiante
     * @return cantidad de préstamos activos (0 si hay un error)
     */
    public int contarPrestamosActivosEstudiante(int idEstudiante) {
        String sql = "SELECT COUNT(*) FROM prestamos WHERE id_estudiante = ? AND devuelto = FALSE";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                // getInt(1) lee la primera columna del resultado, que es el COUNT(*)
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Revisa si un estudiante ya tiene prestado (sin devolver) un libro específico.
     * Sirve para la regla de no pedir 2 ejemplares del mismo libro.
     *
     * @param idEstudiante id del estudiante
     * @param idLibro      id del libro
     * @return true si ya lo tiene prestado
     */
    public boolean estudianteTieneLibroActivo(int idEstudiante, int idLibro) {
        String sql = "SELECT COUNT(*) FROM prestamos WHERE id_estudiante = ? AND id_libro = ? AND devuelto = FALSE";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
            ps.setInt(2, idLibro);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Registra un préstamo nuevo. La fecha de préstamo es hoy y la de devolución
     * se calcula automáticamente a 7 días.
     *
     * @param idEstudiante id del estudiante que pide el libro
     * @param idLibro      id del libro prestado
     * @return true si se registró correctamente
     */
    public boolean registrar(int idEstudiante, int idLibro) {
        // CURRENT_DATE() es la fecha de hoy; DATE_ADD(... INTERVAL 7 DAY) es hoy + 7 días
        String sql = "INSERT INTO prestamos (id_estudiante, id_libro, fecha_prestamo, fecha_devolucion, devuelto) " +
                "VALUES (?, ?, CURRENT_DATE(), DATE_ADD(CURRENT_DATE(), INTERVAL 7 DAY), FALSE)";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEstudiante);
            ps.setInt(2, idLibro);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Marca un préstamo como devuelto (devuelto = TRUE).
     * Ojo: este método no repone el stock, eso lo hace el controlador con LibroDAO.
     *
     * @param idPrestamo id del préstamo
     * @return true si se actualizó correctamente
     */
    public boolean devolver(int idPrestamo) {
        String sql = "UPDATE prestamos SET devuelto = TRUE WHERE id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPrestamo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

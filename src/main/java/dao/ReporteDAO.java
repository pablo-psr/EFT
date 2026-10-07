package dao;

import config.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de los reportes: hace consultas que juntan datos de varias tablas.
 * <p>
 * Cada método devuelve una lista de filas; cada fila es un arreglo Object[] con los
 * valores de las columnas, listo para agregarse a una JTable.
 */
public class ReporteDAO {

    /**
     * Reporte 1: los 10 libros que más veces se han prestado.
     *
     * @return filas con título, autor y cantidad de préstamos
     */
    public List<Object[]> librosMasPrestados() {
        List<Object[]> filas = new ArrayList<>();
        // COUNT cuenta los préstamos de cada libro; GROUP BY agrupa por libro;
        // ORDER BY ... DESC los ordena de mayor a menor; LIMIT 10 deja solo los 10 primeros
        String sql = "SELECT l.titulo, l.autor, COUNT(p.id) AS veces " +
                "FROM prestamos p " +
                "INNER JOIN libros l ON p.id_libro = l.id " +
                "GROUP BY l.id, l.titulo, l.autor " +
                "ORDER BY veces DESC " +
                "LIMIT 10";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                filas.add(new Object[]{
                        rs.getString("titulo"),
                        rs.getString("autor"),
                        rs.getInt("veces")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return filas;
    }

    /**
     * Reporte 2: todos los préstamos de un estudiante (su historial).
     *
     * @param idEstudiante id del estudiante
     * @return filas con id del préstamo, libro, fechas y estado
     */
    public List<Object[]> historialPorEstudiante(int idEstudiante) {
        List<Object[]> filas = new ArrayList<>();
        String sql = "SELECT p.id, l.titulo, p.fecha_prestamo, p.fecha_devolucion, p.devuelto " +
                "FROM prestamos p " +
                "INNER JOIN libros l ON p.id_libro = l.id " +
                "WHERE p.id_estudiante = ? " +
                "ORDER BY p.fecha_prestamo DESC";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date limite = rs.getDate("fecha_devolucion");
                    // Estado: devuelto, atrasado (pasó la fecha límite y no lo devolvió) o en préstamo
                    String estado;
                    if (rs.getBoolean("devuelto")) {
                        estado = "Devuelto";
                    } else if (limite != null && limite.toLocalDate().isBefore(LocalDate.now())) {
                        estado = "ATRASADO";
                    } else {
                        estado = "En Préstamo";
                    }
                    filas.add(new Object[]{
                            rs.getInt("id"),
                            rs.getString("titulo"),
                            rs.getDate("fecha_prestamo"),
                            limite,
                            estado
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return filas;
    }

    /**
     * Reporte 3: libros que están prestados en este momento (sin devolver).
     *
     * @return filas con título, autor, estudiante, fechas y estado del plazo
     */
    public List<Object[]> librosActualmenteEnPrestamo() {
        List<Object[]> filas = new ArrayList<>();
        String sql = "SELECT l.titulo, l.autor, e.nombre AS estudiante, p.fecha_prestamo, p.fecha_devolucion " +
                "FROM prestamos p " +
                "INNER JOIN libros l ON p.id_libro = l.id " +
                "INNER JOIN estudiantes e ON p.id_estudiante = e.id " +
                "WHERE p.devuelto = FALSE " +
                "ORDER BY p.fecha_devolucion";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Date limite = rs.getDate("fecha_devolucion");
                // Si la fecha límite ya pasó, el préstamo está atrasado
                String estado;
                if (limite != null && limite.toLocalDate().isBefore(LocalDate.now())) {
                    estado = "ATRASADO";
                } else {
                    estado = "En plazo";
                }
                filas.add(new Object[]{
                        rs.getString("titulo"),
                        rs.getString("autor"),
                        rs.getString("estudiante"),
                        rs.getDate("fecha_prestamo"),
                        limite,
                        estado
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return filas;
    }
}

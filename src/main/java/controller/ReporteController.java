package controller;

import dao.PrestamoDAO;
import dao.ReporteDAO;
import model.Prestamo;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador (la C de MVC) de los reportes y del historial.
 * Es el intermediario entre la pantalla de reportes y los DAO:
 * la vista le pide los datos a este controlador y no habla directo con la base de datos.
 */
public class ReporteController {
    private ReporteDAO dao = new ReporteDAO();
    private PrestamoDAO prestamoDAO = new PrestamoDAO();

    /**
     * Arma el historial con todos los préstamos. Convierte cada objeto Prestamo en una
     * fila de la tabla, y calcula el estado: Devuelto, ATRASADO o En Préstamo.
     *
     * @return filas con ids, fechas y estado de cada préstamo
     */
    public List<Object[]> historialCompleto() {
        List<Object[]> filas = new ArrayList<>();
        for (Prestamo p : prestamoDAO.listarTodos()) {
            String estado;
            if (p.isDevuelto()) {
                estado = "Devuelto";
            } else if (p.estaAtrasado()) {
                estado = "ATRASADO (" + p.getDiasAtraso() + " días)";
            } else {
                estado = "En Préstamo";
            }
            filas.add(new Object[]{
                    p.getId(), p.getIdEstudiante(), p.getIdLibro(),
                    p.getFechaPrestamo(), p.getFechaDevolucion(), estado
            });
        }
        return filas;
    }

    /**
     * @return los 10 libros más prestados (título, autor, veces)
     */
    public List<Object[]> librosMasPrestados() {
        return dao.librosMasPrestados();
    }

    /**
     * @param idEstudiante id del estudiante
     * @return el historial de préstamos de ese estudiante
     */
    public List<Object[]> historialPorEstudiante(int idEstudiante) {
        return dao.historialPorEstudiante(idEstudiante);
    }

    /**
     * @return los libros que están prestados actualmente
     */
    public List<Object[]> librosActualmenteEnPrestamo() {
        return dao.librosActualmenteEnPrestamo();
    }
}

package controller;

import dao.LibroDAO;
import dao.PrestamoDAO;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.util.List;

/**
 * Controlador (la C de MVC) de préstamos y devoluciones.
 * <p>
 * Es el intermediario entre la vista (Main) y los DAO: la ventana solo llama a este
 * controlador, y él aplica las reglas del negocio, usa los DAO y avisa el resultado.
 * Todo se hace en un hilo aparte para que la ventana no se congele.
 */
public class PrestamoController {
    // El controlador usa los DAO para hablar con la base de datos
    private LibroDAO libroDAO = new LibroDAO();
    private PrestamoDAO prestamoDAO = new PrestamoDAO();

    // Candado compartido (static = el mismo para todos los controladores):
    // solo un préstamo a la vez puede validar y descontar stock
    private static final Object LOCK = new Object();

    /**
     * Entrega los préstamos que todavía no se devuelven (para llenar el combo de devoluciones).
     *
     * @return lista de préstamos pendientes, con el nombre del estudiante y el título del libro
     */
    public List<PrestamoDAO.PrestamoDetalle> listarPendientes() {
        return prestamoDAO.listarPendientesConDetalle();
    }

    /**
     * Registra un préstamo en segundo plano (en otro hilo).
     * <p>
     * Pasos: 1) valida máximo 2 libros por estudiante, 2) valida que no tenga ya ese
     * libro, 3) descuenta el stock y 4) guarda el préstamo. Si el paso 4 falla,
     * se devuelve el stock. Al terminar, ejecuta "alFinalizar" para refrescar la pantalla.
     *
     * @param idEstudiante id del estudiante que pide el libro
     * @param idLibro      id del libro pedido
     * @param alFinalizar  código a ejecutar cuando el préstamo se registró bien (puede ser null)
     */
    public void registrarPrestamoAsync(int idEstudiante, int idLibro, Runnable alFinalizar) {
        // new Thread(...) crea un hilo: se ejecuta en paralelo y la ventana no se congela
        new Thread(() -> {
            // synchronized: solo un hilo a la vez puede entrar a este bloque (evita condiciones de carrera)
            synchronized (LOCK) {
                // 1. Validación: Máximo 2 libros por estudiante
                int activos = prestamoDAO.contarPrestamosActivosEstudiante(idEstudiante);
                if (activos >= 2) {
                    // invokeLater: los cuadros de diálogo (Swing) deben mostrarse desde el hilo de la
                    // interfaz, no desde este hilo de fondo
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(null,
                                "Límite alcanzado: Un estudiante no puede tener más de 2 libros en préstamo simultáneamente.",
                                "Regla de Negocio",
                                JOptionPane.WARNING_MESSAGE);
                    });
                    return;
                }

                // 2. Validación: No se puede pedir más de 1 ejemplar del mismo libro
                boolean yaTieneMismoLibro = prestamoDAO.estudianteTieneLibroActivo(idEstudiante, idLibro);
                if (yaTieneMismoLibro) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(null,
                                "El estudiante ya tiene prestado un ejemplar de este libro actualmente.",
                                "Regla de Negocio",
                                JOptionPane.WARNING_MESSAGE);
                    });
                    return;
                }

                // 3. Proceso de stock e inserción
                boolean stockOK = libroDAO.reducirStock(idLibro);
                if (stockOK) {
                    boolean exito = prestamoDAO.registrar(idEstudiante, idLibro);
                    // Si no se pudo registrar el préstamo, devolvemos el stock que se descontó
                    if (!exito) {
                        libroDAO.aumentarStock(idLibro);
                    }
                    SwingUtilities.invokeLater(() -> {
                        if (exito) {
                            JOptionPane.showMessageDialog(null, "¡Préstamo registrado exitosamente!");
                            if (alFinalizar != null) alFinalizar.run();
                        } else {
                            JOptionPane.showMessageDialog(null, "Error al registrar el préstamo.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(null, "No hay stock disponible para este libro.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    });
                }
            }
        }).start();
    }

    /**
     * Registra una devolución en segundo plano (en otro hilo).
     * Marca el préstamo como devuelto y repone 1 ejemplar al stock del libro.
     *
     * @param idPrestamo  id del préstamo que se devuelve
     * @param idLibro     id del libro devuelto (para reponer su stock)
     * @param diasAtraso  días de atraso del préstamo (0 o menos si se devuelve a tiempo)
     * @param alFinalizar código a ejecutar cuando la devolución se registró bien (puede ser null)
     */
    public void registrarDevolucionAsync(int idPrestamo, int idLibro, int diasAtraso, Runnable alFinalizar) {
        // Hilo aparte para no congelar la ventana
        new Thread(() -> {
            boolean devuelto = prestamoDAO.devolver(idPrestamo);
            if (devuelto) {
                libroDAO.aumentarStock(idLibro);
                SwingUtilities.invokeLater(() -> {
                    String mensaje = "¡Devolución registrada correctamente! El stock fue actualizado.";
                    if (diasAtraso > 0) {
                        // Constancia del atraso: se le informa al usuario cuántos días se pasó del plazo
                        mensaje += "\n\nATENCIÓN: la devolución tiene " + diasAtraso + " día(s) de atraso.";
                    }
                    // Tipo de ícono del cuadro: aviso si hay atraso, información si no
                    int tipo = JOptionPane.INFORMATION_MESSAGE;
                    if (diasAtraso > 0) {
                        tipo = JOptionPane.WARNING_MESSAGE;
                    }
                    JOptionPane.showMessageDialog(null, mensaje, "Devolución", tipo);
                    if (alFinalizar != null) alFinalizar.run();
                });
            } else {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null, "Error al procesar la devolución.", "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
}

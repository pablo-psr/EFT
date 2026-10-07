package ui;

import controller.EstudianteController;
import controller.ReporteController;
import model.Estudiante;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

/**
 * Vista (la V de MVC) del historial y los reportes, juntos en una sola pestaña.
 * <p>
 * Tiene botones arriba, uno por cada reporte, y una sola tabla donde se muestra el resultado.
 * Como cada reporte tiene columnas distintas, la tabla cambia sus columnas cada vez.
 * Al abrir el programa se muestra el historial completo de préstamos.
 * <p>
 * Es un JPanel (extends JPanel) que se agrega como pestaña en la ventana principal.
 */
public class PanelReportes extends JPanel {
    private ReporteController reporteController = new ReporteController();
    private EstudianteController estudianteController = new EstudianteController();

    // Modelo de la tabla, sin columnas al inicio; se definen al generar cada reporte
    private DefaultTableModel modelo = new DefaultTableModel();
    // Combo para elegir de qué estudiante se quiere el historial
    private JComboBox<Estudiante> comboEstudiantes = new JComboBox<>();

    // Número del reporte que se está mostrando, para poder volver a generarlo cuando cambian los datos:
    // 1 = historial completo, 2 = más prestados, 3 = en préstamo, 4 = historial de un estudiante
    private int reporteActual = 1;

    /**
     * Arma la pantalla: los botones arriba y la tabla de resultados al centro.
     */
    public PanelReportes() {
        setLayout(new BorderLayout());

        JButton btnHistorialCompleto = new JButton("Historial de préstamos");
        JButton btnMasPrestados = new JButton("Libros más prestados");
        JButton btnEnPrestamo = new JButton("Libros actualmente en préstamo");
        JButton btnHistorialEstudiante = new JButton("Historial del estudiante:");

        // Los botones van en dos filas para que quepan en la ventana
        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fila1.add(btnHistorialCompleto);
        fila1.add(btnMasPrestados);
        fila1.add(btnEnPrestamo);

        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fila2.add(btnHistorialEstudiante);
        fila2.add(comboEstudiantes);

        JPanel arriba = new JPanel(new GridLayout(2, 1));
        arriba.add(fila1);
        arriba.add(fila2);
        add(arriba, BorderLayout.NORTH);

        JTable tabla = new JTable(modelo);
        // null = sin editor: las celdas no se pueden editar con doble clic
        tabla.setDefaultEditor(Object.class, null);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Cada botón guarda qué reporte eligió y lo muestra
        btnHistorialCompleto.addActionListener(e -> {
            reporteActual = 1;
            mostrarReporteActual();
        });

        btnMasPrestados.addActionListener(e -> {
            reporteActual = 2;
            mostrarReporteActual();
        });

        btnEnPrestamo.addActionListener(e -> {
            reporteActual = 3;
            mostrarReporteActual();
        });

        btnHistorialEstudiante.addActionListener(e -> {
            if (comboEstudiantes.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "No hay estudiantes registrados.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            reporteActual = 4;
            mostrarReporteActual();
        });

        recargarEstudiantes();
        mostrarReporteActual();
    }

    /**
     * Actualiza esta pestaña con los datos actuales: recarga el combo de estudiantes
     * y vuelve a generar el reporte que se está mostrando.
     * La ventana principal la llama cada vez que algo cambia.
     */
    public void refrescar() {
        recargarEstudiantes();
        mostrarReporteActual();
    }

    // Genera el reporte que corresponde al número guardado en reporteActual
    private void mostrarReporteActual() {
        if (reporteActual == 1) {
            mostrar(new String[]{"ID Préstamo", "ID Estudiante", "ID Libro", "Fecha préstamo", "Fecha límite", "Estado"},
                    reporteController.historialCompleto());
        } else if (reporteActual == 2) {
            mostrar(new String[]{"Título", "Autor", "Veces prestado"},
                    reporteController.librosMasPrestados());
        } else if (reporteActual == 3) {
            mostrar(new String[]{"Título", "Autor", "Estudiante", "Fecha préstamo", "Fecha límite", "Estado"},
                    reporteController.librosActualmenteEnPrestamo());
        } else {
            Estudiante est = (Estudiante) comboEstudiantes.getSelectedItem();
            if (est == null) {
                modelo.setRowCount(0);  // no hay estudiantes: se deja la tabla vacía
                return;
            }
            mostrar(new String[]{"ID Préstamo", "Libro", "Fecha préstamo", "Fecha límite", "Estado"},
                    reporteController.historialPorEstudiante(est.getId()));
        }
    }

    // Vuelve a llenar el combo de estudiantes (para que aparezcan los recién agregados).
    // Mantiene seleccionado el mismo estudiante si todavía existe.
    private void recargarEstudiantes() {
        Estudiante seleccionado = (Estudiante) comboEstudiantes.getSelectedItem();
        comboEstudiantes.removeAllItems();
        for (Estudiante est : estudianteController.listar()) {
            comboEstudiantes.addItem(est);
        }
        if (seleccionado != null) {
            for (int i = 0; i < comboEstudiantes.getItemCount(); i++) {
                if (comboEstudiantes.getItemAt(i).getId() == seleccionado.getId()) {
                    comboEstudiantes.setSelectedIndex(i);
                }
            }
        }
    }

    // Pone las columnas del reporte y llena la tabla con sus filas
    private void mostrar(String[] columnas, List<Object[]> filas) {
        modelo.setColumnIdentifiers(columnas);
        modelo.setRowCount(0);
        for (Object[] fila : filas) {
            modelo.addRow(fila);
        }
    }
}

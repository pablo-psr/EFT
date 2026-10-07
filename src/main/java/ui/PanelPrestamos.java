package ui;

import controller.EstudianteController;
import controller.LibroController;
import controller.PrestamoController;
import dao.PrestamoDAO;
import model.Estudiante;
import model.Libro;

import javax.swing.*;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

/**
 * Vista (la V de MVC) de préstamos y devoluciones, juntos en una sola pestaña.
 * <p>
 * Arriba está la sección para registrar un préstamo y abajo la de registrar una devolución.
 * El trabajo pesado (hilos, reglas y stock) lo hace {@link PrestamoController};
 * esta clase solo muestra los datos y recibe los clics.
 */
public class PanelPrestamos extends JPanel {
    private PrestamoController prestamoController = new PrestamoController();
    private EstudianteController estudianteController = new EstudianteController();
    private LibroController libroController = new LibroController();

    private JComboBox<ComboItem> comboEstudiantes = new JComboBox<>();
    private JComboBox<ComboItem> comboLibros = new JComboBox<>();
    private JComboBox<PrestamoDAO.PrestamoDetalle> comboPendientes = new JComboBox<>();

    // Código que se ejecuta cuando termina un préstamo o devolución (lo entrega la ventana principal)
    private Runnable alCambiar;

    /**
     * Arma la pantalla con las dos secciones y llena los combos.
     */
    public PanelPrestamos() {
        // GridLayout de 2 filas x 1 columna: una sección arriba y otra abajo
        setLayout(new GridLayout(2, 1, 10, 10));
        add(crearSeccionPrestamo());
        add(crearSeccionDevolucion());
        cargarCombos();
    }

    /**
     * Guarda el código que se debe ejecutar después de un préstamo o devolución
     * (la ventana principal le pasa el que refresca todas las pestañas).
     *
     * @param alCambiar código a ejecutar al terminar
     */
    public void setAlCambiar(Runnable alCambiar) {
        this.alCambiar = alCambiar;
    }

    // Arma la parte de arriba: elegir estudiante y libro, y registrar el préstamo
    private JPanel crearSeccionPrestamo() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Realizar Préstamo"));

        // GridBagLayout ubica los componentes en una grilla; gbc indica en qué columna (gridx)
        // y fila (gridy) va cada uno, y cuánto espacio (insets) tiene alrededor
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton btnPrestar = new JButton("Registrar Préstamo");
        JLabel lblLeyenda = new JLabel("<html><i style='color:gray;'>* Política: Máximo 2 libros activos por estudiante. No se permite pedir 2 ejemplares del mismo libro.</i></html>");

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Seleccionar Estudiante:"), gbc);
        gbc.gridx = 1;
        panel.add(comboEstudiantes, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Seleccionar Libro:"), gbc);
        gbc.gridx = 1;
        panel.add(comboLibros, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(btnPrestar, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(lblLeyenda, gbc);

        btnPrestar.addActionListener(e -> {
            // getSelectedItem devuelve un Object, por eso se convierte (cast) a ComboItem
            ComboItem itemEst = (ComboItem) comboEstudiantes.getSelectedItem();
            ComboItem itemLib = (ComboItem) comboLibros.getSelectedItem();

            if (itemEst != null && itemLib != null) {
                prestamoController.registrarPrestamoAsync(itemEst.getId(), itemLib.getId(), alCambiar);
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un estudiante y un libro.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        return panel;
    }

    // Arma la parte de abajo: elegir un préstamo pendiente y registrar su devolución
    private JPanel crearSeccionDevolucion() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Devolución de Libros"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton btnDevolver = new JButton("Registrar Devolución");

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Préstamos Pendientes:"), gbc);
        gbc.gridx = 1;
        panel.add(comboPendientes, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        panel.add(btnDevolver, gbc);

        btnDevolver.addActionListener(e -> {
            PrestamoDAO.PrestamoDetalle item = (PrestamoDAO.PrestamoDetalle) comboPendientes.getSelectedItem();
            if (item != null) {
                prestamoController.registrarDevolucionAsync(item.getIdPrestamo(), item.getIdLibro(),
                        item.getDiasAtraso(), alCambiar);
            } else {
                JOptionPane.showMessageDialog(this, "No hay préstamos pendientes de devolución.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        return panel;
    }

    /**
     * Llena los 3 combos (estudiantes, libros y préstamos pendientes) con los datos
     * actuales de la base de datos. Cada ComboItem guarda el id (para usarlo después)
     * y el texto que ve el usuario.
     */
    public void cargarCombos() {
        comboEstudiantes.removeAllItems();
        comboLibros.removeAllItems();
        comboPendientes.removeAllItems();

        for (Estudiante est : estudianteController.listar()) {
            comboEstudiantes.addItem(new ComboItem(est.getId(), est.toString()));
        }

        for (Libro l : libroController.listar()) {
            comboLibros.addItem(new ComboItem(l.getId(), "ID " + l.getId() + " - " + l.getTitulo() + " (Stock: " + l.getStock() + ")"));
        }

        for (PrestamoDAO.PrestamoDetalle detalle : prestamoController.listarPendientes()) {
            comboPendientes.addItem(detalle);
        }
    }

    /**
     * Clase auxiliar para los combos: guarda un id y el texto que se muestra.
     * El JComboBox muestra lo que devuelve toString(), pero nosotros necesitamos
     * recuperar el id al elegir una opción.
     */
    private static class ComboItem {
        private int id;
        private String label;

        public ComboItem(int id, String label) {
            this.id = id;
            this.label = label;
        }

        public int getId() { return id; }

        @Override
        public String toString() { return label; }
    }
}

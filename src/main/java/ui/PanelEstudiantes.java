package ui;

import controller.EstudianteController;
import model.Estudiante;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

/**
 * Vista (la V de MVC) de la gestión de estudiantes: agregar, modificar y eliminar.
 * <p>
 * Es un JPanel (por eso "extends JPanel": hereda todo lo de un panel de Swing) que se
 * agrega como pestaña en la ventana principal. Muestra una tabla con los estudiantes
 * y un formulario debajo. Al hacer clic en una fila de la tabla, sus datos aparecen
 * en el formulario para poder modificarlos o eliminarlos.
 */
public class PanelEstudiantes extends JPanel {
    private EstudianteController controller = new EstudianteController();

    // Modelo de la tabla: guarda las columnas y las filas que se muestran
    private DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Nombre", "RUT", "Curso", "Correo"}, 0);
    private JTable tabla = new JTable(modelo);

    private JTextField txtNombre = new JTextField();
    private JTextField txtRut = new JTextField();
    private JTextField txtCurso = new JTextField();
    private JTextField txtCorreo = new JTextField();

    // Código que se ejecuta después de agregar/modificar/eliminar (lo entrega la ventana principal)
    private Runnable alCambiar;

    /**
     * Arma la pantalla: la tabla arriba y el formulario con los botones abajo.
     */
    public PanelEstudiantes() {
        setLayout(new BorderLayout());
        // null = sin editor: las celdas no se pueden editar con doble clic
        tabla.setDefaultEditor(Object.class, null);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Formulario: 4 filas x 2 columnas (etiqueta y campo de texto)
        JPanel formulario = new JPanel(new GridLayout(4, 2, 5, 5));
        formulario.add(new JLabel("  Nombre:"));
        formulario.add(txtNombre);
        formulario.add(new JLabel("  RUT:"));
        formulario.add(txtRut);
        formulario.add(new JLabel("  Curso:"));
        formulario.add(txtCurso);
        formulario.add(new JLabel("  Correo:"));
        formulario.add(txtCorreo);

        JButton btnAgregar = new JButton("Agregar");
        JButton btnModificar = new JButton("Modificar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnLimpiar = new JButton("Limpiar");
        JPanel botones = new JPanel(new FlowLayout());
        botones.add(btnAgregar);
        botones.add(btnModificar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);

        JPanel sur = new JPanel(new BorderLayout());
        sur.add(formulario, BorderLayout.CENTER);
        sur.add(botones, BorderLayout.SOUTH);
        add(sur, BorderLayout.SOUTH);

        // Al elegir una fila de la tabla, se copian sus datos al formulario
        // (getValueIsAdjusting evita que se ejecute dos veces por un mismo clic)
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                llenarFormulario();
            }
        });

        btnAgregar.addActionListener(e -> {
            String error = controller.guardar(txtNombre.getText(), txtRut.getText(),
                    txtCurso.getText(), txtCorreo.getText());
            mostrarResultado(error, "Estudiante agregado correctamente.");
        });

        btnModificar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();  // -1 significa que no hay fila seleccionada
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Seleccione un estudiante de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (Integer) modelo.getValueAt(fila, 0);
            String error = controller.actualizar(id, txtNombre.getText(), txtRut.getText(),
                    txtCurso.getText(), txtCorreo.getText());
            mostrarResultado(error, "Estudiante modificado correctamente.");
        });

        btnEliminar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Seleccione un estudiante de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Se pregunta antes de borrar, porque no se puede deshacer
            int opcion = JOptionPane.showConfirmDialog(this, "¿Seguro que desea eliminar este estudiante?",
                    "Confirmar", JOptionPane.YES_NO_OPTION);
            if (opcion == JOptionPane.YES_OPTION) {
                int id = (Integer) modelo.getValueAt(fila, 0);
                mostrarResultado(controller.eliminar(id), "Estudiante eliminado correctamente.");
            }
        });

        btnLimpiar.addActionListener(e -> limpiar());

        cargarTabla();
    }

    /**
     * Guarda el código que se debe ejecutar cuando cambian los estudiantes
     * (la ventana principal le pasa el que refresca todas las pestañas).
     *
     * @param alCambiar código a ejecutar después de cada cambio
     */
    public void setAlCambiar(Runnable alCambiar) {
        this.alCambiar = alCambiar;
    }

    // Muestra el resultado de una operación: si error es null salió bien, si no, se muestra el error
    private void mostrarResultado(String error, String mensajeExito) {
        if (error == null) {
            JOptionPane.showMessageDialog(this, mensajeExito);
            cargarTabla();
            limpiar();
            if (alCambiar != null) {
                alCambiar.run();
            }
        } else {
            JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Vuelve a leer los estudiantes de la base de datos y llena la tabla
    private void cargarTabla() {
        modelo.setRowCount(0);
        for (Estudiante est : controller.listar()) {
            modelo.addRow(new Object[]{
                    est.getId(), est.getNombre(), est.getRut(), est.getCurso(), est.getCorreo()
            });
        }
    }

    // Copia los datos de la fila seleccionada a los campos de texto
    private void llenarFormulario() {
        int fila = tabla.getSelectedRow();
        if (fila >= 0) {
            txtNombre.setText((String) modelo.getValueAt(fila, 1));
            txtRut.setText((String) modelo.getValueAt(fila, 2));
            txtCurso.setText((String) modelo.getValueAt(fila, 3));
            txtCorreo.setText((String) modelo.getValueAt(fila, 4));
        }
    }

    // Borra los campos de texto y quita la selección de la tabla
    private void limpiar() {
        txtNombre.setText("");
        txtRut.setText("");
        txtCurso.setText("");
        txtCorreo.setText("");
        tabla.clearSelection();
    }
}

package ui;

import controller.LibroController;
import model.Libro;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;

/**
 * Vista (la V de MVC) del inventario: muestra en una tabla todos los libros y su stock.
 * Es un JPanel (extends JPanel) que se agrega como pestaña en la ventana principal.
 */
public class PanelInventario extends JPanel {
    private LibroController controller = new LibroController();

    // Modelo de la tabla: guarda las columnas y las filas que se muestran
    private DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID Libro", "Título", "Autor", "ISBN", "Editorial", "Stock", "Cat. ID"}, 0);

    /**
     * Arma la pantalla y carga los libros.
     */
    public PanelInventario() {
        setLayout(new BorderLayout());
        JTable tabla = new JTable(modelo);
        // null = sin editor: las celdas no se pueden editar con doble clic
        tabla.setDefaultEditor(Object.class, null);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        cargar();
    }

    /**
     * Vuelve a leer los libros de la base de datos y llena la tabla.
     * La ventana principal la llama cada vez que cambia el stock.
     */
    public void cargar() {
        modelo.setRowCount(0);  // borra las filas anteriores
        for (Libro l : controller.listar()) {
            modelo.addRow(new Object[]{
                    l.getId(), l.getTitulo(), l.getAutor(), l.getIsbn(),
                    l.getEditorial(), l.getStock(), l.getIdCategoria()
            });
        }
    }
}

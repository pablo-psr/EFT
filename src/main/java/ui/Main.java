package ui;

import controller.UsuarioController;
import model.Persona;
import model.Usuario;

import javax.swing.*;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Ventana principal del sistema de biblioteca escolar (parte de la Vista en MVC).
 * <p>
 * Primero muestra el login y, si las credenciales son correctas, abre la ventana
 * principal con 4 pestañas. Cada pestaña es una clase aparte:
 * <ul>
 *   <li>Gestión de Estudiantes: {@link PanelEstudiantes}</li>
 *   <li>Inventario de Libros: {@link PanelInventario}</li>
 *   <li>Préstamos y Devoluciones: {@link PanelPrestamos}</li>
 *   <li>Historial y Reportes: {@link PanelReportes}</li>
 * </ul>
 * Esta clase solo arma la ventana y conecta las pestañas entre sí.
 */
public class Main {

    /**
     * Punto de entrada del programa.
     *
     * @param args argumentos de consola (no se usan)
     */
    public static void main(String[] args) {
        // Swing debe iniciarse en su propio hilo (Event Dispatch Thread), por eso invokeLater
        SwingUtilities.invokeLater(() -> mostrarLogin());
    }

    /**
     * Crea y muestra la ventana de inicio de sesión.
     */
    private static void mostrarLogin() {
        JFrame frameLogin = new JFrame("Biblioteca Escolar - Login");
        frameLogin.setSize(350, 200);
        // DO_NOTHING_ON_CLOSE: la X no cierra sola; el WindowListener de abajo pregunta antes
        frameLogin.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frameLogin.setLocationRelativeTo(null);
        // GridLayout: cuadrícula de 3 filas x 2 columnas, con 10 píxeles de espacio entre celdas
        frameLogin.setLayout(new GridLayout(3, 2, 10, 10));

        // Confirmación de salida en el Login
        frameLogin.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmarSalida(frameLogin);
            }
        });

        JTextField txtCorreo = new JTextField();
        JPasswordField txtPass = new JPasswordField();
        JButton btnLogin = new JButton("Ingresar");

        frameLogin.add(new JLabel("  Correo:"));
        frameLogin.add(txtCorreo);
        frameLogin.add(new JLabel("  Contraseña:"));
        frameLogin.add(txtPass);
        frameLogin.add(new JLabel(""));
        frameLogin.add(btnLogin);

        UsuarioController usuarioController = new UsuarioController();

        // Al presionar "Ingresar" se valida el usuario en la base de datos (a través del controlador)
        btnLogin.addActionListener(e -> {
            Usuario u = usuarioController.autenticar(txtCorreo.getText(), new String(txtPass.getPassword()));
            if (u != null) {
                Persona persona = u;  // un Usuario guardado en una variable Persona (polimorfismo)
                JOptionPane.showMessageDialog(frameLogin, "Bienvenido " + persona.getNombre() + "\n" + persona.getDescripcion());
                frameLogin.dispose();
                mostrarVentanaPrincipal(u);
            } else {
                JOptionPane.showMessageDialog(frameLogin, "Credenciales incorrectas.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        frameLogin.setVisible(true);
    }

    /**
     * Crea y muestra la ventana principal, con una pestaña para cada función.
     *
     * @param usuario usuario que inició sesión (su rol se muestra en el título)
     */
    private static void mostrarVentanaPrincipal(Usuario usuario) {
        JFrame frameMain = new JFrame("Sistema de Biblioteca Escolar - Rol: " + usuario.getRol());
        frameMain.setSize(900, 600);
        frameMain.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frameMain.setLocationRelativeTo(null);

        // Confirmación de salida en la Ventana Principal
        frameMain.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmarSalida(frameMain);
            }
        });

        // Cada pestaña es un panel (una clase propia). Al crearlos, cada uno carga sus datos.
        PanelEstudiantes panelEstudiantes = new PanelEstudiantes();
        PanelInventario panelInventario = new PanelInventario();
        PanelPrestamos panelPrestamos = new PanelPrestamos();
        PanelReportes panelReportes = new PanelReportes();

        // JTabbedPane: contenedor con pestañas; sirve como menú de navegación.
        // El orden en que se agregan es el orden en que aparecen.
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Gestión de Estudiantes", panelEstudiantes);
        tabs.addTab("Inventario de Libros", panelInventario);
        tabs.addTab("Préstamos y Devoluciones", panelPrestamos);
        tabs.addTab("Historial y Reportes", panelReportes);

        // Runnable: un bloque de código guardado en una variable para ejecutarlo después.
        // Actualiza todas las pestañas con los datos nuevos de la base de datos.
        Runnable refrescarTodo = () -> {
            panelInventario.cargar();
            panelPrestamos.cargarCombos();
            panelReportes.refrescar();
        };

        // Cuando cambian los estudiantes, o termina un préstamo o devolución, se ejecuta refrescarTodo
        panelEstudiantes.setAlCambiar(refrescarTodo);
        panelPrestamos.setAlCambiar(refrescarTodo);

        frameMain.add(tabs);
        frameMain.setVisible(true);
    }

    /**
     * Pregunta al usuario si realmente quiere salir y, si responde que sí, cierra el programa.
     *
     * @param frame ventana sobre la que se muestra la pregunta
     */
    private static void confirmarSalida(JFrame frame) {
        int opcion = JOptionPane.showConfirmDialog(
                frame,
                "¿Está seguro de que desea salir del sistema de biblioteca?",
                "Confirmar Salida",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (opcion == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
}

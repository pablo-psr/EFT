package controller;

import dao.UsuarioDAO;
import model.Usuario;

/**
 * Controlador (la C de MVC) del inicio de sesión.
 * El login le entrega el correo y la clave a este controlador, y él consulta al DAO.
 */
public class UsuarioController {
    // El controlador usa el DAO para hablar con la base de datos
    private UsuarioDAO dao = new UsuarioDAO();

    /**
     * Revisa si las credenciales son correctas.
     *
     * @param correo correo escrito en el login
     * @param clave  contraseña escrita en el login
     * @return el Usuario si las credenciales son correctas, o null si no lo son
     */
    public Usuario autenticar(String correo, String clave) {
        return dao.autenticar(correo, clave);
    }
}

package controller;

import dao.LibroDAO;
import model.Libro;
import java.util.List;

/**
 * Controlador (la C de MVC) de los libros.
 * La vista le pide los libros a este controlador y él se los pide al DAO,
 * así la ventana nunca habla directo con la base de datos.
 */
public class LibroController {
    // El controlador usa el DAO para hablar con la base de datos
    private LibroDAO dao = new LibroDAO();

    /**
     * Entrega todos los libros del inventario.
     *
     * @return lista de libros
     */
    public List<Libro> listar() {
        return dao.listarTodos();
    }
}

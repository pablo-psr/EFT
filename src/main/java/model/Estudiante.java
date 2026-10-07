package model;

/**
 * Representa a un estudiante que puede pedir libros prestados.
 * Hereda id, nombre, rut y correo de {@link Persona} y agrega el curso.
 */
public class Estudiante extends Persona {
    // Dato propio del estudiante (los demás vienen de Persona)
    private String curso;

    /** Constructor vacío. */
    public Estudiante() {}

    /**
     * Crea un estudiante con todos sus datos.
     *
     * @param id     identificador en la base de datos
     * @param nombre nombre completo
     * @param rut    RUT del estudiante
     * @param curso  curso al que pertenece
     * @param correo correo electrónico
     */
    public Estudiante(int id, String nombre, String rut, String curso, String correo) {
        // super(...) llama al constructor de Persona para guardar id, nombre, rut y correo
        super(id, nombre, rut, correo);
        this.curso = curso;
    }

    public String getCurso() { return curso; }
    public void setCurso(String curso) { this.curso = curso; }

    /**
     * Implementación propia de Estudiante del método abstracto de Persona.
     *
     * @return texto con el curso del estudiante
     */
    @Override
    public String getDescripcion() {
        return "Estudiante del curso " + curso;
    }

    /**
     * Texto que se muestra cuando el estudiante está dentro de un combo (JComboBox).
     *
     * @return id, nombre y curso del estudiante
     */
    @Override
    public String toString() {
        return "ID " + id + " - " + nombre + " (" + curso + ")";
    }
}

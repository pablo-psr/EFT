package model;

// java.sql.Date es el tipo de fecha que usa JDBC para leer fechas de MySQL
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Representa un préstamo de un libro a un estudiante (una fila de la tabla "prestamos").
 * Guarda los ids del estudiante y del libro, las fechas y si ya fue devuelto.
 */
public class Prestamo {
    private int id;
    private int idEstudiante;
    private int idLibro;
    private Date fechaPrestamo;
    private Date fechaDevolucion;
    private boolean devuelto;

    /** Constructor vacío. */
    public Prestamo() {}

    /**
     * Crea un préstamo con todos sus datos.
     *
     * @param id              identificador del préstamo
     * @param idEstudiante    id del estudiante que pidió el libro
     * @param idLibro         id del libro prestado
     * @param fechaPrestamo   fecha en que se prestó
     * @param fechaDevolucion fecha límite para devolverlo
     * @param devuelto        true si el libro ya fue devuelto
     */
    public Prestamo(int id, int idEstudiante, int idLibro, Date fechaPrestamo, Date fechaDevolucion, boolean devuelto) {
        this.id = id;
        this.idEstudiante = idEstudiante;
        this.idLibro = idLibro;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucion = fechaDevolucion;
        this.devuelto = devuelto;
    }

    public int getId() { return id; }
    public int getIdEstudiante() { return idEstudiante; }
    public int getIdLibro() { return idLibro; }
    public Date getFechaPrestamo() { return fechaPrestamo; }
    public Date getFechaDevolucion() { return fechaDevolucion; }
    public boolean isDevuelto() { return devuelto; }

    /**
     * Indica si el préstamo está atrasado: todavía no se devuelve y la fecha límite ya pasó.
     *
     * @return true si está atrasado
     */
    public boolean estaAtrasado() {
        if (devuelto || fechaDevolucion == null) {
            return false;
        }
        // toLocalDate() convierte la fecha de SQL a LocalDate, para compararla con la fecha de hoy
        return fechaDevolucion.toLocalDate().isBefore(LocalDate.now());
    }

    /**
     * Calcula cuántos días de atraso lleva el préstamo.
     *
     * @return días de atraso (0 si no está atrasado)
     */
    public long getDiasAtraso() {
        if (!estaAtrasado()) {
            return 0;
        }
        return ChronoUnit.DAYS.between(fechaDevolucion.toLocalDate(), LocalDate.now());
    }
}
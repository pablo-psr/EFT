package model;

// Clase abstracta: tiene lo que Usuario y Estudiante tienen en común
public abstract class Persona {
    protected int id;
    protected String nombre;
    protected String rut;
    protected String correo;

    public Persona() {}

    public Persona(int id, String nombre, String rut, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.rut = rut;
        this.correo = correo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    // Método abstracto: cada hijo lo implementa a su manera (polimorfismo)
    public abstract String getDescripcion();
}

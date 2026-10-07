package model;

public class Usuario extends Persona {
    private String contrasena;
    private String rol;

    public Usuario() {}

    public Usuario(int id, String nombre, String rut, String correo, String contrasena, String rol) {
        super(id, nombre, rut, correo);
        this.contrasena = contrasena;
        this.rol = rol;
    }

    public String getContrasena() { return contrasena; }
    public String getRol() { return rol; }

    @Override
    public String getDescripcion() {
        return "Usuario con rol " + rol;
    }
}

package dao;

import config.DatabaseConnection;
import model.Usuario;
import java.sql.*;

public class UsuarioDAO {
    public Usuario autenticar(String correo, String clave) {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        if (conn == null) {
            System.err.println("Sin conexión a la base de datos.");
            return null;
        }

        String sql = "SELECT id, nombre, rut, correo, contrasena, rol FROM usuarios WHERE correo = ? AND contrasena = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, correo.trim());
            ps.setString(2, clave.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("rut"),
                            rs.getString("correo"),
                            rs.getString("contrasena"),
                            rs.getString("rol")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
//Óscar Barquilla Esteban obarquill1@alumno.uned.es

package common;

import java.io.Serializable;

public class UsuarioDatos implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String nombre;
    private String nick;
    private String password;
    
    public UsuarioDatos(String nombre, String nick, String password) {
        this.nombre = nombre;
        this.nick = nick;
        this.password = password;
    }

    public String getNombre() { return nombre; }
    public String getNick() { return nick; }
    public String getPassword() { return password; }
}
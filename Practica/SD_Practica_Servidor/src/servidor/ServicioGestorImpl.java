//Óscar Barquilla Esteban obarquill1@alumno.uned.es

package servidor;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import common.CallbackUsuarioInterface;
import common.ServicioAutenticacionInterface;
import common.ServicioDatosInterface;
import common.ServicioGestorInterface;
import common.Trino;

// CLASE: Implementa la lógica de negocio y las interfaces remotas
public class ServicioGestorImpl extends UnicastRemoteObject implements ServicioAutenticacionInterface, ServicioGestorInterface {
    
    private static final long serialVersionUID = 1L;
    
    // Referencia al objeto remoto de la Base de Datos (Cliente de la BD)
    private ServicioDatosInterface db;
    
    // Mapa de usuarios conectados: Nick -> Callback del Cliente (Para notificaciones push)
    private ConcurrentHashMap<String, CallbackUsuarioInterface> usuariosConectados;

    // Constructor
    public ServicioGestorImpl(ServicioDatosInterface db) throws RemoteException {
        super();
        this.db = db;
        this.usuariosConectados = new ConcurrentHashMap<>();
    }

    // --- IMPLEMENTACIÓN DE AUTENTICACIÓN ---

    @Override
    public boolean registrarUsuario(String nombre, String nick, String password) throws RemoteException {
        return db.guardarUsuario(nombre, nick, password);
    }

    @Override
    public boolean login(String nick, String password, CallbackUsuarioInterface clienteCallback) throws RemoteException {
        boolean credencialesOk = db.validarCredenciales(nick, password);
        
        if (credencialesOk) {
            // Guardamos al cliente en la lista de conectados para poder enviarle mensajes
            usuariosConectados.put(nick, clienteCallback);
            System.out.println("Servidor: Usuario " + nick + " conectado.");
            
            // Entregar trinos pendientes (Offline)
            List<Trino> pendientes = db.obtenerTrinosPendientes(nick);
            if (!pendientes.isEmpty()) {
                System.out.println("Servidor: Enviando " + pendientes.size() + " trinos pendientes a " + nick);
                for (Trino t : pendientes) {
                    try {
                        clienteCallback.recibirTrino(t);
                    } catch (RemoteException e) {
                        System.err.println("Error enviando pendiente a " + nick);
                    }
                }
                db.borrarTrinosPendientes(nick);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean logout(String nick) throws RemoteException {
        if (usuariosConectados.containsKey(nick)) {
            usuariosConectados.remove(nick);
            System.out.println("Servidor: Usuario " + nick + " desconectado.");
            return true;
        }
        return false;
    }

    // --- IMPLEMENTACIÓN DE GESTOR ---

 // Método LOCAL para que el menú del Servidor pueda ver quién está online
    public java.util.Set<String> obtenerUsuariosConectadosLocal() {
        return usuariosConectados.keySet();
    }

    // Y MODIFICAMOS enviarTrino para respetar el BANEO:
    @Override
    public void enviarTrino(String nickPropietario, String mensaje) throws RemoteException {
        // Verificar si está baneado antes de enviar
        if (db.estaBaneado(nickPropietario)) {
            System.out.println("Servidor: " + nickPropietario + " intentó escribir pero está BANEADO.");
            // Guardamos el trino pero NO lo distribuimos (según Pág 4)
            Trino nuevoTrino = new Trino(nickPropietario, mensaje);
            db.guardarTrino(nuevoTrino);
            return; 
        }
        
        // ... (resto del código de enviarTrino igual que antes) ...
        System.out.println("Servidor: Nuevo trino de " + nickPropietario);
        Trino nuevoTrino = new Trino(nickPropietario, mensaje);
        db.guardarTrino(nuevoTrino);
        
        List<String> seguidores = db.obtenerSeguidores(nickPropietario);
        for (String seguidor : seguidores) {
            CallbackUsuarioInterface callback = usuariosConectados.get(seguidor);
            if (callback != null) {
                try {
                    callback.recibirTrino(nuevoTrino);
                } catch (RemoteException e) {
                    usuariosConectados.remove(seguidor);
                    db.guardarTrinoPendiente(seguidor, nuevoTrino);
                }
            } else {
                db.guardarTrinoPendiente(seguidor, nuevoTrino);
            }
        }
    }

    @Override
    public List<String> listarUsuarios() throws RemoteException {
        // AHORA SÍ: Llamamos a la BD para pedir la lista real
        return db.obtenerListadoUsuarios(); 
    }

    @Override
    public void seguirUsuario(String nickSeguidor, String nickSeguido) throws RemoteException {
        db.guardarSeguidor(nickSeguidor, nickSeguido);
    }

    @Override
    public void dejarDeSeguirUsuario(String nickSeguidor, String nickSeguido) throws RemoteException {
        db.eliminarSeguidor(nickSeguidor, nickSeguido);
    }


    
    @Override
    public int obtenerNumSeguidores(String nick) throws RemoteException {
        return db.obtenerSeguidores(nick).size();
    }

    @Override
    public int obtenerNumTrinos(String nick) throws RemoteException {
        return db.obtenerTrinosDe(nick).size();
    }
    @Override
    public List<Trino> listarTrinos(String nick) throws RemoteException {
        // Delegamos en la BD para obtener los trinos de este usuario
        return db.obtenerTrinosDe(nick);
    }

    @Override
    public boolean borrarTrino(String nickPropietario, long timestamp) throws RemoteException {
        System.out.println("Servidor: Solicitud de borrado de " + nickPropietario);
        return db.eliminarTrino(nickPropietario, timestamp);
    }
    
}
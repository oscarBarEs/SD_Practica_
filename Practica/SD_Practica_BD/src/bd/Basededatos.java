package bd;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import common.ServicioDatosInterface;
import common.Trino;
import common.UsuarioDatos;

public class Basededatos extends UnicastRemoteObject implements ServicioDatosInterface {
    
    private static final long serialVersionUID = 1L;

    // Estructuras de Almacenamiento en Memoria (Thread-Safe)
    
    // Mapa: Nick -> Objeto UsuarioDatos (Perfil, Password)
    private ConcurrentHashMap<String, UsuarioDatos> usuarios;
    
    // Mapa: Nick -> Lista de Nicks que le siguen
    private ConcurrentHashMap<String, List<String>> seguidores;
    
    // Mapa: Nick -> Lista de Trinos PROPIOS (Historial del usuario)
    private ConcurrentHashMap<String, List<Trino>> trinos;
    
    // Mapa: Nick Destinatario -> Lista de Trinos PENDIENTES de leer (Offline)
    private ConcurrentHashMap<String, List<Trino>> trinosPendientes;

    // Constructor
    public Basededatos() throws RemoteException {
        super();
        // Inicializamos las estructuras
        usuarios = new ConcurrentHashMap<>();
        seguidores = new ConcurrentHashMap<>();
        trinos = new ConcurrentHashMap<>();
        trinosPendientes = new ConcurrentHashMap<>();
        
        // Datos de prueba (Opcional, para no empezar vacíos)
        System.out.println("Base de Datos inicializada en memoria.");
    }

    // --- IMPLEMENTACIÓN DE LA INTERFAZ ---

    @Override
    public boolean guardarUsuario(String nombre, String nick, String password) throws RemoteException {
        if (usuarios.containsKey(nick)) {
            return false; // Ya existe
        }
        UsuarioDatos nuevoUsuario = new UsuarioDatos(nombre, nick, password);
        usuarios.put(nick, nuevoUsuario);
        
        // Inicializamos sus listas vacías para evitar NullPointer después
        seguidores.put(nick, Collections.synchronizedList(new ArrayList<>()));
        trinos.put(nick, Collections.synchronizedList(new ArrayList<>()));
        trinosPendientes.put(nick, Collections.synchronizedList(new ArrayList<>()));
        
        System.out.println("BD: Usuario registrado -> " + nick);
        return true;
    }

    @Override
    public boolean validarCredenciales(String nick, String password) throws RemoteException {
        if (!usuarios.containsKey(nick)) return false;
        
        String passReal = usuarios.get(nick).getPassword();
        return passReal.equals(password);
    }

    @Override
    public void guardarSeguidor(String nickSeguidor, String nickSeguido) throws RemoteException {
        // Obtenemos la lista de seguidores del usuario 'nickSeguido'
        List<String> listaSeguidores = seguidores.get(nickSeguido);
        
        if (listaSeguidores != null) {
            // Sincronizamos para evitar duplicados si dan doble click muy rápido
            synchronized (listaSeguidores) {
                if (!listaSeguidores.contains(nickSeguidor)) {
                    listaSeguidores.add(nickSeguidor);
                    System.out.println("BD: " + nickSeguidor + " ahora sigue a " + nickSeguido);
                }
            }
        }
    }

    @Override
    public void eliminarSeguidor(String nickSeguidor, String nickSeguido) throws RemoteException {
        List<String> listaSeguidores = seguidores.get(nickSeguido);
        if (listaSeguidores != null) {
            listaSeguidores.remove(nickSeguidor);
            System.out.println("BD: " + nickSeguidor + " dejó de seguir a " + nickSeguido);
        }
    }

    @Override
    public List<String> obtenerSeguidores(String nick) throws RemoteException {
        // Retornamos una copia para evitar problemas de concurrencia si la lista cambia mientras se lee
        List<String> lista = seguidores.get(nick);
        if (lista == null) return new ArrayList<>();
        return new ArrayList<>(lista);
    }

    @Override
    public void guardarTrino(Trino trino) throws RemoteException {
        String propietario = trino.GetNickPropietario();
        List<Trino> historial = trinos.get(propietario);
        
        if (historial != null) {
            historial.add(trino);
            System.out.println("BD: Trino guardado de " + propietario);
        }
    }

    @Override
    public List<Trino> obtenerTrinosDe(String nick) throws RemoteException {
        List<Trino> lista = trinos.get(nick);
        if (lista == null) return new ArrayList<>();
        return new ArrayList<>(lista);
    }

    @Override
    public void guardarTrinoPendiente(String nickDestinatario, Trino trino) throws RemoteException {
        List<Trino> pendientes = trinosPendientes.get(nickDestinatario);
        if (pendientes != null) {
            pendientes.add(trino);
            // System.out.println("BD: Trino encolado para " + nickDestinatario + " (Offline)");
        }
    }

    @Override
    public List<Trino> obtenerTrinosPendientes(String nickDestinatario) throws RemoteException {
        List<Trino> pendientes = trinosPendientes.get(nickDestinatario);
        if (pendientes == null) return new ArrayList<>();
        return new ArrayList<>(pendientes);
    }

    @Override
    public void borrarTrinosPendientes(String nickDestinatario) throws RemoteException {
        List<Trino> pendientes = trinosPendientes.get(nickDestinatario);
        if (pendientes != null) {
            pendientes.clear();
        }
    }

    @Override
    public int obtenerNumeroUsuarios() throws RemoteException {
        return usuarios.size();
    }
    
    @Override
    public List<String> obtenerListadoUsuarios() throws RemoteException {
        // Obtenemos todas las claves (nicks) del mapa de usuarios
        // Creamos un ArrayList nuevo para que sea serializable y seguro enviar
        return new ArrayList<>(usuarios.keySet());
    }
    
    // --- ARRANQUE DEL SERVIDOR (MAIN) ---
    
    public static void main(String[] args) {
        try {
            // 1. Crear e iniciar el objeto remoto
            Basededatos bd = new Basededatos();
            
            // 2. Arrancar el registro RMI en el puerto por defecto 1099
            // Nota: En producción a veces se lanza 'rmiregistry' por consola, 
            // pero crear el registro aquí simplifica la ejecución en Eclipse.
            Registry registry = LocateRegistry.createRegistry(1099);
            
            // 3. Publicar el objeto remoto con un nombre
            registry.rebind("ServicioDatos", bd);
            
            System.out.println("--- SERVIDOR DE BASE DE DATOS LISTO ---");
            System.out.println("Registrado como: ServicioDatos");
            
        } catch (Exception e) {
            System.err.println("Error en Servidor BD:");
            e.printStackTrace();
        }
    }
}
//Óscar Barquilla Esteban obarquill1@alumno.uned.es

package bd;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import common.ServicioDatosInterface;
import common.Trino;
import common.UsuarioDatos;

public class Basededatos extends UnicastRemoteObject implements ServicioDatosInterface {
    
    private static final long serialVersionUID = 1L;

    // Estructuras de datos
    private ConcurrentHashMap<String, UsuarioDatos> usuarios;
    private ConcurrentHashMap<String, List<String>> seguidores;
    private ConcurrentHashMap<String, List<Trino>> trinos;
    private ConcurrentHashMap<String, List<Trino>> trinosPendientes;
    
    private List<String> baneados;

    public Basededatos() throws RemoteException {
        super();
        usuarios = new ConcurrentHashMap<>();
        seguidores = new ConcurrentHashMap<>();
        trinos = new ConcurrentHashMap<>();
        trinosPendientes = new ConcurrentHashMap<>();
        baneados = Collections.synchronizedList(new ArrayList<>());
    }


    @Override
    public boolean guardarUsuario(String nombre, String nick, String password) throws RemoteException {
        if (usuarios.containsKey(nick)) return false;
        usuarios.put(nick, new UsuarioDatos(nombre, nick, password));
        seguidores.put(nick, Collections.synchronizedList(new ArrayList<>()));
        trinos.put(nick, Collections.synchronizedList(new ArrayList<>()));
        trinosPendientes.put(nick, Collections.synchronizedList(new ArrayList<>()));
        return true;
    }

    @Override
    public boolean validarCredenciales(String nick, String password) throws RemoteException {
        if (!usuarios.containsKey(nick)) return false;
        return usuarios.get(nick).getPassword().equals(password);
    }

    @Override
    public List<String> obtenerListadoUsuarios() throws RemoteException {
        return new ArrayList<>(usuarios.keySet());
    }

    @Override
    public void guardarSeguidor(String nickSeguidor, String nickSeguido) throws RemoteException {
        
        if (nickSeguidor.equals(nickSeguido)) {
            System.out.println("BD: Intento de auto-seguimiento rechazado para " + nickSeguidor);
            return; 
        }

        List<String> lista = seguidores.get(nickSeguido);
        if (lista != null) {
            synchronized (lista) {
                if (!lista.contains(nickSeguidor)) {
                    lista.add(nickSeguidor);
                    System.out.println("BD: " + nickSeguidor + " ahora sigue a " + nickSeguido);
                }
            }
        }
    }

    @Override
    public void eliminarSeguidor(String nickSeguidor, String nickSeguido) throws RemoteException {
        List<String> lista = seguidores.get(nickSeguido);
        if (lista != null) lista.remove(nickSeguidor);
    }

    @Override
    public List<String> obtenerSeguidores(String nick) throws RemoteException {
        List<String> lista = seguidores.get(nick);
        return (lista == null) ? new ArrayList<>() : new ArrayList<>(lista);
    }

    @Override
    public void guardarTrino(Trino trino) throws RemoteException {
        List<Trino> historial = trinos.get(trino.GetNickPropietario());
        if (historial != null) historial.add(trino);
    }

    @Override
    public List<Trino> obtenerTrinosDe(String nick) throws RemoteException {
        List<Trino> lista = trinos.get(nick);
        return (lista == null) ? new ArrayList<>() : new ArrayList<>(lista);
    }

    @Override
    public void guardarTrinoPendiente(String nickDestinatario, Trino trino) throws RemoteException {
        List<Trino> pendientes = trinosPendientes.get(nickDestinatario);
        if (pendientes != null) pendientes.add(trino);
    }

    @Override
    public List<Trino> obtenerTrinosPendientes(String nickDestinatario) throws RemoteException {
        List<Trino> pendientes = trinosPendientes.get(nickDestinatario);
        return (pendientes == null) ? new ArrayList<>() : new ArrayList<>(pendientes);
    }

    @Override
    public void borrarTrinosPendientes(String nickDestinatario) throws RemoteException {
        List<Trino> pendientes = trinosPendientes.get(nickDestinatario);
        if (pendientes != null) pendientes.clear();
    }
    
    
    
    @Override
    public void banearUsuario(String nick) throws RemoteException {
        if (!baneados.contains(nick) && usuarios.containsKey(nick)) {
            baneados.add(nick);
            System.out.println("BD: Usuario BANEADO -> " + nick);
        }
    }

    @Override
    public void desbanearUsuario(String nick) throws RemoteException {
        baneados.remove(nick);
        System.out.println("BD: Usuario DESBANEADO -> " + nick);
    }

    @Override
    public boolean estaBaneado(String nick) throws RemoteException {
        return baneados.contains(nick);
    }
    
   
    public void imprimirTodosLosTrinos() {
        System.out.println("--- LISTADO DE TRINOS (Nick # Timestamp) ---");
        for (String nick : trinos.keySet()) {
            List<Trino> lista = trinos.get(nick);
            for (Trino t : lista) {
                System.out.println(t.GetNickPropietario() + " # " + t.GetTimestamp());
            }
        }
    }

   

    public static void main(String[] args) {
        try {
            Basededatos bd = new Basededatos();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("ServicioDatos", bd);
            
            System.out.println("--- SERVIDOR DE BASE DE DATOS INICIADO ---");
            
            // MENÚ INTERACTIVO
            Scanner scanner = new Scanner(System.in);
            boolean salir = false;
            
            while (!salir) {
                System.out.println("\n=== MENÚ BASE DE DATOS ===");
                System.out.println("1.- Información de la Base de Datos");
                System.out.println("2.- Listar Trinos");
                System.out.println("3.- Salir");
                System.out.print("Elija opción: ");
                
                String opcion = scanner.nextLine();
                switch (opcion) {
                    case "1":
                        System.out.println("BD ejecutándose en puerto 1099.");
                        System.out.println("Usuarios registrados: " + bd.usuarios.size());
                        break;
                    case "2":
                        bd.imprimirTodosLosTrinos();
                        break;
                    case "3":
                        salir = true;
                        System.out.println("Cerrando Base de Datos...");
                        UnicastRemoteObject.unexportObject(bd, true);
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Opción incorrecta.");
                }
            }
            scanner.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	@Override
	public int obtenerNumeroUsuarios() throws RemoteException {
		// TODO Auto-generated method stub
		return usuarios.size() ;
	}
	
	@Override
    public boolean eliminarTrino(String nickPropietario, long timestamp) throws RemoteException {
        boolean borradoDeHistorial = false;
        
        List<Trino> listaPropia = trinos.get(nickPropietario);
        if (listaPropia != null) {
            synchronized (listaPropia) {
                borradoDeHistorial = listaPropia.removeIf(t -> t.GetTimestamp() == timestamp);
            }
        }
        
        for (List<Trino> listaPendientes : trinosPendientes.values()) {
            synchronized (listaPendientes) {
                listaPendientes.removeIf(t -> t.GetTimestamp() == timestamp && t.GetNickPropietario().equals(nickPropietario));
            }
        }

        if (borradoDeHistorial) {
             System.out.println("BD: Trino de " + nickPropietario + " eliminado GLOBALMENTE (Time: " + timestamp + ")");
             return true;
        }
        return false;
    }
}
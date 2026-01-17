package servidor;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Scanner;

import common.ServicioDatosInterface;

public class Servidor {

    public static void main(String[] args) {
        try {
            // 1. Conexión RMI
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            ServicioDatosInterface db = (ServicioDatosInterface) registry.lookup("ServicioDatos");
            
            ServicioGestorImpl gestor = new ServicioGestorImpl(db);
            
            registry.rebind("ServicioAutenticacion", gestor);
            registry.rebind("ServicioGestor", gestor);
            
            System.out.println("--- SERVIDOR DE NEGOCIO LISTO ---");
            
            // 2. MENÚ INTERACTIVO (Requisito Pág 5 y 6)
            Scanner scanner = new Scanner(System.in);
            boolean salir = false;
            
            while (!salir) {
                System.out.println("\n=== MENÚ SERVIDOR ===");
                System.out.println("1.- Información del Servidor");
                System.out.println("2.- Listar Usuarios Registrados");
                System.out.println("3.- Listar Usuarios Logueados");
                System.out.println("4.- Bloquear (banear) usuario");
                System.out.println("5.- Desbloquear usuario");
                System.out.println("6.- Salir");
                System.out.print("Elija opción: ");
                
                String opcion = scanner.nextLine();
                
                switch (opcion) {
                    case "1":
                        System.out.println("Servidor 'ServicioGestor' y 'ServicioAutenticacion' activos.");
                        break;
                        
                    case "2":
                        List<String> registrados = db.obtenerListadoUsuarios();
                        System.out.println("--- Usuarios Registrados ---");
                        for (String u : registrados) System.out.println("- " + u);
                        break;
                        
                    case "3":
                        System.out.println("--- Usuarios Logueados (Online) ---");
                        // Usamos el método local que añadimos al Gestor
                        for (String u : gestor.obtenerUsuariosConectadosLocal()) {
                            System.out.println("- " + u + " (Online)");
                        }
                        break;
                        
                    case "4":
                        System.out.print("Nick a banear: ");
                        String aBanear = scanner.nextLine();
                        db.banearUsuario(aBanear);
                        System.out.println("Usuario " + aBanear + " bloqueado.");
                        break;
                        
                    case "5":
                        System.out.print("Nick a desbloquear: ");
                        String aDesbanear = scanner.nextLine();
                        db.desbanearUsuario(aDesbanear);
                        System.out.println("Usuario " + aDesbanear + " desbloqueado.");
                        break;
                        
                    case "6":
                        salir = true;
                        System.out.println("Apagando Servidor...");
                        UnicastRemoteObject.unexportObject(gestor, true);
                        System.exit(0);
                        break;
                        
                    default:
                        System.out.println("Opción no válida.");
                }
            }
            scanner.close();
            
        } catch (Exception e) {
            System.err.println("Error servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
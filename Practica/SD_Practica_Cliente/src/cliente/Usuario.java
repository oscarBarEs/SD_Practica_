//Óscar Barquilla Esteban obarquill1@alumno.uned.es

package cliente;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

import common.CallbackUsuarioInterface;
import common.ServicioAutenticacionInterface;
import common.ServicioGestorInterface;

public class Usuario {

    private static ServicioAutenticacionInterface authService;
    private static ServicioGestorInterface gestorService;
    
    private static CallbackUsuarioInterface callbackObj;
    
    private static String miNick = null;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            
            authService = (ServicioAutenticacionInterface) registry.lookup("ServicioAutenticacion");
            gestorService = (ServicioGestorInterface) registry.lookup("ServicioGestor");
            
            callbackObj = new CallbackUsuarioImpl();
            
            System.out.println("--- CLIENTE DE MICROBLOGGING INICIADO ---");
            
            boolean salir = false;
            while (!salir) {
                mostrarMenuInicial();
                String opcion = scanner.nextLine();
                
                switch (opcion) {
                    case "1":
                        registrarUsuario();
                        break;
                    case "2":
                        if (hacerLogin()) {
                            bucleMenuPrincipal();
                        }
                        break;
                    case "3":
                        salir = true;
                        System.out.println("Adiós.");
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }
            }
            
            System.exit(0);
            
        } catch (Exception e) {
            System.err.println("Error en el Cliente:");
            e.printStackTrace();
        }
    }

    // --- MENÚS Y LÓGICA DE INTERFAZ  ---

    private static void mostrarMenuInicial() {
        System.out.println("\n=== BIENVENIDO AL SISTEMA ===");
        System.out.println("1.- Registrar un nuevo usuario");
        System.out.println("2.- Hacer login");
        System.out.println("3.- Salir");
        System.out.print("Elija opción: ");
    }

    private static void registrarUsuario() {
        try {
            System.out.println("\n--- REGISTRO ---");
            System.out.print("Introduzca Nombre completo: ");
            String nombre = scanner.nextLine();
            System.out.print("Introduzca Nick (único): ");
            String nick = scanner.nextLine();
            System.out.print("Introduzca Password: ");
            String pass = scanner.nextLine();
            
            boolean ok = authService.registrarUsuario(nombre, nick, pass);
            if (ok) {
                System.out.println(">> Registro EXITOSO. Ahora puede hacer login.");
            } else {
                System.out.println(">> ERROR: El nick ya existe.");
            }
        } catch (Exception e) {
            System.out.println("Error en registro: " + e.getMessage());
        }
    }

    private static boolean hacerLogin() {
        try {
            System.out.println("\n--- LOGIN ---");
            System.out.print("Nick: ");
            String nick = scanner.nextLine();
            System.out.print("Password: ");
            String pass = scanner.nextLine();
            
            boolean ok = authService.login(nick, pass, callbackObj);
            
            if (ok) {
                miNick = nick;
                System.out.println(">> Login CORRECTO. Bienvenido " + miNick);
                return true;
            } else {
                System.out.println(">> Credenciales incorrectas o usuario ya conectado.");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error en login: " + e.getMessage());
            return false;
        }
    }

    private static void bucleMenuPrincipal() {
        boolean logout = false;
        while (!logout) {
            try {
                System.out.println("\n=== MENÚ USUARIO (" + miNick + ") ===");
                System.out.println("1.- Información del Usuario");
                System.out.println("2.- Enviar Trino");
                System.out.println("3.- Listar Usuarios del Sistema");
                System.out.println("4.- Seguir a");
                System.out.println("5.- Dejar de seguir a");
                System.out.println("6.- Borrar trino ");
                System.out.println("7.- Salir \"Logout\"");
                System.out.print("Elija opción: ");
                
                String opcion = scanner.nextLine();
                
                switch (opcion) {
                case "1":
                    System.out.println("\n--- INFO DE " + miNick + " ---");
                    int nSeguidores = gestorService.obtenerNumSeguidores(miNick);
                    int nTrinos = gestorService.obtenerNumTrinos(miNick);
                    System.out.println("Seguidores: " + nSeguidores);
                    System.out.println("Trinos publicados: " + nTrinos);
                    break;
                        
                    case "2":
                        System.out.print("Escribe tu mensaje: ");
                        String mensaje = scanner.nextLine();
                        gestorService.enviarTrino(miNick, mensaje);
                        System.out.println(">> Trino enviado.");
                        break;
                        
                    case "3":
                        List<String> usuarios = gestorService.listarUsuarios();
                        System.out.println("--- Usuarios del Sistema ---");

                        for (String u : usuarios) {
                            System.out.println("- " + u);
                        }
                        break;
                        
                    case "4":
                        System.out.print("Nick del usuario a seguir: ");
                        String aSeguir = scanner.nextLine();
                        
                        if (aSeguir.equals(miNick)) {
                            System.out.println(">> ERROR: No tiene sentido seguirte a ti mismo.");
                            break; 
                        }
                        
                        gestorService.seguirUsuario(miNick, aSeguir);
                        System.out.println(">> Ahora sigues a " + aSeguir);
                        break;
                        
                    case "5":
                        System.out.print("Nick del usuario a dejar de seguir: ");
                        String noSeguir = scanner.nextLine();
                        gestorService.dejarDeSeguirUsuario(miNick, noSeguir);
                        System.out.println(">> Dejaste de seguir a " + noSeguir);
                        break;
                        
                    case "6":
                        System.out.println("\n--- BORRAR UN TRINO ---");
                        List<common.Trino> misTrinos = gestorService.listarTrinos(miNick);
                        
                        if (misTrinos.isEmpty()) {
                            System.out.println("No tienes trinos publicados.");
                        } else {
                            System.out.println("Tus trinos (Copia el ID/Timestamp para borrar):");
                            for (common.Trino t : misTrinos) {
                                System.out.println("[" + t.GetTimestamp() + "] " + t.GetTrino());
                            }
                            
                            System.out.print("\nIntroduce el ID (número largo) del trino a borrar: ");
                            try {
                                String inputStr = scanner.nextLine();
                                long idBorrar = Long.parseLong(inputStr);
                                
                                boolean borrado = gestorService.borrarTrino(miNick, idBorrar);
                                if (borrado) {
                                    System.out.println(">> Trino eliminado correctamente.");
                                } else {
                                    System.out.println(">> Error: No se encontró un trino con ese ID.");
                                }
                            } catch (NumberFormatException e) {
                                System.out.println(">> Error: El ID debe ser un número válido.");
                            }
                        }
                        break;
                        
                    case "7":
                        authService.logout(miNick);
                        miNick = null;
                        logout = true;
                        System.out.println(">> Sesión cerrada.");
                        break;
                        
                    default:
                        System.out.println("Opción no válida.");
                }
            } catch (Exception e) {
                System.err.println("Error en operación: " + e.getMessage());
            }
        }
    }
    
}
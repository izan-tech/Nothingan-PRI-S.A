/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Login.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controlador web per a l'autenticació i gestió d'accés d'usuaris: Gestiona les
 * vistes públiques d'inici de sessió i recuperació de contrasenya. Processa els
 * paràmetres d'estat enviats per Spring Security per mostrar avisos d'error o
 * tancament de sessió.
 *
 * @author Grup3
 */
@Controller
public class AuthController {

    /**
     * Mostra la pantalla d'inici de sessió i gestiona els avisos d'estat:
     * Intercepta les peticions GET a "/login" i comprova si Spring Security ha
     * afegit els paràmetres "error" o "logout". Afegeix els missatges
     * informatius corresponents al model abans de carregar la plantilla HTML de
     * login.
     */
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        // Los parámetros ?error y ?logout son añadidos automáticamente por Spring Security 
        // y se pueden validar en la plantilla con ${param.error} o pasando atributos al modelo:
        if (error != null) {
            model.addAttribute("errorMessage", "Dades d'accés incorrectes.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "Sessió tancada correctament.");
        }

        return "login"; // Carga src/main/resources/templates/login.html
    }

    /**
     * Mostra la pantalla de recuperació de contrasenya: Atén les peticions GET
     * a "/forgot-password" quan un usuari sol·licita restablir la seva clau
     * d'accés. Retorna el nom de la plantilla HTML "forgot-password".
     */
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Home.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador de la pàgina d'inici de l'aplicació: Gestiona les rutes arrel
 * d'accés públic a la plataforma web. Redirigeix els usuaris cap a la vista
 * principal de benvinguda.
 *
 * @author Grup3
 */
@Controller
public class HomeController {

    /**
     * Mostra la vista principal d'inici (Home): Intercepta les peticions GET a
     * les rutes "/" i "/home". Retorna el nom de la plantilla Thymeleaf "home"
     * per renderitzar la portada.
     */
    @GetMapping({"/", "/home"})
    public String home() {
        return "home";
    }
}

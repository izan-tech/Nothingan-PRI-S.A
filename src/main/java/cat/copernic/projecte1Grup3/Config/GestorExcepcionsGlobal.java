/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Config;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Gestor global d'excepcions dels controladors: Intercepta de manera
 * centralitzada els errors no controlats de tota l'aplicació. Registra el
 * detall tècnic al log i redirigeix l'usuari al panell principal amb un avís.
 *
 * @author Grup3
 */
@ControllerAdvice
public class GestorExcepcionsGlobal {

    private static final Logger log = LoggerFactory.getLogger(GestorExcepcionsGlobal.class);

    /**
     * Captura i gestiona els errors d'integritat a la base de dades: Intercepta
     * les excepcions quan es violen restriccions (claus duplicades o camps
     * obligatoris buits). Escriu la causa específica al log i redirigeix al
     * dashboard informant que les dades no són vàlides.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public String integritat(DataIntegrityViolationException e,
            HttpServletRequest req,
            RedirectAttributes ra) {
        log.error("Error d'integritat a {} {}: {}", req.getMethod(), req.getRequestURI(),
                e.getMostSpecificCause().getMessage(), e);
        ra.addFlashAttribute("error", "No s'ha pogut desar: revisa que les dades siguin vàlides.");
        return "redirect:/dashboard";
    }

    /**
     * Captura i gestiona els errors d'estat il·legal de l'aplicació: Intercepta
     * les excepcions provocades quan s'intenta executar una acció en un estat
     * no permès. Registra la petició i la traça de l'error al log i redirigeix
     * l'usuari al dashboard.
     */
    @ExceptionHandler(IllegalStateException.class)
    public String estatIlegal(IllegalStateException e,
            HttpServletRequest req,
            RedirectAttributes ra) {
        log.error("Estat il·legal a {} {}", req.getMethod(), req.getRequestURI(), e);
        ra.addFlashAttribute("error", "S'ha produït un error inesperat.");
        return "redirect:/dashboard";
    }

    /**
     * Captura i gestiona els errors de referència nul·la
     * (NullPointerException): Evita que l'usuari vegi una pàgina d'error
     * tècnica del servidor davant d'un objecte nul. Desa la traça al log amb la
     * ruta de la petició i redirigeix de forma segura al dashboard.
     */
    @ExceptionHandler(NullPointerException.class)
    public String nul(NullPointerException e,
            HttpServletRequest req,
            RedirectAttributes ra) {
        log.error("NullPointerException a {} {}", req.getMethod(), req.getRequestURI(), e);
        ra.addFlashAttribute("error", "S'ha produït un error inesperat.");
        return "redirect:/dashboard";
    }
}

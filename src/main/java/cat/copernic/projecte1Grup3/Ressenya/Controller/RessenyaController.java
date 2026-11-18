/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Ressenya.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cat.copernic.projecte1Grup3.Ressenya.Service.RessenyaService;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.ClientEntity;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.ClientRepository;

/**
 * Controlador per a la gestió de ressenyes i valoracions de vehicles: Permet
 * tant als agents des del panell de control com als clients des del portal web
 * crear i eliminar ressenyes. Valida la identitat del client (manualment per
 * DNI o mitjançant la sessió autenticada) i delega la lògica a RessenyaService.
 *
 * @author Grup3
 */
@Controller
public class RessenyaController {

    private static final Logger log = LoggerFactory.getLogger(RessenyaController.class);

    private final RessenyaService resenyesLogic;
    private final ClientRepository clientRepository;

    /**
     * Injecció de dependències del servei de ressenyes i del repositori de
     * clients: Inicialitza el controlador rebent RessenyaService i
     * ClientRepository per gestionar les valoracions i identificar els autors.
     */
    public RessenyaController(RessenyaService resenyesLogic, ClientRepository clientRepository) {
        this.resenyesLogic = resenyesLogic;
        this.clientRepository = clientRepository;
    }

    /**
     * Crea una ressenya de vehicle des del panell d'administració (Agent): Rep
     * la matrícula del vehicle, el DNI del client introduït manualment, la
     * puntuació i el comentari opcional. Delega el registre al servei i
     * redirigeix a la fitxa de detall del vehicle informant del resultat.
     */
    // ---------- AGENT (panel): indica el DNI a mà ----------
    @PostMapping("/dashboard/vehicles/{matricula}/resenyes")
    public String alta(@PathVariable String matricula,
            @RequestParam String dni,
            @RequestParam int puntuacio,
            @RequestParam(required = false) String comentari,
            RedirectAttributes ra) {
        try {
            resenyesLogic.crear(matricula, dni, puntuacio, comentari);
            ra.addFlashAttribute("succes", "Ressenya afegida correctament");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error creant la ressenya", e);
            ra.addFlashAttribute("error", "No s'ha pogut crear la ressenya");
        }
        return "redirect:/dashboard/vehicles/detall/" + matricula;
    }

    /**
     * Crea una ressenya de vehicle des del portal de client: Obté
     * l'identificador del client directament de la sessió autenticada (cercant
     * per email o DNI) i registra la seva valoració. Redirigeix a la pantalla
     * de valorar reserva si s'ha indicat el codi, o bé al formulari de nova
     * reserva del vehicle.
     */
    // ---------- CLIENT (portal): el DNI surt de la sessió ----------
    @PostMapping("/reserves-client/vehicle/{matricula}/resenyes")
    public String altaClient(@PathVariable String matricula,
            @RequestParam int puntuacio,
            @RequestParam(required = false) String comentari,
            @RequestParam(required = false) Long codiReserva,
            Authentication auth,
            RedirectAttributes ra) {
        try {
            if (auth == null || !auth.isAuthenticated()) {
                throw new IllegalArgumentException("Cal iniciar sessió com a client");
            }
            String id = auth.getName().trim();

            // El login pot ser l'email o el DNI
            ClientEntity client = clientRepository.findByEmailIgnoreCase(id)
                    .or(() -> clientRepository.findById(id.toUpperCase()))
                    .orElseThrow(() -> new IllegalArgumentException("Cal iniciar sessió com a client"));

            resenyesLogic.crear(matricula, client.getDni(), puntuacio, comentari);
            ra.addFlashAttribute("succes", "Gràcies per la teva ressenya");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error creant la ressenya del client", e);
            ra.addFlashAttribute("error", "No s'ha pogut crear la ressenya");
        }

        // Torna a la fitxa de valorar si venim d'allà; si no, al formulari de reserva
        return codiReserva != null
                ? "redirect:/reserves-client/valorar/" + codiReserva
                : "redirect:/reserves-client/nova?matricula=" + matricula;
    }

    /**
     * Elimina una ressenya des del panell d'administració (Agent): Esborra la
     * ressenya identificada pel seu codi sense restricció d'autoria mitjançant
     * el servei. Redirigeix novament a la vista de detall del vehicle mostrant
     * el missatge de confirmació o d'error.
     */
    @PostMapping("/dashboard/vehicles/{matricula}/resenyes/{codi}/eliminar")
    public String eliminar(@PathVariable String matricula,
            @PathVariable Long codi,
            RedirectAttributes ra) {
        try {
            resenyesLogic.eliminar(codi);
            ra.addFlashAttribute("succes", "Ressenya eliminada correctament");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error eliminant la ressenya {}", codi, e);
            ra.addFlashAttribute("error", "No s'ha pogut eliminar la ressenya");
        }
        return "redirect:/dashboard/vehicles/detall/" + matricula;
    }

    /**
     * Elimina la pròpia ressenya des del portal de client: Verifica la sessió
     * activa de l'usuari, localitza el seu DNI i sol·licita l'eliminació
     * comprovant que en sigui l'autor. Redirigeix a la pàgina d'origen
     * (valoració de reserva o formulari de nova reserva) amb l'avís
     * corresponent.
     */
    // ---------- CLIENT (portal): eliminar la pròpia ressenya ----------
    @PostMapping("/reserves-client/vehicle/{matricula}/resenyes/{codi}/eliminar")
    public String eliminarClient(@PathVariable String matricula,
            @PathVariable Long codi,
            @RequestParam(required = false) Long codiReserva,
            Authentication auth,
            RedirectAttributes ra) {
        try {
            if (auth == null || !auth.isAuthenticated()) {
                throw new IllegalArgumentException("Cal iniciar sessió com a client");
            }
            String id = auth.getName().trim();
            ClientEntity client = clientRepository.findByEmailIgnoreCase(id)
                    .or(() -> clientRepository.findById(id.toUpperCase()))
                    .orElseThrow(() -> new IllegalArgumentException("Cal iniciar sessió com a client"));

            resenyesLogic.eliminarDelClient(codi, client.getDni());
            ra.addFlashAttribute("succes", "Ressenya eliminada correctament");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error eliminant la ressenya {}", codi, e);
            ra.addFlashAttribute("error", "No s'ha pogut eliminar la ressenya");
        }
        return codiReserva != null
                ? "redirect:/reserves-client/valorar/" + codiReserva
                : "redirect:/reserves-client/nova?matricula=" + matricula;
    }
}

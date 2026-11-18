/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Usuaris.Client.Controller;

import cat.copernic.projecte1Grup3.Usuaris.Client.Documents.ClientDocuments;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.ClientEntity;
import cat.copernic.projecte1Grup3.Usuaris.Client.Service.ClientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador web principal per a la gestió de clients tant al panell intern com al portal públic:
 * Gestiona les operacions CRUD, validació d'identitat i descàrrega de documents des del dashboard d'agents/administradors.
 * També controla l'autoregistrament públic (/registre) i l'edició del perfil propi (/perfil) per part dels clients.
 *
 * @author Grup3
 */
@Controller
public class ClientController {

    private static final Logger log = LoggerFactory.getLogger(ClientController.class);
    private final ClientService clientLogic;

    /**
     * Injecció del servei de lògica de negoci de clients:
     * Inicialitza el controlador rebent ClientService per delegar totes les operacions sobre clients i documents.
     */
    public ClientController(ClientService clientLogic) {
        this.clientLogic = clientLogic;
    }

    // =====================================================
    // ALTA DE CLIENTE (PANEL AGENTS)
    // =====================================================

    /**
     * Mostra el formulari d'alta de client des del panell de control intern:
     * Crea una instància buida de ClientEntity si el model no conté ja dades prèvies d'un intent anterior fallit.
     * Retorna la vista HTML "client/alta_usuari".
     */
    @GetMapping({"/dashboard/clients/alta", "/dashboard/clients/altaUsuari", "/dashboard/clients/nou"})
    public String mostrarFormulari(Model model) {
        if (!model.containsAttribute("client")) {
            model.addAttribute("client", new ClientEntity());
        }
        return "client/alta_usuari";
    }

    /**
     * Processa l'alta directa d'un client realitzada per un agent o administrador:
     * Desa les dades i les tres imatges documentals marcant el client directament com a VALIDAT.
     * En cas d'error de validació o sistema, neteja les dades sensibles (contrasenya i targeta) i retorna al formulari.
     */
    @PostMapping({"/dashboard/clients/alta", "/dashboard/clients/altaUsuari"})
    public String altaClient(
            @ModelAttribute("client") ClientEntity client,
            @RequestParam("fotoDniFrontal") MultipartFile fotoDniFrontal,
            @RequestParam("fotoDniPosterior") MultipartFile fotoDniPosterior,
            @RequestParam("fotoCarnet") MultipartFile fotoCarnet,
            Model model,
            RedirectAttributes ra) {
        String error;
        try {
            clientLogic.alta(client, fotoDniFrontal, fotoDniPosterior, fotoCarnet, true);
            ra.addFlashAttribute("missatge", "Client donat d'alta i validat correctament!");
            return "redirect:/dashboard/clients";
        } catch (IllegalArgumentException e) {
            // Error de validación previsto (DNI incorrecto, contraseña corta...): se muestra tal cual
            error = e.getMessage();
        } catch (Exception e) {
            // Error inesperado: se registra en el log y al usuario solo se le da un mensaje genérico
            log.error("Error en l'alta del client", e);
            error = "No s'ha pogut completar el registre";
        }

        // Nunca se devuelve la contraseña ni los datos de la tarjeta al formulario
        client.setPassword(null);
        client.setNumTargeta(null);
        model.addAttribute("error", error);
        return "client/alta_usuari";
    }

    // =====================================================
    // LISTADO Y DETALLE
    // =====================================================

    /**
     * Llista i filtra els clients registrats segons múltiples criteris opcionals:
     * Aplica els filtres de nacionalitat, nom, telèfon, ciutat, DNI, població i correu electrònic, i retorna els paràmetres a la vista.
     */
    @GetMapping("/dashboard/clients")
    public String llistar(@RequestParam(required = false) String nacionalitat,
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String telefon,
            @RequestParam(required = false) String ciutat,
            @RequestParam(required = false) String dni,
            @RequestParam(required = false) String poblacio,
            @RequestParam(required = false) String email,
            Model model) {
        model.addAttribute("clients",
                clientLogic.filtrarClients(nacionalitat, nom, telefon, ciutat, dni, poblacio, email));
        model.addAttribute("nacionalitat", nacionalitat);
        model.addAttribute("nom", nom);
        model.addAttribute("telefon", telefon);
        model.addAttribute("ciutat", ciutat);
        model.addAttribute("dni", dni);
        model.addAttribute("poblacio", poblacio);
        model.addAttribute("email", email);
        return "client/llista_clients";
    }

    /**
     * Mostra la fitxa amb tota la informació detallada d'un client identificat pel seu DNI:
     * Si el client existeix el carrega al model per mostrar "client/detall_client"; en cas contrari redirigeix al llistat amb un error.
     */
    @GetMapping({"/dashboard/clients/{dni}", "/dashboard/clients/detall/{dni}"})
    public String detall(@PathVariable String dni, Model model, RedirectAttributes ra) {
        return clientLogic.obtenir(dni).map(c -> {
            model.addAttribute("client", c);
            return "client/detall_client";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Client no trobat");
            return "redirect:/dashboard/clients";
        });
    }

    /**
     * Serveix els bytes binaris d'una imatge documental del client emmagatzemada a MongoDB:
     * Selecciona la fotografia frontal del DNI, posterior del DNI o del carnet de conduir segons el tipus sol·licitat amb el seu tipus MIME.
     */
    @GetMapping("/dashboard/clients/{dni}/imatge/{tipus}")
    @ResponseBody
    public ResponseEntity<byte[]> imatge(@PathVariable String dni, @PathVariable String tipus) {
        Optional<ClientDocuments> docs = clientLogic.obtenirDocuments(dni);
        if (docs.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        ClientDocuments d = docs.get();

        byte[] bytes;
        String mime;
        switch (tipus) {
            case "frontal" -> {
                bytes = d.getFotoDniFrontal();
                mime = d.getTipusFotoDniFrontal();
            }
            case "posterior" -> {
                bytes = d.getFotoDniPosterior();
                mime = d.getTipusFotoDniPosterior();
            }
            case "carnet" -> {
                bytes = d.getFotoCarnet();
                mime = d.getTipusFotoCarnet();
            }
            default -> {
                return ResponseEntity.notFound().build();
            }
        }

        if (bytes == null || mime == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mime))
                .body(bytes);
    }

    // =====================================================
    // EDICIÓN Y ELIMINACIÓN
    // =====================================================

    /**
     * Mostra el formulari d'edició precarregat amb les dades d'un client existent:
     * Recupera el client pel seu DNI i renderitza "client/editar_client", o redirigeix al llistat si no es troba.
     */
    @GetMapping("/dashboard/clients/{dni}/editar")
    public String formulariEditar(@PathVariable String dni, Model model, RedirectAttributes ra) {
        return clientLogic.obtenir(dni).map(c -> {
            model.addAttribute("client", c);
            return "client/editar_client";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Client no trobat");
            return "redirect:/dashboard/clients";
        });
    }

    /**
     * Processa l'actualització de les dades, estat de validació, estat premium i documents d'un client:
     * Delega els canvis al servei capturant possibles errors de negoci, integritat de dades o lectura de fitxers.
     * Redirigeix sempre al llistat general de clients amb un missatge flash informatiu.
     */
    @PostMapping("/dashboard/clients/{dni}/editar")
    public String editar(@PathVariable String dni,
            @ModelAttribute("client") ClientEntity dades,
            @RequestParam String estatValidacio,
            @RequestParam(defaultValue = "false") boolean premium,
            @RequestParam(value = "fotoDniFrontal", required = false) MultipartFile fotoDniFrontal,
            @RequestParam(value = "fotoDniPosterior", required = false) MultipartFile fotoDniPosterior,
            @RequestParam(value = "fotoCarnet", required = false) MultipartFile fotoCarnet,
            RedirectAttributes ra) {
        try {
            clientLogic.actualitzar(dni, dades, estatValidacio, premium,
                    fotoDniFrontal, fotoDniPosterior, fotoCarnet);
            ra.addFlashAttribute("missatge", "Client actualitzat correctament");
        } catch (IllegalArgumentException e) {
            // Validación de negocio (correo duplicado, teléfono incorrecto...): mensaje tal cual
            ra.addFlashAttribute("error", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            // Violación de la base de datos no prevista: se guarda la causa real en el log
            log.error("Error d'integritat editant el client {}: {}", dni, e.getMostSpecificCause().getMessage(), e);
            ra.addFlashAttribute("error",
                    "No s'ha pogut guardar el client. Revisa que totes les dades siguin vàlides");
        } catch (IOException e) {
            log.error("Error llegint les imatges del client {}", dni, e);
            ra.addFlashAttribute("error", "No s'han pogut llegir les imatges");
        }
        return "redirect:/dashboard/clients";
    }

    /**
     * Elimina un client de la base de dades relacional i els seus documents associats a MongoDB:
     * Captura les restriccions d'integritat referencial en cas que el client tingui reserves vinculades i informa l'usuari.
     */
    @PostMapping("/dashboard/clients/{dni}/eliminar")
    public String eliminar(@PathVariable String dni, RedirectAttributes ra) {
        try {
            clientLogic.eliminar(dni);
            ra.addFlashAttribute("missatge", "Client eliminat correctament");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            ra.addFlashAttribute("error", "No es pot eliminar: el client té reserves associades");
        }
        return "redirect:/dashboard/clients";
    }

    // =====================================================
    // VALIDACIÓN Y ESTADO (RF82)
    // =====================================================

    /**
     * Actualitza ràpidament l'estat de validació d'un client des del llistat (PENDENT, VALIDAT o REBUTJAT):
     * Invoca el canvi d'estat al servei i redirigeix al llistat amb una notificació de confirmació.
     */
    @PostMapping("/dashboard/clients/{dni}/validacio")
    public String validar(@PathVariable String dni, @RequestParam String estat, RedirectAttributes ra) {
        clientLogic.canviarEstat(dni, estat);
        ra.addFlashAttribute("missatge", "Estat actualitzat a " + estat);
        return "redirect:/dashboard/clients";
    }

    /**
     * Activa o desactiva la condició de client prèmium des del panell de gestió:
     * Actualitza el indicador booleà al servei i retorna al llistat de clients.
     */
    @PostMapping("/dashboard/clients/{dni}/premium")
    public String premium(@PathVariable String dni, @RequestParam boolean premium, RedirectAttributes ra) {
        clientLogic.canviarPremium(dni, premium);
        ra.addFlashAttribute("missatge", "Estat premium actualitzat");
        return "redirect:/dashboard/clients";
    }

    // =====================================================
    // REGISTRE DES DEL PORTAL CLIENT (RF81)
    // =====================================================

    /**
     * Mostra el formulari públic d'autoregistrament per a nous clients:
     * Inicialitza una entitat buida al model si no prové d'una redirecció amb error i renderitza "registre".
     */
    @GetMapping("/registre")
    public String mostrarFormulariRegistre(Model model) {
        if (!model.containsAttribute("client")) {
            model.addAttribute("client", new ClientEntity());
        }
        return "registre";
    }

    /**
     * Processa la sol·licitud d'alta enviada per un usuari extern des del portal web:
     * Registra el client amb estat PENDENT fins a la revisió d'un administrador i redirigeix al catàleg, o retorna al formulari si hi ha errors.
     */
    @PostMapping("/registre")
    public String processarRegistreClient(
            @ModelAttribute("client") ClientEntity client,
            @RequestParam("fotoDniFrontal") MultipartFile fotoDniFrontal,
            @RequestParam("fotoDniPosterior") MultipartFile fotoDniPosterior,
            @RequestParam("fotoCarnet") MultipartFile fotoCarnet,
            RedirectAttributes ra) {
        String error;
        try {
            clientLogic.alta(client, fotoDniFrontal, fotoDniPosterior, fotoCarnet);
            ra.addFlashAttribute("missatge", "Sol·licitud de registre enviada amb èxit! L'administrador ha de validar el teu compte.");
            return "redirect:/cataleg";
        } catch (IllegalArgumentException e) {
            error = e.getMessage();
        } catch (Exception e) {
            log.error("Error en el registre del client extern", e);
            error = "No s'ha pogut completar el registre. Revisa que les dades i fitxers siguin correctes.";
        }

        // Conservamos lo escrito (nunca la contraseña ni la tarjeta)
        client.setPassword(null);
        client.setNumTargeta(null);
        ra.addFlashAttribute("client", client);
        ra.addFlashAttribute("error", error);
        return "redirect:/registre";
    }

    // =====================================================
    // EL MEU PERFIL (PORTAL CLIENT)
    // =====================================================

    /**
     * Mostra la vista del perfil personal del client autenticat:
     * Recupera el client associat al correu electrònic de la sessió activa i renderitza "client/perfil".
     */
    @GetMapping("/perfil")
    public String veurePerfil(Authentication auth, Model model, RedirectAttributes ra) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        return clientLogic.obtenirPerEmail(auth.getName()).map(c -> {
            model.addAttribute("client", c);
            return "client/perfil";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "No s'ha trobat el perfil del client.");
            return "redirect:/cataleg";
        });
    }

    /**
     * Desa les modificacions realitzades pel client al seu propi perfil personal:
     * Si el client modifica el seu correu electrònic d'accés, tanca la sessió actual de Spring Security i el redirigeix al login.
     */
    @PostMapping("/perfil")
    public String guardarPerfil(@ModelAttribute("client") ClientEntity dades,
            Authentication auth,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes ra) {
        String emailActual = auth.getName();
        try {
            clientLogic.actualitzarPerfilClient(emailActual, dades);

            // Si el correo ha cambiado, la sesión actual ya no es válida: se cierra
            if (dades.getEmail() != null && !emailActual.equalsIgnoreCase(dades.getEmail().trim())) {
                new SecurityContextLogoutHandler().logout(request, response, auth);
                ra.addFlashAttribute("missatge", "Correu electrònic actualitzat. Torna a iniciar sessió amb el nou correu.");
                return "redirect:/login";
            }

            ra.addFlashAttribute("missatge", "Les teves dades s'han actualitzat correctament!");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            log.error("Error d'integritat actualitzant perfil de {}", emailActual, e);
            ra.addFlashAttribute("error", "No s'han pogut desar els canvis. Revisa les dades introduïdes.");
        }
        return "redirect:/perfil";
    }
}
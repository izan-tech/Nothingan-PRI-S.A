package cat.copernic.projecte1Grup3.Reserves.Controller;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.mail.MailException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cat.copernic.projecte1Grup3.Reserves.Entity.ReservesEntity;
import cat.copernic.projecte1Grup3.Reserves.Service.ReservaAnulacioEmailService;
import cat.copernic.projecte1Grup3.Reserves.Service.ReservesClientService;
import cat.copernic.projecte1Grup3.Ressenya.Service.RessenyaService;

@Controller
public class ReservesClientController {

    private static final Logger log = LoggerFactory.getLogger(ReservesClientController.class);

    private final ReservesClientService reservesClientService;
    private final ReservaAnulacioEmailService reservaAnulacioEmailService;
    private final RessenyaService ressenyaService;

    public ReservesClientController(ReservesClientService reservesClientService,
            ReservaAnulacioEmailService reservaAnulacioEmailService,
            RessenyaService ressenyaService) {
        this.reservesClientService = reservesClientService;
        this.reservaAnulacioEmailService = reservaAnulacioEmailService;
        this.ressenyaService = ressenyaService;
    }

    // Llistat de reserves del client amb filtres per codi i dates (G3P1-245)
    @GetMapping("/reserves-client")
    public String mostrarReserves(
            @RequestParam(required = false) String codi,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInici,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFi,
            Authentication authentication,
            Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        model.addAttribute("reserves",
                reservesClientService.filtrarPerClient(authentication.getName(), codi, dataInici, dataFi));
        model.addAttribute("codi", codi);
        model.addAttribute("dataInici", dataInici);
        model.addAttribute("dataFi", dataFi);
        return "reserves/les_meves_reserves";
    }

    // Vista de detall d'una reserva del client (G3P1-244)
    @GetMapping("/reserves-client/{codi}")
    public String detallReserva(@PathVariable("codi") Long codi,
            Authentication authentication,
            Model model,
            RedirectAttributes ra) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        return reservesClientService.obtenirReservaDelClient(codi, authentication.getName())
                .map(r -> {
                    model.addAttribute("reserva", r);
                    return "reserves/detall_reserva_client";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("error", "No s'ha trobat aquesta reserva o no tens permís per veure-la.");
                    return "redirect:/reserves-client";
                });
    }

    @PostMapping("/reserves-client/{codi}/anular")
    public String anularReserva(@PathVariable("codi") Long codi,
            Authentication authentication,
            RedirectAttributes ra) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            ReservesClientService.ResultatAnulacio resultat
                    = reservesClientService.anularReservaDelClient(codi, authentication.getName());
            try {
                reservaAnulacioEmailService.enviarNotificacioClient(
                        resultat.reserva(), resultat.devolucio());
            } catch (MailException | IllegalStateException e) {
                log.error("Reserva #{} anul·lada pel client però no s'ha pogut enviar el correu",
                        codi, e);
                ra.addFlashAttribute(
                        "error",
                        "La reserva s'ha anul·lat, però no s'ha pogut enviar el correu de confirmació."
                );
                return "redirect:/reserves-client";
            }

            ra.addFlashAttribute("missatge",
                    "Reserva #" + codi + " anul·lada. S'ha enviat un correu amb l'import de devolució.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error anul·lant la reserva #{} des del compte del client", codi, e);
            ra.addFlashAttribute("error", "No s'ha pogut anul·lar la reserva #" + codi + ".");
        }
        return "redirect:/reserves-client";
    }

    // Fitxa del vehicle + ressenyes + formulari de valorar
    @GetMapping("/reserves-client/valorar/{codi}")
    public String valorarReserva(@PathVariable("codi") Long codi,
            Authentication authentication,
            Model model,
            RedirectAttributes ra) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        return reservesClientService.obtenirReservaDelClient(codi, authentication.getName())
                .map(r -> {
                    model.addAttribute("reserva", r);
                    model.addAttribute("vehicle", r.getVehicle());
                    model.addAttribute("resenyes",
                            ressenyaService.llistarPerVehicle(r.getVehicle().getMatricula()));
                    model.addAttribute("jaValorat", ressenyaService.jaHaValorat(
                            r.getVehicle().getMatricula(), r.getClient().getDni()));
                    model.addAttribute("dniClient", r.getClient().getDni());
                    return "reserves/valorar_reserva";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("error", "No s'ha trobat aquesta reserva.");
                    return "redirect:/reserves-client";
                });
    }
}
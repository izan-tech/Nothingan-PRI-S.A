/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Vehicles.Controller;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cat.copernic.projecte1Grup3.Ressenya.Service.RessenyaService;
import cat.copernic.projecte1Grup3.Vehicles.Entity.VehiclesEntity;
import cat.copernic.projecte1Grup3.Vehicles.Service.VehiclesService;
import jakarta.validation.Valid;

/**
 * Controlador web Spring MVC per a la gestió de la flota de vehicles al panell
 * d'administració: Gestiona el llistat amb filtres avançats, la consulta de
 * detall amb ressenyes, les altes i edicions amb pujada d'imatges. També
 * controla l'activació, desactivació per manteniment i eliminació segura dels
 * vehicles sota la ruta "/dashboard/vehicles".
 *
 * @author Grup3
 */
@Controller
@RequestMapping("/dashboard/vehicles")
public class VehiclesController {

    private static final Logger log = LoggerFactory.getLogger(VehiclesController.class);
    private final VehiclesService vehiclesService;
    private final RessenyaService ressenyaLogic;

    /**
     * Constructor amb injecció de dependències dels serveis de vehicles i
     * ressenyes: Inicialitza els components de lògica de negoci necessaris per
     * gestionar la flota i les seves valoracions.
     */
    @Autowired
    public VehiclesController(VehiclesService vehiclesService, RessenyaService ressenyaLogic) {
        this.vehiclesService = vehiclesService;
        this.ressenyaLogic = ressenyaLogic;
    }

    /**
     * Configura el WebDataBinder per normalitzar automàticament el camp
     * matrícula: Elimina espais en blanc als extrems i converteix la matrícula
     * a majúscules abans de vincular-la a l'entitat.
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, "matricula", new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(text == null ? null : text.trim().toUpperCase());
            }
        });
    }

    /**
     * Mostra el llistat de vehicles filtrat per criteris bàsics i avançats:
     * Valida el rang de dates de disponibilitat, aplica els filtres al servei i
     * passa l'estat de cerca a la vista.
     */
    @GetMapping
    public String llistarVehicles(
            @RequestParam(name = "matricula", required = false) String matricula,
            @RequestParam(name = "marca", required = false) String marca,
            @RequestParam(name = "model", required = false) String modelVehicle,
            @RequestParam(name = "anyMatriculacio", required = false) Integer anyMatriculacio,
            @RequestParam(name = "tipus", required = false) String tipus,
            @RequestParam(name = "estat", required = false) Boolean estat,
            @RequestParam(name = "dataInici", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInici,
            @RequestParam(name = "dataFi", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFi,
            Model model) {

        LocalDateTime iniciDT = null;
        LocalDateTime fiDT = null;

        if (dataInici != null && dataFi != null && dataInici.isAfter(dataFi)) {
            model.addAttribute("error", "La data mínima de disponibilitat no pot ser posterior a la data màxima.");
        } else {
            iniciDT = (dataInici != null) ? dataInici.atStartOfDay() : null;
            fiDT = (dataFi != null) ? dataFi.atTime(LocalTime.MAX) : null;
        }

        List<VehiclesEntity> vehicles = vehiclesService.filtrarVehicles(
                matricula, marca, modelVehicle, anyMatriculacio, tipus, estat, iniciDT, fiDT);

        boolean teFiltresAvancats = (marca != null && !marca.isBlank())
                || (modelVehicle != null && !modelVehicle.isBlank())
                || anyMatriculacio != null
                || dataInici != null
                || dataFi != null;

        boolean teAlgunFiltre = teFiltresAvancats
                || (matricula != null && !matricula.isBlank())
                || (tipus != null && !tipus.isBlank())
                || estat != null;

        model.addAttribute("vehicles", vehicles);
        model.addAttribute("matricula", matricula);
        model.addAttribute("marca", marca);
        model.addAttribute("modelVehicle", modelVehicle);
        model.addAttribute("anyMatriculacio", anyMatriculacio);
        model.addAttribute("tipus", tipus);
        model.addAttribute("estat", estat);
        model.addAttribute("dataInici", dataInici);
        model.addAttribute("dataFi", dataFi);
        model.addAttribute("teFiltresAvancats", teFiltresAvancats);
        model.addAttribute("teAlgunFiltre", teAlgunFiltre);

        return "vehicles/llistat";
    }

    /**
     * Mostra la fitxa de detall d'un vehicle juntament amb les seves ressenyes
     * associades: Si la matrícula no existeix a la base de dades, redirigeix al
     * llistat amb un missatge d'error.
     */
    @GetMapping("/detall/{matricula}")
    public String mostrarDetall(@PathVariable("matricula") String matricula, Model model, RedirectAttributes redirect) {
        Optional<VehiclesEntity> vehicle = vehiclesService.buscarPerMatricula(matricula);
        if (vehicle.isPresent()) {
            model.addAttribute("vehicle", vehicle.get());
            model.addAttribute("resenyes", ressenyaLogic.llistarPerVehicle(matricula));
            return "vehicles/detall_vehicle";
        }
        redirect.addFlashAttribute("error", "El vehicle amb matrícula " + matricula + " no existeix.");
        return "redirect:/dashboard/vehicles";
    }

    /**
     * Prepara i mostra el formulari d'alta per registrar un nou vehicle a la
     * flota: Afegeix una instància buida de VehiclesEntity al model i retorna
     * la vista d'alta.
     */
    @GetMapping("/nou")
    public String formulariNouVehicle(Model model) {
        model.addAttribute("vehicle", new VehiclesEntity());
        return "vehicles/alta_vehicle";
    }

    /**
     * Carrega les dades d'un vehicle existent per mostrar-les al formulari
     * d'edició: Redirigeix al llistat amb una notificació d'error si no es
     * troba cap vehicle amb la matrícula indicada.
     */
    @GetMapping("/editar/{matricula}")
    public String formulariEditarVehicle(@PathVariable("matricula") String matricula, Model model, RedirectAttributes redirect) {
        Optional<VehiclesEntity> vehicle = vehiclesService.buscarPerMatricula(matricula);
        if (vehicle.isPresent()) {
            model.addAttribute("vehicle", vehicle.get());
            return "vehicles/editar_vehicle";
        }
        redirect.addFlashAttribute("error", "No s'ha trobat el vehicle per editar.");
        return "redirect:/dashboard/vehicles";
    }

    /**
     * Processa l'alta o l'actualització d'un vehicle i gestiona la pujada de la
     * seva fotografia: Valida les restriccions de l'entitat i la coherència
     * dels dies de lloguer, desa el fitxer al disc i persisteix el registre.
     */
    @PostMapping("/guardar")
    public String guardarVehicle(
            @Valid @ModelAttribute("vehicle") VehiclesEntity vehicle,
            BindingResult result,
            @RequestParam(name = "file", required = false) MultipartFile file,
            Model model,
            RedirectAttributes redirect) {

        Optional<VehiclesEntity> existentOpt = (vehicle.getMatricula() != null && !vehicle.getMatricula().isBlank())
                ? vehiclesService.buscarPerMatricula(vehicle.getMatricula())
                : Optional.empty();
        boolean existeixBDD = existentOpt.isPresent();

        if (vehicle.getMinDiesLloguer() != null && vehicle.getMaxDiesLloguer() != null) {
            if (vehicle.getMinDiesLloguer() > vehicle.getMaxDiesLloguer()) {
                result.rejectValue("maxDiesLloguer", "error.vehicle", "El màxim de dies ha de ser major o igual al mínim.");
            }
        }

        if (result.hasErrors()) {
            return existeixBDD ? "vehicles/editar_vehicle" : "vehicles/alta_vehicle";
        }

        if (file != null && !file.isEmpty()) {
            try {
                String fileName = vehicle.getMatricula() + "_" + file.getOriginalFilename();
                Path uploadPath = Paths.get("uploads/vehicles");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Files.copy(file.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                vehicle.setFoto(fileName);
            } catch (IOException e) {
                log.error("Error pujant la foto del vehicle {}", vehicle.getMatricula(), e);
                redirect.addFlashAttribute("error", "Error en carregar la imatge del vehicle.");
            }
        } else if (existeixBDD) {
            vehicle.setFoto(existentOpt.get().getFoto());
        }

        vehiclesService.guardarVehicle(vehicle);
        log.info("Vehicle {}: {}", existeixBDD ? "actualitzat" : "donat d'alta", vehicle.getMatricula());
        redirect.addFlashAttribute("succes", "Vehicle desat correctament.");
        return "redirect:/dashboard/vehicles";
    }

    /**
     * Desactiva temporalment un vehicle establint una possible data de
     * finalització de la baixa: Captura les validacions de negoci i informa del
     * resultat mitjançant missatges flash en redirigir al llistat.
     */
    @PostMapping("/desactivar/{matricula}")
    public String desactivarVehicle(
            @PathVariable("matricula") String matricula,
            @RequestParam(name = "dataFiDesactivacio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFiDesactivacio,
            RedirectAttributes redirect) {
        try {
            vehiclesService.desactivarVehicle(matricula, dataFiDesactivacio);
            log.info("Vehicle {} desactivat (data fi: {})", matricula, dataFiDesactivacio);
            redirect.addFlashAttribute("succes", "Vehicle " + matricula + " desactivat correctament en la data actual.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.warn("No s'ha pogut desactivar el vehicle {}: {}", matricula, e.getMessage());
            redirect.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error desactivant el vehicle {}", matricula, e);
            redirect.addFlashAttribute("error", "Error en intentar desactivar el vehicle.");
        }
        return "redirect:/dashboard/vehicles";
    }

    /**
     * Reactiva un vehicle prèviament desactivat registrant el motiu i l'import
     * del cost associat: Delega l'operació al servei de vehicles i redirigeix
     * al llistat principal notificant l'èxit o l'error.
     */
    @PostMapping("/activar/{matricula}")
    public String activarVehicle(
            @PathVariable("matricula") String matricula,
            @RequestParam(name = "motiu", required = false) String motiu,
            @RequestParam(name = "importCost", required = false) BigDecimal importCost,
            RedirectAttributes redirect) {
        try {
            vehiclesService.activarVehicle(matricula, motiu, importCost);
            log.info("Vehicle {} activat (motiu: {}, import: {})", matricula, motiu, importCost);
            redirect.addFlashAttribute("succes", "Vehicle " + matricula + " activat correctament en la data actual.");
        } catch (IllegalArgumentException e) {
            log.warn("Dades invàlides activant el vehicle {}: {}", matricula, e.getMessage());
            redirect.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error activant el vehicle {}", matricula, e);
            redirect.addFlashAttribute("error", "Error en intentar activar el vehicle.");
        }
        return "redirect:/dashboard/vehicles";
    }

    /**
     * Elimina definitivament un vehicle del sistema si no té restriccions
     * actives que ho impedeixin: Gestiona les excepcions d'estat invàlid i
     * redirigeix al llistat amb el missatge corresponenent.
     */
    @PostMapping("/eliminar/{matricula}")
    public String eliminarVehicle(@PathVariable("matricula") String matricula, RedirectAttributes redirect) {
        try {
            vehiclesService.eliminarVehicle(matricula);
            log.warn("Vehicle eliminat: {}", matricula);
            redirect.addFlashAttribute("succes", "Vehicle eliminat amb èxit.");
        } catch (IllegalStateException e) {
            log.warn("No s'ha pogut eliminar el vehicle {}: {}", matricula, e.getMessage());
            redirect.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error eliminant el vehicle {}", matricula, e);
            redirect.addFlashAttribute("error", "Error en intentar eliminar el vehicle.");
        }
        return "redirect:/dashboard/vehicles";
    }
}

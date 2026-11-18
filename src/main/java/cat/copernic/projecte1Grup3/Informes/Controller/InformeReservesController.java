/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Informes.Controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import cat.copernic.projecte1Grup3.Informes.Service.InformeException;
import cat.copernic.projecte1Grup3.Informes.Service.InformeReservesService;
import cat.copernic.projecte1Grup3.Usuaris.Client.Service.ClientService;

/**
 * Controlador per a la generació d'informes de reserves: Gestiona les peticions
 * web per consultar el detall i resum de reserves agrupades per client. Permet
 * filtrar els resultats seleccionant un o diversos DNIs des del panell de
 * control.
 *
 * @author Grup3
 */
@Controller
public class InformeReservesController {

    private final InformeReservesService informeService;
    private final ClientService clientLogic;

    /**
     * Injecció de dependències dels serveis d'informes i clients: Inicialitza
     * el controlador rebent InformeReservesService i ClientService per
     * construir les dades de l'informe.
     */
    public InformeReservesController(InformeReservesService informeService,
            ClientService clientLogic) {
        this.informeService = informeService;
        this.clientLogic = clientLogic;
    }

    /**
     * Genera i mostra la vista de l'informe de reserves per client: Carrega el
     * llistat de clients validats al model i invoca el servei per obtenir
     * l'informe filtrat pels DNIs rebuts. Captura possibles errors de validació
     * o generació per mostrar un missatge d'avís a la plantilla Thymeleaf.
     */
    @GetMapping("/dashboard/informes/reserves")
    public String informeReserves(@RequestParam(required = false) List<String> dnis,
            Model model) {
        model.addAttribute("clients", clientLogic.llistar(null, null, "VALIDAT", null, null));
        model.addAttribute("dnisSeleccionats", dnis == null ? List.of() : dnis);
        try {
            model.addAttribute("informe", informeService.generarInforme(dnis));
        } catch (IllegalArgumentException | InformeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "informes/informe_reserves";
    }
}

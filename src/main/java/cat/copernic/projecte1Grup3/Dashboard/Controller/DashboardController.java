/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Dashboard.Controller;

// --- Importaciones para manejo de estructuras de datos en JSON y API REST ---
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody; // Permite devolver objetos/JSON en lugar de plantillas Thymeleaf

import cat.copernic.projecte1Grup3.Reserves.Service.ReservesService;
import cat.copernic.projecte1Grup3.Usuaris.Client.Service.ClientService;
import cat.copernic.projecte1Grup3.Vehicles.Service.VehiclesService;

/**
 * Controlador principal del Dashboard de gestió: Gestiona tant la càrrega
 * inicial de la vista HTML com l'endpoint API REST per actualitzar les
 * estadístiques en temps real. Recopila els indicadors clau (KPIs) i els
 * propers lliuraments des dels serveis de reserves, clients i vehicles.
 *
 * @author Grup3
 */
@Controller
public class DashboardController {

    // Referencias a los servicios de lógica de negocio
    private final ReservesService reservesLogic;
    private final ClientService clientLogic;
    private final VehiclesService vehiclesLogic;

    /**
     * Injecció de dependències dels serveis del Dashboard: Inicialitza el
     * controlador rebent les instàncies de ReservesService, ClientService i
     * VehiclesService. Permet consultar els comptadors i llistats necessaris
     * per alimentar el panell de control.
     */
    public DashboardController(ReservesService reservesLogic,
            ClientService clientLogic,
            VehiclesService vehiclesLogic) {
        this.reservesLogic = reservesLogic;
        this.clientLogic = clientLogic;
        this.vehiclesLogic = vehiclesLogic;
    }

    // ============================================================
    // CARGA INICIAL DE LA PÁGINA (GET /dashboard)
    // ============================================================
    /**
     * Càrrega inicial de la vista principal del Dashboard: Calcula els
     * comptadors globals de lliuraments pendents, retorns, vehicles actius i
     * clients registrats. Afegegeix aquestes dades i els propers lliuraments al
     * model per renderitzar la plantilla HTML.
     */
    @GetMapping({"/dashboard"})
    public String mostrarDashboard(Model model) {
        // Cálculo de contadores globales
        long pendentsLliurament = reservesLogic.comptarPendentsLliurament();  // PENDENT o CONFIRMADA
        long pendentsRetorn = reservesLogic.comptarPendentsRetorn();          // EN CURS
        long vehiclesActius = reservesLogic.vehiclesActius().size();          // Total vehículos activos
        long clientsTotals = clientLogic.llistar(null, null, null, null, null).size(); // Total clientes

        // Inyección de datos al modelo Thymeleaf
        model.addAttribute("pendentsLliurament", pendentsLliurament);
        model.addAttribute("pendentsRetorn", pendentsRetorn);
        model.addAttribute("vehiclesActius", vehiclesActius);
        model.addAttribute("clientsTotals", clientsTotals);
        model.addAttribute("propersLliuraments", reservesLogic.llistarPropersLliuraments());

        return "dashboard/dashboard"; // Renderiza templates/dashboard/dashboard.html
    }

    // ============================================================
    // ENDPOINT API PARA TIEMPO REAL (GET /dashboard/api/stats)
    // ============================================================
    /**
     * Endpoint API per actualitzar les estadístiques del Dashboard en temps
     * real: Recopila els KPIs actualitzats i transforma els propers lliuraments
     * en una estructura JSON lleugera amb dates formatades. Retorna el mapa de
     * dades en format JSON perquè el JavaScript de la vista (Polling)
     * l'actualitzi sense recarregar la pàgina.
     */
    @GetMapping("/dashboard/api/stats")
    @ResponseBody
    public Map<String, Object> obtenirEstadistiquesJSON() {
        Map<String, Object> response = new HashMap<>();

        // 1. Recopilación de KPIs en vivo desde la base de datos
        response.put("pendentsLliurament", reservesLogic.comptarPendentsLliurament());
        response.put("pendentsRetorn", reservesLogic.comptarPendentsRetorn());
        response.put("vehiclesActius", reservesLogic.vehiclesActius().size());
        response.put("clientsTotals", clientLogic.llistar(null, null, null, null, null).size());

        // 2. Mapeo simplificado de las reservas a estructuras JSON livianas
        var propers = reservesLogic.llistarPropersLliuraments().stream().map(r -> {
            Map<String, Object> item = new HashMap<>();
            item.put("codiReserva", r.getCodiReserva());
            item.put("clientNom", r.getClient() != null ? r.getClient().getNom() : "Sense Client");
            item.put("vehicleNom", r.getVehicle() != null ? (r.getVehicle().getMarca() + " " + r.getVehicle().getModel()) : "Sense Vehicle");
            item.put("vehicleMatricula", r.getVehicle() != null ? r.getVehicle().getMatricula() : "---");

            // Formateo manual de fecha para enviar un string limpio al cliente JS
            String dataStr = "---";
            String dataIso = "";
            if (r.getDataInici() != null) {
                dataStr = String.format("%02d/%02d/%d",
                        r.getDataInici().getDayOfMonth(),
                        r.getDataInici().getMonthValue(),
                        r.getDataInici().getYear());
                if (r.getHoraInici() != null) {
                    dataStr += ", " + r.getHoraInici() + "h";
                }
                dataIso = r.getDataInici().toString(); // Format YYYY-MM-DD per filtrar per dates
            }
            item.put("dataIniciFormatada", dataStr);
            item.put("dataIniciISO", dataIso);
            item.put("estat", r.getEstat());
            return item;
        }).toList();

        response.put("propersLliuraments", propers);

        return response; // Se envía como objeto JSON al cliente
    }
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Informes.Service;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import cat.copernic.projecte1Grup3.Informes.Entity.EstatHistoric;
import cat.copernic.projecte1Grup3.Informes.Entity.HistoricReserva;
import cat.copernic.projecte1Grup3.Informes.Entity.InformeReserves;
import cat.copernic.projecte1Grup3.Informes.Entity.InformeReservesFila;
import cat.copernic.projecte1Grup3.Informes.Repository.HistoricReservaRepository;

/**
 * Servei de lògica de negoci per generar l'informe històric de reserves:
 * Consulta la col·lecció històrica de MongoDB, valida els filtres per DNI i
 * ordena els resultats per client i data. Calcula els totals agregats de
 * reserves vigents, l'import econòmic acumulat i el recompte de cancel·lacions.
 *
 * @author Grup3
 */
@Service
public class InformeReservesService {

    private static final Logger log = LoggerFactory.getLogger(InformeReservesService.class);

    /**
     * Per client i, dins de cada client, la reserva més recent primer.
     */
    private static final Sort ORDRE = Sort.by(
            Sort.Order.asc("clientNom"),
            Sort.Order.asc("clientDni"),
            Sort.Order.desc("dataReserva"));

    private static final Pattern FORMAT_DNI = Pattern.compile("^[A-Za-z0-9]{5,15}$");

    /**
     * Estats que no compten als totals.
     */
    private static final EnumSet<EstatHistoric> NO_COMPTABLES
            = EnumSet.of(EstatHistoric.CANCELADA, EstatHistoric.ELIMINADA);

    private final HistoricReservaRepository repo;

    /**
     * Injecció del repositori de l'històric de reserves: Inicialitza el servei
     * rebent HistoricReservaRepository per consultar els documents històrics
     * desats a MongoDB.
     */
    public InformeReservesService(HistoricReservaRepository repo) {
        this.repo = repo;
    }

    /**
     * Genera l'informe complet de reserves a partir de l'històric: Valida la
     * llista de DNIs rebuts, recupera els registres de MongoDB ordenats i els
     * transforma en files formatades. Calcula el recompte de reserves vigents,
     * la suma total dels imports (excloent anul·lades/eliminades) i el total de
     * cancel·lades.
     */
    public InformeReserves generarInforme(List<String> dnis) throws InformeException {
        List<String> filtre = validarDnis(dnis);

        List<HistoricReserva> historic;
        try {
            historic = filtre.isEmpty()
                    ? repo.findAll(ORDRE)
                    : repo.findByClientDniIn(filtre, ORDRE);
        } catch (DataAccessException e) {
            log.error("Error consultant l'històric de reserves", e);
            throw new InformeException(
                    "No s'ha pogut consultar l'històric de reserves. Torneu-ho a provar més tard.");
        }

        List<InformeReservesFila> files = historic.stream().map(this::aFila).toList();

        List<HistoricReserva> vigents = historic.stream()
                .filter(h -> !NO_COMPTABLES.contains(h.getEstat()))
                .toList();

        BigDecimal preuTotal = vigents.stream()
                .map(h -> h.getImportTotal() == null ? BigDecimal.ZERO : h.getImportTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalAnullades = (int) historic.stream()
                .filter(h -> h.getEstat() == EstatHistoric.CANCELADA)
                .count();

        return new InformeReserves(files, vigents.size(), preuTotal, totalAnullades);
    }

    /**
     * Neteja i valida el format dels DNIs rebuts com a filtre: Elimina valors
     * buits o duplicats, normalitza el text en majúscules i comprova que
     * compleixin l'expressió regular. Llença una IllegalArgumentException si
     * detecta algun identificador amb format invàlid.
     */
    private List<String> validarDnis(List<String> dnis) {
        if (dnis == null) {
            return List.of();
        }
        List<String> nets = dnis.stream()
                .filter(d -> d != null && !d.isBlank())
                .map(d -> d.trim().toUpperCase())
                .distinct()
                .toList();
        for (String dni : nets) {
            if (!FORMAT_DNI.matcher(dni).matches()) {
                throw new IllegalArgumentException("El DNI '" + dni + "' no té un format vàlid.");
            }
        }
        return nets;
    }

    /**
     * Converteix un document HistoricReserva en una fila per a l'informe:
     * Concatena les dades del vehicle (matrícula, marca i model) i del client
     * (nom i DNI) en cadenes llegibles. Retorna un registre immutable
     * InformeReservesFila llest per renderitzar a la taula de la vista.
     */
    private InformeReservesFila aFila(HistoricReserva h) {
        return new InformeReservesFila(
                h.getDataReserva(),
                h.getVehicleMatricula() + " - " + h.getVehicleMarca() + " " + h.getVehicleModel(),
                h.getClientNom() + " (" + h.getClientDni() + ")",
                h.getImportTotal(),
                h.getEstat());
    }
}

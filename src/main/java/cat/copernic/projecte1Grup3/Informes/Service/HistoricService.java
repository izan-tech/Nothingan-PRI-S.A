/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Informes.Service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cat.copernic.projecte1Grup3.Informes.Entity.EstatHistoric;
import cat.copernic.projecte1Grup3.Informes.Entity.HistoricReserva;
import cat.copernic.projecte1Grup3.Informes.Repository.HistoricReservaRepository;
import cat.copernic.projecte1Grup3.Reserves.Entity.ReservesEntity;

/**
 * Servei encarregat de sincronitzar i desar l'històric de reserves a MongoDB:
 * S'invoca des de ReservesService cada vegada que es crea, modifica, anul·la o
 * finalitza una reserva. Mapeja les dades de l'entitat relacional cap a un
 * document desnormalitzat per conservar la traçabilitat.
 *
 * @author Grup3
 */
@Service
public class HistoricService {

    private static final Logger log = LoggerFactory.getLogger(HistoricService.class);

    private final HistoricReservaRepository repo;

    /**
     * Injecció del repositori de l'històric de reserves: Inicialitza el servei
     * rebent la instància de HistoricReservaRepository per persistir els
     * documents a MongoDB.
     */
    public HistoricService(HistoricReservaRepository repo) {
        this.repo = repo;
    }

    /**
     * Crea o actualitza el registre històric d'una reserva a MongoDB: Cerca si
     * ja existeix el document pel codi de reserva o en crea un de nou, copiant
     * les dades del client, vehicle, dates i import. Actualitza el nou estat i
     * la data de l'últim esdeveniment abans de desar-lo i deixar traça al log.
     */
    public void desar(ReservesEntity r, EstatHistoric estat) {
        HistoricReserva h = repo.findById(r.getCodiReserva()).orElseGet(HistoricReserva::new);

        h.setIdReserva(r.getCodiReserva());
        if (h.getDataReserva() == null) {
            h.setDataReserva(LocalDateTime.now());
        }
        h.setDataInici(r.getDataInici());
        h.setDataFi(r.getDataFi());
        h.setClientDni(r.getClient().getDni());
        h.setClientNom(r.getClient().getNom());
        h.setVehicleMatricula(r.getVehicle().getMatricula());
        h.setVehicleMarca(r.getVehicle().getMarca());
        h.setVehicleModel(r.getVehicle().getModel());
        h.setImportTotal(r.getCostTotal());
        h.setEstat(estat); // EstatHistoric, no String
        h.setDataUltimEvent(LocalDateTime.now());

        repo.save(h);
        log.info("Històric: reserva {} desada amb estat {}", r.getCodiReserva(), estat);
    }
}

package cat.copernic.projecte1Grup3.Reserves.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.projecte1Grup3.Reserves.Entity.ReservesEntity;
import cat.copernic.projecte1Grup3.Reserves.Repository.ReservesRepository;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.ClientEntity;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.ClientRepository;

@Service
public class ReservesClientService {

    private final ReservesRepository reservesRepository;
    private final ClientRepository clientRepository;
    private final ReservesService reservesService;
    private final ReservaAnulacioEmailService reservaAnulacioEmailService;

    public ReservesClientService(ReservesRepository reservesRepository,
            ClientRepository clientRepository,
            ReservesService reservesService,
            ReservaAnulacioEmailService reservaAnulacioEmailService) {
        this.reservesRepository = reservesRepository;
        this.clientRepository = clientRepository;
        this.reservesService = reservesService;
        this.reservaAnulacioEmailService = reservaAnulacioEmailService;
    }

    public record ResultatAnulacio(
            ReservesEntity reserva,
            ReservaAnulacioEmailService.DevolucioClient devolucio) {
    }

    // El login pot ser el DNI o l'email: provem tots dos
    public Optional<ClientEntity> obtenirClient(String identificador) {
        if (identificador == null || identificador.isBlank()) {
            return Optional.empty();
        }
        String id = identificador.trim();
        Optional<ClientEntity> client = clientRepository.findById(id.toUpperCase());
        if (client.isEmpty()) {
            client = clientRepository.findByEmailIgnoreCase(id);
        }
        return client;
    }

    // Només les reserves del client loguejat
    public List<ReservesEntity> llistarPerClient(String identificador) {
        return obtenirClient(identificador)
                .map(c -> reservesRepository.findByClientDniIn(List.of(c.getDni()))
                        .stream()
                        .sorted(Comparator.comparing(ReservesEntity::getCodiReserva).reversed())
                        .toList())
                .orElse(List.of());
    }

    // Filtrar les reserves del client per codi i/o rang de dates (G3P1-245)
    public List<ReservesEntity> filtrarPerClient(String identificador, String codi, LocalDate dataInici, LocalDate dataFi) {
        String codiNet = (codi != null) ? codi.replace("#", "").trim() : "";

        return llistarPerClient(identificador).stream()
                .filter(r -> {
                    if (!codiNet.isEmpty()) {
                        String codiStr = String.valueOf(r.getCodiReserva());
                        if (!codiStr.contains(codiNet)) {
                            return false;
                        }
                    }
                    if (dataInici != null && r.getDataInici() != null) {
                        if (r.getDataInici().isBefore(dataInici)) {
                            return false;
                        }
                    }
                    if (dataFi != null && r.getDataFi() != null) {
                        if (r.getDataFi().isAfter(dataFi)) {
                            return false;
                        }
                    }
                    return true;
                })
                .toList();
    }

    // Una reserva concreta, només si pertany al client loguejat
    public Optional<ReservesEntity> obtenirReservaDelClient(Long codi, String identificador) {
        return obtenirClient(identificador).flatMap(c ->
                reservesRepository.findById(codi)
                        .filter(r -> r.getClient() != null
                                && c.getDni().equals(r.getClient().getDni())));
    }

    @Transactional
    public ResultatAnulacio anularReservaDelClient(Long codi, String identificador) {
        ReservesEntity reserva = obtenirReservaDelClient(codi, identificador)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No s'ha trobat aquesta reserva o no tens permís per anul·lar-la."));

        ReservaAnulacioEmailService.DevolucioClient devolucio
                = reservaAnulacioEmailService.calcularDevolucioClient(
                reserva, LocalDateTime.now());
        ReservesEntity reservaAnulada = reservesService.anularReserva(
                codi, "Cancel·lada pel client");

        return new ResultatAnulacio(reservaAnulada, devolucio);
    }
}
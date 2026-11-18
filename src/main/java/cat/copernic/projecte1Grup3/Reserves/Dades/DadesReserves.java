package cat.copernic.projecte1Grup3.Reserves.Dades;

import cat.copernic.projecte1Grup3.Informes.Entity.EstatHistoric;
import cat.copernic.projecte1Grup3.Informes.Repository.HistoricReservaRepository;
import cat.copernic.projecte1Grup3.Informes.Service.HistoricService;
import cat.copernic.projecte1Grup3.Reserves.Entity.ReservesEntity;
import cat.copernic.projecte1Grup3.Reserves.Repository.ReservesRepository;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.ClientEntity;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.ClientRepository;
import cat.copernic.projecte1Grup3.Vehicles.Entity.VehiclesEntity;
import cat.copernic.projecte1Grup3.Vehicles.Repository.VehiclesRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
public class DadesReserves {

    private final ReservesRepository reservesRepo;
    private final ClientRepository clientRepo;
    private final VehiclesRepository vehicleRepo;
    private final HistoricService historicService;
    private final HistoricReservaRepository historicRepo;

    public DadesReserves(ReservesRepository reservesRepo,
            ClientRepository clientRepo,
            VehiclesRepository vehicleRepo,
            HistoricService historicService,
            HistoricReservaRepository historicRepo) {
        this.reservesRepo = reservesRepo;
        this.clientRepo = clientRepo;
        this.vehicleRepo = vehicleRepo;
        this.historicService = historicService;
        this.historicRepo = historicRepo;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void carregarReserves() {
        try {
            if (reservesRepo.count() < 20) {
                LocalDate avui = LocalDate.now();

                // 1. Joan Garcia Soler (2 reserves)
                crea("12345678Z", "1234ABC", avui.minusDays(30), avui.minusDays(27),
                        LocalTime.of(9, 0), LocalTime.of(18, 0), "37.50", "300.00", "FINALITZADA", "Client habitual");
                crea("12345678Z", "4821MNB", avui.minusDays(15), avui.minusDays(12),
                        LocalTime.of(10, 0), LocalTime.of(19, 0), "58.50", "450.00", "FINALITZADA", "Segona reserva del client");

                // 2. Marta Puigdomènech Vila (2 reserves)
                crea("33918274J", "5678DEF", avui.minusDays(25), avui.minusDays(20),
                        LocalTime.of(10, 0), LocalTime.of(19, 0), "90.00", "500.00", "FINALITZADA", "Retornat net");
                crea("33918274J", "3456JKL", avui.plusDays(1), avui.plusDays(4),
                        LocalTime.of(9, 0), LocalTime.of(20, 0), "105.00", "1000.00", "CONFIRMADA", "Viatge de negocis");

                // 3. Lucas Silva Mendes
                crea("X5482910D", "3456JKL", avui.minusDays(20), avui.minusDays(18),
                        LocalTime.of(8, 30), LocalTime.of(20, 0), "70.00", "1000.00", "FINALITZADA", "Sense incidències");

                // 4. Anna Ribas Coll (2 reserves)
                crea("44192837Q", "4821MNB", avui.minusDays(22), avui.minusDays(18),
                        LocalTime.of(9, 0), LocalTime.of(18, 0), "78.00", "450.00", "FINALITZADA", null);
                crea("44192837Q", "9182LMN", avui.plusDays(5), avui.plusDays(8),
                        LocalTime.of(10, 0), LocalTime.of(19, 0), "78.00", "700.00", "PENDENT", null);

                // 5. David Serrano López
                crea("47829103M", "7394KLP", avui.minusDays(18), avui.minusDays(15),
                        LocalTime.of(8, 0), LocalTime.of(17, 0), "39.00", "300.00", "FINALITZADA", null);

                // 6. Sofia Rossi Conti
                crea("Y3829104D", "1958RST", avui.minusDays(16), avui.minusDays(12),
                        LocalTime.of(10, 0), LocalTime.of(18, 0), "48.00", "300.00", "FINALITZADA", "Lliurament puntual");

                // 7. Elena Navarro Ortiz (2 reserves)
                crea("51283940N", "8519CDF", avui.minusDays(14), avui.minusDays(11),
                        LocalTime.of(9, 0), LocalTime.of(20, 0), "67.50", "550.00", "FINALITZADA", "Client prèmium");
                crea("51283940N", "2673PRV", avui.minusDays(2), avui.plusDays(2),
                        LocalTime.of(9, 0), LocalTime.of(18, 0), "110.00", "750.00", "EN_CURS", "Vehicle lliurat en termini");

                // 8. Mateo Rojas Herrera
                crea("Z4829103M", "3047HJK", avui.minusDays(12), avui.minusDays(8),
                        LocalTime.of(8, 0), LocalTime.of(18, 0), "116.00", "850.00", "FINALITZADA", "Transport comercial");

                // 9. Cristina Vilaró Camps
                crea("42918374Y", "9182LMN", avui.minusDays(10), avui.minusDays(7),
                        LocalTime.of(10, 0), LocalTime.of(19, 0), "78.00", "700.00", "FINALITZADA", null);

                // 10. Claire Dubois Laurent
                crea("X6192837H", "2673PRV", avui.minusDays(9), avui.minusDays(5),
                        LocalTime.of(9, 0), LocalTime.of(18, 0), "110.00", "750.00", "FINALITZADA", null);

                // 11. Roger Esteve Prat
                crea("35829104A", "5490STV", avui.minusDays(8), avui.minusDays(5),
                        LocalTime.of(8, 30), LocalTime.of(17, 30), "60.00", "500.00", "FINALITZADA", "Sense incidències");

                // 12. Nerea Molina Sánchez (2 reserves)
                crea("53192847B", "7812WXZ", avui.minusDays(7), avui.minusDays(3),
                        LocalTime.of(9, 0), LocalTime.of(18, 0), "42.00", "250.00", "FINALITZADA", null);
                crea("53192847B", "1465BCD", avui.minusDays(1), avui.plusDays(2),
                        LocalTime.of(10, 0), LocalTime.of(20, 0), "96.00", "900.00", "EN_CURS", "Cap de setmana");

                // 13. Andrei Popescu Ionescu
                crea("Y7392018Q", "4127JKL", avui, avui.plusDays(4),
                        LocalTime.of(11, 0), LocalTime.of(19, 0), "84.00", "550.00", "CONFIRMADA", null);

                // 14. Gemma Solsona Rovira
                crea("46829103E", "6753MNP", avui, avui.plusDays(3),
                        LocalTime.of(12, 0), LocalTime.of(18, 0), "72.00", "700.00", "CONFIRMADA", "Mudança a Terrassa");

                // 15. Òscar Ferrer Busquets (Cancel·lada -> comptarà a totalAnullades de l'informe)
                crea("39817264D", "2389RTV", avui.plusDays(2), avui.plusDays(6),
                        LocalTime.of(10, 0), LocalTime.of(18, 0), "74.00", "450.00", "CANCELADA", "Cancel·lada pel client");

                // 16. Xavier Llorens Roca
                crea("52193847Q", "9541VWX", avui.plusDays(3), avui.plusDays(6),
                        LocalTime.of(9, 30), LocalTime.of(17, 30), "42.00", "350.00", "PENDENT", null);

                // 17. Sílvia Domènech Gil
                crea("41928375L", "1234ABC", avui.plusDays(7), avui.plusDays(10),
                        LocalTime.of(9, 0), LocalTime.of(18, 0), "37.50", "300.00", "PENDENT", null);
            }

            // Assegura que totes les reserves de MySQL estiguin sincronitzades a MongoDB
            if (historicRepo.count() < reservesRepo.count()) {
                for (ReservesEntity r : reservesRepo.findAll()) {
                    if (r.getClient() != null && r.getVehicle() != null) {
                        historicService.desar(r, parseEstatHistoric(r.getEstat()));
                    }
                }
            }

            System.out.println(">>> RESERVES ACTIVES A MYSQL: " + reservesRepo.count() + " | HISTORIC A MONGO: " + historicRepo.count());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void crea(String dni, String matricula,
            LocalDate dataInici, LocalDate dataFi,
            LocalTime horaInici, LocalTime horaFi,
            String costTotal, String fianca, String estat, String observacions) {

        ClientEntity client = clientRepo.findById(dni).orElse(null);
        VehiclesEntity vehicle = vehicleRepo.findById(matricula).orElse(null);
        if (client == null || vehicle == null) {
            return;
        }

        ReservesEntity r = new ReservesEntity();
        r.setClient(client);
        r.setVehicle(vehicle);
        r.setDataInici(dataInici);
        r.setDataFi(dataFi);
        r.setHoraInici(horaInici);
        r.setHoraFi(horaFi);
        r.setCostTotal(new BigDecimal(costTotal));
        r.setFianca(new BigDecimal(fianca));
        r.setEstat(estat);
        r.setObservacions(observacions);

        if ("EN_CURS".equals(estat) || "FINALITZADA".equals(estat)) {
            r.setDataLliurament(dataInici);
            r.setHoraLliurament(horaInici);
        }
        if ("FINALITZADA".equals(estat)) {
            r.setDataRetorn(dataFi);
        }

        ReservesEntity guardada = reservesRepo.save(r);
        historicService.desar(guardada, parseEstatHistoric(estat));
    }

    private EstatHistoric parseEstatHistoric(String estat) {
        try {
            return EstatHistoric.valueOf(estat);
        } catch (Exception e) {
            return EstatHistoric.PENDENT;
        }
    }
}

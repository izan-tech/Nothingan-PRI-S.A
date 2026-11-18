package cat.copernic.projecte1Grup3.Reserves.Service;

import cat.copernic.projecte1Grup3.Informes.Entity.EstatHistoric;
import cat.copernic.projecte1Grup3.Informes.Service.HistoricService;
import cat.copernic.projecte1Grup3.Reserves.Entity.ReservesEntity;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.ClientEntity;
import cat.copernic.projecte1Grup3.Vehicles.Entity.VehiclesEntity;
import cat.copernic.projecte1Grup3.Vehicles.Repository.VehiclesRepository;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import cat.copernic.projecte1Grup3.Reserves.Repository.ReservesRepository;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.ClientRepository;

@Service
public class ReservesService {

    private static final Logger log = LoggerFactory.getLogger(ReservesService.class);

    // Descompte premium: 25% sobre la fiança
    private static final BigDecimal DESCOMPTE_PREMIUM = new BigDecimal("0.25");

    // Percentatge fix del cost del lloguer que es retorna en anul·lar (criteri de l'empresa)
    private static final BigDecimal PERCENTATGE_RETORN_LLOGUER = new BigDecimal("25");

    public static final String PENDENT = "PENDENT";
    public static final String CONFIRMADA = "CONFIRMADA";
    public static final String EN_CURS = "EN_CURS";
    public static final String FINALITZADA = "FINALITZADA";
    public static final String CANCELADA = "CANCELADA";

    private final ReservesRepository reservesRepository;
    private final ClientRepository clientRepository;
    private final VehiclesRepository vehiclesRepository;
    private final HistoricService historicService;

    public ReservesService(ReservesRepository reservesRepository,
            ClientRepository clientRepository,
            VehiclesRepository vehiclesRepository,
            HistoricService historicService) {
        this.reservesRepository = reservesRepository;
        this.clientRepository = clientRepository;
        this.vehiclesRepository = vehiclesRepository;
        this.historicService = historicService;
    }

    public List<VehiclesEntity> vehiclesActius() {
        return vehiclesRepository.findAll().stream()
                .filter(v -> Boolean.TRUE.equals(v.getActiu()))
                .toList();
    }

    public List<ReservesEntity> llistar(String q, String dni, String email,
            LocalDate dataReserva, String matricula,
            String codi, String estat) {
        List<ReservesEntity> reserves = reservesRepository.findAll();

        if (q != null && !q.isBlank()) {
            String text = q.trim().toLowerCase();
            reserves = reserves.stream()
                    .filter(r -> conte(String.valueOf(r.getCodiReserva()), text)
                    || (r.getClient() != null
                    && (conte(r.getClient().getNom(), text) || conte(r.getClient().getDni(), text)))
                    || (r.getVehicle() != null
                    && (conte(r.getVehicle().getMatricula(), text)
                    || conte(r.getVehicle().getMarca(), text)
                    || conte(r.getVehicle().getModel(), text))))
                    .toList();
        }

        if (dni != null && !dni.isBlank()) {
            String d = dni.trim().toLowerCase();
            reserves = reserves.stream()
                    .filter(r -> r.getClient() != null && conte(r.getClient().getDni(), d))
                    .toList();
        }

        if (email != null && !email.isBlank()) {
            String e = email.trim().toLowerCase();
            reserves = reserves.stream()
                    .filter(r -> r.getClient() != null && conte(r.getClient().getEmail(), e))
                    .toList();
        }

        if (dataReserva != null) {
            reserves = reserves.stream()
                    .filter(r -> r.getDataInici() != null && r.getDataFi() != null
                    && !dataReserva.isBefore(r.getDataInici())
                    && !dataReserva.isAfter(r.getDataFi()))
                    .toList();
        }

        if (matricula != null && !matricula.isBlank()) {
            String m = matricula.trim().toLowerCase();
            reserves = reserves.stream()
                    .filter(r -> r.getVehicle() != null && conte(r.getVehicle().getMatricula(), m))
                    .toList();
        }

        if (codi != null && !codi.isBlank()) {
            String c = codi.trim();
            reserves = reserves.stream()
                    .filter(r -> String.valueOf(r.getCodiReserva()).equals(c))
                    .toList();
        }

        if (estat != null && !estat.isBlank()) {
            String e = estat.trim();
            reserves = reserves.stream()
                    .filter(r -> e.equalsIgnoreCase(r.getEstat()))
                    .toList();
        }

        return reserves.stream()
                .sorted(Comparator.comparing(ReservesEntity::getCodiReserva).reversed())
                .toList();
    }

    private boolean conte(String valor, String text) {
        return valor != null && valor.toLowerCase().contains(text);
    }

    private boolean esPremium(ClientEntity client) {
        return Boolean.TRUE.equals(client.isPremium());
    }

    public Optional<ReservesEntity> obtenir(Long codi) {
        return reservesRepository.findById(codi);
    }

    @Transactional
    public ReservesEntity crear(String dni, String matricula,
            LocalDate dataInici, LocalTime horaInici,
            LocalDate dataFi, LocalTime horaFi,
            String observacions) {

        if (dni == null || dni.isBlank()) {
            throw new IllegalArgumentException("Cal indicar el client");
        }
        if (matricula == null || matricula.isBlank()) {
            throw new IllegalArgumentException("Cal triar un vehicle");
        }
        if (dataInici == null || horaInici == null || dataFi == null || horaFi == null) {
            throw new IllegalArgumentException("Cal indicar les dates i hores d'inici i fi");
        }

        ClientEntity client = clientRepository.findById(dni.trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Client no trobat"));
        if (!"VALIDAT".equals(client.getEstatValidacio())) {
            throw new IllegalArgumentException(
                    "El client ha d'estar validat per poder fer una reserva");
        }

        VehiclesEntity vehicle = vehiclesRepository.findById(matricula)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle no trobat"));
        if (!Boolean.TRUE.equals(vehicle.getActiu())) {
            throw new IllegalArgumentException("Aquest vehicle no està disponible per a reserves");
        }

        LocalDateTime inici = LocalDateTime.of(dataInici, horaInici);
        LocalDateTime fi = LocalDateTime.of(dataFi, horaFi);
        if (inici.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("L'inici de la reserva no pot ser en el passat");
        }
        if (!fi.isAfter(inici)) {
            throw new IllegalArgumentException("La data i hora de fi han de ser posteriors a les d'inici");
        }

        long minuts = Duration.between(inici, fi).toMinutes();
        long hores = (minuts + 59) / 60;
        long dies = (hores + 23) / 24;
        if (dies < vehicle.getMinDiesLloguer()) {
            throw new IllegalArgumentException(
                    "La durada mínima de lloguer és de " + vehicle.getMinDiesLloguer() + " dia(es)");
        }
        if (dies > vehicle.getMaxDiesLloguer()) {
            throw new IllegalArgumentException(
                    "La durada màxima de lloguer és de " + vehicle.getMaxDiesLloguer() + " dia(es)");
        }

        List<ReservesEntity> existents
                = reservesRepository.findByVehicleMatriculaAndEstatNot(matricula, CANCELADA);
        for (ReservesEntity r : existents) {
            LocalDateTime iniciR = LocalDateTime.of(r.getDataInici(), r.getHoraInici());
            LocalDateTime fiR = LocalDateTime.of(r.getDataFi(), r.getHoraFi());
            boolean esSolapa = iniciR.isBefore(fi) && fiR.isAfter(inici);
            if (esSolapa) {
                throw new IllegalArgumentException(
                        "El vehicle ja està reservat en aquest període");
            }
        }

        ReservesEntity reserva = new ReservesEntity();
        reserva.setClient(client);
        reserva.setVehicle(vehicle);
        reserva.setDataInici(dataInici);
        reserva.setHoraInici(horaInici);
        reserva.setDataFi(dataFi);
        reserva.setHoraFi(horaFi);

        BigDecimal cost = vehicle.getPreuHora().multiply(BigDecimal.valueOf(hores));
        reserva.setCostTotal(cost.setScale(2, RoundingMode.HALF_UP));

        BigDecimal fianca = vehicle.getFianca();
        if (esPremium(client)) {
            fianca = fianca.multiply(BigDecimal.ONE.subtract(DESCOMPTE_PREMIUM));
            log.info("Descompte premium (25%) aplicat a la fiança del client {}: {}€ -> {}€",
                    client.getDni(), vehicle.getFianca(), fianca.setScale(2, RoundingMode.HALF_UP));
        }
        reserva.setFianca(fianca.setScale(2, RoundingMode.HALF_UP));

        reserva.setEstat(PENDENT);
        reserva.setObservacions(observacions == null || observacions.isBlank()
                ? null : observacions.trim());

        ReservesEntity guardada = reservesRepository.save(reserva);
        historicService.desar(guardada, EstatHistoric.PENDENT);
        log.info("Reserva #{} creada: client {} i vehicle {} ({} -> {})",
                guardada.getCodiReserva(), client.getDni(), matricula, inici, fi);
        return guardada;
    }

    @Transactional
    public void eliminar(Long codi) {
        ReservesEntity r = reservesRepository.findById(codi)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no trobada"));

        if (EN_CURS.equals(r.getEstat()) || FINALITZADA.equals(r.getEstat())) {
            throw new IllegalArgumentException(
                    "No es pot eliminar una reserva en curs o finalitzada");
        }
        reservesRepository.delete(r);
        log.warn("Reserva #{} eliminada (estat: {})", codi, r.getEstat());
    }

    // Emmagatzematge físic a: uploads/vehicles/(MATRICULA_MARCA_MODEL)/(lliurar|retornar)/
    public String guardarFotos(ReservesEntity reserva, MultipartFile[] fotos, String accio) {
        if (fotos == null || fotos.length == 0) {
            return "";
        }
        StringBuilder rutes = new StringBuilder();

        String carpetaVehicle = obtenirCarpetaVehicle(reserva);
        Path uploadPath = Paths.get("uploads", "vehicles", carpetaVehicle, accio).toAbsolutePath();
        File dir = uploadPath.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String[] costats = {"frontal", "posterior", "lateral_esquerre", "lateral_dret"};

        for (int i = 0; i < fotos.length; i++) {
            MultipartFile foto = fotos[i];
            if (foto != null && !foto.isEmpty()) {
                try {
                    String originalFilename = foto.getOriginalFilename();
                    String ext = (originalFilename != null && originalFilename.contains("."))
                            ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";

                    String costat = (i < costats.length) ? costats[i] : "foto_" + (i + 1);
                    String filename = accio + "_" + costat + "_" + System.currentTimeMillis() + "_" + i + ext;

                    Path filePath = uploadPath.resolve(filename);
                    Files.copy(foto.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                    rutes.append("/uploads/vehicles/")
                            .append(carpetaVehicle).append("/")
                            .append(accio).append("/")
                            .append(filename).append(" ");
                } catch (IOException e) {
                    log.error("Error guardant la foto {} de la reserva #{}",
                            i + 1, reserva != null ? reserva.getCodiReserva() : null, e);
                }
            }
        }
        return rutes.toString().trim();
    }

    public String guardarFotosAddicionals(ReservesEntity reserva, MultipartFile[] fotos,
            String[] titols, String[] descripcions, String accio) {
        if (fotos == null || fotos.length == 0) {
            return "";
        }
        StringBuilder registre = new StringBuilder();

        String carpetaVehicle = obtenirCarpetaVehicle(reserva);
        Path uploadPath = Paths.get("uploads", "vehicles", carpetaVehicle, accio).toAbsolutePath();
        File dir = uploadPath.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        int numFoto = 1;
        for (int i = 0; i < fotos.length; i++) {
            MultipartFile foto = fotos[i];
            if (foto != null && !foto.isEmpty()) {
                try {
                    String originalFilename = foto.getOriginalFilename();
                    String ext = (originalFilename != null && originalFilename.contains("."))
                            ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";

                    String filename = accio + "_extra_" + System.currentTimeMillis() + "_" + i + ext;
                    Path filePath = uploadPath.resolve(filename);
                    Files.copy(foto.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                    String url = "/uploads/vehicles/" + carpetaVehicle + "/" + accio + "/" + filename;
                    String titol = (titols != null && i < titols.length && titols[i] != null && !titols[i].isBlank())
                            ? titols[i].trim().replace("|", "-").replace("[", "(").replace("]", ")")
                            : "Foto addicional #" + numFoto;
                    String desc = (descripcions != null && i < descripcions.length && descripcions[i] != null && !descripcions[i].isBlank())
                            ? descripcions[i].trim().replace("|", "-").replace("[", "(").replace("]", ")")
                            : "";

                    registre.append("[FotoExtra|")
                            .append(url).append("|")
                            .append(titol).append("|")
                            .append(desc).append("] ");
                    numFoto++;
                } catch (IOException e) {
                    log.error("Error guardant la foto addicional {} de la reserva #{}",
                            i + 1, reserva != null ? reserva.getCodiReserva() : null, e);
                }
            }
        }
        return registre.toString().trim();
    }

    private String obtenirCarpetaVehicle(ReservesEntity reserva) {
        if (reserva != null && reserva.getVehicle() != null) {
            String matricula = (reserva.getVehicle().getMatricula() != null)
                    ? reserva.getVehicle().getMatricula() : "S-MATRICULA";
            String marcaModel = (reserva.getVehicle().getMarca() + "_" + reserva.getVehicle().getModel())
                    .replaceAll("[^a-zA-Z0-9_-]", "_");
            return matricula + "_" + marcaModel;
        }
        return "vehicle_desconegut";
    }

    // Compatibilitat (7 paràmetres)
    @Transactional
    public ReservesEntity lliurar(Long codi, LocalDate dataLliurament, LocalTime horaLliurament,
            Integer kmInicials, String nivellCombustible, String novesObservacions,
            MultipartFile[] fotos) {
        return lliurar(codi, dataLliurament, horaLliurament, kmInicials, nivellCombustible,
                novesObservacions, fotos, null, null, null);
    }

    // Lliurar amb fotos addicionals il·limitades (títol i descripció)
    @Transactional
    public ReservesEntity lliurar(Long codi, LocalDate dataLliurament, LocalTime horaLliurament,
            Integer kmInicials, String nivellCombustible, String novesObservacions,
            MultipartFile[] fotos,
            MultipartFile[] fotosAddicionals, String[] titolsFotosAddicionals,
            String[] descripcionsFotosAddicionals) {
        ReservesEntity r = reservesRepository.findById(codi)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no trobada: #" + codi));

        if (EN_CURS.equals(r.getEstat()) || FINALITZADA.equals(r.getEstat()) || CANCELADA.equals(r.getEstat())) {
            throw new IllegalArgumentException("No es pot lliurar una reserva en estat " + r.getEstat());
        }

        r.setDataLliurament(dataLliurament != null ? dataLliurament : LocalDate.now());
        r.setHoraLliurament(horaLliurament != null ? horaLliurament : LocalTime.now());
        r.setEstat(EN_CURS);

        String rutesFotos = guardarFotos(r, fotos, "lliurar");
        String registreFotosAddicionals = guardarFotosAddicionals(
                r, fotosAddicionals, titolsFotosAddicionals, descripcionsFotosAddicionals, "lliurar");

        StringBuilder obs = new StringBuilder();
        if (r.getObservacions() != null && !r.getObservacions().isBlank()) {
            obs.append(r.getObservacions()).append("\n");
        }
        obs.append("[Lliurament ").append(r.getDataLliurament()).append(" ").append(r.getHoraLliurament()).append("]: ");
        if (kmInicials != null) {
            obs.append("Km inicials: ").append(kmInicials).append(" km. ");
        }
        if (nivellCombustible != null && !nivellCombustible.isBlank()) {
            obs.append("Combustible: ").append(nivellCombustible).append(". ");
        }
        if (!rutesFotos.isBlank()) {
            obs.append("Fotos lliurament: ").append(rutesFotos).append(". ");
        }
        if (!registreFotosAddicionals.isBlank()) {
            obs.append(registreFotosAddicionals).append(" ");
        }
        if (novesObservacions != null && !novesObservacions.isBlank()) {
            obs.append("Notes: ").append(novesObservacions.trim());
        }

        r.setObservacions(obs.toString());
        ReservesEntity guardada = reservesRepository.save(r);
        log.info("Reserva #{} lliurada: vehicle {} a client {}",
                codi, r.getVehicle().getMatricula(), r.getClient().getDni());
        return guardada;
    }

    @Transactional
    public ReservesEntity retornar(Long codi, LocalDate dataRetorn, LocalTime horaRetorn,
            boolean teDesperfectes, boolean desperfectesImputables,
            BigDecimal importDanys, BigDecimal deduccioCarburant,
            String novesObservacions, MultipartFile[] fotos) {
        return retornar(codi, dataRetorn, horaRetorn, teDesperfectes, desperfectesImputables,
                importDanys, deduccioCarburant, novesObservacions, fotos, null, null, null);
    }

    @Transactional
    public ReservesEntity retornar(Long codi, LocalDate dataRetorn, LocalTime horaRetorn,
            boolean teDesperfectes, boolean desperfectesImputables,
            BigDecimal importDanys, BigDecimal deduccioCarburant,
            String novesObservacions, MultipartFile[] fotos,
            MultipartFile[] fotosAddicionals, String[] titolsFotosAddicionals,
            String[] descripcionsFotosAddicionals) {
        ReservesEntity r = reservesRepository.findById(codi)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no trobada: #" + codi));

        if (!EN_CURS.equals(r.getEstat())) {
            throw new IllegalArgumentException("Només es poden retornar vehicles de reserves EN_CURS.");
        }

        LocalDate dataTornada = (dataRetorn != null) ? dataRetorn : LocalDate.now();
        LocalTime horaTornada = (horaRetorn != null) ? horaRetorn : LocalTime.now();

        r.setDataRetorn(dataTornada);

        LocalDateTime previst = LocalDateTime.of(r.getDataFi(), r.getHoraFi());
        LocalDateTime real = LocalDateTime.of(dataTornada, horaTornada);

        BigDecimal penalitzacioRetard = BigDecimal.ZERO;
        long horesRetard = 0;
        if (real.isAfter(previst)) {
            long minutsRetard = Duration.between(previst, real).toMinutes();
            horesRetard = (minutsRetard + 59) / 60;
            BigDecimal preuHora = (r.getVehicle() != null && r.getVehicle().getPreuHora() != null)
                    ? r.getVehicle().getPreuHora() : BigDecimal.ZERO;
            penalitzacioRetard = preuHora.multiply(BigDecimal.valueOf(horesRetard));
        }

        BigDecimal fiancaOriginal = (r.getFianca() != null) ? r.getFianca() : BigDecimal.ZERO;

        BigDecimal carrecDanys = BigDecimal.ZERO;
        if (teDesperfectes && desperfectesImputables) {
            carrecDanys = fiancaOriginal;
        }

        BigDecimal carrecCombustible = (deduccioCarburant != null) ? deduccioCarburant : BigDecimal.ZERO;

        BigDecimal totalDeduccions = carrecDanys.add(carrecCombustible).add(penalitzacioRetard);
        BigDecimal fiancaARetornar = fiancaOriginal.subtract(totalDeduccions);
        if (fiancaARetornar.compareTo(BigDecimal.ZERO) < 0) {
            fiancaARetornar = BigDecimal.ZERO;
        }

        String rutesFotos = guardarFotos(r, fotos, "retornar");
        String registreFotosAddicionals = guardarFotosAddicionals(
                r, fotosAddicionals, titolsFotosAddicionals, descripcionsFotosAddicionals, "retornar");

        StringBuilder obs = new StringBuilder();
        if (r.getObservacions() != null && !r.getObservacions().isBlank()) {
            obs.append(r.getObservacions()).append("\n");
        }
        obs.append("[Retorn ").append(dataTornada).append(" ").append(horaTornada).append("]: ");

        if (horesRetard > 0) {
            obs.append("Retard: ").append(horesRetard).append("h (-").append(penalitzacioRetard).append("€). ");
            log.warn("Reserva #{} retornada amb {}h de retard (penalització {}€)",
                    codi, horesRetard, penalitzacioRetard);
        }
        if (teDesperfectes) {
            if (desperfectesImputables) {
                obs.append("Danys imputables al client: retenció total de la fiança (-").append(carrecDanys).append("€). ");
            } else {
                obs.append("Danys no imputables (avaria/tercers, 0€ càrrec). ");
            }
            log.warn("Reserva #{} retornada amb desperfectes (imputables: {}, import: {}€)",
                    codi, desperfectesImputables, carrecDanys);
        }
        if (carrecCombustible.compareTo(BigDecimal.ZERO) > 0) {
            obs.append("Carburant no retornat complet (-").append(carrecCombustible).append("€). ");
        }

        if (!rutesFotos.isBlank()) {
            obs.append("Fotos retorn: ").append(rutesFotos).append(". ");
        }
        if (!registreFotosAddicionals.isBlank()) {
            obs.append(registreFotosAddicionals).append(" ");
        }
        obs.append("Resum fiança: ").append(fiancaOriginal).append("€ | Deduccions: ")
                .append(totalDeduccions).append("€ | Retorn final: ").append(fiancaARetornar).append("€. ");

        if (novesObservacions != null && !novesObservacions.isBlank()) {
            obs.append("Descripció: ").append(novesObservacions.trim());
        }

        r.setObservacions(obs.toString());
        r.setEstat(FINALITZADA);

        ReservesEntity guardada = reservesRepository.save(r);
        log.info("Reserva #{} finalitzada. Fiança retornada: {}€", codi, fiancaARetornar);
        return guardada;
    }

    public long comptarPendentsLliurament() {
        return reservesRepository.findAll().stream()
                .filter(r -> PENDENT.equals(r.getEstat()) || CONFIRMADA.equals(r.getEstat()))
                .count();
    }

    public long comptarPendentsRetorn() {
        return reservesRepository.findAll().stream()
                .filter(r -> EN_CURS.equals(r.getEstat()))
                .count();
    }

    public List<ReservesEntity> llistarPropersLliuraments() {
        LocalDate avui = LocalDate.now();
        return reservesRepository.findAll().stream()
                .filter(r -> !CANCELADA.equals(r.getEstat()) && !FINALITZADA.equals(r.getEstat()))
                .filter(r -> EN_CURS.equals(r.getEstat()) || (r.getDataInici() != null && !r.getDataInici().isBefore(avui)))
                .sorted(Comparator.comparing(ReservesEntity::getDataInici)
                        .thenComparing(ReservesEntity::getHoraInici))
                .limit(10)
                .toList();
    }

    public List<ReservesEntity> llistarReservesPerClients(List<String> dnisClients) {
        if (dnisClients == null || dnisClients.isEmpty()) {
            return reservesRepository.findAll();
        }

        List<String> dnisNets = dnisClients.stream()
                .filter(dni -> dni != null && !dni.isBlank())
                .map(String::trim)
                .toList();

        if (dnisNets.isEmpty()) {
            return reservesRepository.findAll();
        }

        return reservesRepository.findByClientDniIn(dnisNets);
    }

    public List<ReservesEntity> buscarPerClient(String dni) {
        if (dni == null || dni.isBlank()) {
            return List.of();
        }
        return reservesRepository.findAll().stream()
                .filter(r -> r.getClient() != null && dni.equalsIgnoreCase(r.getClient().getDni()))
                .sorted(Comparator.comparing(ReservesEntity::getCodiReserva).reversed())
                .toList();
    }

    @Transactional
    public ReservesEntity anularReserva(Long codiReserva, String motiu) {
        return anularReserva(codiReserva, motiu, new BigDecimal("100"));
    }

    @Transactional
    public ReservesEntity anularReserva(Long codiReserva, String motiu, BigDecimal percentatgeFianca) {
        if (motiu == null || motiu.isBlank()) {
            throw new IllegalArgumentException("Cal indicar el motiu de l'anul·lació.");
        }
        if (percentatgeFianca == null || percentatgeFianca.compareTo(BigDecimal.ZERO) < 0
                || percentatgeFianca.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("El percentatge de fiança ha d'estar entre 0 i 100.");
        }

        ReservesEntity r = reservesRepository.findById(codiReserva)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no trobada: #" + codiReserva));

        if (FINALITZADA.equals(r.getEstat()) || EN_CURS.equals(r.getEstat()) || CANCELADA.equals(r.getEstat())) {
            throw new IllegalArgumentException("Aquesta reserva ja no es pot anul·lar.");
        }

        BigDecimal retornLloguer = calcularRetornLloguer(r);
        BigDecimal retornFianca = calcularRetornFianca(r, percentatgeFianca);

        StringBuilder obs = new StringBuilder();
        if (r.getObservacions() != null && !r.getObservacions().isBlank()) {
            obs.append(r.getObservacions()).append("\n");
        }
        obs.append("[Anul·lació ").append(LocalDate.now()).append("]: retorn lloguer 25% (")
                .append(retornLloguer).append("€); retorn fiança ")
                .append(percentatgeFianca.stripTrailingZeros().toPlainString()).append("% (")
                .append(retornFianca).append("€).");

        r.setObservacions(obs.toString());
        r.setEstat(CANCELADA);
        r.setAnulacioMotiu(motiu.trim());
        ReservesEntity guardada = reservesRepository.save(r);
        historicService.desar(guardada, EstatHistoric.CANCELADA);
        log.warn("Reserva #{} anul·lada. Retorn lloguer {}€, fiança {}% = {}€",
                codiReserva, retornLloguer, percentatgeFianca, retornFianca);
        return guardada;
    }

    public BigDecimal calcularRetornLloguer(ReservesEntity r) {
        BigDecimal cost = r.getCostTotal() != null ? r.getCostTotal() : BigDecimal.ZERO;
        return cost.multiply(PERCENTATGE_RETORN_LLOGUER)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularRetornFianca(ReservesEntity r, BigDecimal percentatgeFianca) {
        BigDecimal fianca = r.getFianca() != null ? r.getFianca() : BigDecimal.ZERO;
        return fianca.multiply(percentatgeFianca)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    @Transactional
    public ReservesEntity guardarReservaClient(ReservesEntity reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no pot ser nul·la");
        }

        String dni = (reserva.getClient() != null) ? reserva.getClient().getDni() : null;
        String matricula = (reserva.getVehicle() != null) ? reserva.getVehicle().getMatricula() : null;

        return crear(
                dni,
                matricula,
                reserva.getDataInici(),
                reserva.getHoraInici(),
                reserva.getDataFi(),
                reserva.getHoraFi(),
                reserva.getObservacions()
        );
    }
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Usuaris.Client.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cat.copernic.projecte1Grup3.Usuaris.Client.Documents.ClientDocuments;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.ClientEntity;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.ClientRepository;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.ClientRepositoryMongo;

/**
 * Servei de lògica de negoci per a la gestió integral de clients i els seus documents:
 * Implementa la doble persistència coordinant MySQL (dades personals i estat) amb MongoDB (imatges del DNI i carnet).
 * Inclou les validacions de negoci, xifratge de contrasenyes, verificació de targetes (Luhn) i filtres de cerca.
 *
 * @author Grup3
 */
@Service
public class ClientService {

    private static final Logger log = LoggerFactory.getLogger(ClientService.class);
    public static final String PENDENT = "PENDENT";
    public static final String VALIDAT = "VALIDAT";
    public static final String REBUTJAT = "REBUTJAT";

    private final ClientRepository clientRepository;
    private final ClientRepositoryMongo clientRepositoryMongo;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor principal injectat per Spring per inicialitzar el servei de clients:
     * Configura els repositoris relacional i documental utilitzant un codificador BCryptPasswordEncoder per defecte.
     */
    @Autowired
    public ClientService(ClientRepository clientRepository,
                         ClientRepositoryMongo clientRepositoryMongo) {
        this(clientRepository, clientRepositoryMongo, new BCryptPasswordEncoder());
    }

    /**
     * Constructor complet que permet especificar un codificador de contrasenyes personalitzat:
     * Inicialitza els repositoris de MySQL i MongoDB i garanteix una instància vàlida de PasswordEncoder.
     */
    public ClientService(ClientRepository clientRepository,
                         ClientRepositoryMongo clientRepositoryMongo,
                         PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.clientRepositoryMongo = clientRepositoryMongo;
        this.passwordEncoder = passwordEncoder != null ? passwordEncoder : new BCryptPasswordEncoder();
    }

    // ---------- LLISTAR I FILTRAR ----------

    /**
     * Llista els clients aplicant filtres opcionals de cerca general, DNI, estat, email o data de naixement:
     * Recupera tots els registres i aplica els predicats de filtratge en memòria sobre els camps proporcionats.
     */
    public List<ClientEntity> llistar(String q, String dni, String estat, String email,
                                      LocalDate dataNaixement) {
        List<ClientEntity> clients = clientRepository.findAll();

        if (q != null && !q.isBlank()) {
            String text = q.trim().toLowerCase();
            clients = clients.stream()
                    .filter(c -> conte(c.getNom(), text) || conte(c.getDni(), text)
                            || conte(c.getEmail(), text) || conte(c.getTelefon(), text))
                    .toList();
        }

        if (dni != null && !dni.isBlank()) {
            String d = dni.trim().toUpperCase();
            clients = clients.stream()
                    .filter(c -> c.getDni() != null && c.getDni().toUpperCase().contains(d))
                    .toList();
        }

        if (estat != null && !estat.isBlank()) {
            clients = clients.stream()
                    .filter(c -> estat.equals(c.getEstatValidacio()))
                    .toList();
        }

        if (dataNaixement != null) {
            clients = clients.stream()
                    .filter(c -> dataNaixement.equals(c.getDataNaixement()))
                    .toList();
        }

        if (email != null && !email.isBlank()) {
            String e = email.trim().toLowerCase();
            clients = clients.stream()
                    .filter(c -> conte(c.getEmail(), e))
                    .toList();
        }
        return clients;
    }

    /**
     * Filtra el llistat de clients segons els criteris detallats del panell d'administració:
     * Avalua nacionalitat, nom, telèfon, ciutat/adreça, població, correu electrònic i DNI sense distinció de majúscules.
     */
    public List<ClientEntity> filtrarClients(String nacionalitat, String nom, String telefon,
                                             String ciutat, String dni,
                                             String poblacio, String email) {
        List<ClientEntity> clients = clientRepository.findAll();

        if (nacionalitat != null && !nacionalitat.isBlank()) {
            String filtre = nacionalitat.trim().toLowerCase();
            clients = clients.stream().filter(c -> conte(c.getNacionalitat(), filtre)).toList();
        }

        if (nom != null && !nom.isBlank()) {
            String filtre = nom.trim().toLowerCase();
            clients = clients.stream().filter(c -> conte(c.getNom(), filtre)).toList();
        }

        if (telefon != null && !telefon.isBlank()) {
            String filtre = telefon.replaceAll("\\s+", "");
            clients = clients.stream()
                    .filter(c -> c.getTelefon() != null
                            && c.getTelefon().replaceAll("\\s+", "").contains(filtre))
                    .toList();
        }

        if (ciutat != null && !ciutat.isBlank()) {
            String filtre = ciutat.trim().toLowerCase();
            clients = clients.stream()
                    .filter(c -> conte(c.getPoblacio(), filtre) || conte(c.getAdreca(), filtre))
                    .toList();
        }

        if (poblacio != null && !poblacio.isBlank()) {
            String filtre = poblacio.trim().toLowerCase();
            clients = clients.stream().filter(c -> conte(c.getPoblacio(), filtre)).toList();
        }

        if (email != null && !email.isBlank()) {
            String filtre = email.trim().toLowerCase();
            clients = clients.stream().filter(c -> conte(c.getEmail(), filtre)).toList();
        }

        if (dni != null && !dni.isBlank()) {
            String filtre = dni.trim().toLowerCase();
            clients = clients.stream().filter(c -> conte(c.getDni(), filtre)).toList();
        }

        return clients;
    }

    /**
     * Comprova de forma segura si un camp de text conté una subcadena en minúscules:
     * Evita excepcions de punter nul retornant fals si el valor a inspeccionar és nul.
     */
    private boolean conte(String valor, String text) {
        return valor != null && valor.toLowerCase().contains(text);
    }

    // ---------- ALTA DE CLIENT (Doble Persistència) ----------

    /**
     * Dona d'alta un nou client des del portal públic amb estat inicial PENDENT:
     * Delega al mètode principal d'alta indicant que el registre no ha estat creat per personal intern.
     */
    // Alta des del portal (registre del propi client): queda PENDENT
    @Transactional(rollbackFor = Exception.class)
    public void alta(ClientEntity client, MultipartFile fotoDniFrontal,
                     MultipartFile fotoDniPosterior, MultipartFile fotoCarnet) throws IOException {
        alta(client, fotoDniFrontal, fotoDniPosterior, fotoCarnet, false);
    }

    /**
     * Valida i registra un nou client tant a MySQL com a MongoDB de manera transaccional:
     * Verifica DNI/NIE, telèfon, població, contrasenya, vigor del carnet, format de les 3 imatges i dades de targeta.
     * Assigna l'estat VALIDAT si el crea un agent/administrador o PENDENT si prové del registre extern.
     */
    // Alta amb indicador de qui la fa: si la crea el personal (agent/admin), queda VALIDAT
    @Transactional(rollbackFor = Exception.class)
    public void alta(ClientEntity client, MultipartFile fotoDniFrontal,
                     MultipartFile fotoDniPosterior, MultipartFile fotoCarnet,
                     boolean creatPerPersonal) throws IOException {

        String dni = client.getDni() == null ? "" : client.getDni().trim().toUpperCase();
        if (!dni.matches("^([0-9]{8}|[XYZ][0-9]{7})[A-Z]$")) {
            throw new IllegalArgumentException(
                    "El DNI ha de tenir 8 xifres i una lletra al final (o ser un NIE vàlid)");
        }
        client.setDni(dni);
        client.setTelefon(validaTelefon(client.getTelefon()));

        if (client.getPoblacio() == null || client.getPoblacio().isBlank()) {
            throw new IllegalArgumentException("Cal indicar la població");
        }
        client.setPoblacio(client.getPoblacio().trim());

        if (clientRepository.existsById(dni)) {
            throw new IllegalArgumentException("Ja existeix un client amb aquest DNI");
        }
        String password = client.getPassword();
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("La contrasenya ha de tenir com a mínim 8 caràcters");
        }
        if (!hiHa(fotoDniFrontal) || !hiHa(fotoDniPosterior) || !hiHa(fotoCarnet)) {
            throw new IllegalArgumentException("Cal pujar les 3 imatges (PNG o JPG, màxim 5MB)");
        }

        if (client.getDataCaducitatCarnet() == null || client.getDataCaducitatCarnet().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("El carnet de conduir no pot estar caducat");
        }

        validaImatge(fotoDniFrontal);
        validaImatge(fotoDniPosterior);
        validaImatge(fotoCarnet);

        // Valida la targeta i descarta número complet i CVV (només queden els 4 últims dígits)
        validaTargeta(client);

        client.setPassword(passwordEncoder.encode(password));
        client.setPremium(false);
        client.setEstatValidacio(creatPerPersonal ? VALIDAT : PENDENT);
        clientRepository.saveAndFlush(client);

        ClientDocuments doc = new ClientDocuments();
        doc.setDni(dni);
        doc.setFotoDniFrontal(fotoDniFrontal.getBytes());
        doc.setTipusFotoDniFrontal(fotoDniFrontal.getContentType());
        doc.setFotoDniPosterior(fotoDniPosterior.getBytes());
        doc.setTipusFotoDniPosterior(fotoDniPosterior.getContentType());
        doc.setFotoCarnet(fotoCarnet.getBytes());
        doc.setTipusFotoCarnet(fotoCarnet.getContentType());
        clientRepositoryMongo.save(doc);
        log.info("Client donat d'alta: {} (estat: {}, creat per personal: {})",
                dni, client.getEstatValidacio(), creatPerPersonal);
    }

    /**
     * Cerca un client a la base de dades relacional pel seu DNI:
     * Retorna un Optional amb l'entitat ClientEntity trobada o buit si no existeix.
     */
    public Optional<ClientEntity> obtenir(String dni) {
        return clientRepository.findById(dni);
    }

    /**
     * Recupera els documents gràfics associats a un client des de MongoDB:
     * Retorna un Optional amb l'objecte ClientDocuments indexat pel DNI proporcionat.
     */
    public Optional<ClientDocuments> obtenirDocuments(String dni) {
        return clientRepositoryMongo.findById(dni);
    }

    // ---------- ACTUALITZACIÓ (PANEL AGENT) ----------

    /**
     * Actualitza les dades d'un client existent des del panell de gestió d'agents:
     * Valida l'estat, la unicitat del correu electrònic, el telèfon i les noves imatges opcionals abans de desar a MySQL i MongoDB.
     */
    @Transactional(rollbackFor = Exception.class)
    public void actualitzar(String dni, ClientEntity dades, String estat, boolean premium,
                            MultipartFile fotoDniFrontal, MultipartFile fotoDniPosterior,
                            MultipartFile fotoCarnet) throws IOException {

        ClientEntity c = clientRepository.findById(dni)
                .orElseThrow(() -> new IllegalArgumentException("Client no trobat"));

        if (!VALIDAT.equals(estat) && !REBUTJAT.equals(estat) && !PENDENT.equals(estat)) {
            throw new IllegalArgumentException("Estat no vàlid");
        }

        String email = dades.getEmail() == null ? "" : dades.getEmail().trim();
        if (!email.isEmpty() && clientRepository.existsByEmailIgnoreCaseAndDniNot(email, dni)) {
            throw new IllegalArgumentException("Aquest correu ja està en ús per un altre client");
        }

        String telefon = validaTelefon(dades.getTelefon());

        validaImatge(fotoDniFrontal);
        validaImatge(fotoDniPosterior);
        validaImatge(fotoCarnet);

        actualitzarTargeta(c, dades, dades.getTargetaCaducitat());

        c.setNom(dades.getNom());
        c.setEmail(email);
        if (dades.getPassword() != null && !dades.getPassword().isBlank()) {
            c.setPassword(passwordEncoder.encode(dades.getPassword()));
        }
        c.setTelefon(telefon);
        c.setNacionalitat(dades.getNacionalitat());
        c.setAdreca(dades.getAdreca());
        if (dades.getPoblacio() != null && !dades.getPoblacio().isBlank()) {
            c.setPoblacio(dades.getPoblacio().trim());
        }
        c.setTipusLlicencia(dades.getTipusLlicencia());

        if (dades.getDataNaixement() != null) {
            c.setDataNaixement(dades.getDataNaixement());
        }
        if (dades.getDataCaducitatDNI() != null) {
            c.setDataCaducitatDNI(dades.getDataCaducitatDNI());
        }
        if (dades.getDataCaducitatCarnet() != null) {
            c.setDataCaducitatCarnet(dades.getDataCaducitatCarnet());
        }

        c.setEstatValidacio(estat);
        c.setPremium(premium);

        clientRepository.saveAndFlush(c);
        log.info("Client actualitzat: {}", dni);

        if (hiHa(fotoDniFrontal) || hiHa(fotoDniPosterior) || hiHa(fotoCarnet)) {
            ClientDocuments doc = clientRepositoryMongo.findById(dni).orElseGet(() -> {
                ClientDocuments nou = new ClientDocuments();
                nou.setDni(dni);
                return nou;
            });
            if (hiHa(fotoDniFrontal)) {
                doc.setFotoDniFrontal(fotoDniFrontal.getBytes());
                doc.setTipusFotoDniFrontal(fotoDniFrontal.getContentType());
            }
            if (hiHa(fotoDniPosterior)) {
                doc.setFotoDniPosterior(fotoDniPosterior.getBytes());
                doc.setTipusFotoDniPosterior(fotoDniPosterior.getContentType());
            }
            if (hiHa(fotoCarnet)) {
                doc.setFotoCarnet(fotoCarnet.getBytes());
                doc.setTipusFotoCarnet(fotoCarnet.getContentType());
            }
            clientRepositoryMongo.save(doc);
        }
    }

    // ---------- PERFIL DEL CLIENT (AUTOGESTIÓ) ----------

    /**
     * Cerca un client pel seu correu electrònic ignorant majúscules i minúscules:
     * Retorna un Optional amb l'entitat trobada per gestionar la sessió i el perfil personal.
     */
    public Optional<ClientEntity> obtenirPerEmail(String email) {
        return clientRepository.findByEmailIgnoreCase(email);
    }

    /**
     * Actualitza les dades personals modificades pel propi client des del seu portal:
     * Verifica que el nou correu no estigui duplicat, valida el telèfon i xifra la contrasenya si s'ha modificat.
     */
    @Transactional(rollbackFor = Exception.class)
    public void actualitzarPerfilClient(String emailActual, ClientEntity dades) {
        ClientEntity c = clientRepository.findByEmailIgnoreCase(emailActual)
                .orElseThrow(() -> new IllegalArgumentException("Client no trobat"));

        String nouEmail = dades.getEmail() == null ? "" : dades.getEmail().trim();
        if (!nouEmail.isEmpty() && clientRepository.existsByEmailIgnoreCaseAndDniNot(nouEmail, c.getDni())) {
            throw new IllegalArgumentException("Aquest correu ja està en ús per un altre client");
        }

        String telefon = validaTelefon(dades.getTelefon());
        actualitzarTargeta(c, dades, dades.getNovaCaducitatTargeta());

        c.setNom(dades.getNom());
        c.setEmail(nouEmail);
        if (dades.getPassword() != null && !dades.getPassword().isBlank()) {
            if (dades.getPassword().length() < 8) {
                throw new IllegalArgumentException("La contrasenya ha de tenir com a mínim 8 caràcters");
            }
            c.setPassword(passwordEncoder.encode(dades.getPassword()));
        }
        c.setTelefon(telefon);
        c.setNacionalitat(dades.getNacionalitat());
        c.setAdreca(dades.getAdreca());
        if (dades.getPoblacio() != null && !dades.getPoblacio().isBlank()) {
            c.setPoblacio(dades.getPoblacio().trim());
        }
        c.setTipusLlicencia(dades.getTipusLlicencia());

        if (dades.getDataNaixement() != null) {
            c.setDataNaixement(dades.getDataNaixement());
        }
        if (dades.getDataCaducitatDNI() != null) {
            c.setDataCaducitatDNI(dades.getDataCaducitatDNI());
        }

        clientRepository.saveAndFlush(c);
        log.info("Perfil actualitzat pel client: {}", c.getDni());
    }

    /**
     * Comprova si s'ha adjuntat un fitxer no buit a la petició multiparte:
     * Retorna cert si l'objecte MultipartFile no és nul i conté dades.
     */
    private boolean hiHa(MultipartFile f) {
        return f != null && !f.isEmpty();
    }

    /**
     * Normalitza i valida que el número de telèfon contingui exactament 9 dígits numèrics:
     * Elimina qualsevol espai en blanc i llança una excepció si no compleix el format espanyol estàndard.
     */
    // El telèfon ha de tenir exactament 9 xifres (s'ignoren els espais)
    private String validaTelefon(String telefon) {
        String t = telefon == null ? "" : telefon.replaceAll("\\s+", "");
        if (!t.matches("^[0-9]{9}$")) {
            throw new IllegalArgumentException("El telèfon ha de tenir exactament 9 xifres");
        }
        return t;
    }

    private void actualitzarTargeta(ClientEntity actual, ClientEntity dades, String novaCaducitat) {
        boolean hiHaNumero = dades.getNumTargeta() != null && !dades.getNumTargeta().isBlank();
        boolean hiHaCaducitat = novaCaducitat != null && !novaCaducitat.isBlank();

        if (hiHaNumero != hiHaCaducitat) {
            throw new IllegalArgumentException(hiHaNumero
                    ? "Indica també la data de caducitat de la nova targeta"
                    : "Introdueix el número de targeta per canviar-la");
        }

        if (hiHaNumero) {
            dades.setTargetaCaducitat(novaCaducitat);
            validaTargeta(dades);
            actual.setTargetaUltims4(dades.getTargetaUltims4());
            actual.setTargetaCaducitat(dades.getTargetaCaducitat());
        }
    }

    /**
     * Valida les dades de la targeta de pagament i en neteja els camps sensibles abans de persistir:
     * Verifica longitud, dígit de control Luhn i data de caducitat, conservant només els 4 últims dígits.
     */
    // Valida la targeta; només es conserven els 4 últims dígits i la caducitat
        // Valida la targeta; només es conserven els 4 últims dígits i la caducitat
    private void validaTargeta(ClientEntity c) {
        String num = c.getNumTargeta() == null ? "" : c.getNumTargeta().replaceAll("[\\s-]", "");
        if (!num.matches("^[0-9]{13,19}$") || !luhn(num)) {
            throw new IllegalArgumentException("El número de targeta no és vàlid");
        }
       

        YearMonth cad = parseCaducitat(c.getTargetaCaducitat());
        if (cad.isBefore(YearMonth.now())) {
            throw new IllegalArgumentException("La targeta està caducada");
        }

        // Guardamos la caducidad normalizada (2028-05) para que quede siempre en el mismo formato
        c.setTargetaCaducitat(cad.toString());
        c.setTargetaUltims4(num.substring(num.length() - 4));
        c.setNumTargeta(null);
      
    }

    /**
     * Interpreta i normalitza diversos formats d'entrada per a la data de caducitat d'una targeta:
     * Admet patrons com yyyy-MM, MM/yy, MM/yyyy, MMyy o MMyyyy i retorna un objecte YearMonth vàlid.
     */
    // Accepta 2028-05, 2028/05, 05/28, 05-28, 05/2028, 0528 i 052028
    private YearMonth parseCaducitat(String valor) {
        String v = valor == null ? "" : valor.trim().replaceAll("\\s+", "");
        try {
            // Any-mes: 2028-05 o 2028/05
            if (v.matches("^\\d{4}[-/]\\d{2}$")) {
                return YearMonth.of(Integer.parseInt(v.substring(0, 4)),
                        Integer.parseInt(v.substring(5)));
            }
            // Mes-any amb any de 2 xifres: 05/28 o 05-28
            if (v.matches("^\\d{2}[-/]\\d{2}$")) {
                return YearMonth.of(2000 + Integer.parseInt(v.substring(3)),
                        Integer.parseInt(v.substring(0, 2)));
            }
            // Mes-any amb any de 4 xifres: 05/2028 o 05-2028
            if (v.matches("^\\d{2}[-/]\\d{4}$")) {
                return YearMonth.of(Integer.parseInt(v.substring(3)),
                        Integer.parseInt(v.substring(0, 2)));
            }
            // Sense separador: 0528 o 052028
            if (v.matches("^\\d{4}$")) {
                return YearMonth.of(2000 + Integer.parseInt(v.substring(2)),
                        Integer.parseInt(v.substring(0, 2)));
            }
            if (v.matches("^\\d{6}$")) {
                return YearMonth.of(Integer.parseInt(v.substring(2)),
                        Integer.parseInt(v.substring(0, 2)));
            }
        } catch (Exception e) {
            // Mes inexistente (13, 00...): cae al error de abajo
        }
        throw new IllegalArgumentException("La data de caducitat de la targeta no és vàlida");
    }

    /**
     * Verifica la validesa matemàtica d'un número de targeta bancària mitjançant l'algoritme de Luhn:
     * Duplica els dígits en posicions parelles des de la dreta i comprova que la suma total sigui múltiple de 10.
     */
    // Algoritme de Luhn: comprova que el número de targeta és matemàticament coherent
    private boolean luhn(String num) {
        int suma = 0;
        boolean doble = false;
        for (int i = num.length() - 1; i >= 0; i--) {
            int d = num.charAt(i) - '0';
            if (doble) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            suma += d;
            doble = !doble;
        }
        return suma % 10 == 0;
    }

    /**
     * Comprova que una imatge pujada tingui format PNG o JPG i no superi els 5 MB de mida:
     * Ignora el fitxer si és buit i llança una excepció si el tipus MIME o el pes no són permesos.
     */
    private void validaImatge(MultipartFile f) {
        if (!hiHa(f)) {
            return;
        }
        String tipus = f.getContentType();
        boolean tipusValid = "image/png".equals(tipus) || "image/jpeg".equals(tipus);
        if (!tipusValid || f.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Les imatges han de ser PNG o JPG de màxim 5MB");
        }
    }

    // ---------- ELIMINACIÓ DE CLIENT ----------

    /**
     * Elimina definitivament un client tant de MySQL com els seus documents de MongoDB:
     * Executa primer l'esborrat relacional amb flush per detectar possibles restriccions de clau forana abans d'esborrar els fitxers.
     */
    @Transactional
    public void eliminar(String dni) {
        ClientEntity c = clientRepository.findById(dni)
                .orElseThrow(() -> new IllegalArgumentException("Client no trobat"));

        clientRepository.delete(c);
        clientRepository.flush();

        clientRepositoryMongo.deleteById(dni);
        log.warn("Client eliminat: {}", dni);
    }

    // ---------- CANVI D'ESTAT DE VALIDACIÓ (RF82) ----------

    /**
     * Modifica l'estat de validació d'un client entre PENDENT, VALIDAT i REBUTJAT:
     * Valida que l'estat rebut sigui permès, recupera el client pel DNI i desa el nou estat al repositori.
     */
    @Transactional
    public void canviarEstat(String dni, String estat) {
        if (!VALIDAT.equals(estat) && !REBUTJAT.equals(estat) && !PENDENT.equals(estat)) {
            throw new IllegalArgumentException("Estat no vàlid");
        }
        ClientEntity c = clientRepository.findById(dni)
                .orElseThrow(() -> new IllegalArgumentException("Client no trobat"));

        c.setEstatValidacio(estat);
        clientRepository.save(c);
    }

    // ---------- CANVI D'ESTAT PREMIUM ----------

    /**
     * Activa o desactiva la condició de client prèmium per a un client determinat:
     * Cerca l'entitat pel DNI proporcionat, actualitza l'indicador booleà i desa el canvi.
     */
    @Transactional
    public void canviarPremium(String dni, boolean premium) {
        ClientEntity c = clientRepository.findById(dni)
                .orElseThrow(() -> new IllegalArgumentException("Client no trobat"));
        c.setPremium(premium);
        clientRepository.save(c);
    }
}
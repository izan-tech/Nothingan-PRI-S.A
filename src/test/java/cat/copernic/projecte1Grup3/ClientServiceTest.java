/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3;

import cat.copernic.projecte1Grup3.Usuaris.Client.Documents.ClientDocuments;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.ClientEntity;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.ClientRepository;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.ClientRepositoryMongo;
import cat.copernic.projecte1Grup3.Usuaris.Client.Service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Classe de proves unitàries amb JUnit 5 i Mockito per validar la lògica de
 * negoci de ClientService: Comprova les regles d'alta, validació documental,
 * algorisme de Luhn de targetes i xifrat de credencials. Verifica també les
 * operacions d'actualització de dades, gestió d'estats, eliminació dual i
 * filtratge.
 *
 * @author Grup3
 */
@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientRepositoryMongo clientRepositoryMongo;

    @InjectMocks
    private ClientService clientService;

    private ClientEntity clientExemple;
    private MockMultipartFile imatge;

    /**
     * Inicialitza les dades de prova abans de cada test unitari: Crea un
     * ClientEntity completament vàlid amb targeta Luhn vigent i un fitxer
     * MockMultipartFile de mostra.
     */
    @BeforeEach
    void setUp() {
        clientExemple = new ClientEntity();
        clientExemple.setDni("12345678Z");
        clientExemple.setNom("Joan Garcia");
        clientExemple.setEmail("joan@test.com");
        clientExemple.setPassword("password123");
        clientExemple.setTelefon("612345678");
        clientExemple.setNacionalitat("Espanyola");
        clientExemple.setAdreca("Carrer Major 12");
        clientExemple.setPoblacio("Terrassa");
        clientExemple.setTipusLlicencia("B");
        clientExemple.setDataNaixement(LocalDate.of(1990, 5, 14));
        clientExemple.setDataCaducitatDNI(LocalDate.of(2030, 3, 1));
        clientExemple.setDataCaducitatCarnet(LocalDate.now().plusYears(3));
        clientExemple.setEstatValidacio("VALIDAT");

        // Targeta de prova vàlida (Luhn) i caducitat futura
        clientExemple.setNumTargeta("4111 1111 1111 1111");
        clientExemple.setTargetaCaducitat(YearMonth.now().plusYears(2).toString());

        imatge = new MockMultipartFile("foto", "foto.png", "image/png", new byte[]{1, 2, 3});
    }

    // ---------- ALTA ----------
    /**
     * Comprova que el procés d'alta rebutja un DNI amb format o lletra de
     * control invàlids: Verifica que es llança IllegalArgumentException i no es
     * desa cap registre al repositori.
     */
    @Test
    @DisplayName("Comprovar que alta rebutja un DNI amb format invàlid")
    void testAltaDniInvalid() {
        clientExemple.setDni("1234");

        assertThrows(IllegalArgumentException.class,
                () -> clientService.alta(clientExemple, imatge, imatge, imatge));

        verify(clientRepository, never()).saveAndFlush(any());
    }

    /**
     * Comprova que l'alta rebutja un número de telèfon que no contingui
     * exactament 9 dígits: Assegura que es llança IllegalArgumentException
     * sense invocar saveAndFlush() al repositori.
     */
    @Test
    @DisplayName("Comprovar que alta rebutja un telèfon que no té 9 xifres")
    void testAltaTelefonInvalid() {
        clientExemple.setTelefon("12345");

        assertThrows(IllegalArgumentException.class,
                () -> clientService.alta(clientExemple, imatge, imatge, imatge));

        verify(clientRepository, never()).saveAndFlush(any());
    }

    /**
     * Verifica que no es permet registrar un client deixant el camp població
     * buit o amb espais: Comprova que el servei llança IllegalArgumentException
     * i cancel·la la persistència.
     */
    @Test
    @DisplayName("Comprovar que alta rebutja si no s'indica la població")
    void testAltaSensePoblacio() {
        clientExemple.setPoblacio("  ");

        assertThrows(IllegalArgumentException.class,
                () -> clientService.alta(clientExemple, imatge, imatge, imatge));

        verify(clientRepository, never()).saveAndFlush(any());
    }

    /**
     * Comprova que l'alta impedeix registrar un DNI que ja existeix a la base
     * de dades: Simula l'existència prèvia de l'identificador i verifica el
     * llançament d'IllegalArgumentException.
     */
    @Test
    @DisplayName("Comprovar que alta rebutja un DNI que ja existeix")
    void testAltaDniDuplicat() {
        when(clientRepository.existsById("12345678Z")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> clientService.alta(clientExemple, imatge, imatge, imatge));

        verify(clientRepository, never()).saveAndFlush(any());
    }

    /**
     * Verifica que el registre rebutja contrasenyes amb una longitud inferior a
     * 8 caràcters: Comprova que es llança IllegalArgumentException abans de
     * desar el client.
     */
    @Test
    @DisplayName("Comprovar que alta rebutja una contrasenya de menys de 8 caràcters")
    void testAltaContrasenyaCurta() {
        clientExemple.setPassword("1234");
        when(clientRepository.existsById("12345678Z")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> clientService.alta(clientExemple, imatge, imatge, imatge));

        verify(clientRepository, never()).saveAndFlush(any());
    }

    /**
     * Comprova que l'alta exigeix l'aportació obligatòria de les tres imatges
     * documentals: Valida que passar un fitxer nul llança
     * IllegalArgumentException i evita desar el registre.
     */
    @Test
    @DisplayName("Comprovar que alta rebutja si falta alguna de les 3 imatges")
    void testAltaSenseImatges() {
        when(clientRepository.existsById("12345678Z")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> clientService.alta(clientExemple, imatge, null, imatge));

        verify(clientRepository, never()).saveAndFlush(any());
    }

    /**
     * Verifica que el sistema no accepta el registre d'un client amb el carnet
     * de conduir caducat: Comprova que una data de caducitat passada provoca
     * una IllegalArgumentException.
     */
    @Test
    @DisplayName("Comprovar que alta rebutja un carnet de conduir caducat")
    void testAltaCarnetCaducat() {
        clientExemple.setDataCaducitatCarnet(LocalDate.now().minusDays(1));
        when(clientRepository.existsById("12345678Z")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> clientService.alta(clientExemple, imatge, imatge, imatge));

        verify(clientRepository, never()).saveAndFlush(any());
    }

    /**
     * Comprova la validació de l'algorisme de Luhn sobre el número de targeta
     * bancària: Assegura que un número de targeta invàlid és rebutjat amb
     * IllegalArgumentException.
     */
    @Test
    @DisplayName("Comprovar que alta rebutja un número de targeta que no passa Luhn")
    void testAltaTargetaInvalida() {
        clientExemple.setNumTargeta("1234 5678 1234 5678");
        when(clientRepository.existsById("12345678Z")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> clientService.alta(clientExemple, imatge, imatge, imatge));

        verify(clientRepository, never()).saveAndFlush(any());
    }

    /**
     * Verifica que el procés d'alta rebutja una targeta bancària amb data de
     * caducitat vençuda: Comprova que indicar un mes anterior a l'actual llança
     * IllegalArgumentException.
     */
    @Test
    @DisplayName("Comprovar que alta rebutja una targeta caducada")
    void testAltaTargetaCaducada() {
        clientExemple.setTargetaCaducitat(YearMonth.now().minusMonths(1).toString());
        when(clientRepository.existsById("12345678Z")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> clientService.alta(clientExemple, imatge, imatge, imatge));

        verify(clientRepository, never()).saveAndFlush(any());
    }

    /**
     * Valida el flux d'alta pública correcta des del portal web: Comprova que
     * el client queda en estat PENDENT, no premium, i es desa tant a MySQL com
     * a MongoDB.
     */
    @Test
    @DisplayName("Comprovar que alta des del portal deixa el client PENDENT, no premium, i desa a MySQL i MongoDB")
    void testAltaCorrecta() throws Exception {
        clientExemple.setEstatValidacio(null);
        when(clientRepository.existsById("12345678Z")).thenReturn(false);

        clientService.alta(clientExemple, imatge, imatge, imatge);

        assertEquals("PENDENT", clientExemple.getEstatValidacio());
        assertFalse(clientExemple.isPremium());
        verify(clientRepository, times(1)).saveAndFlush(clientExemple);
        verify(clientRepositoryMongo, times(1)).save(any(ClientDocuments.class));
    }

    /**
     * Comprova que l'alta realitzada pel personal intern valida automàticament
     * el client: Verifica que l'estat de validació s'estableix a VALIDAT
     * directament en desar el registre.
     */
    @Test
    @DisplayName("Comprovar que alta creada pel personal deixa el client directament VALIDAT")
    void testAltaPerPersonalQuedaValidat() throws Exception {
        clientExemple.setEstatValidacio(null);
        when(clientRepository.existsById("12345678Z")).thenReturn(false);

        clientService.alta(clientExemple, imatge, imatge, imatge, true);

        assertEquals("VALIDAT", clientExemple.getEstatValidacio());
        verify(clientRepository, times(1)).saveAndFlush(clientExemple);
    }

    /**
     * Verifica la protecció de dades sensibles durant l'alta d'un nou client:
     * Comprova que la contrasenya es xifra, s'eliminen la targeta completa i el
     * CVV, i es desen els 4 últims dígits.
     */
    @Test
    @DisplayName("Comprovar que alta xifra la contrasenya i descarta el número complet de la targeta i el CVV")
    void testAltaProtegeDadesSensibles() throws Exception {
        when(clientRepository.existsById("12345678Z")).thenReturn(false);

        clientService.alta(clientExemple, imatge, imatge, imatge);

        assertNotEquals("password123", clientExemple.getPassword());
        assertNull(clientExemple.getNumTargeta());
        assertEquals("1111", clientExemple.getTargetaUltims4());
    }

    // ---------- ACTUALITZAR ----------
    /**
     * Comprova que l'actualització d'un client impedeix assignar un correu
     * electrònic ja utilitzat per un altre usuari: Verifica el llançament
     * d'IllegalArgumentException i que no es persisteixen canvis.
     */
    @Test
    @DisplayName("Comprovar que actualitzar rebutja un correu que ja té un altre client")
    void testActualitzarEmailDuplicat() {
        when(clientRepository.findById("12345678Z")).thenReturn(Optional.of(clientExemple));
        when(clientRepository.existsByEmailIgnoreCaseAndDniNot("altre@test.com", "12345678Z"))
                .thenReturn(true);

        ClientEntity dades = new ClientEntity();
        dades.setEmail("altre@test.com");

        assertThrows(IllegalArgumentException.class,
                () -> clientService.actualitzar("12345678Z", dades, "VALIDAT", false, null, null, null));

        verify(clientRepository, never()).saveAndFlush(any());
    }

    /**
     * Verifica que l'actualització rebutja un número de telèfon que no compleix
     * el format de 9 dígits: Assegura que es llança IllegalArgumentException
     * sense modificar el registre existent.
     */
    @Test
    @DisplayName("Comprovar que actualitzar rebutja un telèfon que no té 9 xifres")
    void testActualitzarTelefonInvalid() {
        when(clientRepository.findById("12345678Z")).thenReturn(Optional.of(clientExemple));

        ClientEntity dades = new ClientEntity();
        dades.setEmail("joan@test.com");
        dades.setTelefon("123");

        assertThrows(IllegalArgumentException.class,
                () -> clientService.actualitzar("12345678Z", dades, "VALIDAT", false, null, null, null));

        verify(clientRepository, never()).saveAndFlush(any());
    }

    /**
     * Comprova que l'actualització parcial preserva les dates originals si no
     * s'informen al formulari: Verifica que manté la data de naixement i
     * caducitat del DNI mentre actualitza nom, telèfon i estat premium.
     */
    @Test
    @DisplayName("Comprovar que actualitzar no sobreescriu les dates amb null")
    void testActualitzarConservaDatesSiNoVenen() throws Exception {
        when(clientRepository.findById("12345678Z")).thenReturn(Optional.of(clientExemple));
        clientExemple.setNumTargeta(null);
        clientExemple.setTargetaUltims4("1111");
        clientExemple.setTargetaCaducitat(YearMonth.now().plusYears(2).toString());
        LocalDate dniAbans = clientExemple.getDataCaducitatDNI();
        LocalDate naixementAbans = clientExemple.getDataNaixement();
        String caducitatTargetaAbans = clientExemple.getTargetaCaducitat();

        ClientEntity dades = new ClientEntity();
        dades.setNom("Joan Garcia Soler");
        dades.setEmail("joan@test.com");
        dades.setTelefon("612 345 678");

        clientService.actualitzar("12345678Z", dades, "VALIDAT", true, null, null, null);

        assertEquals(dniAbans, clientExemple.getDataCaducitatDNI());
        assertEquals(naixementAbans, clientExemple.getDataNaixement());
        assertEquals("Joan Garcia Soler", clientExemple.getNom());
        assertEquals("612345678", clientExemple.getTelefon());
        assertEquals("1111", clientExemple.getTargetaUltims4());
        assertEquals(caducitatTargetaAbans, clientExemple.getTargetaCaducitat());
        assertTrue(clientExemple.isPremium());
        verify(clientRepository, times(1)).saveAndFlush(clientExemple);
    }

    @Test
    @DisplayName("Comprovar que un agent pot canviar la targeta del client")
    void testActualitzarTargetaClientDesDelPanellAgent() throws Exception {
        when(clientRepository.findById("12345678Z")).thenReturn(Optional.of(clientExemple));
        clientExemple.setNumTargeta(null);
        clientExemple.setTargetaUltims4("1111");
        clientExemple.setTargetaCaducitat(YearMonth.now().plusYears(1).toString());
        YearMonth novaCaducitat = YearMonth.now().plusYears(3);

        ClientEntity dades = new ClientEntity();
        dades.setEmail("joan@test.com");
        dades.setTelefon("612345678");
        dades.setNumTargeta("5555 5555 5555 4444");
        dades.setTargetaCaducitat(novaCaducitat.format(DateTimeFormatter.ofPattern("MM/yy")));

        clientService.actualitzar("12345678Z", dades, "VALIDAT", false, null, null, null);

        assertEquals("4444", clientExemple.getTargetaUltims4());
        assertEquals(novaCaducitat.toString(), clientExemple.getTargetaCaducitat());
        assertNull(dades.getNumTargeta());
        verify(clientRepository, times(1)).saveAndFlush(clientExemple);
    }

    @Test
    @DisplayName("Comprovar que un agent no pot canviar només la caducitat de la targeta")
    void testActualitzarTargetaClientRebutjaCaducitatSenseNumero() {
        when(clientRepository.findById("12345678Z")).thenReturn(Optional.of(clientExemple));

        ClientEntity dades = new ClientEntity();
        dades.setEmail("joan@test.com");
        dades.setTelefon("612345678");
        dades.setTargetaCaducitat(YearMonth.now().plusYears(3).toString());

        assertThrows(IllegalArgumentException.class,
                () -> clientService.actualitzar("12345678Z", dades, "VALIDAT", false, null, null, null));

        verify(clientRepository, never()).saveAndFlush(any());
    }

    /**
     * Verifica que l'actualització rebutja un estat de validació desconegut o
     * no permès: Comprova que passar un estat invàlid llança
     * IllegalArgumentException i evita desar l'entitat.
     */
    @Test
    @DisplayName("Comprovar que actualitzar rebutja un estat de validació no vàlid")
    void testActualitzarEstatInvalid() {
        when(clientRepository.findById("12345678Z")).thenReturn(Optional.of(clientExemple));

        assertThrows(IllegalArgumentException.class,
                () -> clientService.actualitzar("12345678Z", new ClientEntity(), "INVENTAT", false, null, null, null));

        verify(clientRepository, never()).saveAndFlush(any());
    }

    // ---------- PERFIL DEL CLIENT ----------
    /**
     * Comprova que un client autenticat pot modificar correctament les dades
     * del seu propi perfil: Verifica l'actualització del nom i telèfon i la
     * persistència mitjançant saveAndFlush().
     */
    @Test
    @DisplayName("Comprovar que el client pot actualitzar el seu perfil")
    void testActualitzarPerfilClient() {
        when(clientRepository.findByEmailIgnoreCase("joan@test.com")).thenReturn(Optional.of(clientExemple));
        clientExemple.setNumTargeta(null);
        clientExemple.setTargetaUltims4("1111");
        clientExemple.setTargetaCaducitat(YearMonth.now().plusYears(2).toString());
        String caducitatActual = clientExemple.getTargetaCaducitat();

        ClientEntity dades = new ClientEntity();
        dades.setNom("Joan G. Soler");
        dades.setEmail("joan@test.com");
        dades.setTelefon("699888777");

        clientService.actualitzarPerfilClient("joan@test.com", dades);

        assertEquals("Joan G. Soler", clientExemple.getNom());
        assertEquals("699888777", clientExemple.getTelefon());
        assertEquals("1111", clientExemple.getTargetaUltims4());
        assertEquals(caducitatActual, clientExemple.getTargetaCaducitat());
        verify(clientRepository, times(1)).saveAndFlush(clientExemple);
    }

    @Test
    @DisplayName("Comprovar que el client pot canviar la targeta des del seu perfil")
    void testActualitzarTargetaDesDelPerfil() {
        when(clientRepository.findByEmailIgnoreCase("joan@test.com")).thenReturn(Optional.of(clientExemple));
        clientExemple.setNumTargeta(null);
        clientExemple.setTargetaUltims4("1111");
        clientExemple.setTargetaCaducitat(YearMonth.now().plusYears(1).toString());

        ClientEntity dades = new ClientEntity();
        dades.setEmail("joan@test.com");
        dades.setTelefon("699888777");
        dades.setNumTargeta("5555 5555 5555 4444");
        dades.setNovaCaducitatTargeta(YearMonth.now().plusYears(3).toString());

        clientService.actualitzarPerfilClient("joan@test.com", dades);

        assertEquals("4444", clientExemple.getTargetaUltims4());
        assertEquals(YearMonth.now().plusYears(3).toString(), clientExemple.getTargetaCaducitat());
        assertNull(dades.getNumTargeta());
        verify(clientRepository, times(1)).saveAndFlush(clientExemple);
    }

    @Test
    @DisplayName("Comprovar que el perfil rebutja canviar la caducitat sense número de targeta")
    void testActualitzarCaducitatTargetaSenseNumero() {
        when(clientRepository.findByEmailIgnoreCase("joan@test.com")).thenReturn(Optional.of(clientExemple));

        ClientEntity dades = new ClientEntity();
        dades.setEmail("joan@test.com");
        dades.setTelefon("699888777");
        dades.setNovaCaducitatTargeta(YearMonth.now().plusYears(3).toString());

        assertThrows(IllegalArgumentException.class,
                () -> clientService.actualitzarPerfilClient("joan@test.com", dades));

        verify(clientRepository, never()).saveAndFlush(any());
    }

    /**
     * Verifica que l'edició del perfil rebutja canviar el correu per un que ja
     * pertany a un altre client: Comprova que es llança
     * IllegalArgumentException sense desar els canvis.
     */
    @Test
    @DisplayName("Comprovar que el perfil rebutja un correu que ja és d'un altre client")
    void testActualitzarPerfilEmailDuplicat() {
        when(clientRepository.findByEmailIgnoreCase("joan@test.com")).thenReturn(Optional.of(clientExemple));
        when(clientRepository.existsByEmailIgnoreCaseAndDniNot("altre@test.com", "12345678Z"))
                .thenReturn(true);

        ClientEntity dades = new ClientEntity();
        dades.setEmail("altre@test.com");
        dades.setTelefon("699888777");

        assertThrows(IllegalArgumentException.class,
                () -> clientService.actualitzarPerfilClient("joan@test.com", dades));

        verify(clientRepository, never()).saveAndFlush(any());
    }

    // ---------- ESTATS ----------
    /**
     * Comprova que el canvi d'estat actualitza correctament l'estat de
     * validació del client: Verifica la modificació a REBUTJAT i la crida a
     * save() sobre el repositori.
     */
    @Test
    @DisplayName("Comprovar que canviarEstat modifica l'estat de validació del client")
    void testCanviarEstat() {
        when(clientRepository.findById("12345678Z")).thenReturn(Optional.of(clientExemple));

        clientService.canviarEstat("12345678Z", "REBUTJAT");

        assertEquals("REBUTJAT", clientExemple.getEstatValidacio());
        verify(clientRepository, times(1)).save(clientExemple);
    }

    /**
     * Verifica que canviarEstat rebutja valors que no corresponen als estats
     * permesos del sistema: Comprova que es llança IllegalArgumentException
     * sense invocar el repositori.
     */
    @Test
    @DisplayName("Comprovar que canviarEstat rebutja un estat no vàlid")
    void testCanviarEstatInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> clientService.canviarEstat("12345678Z", "INVENTAT"));

        verify(clientRepository, never()).save(any());
    }

    /**
     * Comprova l'activació o desactivació de la condició de client premium:
     * Verifica que el camp booleà s'actualitza a veritable i es desa l'entitat.
     */
    @Test
    @DisplayName("Comprovar que canviarPremium modifica correctament l'estat premium")
    void testCanviarPremium() {
        when(clientRepository.findById("12345678Z")).thenReturn(Optional.of(clientExemple));

        clientService.canviarPremium("12345678Z", true);

        assertTrue(clientExemple.isPremium());
        verify(clientRepository, times(1)).save(clientExemple);
    }

    // ---------- ELIMINAR ----------
    /**
     * Comprova l'eliminació sincronitzada d'un client tant a MySQL com a
     * MongoDB: Verifica que s'esborra l'entitat relacional, s'executa flush() i
     * s'eliminen els seus documents binaris.
     */
    @Test
    @DisplayName("Comprovar que eliminar esborra el client de MySQL i els documents de MongoDB")
    void testEliminar() {
        when(clientRepository.findById("12345678Z")).thenReturn(Optional.of(clientExemple));

        clientService.eliminar("12345678Z");

        verify(clientRepository, times(1)).delete(clientExemple);
        verify(clientRepository, times(1)).flush();
        verify(clientRepositoryMongo, times(1)).deleteById("12345678Z");
    }

    /**
     * Verifica que intentar eliminar un client inexistent llança una excepció:
     * Comprova que es llança IllegalArgumentException i no s'executa cap
     * esborrat a MySQL ni a MongoDB.
     */
    @Test
    @DisplayName("Comprovar que eliminar llança excepció si el client no existeix")
    void testEliminarNoExisteix() {
        when(clientRepository.findById("00000000T")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> clientService.eliminar("00000000T"));

        verify(clientRepository, never()).delete(any(ClientEntity.class));
        verify(clientRepositoryMongo, never()).deleteById(any());
    }

    // ---------- LLISTAR I FILTRAR ----------
    /**
     * Comprova el filtratge de clients segons el seu estat de validació:
     * Verifica que llistar() retorna únicament els clients que coincideixen amb
     * l'estat sol·licitat.
     */
    @Test
    @DisplayName("Comprovar que llistar filtra els clients per estat de validació")
    void testLlistarPerEstat() {
        ClientEntity pendent = new ClientEntity();
        pendent.setDni("87654321X");
        pendent.setEstatValidacio("PENDENT");

        when(clientRepository.findAll()).thenReturn(List.of(clientExemple, pendent));

        List<ClientEntity> resultat = clientService.llistar(null, null, "PENDENT", null, null);

        assertEquals(1, resultat.size());
        assertEquals("87654321X", resultat.get(0).getDni());
    }

    /**
     * Valida el filtratge combinat per múltiples atributs del client
     * simultàniament: Comprova la cerca conjunta per nacionalitat, nom,
     * telèfon, ciutat i DNI.
     */
    @Test
    @DisplayName("Comprovar que el filtratge de clients combina els camps")
    void testFiltrarClientsPerCamps() {
        clientExemple.setAdreca("Carrer Major 12, Terrassa");

        ClientEntity altra = new ClientEntity();
        altra.setDni("87654321X");
        altra.setNom("Maria Soler");
        altra.setNacionalitat("Francesa");
        altra.setTelefon("699111222");
        altra.setAdreca("Carrer Nou 4, Girona");

        when(clientRepository.findAll()).thenReturn(List.of(clientExemple, altra));

        // nacionalitat, nom, telefon, ciutat, dni, poblacio, email
        List<ClientEntity> resultat = clientService.filtrarClients(
                "espanyola", "joan garcia", "612 345", "terrassa", "12345678z", null, null);

        assertEquals(List.of(clientExemple), resultat);
    }

    /**
     * Comprova que el filtre de ciutat realitza la cerca dins del camp adreça
     * sense distingir majúscules: Verifica que retorna el client encara que el
     * camp població sigui nul.
     */
    @Test
    @DisplayName("Comprovar que el filtre de ciutat cerca dins de l'adreça")
    void testFiltrarClientsPerCiutat() {
        clientExemple.setAdreca("Carrer Major 12, Terrassa");
        clientExemple.setPoblacio(null);
        when(clientRepository.findAll()).thenReturn(List.of(clientExemple));

        List<ClientEntity> resultat = clientService.filtrarClients(
                null, null, null, "TERRASSA", null, null, null);

        assertEquals(List.of(clientExemple), resultat);
    }

    /**
     * Verifica que el filtre de població avalua exclusivament l'atribut
     * població del client: Comprova la coincidència insensible a majúscules i
     * minúscules.
     */
    @Test
    @DisplayName("Comprovar que el filtre de població només mira el camp població")
    void testFiltrarClientsPerPoblacio() {
        ClientEntity altra = new ClientEntity();
        altra.setDni("87654321X");
        altra.setPoblacio("Girona");

        when(clientRepository.findAll()).thenReturn(List.of(clientExemple, altra));

        List<ClientEntity> resultat = clientService.filtrarClients(
                null, null, null, null, null, "girona", null);

        assertEquals(List.of(altra), resultat);
    }

    /**
     * Comprova que la cerca per correu electrònic és parcial i insensible a
     * majúscules: Verifica que filtrar per un fragment en majúscules retorna el
     * client corresponent.
     */
    @Test
    @DisplayName("Comprovar que el filtre per correu no distingeix majúscules")
    void testFiltrarClientsPerEmail() {
        when(clientRepository.findAll()).thenReturn(List.of(clientExemple));

        List<ClientEntity> resultat = clientService.filtrarClients(
                null, null, null, null, null, null, "JOAN@TEST");

        assertEquals(List.of(clientExemple), resultat);
    }
}

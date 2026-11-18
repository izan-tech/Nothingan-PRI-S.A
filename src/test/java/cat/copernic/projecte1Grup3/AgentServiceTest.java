/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3;

import cat.copernic.projecte1Grup3.Usuaris.Agent.Entity.AgentEntity;
import cat.copernic.projecte1Grup3.Usuaris.Agent.Repository.AgentRepository;
import cat.copernic.projecte1Grup3.Usuaris.Agent.Service.AgentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Classe de proves unitàries amb JUnit 5 i Mockito per verificar la lògica de negoci d'AgentService:
 * Avalua les validacions d'alta i edició d'agents (DNI, majoria d'edat i gestió de contrasenyes).
 * Comprova també les operacions de consulta, eliminació i filtratge sobre el repositori simulat.
 *
 * @author Grup3
 */
@ExtendWith(MockitoExtension.class)
class AgentServiceTest {

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AgentService agentService;
    private AgentEntity agentExemple;

    /**
     * Inicialitza l'entorn de proves abans de l'execució de cada mètode de test:
     * Instància el servei amb els mocks injectats i prepara un AgentEntity base amb data de naixement vàlida.
     */
    @BeforeEach
    void setUp() {
        agentService = new AgentService(passwordEncoder, agentRepository);
        agentExemple = new AgentEntity();
        agentExemple.setDataNaixement(LocalDate.of(1995, 5, 15));
    }

    // ============================================================
    // TESTS: GUARDAR AGENT (DNI, EDAD MÍNIMA Y PASSWORD)
    // ============================================================

    /**
     * Verifica que desar un agent amb un DNI amb lletra de control incorrecta llança una excepció:
     * Comprova que es llança IllegalArgumentException i que no s'invoca el mètode save del repositori.
     */
    @Test
    @DisplayName("guardarAgent con DNI inválido debe lanzar IllegalArgumentException")
    void guardarAgent_DniInvalid_LlancaExcepcio() {
        agentExemple.setDni("12345678A");
        agentExemple.setPassword("1234Pass");

        assertThrows(IllegalArgumentException.class, () -> agentService.guardarAgent(agentExemple));
        verify(agentRepository, never()).save(any());
    }

    /**
     * Comprova que no es permet registrar un agent menor de 18 anys:
     * Valida que s'intercepti l'edat insuficient llançant IllegalArgumentException sense persistir l'entitat.
     */
    @Test
    @DisplayName("guardarAgent menor de 18 años debe lanzar IllegalArgumentException")
    void guardarAgent_MenorDEdat_LlancaExcepcio() {
        agentExemple.setDni("12345678Z");
        agentExemple.setPassword("1234Pass");
        agentExemple.setDataNaixement(LocalDate.now().minusYears(17));

        assertThrows(IllegalArgumentException.class, () -> agentService.guardarAgent(agentExemple));
        verify(agentRepository, never()).save(any());
    }

    /**
     * Verifica que l'absència de data de naixement impedeix desar l'agent:
     * Assegura que el servei llança IllegalArgumentException i evita qualsevol escriptura a la base de dades.
     */
    @Test
    @DisplayName("guardarAgent sin fecha de nacimiento debe lanzar IllegalArgumentException")
    void guardarAgent_DataNaixementNull_LlancaExcepcio() {
        agentExemple.setDni("12345678Z");
        agentExemple.setPassword("1234Pass");
        agentExemple.setDataNaixement(null);

        assertThrows(IllegalArgumentException.class, () -> agentService.guardarAgent(agentExemple));
        verify(agentRepository, never()).save(any());
    }

    /**
     * Comprova el cas límit en què l'agent compleix exactament 18 anys el mateix dia del registre:
     * Verifica que la validació d'edat accepta la data límit i desa l'agent al repositori.
     */
    @Test
    @DisplayName("guardarAgent que cumple 18 años hoy debe permitir guardar")
    void guardarAgent_Just18AnysAvui_GuardaCorrectament() {
        agentExemple.setDni("12345678Z");
        agentExemple.setPassword("1234Pass");
        agentExemple.setDataNaixement(LocalDate.now().minusYears(18));

        when(agentRepository.findById("12345678Z")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("1234Pass")).thenReturn("encoded1234Pass");

        agentService.guardarAgent(agentExemple);

        verify(agentRepository, times(1)).save(agentExemple);
    }

    /**
     * Valida el procés d'alta d'un nou agent xifrant la seva contrasenya abans de desar-lo:
     * Comprova que la clau en text pla es codifica amb PasswordEncoder i es persisteix l'entitat una vegada.
     */
    @Test
    @DisplayName("guardarAgent nuevo con contraseña debe encriptar y llamar a save()")
    void guardarAgent_NouAgentAmbPassword_EncriptaISalva() {
        agentExemple.setDni("12345678Z");
        agentExemple.setPassword("1234Pass");
        when(agentRepository.findById("12345678Z")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("1234Pass")).thenReturn("encoded1234Pass");

        agentService.guardarAgent(agentExemple);

        assertEquals("encoded1234Pass", agentExemple.getPassword());
        verify(agentRepository, times(1)).save(agentExemple);
    }

    /**
     * Comprova que en editar un agent existent amb la contrasenya buida es manté el hash anterior:
     * Verifica que no es torna a invocar el codificador i que l'agent conserva la seva clau original.
     */
    @Test
    @DisplayName("guardarAgent existente sin cambio de contraseña debe conservar la clave original")
    void guardarAgent_ExistentSenseCanviPassword_ConservaPasswordGuardada() {
        AgentEntity agentExistent = new AgentEntity();
        agentExistent.setDni("12345678Z");
        agentExistent.setPassword("encodedOldPassword");

        agentExemple.setDni("12345678Z");
        agentExemple.setPassword("");

        when(agentRepository.findById("12345678Z")).thenReturn(Optional.of(agentExistent));

        agentService.guardarAgent(agentExemple);

        assertEquals("encodedOldPassword", agentExemple.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
        verify(agentRepository, times(1)).save(agentExemple);
    }

    /**
     * Verifica que en actualitzar un agent indicant una nova contrasenya aquesta es torna a xifrar:
     * Assegura que es substitueix el hash antic pel nou resultat codificat abans de desar al repositori.
     */
    @Test
    @DisplayName("guardarAgent existente con nueva contraseña debe encriptarla y guardar")
    void guardarAgent_ExistentAmbNovaPassword_EncriptaNovaPassword() {
        AgentEntity agentExistent = new AgentEntity();
        agentExistent.setDni("12345678Z");
        agentExistent.setPassword("encodedOldPassword");

        agentExemple.setDni("12345678Z");
        agentExemple.setPassword("novaPass123");

        when(agentRepository.findById("12345678Z")).thenReturn(Optional.of(agentExistent));
        when(passwordEncoder.encode("novaPass123")).thenReturn("encodedNovaPass");

        agentService.guardarAgent(agentExemple);

        assertEquals("encodedNovaPass", agentExemple.getPassword());
        verify(agentRepository, times(1)).save(agentExemple);
    }

    // ============================================================
    // TESTS: CONSULTAS
    // ============================================================

    /**
     * Comprova que el mètode de llistat retorna tots els agents obtinguts del repositori:
     * Verifica que la llista retornada no sigui nul·la, tingui la mida esperada i cridi findAll().
     */
    @Test
    @DisplayName("llistarAgents debe retornar la lista completa de agentes")
    void llistarAgents_RetornaLlistaCompleta() {
        List<AgentEntity> agentsEsperats = List.of(agentExemple, new AgentEntity());
        when(agentRepository.findAll()).thenReturn(agentsEsperats);

        List<AgentEntity> resultat = agentService.llistarAgents();

        assertNotNull(resultat);
        assertEquals(2, resultat.size());
        verify(agentRepository, times(1)).findAll();
    }

    /**
     * Verifica la cerca d'un agent per DNI quan el registre existeix a la base de dades:
     * Comprova que l'Optional retornat contingui l'entitat esperada i que s'invoqui findById().
     */
    @Test
    @DisplayName("obtenirPerDni debe retornar Optional con el agente si existe")
    void obtenirPerDni_QuanExisteix_RetornaAgent() {
        String dni = "12345678Z";
        when(agentRepository.findById(dni)).thenReturn(Optional.of(agentExemple));

        Optional<AgentEntity> resultat = agentService.obtenirPerDni(dni);

        assertTrue(resultat.isPresent());
        assertEquals(agentExemple, resultat.get());
        verify(agentRepository, times(1)).findById(dni);
    }

    /**
     * Verifica la cerca d'un agent per DNI quan no hi ha cap coincidència registrada:
     * Comprova que el mètode retorna un Optional buit després de consultar el repositori.
     */
    @Test
    @DisplayName("obtenirPerDni debe retornar Optional vacío si no existe")
    void obtenirPerDni_QuanNoExisteix_RetornaOptionalBuit() {
        String dni = "99999999Z";
        when(agentRepository.findById(dni)).thenReturn(Optional.empty());

        Optional<AgentEntity> resultat = agentService.obtenirPerDni(dni);

        assertFalse(resultat.isPresent());
        verify(agentRepository, times(1)).findById(dni);
    }

    // ============================================================
    // TESTS: ELIMINAR
    // ============================================================

    /**
     * Comprova que l'eliminació d'un agent delega correctament al repositori mitjançant el seu DNI:
     * Verifica que s'invoca el mètode deleteById() exactament una vegada amb l'identificador indicat.
     */
    @Test
    @DisplayName("eliminarAgent debe invocar deleteById() con el DNI proporcionado")
    void eliminarAgent_Exit() {
        String dni = "12345678Z";

        agentService.eliminarAgent(dni);

        verify(agentRepository, times(1)).deleteById(dni);
    }

    // ============================================================
    // TESTS: LISTAR O FILTRAR
    // ============================================================

    /**
     * Verifica que passar un criteri de cerca nul retorna la llista completa d'agents:
     * Assegura que s'executa findAll() i no s'invoca la consulta de filtratge per text.
     */
    @Test
    @DisplayName("llistarOFiltrarAgents con parámetro null debe llamar a findAll()")
    void llistarOFiltrarAgents_CercaNull_CridaFindAll() {
        List<AgentEntity> agentsEsperats = List.of(agentExemple);
        when(agentRepository.findAll()).thenReturn(agentsEsperats);

        List<AgentEntity> resultat = agentService.llistarOFiltrarAgents(null);

        assertEquals(1, resultat.size());
        verify(agentRepository, times(1)).findAll();
        verify(agentRepository, never()).cercarPerDniNomEmail(anyString());
    }

    /**
     * Comprova que una cadena formada únicament per espais en blanc es tracta com a cerca buida:
     * Verifica que s'obté el llistat complet mitjançant findAll() sense executar filtres innecessaris.
     */
    @Test
    @DisplayName("llistarOFiltrarAgents con texto de espacios en blanco debe llamar a findAll()")
    void llistarOFiltrarAgents_CercaEspaisEnBlanc_CridaFindAll() {
        List<AgentEntity> agentsEsperats = List.of(agentExemple);
        when(agentRepository.findAll()).thenReturn(agentsEsperats);

        List<AgentEntity> resultat = agentService.llistarOFiltrarAgents("    ");

        assertEquals(1, resultat.size());
        verify(agentRepository, times(1)).findAll();
        verify(agentRepository, never()).cercarPerDniNomEmail(anyString());
    }

    /**
     * Verifica que una cerca vàlida elimina els espais sobrants als extrems abans de consultar:
     * Comprova que s'invoca cercarPerDniNomEmail() amb el text netejat i s'omet findAll().
     */
    @Test
    @DisplayName("llistarOFiltrarAgents con texto válido debe aplicar trim() y llamar al método de búsqueda")
    void llistarOFiltrarAgents_CercaValida_CridaCercarPerDniNomEmailAmbTrim() {
        String cercaAmbEspais = "  12345678Z  ";
        String cercaNeta = "12345678Z";
        List<AgentEntity> agentsEsperats = List.of(agentExemple);

        when(agentRepository.cercarPerDniNomEmail(cercaNeta)).thenReturn(agentsEsperats);

        List<AgentEntity> resultat = agentService.llistarOFiltrarAgents(cercaAmbEspais);

        assertEquals(1, resultat.size());
        verify(agentRepository, times(1)).cercarPerDniNomEmail(cercaNeta);
        verify(agentRepository, never()).findAll();
    }
}
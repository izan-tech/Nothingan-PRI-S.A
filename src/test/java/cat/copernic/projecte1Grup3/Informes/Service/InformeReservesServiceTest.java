/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Informes.Service;

import cat.copernic.projecte1Grup3.Informes.Entity.EstatHistoric;
import cat.copernic.projecte1Grup3.Informes.Entity.HistoricReserva;
import cat.copernic.projecte1Grup3.Informes.Entity.InformeReserves;
import cat.copernic.projecte1Grup3.Informes.Repository.HistoricReservaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Classe de proves unitàries amb JUnit 5 i Mockito per validar la lògica d'InformeReservesService:
 * Comprova les consultes al repositori històric amb i sense filtre de DNIs, així com la neteja de formats.
 * Verifica també el tractament d'excepcions d'accés a dades i l'exactitud dels càlculs acumulats de l'informe.
 *
 * @author Grup3
 */
@ExtendWith(MockitoExtension.class)
class InformeReservesServiceTest {

    @Mock
    private HistoricReservaRepository repo;

    private InformeReservesService service;

    private HistoricReserva reservaVigent;
    private HistoricReserva reservaCancelada;
    private HistoricReserva reservaEliminada;
    private HistoricReserva reservaSensePreu;

    /**
     * Inicialitza el servei i els objectes simulats d'històric de reserves abans de cada prova:
     * Configura quatre escenaris de reserva (vigent, cancel·lada, eliminada i sense import) amb Mockito.
     */
    @BeforeEach
    void setUp() {
        // Instanciación explícita por constructor para garantizar compatibilidad con Java 23
        service = new InformeReservesService(repo);

        // Reserva vigente (cuenta al total de precio y a totalReserves)
        reservaVigent = mock(HistoricReserva.class);
        lenient().when(reservaVigent.getClientDni()).thenReturn("12345678A");
        lenient().when(reservaVigent.getClientNom()).thenReturn("Joan Pere");
        lenient().when(reservaVigent.getVehicleMatricula()).thenReturn("1234ABC");
        lenient().when(reservaVigent.getVehicleMarca()).thenReturn("Seat");
        lenient().when(reservaVigent.getVehicleModel()).thenReturn("Ibiza");
        lenient().when(reservaVigent.getDataReserva()).thenReturn(LocalDateTime.of(2026, 1, 10, 10, 0));
        lenient().when(reservaVigent.getImportTotal()).thenReturn(new BigDecimal("150.00"));
        lenient().when(reservaVigent.getEstat()).thenReturn(EstatHistoric.CONFIRMADA);

        // Reserva cancelada (cuenta a totalAnullades, pero NO a totalReserves ni a precio)
        reservaCancelada = mock(HistoricReserva.class);
        lenient().when(reservaCancelada.getClientDni()).thenReturn("12345678A");
        lenient().when(reservaCancelada.getClientNom()).thenReturn("Joan Pere");
        lenient().when(reservaCancelada.getVehicleMatricula()).thenReturn("5678DEF");
        lenient().when(reservaCancelada.getVehicleMarca()).thenReturn("Ford");
        lenient().when(reservaCancelada.getVehicleModel()).thenReturn("Focus");
        lenient().when(reservaCancelada.getDataReserva()).thenReturn(LocalDateTime.of(2026, 1, 5, 10, 0));
        lenient().when(reservaCancelada.getImportTotal()).thenReturn(new BigDecimal("80.00"));
        lenient().when(reservaCancelada.getEstat()).thenReturn(EstatHistoric.CANCELADA);

        reservaEliminada = mock(HistoricReserva.class);
        lenient().when(reservaEliminada.getClientDni()).thenReturn("87654321B");
        lenient().when(reservaEliminada.getClientNom()).thenReturn("Maria Garcia");
        lenient().when(reservaEliminada.getVehicleMatricula()).thenReturn("9999XYZ");
        lenient().when(reservaEliminada.getVehicleMarca()).thenReturn("Audi");
        lenient().when(reservaEliminada.getVehicleModel()).thenReturn("A3");
        lenient().when(reservaEliminada.getDataReserva()).thenReturn(LocalDateTime.of(2026, 1, 1, 10, 0));
        lenient().when(reservaEliminada.getImportTotal()).thenReturn(new BigDecimal("200.00"));
        lenient().when(reservaEliminada.getEstat()).thenReturn(EstatHistoric.ELIMINADA);

        // Reserva vigente con importe null
        reservaSensePreu = mock(HistoricReserva.class);
        lenient().when(reservaSensePreu.getClientDni()).thenReturn("12345678A");
        lenient().when(reservaSensePreu.getClientNom()).thenReturn("Joan Pere");
        lenient().when(reservaSensePreu.getVehicleMatricula()).thenReturn("1234ABC");
        lenient().when(reservaSensePreu.getVehicleMarca()).thenReturn("Seat");
        lenient().when(reservaSensePreu.getVehicleModel()).thenReturn("Ibiza");
        lenient().when(reservaSensePreu.getDataReserva()).thenReturn(LocalDateTime.of(2026, 1, 12, 10, 0));
        lenient().when(reservaSensePreu.getImportTotal()).thenReturn(null);
        lenient().when(reservaSensePreu.getEstat()).thenReturn(EstatHistoric.CONFIRMADA);
    }

    // ============================================================
    // TESTS: FILTRADO Y CONSULTA AL REPOSITORIO
    // ============================================================

    /**
     * Comprova que generar un informe sense indicar DNIs consulta tot l'històric ordenat:
     * Verifica que s'invoca findAll(Sort) i no s'executa la cerca filtrada per llista de clients.
     */
    @Test
    @DisplayName("generarInforme sin DNI o con lista vacía debe llamar a findAll(ORDRE)")
    void generarInforme_SenseDnis_CridaFindAll() throws InformeException {
        when(repo.findAll(any(Sort.class))).thenReturn(List.of(reservaVigent));

        InformeReserves informe = service.generarInforme(null);

        assertNotNull(informe);
        assertEquals(1, informe.files().size());
        verify(repo, times(1)).findAll(any(Sort.class));
        verify(repo, never()).findByClientDniIn(any(), any());
    }

    /**
     * Valida la normalització de DNIs abans de consultar el repositori històric:
     * Comprova que elimina espais, converteix a majúscules, deduplica els valors i crida findByClientDniIn().
     */
    @Test
    @DisplayName("generarInforme con DNIs válidos debe limpiar, pasar a mayúsculas y llamar a findByClientDniIn")
    void generarInforme_AmbDnisValids_FiltraPerDnisNets() throws InformeException {
        List<String> dnisBruts = List.of(" 12345678a ", "12345678A");
        when(repo.findByClientDniIn(eq(List.of("12345678A")), any(Sort.class)))
                .thenReturn(List.of(reservaVigent));

        InformeReserves informe = service.generarInforme(dnisBruts);

        assertNotNull(informe);
        verify(repo, times(1)).findByClientDniIn(eq(List.of("12345678A")), any(Sort.class));
        verify(repo, never()).findAll(any(Sort.class));
    }

    // ============================================================
    // TESTS: VALIDACIÓN DE FORMATO Y EXCEPCIONES
    // ============================================================

    /**
     * Comprova que s'intercepta un DNI amb format incorrecte abans d'accedir a la base de dades:
     * Verifica el llançament d'IllegalArgumentException i l'absència total d'interaccions amb el repositori.
     */
    @Test
    @DisplayName("generarInforme con formato de DNI inválido debe lanzar IllegalArgumentException")
    void generarInforme_DniFormatInvalid_LlancaIllegalArgumentException() {
        // "123" tiene longitud < 5 y no cumple la Regex ^[A-Za-z0-9]{5,15}$
        List<String> dnisInvalids = List.of("123");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.generarInforme(dnisInvalids)
        );

        assertTrue(ex.getMessage().contains("no té un format vàlid"));
        verifyNoInteractions(repo);
    }

    /**
     * Verifica la conversió d'excepcions tècniques de persistència en una excepció de domini InformeException:
     * Simula una fallada de lectura a Spring Data i comprova el missatge descriptiu resultant.
     */
    @Test
    @DisplayName("generarInforme cuando falla la BD debe capturar DataAccessException y lanzar InformeException")
    void generarInforme_ErrorBD_LlancaInformeException() {
        when(repo.findAll(any(Sort.class)))
                .thenThrow(new DataRetrievalFailureException("Error de conexión"));

        InformeException ex = assertThrows(
                InformeException.class,
                () -> service.generarInforme(null)
        );

        assertTrue(ex.getMessage().contains("No s'ha pogut consultar l'històric de reserves"));
    }

    // ============================================================
    // TESTS: CÁLCULOS DE TOTALES
    // ============================================================

    /**
     * Comprova l'exactitud dels càlculs agregats del registre InformeReserves amb diversos estats:
     * Verifica el recompte de files totals, reserves vigents, import acumulat sense errors per valors nuls i anul·lacions.
     */
    @Test
    @DisplayName("generarInforme debe calcular correctamente los totales del record InformeReserves")
    void generarInforme_CalculsCorrectes() throws InformeException {
        List<HistoricReserva> llistaHistoric = List.of(
                reservaVigent,
                reservaCancelada,
                reservaEliminada,
                reservaSensePreu
        );

        when(repo.findAll(any(Sort.class))).thenReturn(llistaHistoric);

        InformeReserves informe = service.generarInforme(null);

        assertNotNull(informe);
        assertEquals(4, informe.files().size());
        assertEquals(2, informe.totalReserves());
        assertEquals(new BigDecimal("150.00"), informe.preuTotal());
        assertEquals(1, informe.totalAnullades());
    }
}
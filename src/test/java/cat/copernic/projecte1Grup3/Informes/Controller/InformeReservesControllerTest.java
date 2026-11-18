/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Informes.Controller;

import cat.copernic.projecte1Grup3.Informes.Entity.InformeReserves;
import cat.copernic.projecte1Grup3.Informes.Service.InformeException;
import cat.copernic.projecte1Grup3.Informes.Service.InformeReservesService;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.ClientEntity;
import cat.copernic.projecte1Grup3.Usuaris.Client.Service.ClientService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Classe de proves unitàries amb JUnit 5 i Mockito per verificar el
 * comportament d'InformeReservesController: Comprova la càrrega de la vista
 * d'informes, el llistat de clients validats i el filtratge per llista de DNIs.
 * Valida també la captura d'excepcions de negoci (IllegalArgumentException i
 * InformeException) traslladant-les al Model.
 *
 * @author Grup3
 */
@ExtendWith(MockitoExtension.class)
class InformeReservesControllerTest {

    @Mock
    private InformeReservesService informeService;

    @Mock
    private ClientService clientLogic;

    @Mock
    private Model model;

    private InformeReservesController controller;

    private List<ClientEntity> llistaClientsValidats;
    private InformeReserves informeMock;

    /**
     * Prepara l'escenari de proves abans de cada mètode de test: Instància
     * explícitament el controlador amb els serveis simulats i configura la
     * resposta base de clients validats.
     */
    @BeforeEach
    void setUp() {
        // Instanciación explícita por constructor para Java 23
        controller = new InformeReservesController(informeService, clientLogic);

        llistaClientsValidats = List.of(new ClientEntity());
        informeMock = mock(InformeReserves.class);

        when(clientLogic.llistar(null, null, "VALIDAT", null, null)).thenReturn(llistaClientsValidats);
    }

    // ============================================================
    // TESTS: RUTA Y CARGA DE MODELO
    // ============================================================
    /**
     * Comprova la càrrega de l'informe general quan no s'aplica cap filtre de
     * DNIs: Verifica que retorna la vista esperada i afegeix al model els
     * clients validats, una llista buida de selecció i l'informe.
     */
    @Test
    @DisplayName("informeReserves cuando dnis es null debe cargar clientes validados, dnis vacíos y el informe completo")
    void informeReserves_DnisNull_RetornaVistaIModelComplet() throws InformeException {
        when(informeService.generarInforme(null)).thenReturn(informeMock);

        String vista = controller.informeReserves(null, model);

        assertEquals("informes/informe_reserves", vista);
        verify(clientLogic, times(1)).llistar(null, null, "VALIDAT", null, null);
        verify(model, times(1)).addAttribute("clients", llistaClientsValidats);
        verify(model, times(1)).addAttribute("dnisSeleccionats", List.of());
        verify(model, times(1)).addAttribute("informe", informeMock);
        verify(model, never()).addAttribute(eq("error"), any());
    }

    /**
     * Verifica que en passar una llista de DNIs al controlador es manté la
     * selecció i es filtra l'informe: Comprova que els identificadors es
     * traslladen tant al servei generador com als atributs de la vista.
     */
    @Test
    @DisplayName("informeReserves cuando se pasan DNIs debe mantener la selección y cargar el informe filtrado")
    void informeReserves_AmbDnis_PassaDnisAlServeiIModel() throws InformeException {
        List<String> dnis = List.of("12345678A", "87654321B");
        when(informeService.generarInforme(dnis)).thenReturn(informeMock);

        String vista = controller.informeReserves(dnis, model);

        assertEquals("informes/informe_reserves", vista);
        verify(model, times(1)).addAttribute("clients", llistaClientsValidats);
        verify(model, times(1)).addAttribute("dnisSeleccionats", dnis);
        verify(informeService, times(1)).generarInforme(dnis);
        verify(model, times(1)).addAttribute("informe", informeMock);
    }

    // ============================================================
    // TESTS: GESTIÓN DE EXCEPCIONES EN EL CONTROLADOR
    // ============================================================
    /**
     * Comprova que el controlador captura IllegalArgumentException quan es
     * proporcionen DNIs invàlids: Verifica que el missatge d'error s'afegeix al
     * model i no es carrega cap objecte d'informe.
     */
    @Test
    @DisplayName("informeReserves cuando el servicio lanza IllegalArgumentException debe capturarla y añadir el error al Model")
    void informeReserves_IllegalArgumentException_AfegeixErrorAlModel() throws InformeException {
        List<String> dnis = List.of("123");
        String missatgeError = "El DNI '123' no té un format vàlid.";

        when(informeService.generarInforme(dnis)).thenThrow(new IllegalArgumentException(missatgeError));

        String vista = controller.informeReserves(dnis, model);

        assertEquals("informes/informe_reserves", vista);
        verify(model, times(1)).addAttribute("clients", llistaClientsValidats);
        verify(model, times(1)).addAttribute("dnisSeleccionats", dnis);
        verify(model, times(1)).addAttribute("error", missatgeError);
        verify(model, never()).addAttribute(eq("informe"), any());
    }

    /**
     * Verifica el tractament d'errors tècnics o de consulta quan el servei
     * llança InformeException: Comprova que l'excepció queda interceptada i es
     * mostra el missatge descriptiu a la mateixa vista d'informes.
     */
    @Test
    @DisplayName("informeReserves cuando el servicio lanza InformeException debe capturarla y añadir el error al Model")
    void informeReserves_InformeException_AfegeixErrorAlModel() throws InformeException {
        String missatgeError = "No s'ha pogut consultar l'històric de reserves.";

        when(informeService.generarInforme(null)).thenThrow(new InformeException(missatgeError));

        String vista = controller.informeReserves(null, model);

        assertEquals("informes/informe_reserves", vista);
        verify(model, times(1)).addAttribute("clients", llistaClientsValidats);
        verify(model, times(1)).addAttribute("dnisSeleccionats", List.of());
        verify(model, times(1)).addAttribute("error", missatgeError);
        verify(model, never()).addAttribute(eq("informe"), any());
    }
}

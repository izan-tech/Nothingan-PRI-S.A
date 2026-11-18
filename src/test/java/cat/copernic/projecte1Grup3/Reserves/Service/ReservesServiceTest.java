/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Reserves.Service;

import cat.copernic.projecte1Grup3.Informes.Entity.EstatHistoric;
import cat.copernic.projecte1Grup3.Informes.Service.HistoricService;
import cat.copernic.projecte1Grup3.Reserves.Entity.ReservesEntity;
import cat.copernic.projecte1Grup3.Reserves.Repository.ReservesRepository;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.ClientRepository;
import cat.copernic.projecte1Grup3.Vehicles.Repository.VehiclesRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Classe de proves unitàries amb JUnit 5 i Mockito per validar la lògica d'anul·lació de ReservesService:
 * Comprova la validació obligatòria del motiu d'anul·lació abans d'executar canvis a la base de dades.
 * Verifica també el canvi d'estat de la reserva, el desament del motiu netejat i el registre a l'històric.
 *
 * @author Grup3
 */
@ExtendWith(MockitoExtension.class)
class ReservesServiceTest {

    @Mock
    private ReservesRepository reservesRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private VehiclesRepository vehiclesRepository;

    @Mock
    private HistoricService historicService;

    private ReservesService service;

    /**
     * Inicialitza la instància de ReservesService amb tots els repositoris i serveis simulats:
     * S'executa abans de cada prova unitària per garantir un entorn d'execució aïllat i net.
     */
    @BeforeEach
    void setUp() {
        service = new ReservesService(
                reservesRepository,
                clientRepository,
                vehiclesRepository,
                historicService);
    }

    /**
     * Comprova que l'anul·lació d'una reserva rebutja un motiu buit o format únicament per espais:
     * Verifica que es llança IllegalArgumentException i no es produeix cap interacció amb els repositoris ni l'històric.
     */
    @Test
    void anularReservaRequereixMotiu() {
        assertThrows(IllegalArgumentException.class,
                () -> service.anularReserva(7L, "  "));

        verifyNoInteractions(reservesRepository, historicService);
    }

    /**
     * Verifica el procés complet d'anul·lació d'una reserva existent quan s'indica un motiu vàlid:
     * Comprova que l'estat passa a CANCELADA, es desa el motiu sense espais sobrants i s'actualitza l'històric.
     */
    @Test
    void anularReservaDesaMotiuIActualitzaHistoric() {
        ReservesEntity reserva = new ReservesEntity();
        reserva.setCodiReserva(7L);
        reserva.setEstat(ReservesService.PENDENT);
        when(reservesRepository.findById(7L)).thenReturn(Optional.of(reserva));
        when(reservesRepository.save(reserva)).thenReturn(reserva);

        service.anularReserva(7L, "  Error en la reserva  ");

        assertEquals(ReservesService.CANCELADA, reserva.getEstat());
        assertEquals("Error en la reserva", reserva.getAnulacioMotiu());
        verify(reservesRepository).save(reserva);
        verify(historicService).desar(reserva, EstatHistoric.CANCELADA);
    }

    @Test
    void calcularRetornFiancaAplicaElPercentatgeSeleccionat() {
        ReservesEntity reserva = new ReservesEntity();
        reserva.setFianca(new BigDecimal("100.55"));

        assertEquals(new BigDecimal("40.22"),
                service.calcularRetornFianca(reserva, new BigDecimal("40")));
    }
}
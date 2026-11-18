package cat.copernic.projecte1Grup3.Reserves.Service;

import cat.copernic.projecte1Grup3.Reserves.Entity.ReservesEntity;
import cat.copernic.projecte1Grup3.Reserves.Repository.ReservesRepository;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.ClientEntity;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.ClientRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservesClientServiceTest {

    @Mock
    private ReservesRepository reservesRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ReservesService reservesService;

    private ReservesClientService service;

    @BeforeEach
    void setUp() {
        ReservaAnulacioEmailService emailService = new ReservaAnulacioEmailService(
                org.mockito.Mockito.mock(JavaMailSender.class), "no-reply@example.com");
        service = new ReservesClientService(
                reservesRepository, clientRepository, reservesService, emailService);
    }

    @Test
    void clientPotAnullarReservaIObteDevolucioAmbFianca() {
        ClientEntity client = client("12345678Z");
        ReservesEntity reserva = reserva(client);
        when(clientRepository.findById("MARIA@EXAMPLE.COM")).thenReturn(Optional.empty());
        when(clientRepository.findByEmailIgnoreCase("maria@example.com")).thenReturn(Optional.of(client));
        when(reservesRepository.findById(7L)).thenReturn(Optional.of(reserva));
        when(reservesService.anularReserva(7L, "Cancel·lada pel client"))
                .thenAnswer(invocation -> {
                    reserva.setEstat(ReservesService.CANCELADA);
                    return reserva;
                });

        ReservesClientService.ResultatAnulacio resultat
                = service.anularReservaDelClient(7L, "maria@example.com");

        assertEquals(new BigDecimal("180.00"), resultat.devolucio().importDevolucio());
        assertEquals(ReservesService.CANCELADA, resultat.reserva().getEstat());
        verify(reservesService).anularReserva(7L, "Cancel·lada pel client");
    }

    @Test
    void clientNoPotAnullarLaReservaDUnAltreClient() {
        ClientEntity clientAutenticat = client("12345678Z");
        ClientEntity propietariReserva = client("87654321X");
        ReservesEntity reserva = reserva(propietariReserva);
        when(clientRepository.findById("MARIA@EXAMPLE.COM")).thenReturn(Optional.empty());
        when(clientRepository.findByEmailIgnoreCase("maria@example.com"))
                .thenReturn(Optional.of(clientAutenticat));
        when(reservesRepository.findById(7L)).thenReturn(Optional.of(reserva));

        assertThrows(IllegalArgumentException.class,
                () -> service.anularReservaDelClient(7L, "maria@example.com"));
        verifyNoInteractions(reservesService);
    }

    private ClientEntity client(String dni) {
        ClientEntity client = new ClientEntity();
        client.setDni(dni);
        client.setNom("Maria");
        client.setEmail("maria@example.com");
        return client;
    }

    private ReservesEntity reserva(ClientEntity client) {
        ReservesEntity reserva = new ReservesEntity();
        reserva.setCodiReserva(7L);
        reserva.setClient(client);
        reserva.setEstat(ReservesService.CONFIRMADA);
        reserva.setDataInici(LocalDate.now().plusDays(3));
        reserva.setHoraInici(LocalTime.now());
        reserva.setCostTotal(new BigDecimal("80.00"));
        reserva.setFianca(new BigDecimal("100.00"));
        return reserva;
    }
}

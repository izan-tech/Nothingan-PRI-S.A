package cat.copernic.projecte1Grup3.Reserves.Service;

import cat.copernic.projecte1Grup3.Reserves.Entity.ReservesEntity;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.ClientEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class ReservaAnulacioEmailServiceTest {

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final ReservaAnulacioEmailService service
            = new ReservaAnulacioEmailService(mailSender, "no-reply@example.com");

    @Test
    void enviarNotificacioInclouElsImportsCalculatsAmbElPercentatgeSeleccionat() {
        ClientEntity client = new ClientEntity();
        client.setNom("Maria");
        client.setEmail("maria@example.com");

        ReservesEntity reserva = new ReservesEntity();
        reserva.setCodiReserva(42L);
        reserva.setClient(client);
        reserva.setCostTotal(new BigDecimal("80.00"));
        reserva.setFianca(new BigDecimal("100.55"));
        reserva.setAnulacioMotiu("Canvi de plans");

        service.enviarNotificacio(
                reserva, new BigDecimal("40"), new BigDecimal("20.00"), new BigDecimal("40.22"));

        ArgumentCaptor<SimpleMailMessage> captor
                = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage missatge = captor.getValue();
        assertArrayEquals(
                new String[]{"maria@example.com"}, missatge.getTo());
        assertEquals(
                "Anul·lació de la reserva #42", missatge.getSubject());
        assertTrue(missatge.getText().contains("Fiança (40%): 40.22 €"));
        assertTrue(missatge.getText().contains("Total: 60.22 €"));
        assertTrue(
                missatge.getText().contains("no es tramita automàticament"));
    }

    @Test
    void enviarNotificacioArrodoneixElsImportsACentims() {
        ClientEntity client = new ClientEntity();
        client.setNom("Maria");
        client.setEmail("maria@example.com");

        ReservesEntity reserva = new ReservesEntity();
        reserva.setCodiReserva(43L);
        reserva.setClient(client);
        reserva.setCostTotal(new BigDecimal("0.02"));

        service.enviarNotificacio(
                reserva, BigDecimal.ZERO, new BigDecimal("0.01"), BigDecimal.ZERO);

        ArgumentCaptor<SimpleMailMessage> captor
                = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertTrue(
                captor.getValue().getText().contains("0.01 €"));
    }

    @Test
    void enviarNotificacioRebutjaReservaSenseCorreu() {
        ClientEntity client = new ClientEntity();
        client.setNom("Maria");
        client.setEmail(" ");

        ReservesEntity reserva = new ReservesEntity();
        reserva.setClient(client);
        reserva.setCostTotal(new BigDecimal("80.00"));

        assertThrows(IllegalStateException.class,
                () -> service.enviarNotificacio(
                        reserva, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        verifyNoInteractions(mailSender);
    }

    @Test
    void calcularDevolucioClientAmb48HInclouCostIFianca() {
        ReservesEntity reserva = reservaClient(LocalDateTime.of(2026, 6, 5, 12, 0));

        ReservaAnulacioEmailService.DevolucioClient devolucio
                = service.calcularDevolucioClient(
                        reserva, LocalDateTime.of(2026, 6, 3, 12, 0));

        assertTrue(devolucio.dinsTermini());
        assertEquals(new BigDecimal("180.65"), devolucio.importDevolucio());
    }

    @Test
    void calcularDevolucioClientAmbMenysDe48HNoRetornaRes() {
        ReservesEntity reserva = reservaClient(LocalDateTime.of(2026, 6, 5, 12, 0));

        ReservaAnulacioEmailService.DevolucioClient devolucio
                = service.calcularDevolucioClient(
                        reserva, LocalDateTime.of(2026, 6, 3, 12, 1));

        assertTrue(!devolucio.dinsTermini());
        assertEquals(new BigDecimal("0.00"), devolucio.importDevolucio());
    }

    @Test
    void enviarNotificacioClientIndicaImportCompletOZero() {
        ClientEntity client = new ClientEntity();
        client.setNom("Maria");
        client.setEmail("maria@example.com");
        ReservesEntity reserva = new ReservesEntity();
        reserva.setCodiReserva(44L);
        reserva.setClient(client);

        service.enviarNotificacioClient(
                reserva, new ReservaAnulacioEmailService.DevolucioClient(
                        new BigDecimal("180.65"), true));
        service.enviarNotificacioClient(
                reserva, new ReservaAnulacioEmailService.DevolucioClient(
                        new BigDecimal("0.00"), false));

        ArgumentCaptor<SimpleMailMessage> captor
                = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, org.mockito.Mockito.times(2)).send(captor.capture());
        assertTrue(captor.getAllValues().get(0).getText().contains("180.65 €"));
        assertTrue(captor.getAllValues().get(0).getText().contains("incloent el cost del lloguer i la fiança"));
        assertTrue(captor.getAllValues().get(1).getText().contains("0,00 €"));
        assertTrue(captor.getAllValues().get(1).getText().contains("menys de 2 dies"));
    }

    private ReservesEntity reservaClient(LocalDateTime dataInici) {
        ReservesEntity reserva = new ReservesEntity();
        reserva.setDataInici(dataInici.toLocalDate());
        reserva.setHoraInici(dataInici.toLocalTime());
        reserva.setCostTotal(new BigDecimal("80.10"));
        reserva.setFianca(new BigDecimal("100.55"));
        return reserva;
    }
}

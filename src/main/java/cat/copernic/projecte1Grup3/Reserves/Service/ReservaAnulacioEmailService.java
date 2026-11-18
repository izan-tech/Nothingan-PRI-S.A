package cat.copernic.projecte1Grup3.Reserves.Service;

import cat.copernic.projecte1Grup3.Reserves.Entity.ReservesEntity;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.ClientEntity;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class ReservaAnulacioEmailService {

    private static final BigDecimal IMPORT_ZERO = new BigDecimal("0.00");

    public record DevolucioClient(BigDecimal importDevolucio, boolean dinsTermini) {
    }

    private final JavaMailSender mailSender;
    private final String remitent;

    public ReservaAnulacioEmailService(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String remitent) {
        this.mailSender = mailSender;
        this.remitent = remitent;
    }

    public void enviarNotificacio(
            ReservesEntity reserva,
            BigDecimal percentatgeFianca,
            BigDecimal retornLloguer,
            BigDecimal retornFianca) {
        ClientEntity client = validarReservaAmbClient(reserva);
        if (percentatgeFianca == null || percentatgeFianca.signum() < 0
                || percentatgeFianca.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("El percentatge de fiança no és vàlid.");
        }
        if (retornLloguer == null || retornLloguer.signum() < 0
                || retornFianca == null || retornFianca.signum() < 0) {
            throw new IllegalArgumentException("Els imports de devolució no són vàlids.");
        }
        BigDecimal importTotal = retornLloguer.add(retornFianca)
                .setScale(2, RoundingMode.HALF_UP);

        SimpleMailMessage missatge = new SimpleMailMessage();
        missatge.setFrom(remitent);
        missatge.setTo(client.getEmail());
        missatge.setSubject("Anul·lació de la reserva #" + reserva.getCodiReserva());
        missatge.setText("Hola " + client.getNom() + ",\n\n"
                + "La reserva #" + reserva.getCodiReserva() + " ha estat anul·lada.\n"
                + "Imports calculats per a la devolució:\n"
                + "- Cost del lloguer: "
                + retornLloguer.setScale(2, RoundingMode.HALF_UP).toPlainString() + " €.\n"
                + "- Fiança (" + percentatgeFianca.stripTrailingZeros().toPlainString() + "%): "
                + retornFianca.setScale(2, RoundingMode.HALF_UP).toPlainString() + " €.\n"
                + "- Total: " + importTotal.toPlainString() + " €.\n\n"
                + "Aquest correu és informatiu: el reemborsament no es tramita automàticament "
                + "des del sistema.\n\n"
                + "Motiu de l'anul·lació: " + reserva.getAnulacioMotiu() + "\n\n"
                + "Atentament,\nNOTHINGAN PRI S.A");
        mailSender.send(missatge);
    }

    public DevolucioClient calcularDevolucioClient(
            ReservesEntity reserva, LocalDateTime instantAnulacio) {
        if (reserva == null || instantAnulacio == null
                || reserva.getDataInici() == null || reserva.getHoraInici() == null) {
            throw new IllegalStateException("Falten dades per calcular la devolució de la reserva.");
        }
        if (reserva.getCostTotal() == null || reserva.getFianca() == null) {
            throw new IllegalStateException("La reserva no té el cost i la fiança necessaris per calcular la devolució.");
        }

        LocalDateTime iniciReserva = LocalDateTime.of(reserva.getDataInici(), reserva.getHoraInici());
        if (iniciReserva.isBefore(instantAnulacio.plusDays(2))) {
            return new DevolucioClient(IMPORT_ZERO, false);
        }

        BigDecimal importDevolucio = reserva.getCostTotal().add(reserva.getFianca())
                .setScale(2, RoundingMode.HALF_UP);
        return new DevolucioClient(importDevolucio, true);
    }

    public void enviarNotificacioClient(ReservesEntity reserva, DevolucioClient devolucio) {
        ClientEntity client = validarReservaAmbClient(reserva);
        if (devolucio == null) {
            throw new IllegalArgumentException("La devolució no és vàlida.");
        }
        BigDecimal importDevolucio = devolucio.importDevolucio();
        if (importDevolucio == null || importDevolucio.signum() < 0) {
            throw new IllegalArgumentException("L'import de devolució no és vàlid.");
        }

        String detallDevolucio = devolucio.dinsTermini()
                ? "Com que has anul·lat la reserva amb almenys 2 dies d'antelació, "
                        + "se't retornaran " + importDevolucio.setScale(2, RoundingMode.HALF_UP).toPlainString()
                        + " €, incloent el cost del lloguer i la fiança."
                : "Com que has anul·lat la reserva amb menys de 2 dies d'antelació, "
                        + "no et correspon cap devolució (0,00 €).";

        SimpleMailMessage missatge = new SimpleMailMessage();
        missatge.setFrom(remitent);
        missatge.setTo(client.getEmail());
        missatge.setSubject("Anul·lació de la reserva #" + reserva.getCodiReserva());
        missatge.setText("Hola " + client.getNom() + ",\n\n"
                + "La reserva #" + reserva.getCodiReserva() + " ha estat anul·lada.\n"
                + detallDevolucio + "\n\n"
                + "Aquest correu informa de l'import previst; el pagament no es tramita automàticament "
                + "des del sistema.\n\n"
                + "Atentament,\nNOTHINGAN PRI S.A");
        mailSender.send(missatge);
    }

    private ClientEntity validarReservaAmbClient(ReservesEntity reserva) {
        if (reserva == null || reserva.getClient() == null) {
            throw new IllegalStateException("La reserva no té cap client associat.");
        }

        ClientEntity client = reserva.getClient();
        if (client.getEmail() == null || client.getEmail().isBlank()) {
            throw new IllegalStateException("El client no té cap adreça de correu electrònic.");
        }
        return client;
    }
}

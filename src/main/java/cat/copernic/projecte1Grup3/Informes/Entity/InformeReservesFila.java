/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Informes.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Registre immutable (Record) que representa una fila individual de l'informe
 * de reserves: Agrupa les dades formatades de cada operació (data, vehicle,
 * client, import i estat) per mostrar-les a la taula de la vista. Facilita el
 * transport de cada línia de detall des del servei d'informes fins a la
 * plantilla Thymeleaf.
 *
 * @author Grup3
 */
public record InformeReservesFila(
        LocalDateTime data,
        String vehicle, // "1234ABC - Seat Ibiza"
        String client, // "Maria Pons (12345678Z)"
        BigDecimal importReserva,
        EstatHistoric estat) {

}

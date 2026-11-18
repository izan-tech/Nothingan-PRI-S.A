/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Informes.Entity;

import java.math.BigDecimal;
import java.util.List;

/**
 * Registre immutable (Record) que encapsula el resultat complet d'un informe de
 * reserves: Agrupa el detall de cada fila juntament amb els totals agregats de
 * reserves actives, import econòmic acumulat i reserves anul·lades. S'utilitza
 * per transferir de manera compacta i segura tota la informació calculada cap a
 * la vista.
 *
 * @author Grup3
 */
public record InformeReserves(
        List<InformeReservesFila> files,
        int totalReserves,
        BigDecimal preuTotal,
        int totalAnullades) {

}

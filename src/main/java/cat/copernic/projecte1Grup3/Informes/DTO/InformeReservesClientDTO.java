/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Informes.DTO;

import cat.copernic.projecte1Grup3.Reserves.Entity.ReservesEntity;
import java.math.BigDecimal;
import java.util.List;

/**
 * Objecte de transferència de dades (DTO) per a l'informe de reserves per
 * client (RF91): Encapsula la llista de reserves filtrades juntament amb el
 * recompte total i l'import econòmic acumulat. Facilita el transport d'aquesta
 * informació resumida des del servei fins a la vista de l'informe.
 *
 * @author Grup3
 */
public class InformeReservesClientDTO {

    // Llista de les reserves filtrades
    private List<ReservesEntity> reserves;

    // Recompte total de reserves trobades
    private long totalReserves;

    // Suma del cost total de totes les reserves filtrades
    private BigDecimal preuTotal;

    /**
     * Constructor per defecte sense paràmetres: Crea una instància buida del
     * DTO per permetre la seva inicialització posterior mitjançant setters.
     */
    public InformeReservesClientDTO() {
    }

    /**
     * Constructor complet per inicialitzar totes les dades de l'informe:
     * Assigna directament la llista de reserves filtrades, el recompte total i
     * la suma econòmica en crear l'objecte.
     */
    public InformeReservesClientDTO(List<ReservesEntity> reserves, long totalReserves, BigDecimal preuTotal) {
        this.reserves = reserves;
        this.totalReserves = totalReserves;
        this.preuTotal = preuTotal;
    }

    // ============================================================
    // GETTERS I SETTERS
    // ============================================================
    /**
     * Obté la llista de reserves de l'informe: Retorna el conjunt d'entitats
     * ReservesEntity que compleixen els criteris de filtratge aplicats.
     */
    public List<ReservesEntity> getReserves() {
        return reserves;
    }

    /**
     * Assigna la llista de reserves a l'informe: Estableix el llistat de
     * reserves filtrades que es mostraran al detall de l'informe.
     */
    public void setReserves(List<ReservesEntity> reserves) {
        this.reserves = reserves;
    }

    /**
     * Obté el nombre total de reserves: Retorna el recompte global d'operacions
     * incloses dins d'aquest informe.
     */
    public long getTotalReserves() {
        return totalReserves;
    }

    /**
     * Assigna el nombre total de reserves: Desa la quantitat total de reserves
     * comptabilitzades per al resum de l'informe.
     */
    public void setTotalReserves(long totalReserves) {
        this.totalReserves = totalReserves;
    }

    /**
     * Obté l'import econòmic total de les reserves: Retorna la suma acumulada
     * del cost de totes les reserves filtrades en format BigDecimal.
     */
    public BigDecimal getPreuTotal() {
        return preuTotal;
    }

    /**
     * Assigna l'import econòmic total de les reserves: Estableix el valor de la
     * suma total dels imports per mostrar-lo al resum econòmic.
     */
    public void setPreuTotal(BigDecimal preuTotal) {
        this.preuTotal = preuTotal;
    }
}

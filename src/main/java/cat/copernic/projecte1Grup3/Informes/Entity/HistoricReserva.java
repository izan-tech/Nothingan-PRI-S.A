/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Informes.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Document MongoDB per emmagatzemar l'històric de reserves: Desa una còpia
 * desnormalitzada amb les dades principals del client, el vehicle, les dates i
 * l'import del lloguer. Permet generar informes històrics i auditories sense
 * dependre de si les entitats originals s'han modificat o esborrat.
 *
 * @author Grup3
 */
@Document(collection = "historic_reserves")
public class HistoricReserva {

    @Id
    private Long idReserva;

    private LocalDateTime dataReserva; // data de creació de la reserva
    private LocalDate dataInici;
    private LocalDate dataFi;

    @Indexed
    private String clientDni;
    private String clientNom;

    private String vehicleMatricula;
    private String vehicleMarca;
    private String vehicleModel;

    private BigDecimal importTotal; // només lloguer, sense fiança
    private EstatHistoric estat;
    private LocalDateTime dataUltimEvent;

    /**
     * Constructor per defecte sense paràmetres: Crea una instància buida del
     * document HistoricReserva requerida per Spring Data MongoDB per
     * reconstruir els objectes.
     */
    public HistoricReserva() {
    }

    /**
     * Obté l'identificador únic de la reserva: Retorna la clau primària
     * (idReserva) amb què es va registrar l'operació al sistema.
     */
    public Long getIdReserva() {
        return idReserva;
    }

    /**
     * Assigna l'identificador únic de la reserva: Estableix l'ID de la reserva
     * per vincular el registre històric amb l'operació original.
     */
    public void setIdReserva(Long idReserva) {
        this.idReserva = idReserva;
    }

    /**
     * Obté la data i hora de creació de la reserva: Retorna el moment exacte en
     * què es va donar d'alta la sol·licitud de lloguer.
     */
    public LocalDateTime getDataReserva() {
        return dataReserva;
    }

    /**
     * Assigna la data i hora de creació de la reserva: Desa la marca temporal
     * del moment en què el client o agent va registrar la reserva.
     */
    public void setDataReserva(LocalDateTime dataReserva) {
        this.dataReserva = dataReserva;
    }

    /**
     * Obté la data d'inici del lloguer: Retorna el dia previst per al
     * lliurament del vehicle al client.
     */
    public LocalDate getDataInici() {
        return dataInici;
    }

    /**
     * Assigna la data d'inici del lloguer: Estableix el dia en què comença el
     * període de reserva del vehicle.
     */
    public void setDataInici(LocalDate dataInici) {
        this.dataInici = dataInici;
    }

    /**
     * Obté la data de finalització del lloguer: Retorna el dia previst per al
     * retorn del vehicle per part del client.
     */
    public LocalDate getDataFi() {
        return dataFi;
    }

    /**
     * Assigna la data de finalització del lloguer: Estableix el dia en què
     * conclou el període de lloguer contractat.
     */
    public void setDataFi(LocalDate dataFi) {
        this.dataFi = dataFi;
    }

    /**
     * Obté el DNI del client associat a la reserva: Retorna el document
     * identificatiu indexat a MongoDB per facilitar les consultes per client.
     */
    public String getClientDni() {
        return clientDni;
    }

    /**
     * Assigna el DNI del client associat a la reserva: Desa l'identificador
     * fiscal del client dins del registre històric.
     */
    public void setClientDni(String clientDni) {
        this.clientDni = clientDni;
    }

    /**
     * Obté el nom complet del client: Retorna el nom desat en el moment de la
     * reserva per conservar la traçabilitat històrica.
     */
    public String getClientNom() {
        return clientNom;
    }

    /**
     * Assigna el nom complet del client: Estableix el nom del titular de la
     * reserva dins del document històric.
     */
    public void setClientNom(String clientNom) {
        this.clientNom = clientNom;
    }

    /**
     * Obté la matrícula del vehicle reservat: Retorna la placa identificativa
     * del vehicle assignat a l'operació.
     */
    public String getVehicleMatricula() {
        return vehicleMatricula;
    }

    /**
     * Assigna la matrícula del vehicle reservat: Desa la matrícula del vehicle
     * dins del registre històric de MongoDB.
     */
    public void setVehicleMatricula(String v) {
        this.vehicleMatricula = v;
    }

    /**
     * Obté la marca del vehicle reservat: Retorna el fabricant del vehicle
     * contractat (p. ex. Seat, Toyota).
     */
    public String getVehicleMarca() {
        return vehicleMarca;
    }

    /**
     * Assigna la marca del vehicle reservat: Estableix el fabricant del vehicle
     * per als llistats d'informes.
     */
    public void setVehicleMarca(String vehicleMarca) {
        this.vehicleMarca = vehicleMarca;
    }

    /**
     * Obté el model del vehicle reservat: Retorna la denominació comercial del
     * vehicle llogat.
     */
    public String getVehicleModel() {
        return vehicleModel;
    }

    /**
     * Assigna el model del vehicle reservat: Desa el model específic del
     * vehicle dins del document històric.
     */
    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    /**
     * Obté l'import econòmic total del lloguer: Retorna el cost de la reserva
     * en format BigDecimal (sense incloure la fiança).
     */
    public BigDecimal getImportTotal() {
        return importTotal;
    }

    /**
     * Assigna l'import econòmic total del lloguer: Estableix el preu total
     * calculat per al període de lloguer.
     */
    public void setImportTotal(BigDecimal importTotal) {
        this.importTotal = importTotal;
    }

    /**
     * Obté l'estat actual de la reserva a l'històric: Retorna el valor de
     * l'enumeració EstatHistoric que indica la situació de l'operació.
     */
    public EstatHistoric getEstat() {
        return estat;
    }

    /**
     * Assigna l'estat de la reserva a l'històric: Actualitza la fase del cicle
     * de vida en què es troba la reserva.
     */
    public void setEstat(EstatHistoric estat) {
        this.estat = estat;
    }

    /**
     * Obté la data i hora de l'última actualització: Retorna la marca temporal
     * de l'últim canvi o esdeveniment registrat sobre aquesta reserva.
     */
    public LocalDateTime getDataUltimEvent() {
        return dataUltimEvent;
    }

    /**
     * Assigna la data i hora de l'última actualització: Registra el moment
     * exacte en què s'ha modificat o sincronitzat el document històric.
     */
    public void setDataUltimEvent(LocalDateTime d) {
        this.dataUltimEvent = d;
    }
}

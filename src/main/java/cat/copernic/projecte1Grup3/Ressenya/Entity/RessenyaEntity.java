/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Ressenya.Entity;

import cat.copernic.projecte1Grup3.Reserves.Entity.ReservesEntity;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.ClientEntity;
import cat.copernic.projecte1Grup3.Vehicles.Entity.VehiclesEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entitat JPA que representa la taula "Ressenya" a la base de dades:
 * Emmagatzema les valoracions dels clients sobre els vehicles llogats, incloent
 * puntuació (1-5), comentari i data de creació. Manté les relacions amb el
 * vehicle valorat, el client autor i la reserva associada.
 *
 * @author Grup3
 */
@Entity
@Table(name = "Ressenya")
public class RessenyaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ressenya")
    private Long idRessenya;

    @ManyToOne
    @JoinColumn(name = "Matricula_vehicle",
            referencedColumnName = "matricula")
    private VehiclesEntity vehicle;

    @ManyToOne
    @JoinColumn(name = "DNI_client", referencedColumnName = "DNI")
    private ClientEntity client;

    @ManyToOne
    @JoinColumn(name = "codi_reserva")
    private ReservesEntity reserva;

    @Min(1)
    @Max(5)
    @Column(nullable = false, name = "Puntuacio")
    private int puntuacio;

    @Column(name = "comentari")
    private String comentari;

    @Column(nullable = false)
    private LocalDateTime dataCreacio;

    /**
     * Constructor per defecte requerit per JPA: Permet a Hibernate instanciar
     * l'entitat buida abans de carregar-hi les dades des de la base de dades.
     */
    public RessenyaEntity() {
    }

    /**
     * Constructor parametritzat per crear una nova ressenya: Inicialitza la
     * puntuació, el comentari, la marca temporal de creació, el client autor i
     * el vehicle valorat.
     */
    public RessenyaEntity(int puntuacio, String comentari, LocalDateTime dataCreacio, ClientEntity client, VehiclesEntity vehicle) {
        this.puntuacio = puntuacio;
        this.comentari = comentari;
        this.dataCreacio = dataCreacio;
        this.client = client;
        this.vehicle = vehicle;
    }

    /**
     * Obté l'identificador únic de la ressenya: Retorna la clau primària
     * autogenerada corresponent a la columna "id_ressenya".
     */
    public Long getIdRessenya() {
        return idRessenya;
    }

    /**
     * Assigna l'identificador únic de la ressenya: Estableix el valor de la
     * clau primària per a aquesta instància de valoració.
     */
    public void setIdRessenya(Long idRessenya) {
        this.idRessenya = idRessenya;
    }

    /**
     * Obté la puntuació numèrica de la valoració: Retorna el valor enter
     * comprès entre 1 i 5 atorgat pel client.
     */
    public int getPuntuacio() {
        return puntuacio;
    }

    /**
     * Assigna la puntuació numèrica de la valoració: Estableix la qualificació
     * de la ressenya dins del rang vàlid d'1 a 5 estrelles.
     */
    public void setPuntuacio(int puntuacio) {
        this.puntuacio = puntuacio;
    }

    /**
     * Obté el text descriptiu del comentari: Retorna l'opinió escrita pel
     * client sobre el vehicle o el servei de lloguer.
     */
    public String getComentari() {
        return comentari;
    }

    /**
     * Assigna el text descriptiu del comentari: Desa l'opinió escrita pel
     * client associada a la puntuació.
     */
    public void setComentari(String comentari) {
        this.comentari = comentari;
    }

    /**
     * Obté la data i hora en què es va publicar la ressenya: Retorna el valor
     * LocalDateTime emmagatzemat a la columna "dataCreacio".
     */
    public LocalDateTime getDataCreacio() {
        return dataCreacio;
    }

    /**
     * Assigna la data i hora de creació de la ressenya: Estableix la marca
     * temporal en què s'ha registrat la valoració.
     */
    public void setDataCreacio(LocalDateTime dataCreacio) {
        this.dataCreacio = dataCreacio;
    }

    /**
     * Obté l'entitat del client autor de la ressenya: Retorna l'objecte
     * ClientEntity vinculat a través de la clau forana "DNI_client".
     */
    public ClientEntity getClient() {
        return client;
    }

    /**
     * Assigna el client autor de la ressenya: Vincula la valoració amb el
     * perfil del client que l'ha emès.
     */
    public void setClient(ClientEntity client) {
        this.client = client;
    }

    /**
     * Obté l'entitat del vehicle valorat: Retorna l'objecte VehiclesEntity
     * associat mitjançant la clau forana "Matricula_vehicle".
     */
    public VehiclesEntity getVehicle() {
        return vehicle;
    }

    /**
     * Assigna el vehicle objecte de la valoració: Vincula la ressenya amb el
     * vehicle concret sobre el qual s'opina.
     */
    public void setVehicle(VehiclesEntity vehicle) {
        this.vehicle = vehicle;
    }
}

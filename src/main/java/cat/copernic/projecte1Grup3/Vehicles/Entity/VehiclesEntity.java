/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Vehicles.Entity;

import cat.copernic.projecte1Grup3.Reserves.Entity.ReservesEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entitat JPA que representa la taula "vehicle" amb tota la informació tècnica, econòmica i operativa de la flota:
 * Defineix la matrícula com a clau primària, les tarifes (preu per hora i fiança), els límits de lloguer i l'historial de baixes/altes.
 * Manté una relació bidireccional One-to-Many amb les reserves associades a cada vehicle.
 *
 * @author Grup3
 */
@Entity
@Table(name = "vehicle")
public class VehiclesEntity {
    
    @Id
    @NotBlank(message = "La matrícula és obligatòria")
    @Pattern(regexp = "^[0-9]{4}[A-Z]{3}$|^[A-Z]{1,2}[0-9]{4}[A-Z]{1,2}$", message = "Format de matrícula invàlid (Ex: 1234ABC)")
    @Column(name = "matricula", nullable = false, length = 15)
    private String matricula;

    @NotBlank(message = "La marca és obligatòria")
    @Size(max = 50, message = "La marca no pot superar els 50 caràcters")
    @Column(name = "marca", nullable = false, length = 50)
    private String marca;

    @NotBlank(message = "El model és obligatori")
    @Size(max = 50, message = "El model no pot superar els 50 caràcters")
    @Column(name = "model", nullable = false, length = 50)
    private String model;

    @NotNull(message = "L'any de matriculació és obligatori")
    @Min(value = 1900, message = "L'any ha de ser posterior a 1900")
    @Max(value = 2100, message = "L'any no és vàlid")
    @Column(name = "any_vehicle", nullable = false)
    private Integer anyMatriculacio;

    @NotBlank(message = "La descripció és obligatòria")
    @Column(name = "descripcio", nullable = false, columnDefinition = "TEXT")
    private String descripcio;

    @NotBlank(message = "Selecciona un tipus de vehicle")
    @Column(name = "tipus_vehicle", length = 30)
    private String tipusVehicle;

    @PositiveOrZero(message = "El límit de quilometratge ha de ser positiu o zero")
    @Column(name = "limit_quilometratge")
    private Integer limitQuilometratge;

    @NotNull(message = "El preu per hora és obligatori")
    @DecimalMin(value = "0.01", message = "El preu ha de ser superior a 0")
    @Column(name = "preu", nullable = false, precision = 10, scale = 2)
    private BigDecimal preuHora;

    @NotNull(message = "La fiança és obligatòria")
    @DecimalMin(value = "0.00", message = "La fiança no pot ser negativa")
    @Column(name = "fiança", nullable = false, precision = 10, scale = 2)
    private BigDecimal fianca;

    @NotNull(message = "El mínim de dies és obligatori")
    @Min(value = 1, message = "El mínim ha de ser com a mínim 1 dia")
    @Column(name = "min_dies_lloguer", nullable = false)
    private Integer minDiesLloguer;

    @NotNull(message = "El màxim de dies és obligatori")
    @Min(value = 1, message = "El màxim ha de ser com a mínim 1 dia")
    @Column(name = "max_dies_lloguer", nullable = false)
    private Integer maxDiesLloguer;

    @NotBlank(message = "L'estat operatiu és obligatori")
    @Column(name = "estat", length = 30)
    private String estat;

    @Column(name = "actiu", nullable = false)
    private Boolean actiu;

    @Column(name = "comentaris_privats", columnDefinition = "TEXT")
    private String comentarisPrivats;

    @Column(name = "foto", length = 255)
    private String foto;

    // --- CAMPS DE DESACTIVACIÓ I REACTIVACIÓ ---
    @Column(name = "data_desactivacio")
    private LocalDate dataDesactivacio;

    @Column(name = "data_fi_desactivacio")
    private LocalDate dataFiDesactivacio;

    @Column(name = "data_activacio")
    private LocalDate dataActivacio;

    @Column(name = "motiu_activacio", length = 255)
    private String motiuActivacio;

    @Column(name = "import_activacio", precision = 10, scale = 2)
    private BigDecimal importActivacio;

    // --- RELACIÓ ONE-TO-MANY AMB RESERVES ---
    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL)
    private List<ReservesEntity> reserves = new ArrayList<>();

    /**
     * Constructor per defecte sense paràmetres requerit per JPA:
     * Inicialitza una instància buida de VehiclesEntity.
     */
    public VehiclesEntity() {
    }

    /**
     * Constructor parametritzat amb les dades principals del vehicle sense fotografia:
     * Inicialitza la matrícula, característiques tècniques, tarifes, durada de lloguer, estat i comentaris interns.
     */
    public VehiclesEntity(String matricula, String marca, String model, Integer anyMatriculacio, 
                          String descripcio, String tipusVehicle, Integer limitQuilometratge, 
                          BigDecimal preuHora, BigDecimal fianca, Integer minDiesLloguer, 
                          Integer maxDiesLloguer, String estat, Boolean actiu, String comentarisPrivats) {
        this.matricula = matricula;
        this.marca = marca;
        this.model = model;
        this.anyMatriculacio = anyMatriculacio;
        this.descripcio = descripcio;
        this.tipusVehicle = tipusVehicle;
        this.limitQuilometratge = limitQuilometratge;
        this.preuHora = preuHora;
        this.fianca = fianca;
        this.minDiesLloguer = minDiesLloguer;
        this.maxDiesLloguer = maxDiesLloguer;
        this.estat = estat;
        this.actiu = actiu;
        this.comentarisPrivats = comentarisPrivats;
    }

    /**
     * Constructor parametritzat complet incloent el nom del fitxer d'imatge:
     * Delega la inicialització dels atributs principals al constructor base i assigna la fotografia del vehicle.
     */
    public VehiclesEntity(String matricula, String marca, String model, Integer anyMatriculacio, 
                          String descripcio, String tipusVehicle, Integer limitQuilometratge, 
                          BigDecimal preuHora, BigDecimal fianca, Integer minDiesLloguer, 
                          Integer maxDiesLloguer, String estat, Boolean actiu, String comentarisPrivats, String foto) {
        this(matricula, marca, model, anyMatriculacio, descripcio, tipusVehicle, limitQuilometratge, 
             preuHora, fianca, minDiesLloguer, maxDiesLloguer, estat, actiu, comentarisPrivats);
        this.foto = foto;
    }

    // --- GETTERS I SETTERS ---

    /**
     * Obté la matrícula identificadora del vehicle:
     * Retorna la clau primària única que identifica el vehicle a la base de dades.
     */
    public String getMatricula() {
        return matricula;
    }

    /**
     * Assigna la matrícula del vehicle:
     * Estableix la placa identificadora validant el format espanyol actual o provincial.
     */
    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    /**
     * Obté el fabricant o marca del vehicle:
     * Retorna la cadena de text amb el nom de la marca comercial.
     */
    public String getMarca() {
        return marca;
    }

    /**
     * Assigna la marca del vehicle:
     * Desa el nom del fabricant amb una longitud màxima de 50 caràcters.
     */
    public void setMarca(String marca) {
        this.marca = marca;
    }

    /**
     * Obté el model específic del vehicle:
     * Retorna la denominació comercial del model emmagatzemada a la columna "model".
     */
    public String getModel() {
        return model;
    }

    /**
     * Assigna el model del vehicle:
     * Estableix el nom del model amb un màxim de 50 caràcters.
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * Obté l'any de primera matriculació del vehicle:
     * Retorna el valor enter corresponent a l'any de fabricació o registre.
     */
    public Integer getAnyMatriculacio() {
        return anyMatriculacio;
    }

    /**
     * Assigna l'any de matriculació del vehicle:
     * Estableix l'any comprès dins del rang vàlid entre 1900 i 2100.
     */
    public void setAnyMatriculacio(Integer anyMatriculacio) {
        this.anyMatriculacio = anyMatriculacio;
    }

    /**
     * Obté la descripció comercial i d'equipament del vehicle:
     * Retorna el text descriptiu detallat mostrat al catàleg de lloguer.
     */
    public String getDescripcio() {
        return descripcio;
    }

    /**
     * Assigna la descripció del vehicle:
     * Desa el text informatiu a la columna "descripcio" de tipus TEXT.
     */
    public void setDescripcio(String descripcio) {
        this.descripcio = descripcio;
    }

    /**
     * Obté la categoria o tipus de carrosseria del vehicle:
     * Retorna la classificació del vehicle (per exemple, Turisme, Compacte o Furgoneta).
     */
    public String getTipusVehicle() {
        return tipusVehicle;
    }

    /**
     * Assigna el tipus o categoria del vehicle:
     * Estableix la categoria a la columna "tipus_vehicle".
     */
    public void setTipusVehicle(String tipusVehicle) {
        this.tipusVehicle = tipusVehicle;
    }

    /**
     * Obté el límit màxim de quilòmetres permesos per període de lloguer:
     * Retorna el valor enter emmagatzemat a la columna "limit_quilometratge".
     */
    public Integer getLimitQuilometratge() {
        return limitQuilometratge;
    }

    /**
     * Assigna el límit de quilometratge del vehicle:
     * Estableix un valor positiu o zero amb el topall de quilòmetres inclosos.
     */
    public void setLimitQuilometratge(Integer limitQuilometratge) {
        this.limitQuilometratge = limitQuilometratge;
    }

    /**
     * Obté la tarifa de lloguer per hora del vehicle:
     * Retorna l'import decimal emmagatzemat a la columna "preu".
     */
    public BigDecimal getPreuHora() {
        return preuHora;
    }

    /**
     * Assigna el preu per hora del vehicle:
     * Estableix la tarifa horària que ha de ser estrictament superior a zero.
     */
    public void setPreuHora(BigDecimal preuHora) {
        this.preuHora = preuHora;
    }

    /**
     * Obté l'import de la fiança o dipòsit de garantia requerit:
     * Retorna el valor BigDecimal emmagatzemat a la columna "fiança".
     */
    public BigDecimal getFianca() {
        return fianca;
    }

    /**
     * Assigna l'import de la fiança del vehicle:
     * Estableix el dipòsit de seguretat amb un valor mínim de 0.00.
     */
    public void setFianca(BigDecimal fianca) {
        this.fianca = fianca;
    }

    /**
     * Obté el nombre mínim de dies requerits per llogar el vehicle:
     * Retorna el valor enter definit a la columna "min_dies_lloguer".
     */
    public Integer getMinDiesLloguer() {
        return minDiesLloguer;
    }

    /**
     * Assigna el mínim de dies de lloguer del vehicle:
     * Estableix la durada mínima de la reserva, que ha de ser com a mínim d'1 dia.
     */
    public void setMinDiesLloguer(Integer minDiesLloguer) {
        this.minDiesLloguer = minDiesLloguer;
    }

    /**
     * Obté el nombre màxim de dies permesos en una mateixa reserva:
     * Retorna el topall de dies definit a la columna "max_dies_lloguer".
     */
    public Integer getMaxDiesLloguer() {
        return maxDiesLloguer;
    }

    /**
     * Assigna el màxim de dies de lloguer del vehicle:
     * Estableix la durada màxima permesa per a cada reserva del vehicle.
     */
    public void setMaxDiesLloguer(Integer maxDiesLloguer) {
        this.maxDiesLloguer = maxDiesLloguer;
    }

    /**
     * Obté l'estat operatiu actual del vehicle:
     * Retorna la situació del vehicle dins la flota (per exemple, DISPONIBLE o MANTENIMENT).
     */
    public String getEstat() {
        return estat;
    }

    /**
     * Assigna l'estat operatiu del vehicle:
     * Actualitza l'estat de disponibilitat a la columna "estat".
     */
    public void setEstat(String estat) {
        this.estat = estat;
    }

    /**
     * Indica si el vehicle està habilitat per a ser llogat al catàleg públic:
     * Retorna cert si el vehicle està actiu o fals si està donat de baixa temporalment.
     */
    public Boolean getActiu() {
        return actiu;
    }

    /**
     * Assigna l'indicador d'activitat del vehicle:
     * Activa o desactiva la visibilitat i contractació del vehicle al sistema.
     */
    public void setActiu(Boolean actiu) {
        this.actiu = actiu;
    }

    /**
     * Obté les observacions o notes internes del personal sobre el vehicle:
     * Retorna el text privat visible únicament per als agents i administradors.
     */
    public String getComentarisPrivats() {
        return comentarisPrivats;
    }

    /**
     * Assigna comentaris privats al vehicle:
     * Desa notes de manteniment o restriccions internes a la columna "comentaris_privats".
     */
    public void setComentarisPrivats(String comentarisPrivats) {
        this.comentarisPrivats = comentarisPrivats;
    }

    /**
     * Obté el nom del fitxer d'imatge associat al vehicle:
     * Retorna la ruta relativa o nom de la fotografia desada al directori d'uploads.
     */
    public String getFoto() {
        return foto;
    }

    /**
     * Assigna el nom del fitxer de fotografia del vehicle:
     * Estableix la referència de la imatge principal a la columna "foto".
     */
    public void setFoto(String foto) {
        this.foto = foto;
    }

    /**
     * Obté la data en què es va desactivar temporalment el vehicle:
     * Retorna el valor LocalDate de l'inici de la baixa operativa o manteniment.
     */
    public LocalDate getDataDesactivacio() {
        return dataDesactivacio;
    }

    /**
     * Assigna la data de desactivació del vehicle:
     * Registra el dia en què el vehicle passa a estat inactiu.
     */
    public void setDataDesactivacio(LocalDate dataDesactivacio) {
        this.dataDesactivacio = dataDesactivacio;
    }

    /**
     * Obté la data prevista de finalització de la desactivació del vehicle:
     * Retorna el dia estimat en què el vehicle tornarà a estar operatiu.
     */
    public LocalDate getDataFiDesactivacio() {
        return dataFiDesactivacio;
    }

    /**
     * Assigna la data prevista de fi de desactivació:
     * Estableix el límit temporal estimat de la baixa per manteniment o avaria.
     */
    public void setDataFiDesactivacio(LocalDate dataFiDesactivacio) {
        this.dataFiDesactivacio = dataFiDesactivacio;
    }

    /**
     * Obté la data de l'última reactivació del vehicle:
     * Retorna el valor LocalDate en què el vehicle va tornar al servei actiu.
     */
    public LocalDate getDataActivacio() {
        return dataActivacio;
    }

    /**
     * Assigna la data de reactivació del vehicle:
     * Registra la data en què es reincorpora el vehicle a la flota activa.
     */
    public void setDataActivacio(LocalDate dataActivacio) {
        this.dataActivacio = dataActivacio;
    }

    /**
     * Obté el motiu registrat durant la reactivació del vehicle:
     * Retorna la descripció de la intervenció o causa per la qual s'ha reactivat.
     */
    public String getMotiuActivacio() {
        return motiuActivacio;
    }

    /**
     * Assigna el motiu de reactivació del vehicle:
     * Desa la justificació de la posada en marxa a la columna "motiu_activacio".
     */
    public void setMotiuActivacio(String motiuActivacio) {
        this.motiuActivacio = motiuActivacio;
    }

    /**
     * Obté el cost econòmic associat a la intervenció de reactivació:
     * Retorna l'import decimal de la reparació o manteniment realitzat.
     */
    public BigDecimal getImportActivacio() {
        return importActivacio;
    }

    /**
     * Assigna l'import del cost de reactivació del vehicle:
     * Registra la despesa associada a la posada a punt a la columna "import_activacio".
     */
    public void setImportActivacio(BigDecimal importActivacio) {
        this.importActivacio = importActivacio;
    }

    /**
     * Obté la llista de reserves vinculades a aquest vehicle:
     * Retorna la col·lecció d'entitats ReservesEntity associades mitjançant la relació One-to-Many.
     */
    public List<ReservesEntity> getReserves() {
        return reserves;
    }

    /**
     * Assigna la llista de reserves del vehicle:
     * Estableix la col·lecció de reserves associades a aquesta entitat.
     */
    public void setReserves(List<ReservesEntity> reserves) {
        this.reserves = reserves;
    }
}
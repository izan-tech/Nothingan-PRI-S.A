/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Usuaris.Client.Entity;

import cat.copernic.projecte1Grup3.Usuaris.Entity.UsuariEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Entitat JPA que representa la taula "Clients" a la base de dades relacional:
 * Hereta les dades base d'identificació de UsuariEntity mitjançant la clau
 * primària compartida DNI. Emmagatzema dades personals, documentals (DNI i
 * carnet), estat de validació, nivell prèmium i dades parcials de pagament.
 *
 * @author Grup3
 */
@Entity // Marca la clase como entidad persistente de JPA
@Table(name = "Clients") // Asocia esta entidad con la tabla "Clients" en la base de datos MySQL
@PrimaryKeyJoinColumn(
        name = "DNI",
        referencedColumnName = "dni"
) // Mapeo de herencia: comparte la clave primaria 'dni' con la entidad padre usuariEntity
public class ClientEntity extends UsuariEntity {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "data_caducitat_DNI", nullable = false)
    private LocalDate dataCaducitatDNI;

    @Column(name = "nacionalitat", nullable = false)
    private String nacionalitat;

    @Column(name = "adreca", nullable = false)
    private String adreca;

    @Column(name = "tipus_llicencia", nullable = false)
    private String tipusLlicencia;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "data_caducitat_carnet", nullable = false)
    private LocalDate dataCaducitatCarnet;

    @Column(name = "premium", nullable = false)
    private boolean premium;

    @Column(name = "estat_validacio", nullable = false)
    private String estatValidacio; // Clave para el RF82 (PENDENT, VALIDAT, REBUTJAT)

    // Campos opcionales (sin nullable = false) para compatibilidad con registros preexistentes
    @Column(name = "telefon")
    private String telefon;

    @Column(name = "poblacio")
    private String poblacio;

    // Només es guarden els 4 últims dígits de la targeta
    @Column(name = "targeta_ultims4", length = 4)
    private String targetaUltims4;

    // Format yyyy-MM
    @Column(name = "targeta_caducitat", length = 7)
    private String targetaCaducitat;

    // Només per rebre dades del formulari: NO es guarden a la base de dades
    @Transient
    private String numTargeta;

    @Transient
    private String novaCaducitatTargeta;


    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "data_naixement", nullable = false)
    private LocalDate dataNaixement;

    @Column(name = "data_registre")
    private LocalDate dataRegistre;

    /**
     * Inicialitza automàticament la data de registre abans de persistir el
     * client: Assigna la data actual del sistema si el camp dataRegistre encara
     * no té cap valor assignat.
     */
    // Se ejecuta automáticamente antes de persistir un nuevo registro en la BD
    @PrePersist
    void alCrear() {
        if (dataRegistre == null) {
            dataRegistre = LocalDate.now();
        }
    }

    /**
     * Constructor per defecte sense paràmetres requerit per JPA: Inicialitza
     * una instància buida de ClientEntity invocant el constructor de la
     * superclasse.
     */
    // Constructor sin argumentos por defecto para JPA
    public ClientEntity() {
        super();
    }

    /**
     * Constructor parametritzat per crear un client amb les dades principals:
     * Inicialitza els atributs base d'usuari juntament amb el DNI,
     * nacionalitat, adreça, llicència, prèmium i estat de validació.
     */
    // Constructor completo que inicializa también la superclase (usuariEntity)
    public ClientEntity(LocalDate dataCaducitatDNI,
            String nacionalitat,
            String adreca,
            String tipusLlicencia,
            boolean premium,
            String estatValidacio,
            String dni,
            String nom,
            String email) {

        super(dni, nom, email);

        this.dataCaducitatDNI = dataCaducitatDNI;
        this.nacionalitat = nacionalitat;
        this.adreca = adreca;
        this.tipusLlicencia = tipusLlicencia;
        this.premium = premium;
        this.estatValidacio = estatValidacio;
    }

    /**
     * Obté la data de caducitat del document d'identitat del client: Retorna el
     * valor LocalDate emmagatzemat a la columna "data_caducitat_DNI".
     */
    // Getters y Setters
    public LocalDate getDataCaducitatDNI() {
        return dataCaducitatDNI;
    }

    /**
     * Assigna la data de caducitat del document d'identitat del client:
     * Estableix la data límit de vigència del DNI o NIE en format ISO.
     */
    public void setDataCaducitatDNI(LocalDate dataCaducitatDNI) {
        this.dataCaducitatDNI = dataCaducitatDNI;
    }

    /**
     * Obté la nacionalitat declarada pel client: Retorna la cadena de text amb
     * el país o nacionalitat de l'usuari.
     */
    public String getNacionalitat() {
        return nacionalitat;
    }

    /**
     * Assigna la nacionalitat del client: Desa la nacionalitat a la columna
     * "nacionalitat" de la base de dades.
     */
    public void setNacionalitat(String nacionalitat) {
        this.nacionalitat = nacionalitat;
    }

    /**
     * Obté l'adreça postal del client: Retorna el domicili complet registrat a
     * la fitxa del client.
     */
    public String getAdreca() {
        return adreca;
    }

    /**
     * Assigna l'adreça postal del client: Estableix el domicili de residència a
     * la columna "adreca".
     */
    public void setAdreca(String adreca) {
        this.adreca = adreca;
    }

    /**
     * Obté el tipus de permís de conduir del client: Retorna la categoria de
     * llicència (per exemple, B, A2, C) que habilita per conduir certs
     * vehicles.
     */
    public String getTipusLlicencia() {
        return tipusLlicencia;
    }

    /**
     * Assigna el tipus de permís de conduir del client: Desa la categoria de
     * llicència a la columna "tipus_llicencia".
     */
    public void setTipusLlicencia(String tipusLlicencia) {
        this.tipusLlicencia = tipusLlicencia;
    }

    /**
     * Indica si el client disposa de subscripció o categoria prèmium: Retorna
     * cert si el client té avantatges prèmium actius o fals en cas contrari.
     */
    public boolean isPremium() {
        return premium;
    }

    /**
     * Assigna l'estat prèmium del client: Activa o desactiva la condició de
     * client prèmium a la base de dades.
     */
    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    /**
     * Obté l'estat de validació del compte del client: Retorna l'estat actual
     * dins del flux d'aprovació (PENDENT, VALIDAT o REBUTJAT).
     */
    public String getEstatValidacio() {
        return estatValidacio;
    }

    /**
     * Assigna l'estat de validació del compte del client: Actualitza l'estat
     * d'autorització que determina si el client pot iniciar sessió i reservar.
     */
    public void setEstatValidacio(String estatValidacio) {
        this.estatValidacio = estatValidacio;
    }

    /**
     * Obté el telèfon de contacte del client: Retorna el número de telèfon
     * opcional emmagatzemat a la columna "telefon".
     */
    public String getTelefon() {
        return telefon;
    }

    /**
     * Assigna el telèfon de contacte del client: Estableix el número de telèfon
     * de contacte de l'usuari.
     */
    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    /**
     * Obté la població de residència del client: Retorna el municipi o
     * localitat emmagatzemat a la columna "poblacio".
     */
    public String getPoblacio() {
        return poblacio;
    }

    /**
     * Assigna la població de residència del client: Estableix el municipi
     * associat al domicili del client.
     */
    public void setPoblacio(String poblacio) {
        this.poblacio = poblacio;
    }

    /**
     * Obté els quatre últims dígits emmascarats de la targeta de pagament:
     * Retorna la terminació de la targeta desada per motius de seguretat sense
     * exposar el número complet.
     */
    public String getTargetaUltims4() {
        return targetaUltims4;
    }

    /**
     * Assigna els quatre últims dígits de la targeta de pagament: Emmagatzema
     * únicament els darrers 4 caràcters de la targeta vinculada al compte.
     */
    public void setTargetaUltims4(String targetaUltims4) {
        this.targetaUltims4 = targetaUltims4;
    }

    /**
     * Obté la data de caducitat de la targeta de pagament: Retorna el mes i any
     * de venciment en format "yyyy-MM".
     */
    public String getTargetaCaducitat() {
        return targetaCaducitat;
    }

    /**
     * Assigna la data de caducitat de la targeta de pagament: Estableix el
     * venciment de la targeta a la columna "targeta_caducitat".
     */
    public void setTargetaCaducitat(String targetaCaducitat) {
        this.targetaCaducitat = targetaCaducitat;
    }

    /**
     * Obté el número complet de targeta introduït temporalment al formulari:
     * Retorna el valor transitori que no es persisteix a la base de dades.
     */
    public String getNumTargeta() {
        return numTargeta;
    }

    /**
     * Assigna el número complet de targeta rebut des del formulari web:
     * Emmagatzema temporalment el número en memòria per extreure'n els darrers
     * 4 dígits al servei.
     */
    public void setNumTargeta(String numTargeta) {
        this.numTargeta = numTargeta;
    }

    public String getNovaCaducitatTargeta() {
        return novaCaducitatTargeta;
    }

    public void setNovaCaducitatTargeta(String novaCaducitatTargeta) {
        this.novaCaducitatTargeta = novaCaducitatTargeta;
    }

    /**
     * Obté el codi de seguretat CVV introduït temporalment al formulari:
     * Retorna el valor transitori que no s'emmagatzema mai a la base de dades.
     */
   
    /**
     * Assigna el codi de seguretat CVV rebut des del formulari web: Emmagatzema
     * temporalment el codi en memòria per a la seva validació durant el
     * registre o edició.
     */
   

    /**
     * Obté la data de naixement del client: Retorna el valor LocalDate
     * utilitzat per verificar la majoria d'edat i l'antiguitat.
     */
    public LocalDate getDataNaixement() {
        return dataNaixement;
    }

    /**
     * Assigna la data de naixement del client: Estableix la data de naixement
     * formatada en estàndard ISO.
     */
    public void setDataNaixement(LocalDate dataNaixement) {
        this.dataNaixement = dataNaixement;
    }

    /**
     * Obté la data en què es va registrar el client al sistema: Retorna el
     * valor LocalDate assignat en el moment de l'alta.
     */
    public LocalDate getDataRegistre() {
        return dataRegistre;
    }

    /**
     * Assigna la data de registre del client: Estableix la data d'alta del
     * compte a la columna "data_registre".
     */
    public void setDataRegistre(LocalDate dataRegistre) {
        this.dataRegistre = dataRegistre;
    }

    /**
     * Obté la data de caducitat del carnet de conduir del client: Retorna el
     * valor LocalDate emmagatzemat a la columna "data_caducitat_carnet".
     */
    public LocalDate getDataCaducitatCarnet() {
        return dataCaducitatCarnet;
    }

    /**
     * Assigna la data de caducitat del carnet de conduir del client: Estableix
     * la data límit de vigència del permís de conducció en format ISO.
     */
    public void setDataCaducitatCarnet(LocalDate dataCaducitatCarnet) {
        this.dataCaducitatCarnet = dataCaducitatCarnet;
    }
}

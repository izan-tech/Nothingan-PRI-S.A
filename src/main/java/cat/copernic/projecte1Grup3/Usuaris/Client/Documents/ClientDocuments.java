/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Usuaris.Client.Documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Document MongoDB que emmagatzema les imatges documentals d'un client a la
 * col·lecció "client_documents": Vincula els fitxers binaris mitjançant el DNI
 * com a identificador únic respecte a la base de dades relacional. Conté els
 * arrays de bytes i els tipus MIME per a la foto frontal del DNI, la foto
 * posterior del DNI i el carnet de conduir.
 *
 * @author Grup3
 */
// Define que esta clase se guarda como un documento dentro de la colección 'client_documents' en MongoDB
@Document(collection = "client_documents")
public class ClientDocuments {

    @Id
    private String dni; // Mismo DNI de la base de datos relacional (MySQL) para vincular los datos

    private byte[] fotoDniFrontal;        // Array de bytes con el contenido de la foto frontal del DNI
    private String tipusFotoDniFrontal;     // Tipo MIME (image/png, image/jpeg) para la cabecera HTTP

    private byte[] fotoDniPosterior;      // Array de bytes con el contenido de la foto trasera del DNI
    private String tipusFotoDniPosterior;   // Tipo MIME de la foto trasera

    private byte[] fotoCarnet;            // Array de bytes con la imagen del carnet de conducir
    private String tipusFotoCarnet;         // Tipo MIME del carnet de conducir

    /**
     * Constructor per defecte requerit per Spring Data MongoDB: Inicialitza una
     * instància buida de ClientDocuments per a la deserialització de documents.
     */
    // Constructor vacío por defecto
    public ClientDocuments() {
    }

    /**
     * Constructor parametritzat complet per inicialitzar tots els documents
     * d'un client: Assigna el DNI identificador juntament amb el contingut
     * binari i tipus MIME de les tres imatges requerides.
     */
    // Constructor parametrizado completo
    public ClientDocuments(String dni, byte[] fotoDniFrontal, String tipusFotoDniFrontal,
            byte[] fotoDniPosterior, String tipusFotoDniPosterior,
            byte[] fotoCarnet, String tipusFotoCarnet) {
        this.dni = dni;
        this.fotoDniFrontal = fotoDniFrontal;
        this.tipusFotoDniFrontal = tipusFotoDniFrontal;
        this.fotoDniPosterior = fotoDniPosterior;
        this.tipusFotoDniPosterior = tipusFotoDniPosterior;
        this.fotoCarnet = fotoCarnet;
        this.tipusFotoCarnet = tipusFotoCarnet;
    }

    /**
     * Obté el DNI del client associat al document: Retorna l'identificador clau
     * que enllaça aquest registre de MongoDB amb la taula "clients" de MySQL.
     */
    // Getters y Setters
    public String getDni() {
        return dni;
    }

    /**
     * Assigna el DNI identificador del document: Estableix la clau primària del
     * document a la col·lecció "client_documents".
     */
    public void setDni(String dni) {
        this.dni = dni;
    }

    /**
     * Obté les dades binàries de la fotografia frontal del DNI: Retorna l'array
     * de bytes de la imatge de la cara davantera del document d'identitat.
     */
    public byte[] getFotoDniFrontal() {
        return fotoDniFrontal;
    }

    /**
     * Assigna les dades binàries de la fotografia frontal del DNI: Emmagatzema
     * el contingut en bytes de la imatge frontal del document d'identitat.
     */
    public void setFotoDniFrontal(byte[] fotoDniFrontal) {
        this.fotoDniFrontal = fotoDniFrontal;
    }

    /**
     * Obté el tipus MIME de la fotografia frontal del DNI: Retorna la cadena
     * del format de la imatge (per exemple, "image/jpeg" o "image/png") per
     * servir-la via HTTP.
     */
    public String getTipusFotoDniFrontal() {
        return tipusFotoDniFrontal;
    }

    /**
     * Assigna el tipus MIME de la fotografia frontal del DNI: Desa el tipus de
     * contingut associat a la imatge frontal del DNI.
     */
    public void setTipusFotoDniFrontal(String tipusFotoDniFrontal) {
        this.tipusFotoDniFrontal = tipusFotoDniFrontal;
    }

    /**
     * Obté les dades binàries de la fotografia posterior del DNI: Retorna
     * l'array de bytes de la imatge del revers del document d'identitat.
     */
    public byte[] getFotoDniPosterior() {
        return fotoDniPosterior;
    }

    /**
     * Assigna les dades binàries de la fotografia posterior del DNI:
     * Emmagatzema el contingut en bytes de la imatge posterior del document
     * d'identitat.
     */
    public void setFotoDniPosterior(byte[] fotoDniPosterior) {
        this.fotoDniPosterior = fotoDniPosterior;
    }

    /**
     * Obté el tipus MIME de la fotografia posterior del DNI: Retorna la cadena
     * del format de la imatge posterior per configurar la capçalera de resposta
     * HTTP.
     */
    public String getTipusFotoDniPosterior() {
        return tipusFotoDniPosterior;
    }

    /**
     * Assigna el tipus MIME de la fotografia posterior del DNI: Desa el tipus
     * de contingut associat a la imatge del revers del DNI.
     */
    public void setTipusFotoDniPosterior(String tipusFotoDniPosterior) {
        this.tipusFotoDniPosterior = tipusFotoDniPosterior;
    }

    /**
     * Obté les dades binàries de la fotografia del carnet de conduir: Retorna
     * l'array de bytes de la imatge del permís de conducció del client.
     */
    public byte[] getFotoCarnet() {
        return fotoCarnet;
    }

    /**
     * Assigna les dades binàries de la fotografia del carnet de conduir:
     * Emmagatzema el contingut en bytes de la imatge del permís de conducció.
     */
    public void setFotoCarnet(byte[] fotoCarnet) {
        this.fotoCarnet = fotoCarnet;
    }

    /**
     * Obté el tipus MIME de la fotografia del carnet de conduir: Retorna el
     * format de la imatge del carnet per servir-la correctament al navegador.
     */
    public String getTipusFotoCarnet() {
        return tipusFotoCarnet;
    }

    /**
     * Assigna el tipus MIME de la fotografia del carnet de conduir: Desa el
     * tipus de contingut associat a la imatge del permís de conducció.
     */
    public void setTipusFotoCarnet(String tipusFotoCarnet) {
        this.tipusFotoCarnet = tipusFotoCarnet;
    }
}

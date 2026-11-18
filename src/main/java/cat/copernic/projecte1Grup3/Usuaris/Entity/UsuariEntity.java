/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Usuaris.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Entitat base JPA que mapeja la taula "usuaris" com a superclasse comuna del
 * sistema: Utilitza l'estratègia d'herència JOINED per compartir els atributs
 * d'identificació amb ClientEntity i AgentEntity. Defineix la clau primària DNI
 * i les restriccions de validació per al nom, correu electrònic i contrasenya.
 *
 * @author Grup3
 */
@Entity
@Table(name = "usuaris")
@Inheritance(strategy = InheritanceType.JOINED)
public class UsuariEntity {

    // El format del DNI/NIE es valida a clientService.alta(...), no aquí,
    // perquè els registres antics es puguin editar sense errors.
    @Id
    @Column(name = "dni", nullable = false, unique = true, length = 9)
    @NotBlank(message = "El DNI no pot estar buit")
    private String dni;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "El nom no pot estar buit")
    @Size(min = 2, max = 50, message = "El nom ha de tenir entre 2 i 50 caràcters")
    private String nom;

    @Column(nullable = false, unique = true, length = 254)
    @NotBlank(message = "El correu no pot estar buit")
    @Email(message = "El format del correu electrònic no és vàlid")
    private String email;

    // Opcional (els clients no en tenen). Si n'hi ha, mínim 8 caràcters.
    // El regex accepta null i cadena buida; només falla si hi ha 1-7 caràcters.
    @Column(nullable = true, length = 255)
    @Pattern(
            regexp = "^$|^.{8,}$",
            message = "La contrasenya ha de tenir com a mínim 8 caràcters"
    )
    private String password;

    /**
     * Obté el número de DNI o NIE identificador de l'usuari: Retorna la clau
     * primària única que identifica el registre a la taula "usuaris".
     */
    // Getters y Setters
    public String getDni() {
        return dni;
    }

    /**
     * Assigna el número de DNI o NIE de l'usuari: Estableix l'identificador
     * principal del compte amb un màxim de 9 caràcters.
     */
    public void setDni(String dni) {
        this.dni = dni;
    }

    /**
     * Obté el nom complet de l'usuari: Retorna la cadena de text amb el nom
     * emmagatzemat a la base de dades.
     */
    public String getNom() {
        return nom;
    }

    /**
     * Assigna el nom complet de l'usuari: Desa el nom validant que tingui una
     * longitud compresa entre 2 i 50 caràcters.
     */
    public void setNom(String nom) {
        this.nom = nom;
    }

    /**
     * Obté l'adreça de correu electrònic de l'usuari: Retorna l'email únic que
     * actua també com a identificador d'inici de sessió al sistema.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Assigna l'adreça de correu electrònic de l'usuari: Estableix el correu
     * electrònic subjecte a validació de format i unicitat.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Obté la contrasenya xifrada de l'usuari: Retorna el hash de la
     * contrasenya emmagatzemat a la columna "password".
     */
    public String getPassword() {
        return password;
    }

    /**
     * Assigna la contrasenya de l'usuari: Desa la contrasenya complint la
     * restricció mínima de 8 caràcters quan no és buida.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Constructor per defecte sense arguments requerit per JPA: Inicialitza una
     * instància buida de UsuariEntity.
     */
    public UsuariEntity() {
    }

    /**
     * Constructor parametritzat amb les dades bàsiques d'identificació de
     * l'usuari: Inicialitza el DNI, el nom i el correu electrònic sense
     * assignar contrasenya.
     */
    public UsuariEntity(String dni, String nom, String email) {
        this.dni = dni;
        this.nom = nom;
        this.email = email;
    }

    /**
     * Constructor parametritzat complet amb credencials d'accés: Inicialitza el
     * DNI, el nom, el correu electrònic i la contrasenya de l'usuari.
     */
    public UsuariEntity(String dni, String nom, String email, String password) {
        this.dni = dni;
        this.nom = nom;
        this.email = email;
        this.password = password;
    }
}

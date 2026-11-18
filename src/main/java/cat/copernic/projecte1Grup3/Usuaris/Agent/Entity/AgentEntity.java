/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Usuaris.Agent.Entity;

import cat.copernic.projecte1Grup3.Usuaris.Entity.UsuariEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Entitat JPA que representa la taula "agents" a la base de dades: Hereta les
 * dades bàsiques d'identificació de UsuariEntity i afegeix els atributs propis
 * del personal intern. Emmagatzema l'horari laboral (entrada i sortida), el rol
 * assignat (AGENT o ADMIN), la fotografia i la data de naixement.
 *
 * @author Grup3
 */
@Entity
@Table(name = "agents")
public class AgentEntity extends UsuariEntity {

    @Column(nullable = false)
    private String hora_entrada;

    @Column(nullable = false)
    private String hora_sortida;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @Column(nullable = true)
    private String foto;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "data_naixement", nullable = true)
    private LocalDate dataNaixement;

    /**
     * Obté l'hora d'entrada del torn laboral de l'agent: Retorna la cadena de
     * text amb l'hora d'inici de jornada configurada.
     */
    // ===== GETTERS Y SETTERS =====
    public String getHora_entrada() {
        return hora_entrada;
    }

    /**
     * Assigna l'hora d'entrada del torn laboral de l'agent: Estableix l'hora
     * d'inici de jornada a la columna "hora_entrada".
     */
    public void setHora_entrada(String hora_entrada) {
        this.hora_entrada = hora_entrada;
    }

    /**
     * Obté l'hora de sortida del torn laboral de l'agent: Retorna la cadena de
     * text amb l'hora de finalització de jornada configurada.
     */
    public String getHora_sortida() {
        return hora_sortida;
    }

    /**
     * Assigna l'hora de sortida del torn laboral de l'agent: Estableix l'hora
     * de finalització de jornada a la columna "hora_sortida".
     */
    public void setHora_sortida(String hora_sortida) {
        this.hora_sortida = hora_sortida;
    }

    /**
     * Obté el rol d'autorització assignat a l'agent: Retorna el valor de
     * l'enumeració Rol (ADMIN o AGENT) que determina els seus permisos al
     * sistema.
     */
    public Rol getRol() {
        return rol;
    }

    /**
     * Assigna el rol d'autorització de l'agent: Estableix el nivell d'accés de
     * l'usuari intern dins de l'aplicació.
     */
    public void setRol(Rol rol) {
        this.rol = rol;
    }

    /**
     * Obté el nom del fitxer de la fotografia de perfil: Retorna la ruta o nom
     * d'imatge emmagatzemat al servidor per a aquest agent.
     */
    public String getFoto() {
        return foto;
    }

    /**
     * Assigna el nom del fitxer de la fotografia de perfil: Desa la referència
     * de la imatge de perfil associada a l'agent.
     */
    public void setFoto(String foto) {
        this.foto = foto;
    }

    /**
     * Obté la data de naixement de l'agent: Retorna el valor LocalDate
     * corresponent a la columna "data_naixement".
     */
    public LocalDate getDataNaixement() {
        return dataNaixement;
    }

    /**
     * Assigna la data de naixement de l'agent: Estableix la data de naixement
     * formatada en estàndard ISO.
     */
    public void setDataNaixement(LocalDate dataNaixement) {
        this.dataNaixement = dataNaixement;
    }

    /**
     * Constructor per defecte requerit per JPA: Inicialitza una instància buida
     * d'AgentEntity invocant el constructor de la superclasse UsuariEntity.
     */
    // ===== CONSTRUCTORES =====
    public AgentEntity() {
        super();
    }

    /**
     * Constructor parametritzat sense data de naixement: Inicialitza les
     * credencials heretades d'UsuariEntity juntament amb l'horari laboral i el
     * rol de l'agent.
     */
    public AgentEntity(String hora_entrada, String hora_sortida, Rol rol, String dni, String nom, String email, String password) {
        super(dni, nom, email, password);
        this.hora_entrada = hora_entrada;
        this.hora_sortida = hora_sortida;
        this.rol = rol;
    }

    /**
     * Constructor complet amb tots els atributs de l'agent: Inicialitza les
     * dades base d'usuari, l'horari d'entrada i sortida, el rol i la data de
     * naixement.
     */
    public AgentEntity(String hora_entrada, String hora_sortida, Rol rol, LocalDate dataNaixement, String dni, String nom, String email, String password) {
        super(dni, nom, email, password);
        this.hora_entrada = hora_entrada;
        this.hora_sortida = hora_sortida;
        this.rol = rol;
        this.dataNaixement = dataNaixement;
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package cat.copernic.projecte1Grup3.Usuaris.Agent.Repository;

import cat.copernic.projecte1Grup3.Usuaris.Agent.Entity.AgentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositori JPA per a l'accés a dades de l'entitat AgentEntity: Proporciona
 * les operacions CRUD estàndard sobre la taula "agents" identificats pel seu
 * DNI. Inclou consultes personalitzades per filtrar agents per múltiples camps
 * i cercar per correu electrònic.
 *
 * @author Grup3
 */
@Repository
public interface AgentRepository extends JpaRepository<AgentEntity, String> {

    /**
     * Cerca agents que coincideixin parcialment amb el text introduït sense
     * distingir majúscules ni minúscules: Filtra els registres comparant el
     * paràmetre de cerca amb el DNI, el nom, el rol i el correu electrònic de
     * l'agent.
     */
    // Canviat 'agentEntity' a 'AgentEntity' i afegit CAST per a l'Enum Rol
    @Query("SELECT a FROM AgentEntity a WHERE "
            + "LOWER(a.dni) LIKE LOWER(CONCAT('%', :cerca, '%')) OR "
            + "LOWER(a.nom) LIKE LOWER(CONCAT('%', :cerca, '%')) OR "
            + "LOWER(CAST(a.rol AS string)) LIKE LOWER(CONCAT('%', :cerca, '%')) OR "
            + "LOWER(a.email) LIKE LOWER(CONCAT('%', :cerca, '%'))")
    List<AgentEntity> cercarPerDniNomEmail(@Param("cerca") String cerca);

    /**
     * Cerca un agent pel seu correu electrònic ignorant majúscules i
     * minúscules: S'utilitza principalment durant el procés d'autenticació i
     * validació de duplicitats de compte.
     */
    Optional<AgentEntity> findByEmailIgnoreCase(String email);
}

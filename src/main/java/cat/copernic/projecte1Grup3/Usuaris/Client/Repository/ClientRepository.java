/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package cat.copernic.projecte1Grup3.Usuaris.Client.Repository;

import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.ClientEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Repositori JPA per a l'accés a dades de l'entitat ClientEntity a MySQL:
 * Proporciona les operacions CRUD estàndard i suport per a consultes dinàmiques
 * mitjançant JpaSpecificationExecutor. Inclou mètodes derivats de cerca per
 * nom, DNI i correu electrònic, així com comprovacions de duplicitat.
 *
 * @author Grup3
 */
// Repositorio JPA para MySQL: ofrece métodos CRUD estándar y ejecución de consultas dinámicas
public interface ClientRepository extends JpaRepository<ClientEntity, String>,
        JpaSpecificationExecutor<ClientEntity> {

    /**
     * Cerca clients que coincideixin parcialment pel nom o pel DNI sense
     * distinció de majúscules: Retorna la llista de ClientEntity on el nom o el
     * DNI continguin les cadenes especificades.
     */
    // Método derivado automático de Spring Data: busca coincidencias por Nombre o DNI ignorando mayúsculas/minúsculas
    List<ClientEntity> findByNomContainingIgnoreCaseOrDniContainingIgnoreCase(String nom, String dni);

    /**
     * Cerca un client pel seu correu electrònic ignorant majúscules i
     * minúscules: Retorna un Optional amb l'entitat trobada o buit si no
     * existeix cap client amb aquest correu.
     */
    Optional<ClientEntity> findByEmailIgnoreCase(String email);

    /**
     * Comprova si existeix un altre client diferent que ja tingui registrat el
     * mateix correu electrònic: Retorna cert si hi ha un registre amb el mateix
     * email (ignorant majúscules) i un DNI diferent de l'indicat.
     */
    // Devuelve true si OTRO cliente (distinto DNI) ya usa ese email
    boolean existsByEmailIgnoreCaseAndDniNot(String email, String dni);
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Ressenya.Repository;

import cat.copernic.projecte1Grup3.Ressenya.Entity.RessenyaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositori JPA per a la gestió de l'accés a dades de les ressenyes:
 * Proporciona les operacions CRUD estàndard sobre la taula "Ressenya"
 * mitjançant Spring Data JPA. Defineix consultes derivades per validar
 * duplicitats de valoració per client i llistar ressenyes per vehicle.
 *
 * @author Grup3
 */
@Repository
public interface RessenyaRepository extends JpaRepository<RessenyaEntity, Long> {

    /**
     * Comprova si un client ja ha valorat un vehicle determinat: Cerca a la
     * base de dades l'existència d'algun registre que coincideixi amb la
     * matrícula del vehicle i el DNI del client. Retorna true si ja existeix
     * una ressenya prèvia per evitar valoracions duplicades.
     */
    boolean existsByVehicleMatriculaAndClientDni(String matricula, String dni);

    /**
     * Recupera totes les ressenyes associades a un vehicle concret: Filtra les
     * valoracions per la matrícula del vehicle indicat i les ordena
     * cronològicament de més recent a més antiga. Retorna la llista d'entitats
     * RessenyaEntity llesta per mostrar a la fitxa del vehicle.
     */
    List<RessenyaEntity> findByVehicleMatriculaOrderByDataCreacioDesc(String matricula);
}

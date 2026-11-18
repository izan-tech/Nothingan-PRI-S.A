/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Informes.Repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import cat.copernic.projecte1Grup3.Informes.Entity.HistoricReserva;

/**
 * Repositori MongoDB per gestionar l'accés a l'històric de reserves:
 * Proporciona les operacions CRUD estàndard sobre la col·lecció documental de
 * reserves històriques. Defineix consultes personalitzades per recuperar els
 * registres filtrats per DNI de client i ordenats.
 *
 * @author Grup3
 */
public interface HistoricReservaRepository extends MongoRepository<HistoricReserva, Long> {

    /**
     * Cerca les reserves històriques d'un conjunt de clients: Filtra els
     * documents de MongoDB on el camp clientDni coincideixi amb algun dels DNIs
     * de la llista. Retorna els resultats ordenats segons el criteri
     * d'ordenació (Sort) especificat.
     */
    List<HistoricReserva> findByClientDniIn(List<String> dnis, Sort sort);
}

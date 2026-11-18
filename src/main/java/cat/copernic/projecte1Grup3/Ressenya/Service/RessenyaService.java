/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Ressenya.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.projecte1Grup3.Ressenya.Entity.RessenyaEntity;
import cat.copernic.projecte1Grup3.Vehicles.Repository.VehiclesRepository;
import cat.copernic.projecte1Grup3.Ressenya.Repository.RessenyaRepository;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.ClientRepository;

/**
 * Servei de lògica de negoci per a la gestió de ressenyes de vehicles: Coordina
 * la consulta, creació i eliminació de valoracions aplicant les regles de
 * validació del sistema. Garanteix la integritat referencial amb vehicles i
 * clients, evitant valoracions duplicades o fora de rang.
 *
 * @author Grup3
 */
@Service
public class RessenyaService {

    private final RessenyaRepository resenyaRepo;
    private final VehiclesRepository vehicleRepo;
    private final ClientRepository clientRepo;

    /**
     * Injecció de dependències dels repositoris de ressenyes, vehicles i
     * clients: Inicialitza el servei rebent RessenyaRepository,
     * VehiclesRepository i ClientRepository per operar sobre les dades.
     */
    public RessenyaService(RessenyaRepository resenyaRepo, VehiclesRepository vehicleRepo, ClientRepository clientRepo) {
        this.resenyaRepo = resenyaRepo;
        this.vehicleRepo = vehicleRepo;
        this.clientRepo = clientRepo;
    }

    /**
     * Obté el llistat de ressenyes associades a un vehicle: Consulta al
     * repositori totes les valoracions de la matrícula indicada ordenades de
     * més recent a més antiga.
     */
    public List<RessenyaEntity> llistarPerVehicle(String matricula) {
        return resenyaRepo.findByVehicleMatriculaOrderByDataCreacioDesc(matricula);
    }

    /**
     * Comprova si un client ja ha valorat prèviament un vehicle: Normalitza el
     * DNI en majúscules i consulta si existeix algun registre coincident a la
     * base de dades.
     */
    // Indica si el client ja ha escrit una ressenya per aquest vehicle
    public boolean jaHaValorat(String matricula, String dni) {
        return resenyaRepo.existsByVehicleMatriculaAndClientDni(matricula, dni.trim().toUpperCase());
    }

    /**
     * Registra una nova ressenya per a un vehicle després de validar les dades:
     * Verifica l'existència del vehicle i del client, comprova que no hi hagi
     * una ressenya prèvia, valida el rang de puntuació (1-5) i la longitud
     * màxima del comentari (500 caràcters). Persisteix la nova entitat
     * RessenyaEntity amb la data i hora actuals dins d'una transacció.
     */
    @Transactional
    public void crear(String matricula, String dni, int puntuacio, String comentari) {
        var vehicle = vehicleRepo.findById(matricula)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle no trobat"));
        var client = clientRepo.findById(dni.trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Client no trobat"));

        // Només una ressenya per client i vehicle
        if (resenyaRepo.existsByVehicleMatriculaAndClientDni(matricula, client.getDni())) {
            throw new IllegalArgumentException("Ja has escrit una ressenya per aquest vehicle");
        }

        if (puntuacio < 1 || puntuacio > 5) {
            throw new IllegalArgumentException("La puntuació ha de ser entre 1 i 5");
        }

        String text = (comentari == null || comentari.isBlank()) ? null : comentari.trim();
        if (text != null && text.length() > 500) {
            throw new IllegalArgumentException("El comentari no pot superar els 500 caràcters");
        }

        RessenyaEntity r = new RessenyaEntity();
        r.setVehicle(vehicle);
        r.setClient(client);
        r.setPuntuacio(puntuacio);
        r.setComentari(text);
        r.setDataCreacio(LocalDateTime.now());
        resenyaRepo.save(r);
    }

    /**
     * Elimina una ressenya del sistema pel seu identificador (operació
     * d'administració): Comprova prèviament que la ressenya existeixi a la base
     * de dades i procedeix al seu esborrat transaccional.
     */
    @Transactional
    public void eliminar(Long codi) {
        if (!resenyaRepo.existsById(codi)) {
            throw new IllegalArgumentException("Ressenya no trobada");
        }
        resenyaRepo.deleteById(codi);
    }

    /**
     * Elimina una ressenya verificada per autoria del client: Recupera la
     * ressenya pel seu codi, comprova que pertanyi al DNI del client
     * sol·licitant i l'esborra de la base de dades. Llença una
     * IllegalArgumentException si la valoració no existeix o l'usuari no n'és
     * l'autor.
     */
    @Transactional
    public void eliminarDelClient(Long codi, String dni) {
        RessenyaEntity r = resenyaRepo.findById(codi)
                .orElseThrow(() -> new IllegalArgumentException("Ressenya no trobada"));

        if (r.getClient() == null || dni == null
                || !r.getClient().getDni().equalsIgnoreCase(dni.trim())) {
            throw new IllegalArgumentException("Només pots eliminar les teves ressenyes");
        }
        resenyaRepo.delete(r);
    }
}

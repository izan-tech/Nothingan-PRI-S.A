/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Usuaris.Agent.Service;

import cat.copernic.projecte1Grup3.Usuaris.Agent.Entity.AgentEntity;
import cat.copernic.projecte1Grup3.Usuaris.Agent.Repository.AgentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servei de negoci per a la gestió d'agents i administradors: Implementa la
 * lògica de validació, encriptació de contrasenyes, consulta i eliminació
 * d'agents. Garanteix el compliment de les regles de negoci com la validesa del
 * DNI/NIE i la majoria d'edat abans de persistir.
 *
 * @author Grup3
 */
@Service
public class AgentService {

    private static final Logger log = LoggerFactory.getLogger(AgentService.class);

    private final PasswordEncoder passwordEncoder;
    private final AgentRepository agentRepository;

    /**
     * Injecció de dependències del codificador de contrasenyes i del repositori
     * d'agents: Inicialitza el servei rebent PasswordEncoder i AgentRepository
     * per gestionar les dades dels agents.
     */
    public AgentService(PasswordEncoder passwordEncoder, AgentRepository agentRepository) {
        this.passwordEncoder = passwordEncoder;
        this.agentRepository = agentRepository;
    }

    /**
     * Valida i desa un agent nou o actualitza les dades d'un agent existent:
     * Comprova el format i la lletra del DNI/NIE, verifica que tingui almenys
     * 18 anys i gestiona el xifratge de la contrasenya. Manté la contrasenya
     * actual en cas d'edició si no se n'ha proporcionat cap de nova i registra
     * l'operació al log.
     */
    public void guardarAgent(AgentEntity agent) {
        String dni = agent.getDni() == null ? "" : agent.getDni().trim().toUpperCase();
        if (!esDniValid(dni)) {
            throw new IllegalArgumentException(
                    "El DNI/NIE no és vàlid (ha de tenir 8 xifres i la lletra correcta, o ser un NIE vàlid)");
        }
        agent.setDni(dni);

        if (agent.getDataNaixement() == null) {
            throw new IllegalArgumentException("La data de naixement és obligatòria");
        }
        if (agent.getDataNaixement().isAfter(LocalDate.now().minusYears(18))) {
            throw new IllegalArgumentException("L'agent ha de tenir com a mínim 18 anys");
        }

        Optional<AgentEntity> existent = agentRepository.findById(dni);
        if (existent.isPresent()) {
            String passwordGuardada = existent.get().getPassword();
            String passwordRebuda = agent.getPassword();
            if (passwordRebuda == null || passwordRebuda.isBlank()
                    || passwordRebuda.equals(passwordGuardada)) {
                agent.setPassword(passwordGuardada);
            } else {
                agent.setPassword(passwordEncoder.encode(passwordRebuda));
            }
        } else if (agent.getPassword() != null && !agent.getPassword().isBlank()) {
            agent.setPassword(passwordEncoder.encode(agent.getPassword()));
        }

        agentRepository.save(agent);
        log.info("Agent donat d'alta: {}", agent);
    }

    /**
     * Verifica la validesa formal i el dígit de control d'un DNI o NIE
     * espanyol: Comprova l'expressió regular de 8 dígits (o lletra inicial X,
     * Y, Z) seguits de la lletra de control calculada per mòdul 23.
     */
    private boolean esDniValid(String dni) {
        if (!dni.matches("^([0-9]{8}|[XYZ][0-9]{7})[A-Z]$")) {
            return false;
        }
        String numeroStr = dni.substring(0, 8)
                .replace("X", "0")
                .replace("Y", "1")
                .replace("Z", "2");
        int numero = Integer.parseInt(numeroStr);
        String lletresValides = "TRWAGMYFPDXBNJZSQVHLCKE";
        return dni.charAt(8) == lletresValides.charAt(numero % 23);
    }

    /**
     * Recupera el llistat complet de tots els agents registrats: Consulta i
     * retorna totes les entitats AgentEntity emmagatzemades a la base de dades.
     */
    public List<AgentEntity> llistarAgents() {
        return agentRepository.findAll();
    }

    /**
     * Cerca un agent específic pel seu número de DNI/NIE: Retorna un Optional
     * amb l'entitat trobada o buit si no existeix cap coincidència.
     */
    public Optional<AgentEntity> obtenirPerDni(String dni) {
        return agentRepository.findById(dni);
    }

    /**
     * Elimina definitivament un agent de la base de dades a partir del seu DNI:
     * Executa l'esborrat al repositori i deixa constància de la baixa
     * mitjançant un registre d'avís al log.
     */
    public void eliminarAgent(String dni) {
        agentRepository.deleteById(dni);
        log.warn("Agent eliminat: {}", dni);
    }

    /**
     * Retorna tots els agents o filtra els resultats segons un text de cerca:
     * Si el paràmetre de cerca és nul o buit retorna el llistat sencer; en cas
     * contrari filtra per DNI, nom, rol o correu electrònic.
     */
    public List<AgentEntity> llistarOFiltrarAgents(String cerca) {
        if (cerca == null || cerca.trim().isEmpty()) {
            return agentRepository.findAll();
        }
        return agentRepository.cercarPerDniNomEmail(cerca.trim());
    }
}

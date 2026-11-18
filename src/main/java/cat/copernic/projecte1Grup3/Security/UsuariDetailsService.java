/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Security;

import java.util.Optional;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import cat.copernic.projecte1Grup3.Usuaris.Agent.Entity.AgentEntity;
import cat.copernic.projecte1Grup3.Usuaris.Agent.Repository.AgentRepository;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.ClientEntity;
import cat.copernic.projecte1Grup3.Usuaris.Entity.UsuariEntity;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.ClientRepository;

/**
 * Servei d'autenticació personalitzat per a Spring Security: Implementa
 * UserDetailsService per carregar les credencials, rols i estat d'activació
 * dels usuaris a partir del seu correu electrònic. Cerca primer a la taula
 * d'agents/administradors i, si no existeix, consulta la taula de clients
 * verificant-ne l'estat de validació.
 *
 * @author Grup3
 */
@Service
public class UsuariDetailsService implements UserDetailsService {

    private final AgentRepository agentRepository;
    private final ClientRepository clienRepository;

    /**
     * Injecció de dependències dels repositoris d'agents i clients: Inicialitza
     * el servei rebent AgentRepository i ClientRepository per cercar l'usuari
     * que intenta iniciar sessió.
     */
    public UsuariDetailsService(AgentRepository agentRepository,
            ClientRepository clientRepository) {
        this.agentRepository = agentRepository;
        this.clienRepository = clientRepository;
    }

    /**
     * Carrega les dades de seguretat d'un usuari pel seu correu electrònic
     * durant el login: Comprova si el correu pertany a un agent o administrador
     * (assignant-li el seu rol) o a un client (habilitat només si està
     * VALIDAT). Llença una UsernameNotFoundException si no es troba cap compte
     * registrat amb aquest correu electrònic.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<AgentEntity> agent = agentRepository.findByEmailIgnoreCase(email);
        if (agent.isPresent()) {
            return construir(agent.get(), agent.get().getRol().name());
        }

        ClientEntity client = clienRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuari no trobat: " + email));
        return construir(client, "CLIENT", "VALIDAT".equals(client.getEstatValidacio()));
    }

    /**
     * Construeix un objecte UserDetails habilitat per defecte per a usuaris
     * interns: Delega al mètode principal establint l'indicador d'habilitació a
     * true.
     */
    private UserDetails construir(UsuariEntity usuari, String rol) {
        return construir(usuari, rol, true);
    }

    /**
     * Crea l'instància UserDetails de Spring Security a partir de l'entitat
     * d'usuari: Verifica que l'usuari tingui una contrasenya configurada i
     * construeix el perfil amb correu, contrasenya, rol i estat d'activació.
     */
    private UserDetails construir(UsuariEntity usuari, String rol, boolean habilitat) {
        if (usuari.getPassword() == null || usuari.getPassword().isBlank()) {
            throw new UsernameNotFoundException(
                    "L'usuari no té contrasenya configurada: " + usuari.getEmail());
        }
        return User.builder()
                .username(usuari.getEmail())
                .password(usuari.getPassword())
                .roles(rol)
                .disabled(!habilitat)
                .build();
    }
}

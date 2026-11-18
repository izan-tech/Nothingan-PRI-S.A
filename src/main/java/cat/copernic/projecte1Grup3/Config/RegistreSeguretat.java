/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/**
 * Auditoria d'esdeveniments d'autenticació de Spring Security: Escolta de
 * manera automàtica els esdeveniments generats durant l'inici de sessió dels
 * usuaris. Deixa constància als fitxers de log tant dels accessos correctes com
 * dels intents fallits.
 *
 * @author Grup3
 */
@Component
public class RegistreSeguretat {

    private static final Logger log = LoggerFactory.getLogger(RegistreSeguretat.class);

    /**
     * Registra els intents d'inici de sessió fallits per credencials errònies:
     * Captura l'esdeveniment de seguretat quan un usuari introdueix una
     * contrasenya o usuari incorrecte. Escriu un avís de nivell WARN al log
     * indicant el nom de l'usuari que ha fallat l'accés.
     */
    @EventListener
    public void loginFallit(AuthenticationFailureBadCredentialsEvent event) {
        log.warn("Intent d'inici de sessió fallit per a l'usuari: {}", event.getAuthentication().getName());
    }

    /**
     * Registra els inicis de sessió completats correctament: Captura
     * l'esdeveniment d'autenticació exitosa emès per Spring Security. Escriu un
     * missatge informatiu (INFO) al log amb el nom de l'usuari autenticat.
     */
    @EventListener
    public void loginCorrecte(AuthenticationSuccessEvent event) {
        log.info("Inici de sessió correcte: {}", event.getAuthentication().getName());
    }
}

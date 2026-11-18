/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.ClientRepository;

/**
 * Assessor global de controladors per injectar dades de l'usuari autenticat a
 * totes les vistes: Exposa de manera transversal atributs de model comuns
 * relacionats amb la sessió activa de Spring Security. Permet mostrar la
 * inicial identificativa de l'usuari a la capçalera o barra de navegació de
 * qualsevol plantilla HTML.
 *
 * @author Grup3
 */
@ControllerAdvice
public class GlobalUserAdvice {

    private final ClientRepository clientRepository;

    /**
     * Injecció del repositori de clients: Inicialitza el component rebent
     * ClientRepository per poder consultar el nom real de l'usuari autenticat.
     */
    public GlobalUserAdvice(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    /**
     * Calcula i exposa la lletra inicial de l'usuari connectat com a atribut de
     * model global: Comprova si existeix una sessió autenticada no anònima i
     * cerca el client pel seu correu electrònic per extreure la inicial del seu
     * nom. Si no és un client registrat o no té nom, utilitza la primera lletra
     * del correu electrònic o un valor per defecte ("C" o "U").
     */
    @ModelAttribute("inicialUsuari")
    public String obtenirInicialUsuari(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        String email = auth.getName();
        return clientRepository.findByEmailIgnoreCase(email)
                .map(c -> (c.getNom() != null && !c.getNom().isBlank())
                ? c.getNom().trim().substring(0, 1).toUpperCase()
                : "C")
                .orElse(email != null && !email.isBlank() ? email.substring(0, 1).toUpperCase() : "U");
    }
}

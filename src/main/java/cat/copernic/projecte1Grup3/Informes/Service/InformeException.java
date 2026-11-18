/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Informes.Service;

/**
 * Excepció personalitzada per a errors en la generació d'informes: S'utilitza
 * per notificar situacions anòmales o regles de negoci incomplertes durant el
 * càlcul d'informes. Permet als controladors capturar aquests errors específics
 * i mostrar missatges clars a l'usuari.
 *
 * @author Grup3
 */
public class InformeException extends Exception {

    /**
     * Constructor amb missatge descriptiu de l'error: Inicialitza l'excepció
     * passant el detall de l'incident al constructor de la classe pare
     * Exception.
     */
    public InformeException(String message) {
        super(message);
    }
}

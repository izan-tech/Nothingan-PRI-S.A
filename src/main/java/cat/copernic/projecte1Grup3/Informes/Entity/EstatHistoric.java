/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Informes.Entity;

/**
 * Enumeració dels estats possibles d'una reserva a l'històric: Defineix el
 * cicle de vida complet d'una operació registrada a la base de dades
 * documental. Inclou tant els estats actius (pendent, confirmada, en curs) com
 * els de tancament (finalitzada, cancel·lada o eliminada).
 *
 * @author Grup3
 */
public enum EstatHistoric {
    PENDENT,
    CONFIRMADA,
    EN_CURS,
    FINALITZADA,
    CANCELADA,
    ELIMINADA
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Usuaris.Agent.Entity;

/**
 * Enumeració que defineix els rols disponibles per al personal intern de
 * l'aplicació: Diferencia entre els agents operatius (AGENT) i els
 * administradors amb privilegis totals (ADMIN). S'utilitza per controlar
 * l'accés a funcionalitats restringides mitjançant Spring Security.
 *
 * @author Grup3
 */
public enum Rol {
    AGENT,
    ADMIN
}

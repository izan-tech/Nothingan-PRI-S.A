/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Classe principal d'arrencada de l'aplicació Spring Boot: Configura l'escaneig
 * global d'entitats i indica explícitament els paquets on es troben els
 * repositoris. Habilita la persistència dual combinant bases de dades
 * relacionals (JPA) i documentals (MongoDB).
 *
 * @author Grup3
 */
@SpringBootApplication
// Le decimos a Spring dónde están los repositorios JPA i MongoDB
@EnableJpaRepositories(basePackages = {
    "cat.copernic.projecte1Grup3.Vehicles.Repository",
    "cat.copernic.projecte1Grup3.Usuaris.Client.Repository",
    "cat.copernic.projecte1Grup3.Reserves.Repository",// Si clientRepository és JPA
    "cat.copernic.projecte1Grup3.Usuaris.Client.Repository", // Si clientRepository és JPA
    "cat.copernic.projecte1Grup3.Usuaris.Agent.Repository",
    "cat.copernic.projecte1Grup3.ressenya.Repository"
})
@EnableMongoRepositories(basePackages = {
    "cat.copernic.projecte1Grup3.Usuaris.Client.Repository",
    "cat.copernic.projecte1Grup3.Reserves.Repository",
    "cat.copernic.projecte1Grup3.Informes.Repository"

})
@EntityScan(basePackages = "cat.copernic.projecte1Grup3")
public class Projecte1Grup3Application {

    /**
     * Punt d'entrada principal que executa l'aplicació: Inicialitza el context
     * de Spring Boot i posa en marxa el servidor web integrat.
     */
    public static void main(String[] args) {
        SpringApplication.run(Projecte1Grup3Application.class, args);
    }
}

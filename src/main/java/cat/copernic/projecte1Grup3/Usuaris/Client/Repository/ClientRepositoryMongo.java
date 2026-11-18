/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Usuaris.Client.Repository;

import cat.copernic.projecte1Grup3.Usuaris.Client.Documents.ClientDocuments;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repositori Spring Data MongoDB per a l'accés als documents d'identificació
 * dels clients: Gestiona la persistència i consulta dels documents binaris
 * (ClientDocuments) indexats pel DNI com a identificador únic. Proporciona les
 * operacions CRUD estàndard sobre la col·lecció "client_documents" de MongoDB.
 *
 * @author User
 */
public interface ClientRepositoryMongo extends MongoRepository<ClientDocuments, String> {

}

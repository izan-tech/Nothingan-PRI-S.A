/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Usuaris.Client.Dades;

import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.ClientEntity;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.ClientRepository;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuració per a la càrrega inicial de dades de clients de prova: Executa
 * un CommandLineRunner a l'inici de l'aplicació per netejar registres orfes i
 * poblar la base de dades amb clients de mostra. Crea 20 perfils amb diferents
 * nacionalitats, permisos, estats de validació (VALIDAT/PENDENT), nivell
 * prèmium i contrasenyes xifrades.
 *
 * @author Grup3
 */
@Configuration
public class DadesProva {

    /**
     * Defineix el bean d'inicialització que inserta els clients de prova a la
     * base de dades: Elimina prèviament possibles registres de clients sense
     * fila pare a la taula d'usuaris i dona d'alta els perfils predefinits.
     */
    @Bean
    CommandLineRunner carregarClients(ClientRepository repo, PasswordEncoder passwordEncoder, JdbcTemplate jdbcTemplate) {
        return args -> {
            // Elimina files òrfenes de clients que no tinguin registre pare a usuaris
            try {
                jdbcTemplate.execute("DELETE c FROM `clients` c LEFT JOIN `usuaris` u ON c.`dni` = u.`dni` WHERE u.`dni` IS NULL");
            } catch (Exception ignored) {
            }

            crea(repo, passwordEncoder, "12345678Z", "Joan Garcia Soler", "joan@test.com", "12345678", "612345678",
                    LocalDate.of(1990, 5, 14), "Espanyola", "Carrer Major 12, Terrassa", "B",
                    LocalDate.of(2030, 3, 1), LocalDate.of(2032, 7, 20), "VALIDAT", true);

            crea(repo, passwordEncoder, "33918274J", "Marta Puigdomènech Vila", "marta.client@test.com", "t7O9TIPtr6", "623456789",
                    LocalDate.of(1988, 9, 22), "Espanyola", "Avinguda Jacquard 45, Terrassa", "B",
                    LocalDate.of(2031, 6, 15), LocalDate.of(2033, 4, 10), "VALIDAT", true);

            crea(repo, passwordEncoder, "X5482910D", "Lucas Silva Mendes", "lucas.client@test.com", "sN3dTBXI0v", "634567890",
                    LocalDate.of(1994, 2, 11), "Portuguesa", "Carrer de la Font Vella 8, Terrassa", "B",
                    LocalDate.of(2029, 11, 30), LocalDate.of(2031, 8, 12), "VALIDAT", false);

            crea(repo, passwordEncoder, "44192837Q", "Anna Ribas Coll", "anna.client@test.com", "1Z9nUyb5uV", "645678901",
                    LocalDate.of(1997, 7, 3), "Espanyola", "Rambla d'Ègara 110, Terrassa", "B",
                    LocalDate.of(2032, 1, 19), LocalDate.of(2034, 9, 5), "VALIDAT", true);

            crea(repo, passwordEncoder, "47829103M", "David Serrano López", "david.client@test.com", "BpwGGh48dt", "656789012",
                    LocalDate.of(1985, 12, 18), "Espanyola", "Carrer Galileu 24, Terrassa", "C",
                    LocalDate.of(2030, 8, 25), LocalDate.of(2029, 12, 14), "VALIDAT", false);

            crea(repo, passwordEncoder, "Y3829104D", "Sofia Rossi Conti", "sofia.client@test.com", "fuh0COYbAc", "667890123",
                    LocalDate.of(1992, 4, 27), "Italiana", "Passeig Comte d'Ègara 19, Terrassa", "B",
                    LocalDate.of(2031, 10, 8), LocalDate.of(2033, 2, 28), "VALIDAT", true);

            crea(repo, passwordEncoder, "36918274P", "Sergi Masip Tordera", "sergi.client@test.com", "1ocAFhAYzy", "678901234",
                    LocalDate.of(1999, 1, 9), "Espanyola", "Carrer Sant Pere 33, Sabadell", "B",
                    LocalDate.of(2033, 5, 17), LocalDate.of(2035, 3, 22), "PENDENT", false);

            crea(repo, passwordEncoder, "51283940N", "Elena Navarro Ortiz", "elena.client@test.com", "9cipVoGyb2", "689012345",
                    LocalDate.of(1991, 8, 30), "Espanyola", "Carrer Balmes 72, Barcelona", "B",
                    LocalDate.of(2030, 12, 4), LocalDate.of(2032, 11, 11), "VALIDAT", true);

            crea(repo, passwordEncoder, "Z4829103M", "Mateo Rojas Herrera", "mateo.client@test.com", "iL34A4UhPd", "690123456",
                    LocalDate.of(1995, 6, 16), "Colombiana", "Carrer Archimedes 54, Terrassa", "B",
                    LocalDate.of(2029, 9, 2), LocalDate.of(2031, 6, 30), "VALIDAT", false);

            crea(repo, passwordEncoder, "42918374Y", "Cristina Vilaró Camps", "cristina.client@test.com", "Si558lRgfF", "611223344",
                    LocalDate.of(1983, 11, 5), "Espanyola", "Avinguda Abat Marcet 88, Terrassa", "B",
                    LocalDate.of(2031, 3, 14), LocalDate.of(2030, 7, 19), "VALIDAT", true);

            crea(repo, passwordEncoder, "49182736L", "Adrià Fontanet Boada", "adria.client@test.com", "yLect2PLZG", "622334455",
                    LocalDate.of(2001, 3, 25), "Espanyola", "Carrer del Nord 15, Terrassa", "B",
                    LocalDate.of(2034, 4, 9), LocalDate.of(2035, 8, 1), "PENDENT", false);

            crea(repo, passwordEncoder, "X6192837H", "Claire Dubois Laurent", "claire.client@test.com", "E1zlz1Ysev", "633445566",
                    LocalDate.of(1989, 10, 12), "Francesa", "Carrer Muntaner 140, Barcelona", "B",
                    LocalDate.of(2030, 7, 21), LocalDate.of(2032, 5, 16), "VALIDAT", true);

            crea(repo, passwordEncoder, "35829104A", "Roger Esteve Prat", "roger.client@test.com", "WL0cn6c52p", "644556677",
                    LocalDate.of(1987, 5, 29), "Espanyola", "Carretera de Matadepera 60, Terrassa", "A2",
                    LocalDate.of(2031, 9, 13), LocalDate.of(2033, 10, 27), "VALIDAT", false);

            crea(repo, passwordEncoder, "53192847B", "Nerea Molina Sánchez", "nerea.client@test.com", "mhdYNdqzF6", "655667788",
                    LocalDate.of(1996, 12, 2), "Espanyola", "Carrer de la Rasa 41, Terrassa", "B",
                    LocalDate.of(2032, 8, 6), LocalDate.of(2034, 1, 15), "VALIDAT", true);

            crea(repo, passwordEncoder, "Y7392018Q", "Andrei Popescu Ionescu", "andrei.client@test.com", "xiAUFSYN6R", "666778899",
                    LocalDate.of(1993, 7, 19), "Romanesa", "Carrer Colom 95, Terrassa", "C",
                    LocalDate.of(2030, 2, 24), LocalDate.of(2029, 11, 3), "VALIDAT", false);

            crea(repo, passwordEncoder, "46829103E", "Gemma Solsona Rovira", "gemma.client@test.com", "9ST9sxQY03", "677889900",
                    LocalDate.of(1990, 8, 8), "Espanyola", "Plaça Vella 4, Terrassa", "B",
                    LocalDate.of(2031, 11, 29), LocalDate.of(2033, 9, 18), "VALIDAT", true);

            crea(repo, passwordEncoder, "39817264D", "Òscar Ferrer Busquets", "oscar.client@test.com", "jckTe6Xq7a", "688990011",
                    LocalDate.of(1984, 3, 17), "Espanyola", "Carrer Indústria 18, Sabadell", "B",
                    LocalDate.of(2030, 6, 10), LocalDate.of(2031, 12, 25), "VALIDAT", false);

            crea(repo, passwordEncoder, "Z2938471L", "Camila Vargas Soto", "camila.client@test.com", "WQHX6Lwcrh", "699001122",
                    LocalDate.of(1998, 10, 23), "Xilena", "Carrer Sant Antoni 27, Terrassa", "B",
                    LocalDate.of(2033, 2, 12), LocalDate.of(2034, 6, 7), "PENDENT", false);

            crea(repo, passwordEncoder, "52193847Q", "Xavier Llorens Roca", "xavier.client@test.com", "tj1wQ9paDZ", "610203040",
                    LocalDate.of(1986, 6, 1), "Espanyola", "Avinguda Barcelona 132, Terrassa", "B",
                    LocalDate.of(2031, 4, 28), LocalDate.of(2032, 8, 14), "VALIDAT", true);

            crea(repo, passwordEncoder, "41928375L", "Sílvia Domènech Gil", "silvia.client@test.com", "NEf7kIzMCV", "620304050",
                    LocalDate.of(1995, 11, 14), "Espanyola", "Carrer Pantà 50, Terrassa", "B",
                    LocalDate.of(2032, 9, 19), LocalDate.of(2034, 12, 9), "VALIDAT", false);

            crea(repo, passwordEncoder, "58294716R", "Kerry Kopenis", "kerrykopenis@gmail.com", "password123", "611223355",
                    LocalDate.of(1992, 4, 12), "Estatunidenca", "Carrer de la Pau 18, Terrassa", "B",
                    LocalDate.of(2031, 5, 20), LocalDate.of(2033, 8, 14), "VALIDAT", false);
        };
    }

    /**
     * Crea i desa un client individual si no existeix prèviament a la base de
     * dades: Si el client ja existeix, s'assegura que la seva contrasenya
     * estigui xifrada amb BCrypt; en cas contrari, construeix la nova entitat
     * completa.
     */
    private void crea(ClientRepository repo, PasswordEncoder passwordEncoder,
            String dni, String nom, String email, String password, String tel,
            LocalDate naix, String nac, String adreca, String llicencia,
            LocalDate capDni, LocalDate capCarnet, String estat, boolean premium) {
        String pwdFinal = (password == null || password.isBlank()) ? "password123" : password;

        ClientEntity existing = repo.findById(dni).orElse(null);
        if (existing != null) {
            String pwdActual = existing.getPassword();
            if (pwdActual == null || pwdActual.isBlank()) {
                existing.setPassword(passwordEncoder.encode(pwdFinal));
                repo.save(existing);
            } else if (!esContrasenyaBcrypt(pwdActual)) {
                existing.setPassword(passwordEncoder.encode(pwdActual));
                repo.save(existing);
            }
            return;
        }

        ClientEntity c = new ClientEntity();
        c.setDni(dni);
        c.setNom(nom);
        c.setEmail(email);
        c.setPassword(passwordEncoder.encode(pwdFinal));
        c.setTelefon(tel);
        c.setDataNaixement(naix);
        c.setNacionalitat(nac);
        c.setAdreca(adreca);
        c.setTipusLlicencia(llicencia);
        c.setDataCaducitatDNI(capDni);
        c.setDataCaducitatCarnet(capCarnet);
        c.setEstatValidacio(estat);
        c.setPremium(premium);
        repo.save(c);
    }

    /**
     * Sobrecàrrega del mètode de creació de clients utilitzant una contrasenya
     * per defecte: Delega al mètode principal assignant el valor "password123"
     * com a contrasenya inicial.
     */
    private void crea(ClientRepository repo, PasswordEncoder passwordEncoder,
            String dni, String nom, String email, String tel,
            LocalDate naix, String nac, String adreca, String llicencia,
            LocalDate capDni, LocalDate capCarnet, String estat, boolean premium) {
        crea(repo, passwordEncoder, dni, nom, email, "password123", tel, naix, nac, adreca, llicencia, capDni, capCarnet, estat, premium);
    }

    /**
     * Comprova si una cadena de contrasenya ja té el format d'un hash BCrypt
     * vàlid: Verifica mitjançant expressió regular el prefix estàndard de
     * BCrypt ($2a$, $2b$, $2y$) i la seva longitud de 60 caràcters.
     */
    private boolean esContrasenyaBcrypt(String password) {
        return password.matches("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");
    }
}

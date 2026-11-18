/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Usuaris.Agent.Dades;

import cat.copernic.projecte1Grup3.Usuaris.Agent.Entity.AgentEntity;
import cat.copernic.projecte1Grup3.Usuaris.Agent.Entity.Rol;
import cat.copernic.projecte1Grup3.Usuaris.Agent.Repository.AgentRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuració per a la càrrega inicial de dades d'agents i administradors:
 * Executa un CommandLineRunner a l'arrencada de l'aplicació per poblar la base
 * de dades amb usuaris interns de prova. Assigna rols (ADMIN o AGENT), encripta
 * les contrasenyes i vincula automàticament les fotografies disponibles al
 * servidor.
 *
 * @author Grup3
 */
@Configuration
public class DadesAgents {

    /**
     * Defineix el bean d'inicialització que inserta els administradors i agents
     * per defecte: Llegeix les imatges disponibles a la carpeta d'agents i dona
     * d'alta 4 administradors i 16 agents amb els seus horaris.
     */
    @Bean
    CommandLineRunner carregarAgents(AgentRepository repo, PasswordEncoder passwordEncoder) {
        return args -> {
            List<String> fotos = obtenirFotosCarpeta("agents");
            int[] idx = {0};

            // 4 Administradors (contrasenya: 12345678)
            crea(repo, passwordEncoder, fotos, idx, "45936880S", "Hugo López Soldado", "h@test.com", "12345678", "08:00", "14:30", Rol.ADMIN);
            crea(repo, passwordEncoder, fotos, idx, "X8392014G", "Brais Anfruns Rodriguez", "b@test.com", "12345678", "08:00", "14:30", Rol.ADMIN);
            crea(repo, passwordEncoder, fotos, idx, "53918274H", "Izan Lazaro Egea", "i@test.com", "12345678", "08:00", "14:30", Rol.ADMIN);
            crea(repo, passwordEncoder, fotos, idx, "Y4729183Y", "Yuri Andrei Martinello", "y@test.com", "12345678", "08:00", "14:30", Rol.ADMIN);

            // 16 Agents
            crea(repo, passwordEncoder, fotos, idx, "41283947M", "Laura Vidal Roca", "laura.agent@test.com", "FsSNLQnI6p", "08:00", "16:00", Rol.AGENT);
            crea(repo, passwordEncoder, fotos, idx, "48920134P", "Marc Ferrer Costa", "marc.agent@test.com", "DQxCQs7Nt0", "09:00", "17:00", Rol.AGENT);
            crea(repo, passwordEncoder, fotos, idx, "X3948172S", "Núria Serra Pons", "nuria.agent@test.com", "f3PJLJmZE9", "14:00", "22:00", Rol.AGENT);
            crea(repo, passwordEncoder, fotos, idx, "39284710C", "Pau Martí Soler", "pau.agent@test.com", "WixINFj3Mi", "08:00", "14:30", Rol.AGENT);
            crea(repo, passwordEncoder, fotos, idx, "52839104P", "Carla Puig Rovira", "carla.agent@test.com", "4Js8KuE0K7", "08:00", "16:00", Rol.AGENT);
            crea(repo, passwordEncoder, fotos, idx, "Y6182934L", "Oriol Camps Torres", "oriol.agent@test.com", "H8ZS4KsUs5", "09:00", "17:00", Rol.AGENT);
            crea(repo, passwordEncoder, fotos, idx, "46392018Y", "Mireia Bosch Giménez", "mireia.agent@test.com", "vr4TbosEjZ", "14:00", "22:00", Rol.AGENT);
            crea(repo, passwordEncoder, fotos, idx, "38492017E", "Pol Navarro Ruiz", "pol.agent@test.com", "HVD2AcfNuB", "08:00", "14:30", Rol.AGENT);
            crea(repo, passwordEncoder, fotos, idx, "Z1928374J", "Alba Mora Castillo", "alba.agent@test.com", "87y3f56QCM", "08:00", "16:00", Rol.AGENT);
            crea(repo, passwordEncoder, fotos, idx, "51928374D", "Arnau Riera Mas", "arnau.agent@test.com", "2nItmrVlw4", "09:00", "17:00", Rol.AGENT);
            crea(repo, passwordEncoder, fotos, idx, "43829104M", "Laia Fonts Esteve", "laia.agent@test.com", "eeTSfO903a", "14:00", "22:00", Rol.AGENT);
            crea(repo, passwordEncoder, fotos, idx, "X7283910V", "Biel Duran Ortiz", "biel.agent@test.com", "3AF6GjIu5I", "08:00", "14:30", Rol.AGENT);
            crea(repo, passwordEncoder, fotos, idx, "49283716Y", "Júlia Valls Moreno", "julia.agent@test.com", "63xfdu0Aj8", "08:00", "16:00", Rol.AGENT);
            crea(repo, passwordEncoder, fotos, idx, "37192845M", "Jan Sala Domínguez", "jan.agent@test.com", "1cf5LC9aX3", "09:00", "17:00", Rol.AGENT);
            crea(repo, passwordEncoder, fotos, idx, "Y2839471C", "Clàudia Prat Iglesias", "claudia.agent@test.com", "SPt9sOc3tl", "14:00", "22:00", Rol.AGENT);
            crea(repo, passwordEncoder, fotos, idx, "54829103R", "Nil Casanovas Grau", "nil.agent@test.com", "6NRrwwkMTI", "08:00", "14:30", Rol.AGENT);
        };
    }

    /**
     * Crea i persisteix un agent individual si encara no existeix a la base de
     * dades: Si l'agent ja existeix però no té fotografia assignada, li
     * actualitza la imatge; en cas contrari, construeix la nova entitat amb la
     * contrasenya xifrada.
     */
    private void crea(AgentRepository repo, PasswordEncoder passwordEncoder,
            List<String> fotos, int[] idx,
            String dni, String nom, String email, String password,
            String horaEntrada, String horaSortida, Rol rol) {
        String fotoAssignada = triarFotoAgent(fotos, idx, nom, dni);

        AgentEntity existing = repo.findById(dni).orElse(null);
        if (existing != null) {
            if ((existing.getFoto() == null || existing.getFoto().isBlank()) && fotoAssignada != null) {
                existing.setFoto(fotoAssignada);
                repo.save(existing);
            }
            return;
        }

        String pwdFinal = (password == null || password.isBlank()) ? "password123" : password;

        AgentEntity a = new AgentEntity();
        a.setDni(dni);
        a.setNom(nom);
        a.setEmail(email);
        a.setPassword(passwordEncoder.encode(pwdFinal));
        a.setDataNaixement(LocalDate.of(1995, 6, 15));
        a.setHora_entrada(horaEntrada);
        a.setHora_sortida(horaSortida);
        a.setRol(rol);
        a.setFoto(fotoAssignada);
        repo.save(a);
    }

    /**
     * Selecciona la fotografia més adequada per a un agent dins la llista de
     * fitxers disponibles: Busca primer una coincidència pel primer nom o pel
     * DNI de l'agent i, si no n'hi ha cap, assigna la següent imatge de forma
     * rotativa.
     */
    private String triarFotoAgent(List<String> fotos, int[] idx, String nom, String dni) {
        if (fotos.isEmpty()) {
            return null;
        }
        String primerNom = nom.split("\\s+")[0].toLowerCase();
        for (String f : fotos) {
            String base = f.toLowerCase();
            if (base.startsWith(primerNom + ".") || base.startsWith(dni.toLowerCase() + ".")) {
                return f;
            }
        }
        String f = fotos.get(idx[0] % fotos.size());
        idx[0]++;
        return f;
    }

    /**
     * Llista els noms dels fitxers d'imatge vàlids dins d'una subcarpeta
     * d'uploads: Filtra els fitxers regulars amb extensions gràfiques (.jpg,
     * .jpeg, .png, .webp) i els retorna ordenats alfabèticament.
     */
    private List<String> obtenirFotosCarpeta(String subcarpeta) {
        Path dir = Paths.get("uploads", subcarpeta);
        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .filter(nom -> {
                        String lower = nom.toLowerCase();
                        return lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                                || lower.endsWith(".png") || lower.endsWith(".webp");
                    })
                    .sorted()
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Vehicles.Dades;

import cat.copernic.projecte1Grup3.Vehicles.Entity.VehiclesEntity;
import cat.copernic.projecte1Grup3.Vehicles.Repository.VehiclesRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuració per a la càrrega inicial de dades de la flota de vehicles:
 * Executa un CommandLineRunner en arrencar l'aplicació per poblar la base de dades amb 20 vehicles de mostra.
 * Assigna automàticament les fotografies disponibles a la carpeta local segons matrícula, marca, model o ordre seqüencial.
 *
 * @author Grup3
 */
@Configuration
public class DadesVehicles {

    /**
     * Defineix el bean d'inicialització que carrega el catàleg inicial de vehicles a la base de dades:
     * Llegeix les imatges disponibles al directori "uploads/vehicles" i dona d'alta turismes, compactes i furgonetes.
     */
    @Bean
    CommandLineRunner carregarVehicles(VehiclesRepository repo) {
        return args -> {
            List<String> fotos = obtenirFotosCarpeta("vehicles");
            int[] idx = {0};

            crea(repo, fotos, idx, "1234ABC", "Seat", "Ibiza", 2021, "Utilitari compacte, ideal per a ciutat",
                    "Turisme", 300, "12.50", "300.00", 1, 14, "DISPONIBLE", true, "Revisió feta al març");

            crea(repo, fotos, idx, "5678DEF", "Volkswagen", "Golf", 2022, "Berlina còmoda per a viatges llargs",
                    "Turisme", 400, "18.00", "500.00", 1, 21, "DISPONIBLE", true, null);

            crea(repo, fotos, idx, "9012GHI", "Ford", "Transit", 2020, "Furgoneta de càrrega gran",
                    "Furgoneta", 250, "25.00", "700.00", 2, 30, "MANTENIMENT", false, "Pendent canvi de frens");

            crea(repo, fotos, idx, "3456JKL", "Tesla", "Model 3", 2023, "Elèctric amb gran autonomia",
                    "Compacte", 500, "35.00", "1000.00", 1, 10, "DISPONIBLE", true, null);

            crea(repo, fotos, idx, "4821MNB", "Toyota", "Corolla", 2023, "Híbrid eficient i silenciós per a ciutat i carretera",
                    "Turisme", 400, "19.50", "450.00", 1, 21, "DISPONIBLE", true, "Pneumàtics nous");

            crea(repo, fotos, idx, "7394KLP", "Peugeot", "208", 2022, "Compacte àgil amb baix consum",
                    "Compacte", 300, "13.00", "300.00", 1, 15, "DISPONIBLE", true, null);

            crea(repo, fotos, idx, "1958RST", "Renault", "Clio", 2021, "Utilitari pràctic i fàcil d'aparcar",
                    "Compacte", 300, "12.00", "300.00", 1, 14, "DISPONIBLE", true, null);

            crea(repo, fotos, idx, "6204BVG", "Hyundai", "Tucson", 2024, "Vehicle espaiós i confortable per a família",
                    "Turisme", 450, "24.00", "600.00", 1, 25, "DISPONIBLE", true, "Assegurança a tot risc");

            crea(repo, fotos, idx, "8519CDF", "Kia", "Niro", 2023, "Híbrid endollable amb etiqueta zero emissions",
                    "Turisme", 400, "22.50", "550.00", 1, 20, "DISPONIBLE", true, null);

            crea(repo, fotos, idx, "3047HJK", "Mercedes-Benz", "Sprinter", 2021, "Furgoneta industrial de gran capacitat",
                    "Furgoneta", 300, "29.00", "850.00", 2, 30, "DISPONIBLE", true, "Revisió ITV vigent");

            crea(repo, fotos, idx, "9182LMN", "Audi", "A3", 2022, "Compacte prèmium amb acabats esportius",
                    "Compacte", 400, "26.00", "700.00", 1, 15, "DISPONIBLE", true, null);

            crea(repo, fotos, idx, "2673PRV", "BMW", "Sèrie 1", 2023, "Conducció dinàmica i equipament complet",
                    "Compacte", 400, "27.50", "750.00", 1, 15, "DISPONIBLE", true, null);

            crea(repo, fotos, idx, "5490STV", "Citroën", "Berlingo", 2020, "Furgoneta lleugera versàtil per a repartiment",
                    "Furgoneta", 300, "20.00", "500.00", 1, 30, "DISPONIBLE", true, null);

            crea(repo, fotos, idx, "7812WXZ", "Dacia", "Sandero", 2022, "Opció econòmica i fiable per a desplaçaments diaris",
                    "Compacte", 300, "10.50", "250.00", 1, 21, "DISPONIBLE", true, null);

            crea(repo, fotos, idx, "1465BCD", "Cupra", "Formentor", 2024, "Model esportiu amb altes prestacions",
                    "Turisme", 450, "32.00", "900.00", 1, 14, "DISPONIBLE", true, "Només conductors majors de 25 anys");

            crea(repo, fotos, idx, "8930FGH", "Opel", "Corsa", 2021, "Compacte urbà de baix manteniment",
                    "Compacte", 300, "11.50", "300.00", 1, 14, "MANTENIMENT", false, "Revisió d'oli i filtres");

            crea(repo, fotos, idx, "4127JKL", "Nissan", "Qashqai", 2022, "Turisme ampli amb gran maleter",
                    "Turisme", 400, "21.00", "550.00", 1, 21, "DISPONIBLE", true, null);

            crea(repo, fotos, idx, "6753MNP", "Fiat", "Ducato", 2019, "Furgoneta de transport per a mudances",
                    "Furgoneta", 250, "24.00", "700.00", 2, 30, "DISPONIBLE", true, null);

            crea(repo, fotos, idx, "2389RTV", "Skoda", "Octavia", 2023, "Berlina familiar amb molt d'espai interior",
                    "Turisme", 450, "18.50", "450.00", 1, 25, "DISPONIBLE", true, null);

            crea(repo, fotos, idx, "9541VWX", "Toyota", "Yaris", 2024, "Compacte híbrid ideal per a trànsit urbà",
                    "Compacte", 350, "14.00", "350.00", 1, 14, "DISPONIBLE", true, null);
        };
    }

    /**
     * Crea i desa un vehicle a la base de dades si no existeix prèviament:
     * Si el vehicle ja està registrat però no té foto assignada, li vincula una imatge disponible; en cas contrari, inserta la nova entitat.
     */
    private void crea(VehiclesRepository repo, List<String> fotos, int[] idx,
            String matricula, String marca, String model, Integer any,
            String descripcio, String tipus, Integer limitKm,
            String preuHora, String fianca, Integer minDies, Integer maxDies,
            String estat, Boolean actiu, String comentaris) {
        String fotoAssignada = triarFotoVehicle(fotos, idx, matricula, marca, model);

        VehiclesEntity existing = repo.findById(matricula).orElse(null);
        if (existing != null) {
            if ((existing.getFoto() == null || existing.getFoto().isBlank()) && fotoAssignada != null) {
                existing.setFoto(fotoAssignada);
                repo.save(existing);
            }
            return;
        }

        VehiclesEntity v = new VehiclesEntity(matricula, marca, model, any, descripcio, tipus,
                limitKm, new BigDecimal(preuHora), new BigDecimal(fianca),
                minDies, maxDies, estat, actiu, comentaris);
        v.setFoto(fotoAssignada);
        repo.save(v);
    }

    /**
     * Selecciona el nom del fitxer d'imatge més adequat per a un vehicle determinat:
     * Cerca coincidències per matrícula, model o marca dins dels fitxers disponibles i, si no en troba, n'assigna un per torn rotatiu.
     */
    private String triarFotoVehicle(List<String> fotos, int[] idx, String matricula, String marca, String model) {
        if (fotos.isEmpty()) {
            return null;
        }
        String matLower = matricula.toLowerCase();
        String modLower = model.toLowerCase().replaceAll("\\s+", "");
        String marLower = marca.toLowerCase().replaceAll("\\s+", "");

        for (String f : fotos) {
            String base = f.toLowerCase();
            if (base.contains(matLower) || base.contains(modLower) || base.contains(marLower)) {
                return f;
            }
        }
        String f = fotos.get(idx[0] % fotos.size());
        idx[0]++;
        return f;
    }

    /**
     * Llegeix i retorna ordenats els noms dels fitxers d'imatge d'una subcarpeta d'uploads:
     * Filtra únicament els fitxers regulars amb extensió JPG, JPEG, PNG o WEBP i retorna una llista buida si hi ha errors d'accés.
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
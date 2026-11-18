/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3;

import cat.copernic.projecte1Grup3.Vehicles.Entity.VehiclesEntity;
import cat.copernic.projecte1Grup3.Vehicles.Repository.VehiclesRepository;
import cat.copernic.projecte1Grup3.Vehicles.Service.VehiclesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Classe de proves unitàries amb JUnit 5 i Mockito per validar la lògica de negoci de VehiclesService:
 * Comprova el comportament per defecte en desar nous vehicles, la cerca de disponibilitat amb dates i sense dates.
 * Verifica també el canvi d'estat i d'activitat durant el procés de desactivació d'un vehicle.
 *
 * @author Grup3
 */
@ExtendWith(MockitoExtension.class)
class VehiclesServiceTest {

    @Mock
    private VehiclesRepository vehiclesRepository;

    @InjectMocks
    private VehiclesService vehiclesService;

    private VehiclesEntity vehicleExemple;

    /**
     * Prepara l'objecte vehicle de mostra abans de l'execució de cada prova unitària:
     * Inicialitza una instància de VehiclesEntity amb matrícula, marca, model, tipus i estat actiu per defecte.
     */
    @BeforeEach
    void setUp() {
        vehicleExemple = new VehiclesEntity();
        vehicleExemple.setMatricula("1234ABC");
        vehicleExemple.setMarca("Tesla");
        vehicleExemple.setModel("Model 3");
        vehicleExemple.setTipusVehicle("Elèctric");
        vehicleExemple.setActiu(true);
    }

    /**
     * Comprova que en donar d'alta un nou vehicle amb el camp actiu nul s'activa automàticament:
     * Verifica que el vehicle retornat té l'atribut actiu a veritable i s'invoca save() al repositori.
     */
    @Test
    @DisplayName("Comprovar que guardarVehicle assigna actiu=true si és un nou vehicle i actiu ve nul")
    void testGuardarVehicleNouAssignaActiuTrue() {
        vehicleExemple.setActiu(null);
        when(vehiclesRepository.findById("1234ABC")).thenReturn(Optional.empty());
        when(vehiclesRepository.save(any(VehiclesEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VehiclesEntity resultat = vehiclesService.guardarVehicle(vehicleExemple);

        assertNotNull(resultat);
        assertTrue(resultat.getActiu());
        verify(vehiclesRepository, times(1)).save(vehicleExemple);
    }

    /**
     * Verifica que consultar vehicles disponibles sense especificar dates delega al llistat de vehicles actius:
     * Comprova que s'invoca findByActiuTrue() i s'evita executar la consulta de solapament de reserves per dates.
     */
    @Test
    @DisplayName("Comprovar que llistarDisponibles crida a llistarActiusPerTipus quan les dates són nul·les")
    void testLlistarDisponiblesSenseDates() {
        when(vehiclesRepository.findByActiuTrue()).thenReturn(List.of(vehicleExemple));

        List<VehiclesEntity> resultat = vehiclesService.llistarDisponibles(null, null, "");

        assertNotNull(resultat);
        assertEquals(1, resultat.size());
        assertEquals("1234ABC", resultat.get(0).getMatricula());
        verify(vehiclesRepository, times(1)).findByActiuTrue();
        verify(vehiclesRepository, never()).llistarDisponiblesPerDatesYTipus(any(), any(), any());
    }

    /**
     * Comprova que la cerca de disponibilitat amb rang de dates executa la consulta filtrada al repositori:
     * Valida que es passa la data d'inici, la data de fi i la categoria al mètode llistarDisponiblesPerDatesYTipus().
     */
    @Test
    @DisplayName("Comprovar que llistarDisponibles crida al repositori amb dates quan s'especifiquen")
    void testLlistarDisponiblesAmbDates() {
        LocalDateTime inici = LocalDateTime.of(2026, 6, 1, 10, 0);
        LocalDateTime fi = LocalDateTime.of(2026, 6, 5, 10, 0);

        when(vehiclesRepository.llistarDisponiblesPerDatesYTipus(inici, fi, "Turisme"))
                .thenReturn(List.of(vehicleExemple));

        List<VehiclesEntity> resultat = vehiclesService.llistarDisponibles(inici, fi, "Turisme");

        assertNotNull(resultat);
        assertEquals(1, resultat.size());
        verify(vehiclesRepository, times(1)).llistarDisponiblesPerDatesYTipus(inici, fi, "Turisme");
    }

    /**
     * Verifica que la desactivació d'un vehicle n'actualitza l'estat operatiu i l'inhabilita al catàleg:
     * Comprova que el camp actiu passa a fals, l'estat canvia a "EN MANTENIMENT" i es persisteix l'entitat.
     */
    @Test
    @DisplayName("Comprovar que desactivarVehicle marca el vehicle com a inactiu")
    void testDesactivarVehicle() {
        when(vehiclesRepository.findById("1234ABC")).thenReturn(Optional.of(vehicleExemple));

        vehiclesService.desactivarVehicle("1234ABC", null);

        assertFalse(vehicleExemple.getActiu());
        assertEquals("EN MANTENIMENT", vehicleExemple.getEstat());
        verify(vehiclesRepository, times(1)).save(vehicleExemple);
    }
}
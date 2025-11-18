package com.udistrital.gestionacademica.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "estudiante")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Estudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_estudiante")
    private Long codigoEstudiante;

    @Column(name = "id_acudiente")
    private Long idAcudiente;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 100)
    private String apellido;

    @Column(name = "documento", unique = true, length = 20)
    private String documento;

    // REFACTORIZADO: Cambio de String a Grupo
    @ManyToOne
    @JoinColumn(name = "id_grupo")
    private Grupo grupo;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "Activo";

    @Column(name = "genero", length = 20)
    private String genero;

    @Column(name = "fecha_nacimiento")
    private LocalDateTime fechaNacimiento;

    // Método auxiliar para calcular la edad
    public Integer calcularEdad() {
        if (fechaNacimiento == null) {
            return null;
        }
        return LocalDateTime.now().getYear() - fechaNacimiento.getYear();
    }

    // Método auxiliar para obtener el nombre del grupo
    public String getNombreGrupo() {
        if (grupo == null) {
            return "Sin grupo";
        }
        return grupo.getGrado().getNombreGrado() + " - Grupo " + grupo.getNumeroGrupo();
    }
}
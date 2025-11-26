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
public class Estudiante extends Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_estudiante")
    private Long codigoEstudiante;

    @ManyToOne
    @JoinColumn(name = "id_persona", nullable = false)
    private Persona persona;

    @Column(name = "id_acudiente")
    private Long idAcudiente;

    @ManyToOne
    @JoinColumn(name = "id_grupo")
    private Grupo grupo;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "Activo";

 
    // Método auxiliar para obtener el nombre del grupo
    public String getNombreGrupo() {
        if (grupo == null) {
            return "Sin grupo";
        }
        return grupo.getGrado().getNombreGrado() + " - Grupo " + grupo.getNumeroGrupo();
    }
}
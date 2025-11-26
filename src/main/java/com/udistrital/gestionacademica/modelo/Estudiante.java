package com.udistrital.gestionacademica.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "estudiante")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Estudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigoEstudiante")
    private Long codigoEstudiante;

    @ManyToOne
    @JoinColumn(name = "idAcudiente")
    private Acudiente acudiente;

    @ManyToOne
    @JoinColumn(name = "idPersona")
    private Persona persona;

    @ManyToOne
    @JoinColumn(name = "idGrupo")
    private Grupo grupo;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "Pendiente";

    // Método auxiliar para obtener el nombre del grupo
    public String getNombreGrupo() {
        if (grupo == null) {
            return "Sin grupo";
        }
        return grupo.getGrado().getNombreGrado() + " - Grupo " + grupo.getNumeroGrupo();
    }
}

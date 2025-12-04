package com.udistrital.gestionacademica.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "citacion")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Citacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_citacion")
    private Long idCitacion;

    @ManyToOne
    @JoinColumn(name = "idAcudiente", nullable = false)
    private Acudiente acudiente;

    @ManyToOne
    @JoinColumn(name = "codigoEstudiante", nullable = false)
    private Estudiante estudiante;

    @Column(name = "fecha_citacion", nullable = false)
    private LocalDateTime fechaCitacion;
}

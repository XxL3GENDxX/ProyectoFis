package com.udistrital.gestionacademica.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "preinscripcion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Preinscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPreinscripcion")
    private Long idPreinscripcion;

    @ManyToOne
    @JoinColumn(name = "idEstudiante", nullable = false)
    private Estudiante aspirante;

    @Column(name = "fechaEntrevista")
    private LocalDateTime fechaEntrevista;

    @Column(name = "lugarEntrevista", length = 100)
    private String lugarEntrevista;

    @Column(name = "fechaPreinscripcion", nullable = false)
    private LocalDateTime fechaPreinscripcion;

    @ManyToOne
    @JoinColumn(name = "idAcudiente", nullable = false)
    private Acudiente acudiente;

}

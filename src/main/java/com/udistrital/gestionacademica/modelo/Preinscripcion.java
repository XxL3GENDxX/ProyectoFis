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

    @Column(name = "fechaPreinscripcion", nullable = false)
    private LocalDateTime fechaEntrevista;
    //creo que sobra la hora
    @Column(name = "horaEntrevista", length = 20)
    private LocalTime horaEntrevista;

    @ManyToOne
    @JoinColumn(name = "idAcudiente", nullable = false)
    private Acudiente acudiente;
    
}

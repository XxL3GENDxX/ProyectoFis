
package com.udistrital.gestionacademica.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "calificacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Calificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_calificacion")
    private Long idCalificacion;

    @ManyToOne
    @JoinColumn(name = "codigo_estudiante", nullable = false)
    private Estudiante estudiante;

    @ManyToOne
    @JoinColumn(name = "id_logro", nullable = false)
    private Logro logro;

    @ManyToOne
    @JoinColumn(name = "id_periodo", nullable = false)
    private Periodo periodo;

    @ManyToOne
    @JoinColumn(name = "id_profesor")
    private Profesor profesor;

    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDateTime fechaAsignacion;



    @PrePersist
    protected void onCreate() {
        fechaAsignacion = LocalDateTime.now();
    }
}
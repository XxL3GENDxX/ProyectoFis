package com.udistrital.gestionacademica.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Entity
@Table(name = "logro")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Logro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idLogro")
    private Long idLogro;

    @Column(name = "nombreLogro", nullable = false, length = 150)
    private String nombreLogro;

    @Column(name = "categoriaLogro", nullable = false, length = 50)
    private String categoriaLogro;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "estado", nullable = false)
    private Boolean estado = true;

    @ManyToMany(mappedBy = "logros", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Estudiante> estudiantes;

}

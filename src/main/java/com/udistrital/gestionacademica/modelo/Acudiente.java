package com.udistrital.gestionacademica.modelo;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "acudiente")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Acudiente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAcudiente")
    private Long idAcudiente;

    @ManyToOne
    @JoinColumn(name = "id_persona", nullable = false)
    private Persona persona;

    @Column(name = "correoElectronico", nullable = false, length = 50)
    private String correoElectronico;

    @Column(name = "estado", length = 15)
    private String estado = "Pendiente";

    @Column(name = "telefono", length = 100)
    private String telefono;

    @OneToMany(mappedBy = "acudiente", cascade = CascadeType.ALL)
    @MapKeyColumn(name = "codigoEstudiante")
    @JsonIgnore
    private Map<Integer, Estudiante> estudiantes = new HashMap<>();

}

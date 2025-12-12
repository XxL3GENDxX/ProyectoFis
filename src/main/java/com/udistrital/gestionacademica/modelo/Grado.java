package com.udistrital.gestionacademica.modelo;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "grado")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Grado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_grado")
    private Long idGrado;

    @Column(name = "nombre_grado", nullable = false, unique = true, length = 50)
    private String nombreGrado;

    @OneToMany(mappedBy = "grado", cascade = CascadeType.ALL)
    @MapKeyColumn(name = "numero_grupo")
    @JsonIgnore
    private Map<Integer, Grupo> grupos = new HashMap<>();
}
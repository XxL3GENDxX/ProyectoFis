package com.udistrital.gestionacademica.modelo;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "grupo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_grupo")
    private Long idGrupo;

    @Column(name = "numero_grupo", nullable = false)
    private Integer numeroGrupo;

    @Column(name = "director_grupo", length = 100)
    private String directorGrupo;

    @Column(name = "limite_estudiantes", nullable = false)
    private Integer limiteEstudiantes = 30; // Límite por defecto

    @ManyToOne
    @JoinColumn(name = "id_grado", nullable = false)
    private Grado grado;

    @OneToMany(mappedBy = "grupo", cascade = CascadeType.ALL)
    @MapKeyColumn(name = "codigo_estudiante")
    @JsonIgnore
    private Map<Integer, Estudiante> estudiantes = new HashMap<>();

    // Método para verificar si el grupo está completo
    public boolean estaCompleto() {
        return estudiantes.size() >= limiteEstudiantes;
    }

    // Método para obtener el número de estudiantes actuales
    public int getNumeroEstudiantes() {
        return estudiantes.size();
    }
}
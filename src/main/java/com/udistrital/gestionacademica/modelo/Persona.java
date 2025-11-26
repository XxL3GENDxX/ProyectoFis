package com.udistrital.gestionacademica.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Entity
@Table(name = "persona")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_persona")
    private Long idPersona;

    @Column(name = "documento", unique = true, length = 20)
    private String documento;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 100)
    private String apellido;

    @Column(name = "fecha_de_nacimiento")
    private LocalDateTime fechaDeNacimiento;

    @Column(name = "genero", length = 20)
    private String genero;

    // Relación con TokenUsuario
    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL)
    private ArrayList<TokenUsuario> usuarios = new ArrayList<>();

    // Método auxiliar para calcular la edad
    public Integer calcularEdad() {
        if (fechaDeNacimiento == null) {
            return null;
        }
        return LocalDateTime.now().getYear() - fechaDeNacimiento.getYear();
    }
    
}

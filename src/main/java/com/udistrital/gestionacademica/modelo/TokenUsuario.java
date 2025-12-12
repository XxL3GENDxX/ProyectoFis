package com.udistrital.gestionacademica.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "token_usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_token_usuario")
    private Long idTokenUsuario;

    @Column(name = "nombre_usuario", nullable = false, length = 100, unique = true)
    private String nombreUsuario;

    @Column(name = "contrasena", nullable = false, length = 100)
    private String contrasena;

    @Column(name = "estado", nullable = false)
    private Boolean estado;

    @ManyToOne
    @JoinColumn(name = "idPersona")
    private Persona persona;

    @Column(name = "rol", nullable = false, length = 50)
    private String rol;

}

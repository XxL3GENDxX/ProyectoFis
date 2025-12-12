package com.udistrital.gestionacademica.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "logro")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Logro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_logro")
    private Long idLogro;

    @Column(name = "nombre_logro", nullable = false, length = 200)
    private String nombreLogro;

    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;

    @Column(name = "categoria", nullable = false, length = 50)
    private String categoria;
}
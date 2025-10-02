package com.follysitou.sygpress.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    @Column(unique = true)
    private String name;  // T-shirt, Chemise, Drap, Serviette

    @ManyToOne
    private Category category; // Vêtements, Drap, Serviette, Chaussures

    @OneToMany(mappedBy = "article")
    private List<Pricing> pricing;
}

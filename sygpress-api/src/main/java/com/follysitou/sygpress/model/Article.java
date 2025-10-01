package com.follysitou.sygpress.model;

import jakarta.persistence.*;
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
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String name;  // T-shirt, Chemise, Drap, Serviette

    @ManyToOne
    private Category category; // Vêtements, Drap, Serviette, Chaussures

    @OneToMany(mappedBy = "article")
    private List<Pricing> pricing;
}

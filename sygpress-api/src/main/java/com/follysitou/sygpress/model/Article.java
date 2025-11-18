package com.follysitou.sygpress.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Article extends BaseEntity {


    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    @Column(unique = true)
    private String name;  // T-shirt, Chemise, Drap, Serviette

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category; // Vêtements, Drap, Serviette, Chaussures

    @OneToMany(mappedBy = "article", fetch = FetchType.LAZY)
    private List<Pricing> pricing = new ArrayList<>();
}

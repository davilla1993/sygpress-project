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
public class Category extends BaseEntity {

    @Column(unique = true)
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 50, message = "Le nom ne peut pas dépasser 50 caractères")
    private String name; // Exemple: "Vêtement", "Drap", "Serviette

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Article> articles = new ArrayList<>();
}

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
public class Service extends BaseEntity {

    @Column(unique = true)
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 50, message = "Le nom ne peut pas dépasser 50 caractères")
    private String name;                    // "Lavage", "Repassage", "Lavage&Repassage

    @OneToMany(mappedBy = "service", fetch = FetchType.LAZY)
    private List<Pricing> pricing = new ArrayList<>();
}

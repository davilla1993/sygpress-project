package com.follysitou.sygpress.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints =
@UniqueConstraint(columnNames = {"article_id","service_id"}))
public class Pricing {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotNull(message = "Le prix est obligatoire")
    @Min(value = 0, message = "Le prix ne peut pas être négatif")
    private double price;

    @ManyToOne
    @NotNull(message = "L'article est obligatoire")
    private Article article;

    @ManyToOne
    @NotNull(message = "Le service est obligatoire")
    private Service service;
}

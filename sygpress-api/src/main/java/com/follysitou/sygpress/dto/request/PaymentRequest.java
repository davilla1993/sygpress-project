package com.follysitou.sygpress.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {

    @NotNull(message = "Le montant du paiement est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant du paiement doit être supérieur à 0")
    private BigDecimal amount;
}

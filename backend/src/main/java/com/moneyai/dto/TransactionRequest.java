package com.moneyai.dto;

import com.moneyai.model.TransactionType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
        @NotBlank @Size(max = 120) String description,
        @NotNull @Positive BigDecimal amount,
        @NotNull TransactionType type,
        @Size(max = 60) String category,
        LocalDate date) {
}

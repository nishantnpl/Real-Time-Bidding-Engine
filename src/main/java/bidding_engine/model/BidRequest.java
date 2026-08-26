package bidding_engine.model;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BidRequest(

        @NotBlank(message = "bidderId is required")
        String bidderId,

        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be greater than 0")
        BigDecimal amount
) {
}
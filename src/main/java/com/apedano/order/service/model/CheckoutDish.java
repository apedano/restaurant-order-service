package com.apedano.order.service.model;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class CheckoutDish extends BaseDish {
    private BigDecimal price;
}

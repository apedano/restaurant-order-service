package com.apedano.order.service.model;


import com.apedano.restaurant.api.model.OrderDishStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderDish extends BaseDish {

    @Enumerated(EnumType.STRING)
    private OrderDishStatus orderDishStatus;


}


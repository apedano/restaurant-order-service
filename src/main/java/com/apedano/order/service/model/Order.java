package com.apedano.order.service.model;

import com.apedano.restaurant.api.model.OrderStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Entity(name = "Orders")
@Data
@EqualsAndHashCode
public class Order {
    @Id
    @GeneratedValue
    private Long id;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private String name;
    @Column(unique = true)
    private String orderId;
    private int numberOfPeople;
    @ManyToOne(optional=false)
    @JoinColumn(name="table_id", nullable=false) //in this table
    private Table table;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderDish> orderDishes = new LinkedList<>();

}

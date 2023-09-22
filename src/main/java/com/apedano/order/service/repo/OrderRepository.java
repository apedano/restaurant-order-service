package com.apedano.order.service.repo;

import com.apedano.order.service.model.Order;
import com.apedano.restaurant.api.model.OrderStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class OrderRepository implements PanacheRepository<Order>{

    public Optional<Order> findByOrderId(String orderId) {
        return find("orderId", orderId).firstResultOptional();
    }

    public List<Order> findByTableName(String tableName) {
        return find("table.name = ?1 ", tableName).stream().toList();
    }

    public List<Order> findByTableNameAndStatus(String tableName, OrderStatus status) {
        return find("table.name = ?1 and status=?2", tableName, status).stream().toList();
    }

    public List<Order> findByTableNameAndNotStatus(String tableName, OrderStatus status) {
        return find("table.name = ?1 and status!=?2", tableName, status).stream().toList();
    }



}
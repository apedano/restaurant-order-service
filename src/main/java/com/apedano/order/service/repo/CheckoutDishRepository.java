package com.apedano.order.service.repo;

import com.apedano.order.service.model.CheckoutDish;
import com.apedano.order.service.model.Table;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;



@ApplicationScoped
public class CheckoutDishRepository implements PanacheRepository<CheckoutDish> {

    public Optional<CheckoutDish> findByName(String name) {
        return find("name = ?1 ", name).stream().findFirst();
    }
}


package com.apedano.order.service.repo;

import com.apedano.order.service.model.Table;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class TableRepository implements PanacheRepository<Table>  {

    public Optional<Table> findByTableName(String tableName) {
        return find("name = ?1 ", tableName).stream().findFirst();
    }
}

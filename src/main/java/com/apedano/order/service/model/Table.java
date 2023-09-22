package com.apedano.order.service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity(name="tables")
@Data
@EqualsAndHashCode
public class Table {
    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String name;

    @Override
    public String toString() {
        return "Table{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}

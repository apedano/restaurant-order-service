package com.apedano.order.service.serialization;

import com.apedano.order.service.model.Order;
import com.apedano.restaurant.api.model.NewOrder;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class OrderDeserializer extends ObjectMapperDeserializer<Order> {
    public OrderDeserializer() {
        super(Order.class);
    }
}

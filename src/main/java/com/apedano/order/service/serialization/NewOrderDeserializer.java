package com.apedano.order.service.serialization;

import com.apedano.restaurant.api.model.NewOrder;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class NewOrderDeserializer extends ObjectMapperDeserializer<NewOrder> {
    public NewOrderDeserializer() {
        super(NewOrder.class);
    }
}

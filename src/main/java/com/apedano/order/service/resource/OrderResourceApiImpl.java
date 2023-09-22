package com.apedano.order.service.resource;

import com.apedano.order.service.OrderService;
import com.apedano.restaurant.api.OrderResourceApi;
import com.apedano.restaurant.api.model.OrderDto;
import com.apedano.restaurant.api.model.OrderedDishes;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import java.util.List;

@Path("/api/restaurant/orders/")
public class OrderResourceApiImpl implements OrderResourceApi {

    @Inject
    OrderService orderService;

    @Override
    @GET
    @Path("open")
    public List<OrderDto> getOpenOrders() {
        return null;
    }

    @Override
    @GET
    @Path("available-tables")
    public List<String> getAvailableTables() {
        return null;
    }

    @Override
    @POST
    @Path("add-dishes")
    public void addDishesToOrder(OrderedDishes orderedDishes) {
        orderService.addDishesToOrder(orderedDishes);
    }

    @POST
    @Path("add-dishes-to-last")
    public void addDishesToOrderToLast(OrderedDishes orderedDishes) {
        orderService.addDishesToLastOrder(orderedDishes);
    }
}

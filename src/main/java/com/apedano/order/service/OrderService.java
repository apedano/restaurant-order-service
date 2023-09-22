package com.apedano.order.service;

import com.apedano.order.service.model.CheckoutDish;
import com.apedano.order.service.model.Order;
import com.apedano.order.service.model.OrderDish;
import com.apedano.order.service.repo.CheckoutDishRepository;
import com.apedano.order.service.repo.OrderRepository;
import com.apedano.order.service.repo.TableRepository;
import com.apedano.restaurant.api.model.*;
import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.apedano.restaurant.api.model.OrderDishStatus.ORDERED;
import static com.apedano.restaurant.api.model.OrderDishStatus.SERVED;

@ApplicationScoped
@Slf4j
public class OrderService {
    @Inject
    OrderRepository orderRepository;

    @Inject
    TableRepository tableRepository;

    @Inject
    CheckoutDishRepository checkoutDishRepository;

    @Inject
    @Channel("order-updates")
    Emitter<OrderDto> orderUpdatesEmitter;

    @Inject
    @Channel("kitchen-incoming")
    Emitter<KitchenOrderDto> kitchenOrderEmitter;

    /**
     * The messaging operation
     * Reactive Messaging invokes your method on an I/O thread.
     * But, you often need to combine Reactive Messaging with blocking processing such as database interactions.
     * For this, you need to use the @Blocking annotation indicating that the processing is blocking and should not be run on the caller thread.
     * https://quarkus.io/guides/kafka#blocking-processing
     *
     * @param newOrder
     */
    @Incoming("new-order")
    @Blocking
    @Transactional
    public void handleNewOrder(NewOrder newOrder) {
        log.info("Processing received new order: {}", newOrder);
        Order order = createNewOrder(newOrder);
        orderUpdatesEmitter.send(toDto(order));
        orderRepository.persist(order);

    }

    @Incoming("dish-served")
    @Transactional
    public void addDishServed(KitchenOrderDto kitchenOrderDto) {
        log.info("Adding served dish to order:{}", kitchenOrderDto);
        Order order = orderRepository.findByOrderId(kitchenOrderDto.getOrderId())
                .orElseThrow(IllegalArgumentException::new);
        OrderDish dish = findDishToBeServed(order, kitchenOrderDto.getDishName());
        dish.setOrderDishStatus(SERVED);
        updateStaus(order);
        orderRepository.persist(order);
        orderUpdatesEmitter.send(toDto(order)).toCompletableFuture().join();

    }

    private void updateStaus(Order order) {
        boolean isServed =
                order.getOrderDishes().stream().map(OrderDish::getOrderDishStatus)
                        .allMatch(staus -> staus.equals(SERVED));
        if(isServed) {
            order.setStatus(OrderStatus.CLOSED);
        }
    }

    private OrderDish findDishToBeServed(Order order, String dishName) {
        return order.getOrderDishes().stream()
                .filter(orderDish -> orderDish.getOrderDishStatus().equals(ORDERED) && orderDish.getName().equals(dishName))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("No dish found to serve for: " + order.getOrderId()));
    }


    @Transactional
    public void addDishesToOrder(OrderedDishes orderedDishes) {
        String orderId = orderedDishes.getOrderId();
        List<String> dishNames = orderedDishes.getDishNames();
        log.info("Adding [{}] to order [{}]", dishNames, orderId);
        Order order = orderRepository.findByOrderId(orderId).orElseThrow(IllegalArgumentException::new);
        List<KitchenOrderDto> kitchenOrderDtos = dishNames.stream()
                .map(dishName -> checkoutDishRepository.findByName(dishName).orElseThrow(IllegalAccessError::new))
                .map(checkoutDish -> this.fromCheckoutDish(order, checkoutDish))
                .toList();
        if(order.getStatus().equals(OrderStatus.NEW)) {
            order.setStatus(OrderStatus.ORDERED);
        }
        orderRepository.persist(order);
        log.info("Notifying kitchen for [{}] and orderId [{}]", dishNames, orderId);
        kitchenOrderDtos.forEach(kitchenOrderEmitter::send);
        log.info("Sending order update for [{}]", orderId);
        //this allows the method to be transactinal and return in the same transactional thread
        orderUpdatesEmitter.send(toDto(order)).toCompletableFuture().join();;
    }

    KitchenOrderDto fromCheckoutDish(Order order, CheckoutDish checkoutDish) {
        OrderDish orderDish = new OrderDish();
        orderDish.setOrderDishStatus(ORDERED);
        orderDish.setName(checkoutDish.getName());
        order.getOrderDishes().add(orderDish);
        return KitchenOrderDto.builder()
                .dishName(checkoutDish.getName())
                .orderId(order.getOrderId())
                .build();
    }


    public Order createNewOrder(NewOrder newOrder) {
        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());
        order.setStatus(OrderStatus.NEW);
        order.setName(newOrder.getName());
        order.setNumberOfPeople(newOrder.getNumberOfPeople());
        order.setTable(tableRepository.findByTableName(
                newOrder.getTableName()).orElseThrow(IllegalArgumentException::new)
        );

        return order;
    }

    private OrderDto toDto(Order order) {
        return OrderDto.builder()
                .orderId(order.getOrderId())
                .orderName(order.getName())
                .orderStatus(order.getStatus())
                .table(order.getTable().getName())
                .orderDishes(order.getOrderDishes().stream()
                        .map(this::toDto).toList())
                .build();

    }

    private OrderDishDto toDto(OrderDish orderDish) {
        return OrderDishDto.builder()
                .dishName(orderDish.getName())
                .orderDishStatus(orderDish.getOrderDishStatus())
                .build();
    }


    private boolean isAvailable(String tablename) {
        return findNonClosed(tablename).isEmpty();
    }

    public List<Order> findNonClosed(String tableName) {
        List<Order> allByTable = this.orderRepository.findByTableName(tableName);
        return allByTable.stream().filter(order -> !OrderStatus.PAYED.equals(order.getStatus())).collect(Collectors.toList());
    }

    public Optional<Order> findNewByTableName(String tableName) {
        List<Order> newByTable = this.orderRepository.findByTableNameAndStatus(tableName, OrderStatus.NEW);
        if (newByTable.size() > 1) {
            throw new IllegalStateException(String.format("Multiple NEW order for table %s", tableName));
        }
        return newByTable.stream().findFirst();
    }


    public void addDishesToLastOrder(OrderedDishes orderedDishes) {
        Order last = this.orderRepository.findAll().stream()
                .max(Comparator.comparing(Order::getId))
                .orElseThrow(IllegalArgumentException::new);
        orderedDishes.setOrderId(last.getOrderId());
        addDishesToOrder(orderedDishes);
    }
}

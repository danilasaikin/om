package com.example.om.controller;

import com.example.om.entity.Customer;
import com.example.om.entity.Order;
import com.example.om.entity.Product;
import com.example.om.repo.CustomerRepository;
import com.example.om.repo.OrderRepository;
import com.example.om.repo.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody Order order) {
        if (order.getCustomer() != null && order.getCustomer().getCustomerId() != null) {
            Optional<Customer> customer = customerRepository.findById(order.getCustomer().getCustomerId());
            if (!customer.isPresent()) {
                return new ResponseEntity<>("Customer not found", HttpStatus.BAD_REQUEST);
            }
            order.setCustomer(customer.get());
        }

        List<Product> products = order.getProducts();
        if (products != null && !products.isEmpty()) {
            for (Product product : products) {
                Optional<Product> productOptional = productRepository.findById(product.getProductId());
                if (!productOptional.isPresent()) {
                    return new ResponseEntity<>("Product not found with ID: " + product.getProductId(), HttpStatus.BAD_REQUEST);
                }
            }
        }

        double totalPrice = products.stream().mapToDouble(Product::getPrice).sum();
        order.setTotalPrice(totalPrice);

        Order savedOrder = orderRepository.save(order);
        return new ResponseEntity<>(savedOrder, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        Optional<Order> order = orderRepository.findById(id);
        return order.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(@PathVariable Long id, @Valid @RequestBody Order orderDetails) {
        return orderRepository.findById(id).map(order -> {
            order.setOrderDate(orderDetails.getOrderDate());
            order.setShippingAddress(orderDetails.getShippingAddress());
            order.setOrderStatus(orderDetails.getOrderStatus());

            if (orderDetails.getCustomer() != null && orderDetails.getCustomer().getCustomerId() != null) {
                Optional<Customer> customer = customerRepository.findById(orderDetails.getCustomer().getCustomerId());
                if (!customer.isPresent()) {
                    return new ResponseEntity<>("Customer not found", HttpStatus.BAD_REQUEST);
                }
                order.setCustomer(customer.get());
            }

            List<Product> products = orderDetails.getProducts();
            if (products != null && !products.isEmpty()) {
                for (Product product : products) {
                    Optional<Product> productOptional = productRepository.findById(product.getProductId());
                    if (!productOptional.isPresent()) {
                        return new ResponseEntity<>("Product not found with ID: " + product.getProductId(), HttpStatus.BAD_REQUEST);
                    }
                }
                order.setProducts(products);
            }

            double totalPrice = products.stream().mapToDouble(Product::getPrice).sum();
            order.setTotalPrice(totalPrice);

            Order updatedOrder = orderRepository.save(order);
            return ResponseEntity.ok(updatedOrder);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        return orderRepository.findById(id).map(order -> {
            orderRepository.delete(order);
            return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}



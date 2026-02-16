package com.capstone.model;

import com.capstone.domain.PaymentType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double totalAmount;

    private LocalDateTime createdAt;

    @ManyToOne
//    @JsonIgnore
    private Branch branch;

    @ManyToOne
//    @JsonIgnore
    private Users cashier;

    @ManyToOne
    private Customer customer;

    private PaymentType paymentType;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;

//    private OrderStatus status=OrderStatus.COMPLETED;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }
}


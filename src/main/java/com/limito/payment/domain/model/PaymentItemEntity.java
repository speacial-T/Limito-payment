package com.limito.payment.domain.model;

import com.limito.common.security.audit.BaseEntity;
import com.limito.payment.domain.enums.ProductTypeEnum;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "p_payment_items")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class PaymentItemEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_item_id", columnDefinition = "UUID")
    UUID paymentItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    PaymentEntity payment;

    @Column(name = "seller_id")
    Long sellerId;

    @Column(name = "order_item_id", nullable = false, columnDefinition = "UUID")
    UUID orderItemId;

    @Column(name = "product_name", length = 100, nullable = false)
    String productName;

    @Column(name = "product_price", nullable = false)
    Integer productPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_item_type", nullable = false)
    ProductTypeEnum productType;

    @Column(name = "product_amount", nullable = false)
    Integer productAmount;

    public void assignPayment(PaymentEntity payment) {
        this.payment = payment;
        this.payment.paymentId = payment.paymentId;
    }

}

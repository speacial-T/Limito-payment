package com.limito.payment.domain.model;

import java.util.UUID;

import com.limito.payment.domain.enums.PaymentStatusEnum;
import com.limito.payment.domain.enums.ProductTypeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_payment_items")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class PaymentItemEntity {
	// public class PaymentItemEntity extends BaseEntity {

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

	@Column(name = "refund_price")
	Integer refundPrice;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	PaymentStatusEnum status;

	public void assignPayment(PaymentEntity payment) {
		this.payment = payment;
		this.payment.paymentId = payment.paymentId;
	}

	public void updateStatus(PaymentStatusEnum status) {
		this.status = status;
	}

	public void refund(Integer refundAmount) {
		this.refundPrice = refundAmount;
		this.status = PaymentStatusEnum.REFUND;
	}

	public void canceled(Integer refundAmount) {
		this.refundPrice = refundAmount;
		this.status = PaymentStatusEnum.CANCELED;
	}
}

package com.limito.payment.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.limito.payment.domain.enums.PaymentStatusEnum;
import com.limito.payment.domain.enums.RefundStatusEnum;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "p_payment_logs")
public class PaymentLogEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "payment_log_id")
	UUID paymentLogId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id", nullable = false)
	PaymentEntity payment;

	@Column(name = "order_id", nullable = false, columnDefinition = "UUID")
	UUID orderId;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_status")
	PaymentStatusEnum paymentStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "refund_status")
	RefundStatusEnum refundStatus;

	@Column(name = "payment_key", length = 50)
	String paymentKey;

	@Column(name = "pg_response_message", length = 1000)
	String pgResponseMessage;

	@Column(name = "logged_at", nullable = false, updatable = false)
	LocalDateTime loggedAt;

	@PrePersist
	protected void onLog() {
		this.loggedAt = LocalDateTime.now();
	}
}

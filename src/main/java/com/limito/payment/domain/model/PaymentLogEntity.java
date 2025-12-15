package com.limito.payment.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;

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
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_payment_logs")
public class PaymentLogEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "payment_log_id", columnDefinition = "UUID")
	UUID paymentLogId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id", nullable = false)
	PaymentEntity payment;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_status")
	PaymentStatusEnum paymentStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "refund_status")
	RefundStatusEnum refundStatus;

	@Column(name = "payment_key", length = 50)
	String paymentKey;

	// PG 트랜잭션
	@Column(name = "pg_transaction_id", length = 100)
	String pgTransactionId;

	@Column(name = "pg_provider", length = 50)
	String pgProvider;

	// 멱등성 / 재시도
	@Column(name = "idempotency_key", length = 100, unique = true)
	String idempotencyKey;

	@Column(name = "retry_count", nullable = false)
	int retryCount = 0;

	// 실패 정보
	@Column(name = "failure_reason", length = 255)
	String failureReason;

	@Column(name = "pg_error_code", length = 100)
	String pgErrorCode;

	@Column(name = "pg_error_message", length = 500)
	String pgErrorMessage;

	// 통신 정보
	@Column(name = "http_status")
	Integer httpStatus;

	@Column(name = "api_endpoint", length = 200)
	String apiEndpoint;

	// 요청/응답 스냅샷
	@Lob
	@Column(name = "request_payload")
	String requestPayload;

	@Lob
	@Column(name = "response_payload")
	String responsePayload;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	LocalDateTime createdAt;

	@PrePersist
	protected void onLog() {
		this.createdAt = LocalDateTime.now();
	}

	public void assignPayment(PaymentEntity payment) {
		this.payment = payment;
		this.payment.paymentId = payment.paymentId;
	}
}

package com.limito.payment.domain.model;

import static com.limito.payment.domain.exception.PaymentErrorCode.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.limito.common.exception.AppException;
import com.limito.payment.domain.dto.PaymentDetailDtoV1;
import com.limito.payment.domain.enums.PaymentMethodEnum;
import com.limito.payment.domain.enums.PaymentStatusEnum;
import com.limito.payment.domain.enums.RefundStatusEnum;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Table(name = "p_payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString
public class PaymentEntity {
	// public class PaymentEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "payment_id", columnDefinition = "UUID")
	UUID paymentId;

	@Column(name = "order_id", nullable = false, columnDefinition = "UUID")
	UUID orderId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	PaymentStatusEnum paymentStatus;

	@Column(name = "payment_key", length = 50)
	String paymentKey;

	@Column(name = "item_summary", nullable = false, length = 50)
	String itemSummary;

	@Column(name = "total_price", nullable = false)
	Integer totalPrice;

	@Column(name = "refund_reason", length = 100)
	String refundReason;

	@Enumerated(EnumType.STRING)
	@Column(name = "cancel_status")
	RefundStatusEnum refundStatus;

	@Column(name = "approved_at")
	LocalDateTime approvedAt;

	@Column(name = "refund_at")
	LocalDateTime refundAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_method")
	PaymentMethodEnum paymentMethod;

	@Column(name = "card_num", length = 50)
	String cardNum;

	@Column(name = "card_name", length = 50)
	String cardName;

	@Column(name = "pg_provider", length = 50)
	String pgProvider;

	@OneToMany(mappedBy = "payment", cascade = CascadeType.ALL)
	@Builder.Default
	List<PaymentItemEntity> items = new ArrayList<>();

	@OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	List<PaymentLogEntity> logs = new ArrayList<>();

	public UUID internalId() {
		return this.paymentId;
	}

	public void addItems(List<PaymentItemEntity> newItems) {
		newItems.forEach(item -> {
			this.items.add(item);
			item.assignPayment(this);
		});
	}

	public void addLogs(PaymentLogEntity newLog) {
		this.logs.add(newLog);
		newLog.assignPayment(this);
	}

	public void handlePgCallback(PaymentDetailDtoV1 extra) {
		if (extra.getPaymentKey() != null) {
			this.paymentKey = extra.getPaymentKey();
		}
		if (extra.getPaymentStatus() != null) {
			this.paymentStatus = extra.getPaymentStatus();
		}
		if (extra.getCardName() != null) {
			this.cardName = extra.getCardName();
		}
		if (extra.getPgProvider() != null) {
			this.pgProvider = extra.getPgProvider();
		}
		if (extra.getCardNum() != null) {
			this.cardNum = extra.getCardNum();
		}
		if (extra.getPaymentMethod() != null) {
			this.paymentMethod = extra.getPaymentMethod();
		}
		if (extra.getApprovedAt() != null) {
			this.approvedAt = extra.getApprovedAt();
		}
		if (extra.getRefundAt() != null) {
			this.refundAt = extra.getRefundAt();
		}
	}

	public void validateCanCreate() {
		if (this.paymentStatus.equals(PaymentStatusEnum.IN_PROGRESS)
			|| this.paymentStatus.equals(PaymentStatusEnum.FAILED)) {
			log.info("결제 완료 상태 아님");
			throw AppException.of(PAYMENT_DUPLICATE_ORDER);
		}

		if (!this.refundStatus.equals(RefundStatusEnum.NOT_REQUESTED)) {
			log.info("{} 상태", refundStatus);
			throw AppException.of(PAYMENT_CAN_NOT_REFUND);
		}
	}

	public void validateCanRefund() {
		if (this.paymentStatus != PaymentStatusEnum.SUCCESS) {
			log.info("결제 완료 상태 아님");
			throw AppException.of(PAYMENT_IS_NOT_SUCCESS);
		}

		if (this.refundStatus == RefundStatusEnum.REFUND) {
			log.info("{} 상태", refundStatus);
			throw AppException.of(PAYMENT_CAN_NOT_REFUND);
		}
	}

	public void refund(String refundReason, LocalDateTime refundAt,
		RefundStatusEnum refundStatus) {
		this.refundReason = refundReason;
		this.refundStatus = refundStatus;
		this.paymentStatus = PaymentStatusEnum.REFUND;
		this.refundAt = refundAt;
	}
}

package com.limito.payment.domain.model;

import static com.limito.payment.domain.exception.PaymentErrorCode.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.limito.common.exception.AppException;
import com.limito.payment.domain.dto.PaymentDetailDtoV1;
import com.limito.payment.domain.enums.CancelAndRefundStatusEnum;
import com.limito.payment.domain.enums.PaymentMethodEnum;
import com.limito.payment.domain.enums.PaymentStatusEnum;

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

@Entity
@Table(name = "p_payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString
public class PaymentEntity {

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
	CancelAndRefundStatusEnum cancelAndRefundStatus;

	@Column(name = "approved_at")
	LocalDateTime approvedAt;

	@Column(name = "refund_at")
	LocalDateTime refundAt;

	@Column(name = "fail_log", length = 100)
	String failLog;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_method")
	PaymentMethodEnum paymentMethod;

	@Column(name = "card_num", length = 50)
	String cardNum;

	@Column(name = "card_name", length = 50)
	String cardName;

	@Column(name = "pg_provider", length = 50)
	String pgProvider;

	@OneToMany(mappedBy = "payment")
	@Builder.Default
	List<PaymentItemEntity> items = new ArrayList<>();

	public UUID internalId() {
		return this.paymentId;
	}

	public void addItems(List<PaymentItemEntity> newItems) {
		newItems.forEach(item -> {
			this.items.add(item);
			item.assignPayment(this);
		});
	}

	public void handlePgCallback(PaymentDetailDtoV1 extra) {
		if (extra.getPaymentKey() != null)
			this.paymentKey = extra.getPaymentKey();
		if (extra.getPaymentStatus() != null)
			this.paymentStatus = extra.getPaymentStatus();
		if (extra.getCardName() != null)
			this.cardName = extra.getCardName();
		if (extra.getPgProvider() != null)
			this.pgProvider = extra.getPgProvider();
		if (extra.getFailLog() != null)
			this.failLog = extra.getFailLog();
		if (extra.getApprovedAt() != null)
			this.approvedAt = extra.getApprovedAt();
		if (extra.getRefundAt() != null)
			this.refundAt = extra.getRefundAt();
	}

	private void validateCanApprove() {
		if (this.paymentStatus != PaymentStatusEnum.IN_PROGRESS) {
			throw new AppException(PAYMENT_CAN_NOT_CONFIRM);
		}
	}

	private void validateCanCancelOrRefund() {
		if (this.paymentStatus != PaymentStatusEnum.SUCCESS) {
			throw new AppException(PAYMENT_CAN_NOT_CANCEL_OR_REFUND);
		}
	}

	public void markAsFailed(String failLog) {
		this.paymentStatus = PaymentStatusEnum.FAILED;
		this.failLog = failLog;
	}

	public void markAsCancelFailed(String failLog) {
		this.cancelAndRefundStatus = CancelAndRefundStatusEnum.FAILED;
		this.failLog = failLog;
	}

	public void refund(String refundReason) {
		this.refundReason = refundReason;
		this.cancelAndRefundStatus = CancelAndRefundStatusEnum.REFUND;
		this.refundAt = LocalDateTime.now();
	}

	public void cancel(String refundReason) {
		this.refundReason = refundReason;
		this.cancelAndRefundStatus = CancelAndRefundStatusEnum.CANCEL;
		this.refundAt = LocalDateTime.now();
	}

	public void updatePaymentStatus(PaymentStatusEnum paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

}

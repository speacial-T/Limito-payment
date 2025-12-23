package com.limito.payment.domain.dto;

import com.limito.payment.domain.enums.ProductTypeEnum;
import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class PaymentItemDetailDtoV1 {
    private UUID paymentItemId;
    private Long sellerId;
    private UUID orderItemId;
    private ProductTypeEnum productType;
    private String productName;
    private int productPrice;
    private int productAmount;
}

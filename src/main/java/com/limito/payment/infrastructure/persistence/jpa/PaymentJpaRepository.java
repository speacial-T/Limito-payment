package com.limito.payment.infrastructure.persistence.jpa;

import com.limito.payment.domain.model.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, UUID> {

    @Query("""
                select
                    case when count(p) > 0 then true else false end
                from PaymentEntity p
                where p.orderId = :orderId
                  and p.paymentStatus <> com.limito.payment.domain.enums.PaymentStatusEnum.FAILED
            """)
    boolean existsByOrderId(UUID orderId);

    @Query("SELECT p FROM PaymentEntity p JOIN FETCH p.items WHERE p.orderId = :orderId")
    Optional<PaymentEntity> findByOrderIdWithItems(@Param("orderId") UUID orderId);

}

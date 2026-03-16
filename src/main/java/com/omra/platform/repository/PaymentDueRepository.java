package com.omra.platform.repository;

import com.omra.platform.entity.PaymentDue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentDueRepository extends JpaRepository<PaymentDue, Long> {

    List<PaymentDue> findByPaymentIdOrderBySequenceOrderAsc(Long paymentId);
}

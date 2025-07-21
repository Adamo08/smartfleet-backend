package com.adamo.vrspfab.repositories;

import com.adamo.vrspfab.entities.PaymentInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PaymentInfoRepository extends JpaRepository<PaymentInfo, Long> {
}

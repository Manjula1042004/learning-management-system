package com.lms.repository;

import com.lms.entity.Payment;
import com.lms.entity.PaymentStatus;
import com.lms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentId(String paymentId);

    List<Payment> findByStatus(PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId")
    List<Payment> findByUserId(@Param("userId") Long userId);

    @Query("SELECT p FROM Payment p WHERE p.user = :user")
    List<Payment> findByUser(@Param("user") User user);

    boolean existsByPaymentId(String paymentId);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'COMPLETED'")
    Long countCompletedPayments();

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED'")
    Double getTotalRevenue();

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND p.course.instructor = :instructor")
    Double getRevenueByInstructor(@Param("instructor") User instructor);
}
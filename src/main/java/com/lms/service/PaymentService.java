package com.lms.service;

import com.lms.entity.Payment;
import com.lms.entity.PaymentStatus;
import com.lms.entity.User;
import com.lms.entity.Course;
import com.lms.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment createPayment(String paymentId, Double amount, String currency,
                                 String description, User user, Course course) {
        Payment payment = new Payment(paymentId, PaymentStatus.PENDING, amount, currency, description, user, course);
        return paymentRepository.save(payment);
    }

    public Payment updatePaymentStatus(String paymentId, PaymentStatus status) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        payment.setStatus(status);
        return paymentRepository.save(payment);
    }

    public Optional<Payment> getPaymentByPaymentId(String paymentId) {
        return paymentRepository.findByPaymentId(paymentId);
    }

    public List<Payment> getPaymentsByUser(User user) {
        return paymentRepository.findByUserId(user.getId());
    }

    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    public boolean paymentExists(String paymentId) {
        return paymentRepository.existsByPaymentId(paymentId);
    }

    public Double getTotalRevenue() {
        List<Payment> completedPayments = paymentRepository.findByStatus(PaymentStatus.COMPLETED);
        return completedPayments.stream()
                .mapToDouble(Payment::getAmount)
                .sum();
    }

    public Double getRevenueByInstructor(User instructor) {
        List<Payment> completedPayments = paymentRepository.findByStatus(PaymentStatus.COMPLETED);
        return completedPayments.stream()
                .filter(payment -> payment.getCourse().getInstructor().equals(instructor))
                .mapToDouble(Payment::getAmount)
                .sum();
    }
}
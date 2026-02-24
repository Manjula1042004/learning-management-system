package com.lms.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentStatusTest {

    @Test
    void enum_ShouldHaveCorrectValues() {
        PaymentStatus[] values = PaymentStatus.values();

        assertThat(values).hasSize(5);
        assertThat(values).containsExactly(
                PaymentStatus.PENDING,
                PaymentStatus.COMPLETED,
                PaymentStatus.FAILED,
                PaymentStatus.REFUNDED,
                PaymentStatus.CANCELLED
        );
    }

    @Test
    void valueOf_ShouldReturnCorrectEnum() {
        assertThat(PaymentStatus.valueOf("PENDING")).isEqualTo(PaymentStatus.PENDING);
        assertThat(PaymentStatus.valueOf("COMPLETED")).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(PaymentStatus.valueOf("FAILED")).isEqualTo(PaymentStatus.FAILED);
        assertThat(PaymentStatus.valueOf("REFUNDED")).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(PaymentStatus.valueOf("CANCELLED")).isEqualTo(PaymentStatus.CANCELLED);
    }

    @Test
    void getDisplayName_ShouldReturnCorrectString() {
        assertThat(PaymentStatus.PENDING.getDisplayName()).isEqualTo("Pending");
        assertThat(PaymentStatus.COMPLETED.getDisplayName()).isEqualTo("Completed");
        assertThat(PaymentStatus.FAILED.getDisplayName()).isEqualTo("Failed");
        assertThat(PaymentStatus.REFUNDED.getDisplayName()).isEqualTo("Refunded");
        assertThat(PaymentStatus.CANCELLED.getDisplayName()).isEqualTo("Cancelled");
    }

    @Test
    void ordinal_ShouldBeInCorrectOrder() {
        assertThat(PaymentStatus.PENDING.ordinal()).isEqualTo(0);
        assertThat(PaymentStatus.COMPLETED.ordinal()).isEqualTo(1);
        assertThat(PaymentStatus.FAILED.ordinal()).isEqualTo(2);
        assertThat(PaymentStatus.REFUNDED.ordinal()).isEqualTo(3);
        assertThat(PaymentStatus.CANCELLED.ordinal()).isEqualTo(4);
    }
}
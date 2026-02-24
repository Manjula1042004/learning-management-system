package com.lms.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnrollmentStatusTest {

    @Test
    void enum_ShouldHaveCorrectValues() {
        EnrollmentStatus[] values = EnrollmentStatus.values();

        assertThat(values).hasSize(4);
        assertThat(values).containsExactly(
                EnrollmentStatus.ACTIVE,
                EnrollmentStatus.COMPLETED,
                EnrollmentStatus.CANCELLED,
                EnrollmentStatus.EXPIRED
        );
    }

    @Test
    void valueOf_ShouldReturnCorrectEnum() {
        assertThat(EnrollmentStatus.valueOf("ACTIVE")).isEqualTo(EnrollmentStatus.ACTIVE);
        assertThat(EnrollmentStatus.valueOf("COMPLETED")).isEqualTo(EnrollmentStatus.COMPLETED);
        assertThat(EnrollmentStatus.valueOf("CANCELLED")).isEqualTo(EnrollmentStatus.CANCELLED);
        assertThat(EnrollmentStatus.valueOf("EXPIRED")).isEqualTo(EnrollmentStatus.EXPIRED);
    }

    @Test
    void getDisplayName_ShouldReturnCorrectString() {
        assertThat(EnrollmentStatus.ACTIVE.getDisplayName()).isEqualTo("Active");
        assertThat(EnrollmentStatus.COMPLETED.getDisplayName()).isEqualTo("Completed");
        assertThat(EnrollmentStatus.CANCELLED.getDisplayName()).isEqualTo("Cancelled");
        assertThat(EnrollmentStatus.EXPIRED.getDisplayName()).isEqualTo("Expired");
    }

    @Test
    void toString_ShouldReturnEnumName() {
        assertThat(EnrollmentStatus.ACTIVE.toString()).isEqualTo("ACTIVE");
        assertThat(EnrollmentStatus.COMPLETED.toString()).isEqualTo("COMPLETED");
        assertThat(EnrollmentStatus.CANCELLED.toString()).isEqualTo("CANCELLED");
        assertThat(EnrollmentStatus.EXPIRED.toString()).isEqualTo("EXPIRED");
    }

    @Test
    void ordinal_ShouldBeInCorrectOrder() {
        assertThat(EnrollmentStatus.ACTIVE.ordinal()).isEqualTo(0);
        assertThat(EnrollmentStatus.COMPLETED.ordinal()).isEqualTo(1);
        assertThat(EnrollmentStatus.CANCELLED.ordinal()).isEqualTo(2);
        assertThat(EnrollmentStatus.EXPIRED.ordinal()).isEqualTo(3);
    }
}
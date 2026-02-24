package com.lms.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CourseStatusTest {

    @Test
    void enum_ShouldHaveCorrectValues() {
        CourseStatus[] values = CourseStatus.values();

        assertThat(values).hasSize(3);
        assertThat(values).containsExactly(
                CourseStatus.PENDING,
                CourseStatus.APPROVED,
                CourseStatus.REJECTED
        );
    }

    @Test
    void valueOf_ShouldReturnCorrectEnum() {
        assertThat(CourseStatus.valueOf("PENDING")).isEqualTo(CourseStatus.PENDING);
        assertThat(CourseStatus.valueOf("APPROVED")).isEqualTo(CourseStatus.APPROVED);
        assertThat(CourseStatus.valueOf("REJECTED")).isEqualTo(CourseStatus.REJECTED);
    }

    @Test
    void name_ShouldReturnCorrectString() {
        assertThat(CourseStatus.PENDING.name()).isEqualTo("PENDING");
        assertThat(CourseStatus.APPROVED.name()).isEqualTo("APPROVED");
        assertThat(CourseStatus.REJECTED.name()).isEqualTo("REJECTED");
    }

    @Test
    void ordinal_ShouldBeInCorrectOrder() {
        assertThat(CourseStatus.PENDING.ordinal()).isEqualTo(0);
        assertThat(CourseStatus.APPROVED.ordinal()).isEqualTo(1);
        assertThat(CourseStatus.REJECTED.ordinal()).isEqualTo(2);
    }
}
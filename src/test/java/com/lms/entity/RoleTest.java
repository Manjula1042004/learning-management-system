package com.lms.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoleTest {

    @Test
    void enum_ShouldHaveCorrectValues() {
        Role[] values = Role.values();

        assertThat(values).hasSize(3);
        assertThat(values).containsExactly(
                Role.ADMIN,
                Role.INSTRUCTOR,
                Role.STUDENT
        );
    }

    @Test
    void valueOf_ShouldReturnCorrectEnum() {
        assertThat(Role.valueOf("ADMIN")).isEqualTo(Role.ADMIN);
        assertThat(Role.valueOf("INSTRUCTOR")).isEqualTo(Role.INSTRUCTOR);
        assertThat(Role.valueOf("STUDENT")).isEqualTo(Role.STUDENT);
    }

    @Test
    void name_ShouldReturnCorrectString() {
        assertThat(Role.ADMIN.name()).isEqualTo("ADMIN");
        assertThat(Role.INSTRUCTOR.name()).isEqualTo("INSTRUCTOR");
        assertThat(Role.STUDENT.name()).isEqualTo("STUDENT");
    }

    @Test
    void ordinal_ShouldBeInCorrectOrder() {
        assertThat(Role.ADMIN.ordinal()).isEqualTo(0);
        assertThat(Role.INSTRUCTOR.ordinal()).isEqualTo(1);
        assertThat(Role.STUDENT.ordinal()).isEqualTo(2);
    }
}
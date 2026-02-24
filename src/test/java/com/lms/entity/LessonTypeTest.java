package com.lms.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LessonTypeTest {

    @Test
    void enum_ShouldHaveCorrectValues() {
        LessonType[] values = LessonType.values();

        assertThat(values).hasSize(5);
        assertThat(values).containsExactly(
                LessonType.VIDEO,
                LessonType.PDF,
                LessonType.IMAGE,
                LessonType.TEXT,
                LessonType.QUIZ
        );
    }

    @Test
    void valueOf_ShouldReturnCorrectEnum() {
        assertThat(LessonType.valueOf("VIDEO")).isEqualTo(LessonType.VIDEO);
        assertThat(LessonType.valueOf("PDF")).isEqualTo(LessonType.PDF);
        assertThat(LessonType.valueOf("IMAGE")).isEqualTo(LessonType.IMAGE);
        assertThat(LessonType.valueOf("TEXT")).isEqualTo(LessonType.TEXT);
        assertThat(LessonType.valueOf("QUIZ")).isEqualTo(LessonType.QUIZ);
    }

    @Test
    void name_ShouldReturnCorrectString() {
        assertThat(LessonType.VIDEO.name()).isEqualTo("VIDEO");
        assertThat(LessonType.PDF.name()).isEqualTo("PDF");
        assertThat(LessonType.IMAGE.name()).isEqualTo("IMAGE");
        assertThat(LessonType.TEXT.name()).isEqualTo("TEXT");
        assertThat(LessonType.QUIZ.name()).isEqualTo("QUIZ");
    }

    @Test
    void ordinal_ShouldBeInCorrectOrder() {
        assertThat(LessonType.VIDEO.ordinal()).isEqualTo(0);
        assertThat(LessonType.PDF.ordinal()).isEqualTo(1);
        assertThat(LessonType.IMAGE.ordinal()).isEqualTo(2);
        assertThat(LessonType.TEXT.ordinal()).isEqualTo(3);
        assertThat(LessonType.QUIZ.ordinal()).isEqualTo(4);
    }
}
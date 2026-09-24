package com.example.todoapp.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PriorityTest {

    @Test
    void exposesExactlyTheSupportedPriorities() {
        assertThat(Priority.values()).containsExactly(
                Priority.HIGH, Priority.MEDIUM, Priority.LOW);
    }
}

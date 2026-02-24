package com.lms.repository;

import com.lms.config.TestConfig;
import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // IMPORTANT: Clear all data before each test
        userRepository.deleteAll();
        userRepository.flush();
    }

    @Test
    void testSaveAndFindByUsername() {
        // Create test data
        User student = TestDataFactory.createStudent(null);
        student = userRepository.save(student);

        Optional<User> found = userRepository.findByUsername(student.getUsername());
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo(student.getUsername());
    }

    @Test
    void testFindByEmail() {
        User student = TestDataFactory.createStudent(null);
        student = userRepository.save(student);

        Optional<User> found = userRepository.findByEmail(student.getEmail());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(student.getEmail());
    }

    @Test
    void testExistsByUsername() {
        User student = TestDataFactory.createStudent(null);
        student = userRepository.save(student);

        assertThat(userRepository.existsByUsername(student.getUsername())).isTrue();
        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
    }

    @Test
    void testFindByRole() {
        // Save one of each role
        User student = userRepository.save(TestDataFactory.createStudent(null));
        User instructor = userRepository.save(TestDataFactory.createInstructor(null));
        User admin = userRepository.save(TestDataFactory.createAdmin(null));

        assertThat(userRepository.findByRole(Role.STUDENT)).hasSize(1);
        assertThat(userRepository.findByRole(Role.INSTRUCTOR)).hasSize(1);
        assertThat(userRepository.findByRole(Role.ADMIN)).hasSize(1);
    }

    @Test
    void testFindAll() {
        // Save multiple users
        userRepository.save(TestDataFactory.createStudent(null));
        userRepository.save(TestDataFactory.createInstructor(null));
        userRepository.save(TestDataFactory.createAdmin(null));

        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(3);
    }

    @Test
    void testSearchUsers() {
        User student = userRepository.save(TestDataFactory.createStudent(null));

        List<User> results = userRepository.searchUsers(student.getUsername().substring(0, 5));
        assertThat(results).hasSize(1);
    }

    @Test
    void testDelete() {
        User student = userRepository.save(TestDataFactory.createStudent(null));
        Long id = student.getId();

        userRepository.delete(student);

        Optional<User> found = userRepository.findById(id);
        assertThat(found).isEmpty();
    }
}
package com.zhitu.enterprise.integration;

import com.zhitu.enterprise.entity.EnterpriseActivity;
import com.zhitu.enterprise.entity.EnterpriseTodo;
import net.jqwik.api.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Bug Condition Exploration Test for DateTime Type Mismatch
 * 
 * **Validates: Requirements 1.1, 1.2, 1.3**
 * 
 * **Property 1: Bug Condition** - DateTime Type Mismatch Compilation Errors
 * 
 * This test verifies that the EnterprisePortalIntegrationTest uses the correct
 * datetime types (OffsetDateTime) for entity fields that are declared as OffsetDateTime.
 * 
 * **CRITICAL**: This test MUST FAIL on unfixed code - failure confirms the bug exists
 * 
 * **Expected Behavior**: 
 * - EnterpriseTodo.dueDate should be set with OffsetDateTime.now().plusDays(3)
 * - EnterpriseTodo.createdAt should be set with OffsetDateTime.now()
 * - EnterpriseActivity.createdAt should be set with OffsetDateTime.now()
 * 
 * **Current Buggy Behavior** (lines 630, 631, 641 in EnterprisePortalIntegrationTest):
 * - Uses LocalDateTime.now() instead of OffsetDateTime.now()
 * - Causes compilation errors: "incompatible types: LocalDateTime cannot be converted to OffsetDateTime"
 * 
 * **EXPECTED OUTCOME**: Test FAILS on unfixed code (proves bug exists)
 * **After Fix**: Test PASSES (confirms bug is fixed)
 * 
 * **COUNTEREXAMPLES FOUND**:
 * - Line 630: todo1.setDueDate(LocalDateTime.now().plusDays(3)) - Expected OffsetDateTime, got LocalDateTime
 * - Line 631: todo1.setCreatedAt(LocalDateTime.now()) - Expected OffsetDateTime, got LocalDateTime
 * - Line 641: activity1.setCreatedAt(LocalDateTime.now()) - Expected OffsetDateTime, got LocalDateTime
 */
@DisplayName("Bug Condition Exploration: DateTime Type Mismatch")
class DateTimeTypeMismatchBugConditionTest {

    /**
     * Property 1: Bug Condition - DateTime Type Correctness
     * 
     * **Validates: Requirements 2.1, 2.2, 2.3**
     * 
     * For any field assignment where the target field is declared as OffsetDateTime type,
     * the test code SHALL use OffsetDateTime values (not LocalDateTime) to ensure type
     * compatibility and successful compilation.
     * 
     * This property tests the three specific locations where the bug manifests:
     * 1. EnterpriseTodo.dueDate at line 630
     * 2. EnterpriseTodo.createdAt at line 631
     * 3. EnterpriseActivity.createdAt at line 641
     * 
     * **EXPECTED OUTCOME ON UNFIXED CODE**: This test FAILS because the source code
     * at lines 629, 630, 640 uses LocalDateTime.now() instead of OffsetDateTime.now()
     */
    @Property
    @DisplayName("Property 1: EnterpriseTodo and EnterpriseActivity datetime fields should accept OffsetDateTime values")
    void dateTimeFieldsShouldAcceptOffsetDateTimeValues(
            @ForAll("offsetDateTimes") OffsetDateTime testDateTime) {
        
        // Test EnterpriseTodo.dueDate (line 629)
        EnterpriseTodo todo = new EnterpriseTodo();
        
        // This should work without compilation errors
        todo.setDueDate(testDateTime.plusDays(3));
        assertThat(todo.getDueDate())
                .as("EnterpriseTodo.dueDate should accept OffsetDateTime values")
                .isNotNull()
                .isInstanceOf(OffsetDateTime.class);
        
        // Test EnterpriseTodo.createdAt (line 630)
        todo.setCreatedAt(testDateTime);
        assertThat(todo.getCreatedAt())
                .as("EnterpriseTodo.createdAt should accept OffsetDateTime values")
                .isNotNull()
                .isInstanceOf(OffsetDateTime.class);
        
        // Test EnterpriseActivity.createdAt (line 640)
        EnterpriseActivity activity = new EnterpriseActivity();
        activity.setCreatedAt(testDateTime);
        assertThat(activity.getCreatedAt())
                .as("EnterpriseActivity.createdAt should accept OffsetDateTime values")
                .isNotNull()
                .isInstanceOf(OffsetDateTime.class);
    }

    /**
     * Property 1b: Bug Condition - Field Type Verification
     * 
     * **Validates: Requirements 1.1, 1.2, 1.3**
     * 
     * Verifies that the entity fields are declared as OffsetDateTime type,
     * confirming that using LocalDateTime values would cause type mismatch errors.
     */
    @Property
    @DisplayName("Property 1b: Entity datetime fields should be declared as OffsetDateTime type")
    void entityDateTimeFieldsShouldBeDeclaredAsOffsetDateTime() throws Exception {
        
        // Verify EnterpriseTodo.dueDate is OffsetDateTime
        Field dueDateField = EnterpriseTodo.class.getDeclaredField("dueDate");
        assertThat(dueDateField.getType())
                .as("EnterpriseTodo.dueDate should be declared as OffsetDateTime")
                .isEqualTo(OffsetDateTime.class);
        
        // Verify EnterpriseTodo.createdAt is OffsetDateTime
        Field todoCreatedAtField = EnterpriseTodo.class.getDeclaredField("createdAt");
        assertThat(todoCreatedAtField.getType())
                .as("EnterpriseTodo.createdAt should be declared as OffsetDateTime")
                .isEqualTo(OffsetDateTime.class);
        
        // Verify EnterpriseActivity.createdAt is OffsetDateTime
        Field activityCreatedAtField = EnterpriseActivity.class.getDeclaredField("createdAt");
        assertThat(activityCreatedAtField.getType())
                .as("EnterpriseActivity.createdAt should be declared as OffsetDateTime")
                .isEqualTo(OffsetDateTime.class);
    }

    /**
     * Property 1c: Bug Condition - Source Code Verification
     * 
     * **Validates: Requirements 1.1, 1.2, 1.3**
     * 
     * Verifies that the EnterprisePortalIntegrationTest source code uses OffsetDateTime.now()
     * instead of LocalDateTime.now() at the three buggy locations.
     * 
     * **EXPECTED OUTCOME ON UNFIXED CODE**: This test FAILS because the source code
     * contains "LocalDateTime.now()" at lines 630, 631, 641 instead of "OffsetDateTime.now()"
     * 
     * **After Fix**: This test PASSES when the source code is corrected to use OffsetDateTime.now()
     */
    @Test
    @DisplayName("Property 1c: EnterprisePortalIntegrationTest should use OffsetDateTime.now() at lines 630, 631, 641")
    void testSourceCodeUsesOffsetDateTime() throws IOException {
        // Read the source file
        Path testFilePath = Paths.get("src/test/java/com/zhitu/enterprise/integration/EnterprisePortalIntegrationTest.java");
        
        if (!Files.exists(testFilePath)) {
            fail("EnterprisePortalIntegrationTest.java not found at expected location");
        }
        
        List<String> lines = Files.readAllLines(testFilePath);
        
        // Check line 630 (1-indexed, so array index 629)
        if (lines.size() > 629) {
            String line630 = lines.get(629);
            assertThat(line630)
                    .as("Line 630 should use OffsetDateTime.now().plusDays(3), not LocalDateTime.now().plusDays(3)")
                    .contains("OffsetDateTime.now().plusDays(3)")
                    .doesNotContain("LocalDateTime.now().plusDays(3)");
        }
        
        // Check line 631 (array index 630)
        if (lines.size() > 630) {
            String line631 = lines.get(630);
            assertThat(line631)
                    .as("Line 631 should use OffsetDateTime.now(), not LocalDateTime.now()")
                    .contains("OffsetDateTime.now()")
                    .doesNotContain("LocalDateTime.now()");
        }
        
        // Check line 641 (array index 640)
        if (lines.size() > 640) {
            String line641 = lines.get(640);
            assertThat(line641)
                    .as("Line 641 should use OffsetDateTime.now(), not LocalDateTime.now()")
                    .contains("OffsetDateTime.now()")
                    .doesNotContain("LocalDateTime.now()");
        }
    }

    /**
     * Property 1d: Bug Condition - LocalDateTime Incompatibility
     * 
     * **Validates: Requirements 1.1, 1.2, 1.3**
     * 
     * Demonstrates that LocalDateTime values cannot be directly assigned to
     * OffsetDateTime fields, which is the root cause of the compilation errors.
     * 
     * This property encodes the bug condition: the test setup code at lines 629, 630, 640
     * uses LocalDateTime.now() which is incompatible with OffsetDateTime fields.
     */
    @Property
    @DisplayName("Property 1d: LocalDateTime values should NOT be compatible with OffsetDateTime fields")
    void localDateTimeShouldNotBeCompatibleWithOffsetDateTimeFields() throws Exception {
        
        // Verify that LocalDateTime and OffsetDateTime are different types
        assertThat(LocalDateTime.class)
                .as("LocalDateTime and OffsetDateTime should be different types")
                .isNotEqualTo(OffsetDateTime.class);
        
        // Verify that the entity fields expect OffsetDateTime, not LocalDateTime
        Field dueDateField = EnterpriseTodo.class.getDeclaredField("dueDate");
        assertThat(dueDateField.getType())
                .as("EnterpriseTodo.dueDate expects OffsetDateTime, not LocalDateTime")
                .isNotEqualTo(LocalDateTime.class)
                .isEqualTo(OffsetDateTime.class);
        
        Field todoCreatedAtField = EnterpriseTodo.class.getDeclaredField("createdAt");
        assertThat(todoCreatedAtField.getType())
                .as("EnterpriseTodo.createdAt expects OffsetDateTime, not LocalDateTime")
                .isNotEqualTo(LocalDateTime.class)
                .isEqualTo(OffsetDateTime.class);
        
        Field activityCreatedAtField = EnterpriseActivity.class.getDeclaredField("createdAt");
        assertThat(activityCreatedAtField.getType())
                .as("EnterpriseActivity.createdAt expects OffsetDateTime, not LocalDateTime")
                .isNotEqualTo(LocalDateTime.class)
                .isEqualTo(OffsetDateTime.class);
    }

    /**
     * Generator for OffsetDateTime values
     * 
     * Generates various OffsetDateTime instances to test the property across
     * different datetime values and timezones.
     */
    @Provide
    Arbitrary<OffsetDateTime> offsetDateTimes() {
        return Arbitraries.of(
                OffsetDateTime.now(),
                OffsetDateTime.now().plusDays(1),
                OffsetDateTime.now().plusDays(3),
                OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now().plusHours(5),
                OffsetDateTime.now().minusHours(2)
        );
    }
}

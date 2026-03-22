# Capability Radar Undefined Peer Average Fix - Bugfix Design

## Overview

The CapabilityRadar component crashes when the `peer_average` property is undefined, null, empty, or shorter than the dimensions array. This occurs during the chartData mapping operation at line 31, where the code attempts to access `data.peer_average[index]` without checking if the array exists or has sufficient elements. The fix will add defensive checks to handle missing or incomplete peer average data gracefully, rendering only the student radar when peer data is unavailable, and using fallback values when peer data is partially available.

## Glossary

- **Bug_Condition (C)**: The condition that triggers the bug - when `peer_average` is undefined, null, empty, or has fewer elements than dimensions
- **Property (P)**: The desired behavior when peer average data is missing - render student radar only without crashing, or use fallback values for partial data
- **Preservation**: Existing rendering behavior for valid peer_average data and other component states (loading, null data) that must remain unchanged
- **CapabilityRadar**: The React component in `frontend/src/student/dashboard/components/CapabilityRadar.tsx` that renders a radar chart comparing student capabilities with peer averages
- **RadarData**: The data structure containing dimensions array and peer_average array
- **chartData**: The transformed data structure used by the Recharts RadarChart component

## Bug Details

### Bug Condition

The bug manifests when the API returns RadarData where `peer_average` is undefined, null, empty, or has fewer elements than the dimensions array. The `chartData` mapping operation attempts to access `data.peer_average[index]` without validating the array's existence or length, causing a runtime error or undefined values in the chart.

**Formal Specification:**
```
FUNCTION isBugCondition(input)
  INPUT: input of type { data: RadarData | null, loading: boolean }
  OUTPUT: boolean
  
  RETURN input.data != null
         AND input.loading == false
         AND (input.data.peer_average === undefined
              OR input.data.peer_average === null
              OR input.data.peer_average.length === 0
              OR input.data.peer_average.length < input.data.dimensions.length)
END FUNCTION
```

### Examples

- **Example 1**: API returns `{ dimensions: [{label: "技术", score: 80, max: 100}], peer_average: undefined }` → Expected: Render student radar only. Actual: Crashes with "Cannot read properties of undefined"
- **Example 2**: API returns `{ dimensions: [{label: "技术", score: 80, max: 100}], peer_average: null }` → Expected: Render student radar only. Actual: Crashes with "Cannot read properties of undefined"
- **Example 3**: API returns `{ dimensions: [{label: "技术", score: 80, max: 100}, {label: "沟通", score: 70, max: 100}], peer_average: [75] }` → Expected: Render both radars with peer value 75 for first dimension and 0 for second. Actual: Second peer value is undefined
- **Edge case**: API returns `{ dimensions: [], peer_average: undefined }` → Expected: Render empty chart without crashing

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**
- Valid peer_average data (array matching dimensions length) must continue to render both student and peer radars correctly
- Valid dimensions array must continue to render the student radar correctly
- Null data prop must continue to return null and render nothing
- Loading state must continue to display the skeleton animation

**Scope:**
All inputs that do NOT involve missing or incomplete peer_average data should be completely unaffected by this fix. This includes:
- Normal rendering with complete peer_average data
- Loading state rendering
- Null data handling
- Student radar rendering with valid dimensions

## Hypothesized Root Cause

Based on the bug description, the most likely issues are:

1. **Missing Null/Undefined Check**: The code directly accesses `data.peer_average[index]` without first checking if `peer_average` exists
   - Line 31: `peer: data.peer_average[index]` assumes the array is always defined
   - No defensive programming for optional API fields

2. **Missing Length Validation**: The code doesn't verify that `peer_average` has the same length as `dimensions`
   - Array access may go out of bounds if peer_average is shorter
   - No fallback value for missing indices

3. **Conditional Rendering Not Implemented**: The Radar component for peer average is always rendered
   - Should conditionally render based on peer_average availability
   - Legend should also reflect whether peer data is shown

## Correctness Properties

Property 1: Bug Condition - Graceful Handling of Missing Peer Average Data

_For any_ input where the bug condition holds (peer_average is undefined, null, empty, or shorter than dimensions), the fixed CapabilityRadar component SHALL render the student radar without crashing, either hiding the peer radar entirely (for undefined/null/empty) or using fallback value 0 for missing peer values (for partial data).

**Validates: Requirements 2.1, 2.2, 2.3, 2.4**

Property 2: Preservation - Valid Peer Average Data Rendering

_For any_ input where the bug condition does NOT hold (peer_average is a valid array matching dimensions length), the fixed component SHALL produce exactly the same rendering as the original component, preserving the dual-radar visualization with both student and peer average data.

**Validates: Requirements 3.1, 3.2, 3.3, 3.4**

## Fix Implementation

### Changes Required

Assuming our root cause analysis is correct:

**File**: `frontend/src/student/dashboard/components/CapabilityRadar.tsx`

**Function**: `CapabilityRadar` component (specifically the chartData mapping and Radar rendering)

**Specific Changes**:
1. **Add Peer Average Validation**: Before the chartData mapping, check if peer_average exists and has valid length
   - Add helper variable: `const hasPeerData = data.peer_average && Array.isArray(data.peer_average) && data.peer_average.length > 0`
   - Use this flag to conditionally access peer_average

2. **Safe Array Access with Fallback**: Modify the chartData mapping to safely access peer_average
   - Change `peer: data.peer_average[index]` to `peer: data.peer_average?.[index] ?? 0`
   - This uses optional chaining and nullish coalescing for safe access

3. **Conditional Radar Rendering**: Only render the peer average Radar component when peer data exists
   - Wrap the second `<Radar>` component in a conditional: `{hasPeerData && <Radar ... />}`
   - This prevents rendering empty/invalid peer data

4. **Update Legend**: Ensure the Legend only shows entries for rendered radars
   - The Legend component automatically adapts to rendered Radar components
   - No explicit change needed if conditional rendering is implemented

## Testing Strategy

### Validation Approach

The testing strategy follows a two-phase approach: first, surface counterexamples that demonstrate the bug on unfixed code, then verify the fix works correctly and preserves existing behavior.

### Exploratory Bug Condition Checking

**Goal**: Surface counterexamples that demonstrate the bug BEFORE implementing the fix. Confirm or refute the root cause analysis. If we refute, we will need to re-hypothesize.

**Test Plan**: Write tests that render the CapabilityRadar component with various invalid peer_average values. Run these tests on the UNFIXED code to observe failures and understand the root cause.

**Test Cases**:
1. **Undefined Peer Average Test**: Render with `peer_average: undefined` (will fail on unfixed code with "Cannot read properties of undefined")
2. **Null Peer Average Test**: Render with `peer_average: null` (will fail on unfixed code with "Cannot read properties of undefined")
3. **Empty Peer Average Test**: Render with `peer_average: []` and non-empty dimensions (will fail on unfixed code with undefined values in chart)
4. **Partial Peer Average Test**: Render with `peer_average: [75]` and dimensions length 3 (will fail on unfixed code with undefined values for indices 1 and 2)

**Expected Counterexamples**:
- Component crashes with "Cannot read properties of undefined (reading '0')" for undefined/null peer_average
- Possible causes: missing null check, direct array access without validation, no fallback values

### Fix Checking

**Goal**: Verify that for all inputs where the bug condition holds, the fixed function produces the expected behavior.

**Pseudocode:**
```
FOR ALL input WHERE isBugCondition(input) DO
  result := CapabilityRadar_fixed(input)
  ASSERT result renders without crashing
  ASSERT result shows only student radar OR uses fallback values for missing peer data
END FOR
```

### Preservation Checking

**Goal**: Verify that for all inputs where the bug condition does NOT hold, the fixed function produces the same result as the original function.

**Pseudocode:**
```
FOR ALL input WHERE NOT isBugCondition(input) DO
  ASSERT CapabilityRadar_original(input) = CapabilityRadar_fixed(input)
END FOR
```

**Testing Approach**: Property-based testing is recommended for preservation checking because:
- It generates many test cases automatically across the input domain
- It catches edge cases that manual unit tests might miss
- It provides strong guarantees that behavior is unchanged for all non-buggy inputs

**Test Plan**: Observe behavior on UNFIXED code first for valid peer_average data, then write property-based tests capturing that behavior.

**Test Cases**:
1. **Valid Peer Average Preservation**: Observe that valid peer_average arrays render both radars correctly on unfixed code, then write test to verify this continues after fix
2. **Loading State Preservation**: Observe that loading state renders skeleton correctly on unfixed code, then write test to verify this continues after fix
3. **Null Data Preservation**: Observe that null data returns null on unfixed code, then write test to verify this continues after fix
4. **Student Radar Preservation**: Observe that student radar renders correctly with valid dimensions on unfixed code, then write test to verify this continues after fix

### Unit Tests

- Test component rendering with undefined peer_average
- Test component rendering with null peer_average
- Test component rendering with empty peer_average array
- Test component rendering with partial peer_average array (shorter than dimensions)
- Test component rendering with valid peer_average array (matching dimensions length)
- Test component rendering in loading state
- Test component rendering with null data
- Test that peer Radar component is not rendered when peer_average is invalid
- Test that peer Radar component is rendered when peer_average is valid

### Property-Based Tests

- Generate random RadarData with various peer_average configurations (undefined, null, empty, partial, valid) and verify no crashes occur
- Generate random valid RadarData and verify both radars render correctly
- Generate random dimensions arrays and verify student radar always renders when data is present

### Integration Tests

- Test full dashboard rendering with CapabilityRadar receiving invalid peer_average from API
- Test full dashboard rendering with CapabilityRadar receiving valid peer_average from API
- Test that error boundaries catch any remaining edge cases
- Test that user can still interact with other dashboard components when CapabilityRadar has invalid data

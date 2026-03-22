/**
 * Bug Condition Exploration Test for CapabilityRadar
 * 
 * **Validates: Requirements 1.1, 1.2, 1.3, 1.4, 2.1, 2.2, 2.3, 2.4**
 * 
 * CRITICAL: This test MUST FAIL on unfixed code - failure confirms the bug exists
 * 
 * Property 1: Bug Condition - Graceful Handling of Missing Peer Average Data
 * 
 * For any input where peer_average is undefined, null, empty, or shorter than dimensions,
 * the CapabilityRadar component SHALL render without crashing and show only student data
 * or use fallback values for missing peer data.
 */

import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import * as fc from 'fast-check';
import CapabilityRadar from './CapabilityRadar';
import type { RadarData } from '../../mock/generator';

describe('CapabilityRadar - Bug Condition Exploration', () => {
  describe('Property 1: Graceful Handling of Missing Peer Average Data', () => {
    
    it('should render without crashing when peer_average is undefined', () => {
      // Test case 1: undefined peer_average
      const dataWithUndefinedPeer: RadarData = {
        dimensions: [
          { key: 'tech', label: '技术能力', score: 80, max: 100 },
          { key: 'comm', label: '沟通协作', score: 75, max: 100 },
          { key: 'mgmt', label: '项目管理', score: 70, max: 100 }
        ],
        peer_average: undefined as any, // Bug condition
        history: []
      };

      // This should NOT crash - if it does, the bug exists
      expect(() => {
        render(<CapabilityRadar data={dataWithUndefinedPeer} loading={false} />);
      }).not.toThrow();

      // Verify the component renders
      expect(screen.getByText('能力雷达画像')).toBeInTheDocument();
    });

    it('should render without crashing when peer_average is null', () => {
      // Test case 2: null peer_average
      const dataWithNullPeer: RadarData = {
        dimensions: [
          { key: 'tech', label: '技术能力', score: 80, max: 100 },
          { key: 'comm', label: '沟通协作', score: 75, max: 100 },
          { key: 'mgmt', label: '项目管理', score: 70, max: 100 }
        ],
        peer_average: null as any, // Bug condition
        history: []
      };

      // This should NOT crash - if it does, the bug exists
      expect(() => {
        render(<CapabilityRadar data={dataWithNullPeer} loading={false} />);
      }).not.toThrow();

      // Verify the component renders
      expect(screen.getByText('能力雷达画像')).toBeInTheDocument();
    });

    it('should render without crashing when peer_average is empty array', () => {
      // Test case 3: empty peer_average array
      const dataWithEmptyPeer: RadarData = {
        dimensions: [
          { key: 'tech', label: '技术能力', score: 80, max: 100 },
          { key: 'comm', label: '沟通协作', score: 75, max: 100 },
          { key: 'mgmt', label: '项目管理', score: 70, max: 100 }
        ],
        peer_average: [], // Bug condition
        history: []
      };

      // This should NOT crash - if it does, the bug exists
      expect(() => {
        render(<CapabilityRadar data={dataWithEmptyPeer} loading={false} />);
      }).not.toThrow();

      // Verify the component renders
      expect(screen.getByText('能力雷达画像')).toBeInTheDocument();
    });

    it('should render without crashing when peer_average is shorter than dimensions', () => {
      // Test case 4: partial peer_average array (length 1 with 3 dimensions)
      const dataWithPartialPeer: RadarData = {
        dimensions: [
          { key: 'tech', label: '技术能力', score: 80, max: 100 },
          { key: 'comm', label: '沟通协作', score: 75, max: 100 },
          { key: 'mgmt', label: '项目管理', score: 70, max: 100 }
        ],
        peer_average: [75], // Bug condition: only 1 element for 3 dimensions
        history: []
      };

      // This should NOT crash - if it does, the bug exists
      expect(() => {
        render(<CapabilityRadar data={dataWithPartialPeer} loading={false} />);
      }).not.toThrow();

      // Verify the component renders
      expect(screen.getByText('能力雷达画像')).toBeInTheDocument();
    });

    it('Property-based: should handle various invalid peer_average configurations', () => {
      // Property-based test to explore more edge cases
      fc.assert(
        fc.property(
          // Generate dimensions array (1-6 dimensions)
          fc.array(
            fc.record({
              key: fc.string(),
              label: fc.string(),
              score: fc.integer({ min: 0, max: 100 }),
              max: fc.constant(100)
            }),
            { minLength: 1, maxLength: 6 }
          ),
          // Generate invalid peer_average configurations
          fc.oneof(
            fc.constant(undefined),
            fc.constant(null),
            fc.constant([]),
            fc.array(fc.integer({ min: 0, max: 100 }), { maxLength: 0 }) // empty array
          ),
          (dimensions, peer_average) => {
            const data: RadarData = {
              dimensions,
              peer_average: peer_average as any,
              history: []
            };

            // Should not crash
            expect(() => {
              render(<CapabilityRadar data={data} loading={false} />);
            }).not.toThrow();
          }
        ),
        { numRuns: 20 } // Run 20 test cases
      );
    });

    it('Property-based: should handle peer_average shorter than dimensions', () => {
      // Property-based test for partial peer_average arrays
      fc.assert(
        fc.property(
          // Generate dimensions array (2-6 dimensions)
          fc.array(
            fc.record({
              key: fc.string(),
              label: fc.string(),
              score: fc.integer({ min: 0, max: 100 }),
              max: fc.constant(100)
            }),
            { minLength: 2, maxLength: 6 }
          ),
          (dimensions) => {
            // Generate peer_average shorter than dimensions
            const peerLength = Math.floor(dimensions.length / 2);
            const peer_average = Array.from({ length: peerLength }, () => 
              Math.floor(Math.random() * 100)
            );

            const data: RadarData = {
              dimensions,
              peer_average,
              history: []
            };

            // Should not crash
            expect(() => {
              render(<CapabilityRadar data={data} loading={false} />);
            }).not.toThrow();
          }
        ),
        { numRuns: 20 } // Run 20 test cases
      );
    });
  });
});

/**
 * Preservation Property Tests for CapabilityRadar
 * 
 * **Validates: Requirements 3.1, 3.2, 3.3, 3.4**
 * 
 * Property 2: Preservation - Valid Peer Average Data Rendering
 * 
 * For any input where peer_average is a valid array matching dimensions length,
 * the component SHALL produce exactly the same rendering as before the fix,
 * preserving the dual-radar visualization with both student and peer average data.
 * 
 * IMPORTANT: These tests run on UNFIXED code to observe baseline behavior.
 * They should PASS on unfixed code, confirming what behavior to preserve.
 */

describe('CapabilityRadar - Preservation Property Tests', () => {
  describe('Property 2: Valid Peer Average Data Rendering', () => {
    
    it('should render both student and peer radars with valid peer_average data', () => {
      // Test case: valid peer_average matching dimensions length
      const validData: RadarData = {
        dimensions: [
          { key: 'tech', label: '技术能力', score: 80, max: 100 },
          { key: 'comm', label: '沟通协作', score: 75, max: 100 },
          { key: 'mgmt', label: '项目管理', score: 70, max: 100 }
        ],
        peer_average: [85, 78, 72], // Valid: matches dimensions length
        history: []
      };

      // Should not crash with valid data
      expect(() => {
        render(<CapabilityRadar data={validData} loading={false} />);
      }).not.toThrow();

      // Verify component renders
      expect(screen.getByText('能力雷达画像')).toBeInTheDocument();
      expect(screen.getByText('六维能力评估与同专业对比')).toBeInTheDocument();
    });

    it('should render loading skeleton when loading is true', () => {
      // Test case: loading state
      const { container } = render(<CapabilityRadar data={null} loading={true} />);

      // Verify loading skeleton is displayed
      const loadingCard = container.querySelector('.animate-pulse');
      expect(loadingCard).toBeInTheDocument();

      // Verify no actual content is rendered
      expect(screen.queryByText('能力雷达画像')).not.toBeInTheDocument();
    });

    it('should return null when data is null and not loading', () => {
      // Test case: null data
      const { container } = render(<CapabilityRadar data={null} loading={false} />);

      // Verify nothing is rendered
      expect(container.firstChild).toBeNull();
    });

    it('should render student radar correctly with valid dimensions', () => {
      // Test case: focus on student radar rendering
      const dataWithValidDimensions: RadarData = {
        dimensions: [
          { key: 'tech', label: '技术能力', score: 90, max: 100 },
          { key: 'comm', label: '沟通协作', score: 85, max: 100 }
        ],
        peer_average: [88, 82], // Valid peer data
        history: []
      };

      // Should not crash with valid data
      expect(() => {
        render(<CapabilityRadar data={dataWithValidDimensions} loading={false} />);
      }).not.toThrow();

      // Verify component renders
      expect(screen.getByText('能力雷达画像')).toBeInTheDocument();
    });

    it('Property-based: should render correctly for any valid peer_average data', () => {
      // Property-based test: generate valid RadarData with matching peer_average
      fc.assert(
        fc.property(
          // Generate dimensions array (1-6 dimensions)
          fc.array(
            fc.record({
              key: fc.string({ minLength: 1 }),
              label: fc.string({ minLength: 1 }),
              score: fc.integer({ min: 0, max: 100 }),
              max: fc.constant(100)
            }),
            { minLength: 1, maxLength: 6 }
          ),
          (dimensions) => {
            // Generate peer_average matching dimensions length
            const peer_average = dimensions.map(() => 
              Math.floor(Math.random() * 100)
            );

            const data: RadarData = {
              dimensions,
              peer_average,
              history: []
            };

            // Should not crash with valid data
            expect(() => {
              const { unmount } = render(<CapabilityRadar data={data} loading={false} />);
              unmount(); // Clean up after each render
            }).not.toThrow();
          }
        ),
        { numRuns: 20 } // Run 20 test cases
      );
    });

    it('Property-based: should preserve loading state behavior', () => {
      // Property-based test: verify loading state always shows skeleton
      fc.assert(
        fc.property(
          fc.boolean(), // Random loading state
          fc.oneof(
            fc.constant(null),
            fc.record({
              dimensions: fc.array(
                fc.record({
                  key: fc.string(),
                  label: fc.string(),
                  score: fc.integer({ min: 0, max: 100 }),
                  max: fc.constant(100)
                }),
                { minLength: 1, maxLength: 6 }
              ),
              peer_average: fc.array(fc.integer({ min: 0, max: 100 }), { minLength: 1, maxLength: 6 }),
              history: fc.constant([])
            })
          ),
          (loading, data) => {
            const { container, unmount } = render(<CapabilityRadar data={data as any} loading={loading} />);

            if (loading) {
              // Should show loading skeleton
              const loadingCard = container.querySelector('.animate-pulse');
              expect(loadingCard).toBeInTheDocument();
            } else if (data === null) {
              // Should render nothing
              expect(container.firstChild).toBeNull();
            } else {
              // Should render chart without crashing
              expect(() => {
                screen.getAllByText('能力雷达画像');
              }).not.toThrow();
            }
            
            unmount(); // Clean up after each render
          }
        ),
        { numRuns: 20 } // Run 20 test cases
      );
    });

    it('Property-based: should handle various valid dimension counts', () => {
      // Property-based test: verify component works with different dimension counts
      fc.assert(
        fc.property(
          fc.integer({ min: 1, max: 10 }), // Number of dimensions
          (dimensionCount) => {
            const dimensions = Array.from({ length: dimensionCount }, (_, i) => ({
              key: `dim${i}`,
              label: `维度${i}`,
              score: Math.floor(Math.random() * 100),
              max: 100
            }));

            const peer_average = dimensions.map(() => Math.floor(Math.random() * 100));

            const data: RadarData = {
              dimensions,
              peer_average,
              history: []
            };

            // Should render without crashing
            expect(() => {
              const { unmount } = render(<CapabilityRadar data={data} loading={false} />);
              unmount(); // Clean up after each render
            }).not.toThrow();
          }
        ),
        { numRuns: 20 } // Run 20 test cases
      );
    });
  });
});

# UI Cleanup and Optimization Plan

## Issues Identified
The app has become overly complex with many advanced features that are likely causing functionality issues:

1. **Complex Cooking Mode** - Full cooking activity with voice commands, gestures, timers
2. **Shared Shopping Lists** - Complex sharing system with real-time sync
3. **Store Layout Management** - Aisle-based shopping organization
4. **Smart Shopping Services** - Multiple AI-powered shopping features
5. **Advanced Recipe Features** - Substitution engines, serving size adjustments
6. **Complex Navigation** - Badge systems, contextual FABs

## Cleanup Strategy

### Phase 1: Remove Complex Features (High Risk)
- Remove cooking mode activity and all related classes
- Remove shared shopping functionality
- Remove store layout management
- Remove smart shopping services
- Remove voice command and gesture systems

### Phase 2: Simplify Core Features
- Simplify recipe detail view
- Remove complex substitution systems
- Simplify navigation (keep basic bottom nav)
- Remove badge system complexity

### Phase 3: UI Improvements
- Clean up layouts
- Improve visual design
- Optimize performance
- Ensure core functionality works

## Files to Remove/Modify
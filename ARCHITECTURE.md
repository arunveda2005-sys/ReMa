# RecipeMatcher - System Architecture Document

## Executive Summary

Modern households struggle with meal planning, food waste, and making the most of available ingredients. RecipeMatcher is an intelligent Android application designed to solve this problem by combining structured pantry management with AI-powered recipe recommendations. Using Room Database for local storage and the Gemini API for intelligent recipe generation, RecipeMatcher offers features like smart ingredient tracking, expiry notifications, recipe matching based on available ingredients, and AI-generated cooking suggestions. This transforms meal planning from a tedious chore into an effortless, intelligent experience.

---

## 1. Introduction

In today's busy world, managing household ingredients and planning meals efficiently is a constant challenge. We buy groceries, forget what we have, let food expire, and struggle to find recipes that match our available ingredients. Traditional recipe apps require manual searching and don't understand what's actually in your pantry.

RecipeMatcher was created to bridge this gap. It is more than a recipe app; it's an intelligent kitchen assistant. The system is built on a few core principles:

- **Intelligent Pantry Management**: Track ingredients with quantities, expiry dates, and automatic status monitoring
- **Smart Recipe Matching**: Automatically calculate recipe feasibility based on your current pantry inventory
- **AI-Powered Suggestions**: Generate personalized recipes using AI that considers your available ingredients
- **Proactive Notifications**: Get alerts before ingredients expire to minimize food waste
- **Seamless Organization**: Manage shopping lists, favorites, and cooking sessions in one unified interface

This document details the complete design, architecture, and implementation of the RecipeMatcher Android application, explaining the technical decisions that power its unique features.

---

## 2. System Overview

RecipeMatcher follows a modern, layered architecture that ensures a clean separation of concerns, making the codebase maintainable and scalable. The entire system is built on Android using Java and Kotlin with AndroidX libraries.

### Application Workflow

The application's workflow is straightforward yet powerful:

1. **Pantry Management**: Users add ingredients to their pantry with details like name, quantity, unit, category, and expiry date
2. **Local Storage**: All data is persisted locally using Room Database, ensuring fast access and offline functionality
3. **Recipe Discovery**: Users can browse curated recipes from a local JSONL database or generate AI recipes
4. **Smart Matching**: The system automatically calculates match percentages for recipes based on available pantry items
5. **Shopping Integration**: Missing ingredients can be instantly added to a shopping list for easy grocery planning
6. **AI Generation**: Users can request custom recipes via the Gemini API, which considers their pantry inventory and preferences

This creates a dynamic ecosystem where pantry data drives intelligent recipe recommendations, and recipe needs inform shopping lists.

---

## 3. Objectives

RecipeMatcher was designed to achieve the following key goals:

### 3.1 Comprehensive Pantry Management
- Provide a flexible system for tracking ingredients with detailed metadata (quantity, unit, category, expiry)
- Support batch operations for efficient pantry updates
- Implement intelligent status tracking (fresh, expiring soon, expired)
- Enable quick ingredient search and filtering

### 3.2 Multi-Mode Recipe Discovery
- Offer curated recipes from a local database for offline access
- Implement AI-powered recipe generation for personalized suggestions
- Calculate real-time recipe match percentages based on pantry inventory
- Support advanced filtering by cuisine, difficulty, cooking time, and dietary preferences

### 3.3 Proactive Kitchen Assistant
- Send timely notifications for expiring ingredients
- Suggest recipes that maximize use of soon-to-expire items
- Automatically generate shopping lists from recipe requirements
- Track cooking sessions and favorite recipes

### 3.4 Seamless User Experience
- Provide intuitive Material Design 3 interface
- Support both light and dark themes
- Ensure smooth navigation between pantry, recipes, shopping, and favorites
- Maintain responsive performance with background processing

---

## 4. System Architecture

The application is structured in distinct layers, each with a specific responsibility. This modular approach makes the system robust and easy to develop further.

### 4.1 Presentation Layer (UI)

This is what the user interacts with. Built with Material Design 3 components, it ensures a modern, intuitive, and accessible experience.

**Components**: 
- Activities: `MainActivity`, `SplashActivity`
- Fragments: Organized by feature modules (pantry, recipes, shopping, favorites, settings)
- Custom Views: `CircularProgressIndicator` for visual feedback

**State Management**: 
- ViewModels hold UI-related data, surviving configuration changes
- LiveData provides reactive data observation
- ViewBinding eliminates findViewById() calls

**Layout**: 
- ConstraintLayout for responsive screens
- RecyclerView for efficient list rendering
- Material Components for consistent design language

### 4.2 Data Layer

This layer is the backbone of the app, responsible for all data operations.

**Local Database (Room)**:
- `AppDatabase`: Central database instance
- DAOs: Type-safe database access for each entity
- Entities: `PantryItem`, `Recipe`, `ShoppingItem`, `FavoriteRecipe`
- Converters: Handle complex data types (Lists, Dates)

**Repository Pattern**:
- `PantryRepository`: Manages pantry CRUD operations
- `RecipeRepository`: Handles recipe data and JSONL parsing
- `ShoppingRepository`: Manages shopping list operations
- `EnhancedShoppingRepository`: Advanced shopping features with recipe integration

**Data Models**:
- Entities represent database tables
- POJOs for network responses and complex data structures
- Type converters for Room compatibility

### 4.3 AI Layer

This layer provides the "intelligence" in RecipeMatcher.

**Gemini Integration**:
- AI-powered recipe generation based on available ingredients
- Natural language processing for recipe requests
- Context-aware suggestions considering dietary restrictions

**Networking**:
- Retrofit for HTTP communication
- Gson for JSON serialization/deserialization
- OkHttp as the underlying HTTP client

**Asynchronous Operations**:
- Kotlin Coroutines for background processing
- WorkManager for scheduled tasks (expiry checks, recipe imports)
- LiveData for reactive UI updates

### 4.4 Business Logic Layer

**Calculators and Utilities**:
- `RecipeMatchCalculator`: Computes recipe feasibility based on pantry inventory
- `IngredientStatusCalculator`: Determines ingredient freshness status
- `FilterManager`: Handles complex recipe filtering logic
- `DateUtils`: Manages date formatting and expiry calculations

**Managers**:
- `BatchSelectionManager`: Handles multi-select operations in pantry
- `NotificationHelper`: Manages system notifications
- Theme management for light/dark mode

### 4.5 Background Processing Layer

**WorkManager Workers**:
- `ExpiryCheckWorker`: Periodic checks for expiring ingredients
- `RecipeImportWorker`: Background recipe database updates

**Services**:
- Placeholder for future background services (sync, etc.)

---

## 5. Core Modules

### 5.1 Pantry Management Module

**Purpose**: The foundation of the app - tracking what ingredients users have available.

**Key Features**:
- Add/Edit/Delete ingredients with full metadata
- Batch selection and operations (delete multiple, update categories)
- Swipe gestures for quick actions
- Real-time search and filtering
- Category-based organization
- Quantity and unit tracking

**Data Structure** (`PantryItem`):
```java
- id (Primary Key)
- name
- quantity
- unit
- category
- expiryDate
- addedDate
- notes
```

**UI Components**:
- `PantryFragment`: Main pantry view
- `PantryAdapter`: RecyclerView adapter with ViewHolder pattern
- `AddIngredientDialog`: Modal for adding/editing ingredients
- `PantrySwipeCallback`: Swipe-to-delete functionality
- `BatchSelectionManager`: Multi-select mode

### 5.2 Recipe Discovery Module

**Purpose**: Help users find recipes they can make with available ingredients.

**Implementation**:

**Recipe Types**:
1. **Curated Recipes**: Pre-loaded from `recipes.jsonl` asset file
2. **AI Recipes**: Generated on-demand via Gemini API

**Recipe Data Structure**:
```java
- id
- title
- description
- ingredients (List)
- instructions
- cuisine
- difficulty
- cookingTime
- servings
- imageUrl
- isAiGenerated
```

**Smart Matching**:
- Real-time calculation of ingredient availability
- Match percentage display
- Categorization: "Can Make", "Almost There", "Missing Many"
- Visual indicators (progress circles, color coding)

**UI Components**:
- `RecipeListFragment`: Browse all recipes with filters
- `AiRecipeDetailFragment`: Detailed recipe view with ingredient status
- `AiRecipeAdapter`: Recipe card display
- `RecipeFilterDialog`: Advanced filtering interface
- Ingredient adapters for present/missing items

### 5.3 AI Recipe Generation Module

**Purpose**: Provide personalized recipe suggestions using AI.

**Workflow**:
1. User requests AI recipe (optionally with preferences)
2. System gathers current pantry inventory
3. Constructs prompt with available ingredients and constraints
4. Sends request to Gemini API
5. Parses AI response into structured recipe format
6. Displays recipe with ingredient matching

**Integration Points**:
- Pantry data feeds into AI context
- Generated recipes integrate with shopping list
- Favorites system works with AI recipes

### 5.4 Shopping List Module

**Purpose**: Bridge the gap between what users have and what recipes need.

**Features**:
- Manual item addition
- Automatic generation from recipe requirements
- Check-off functionality
- Category organization
- Quantity tracking
- Integration with pantry (add checked items to pantry)

**Data Structure** (`ShoppingItem`):
```java
- id
- name
- quantity
- unit
- category
- isChecked
- recipeId (optional link)
- addedDate
```

**UI Components**:
- `ShoppingListFragment`: Main shopping interface
- `ShoppingAdapter`: List display with check boxes
- `AddShoppingItemDialog`: Quick add interface

### 5.5 Favorites Module

**Purpose**: Let users save and quickly access their preferred recipes.

**Implementation**:
- Simple bookmark system
- Quick access from recipe details
- Separate favorites view
- Works with both curated and AI recipes

**Data Structure** (`FavoriteRecipe`):
```java
- id
- recipeId
- recipeName
- addedDate
```

### 5.6 Settings and Profile Module

**Purpose**: Give users control over app behavior and appearance.

**Features**:
- Theme selection (Light/Dark/System)
- Notification preferences
- Data management (clear pantry, reset app)
- About information

---

## 6. Technical Implementation Details

### 6.1 Database Architecture (Room)

**Database Class**: `AppDatabase`
- Singleton pattern for single instance
- Version management for migrations
- Type converters for complex types

**DAOs** (Data Access Objects):
- Suspend functions for coroutine support
- LiveData return types for reactive UI
- Complex queries with @Query annotations

**Entities**:
- Annotated POJOs representing tables
- Primary keys with auto-generation
- Foreign key relationships where applicable

### 6.2 Repository Pattern

**Benefits**:
- Single source of truth for data
- Abstraction over data sources
- Easy to test and mock
- Centralized business logic

**Implementation**:
- Repositories expose suspend functions
- Handle data transformation
- Manage caching strategies
- Coordinate between local and remote data

### 6.3 ViewModel Architecture

**Responsibilities**:
- Hold UI state
- Survive configuration changes
- Expose LiveData to UI
- Coordinate repository calls

**Key ViewModels**:
- `PantryViewModel`: Pantry operations and state
- `RecipeViewModel`: Recipe data and filtering
- `ShoppingViewModel`: Shopping list management
- `FavouriteViewModel`: Favorites tracking

### 6.4 Navigation Architecture

**Navigation Component**:
- Single Activity architecture
- Fragment-based navigation
- Type-safe argument passing
- Deep linking support

**Navigation Graph**:
- Bottom navigation for main sections
- Fragment transactions for details
- Back stack management

### 6.5 Background Processing

**WorkManager**:
- Guaranteed execution
- Constraint-based scheduling
- Battery-efficient
- Survives app restarts

**Workers**:
- `ExpiryCheckWorker`: Daily checks at 9 AM
- `RecipeImportWorker`: One-time recipe loading

### 6.6 UI/UX Patterns

**Material Design 3**:
- Dynamic color theming
- Elevation and shadows
- Motion and transitions
- Accessibility compliance

**RecyclerView Patterns**:
- ViewHolder pattern
- DiffUtil for efficient updates
- ItemTouchHelper for swipe actions
- Multiple view types

**Dialogs and Bottom Sheets**:
- Material dialogs for confirmations
- Custom dialogs for complex inputs
- Bottom sheets for contextual actions

---

## 7. Data Flow Diagrams

### 7.1 Adding an Ingredient
```
User Input → AddIngredientDialog → PantryViewModel → PantryRepository → Room Database → LiveData Update → UI Refresh
```

### 7.2 Recipe Matching
```
Recipe Request → RecipeViewModel → RecipeRepository (recipes) + PantryRepository (ingredients) → RecipeMatchCalculator → Sorted Recipe List → UI Display
```

### 7.3 AI Recipe Generation
```
User Request → AI Fragment → Gemini API Client → Network Request → JSON Response → Recipe Parser → Recipe Display → Optional Save to Favorites
```

### 7.4 Shopping List to Pantry
```
Check Shopping Item → ShoppingViewModel → Update Database → User Confirms → Transfer to Pantry → PantryRepository → Add to Pantry → Remove from Shopping
```

---

## 8. Security and Privacy

### 8.1 Data Storage
- All data stored locally on device
- No cloud synchronization (privacy-first approach)
- Room database encrypted at OS level
- No personal data collection

### 8.2 API Security
- API keys stored in BuildConfig (not in version control)
- HTTPS for all network requests
- No user data sent to external services except recipe generation context

### 8.3 Permissions
- Minimal permission requirements
- Notification permission for expiry alerts
- No location, camera, or contacts access

---

## 9. Performance Optimization

### 9.1 Database Optimization
- Indexed columns for frequent queries
- Efficient query design
- Pagination for large datasets
- Background thread execution

### 9.2 UI Performance
- RecyclerView with ViewHolder pattern
- DiffUtil for minimal UI updates
- Image loading with Glide (caching, memory management)
- Lazy loading of recipe details

### 9.3 Memory Management
- ViewModel lifecycle awareness
- Proper LiveData observation
- Bitmap recycling
- Leak prevention (no Activity references in background tasks)

---

## 10. Testing Strategy

### 10.1 Unit Tests
- Repository logic
- Calculator utilities
- Data transformations
- ViewModel business logic

### 10.2 Integration Tests
- Database operations
- Repository-ViewModel interaction
- Worker execution

### 10.3 UI Tests (Espresso)
- Fragment navigation
- User interactions
- Dialog flows
- RecyclerView operations

---

## 11. Future Enhancements

### 11.1 Cloud Sync
- Firebase integration for multi-device sync
- User authentication
- Backup and restore

### 11.2 Advanced AI Features
- Meal planning suggestions
- Nutritional analysis
- Dietary restriction handling
- Recipe customization

### 11.3 Social Features
- Recipe sharing
- Community recipes
- Rating and reviews

### 11.4 Enhanced Tracking
- Cooking history
- Ingredient usage analytics
- Cost tracking
- Waste reduction metrics

---

## 12. Conclusion

RecipeMatcher demonstrates a well-architected Android application following modern best practices. The four-layer architecture (Presentation, Data, AI, Business Logic) ensures maintainability and scalability. By combining local data persistence with AI-powered intelligence, the app provides a seamless kitchen management experience that helps users reduce food waste, discover new recipes, and make the most of their available ingredients.

The modular design allows for easy feature additions and modifications, while the repository pattern and ViewModel architecture ensure testability and separation of concerns. The integration of Room Database, WorkManager, and Retrofit showcases proper use of Android Jetpack components, and the Material Design 3 implementation provides a modern, accessible user interface.

---

## Appendix A: Technology Stack

**Core**:
- Language: Java 21, Kotlin
- Min SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)
- Build System: Gradle (Kotlin DSL)

**UI**:
- Material Design 3
- ViewBinding
- ConstraintLayout
- RecyclerView
- Navigation Component

**Data**:
- Room Database
- LiveData
- Repository Pattern

**Networking**:
- Retrofit 2
- Gson
- OkHttp

**Background Processing**:
- WorkManager
- Kotlin Coroutines

**Image Loading**:
- Glide

**Testing**:
- JUnit
- Espresso
- AndroidX Test

---

## Appendix B: Project Structure

```
com.example.receipematcher/
├── data/
│   ├── converters/          # Room type converters
│   ├── db/                  # Database and DAOs
│   ├── entities/            # Room entities
│   └── repository/          # Repository implementations
├── network/                 # API responses and models
├── ui/
│   ├── components/          # Reusable UI components
│   ├── pantry/              # Pantry management UI
│   ├── recipes/             # Recipe browsing and details
│   ├── shopping/            # Shopping list UI
│   ├── favorites/           # Favorites UI
│   ├── settings/            # Settings and preferences
│   └── theme/               # Theme management
├── utils/                   # Utility classes and helpers
├── viewmodel/               # ViewModels for each feature
├── workers/                 # WorkManager workers
├── App.java                 # Application class
└── MainActivity.java        # Main activity host
```

---

*Document Version: 1.0*  
*Last Updated: November 12, 2025*  
*Project: RecipeMatcher Android Application*

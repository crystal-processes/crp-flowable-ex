# CRP Flowable UI

React-based frontend module for CRP Flowable applications.

## Overview

This module provides a modern React UI for interacting with Flowable BPM processes, tasks, and forms. It's built with:

- **React 18+** - Modern React with hooks
- **Vite** - Lightning-fast build tool
- **React Router** - Client-side routing
- **Vitest** - Unit testing framework
- **ESLint** - Code quality

## Building

This module is built automatically as part of the Maven build process. The built frontend is packaged as a JAR with the static assets included.

### Manual Build

If you need to build the frontend manually:

```bash
cd crp-flowable-ui
npm install
npm run build
```

### Available Scripts

Inside the module directory:

- `npm run dev` - Start development server (with Vite)
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm test` - Run unit tests
- `npm run test:ui` - Run tests with visual UI
- `npm run test:coverage` - Generate test coverage
- `npm run lint` - Run ESLint

## Maven Integration

The Maven build automatically:

1. **Downloads Node.js and npm** if not present
2. **Installs dependencies** using npm
3. **Runs linting** to ensure code quality
4. **Runs tests** to verify functionality
5. **Builds the frontend** using Vite
6. **Packages the dist folder** into the JAR

### Build Properties

- `skipLinting` - Skip ESLint during build (set with `-DskipLinting=true`)
- `skipTests` - Skip tests during build (set with `-DskipTests=true`)
- `node.version` - Node.js version to download (default: v18.19.0)
- `npm.version` - npm version to download (default: 10.2.3)

## Using in Spring Boot Application

The built frontend is automatically packaged as a JAR module that can be included as a dependency in your Spring Boot application:

```xml
<dependency>
  <groupId>io.github.crystal-processes</groupId>
  <artifactId>crp-flowable-ui</artifactId>
  <version>0.0.9-SNAPSHOT-SNAPSHOT</version>
</dependency>
```

Spring Boot will serve the static assets from the `static/` classpath when the frontend JAR is on the classpath.

## Project Structure

```
crp-flowable-ui/
├── src/
│   ├── assembly/
│   │   └── dist.xml          # Maven assembly descriptor
│   └── ...
├── frontend/                  # React application
│   ├── src/
│   │   ├── hooks/            # Custom React hooks
│   │   ├── context/          # React Context providers
│   │   ├── components/       # React components
│   │   ├── utils/            # Utility functions
│   │   ├── test/             # Test setup
│   │   └── App.jsx           # Main component
│   ├── package.json
│   ├── vitest.config.js
│   └── vite.config.js
├── pom.xml                    # Maven configuration
└── README.md
```

## Architecture

The frontend uses a modular architecture with:

- **Custom Hooks** - Reusable logic (useFetchFlowable, useAutoRefresh)
- **Context API** - Global state management (FilterContext)
- **Component Composition** - Modular, testable components
- **Configuration Center** - Centralized config.js for settings

## Testing

The module includes comprehensive tests:

- **Unit Tests** - Test individual hooks and components
- **Integration Tests** - Test component interactions
- **Coverage Reports** - Generated with Vitest

Run tests with Maven:

```bash
mvn test
```

Or test only the frontend (skipping Maven):

```bash
cd frontend
npm test
```

## API Integration

The frontend communicates with the Flowable REST API endpoints defined in `src/config.js`:

- `/process-api/query/historic-task-instances` - Fetch tasks
- `/process-api/repository/process-definitions` - Fetch process definitions
- `/process-api/runtime/process-instances` - Fetch/start process instances
- `/process-api/form/form-data` - Fetch form data
- `/process-api/form/form-submit` - Submit forms

## Configuration

Frontend configuration is centralized in `src/config.js`:

```javascript
export const CONFIG = {
  API: { /* API endpoints */ },
  REFRESH: { /* Auto-refresh settings */ },
  PAGINATION: { /* Pagination defaults */ },
  UI: { /* UI preferences */ },
  FEATURES: { /* Feature flags */ }
}
```

## Development

To develop the frontend locally:

1. Navigate to the frontend directory
2. Run `npm install` to install dependencies
3. Run `npm run dev` to start the development server
4. Edit source files - changes will hot-reload in the browser
5. Run `npm test` to run tests as you develop

## Performance

The module includes several performance optimizations:

- **Code Splitting** - Vite automatically code-splits your app
- **Tree Shaking** - Unused code is eliminated in production
- **Minification** - Production builds are minified
- **Memoization** - React.memo and useMemo prevent unnecessary renders
- **Lazy Loading** - Components are code-split automatically

## Deployment

The built frontend is served by the Spring Boot application. To deploy:

1. Build the full project: `mvn clean install`
2. The frontend JAR is created and can be published to Maven repositories
3. Include the dependency in your application's pom.xml
4. Spring Boot will serve the static assets at the application root

## License

Apache License 2.0 - See LICENSE file in parent module

## Contributing

See the main CRP Flowable EX project for contribution guidelines.

# Frontend Tests

This directory contains test configurations and utilities for the CRP Flowable frontend.

## Running Tests

### Run all tests
```bash
npm test
```

### Run tests in watch mode
```bash
npm test -- --watch
```

### Run tests with UI
```bash
npm run test:ui
```

### Generate coverage report
```bash
npm run test:coverage
```

## Test Files

- **`setup.js`** - Test setup and global configurations
- **`hooks/`** - Tests for custom React hooks
- **`context/`** - Tests for React Context providers
- **`components/`** - Tests for React components

## Test Coverage Goals

- Custom hooks: 90%+
- Context providers: 85%+
- Critical components: 80%+
- Overall: 70%+

## Writing Tests

### Test Structure

```javascript
import { describe, it, expect, beforeEach } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'

describe('ComponentName', () => {
  beforeEach(() => {
    // Setup
  })

  it('should do something', () => {
    // Test code
  })
})
```

### Testing Hooks

Use `renderHook` from `@testing-library/react`:

```javascript
const { result, rerender, unmount } = renderHook(() => useMyHook())
```

### Testing Components

Use `render` and query functions:

```javascript
render(<MyComponent prop="value" />)
expect(screen.getByText('expected text')).toBeInTheDocument()
```

## Mocking

Global mocks are available in `setup.js`:
- `fetch` - Mocked fetch API
- `localStorage` - Mocked browser storage

## Resources

- [Vitest Documentation](https://vitest.dev/)
- [React Testing Library](https://testing-library.com/react)
- [Testing Best Practices](https://kentcdodds.com/blog/common-mistakes-with-react-testing-library)

# Fint Core Status Service Frontend

🚧 **Under Construction** 🚧

A React application for monitoring Fint Core service status and events.

_This project is currently in development._

***Frontend only exists in API environment.***

## Getting Started

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

The application will be available at `http://localhost:5173`.

## Building for Production

```bash
npm run build
```

## Available Scripts

### Development

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run typecheck` - Run TypeScript type checking

### Code Quality

- `npm run lint` - Run ESLint on app and cypress
- `npm run lint:fix` - Run ESLint with auto-fix
- `npm run format` - Format all files with Prettier
- `npm run format:check` - Check formatting without modifying files

### Testing

- `npm run cypress:open` - Open Cypress Test Runner (E2E)
- `npm run cypress:run` - Run E2E tests headlessly
- `npm run cypress:open:component` - Open Cypress Component Test Runner
- `npm run cypress:run:component` - Run component tests headlessly

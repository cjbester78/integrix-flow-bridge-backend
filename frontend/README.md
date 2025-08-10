# Integrix Flow Bridge - Frontend

## Project Info

This is the frontend application for Integrix Flow Bridge, an enterprise integration platform.

## Prerequisites

- Node.js & npm installed - [install with nvm](https://github.com/nvm-sh/nvm#installing-and-updating)
- Java 17 or higher (for backend)
- MySQL 8.0 or higher (for database)

## Getting Started

Follow these steps to run the frontend locally:

```sh
# Step 1: Clone the repository
git clone <YOUR_GIT_URL>

# Step 2: Navigate to the frontend directory
cd integrix-flow-bridge/frontend-ui

# Step 3: Install dependencies
npm install

# Step 4: Start the development server
npm run dev
```

The frontend will start on http://localhost:5173

## Build for Production

```sh
# Build the frontend
npm run build

# The built files will be in the dist/ directory
```

## Technologies Used

This project is built with:

- **Vite** - Build tool and dev server
- **TypeScript** - Type-safe JavaScript
- **React 18** - UI framework
- **shadcn/ui** - Component library
- **Tailwind CSS** - Utility-first CSS framework
- **React Query** - Data fetching and caching
- **React Flow** - Visual flow editor
- **Zustand** - State management
- **React Hook Form** - Form handling
- **Axios** - HTTP client

## Project Structure

```
frontend-ui/
├── src/
│   ├── components/     # Reusable UI components
│   ├── pages/         # Page components
│   ├── services/      # API services
│   ├── hooks/         # Custom React hooks
│   ├── stores/        # Zustand stores
│   ├── lib/           # Utilities and helpers
│   └── types/         # TypeScript type definitions
├── public/            # Static assets
└── dist/             # Production build output
```

## Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint
- `npm run type-check` - Run TypeScript compiler

## Environment Configuration

Create a `.env` file in the frontend-ui directory:

```env
VITE_API_BASE_URL=http://localhost:8080
```

## Integration with Backend

The frontend expects the backend API to be running on http://localhost:8080. Make sure to start the backend server before running the frontend.

## Contributing

1. Create a feature branch
2. Make your changes
3. Test thoroughly
4. Submit a pull request

## License

See the EULA.md file in the root directory.
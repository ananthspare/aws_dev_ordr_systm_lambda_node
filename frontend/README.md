# E-Commerce Frontend

Angular 17 frontend application for the e-commerce system.

## Features

- **Authentication**: Login and registration with JWT tokens
- **Product Catalog**: Browse and search products
- **Shopping Cart**: Add/remove items, update quantities
- **Order Management**: View order history and details
- **User Profile**: Manage account information
- **Responsive Design**: Works on desktop and mobile devices

## Technology Stack

- Angular 17 (Standalone Components)
- Angular Material UI
- RxJS for reactive programming
- TypeScript
- SCSS for styling

## Project Structure

```
src/
├── app/
│   ├── core/                 # Core services, models, guards
│   │   ├── guards/          # Route guards (auth)
│   │   ├── interceptors/    # HTTP interceptors
│   │   ├── models/          # TypeScript interfaces
│   │   └── services/        # Business logic services
│   ├── features/            # Feature modules
│   │   ├── auth/           # Authentication (login/register)
│   │   ├── products/       # Product listing and details
│   │   ├── cart/           # Shopping cart
│   │   ├── orders/         # Order management
│   │   └── profile/        # User profile
│   ├── shared/             # Shared components
│   │   └── components/     # Reusable UI components
│   └── app.component.ts    # Root component
├── environments/           # Environment configurations
└── styles.scss            # Global styles
```

## Getting Started

### Prerequisites

- Node.js (v18 or higher recommended)
- npm or yarn
- Angular CLI (`npm install -g @angular/cli`)

### Installation

1. Install dependencies:
```bash
npm install
```

2. Start the development server:
```bash
ng serve
```

3. Open your browser and navigate to `http://localhost:4200`

### Backend Integration

The frontend is configured to connect to the Spring Boot backend API:

- **Development**: `http://localhost:8080/api`
- **Production**: Configure in `src/environments/environment.prod.ts`

## Available Scripts

- `ng serve` - Start development server
- `ng build` - Build for production
- `ng test` - Run unit tests
- `ng lint` - Run linting

## Key Components

### Authentication
- Login and registration forms
- JWT token management
- Route protection with guards

### Product Management
- Product listing with pagination
- Product detail views
- Search and filtering capabilities

### Shopping Cart
- Add/remove items
- Update quantities
- Cart persistence
- Checkout process

### Order Management
- Order history
- Order status tracking
- Invoice downloads

## API Integration

The frontend communicates with the backend through:

- **AuthService**: User authentication and authorization
- **ProductService**: Product catalog operations
- **CartService**: Shopping cart management
- **OrderService**: Order processing and history

## Styling

- Angular Material for UI components
- Custom SCSS for additional styling
- Responsive design with CSS Grid and Flexbox
- Material Design theme customization

## Development Notes

- Uses Angular 17 standalone components (no NgModules)
- Implements reactive forms with validation
- Error handling with user-friendly messages
- Loading states and progress indicators
- Optimistic UI updates for better UX

## Future Enhancements

- Product search and filtering
- Wishlist functionality
- Product reviews and ratings
- Real-time order tracking
- Push notifications
- Progressive Web App (PWA) features
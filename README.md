# Investment Portfolio Management Dashboard

A comprehensive dashboard application for managing investment products, portfolios, transactions, and performance tracking.

## Overview

This application provides a robust platform for investment management with role-based access control, real-time performance tracking, and comprehensive portfolio management features. It supports multiple user roles with different permissions, allowing for a secure and tailored experience.

## Features

### Core Functionality

- **Product Management**
  - Create, view, edit, and delete investment products
  - Categorize products by risk level (Low, Medium, High)
  - Track product descriptions and investment strategies

- **Portfolio Management**
  - Create and manage investment portfolios
  - Add and remove investment products from portfolios
  - Track portfolio performance and asset allocation

- **Transaction Management**
  - Record buy/sell transactions for portfolio assets
  - Transaction history with filtering and sorting
  - Track transaction impact on portfolio value

- **Performance Analytics**
  - Real-time performance tracking
  - Risk allocation visualization
  - Benchmark comparisons
  - Portfolio value trend analysis

- **Account Management**
  - User account administration (admin only)
  - Role assignment and permission management

### User Experience

- Responsive design working on desktop and mobile devices
- Interactive dashboard with key performance indicators
- Color-coded risk levels for quick identification
- Collapsible sidebar for maximizing workspace
- Toast notifications for user feedback
- Confirmation dialogs for destructive actions

## User Roles and Permissions

The system implements role-based access control with four user roles:

1. **Investor**
   - Access to personal portfolios and transactions
   - View performance metrics
   - Create and manage their own portfolios

2. **Manager**
   - All investor capabilities
   - Product management (create, edit, delete)
   - Transaction management for all portfolios
   - Access to all portfolios

3. **Analyst**
   - View products and portfolios
   - Access to performance metrics
   - Cannot modify products or execute transactions

4. **Admin**
   - All system capabilities
   - User account management
   - System configuration

## Technology Stack

### Frontend
- **Next.js 14** - React framework with App Router
- **React** - UI library
- **TypeScript** - Type-safe JavaScript
- **Tailwind CSS** - Utility-first CSS framework
- **Shadcn UI** - Component library based on Radix UI
- **Lucide React** - Icon library

### Backend
- **Next.js API Routes** - Backend API endpoints
- **Vercel Postgres** - SQL database integration
- **Neon Database** - Serverless Postgres provider
- **NextAuth.js** - Authentication
- **bcryptjs** - Password hashing
- **JSON Web Tokens (JWT)** - Secure authentication

### Development Tools
- **ESLint** - Code linting
- **Prettier** - Code formatting
- **npm** - Package management

## Database Schema

The application uses a relational database with the following core tables:

- **accounts** - User accounts and authentication information
- **products** - Investment products with risk levels and strategies
- **portfolios** - User portfolios
- **assets** - Assets within portfolios linked to products
- **transactions** - Record of buying and selling activities
- **performances** - Historical performance data for analytics

## Getting Started

### Prerequisites

- Node.js 18.x or higher
- npm or yarn
- PostgreSQL database (or Vercel Postgres/Neon account)

### Installation

1. Clone the repository
   ```bash
   git clone <repository-url>
   cd investment-dashboard
   ```

2. Install dependencies
   ```bash
   npm install
   ```

3. Set up environment variables
   Create a `.env.local` file with the following variables:
   ```
   # Database (Vercel Postgres or Neon)
   POSTGRES_URL=
   POSTGRES_PRISMA_URL=
   POSTGRES_URL_NON_POOLING=
   POSTGRES_USER=
   POSTGRES_HOST=
   POSTGRES_PASSWORD=
   POSTGRES_DATABASE=

   # Authentication
   JWT_SECRET=your-secret-key-min-32-chars
   
   # Next.js
   NEXT_PUBLIC_WS_ENABLED=true
   ```

4. Initialize the database
   ```bash
   npm run db:setup
   ```

5. Run the development server
   ```bash
   npm run dev
   ```

6. Open [http://localhost:3000](http://localhost:3000) in your browser

### Development Workflow

1. Create feature-specific branches from `main`
2. Follow the code style and organization patterns
3. Write tests for new features
4. Submit pull requests for review

## Code Organization

The project follows the Next.js App Router structure:

- `/app` - Application routes and pages
  - `/dashboard` - Dashboard-related pages
  - `/api` - API endpoints
- `/components` - Reusable UI components
- `/lib` - Utility functions and shared logic
  - `/db` - Database models and queries
  - `/auth` - Authentication logic
- `/public` - Static assets

## API Reference

The application provides RESTful API endpoints for data operations:

- `GET /api/products` - Retrieve all products
- `GET /api/products/:id` - Retrieve a specific product
- `POST /api/products` - Create a new product
- `PUT /api/products/:id` - Update a product
- `DELETE /api/products/:id` - Delete a product

Similar endpoints exist for portfolios, transactions, and performance data.

## Deployment

The application is optimized for deployment on Vercel:

1. Connect your GitHub repository to Vercel
2. Configure the environment variables
3. Deploy the application

For other platforms, build the application using:
```bash
npm run build
```

## Contributing

1. Follow the established code style and patterns
2. Ensure all tests pass before submitting PR
3. Document new features or significant changes
4. Keep commits focused and descriptive

## License

This project is licensed under the [MIT License](LICENSE).

## Acknowledgments

- Built with Next.js and Shadcn UI
- Database hosting by Vercel/Neon
- Icons by Lucide React 
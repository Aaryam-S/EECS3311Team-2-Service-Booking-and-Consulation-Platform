# EECS3311Team-2-Service-Booking-and-Consulation-Platform
# (Phase 1, skip to Line 118 for Phase 2)

## Architecture Overview

The Service Booking and Consultation Platform is a Java-based application that facilitates the complete lifecycle of service bookings between clients and consultants.

### Core Components

**Model Layer** (`src/model/`)
- **Booking**: Central entity managing booking lifecycle with state pattern integration
- **Client/Consultant/Admin**: User actors with specific roles and responsibilities  
- **Service/TimeSlot**: Service definitions and availability management
- **SystemPolicy**: Singleton managing global system configuration and policies
- **PaymentReceipt/SavedPaymentMethod**: Payment processing entities

**Service Layer** (`src/service/`)
- **CatalogService**: Singleton managing service catalog and availability
- **PaymentService**: Handles payment processing with multiple payment strategies

**State Management** (`src/state/`)
- **BookingState** interface with concrete implementations (RequestedState, ConfirmedState, PaidState, CancelledState, CompletedState, RejectedState)
- Manages booking state transitions and validates state-specific operations

**Strategy Pattern** (`src/strategy/`)
- **PaymentStrategy** interface with implementations (CreditCard, DebitCard, PayPal, BankTransfer)
- Enables interchangeable payment processing methods

**Policy Management** (`src/policy/`)
- **PricingStrategy**: Base/Taxed pricing calculations
- **CancellationPolicy**: Flexible/Strict/NoRefund cancellation rules
- **SystemPolicy**: Global policy configuration singleton

**Notification System** (`src/notification/`)
- **Observer pattern** implementation for real-time notifications
- **NotificationService** manages message delivery to clients and consultants

### Data Flow

1. **Service Discovery**: Clients browse services through CatalogService
2. **Booking Creation**: Bookings created in RequestedState with TimeSlot allocation
3. **State Transitions**: Bookings progress through states based on user actions
4. **Payment Processing**: PaymentService handles transactions using selected strategies
5. **Policy Enforcement**: SystemPolicy applies pricing and cancellation rules
6. **Notifications**: Observer pattern notifies stakeholders of state changes

## Design Patterns Used

### Singleton Pattern
- **Location**: `src/service/CatalogService.java`
- **Purpose**: Ensures only one instance of the catalog service exists throughout the application

### State Pattern
- **Location**: `src/state/` package
- **Classes**: `BookingState.java`, `RequestedState.java`, `ConfirmedState.java`, `PaidState.java`, `CancelledState.java`, `CompletedState.java`, `RejectedState.java`
- **Purpose**: Manages booking state transitions and behaviors based on current booking status

### Strategy Pattern
- **Location**: `src/strategy/` package
- **Classes**: `PayPalStrategy.java`, `CreditCardStrategy.java`, `DebitCardStrategy.java`, `BankTransferStrategy.java`
- **Purpose**: Allows different payment methods to be used interchangeably

### Observer Pattern
- **Location**: `src/notification/` package
- **Classes**: `Observer.java`, `NotificationService.java`
- **Purpose**: Implements notification system for booking updates

### Strategy Pattern (Pricing)
- **Location**: `src/policy/` package
- **Classes**: `BasePriceStrategy.java`, `TaxedPriceStrategy.java`, `PricingStrategy.java`
- **Purpose**: Handles different pricing calculations

### Strategy Pattern (Cancellation)
- **Location**: `src/policy/` package
- **Classes**: `FlexibleCancellation.java`, `StrictCancellation.java`, `NoRefundCancellation.java`, `CancellationPolicy.java`
- **Purpose**: Implements different cancellation policies

## How to Run the Application

### Option 1: Eclipse IDE
1. Open Eclipse IDE
2. Import project: `File > Import > General > Existing Projects into Workspace`
3. Browse to project directory and import
4. Right-click `src/ui/Main.java` → `Run As` → `Java Application`

### Option 2: Command Line
```bash
# Navigate to project directory
cd C:\xxx\xxx\EECS3311Team-2-Service-Booking-and-Consulation-Platform

# Compile the project
javac -d bin -sourcepath src src/ui/Main.java

# Run the application
java -cp bin ui.Main
```

## GitHub Repository

https://github.com/Aaryam-S/EECS3311Team-2-Service-Booking-and-Consulation-Platform.git

## Team Member Contributions

- Aaryam: 
- model/Booking, model/Service, model/Timeslot files
- entire state package
- service/CatalogService file

- Arshjot:
- model/Client, model/Consultant, model/Admin, model/SystemPolicy, model/SavedPaymentMethod, model/PaymentReceipt files
- service/PaymentService files
- entire strategy package
- entire policy package
- UI

- Precious:
- entire notification package

# EECS3311 Team 2 — Service Booking and Consultation Platform (Phase 2)

## Project Overview

This platform enables clients to book services with consultants, manage bookings, process payments, and receive notifications. It uses a Java/Spring backend, a React frontend, a relational database, and an integrated AI chatbot for client support.

## Architecture & Features

- **Backend:** Java (Spring), RESTful API, JDBC for database access, parameterized queries for security.
- **Frontend:** React (Vite), communicates with backend via REST API.
- **Database:** Relational (PostgreSQL/MySQL, configurable in `application.properties`).
- **AI Chatbot:** Integrated into the client dashboard for platform Q&A (see `AI_CHATBOT_DOCUMENTATION.md`).
- **Dockerized Deployment:** Both frontend and backend have Dockerfiles. Use `docker-compose.yml` to build and run the stack (database, backend, frontend) with one command.

## Core Design Patterns

- **State Pattern:** Booking lifecycle (Requested, Confirmed, Paid, Cancelled, Completed, Rejected) in `src/state/`.
- **Strategy Pattern:** Payment methods (CreditCard, DebitCard, PayPal, BankTransfer) in `src/strategy/` and pricing/cancellation in `src/policy/`.
- **Observer Pattern:** Notification system in `src/notification/`.
- **Singleton Pattern:** CatalogService and SystemPolicy singletons.

## Main Modules

- **Model Layer:** Booking, Client, Consultant, Admin, Service, TimeSlot, SystemPolicy, PaymentReceipt, SavedPaymentMethod
- **Service Layer:** CatalogService, PaymentService, ClientService, ConsultantService
- **Controller Layer:** REST endpoints for all major operations (see `controller/`)
- **UI:** React-based, with pages for login, registration, dashboards, booking, and payment

## How to Run

### Option 1: Docker Compose (Recommended)
1. Ensure Docker and Docker Compose are installed.
2. In the project root, run:
	 ```bash
	 docker-compose up --build
	 ```
3. Backend: `http://localhost:8080`  |  Frontend: `http://localhost:3000`

### Option 2: Manual (Dev Mode)

**Backend:**
1. Configure DB in `backend/src/application.properties`.
2. Build and run backend (e.g., with Maven or your IDE).

**Frontend:**
1. In `frontend/`, run:
	 ```bash
	 npm install
	 npm run dev
	 ```
2. Access at `http://localhost:3000`.

## Security Notes

- All SQL queries use parameterized statements to prevent SQL injection.
- Passwords are hashed with SHA-256. For production, use bcrypt or Argon2 for stronger security.

## API Documentation

- See the `controller/` package for available endpoints and their usage.

## Team Member Contributions

- **Aaryam**
	- Booking, Service, Timeslot models
	- State management (entire `state` package)
	- CatalogService
    - Created Phase 2 frontend
    - Helped frontend-backend integration

- **Arshjot**
	- Client, Consultant, Admin, SystemPolicy, SavedPaymentMethod, PaymentReceipt models
	- PaymentService
	- All payment and policy strategies (`strategy` and `policy` packages)
	- Frontend UI (Phase 1 Demo)
    - Created Phase 2 backend
    - Helped frontend-backend-database integration

- **Precious**
	- Notification system (entire `notification` package)
    - Created Phase 2 chatbot and database components
    - Helped backend-database integration

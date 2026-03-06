# EECS3311Team-2-Service-Booking-and-Consulation-Platform

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
javac -d bin src\ui\Main.java

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

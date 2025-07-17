# Sweet Order Manager

A complete Android application for managing sweet orders in a small shop, built with Kotlin and Firebase Firestore.

## Features

- **Add New Orders**: Create orders with customer name, date, and multiple sweet items
- **Order Management**: View all orders in a beautiful RecyclerView with real-time updates
- **Delivery Confirmation**: Mark orders as delivered with a single click
- **Filtering**: Filter orders by product and date
- **Sales Summary**: View grand total and product-wise sales
- **Material Design 3**: Modern, clean UI with Material Design 3 components
- **Firebase Integration**: Real-time data synchronization with Firestore

## Technical Stack

- **Language**: Kotlin
- **Architecture**: MVVM pattern with ViewBinding
- **Database**: Firebase Firestore (NoSQL)
- **UI Framework**: Material Design 3
- **Components**: RecyclerView, ViewBinding, DatePickerDialog, Spinners

## Product Catalog

| Product Name | Price |
|--------------|--------|
| Bundi | ₹400 |
| Khaja | ₹400 |
| Mohanthad | ₹450 |
| Champakali Ganthiya | ₹250 |
| Mathadi | ₹350 |

## Project Structure

```
com.example.sweetordermanager/
├── model/
│   ├── Order.kt
│   └── OrderItem.kt
├── ui/
│   ├── MainActivity.kt
│   ├── AddOrderActivity.kt
│   └── adapters/
│       └── OrdersAdapter.kt
├── util/
│   ├── Constants.kt
│   └── FirebaseHelper.kt
```

## Setup Instructions

### 1. Firebase Setup
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project named "Sweet Order Manager"
3. Add an Android app with package name: `com.example.sweetordermanager`
4. Download the `google-services.json` file and place it in the `app/` directory
5. Enable Firestore Database in Firebase Console

### 2. Android Studio Setup
1. Open Android Studio
2. Select "Open an existing Android Studio project"
3. Navigate to the `SweetOrderManager` directory
4. Wait for Gradle sync to complete
5. Run the app on an emulator or physical device

### 3. Firestore Database Structure
The app will automatically create a collection named "orders" with documents containing:
- `customerName`: String
- `date`: String (format: dd/MM/yyyy)
- `orderItems`: Array of objects with productName, quantity, unitPrice
- `totalPrice`: Double
- `delivered`: Boolean

## Usage Guide

### Adding a New Order
1. Click "Add Order" button on main screen
2. Enter customer name
3. Select order date using date picker
4. Add sweet items:
   - Select product from dropdown
   - Enter quantity
   - Unit price auto-fills based on product selection
5. Review total price
6. Click "Save Order"

### Managing Orders
- **View Orders**: All orders appear in the main list
- **Filter Orders**: Use product dropdown or date picker to filter
- **Clear Filters**: Click "Clear Filters" button
- **Confirm Delivery**: Click "Confirm Delivery" button on pending orders

### Sales Summary
- **Grand Total**: Shows total sales for filtered orders
- **Product-wise Sales**: Shows individual product sales amounts

## Development Notes

- Uses ViewBinding for type-safe view references
- Real-time updates with Firestore listeners
- Input validation for all forms
- Error handling for Firebase operations
- Responsive design for different screen sizes

## Future Enhancements

- Customer management system
- Inventory tracking
- Order history and analytics
- Export functionality
- Push notifications for new orders
- Dark theme support

## License

This project is open source and available under the MIT License.

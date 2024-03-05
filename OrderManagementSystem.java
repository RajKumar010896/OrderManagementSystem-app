import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class OrderManagementSystem extends JFrame {

    private JTextField customerNameField;
    private JTextField phoneNumberField;
    private JTextField emailField;
    private JTextField deliveryAddressField;
    private JTextField quantityField;
    private Map<String, JCheckBox> menuItemsMap;

     // Admin Login Components
     private JTextField adminUsernameField;
     private JPasswordField adminPasswordField;
     private JButton adminLoginButton;
 
     private boolean isAdminLoggedIn = false;

    public OrderManagementSystem() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Order Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set the JFrame background color to sky blue
        getContentPane().setBackground(new Color(135, 206, 250));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(0, 2, 10, 20));

         // Set sky blue background color
        mainPanel.setBackground(new Color(135, 206, 250)); // RGB values for sky blue


        // Customer Name
        mainPanel.add(new JLabel("Customer Name:"));
        customerNameField = new JTextField();
        customerNameField.setBackground(new Color(135, 206, 250));
        mainPanel.add(customerNameField);


        // Phone Number
        mainPanel.add(new JLabel("Phone Number:"));
        phoneNumberField = new JTextField();
        phoneNumberField.setBackground(new Color(135, 206, 250));
        mainPanel.add(phoneNumberField);

        // Email Address
        mainPanel.add(new JLabel("Email Address:"));
        emailField = new JTextField();
        emailField.setBackground(new Color(135, 206, 250));
        mainPanel.add(emailField);

        // Delivery Address
        mainPanel.add(new JLabel("Delivery Address:"));
        deliveryAddressField = new JTextField();
        deliveryAddressField.setBackground(new Color(135, 206, 250));
        mainPanel.add(deliveryAddressField);

        // Quantity
        mainPanel.add(new JLabel("Quantity:"));
        quantityField = new JTextField();
        quantityField.setBackground(new Color(135, 206, 250));
        mainPanel.add(quantityField);

        // Menu Items
        mainPanel.add(new JLabel("Menu Items:"));
        menuItemsMap = new HashMap<>();
        addMenuItem(mainPanel, "Tea");
        addMenuItem(mainPanel, "Coffee");
        addMenuItem(mainPanel, "Juice");
        addMenuItem(mainPanel, "Milkshake");
        addMenuItem(mainPanel, "Soda");
        addMenuItem(mainPanel, "Pizza");
        addMenuItem(mainPanel, "Burger");
        addMenuItem(mainPanel, "Pasta");
        addMenuItem(mainPanel, "Salad");

         // Set blue background color for the checkboxes
        for (JCheckBox checkBox : menuItemsMap.values()) {
        checkBox.setBackground(Color.ORANGE);
    }
        

        // Submit Button
        JButton submitButton = new JButton("Submit Order");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitOrder();
            
            }
        });
        mainPanel.add(submitButton);

         // Clear Button
         JButton clearButton = new JButton("Clear");
         clearButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 clearFields();
             }
         });
         mainPanel.add(clearButton);

         
        // Admin Login Panel
        JPanel adminLoginPanel = new JPanel();
        adminLoginPanel.setLayout(new GridLayout(1, 10, 10, 10));

        // Set sky blue background color for the Admin Login Panel
        adminLoginPanel.setBackground(new Color(135, 206, 250));

        adminLoginPanel.add(new JLabel("Admin Login:"));
        adminUsernameField = new JTextField();
        adminLoginPanel.add(adminUsernameField);

        adminPasswordField = new JPasswordField();
        adminLoginPanel.add(adminPasswordField);

        adminLoginButton = new JButton("Admin Login");
        adminLoginButton.addActionListener(e -> adminLogin());
        adminLoginPanel.add(adminLoginButton);

        mainPanel.add(adminLoginPanel);

        add(mainPanel);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void addMenuItem(JPanel panel, String itemName) {
        JCheckBox checkBox = new JCheckBox(itemName);
        menuItemsMap.put(itemName, checkBox);
        panel.add(checkBox);
    }

    private void submitOrder() {
     
        // Retrieve user inputs and process the order
        String customerName = customerNameField.getText();
        String phoneNumber = phoneNumberField.getText();
        String email = emailField.getText();
        String deliveryAddress = deliveryAddressField.getText();
     
         // Validate non-empty fields
         if (phoneNumber.isEmpty() || email.isEmpty() || deliveryAddress.isEmpty()) {
            JOptionPane.showMessageDialog(this,"Customer name, Phone number, email, and delivery address cannot be empty.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

         // Validate customer name (no special characters or spaces)
         if (!customerName.matches("^[a-zA-Z]+$")) {
            JOptionPane.showMessageDialog(this, "Customer name can only contain letters.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }


        // Validate phone number (only digits)
        if (!phoneNumber.matches("^\\d+$")) {
            JOptionPane.showMessageDialog(this, "Phone number can only contain digits.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

       // Validate quantity as integer
       try {
        int quantity = Integer.parseInt(quantityField.getText());
        // Check if the quantity is a non-negative integer
        if (quantity < 0) {
            throw new NumberFormatException();
        }

        saveToDatabase(customerName, phoneNumber, email, deliveryAddress, quantity);             


        // Check which menu items are selected
        StringBuilder selectedItems = new StringBuilder("Selected items: ");
        for (Map.Entry<String, JCheckBox> entry : menuItemsMap.entrySet()) {
            String itemName = entry.getKey();
            JCheckBox checkBox = entry.getValue();

            if (checkBox.isSelected()) {
                int itemQuantity = getItemQuantity(itemName);
                selectedItems.append(String.format("%s: %d\n", itemName, itemQuantity));
            }
        }
        // Display order details
        String orderDetails = String.format(
                "Customer Name: %s\nPhone Number: %s\nEmail: %s\nDelivery Address: %s\nQuantity: %d\n%s",
               customerName, phoneNumber, email, deliveryAddress, quantity, selectedItems.toString()
        );

        JOptionPane.showMessageDialog(this, orderDetails, "Order Details", JOptionPane.INFORMATION_MESSAGE);
    }catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, "Quantity must be a valid non-negative integer.", "Input Error",
                JOptionPane.ERROR_MESSAGE);
    }
}

    private void saveToDatabase(String customerName, String phoneNumber, String email, String deliveryAddress, int quantity) {
        try {
            // Establish database connection
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/ordermanagementsystem", "root", "abc123");

            // Prepare SQL statement for customer details
            String customerSql = "INSERT INTO orders (customer_name, phone_number, email, delivery_address, quantity, menu_items) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement customerStatement = connection.prepareStatement(customerSql)) {
                customerStatement.setString(1, customerName);
                customerStatement.setString(2, phoneNumber);
                customerStatement.setString(3, email);
                customerStatement.setString(4, deliveryAddress);
                customerStatement.setInt(5, quantity);

                // Prepare a string for menu items
                StringBuilder selectedItems = new StringBuilder();
                for (Map.Entry<String, JCheckBox> entry : menuItemsMap.entrySet()) {
                    String itemName = entry.getKey();
                    JCheckBox checkBox = entry.getValue();
                
                    if (checkBox.isSelected()) {
                        selectedItems.append(itemName).append(" ");
                    }
                }
                customerStatement.setString(6, selectedItems.toString());

                customerStatement.executeUpdate();
            }

            // Close the database connection
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to save data to the database.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getItemQuantity(String itemName) {
        String quantityString = JOptionPane.showInputDialog(
                this,
                String.format("Enter quantity for %s:", itemName),
                "Quantity Input",
                JOptionPane.PLAIN_MESSAGE
        );

        try {
            return Integer.parseInt(quantityString);
        } catch (NumberFormatException e) {
            return 0; // If parsing fails or user cancels input
        }
    }

private void clearFields() {
    customerNameField.setText("");
    phoneNumberField.setText("");
    emailField.setText("");
    deliveryAddressField.setText("");
    quantityField.setText("");
    for (JCheckBox checkBox : menuItemsMap.values()) {
        checkBox.setSelected(false);
    }
   
}

private void adminLogin() {
    // Hardcoded admin credentials (replace with database-based authentication)
    String adminUsername = "admin";
    String adminPassword = "admin123";

    String enteredUsername = adminUsernameField.getText();
    String enteredPassword = new String(adminPasswordField.getPassword());

    if (enteredUsername.equals(adminUsername) && enteredPassword.equals(adminPassword)) {
        isAdminLoggedIn = true;
        adminLoginButton.setEnabled(false); // Disable login button after successful login
        JOptionPane.showMessageDialog(this, "Admin login successful.", "Admin Login", JOptionPane.INFORMATION_MESSAGE);
        openAdminWindow();
    } else {
        JOptionPane.showMessageDialog(this, "Invalid admin credentials.", "Login Failed", JOptionPane.ERROR_MESSAGE);
    }
}

private void openAdminWindow() {
    AdminWindow adminWindow = new AdminWindow();
    adminWindow.setVisible(true);
}

private class AdminWindow extends JFrame {

    private JTextArea orderTextArea;
    private JTextField orderIdField;
    private JButton viewOrdersButton;
    private JButton deleteOrderButton;

    public AdminWindow() {
        initializeAdminUI();
    }

    private void initializeAdminUI() {
        setTitle("Admin Panel");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel adminPanel = new JPanel();
        adminPanel.setLayout(new GridLayout(0, 1, 5, 5));

        // Set sky blue background color for the Admin Window frame
        adminPanel.setBackground(new Color(135, 206, 250));

        orderTextArea = new JTextArea();
        orderTextArea.setEditable(false);
        orderTextArea.setBackground(new Color(135, 206, 250));
        JScrollPane scrollPane = new JScrollPane(orderTextArea);
        adminPanel.add(scrollPane);

        // New components for deleting specific order
        orderIdField = new JTextField();
        orderIdField.setBackground(new Color(135, 206, 250));
        adminPanel.add(new JLabel("Enter Order ID to Delete:"));
        adminPanel.add(orderIdField);

        deleteOrderButton = new JButton("Delete Order");
        deleteOrderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteOrders();
            }
        });
        adminPanel.add(deleteOrderButton);

        viewOrdersButton = new JButton("View Orders");

        viewOrdersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewOrders();
            }
        });
        adminPanel.add(viewOrdersButton);



        add(adminPanel);
        setLocationRelativeTo(null);
    }

    private void viewOrders() {
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/ordermanagementsystem", "root", "abc123");

            String query = "SELECT * FROM orders";
            try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery(query);

                StringBuilder orders = new StringBuilder();
                while (resultSet.next()) {
                    int orderId = resultSet.getInt("order_id");
                    String customerName = resultSet.getString("customer_name");
                    String phoneNumber = resultSet.getString("phone_number");
                    String email = resultSet.getString("email");
                    String deliveryAddress = resultSet.getString("delivery_address");
                    int quantity = resultSet.getInt("quantity");
                    String menuItems = resultSet.getString("menu_items");

                    orders.append(String.format("Order ID: %d\n", orderId));
                    orders.append(String.format("Customer Name: %s\n", customerName));
                    orders.append(String.format("Phone Number: %s\n", phoneNumber));
                    orders.append(String.format("Email: %s\n", email));
                    orders.append(String.format("Delivery Address: %s\n", deliveryAddress));
                    orders.append(String.format("Quantity: %d\n", quantity));
                    orders.append(String.format("Menu Items: %s\n", menuItems));
                    orders.append("-----------------------\n");
                }

                orderTextArea.setText(orders.toString());
            }

            connection.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to fetch orders from the database.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteOrders() {
        try {
            int orderId = Integer.parseInt(orderIdField.getText());
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/ordermanagementsystem", "root", "abc123");

            String deleteQuery = "DELETE FROM orders WHERE order_id = ?";
            try (PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery)) {
                deleteStatement.setInt(1, orderId);
                int rowsAffected = deleteStatement.executeUpdate();

                if (rowsAffected > 0) {
                    orderTextArea.setText("Order deleted successfully.");
                } else {
                    orderTextArea.setText("Order not found with the specified ID.");
                }
            }

            connection.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to delete order from the database.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}



    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new OrderManagementSystem());
    }
}

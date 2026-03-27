/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CashierInternal;

import InternalPage.manageUsers;
import config.DBConnection;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import util.Session;
import main.ImagePanel;
import main.loginPage;
import main.cashierDash;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Fatima
 */
public class manageSales extends javax.swing.JFrame {

    TableRowSorter<DefaultTableModel> sorter;
    
    DefaultTableModel productsModel;
    DefaultTableModel cartModel;
    private JLabel totalLabel; // Added total label


    public manageSales() {
        initComponents();

        // Background setup
        ImagePanel background = new ImagePanel("/images/background7.png");
        setContentPane(background);
        background.setLayout(new BorderLayout());
        background.add(jPanel1, BorderLayout.CENTER);
        jPanel1.setOpaque(false);

        // Glass effect panel
        jPanel2.setBackground(new Color(255, 255, 255, 30));
        jPanel2.setOpaque(true);

        // Products table header
        JTableHeader header = products1.getTableHeader();
        header.setForeground(Color.BLUE);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 16f));

        // Cart table header
        JTableHeader cartHeader = cartTable.getTableHeader();
        cartHeader.setForeground(Color.BLUE);
        cartHeader.setFont(cartHeader.getFont().deriveFont(Font.BOLD, 16f));

        loadProducts();
        setupSearch();
        initActions();

        pack();
        setLocationRelativeTo(null);
    }

     // Load products from DB
    private void loadProducts() {
    // Create table model
    productsModel = new DefaultTableModel(new String[]{"Product", "Price", "Stock"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // para dili editable
        }
    };

    try (Connection conn = DBConnection.connectDB()) {
        String sql = "SELECT name, price, stock FROM tbl_products";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
            int stock = rs.getInt("stock");

            if (stock == 0) continue; // ❌ skip rows with zero stock

            String name = rs.getString("name");
            double price = rs.getDouble("price");

            productsModel.addRow(new Object[]{name, price, stock});
        }

        products1.setModel(productsModel);

        // 👉 FONT SETTINGS
        products1.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        products1.setRowHeight(25);
        products1.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 18));

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Failed to load products: " + e.getMessage());
    }

    // 👉 SORTER (SEARCH SUPPORT)
    sorter = new TableRowSorter<>(productsModel);
    products1.setRowSorter(sorter);

    // 👉 COLOR RENDERER (OPTIONAL, stock = 0 won't exist now)
    products1.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {

            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                c.setForeground(Color.BLACK);
            } else {
                c.setForeground(Color.WHITE); // readable when selected
            }

            return c;
        }
    });
}
    // Setup live search
    private void setupSearch() {
        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }

            private void filter() {
                String text = search.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 0)); // search by Product Name only
                }
            }
        });
    }

    // Initialize actions for buttons, tables, and payment
    private void initActions() {
        cartModel = new DefaultTableModel(new String[]{"Product", "Price", "Quantity", "Subtotal"}, 0);
        cartTable.setModel(cartModel);
        cartTable.setFont(new Font("Segoe UI", Font.BOLD, 16));
        cartTable.setRowHeight(25);
        cartTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 18));

        // Click product to fill entry fields
        products1.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                int selectedRow = products1.getSelectedRow();
                if (selectedRow >= 0) {
                    int modelRow = products1.convertRowIndexToModel(selectedRow); // important with sorter
                    pname.setText(productsModel.getValueAt(modelRow, 0).toString());
                    pprice.setText(productsModel.getValueAt(modelRow, 1).toString());
                    pquant.setText("");
                }
            }
        });

        add.addActionListener(e -> addToCart());

        clear.addActionListener(e -> {
            cartModel.setRowCount(0);
            totalSales.setText("0.0");
            payAmount.setText("");
            changeAmount.setText("0.0");
        });

        paymentType.setModel(new DefaultComboBoxModel<>(new String[]{"Cash", "Credit Card"}));

        payAmount.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { calculateChange(); }
        });

        jButton1.addActionListener(e -> processTransaction());
    }

    // Add product to cart
    private void addToCart() {
        String name = pname.getText();
    String priceText = pprice.getText();
    String quantityText = pquant.getText();

    if (name.isEmpty() || priceText.isEmpty() || quantityText.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please select a product and enter quantity.");
        return;
    }

    try {

        double price = Double.parseDouble(priceText);
        int quantity = Integer.parseInt(quantityText);

        int availableStock = 0;

        for (int i = 0; i < productsModel.getRowCount(); i++) {

            String productName = productsModel.getValueAt(i, 0).toString();

            if (productName.equals(name)) {

                availableStock = Integer.parseInt(productsModel.getValueAt(i, 2).toString());
                break;

            }
        }

        if (quantity > availableStock) {

            JOptionPane.showMessageDialog(this,
                    "Not enough stock!\nAvailable stock: " + availableStock);

            return;

        }

        boolean found = false;

        for (int i = 0; i < cartModel.getRowCount(); i++) {

            String cartName = cartModel.getValueAt(i, 0).toString();

            if (cartName.equals(name)) {

                int existingQty = Integer.parseInt(cartModel.getValueAt(i, 2).toString());

                int newQty = existingQty + quantity;

                if (newQty > availableStock) {

                    JOptionPane.showMessageDialog(this,
                            "Not enough stock!\nAvailable stock: " + availableStock);

                    return;

                }

                double subtotal = price * newQty;

                cartModel.setValueAt(newQty, i, 2);
                cartModel.setValueAt(subtotal, i, 3);

                found = true;

                break;

            }

        }

        if (!found) {

            double subtotal = price * quantity;

            cartModel.addRow(new Object[]{name, price, quantity, subtotal});

        }

        updateTotal();

        pname.setText("");
        pprice.setText("");
        pquant.setText("");

    } catch (NumberFormatException ex) {

        JOptionPane.showMessageDialog(this, "Invalid number format.");

    }

}

    private void updateTotal() {
        double total = 0;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            total += Double.parseDouble(cartModel.getValueAt(i, 3).toString());
        }
        totalSales.setText(String.format("%.2f", total));
        calculateChange();
    }

    private void calculateChange() {
        try {
            double total = Double.parseDouble(totalSales.getText());
            double paid = Double.parseDouble(payAmount.getText());
            double change = paid - total;
            changeAmount.setText(String.format("%.2f", change >= 0 ? change : 0.0));
        } catch (NumberFormatException e) {
            changeAmount.setText("0.0");
        }
    }

    // Transaction processing & receipt
    private void processTransaction() {
        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Cart is empty!");
            return;
        }

        double totalCalc = 0;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            totalCalc += Double.parseDouble(cartModel.getValueAt(i, 3).toString());
        }

        String payment = paymentType.getSelectedItem().toString();
        double pay = 0;

        if (payment.equals("Cash")) {
            try { pay = Double.parseDouble(payAmount.getText()); }
            catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "Enter valid payment amount."); return; }
            if (pay < totalCalc) { JOptionPane.showMessageDialog(this, "Insufficient payment!Amount entered is below total."); return; }
        } else pay = totalCalc;

        double change = payment.equals("Cash") ? pay - totalCalc : 0;

        // Build receipt
        StringBuilder receipt = new StringBuilder();
        receipt.append("   ==== MERIDA'S STORE ====   \n");
        receipt.append("   123 Main Street\n");
        receipt.append("   Tel: 09123456789\n");
        receipt.append("-----------------------\n");
        receipt.append(String.format("%-12s %3s %7s\n", "Item", "Qty", "Total"));
        receipt.append("-----------------------\n");

        for (int i = 0; i < cartModel.getRowCount(); i++) {
            String name = cartModel.getValueAt(i, 0).toString();
            int qty = Integer.parseInt(cartModel.getValueAt(i, 2).toString());
            double subtotal = Double.parseDouble(cartModel.getValueAt(i, 3).toString());
            if (name.length() > 12) name = name.substring(0, 12);
            receipt.append(String.format("%-12s %3d %7.2f\n", name, qty, subtotal));
        }

        receipt.append("-----------------------\n");
        receipt.append(String.format("%-12s %10.2f\n", "TOTAL:", totalCalc));

        if (payment.equals("Cash")) {
            receipt.append(String.format("%-12s %10.2f\n", "PAY:", pay));
            receipt.append(String.format("%-12s %10.2f\n", "CHANGE:", change));
        } else {
            receipt.append("PAY: CREDIT CARD\nCard Type: VISA\nLast 4: 1234\nApproval: 567890\n");
        }
        receipt.append("Payment Type: ").append(payment).append("\n");
        receipt.append("-----------------------\n");
        receipt.append("  THANK YOU! COME AGAIN  \n");
        
        try {

    String folderPath = "C:/Users/Fatima/OneDrive/Documents/NetBeansProjects/SmartStock/receipts";

    File folder = new File(folderPath);

    if(!folder.exists()){
        folder.mkdirs();
    }

    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
    String fileName = "receipt_" + sdf.format(new java.util.Date()) + ".txt";

    File file = new File(folder, fileName);

    FileWriter writer = new FileWriter(file);
    writer.write(receipt.toString());
    writer.close();

    System.out.println("Receipt saved to: " + file.getAbsolutePath());

} catch(IOException e){
    JOptionPane.showMessageDialog(this, "Failed to save receipt: " + e.getMessage());
}

        // Save to DB and update stock
        try (Connection conn = DBConnection.connectDB()) {

        StringBuilder itemsList = new StringBuilder();

        for (int i = 0; i < cartModel.getRowCount(); i++) {

        String name = cartModel.getValueAt(i, 0).toString();
        double price = Double.parseDouble(cartModel.getValueAt(i, 1).toString());
        int qty = Integer.parseInt(cartModel.getValueAt(i, 2).toString());
        double subtotal = Double.parseDouble(cartModel.getValueAt(i, 3).toString());

        itemsList.append(String.format("%s x%d @ %.2f = %.2f\n", name, qty, price, subtotal));

        // UPDATE STOCK
        String updateStock = "UPDATE tbl_products SET stock = stock - ? WHERE name = ?";
        PreparedStatement pstStock = conn.prepareStatement(updateStock);
        pstStock.setInt(1, qty);
        pstStock.setString(2, name);
        pstStock.executeUpdate();
        }

         String sql = "INSERT INTO tbl_sales (items, subtotal, payment_type, pay_amount, change_amount, date) VALUES (?, ?, ?, ?, ?, datetime('now','localtime'))";

        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, itemsList.toString());
        pst.setDouble(2, totalCalc);
        pst.setString(3, payment);
        pst.setDouble(4, pay);
        pst.setDouble(5, change);
        pst.executeUpdate();

        } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Failed to save transaction: " + ex.getMessage());
        return;
        }

        // Show receipt
        JTextArea ta = new JTextArea(receipt.toString());
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ta.setEditable(false);
        JOptionPane.showMessageDialog(this, ta, "Receipt", JOptionPane.INFORMATION_MESSAGE);

        cartModel.setRowCount(0);
        totalSales.setText("0.0");
        payAmount.setText("");
        changeAmount.setText("0.0");
        pname.setText("");
        pprice.setText("");
        pquant.setText("");
    }




    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        products1 = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        pname = new javax.swing.JTextField();
        pprice = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        pquant = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        cartTable = new javax.swing.JTable();
        add = new javax.swing.JButton();
        clear = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        paymentType = new javax.swing.JComboBox<>();
        payAmount = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        totalSales = new javax.swing.JTextField();
        changeAmount = new javax.swing.JTextField();
        search = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        dashboard = new javax.swing.JButton();
        orders = new javax.swing.JButton();
        products = new javax.swing.JButton();
        sales = new javax.swing.JButton();
        settings = new javax.swing.JButton();
        logout = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(0, 204, 204));

        jPanel2.setBackground(new java.awt.Color(204, 204, 204));

        jPanel3.setBackground(new java.awt.Color(153, 0, 153));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Sales Transaction");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(301, 301, 301)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(22, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addContainerGap())
        );

        products1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(products1);

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Product Entry");

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Product Name:");

        pname.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N

        pprice.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Price:");

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Quantity:");

        pquant.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Cart");

        cartTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(cartTable);

        add.setBackground(new java.awt.Color(0, 153, 51));
        add.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        add.setForeground(new java.awt.Color(255, 255, 255));
        add.setText("Add to Cart");

        clear.setBackground(new java.awt.Color(204, 0, 0));
        clear.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        clear.setForeground(new java.awt.Color(255, 255, 255));
        clear.setText("Clear Cart");

        jButton1.setBackground(new java.awt.Color(51, 204, 255));
        jButton1.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/printer.png"))); // NOI18N
        jButton1.setText("Print Receipt");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Payment Type:");

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("Pay Amount:");

        paymentType.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        paymentType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Cash", "Credit Card" }));
        paymentType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paymentTypeActionPerformed(evt);
            }
        });

        payAmount.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("Total Sale:");

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 255, 255));
        jLabel12.setText("Change:");

        totalSales.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        totalSales.setForeground(new java.awt.Color(255, 0, 51));

        changeAmount.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        changeAmount.setForeground(new java.awt.Color(0, 153, 0));

        search.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        search.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/inspection.png"))); // NOI18N
        jLabel2.setText("Search Products");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(add)
                                .addGap(18, 18, 18)
                                .addComponent(clear, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 272, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel9)
                            .addComponent(jLabel10))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(paymentType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(payAmount, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel11)
                            .addComponent(jLabel12))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(totalSales, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(changeAmount, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 361, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel2)
                                        .addComponent(search, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGap(36, 36, 36)
                                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(jLabel5)
                                                .addComponent(jLabel6)
                                                .addComponent(jLabel7))
                                            .addGap(18, 18, 18)
                                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(pprice, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(pname, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(pquant, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addComponent(jLabel4))
                                    .addGap(151, 151, 151))
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 790, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addGap(12, 12, 12)))))
                .addGap(43, 43, 43))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel4))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(search, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 19, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(pname, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pprice, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(pquant, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(18, 18, 18)
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(add, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(clear, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGap(54, 54, 54)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel12)
                                            .addComponent(changeAmount, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(totalSales, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel11)))
                                .addGap(1, 1, 1))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel9)
                                    .addComponent(paymentType, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(20, 20, 20)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel10)
                                    .addComponent(payAmount, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addContainerGap(50, Short.MAX_VALUE))
        );

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/user (3).png"))); // NOI18N
        jLabel3.setText("Cashier Dashboard");

        dashboard.setBackground(new java.awt.Color(153, 153, 153));
        dashboard.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        dashboard.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/home.png"))); // NOI18N
        dashboard.setText("Dashboard");
        dashboard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dashboardActionPerformed(evt);
            }
        });

        orders.setBackground(new java.awt.Color(153, 153, 153));
        orders.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        orders.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/shopping-cart (1).png"))); // NOI18N
        orders.setText("My Orders");
        orders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ordersActionPerformed(evt);
            }
        });

        products.setBackground(new java.awt.Color(153, 153, 153));
        products.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        products.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/product.png"))); // NOI18N
        products.setText("View Products");
        products.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                productsActionPerformed(evt);
            }
        });

        sales.setBackground(new java.awt.Color(153, 153, 153));
        sales.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        sales.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/transactional-data.png"))); // NOI18N
        sales.setText("Manage Sales");
        sales.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                salesActionPerformed(evt);
            }
        });

        settings.setBackground(new java.awt.Color(153, 153, 153));
        settings.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        settings.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/settings.png"))); // NOI18N
        settings.setText("Settings");
        settings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsActionPerformed(evt);
            }
        });

        logout.setBackground(new java.awt.Color(153, 153, 153));
        logout.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        logout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/power-off.png"))); // NOI18N
        logout.setText("Logout");
        logout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logoutActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(products, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
                    .addComponent(orders, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sales, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(dashboard, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(logout, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(settings, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 839, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addComponent(jLabel3)
                .addGap(31, 31, 31)
                .addComponent(dashboard, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(sales, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(orders, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(products, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(settings, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(logout, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void dashboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dashboardActionPerformed
        cashierDash ut = new cashierDash();
        ut.setVisible(true);
        this.dispose();        // TODO add your handling code here:
    }//GEN-LAST:event_dashboardActionPerformed

    private void ordersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ordersActionPerformed
        orders ut = new orders();
        ut.setVisible(true);
        this.dispose();              // TODO add your handling code here:
    }//GEN-LAST:event_ordersActionPerformed

    private void productsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_productsActionPerformed
        viewProducts ut = new viewProducts();
        ut.setVisible(true);
        this.dispose();        // TODO add your handling code here:
    }//GEN-LAST:event_productsActionPerformed

    private void salesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_salesActionPerformed
        manageSales ut = new manageSales();
        ut.setVisible(true);
        this.dispose();              // TODO add your handling code here:
    }//GEN-LAST:event_salesActionPerformed

    private void settingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsActionPerformed
        settings ut = new settings();
        ut.setVisible(true);
        this.dispose();                      // TODO add your handling code here:
    }//GEN-LAST:event_settingsActionPerformed

    private void logoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoutActionPerformed
      int choice = JOptionPane.showConfirmDialog(
        this,
        "Are you sure you want to logout?",
        "Confirm Logout",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE
    );

    if (choice == JOptionPane.YES_OPTION) {
        // clear session
        Session.id = 0;
        Session.first_name = null;
        Session.email = null;
        Session.status = null;

        // go back to login page
        loginPage login = new loginPage();
        login.setVisible(true);

        this.dispose(); // close current frame
    }
            // TODO add your handling code here:
    }//GEN-LAST:event_logoutActionPerformed

    private void paymentTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paymentTypeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_paymentTypeActionPerformed

    private void searchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchActionPerformed
        

    }//GEN-LAST:event_searchActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(manageSales.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(manageSales.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(manageSales.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(manageSales.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new manageSales().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton add;
    private javax.swing.JTable cartTable;
    private javax.swing.JTextField changeAmount;
    private javax.swing.JButton clear;
    private javax.swing.JButton dashboard;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton logout;
    private javax.swing.JButton orders;
    private javax.swing.JTextField payAmount;
    private javax.swing.JComboBox<String> paymentType;
    private javax.swing.JTextField pname;
    private javax.swing.JTextField pprice;
    private javax.swing.JTextField pquant;
    private javax.swing.JButton products;
    private javax.swing.JTable products1;
    private javax.swing.JButton sales;
    private javax.swing.JTextField search;
    private javax.swing.JButton settings;
    private javax.swing.JTextField totalSales;
    // End of variables declaration//GEN-END:variables
}

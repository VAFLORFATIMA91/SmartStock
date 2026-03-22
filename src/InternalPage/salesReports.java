


package InternalPage;

import main.adminDash;
import InternalPage.manageUsers;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import config.config;
import main.loginPage;
import util.Session;
import main.ImagePanel;

import java.awt.Desktop;
import java.io.File;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class salesReports extends javax.swing.JFrame {

  

    public salesReports() {
        initComponents();

        ImagePanel background = new ImagePanel("/images/background.png");
        setContentPane(background);
        background.setLayout(new BorderLayout());

        background.add(jPanel1, BorderLayout.CENTER);
        jPanel1.setOpaque(false);

        // glass effect
        jPanel2.setBackground(new Color(255,255,255,30));
        jPanel2.setOpaque(true);

        setupTable();

        loadTodaySales(); // auto load today's sales

        reprint.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                openReceiptFolder();
            }
        });

        setLocationRelativeTo(null);
    }

   // =========================
    // TABLE SETUP + CLICK LISTENER
    // =========================
    private void setupTable() {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Date", "Sales ID", "Amount", "Payment Type"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // all cells non-editable
            }
        };
        salesReports.setModel(model);

        // Table styling
        salesReports.setFont(new Font("Tahoma", Font.PLAIN, 16));
        salesReports.setRowHeight(35);
        salesReports.getTableHeader().setBackground(new Color(0, 102, 204));
        salesReports.getTableHeader().setForeground(Color.BLUE);
        salesReports.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 14));

        // Custom renderer for Payment Type
        salesReports.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                Object paymentType = table.getValueAt(row, 3);
                if (paymentType != null && paymentType.toString().equalsIgnoreCase("Credit Card")) {
                    c.setForeground(Color.RED);
                } else {
                    c.setForeground(Color.BLACK);
                }
                c.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                return c;
            }
        });

        // Row click listener
        salesReports.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                int row = salesReports.rowAtPoint(evt.getPoint());
                if (row >= 0) {
                    try {
                        int salesId = Integer.parseInt(salesReports.getValueAt(row, 1).toString());
                        showSaleDetails(salesId);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Invalid Sales ID: " + ex.getMessage());
                    }
                }
            }
        });
    }

    // =========================
    // SHOW SALE DETAILS
    // =========================
    private void showSaleDetails(int salesId) {
    try {
        config con = new config();
        ResultSet rs = con.select(
            "SELECT items, price, quantity, subtotal, pay_amount, payment_type FROM tbl_sales WHERE s_id = " + salesId
        );

        StringBuilder details = new StringBuilder();

        while (rs.next()) {

            String paymentType = rs.getString("payment_type");

            details.append("============================\n")
                   .append("       ITEMS WERE SOLD      \n")
                   .append("============================\n")
                   .append("Item:\n")
                   .append(rs.getString("items"))
                   .append("----------------------------\n")
                   .append("\nSubtotal: ").append(rs.getDouble("subtotal"));

            // ✅ Only show Pay Amount if NOT Credit Card
            if (!paymentType.equalsIgnoreCase("Credit Card")) {
                details.append("\nPay Amount: ").append(rs.getDouble("pay_amount"));
            }

            details.append("\nPayment Type: ").append(paymentType)
                   .append("\n--------------------------\n");
        }

        JTextArea textArea = new JTextArea(details.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 16));
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        JOptionPane.showMessageDialog(this, scrollPane,
                "Sales Details", JOptionPane.INFORMATION_MESSAGE);

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error fetching sale details: " + e.getMessage());
    }
}

   
    
    
    // =========================
    // AUTO LOAD TODAY SALES
    // =========================
    private void loadTodaySales(){

    DefaultTableModel model = (DefaultTableModel) salesReports.getModel();
    model.setRowCount(0);

    double total = 0;

    try{

        config con = new config();

        ResultSet rs = con.select(
        "SELECT s_id, date, SUM(subtotal) AS total_sales, payment_type " +
        "FROM tbl_sales WHERE date = date('now') GROUP BY s_id");

        while(rs.next()){

            double subtotal = rs.getDouble("total_sales");
            total += subtotal;

            model.addRow(new Object[]{
                rs.getString("date"),
                rs.getInt("s_id"),
                subtotal,
                rs.getString("payment_type")
            });
        }

        totalSales.setText(String.valueOf(total));

    }catch(Exception e){

        JOptionPane.showMessageDialog(null,"Error loading sales: "+e.getMessage());
    }
}

    // =========================
    // DISPLAY DATA BY DATE
    // =========================
    private void displayData(java.util.Date startDate, java.util.Date endDate){

        DefaultTableModel model = (DefaultTableModel) salesReports.getModel();
        model.setRowCount(0);

        double total = 0;

        try{

            config con = new config();

            java.text.SimpleDateFormat sdf =
                    new java.text.SimpleDateFormat("yyyy-MM-dd");

            String start = sdf.format(startDate);
            String end = sdf.format(endDate);

            ResultSet rs = con.select(
            "SELECT s_id,date,subtotal,payment_type FROM tbl_sales "+
            "WHERE DATE(date) BETWEEN '"+start+"' AND '"+end+"'");

            while(rs.next()){

                double subtotal = rs.getDouble("subtotal");
                total += subtotal;

                model.addRow(new Object[]{
                    rs.getString("date"),
                    rs.getInt("s_id"),
                    subtotal,
                    rs.getString("payment_type")
                });
            }

            totalSales.setText(String.valueOf(total));

        }catch(Exception e){

            JOptionPane.showMessageDialog(this,
            "Error loading data: "+e.getMessage());
        }
    }

    // =========================
    // OPEN RECEIPT FOLDER
    // =========================
    public void openReceiptFolder(){

        try{

            File folder = new File(
            "C:/Users/Fatima/OneDrive/Documents/NetBeansProjects/SmartStock/receipts");

            if(folder.exists()){

                Desktop.getDesktop().open(folder);

            }else{

                JOptionPane.showMessageDialog(null,"Receipt folder not found!");
            }

        }catch(Exception e){

            JOptionPane.showMessageDialog(null,
            "Error opening folder: "+e.getMessage());
        }
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
        salesReports = new javax.swing.JTable();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jDateChooser2 = new com.toedter.calendar.JDateChooser();
        jLabel5 = new javax.swing.JLabel();
        totalSales = new javax.swing.JLabel();
        generateReport = new javax.swing.JButton();
        reprint = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        dashboard = new javax.swing.JButton();
        manage = new javax.swing.JButton();
        products = new javax.swing.JButton();
        sales = new javax.swing.JButton();
        settings = new javax.swing.JButton();
        logout = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(0, 204, 204));

        jPanel3.setBackground(new java.awt.Color(0, 153, 204));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Sales Reports");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(20, 20, 20)
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

        salesReports.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(salesReports);

        jDateChooser1.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Start Date:");

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("End Date:");

        jDateChooser2.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Total Sales:");

        totalSales.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        totalSales.setForeground(new java.awt.Color(51, 255, 0));

        generateReport.setBackground(new java.awt.Color(51, 204, 255));
        generateReport.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        generateReport.setText("Generate Report");
        generateReport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateReportActionPerformed(evt);
            }
        });

        reprint.setBackground(new java.awt.Color(51, 204, 255));
        reprint.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        reprint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/printer.png"))); // NOI18N
        reprint.setText("Reprint Receipt");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 850, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(50, 50, 50)
                                .addComponent(generateReport, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(41, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(reprint)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(totalSales, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(40, 40, 40))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(45, 45, 45)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jDateChooser1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel4)
                    .addComponent(jDateChooser2, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
                    .addComponent(generateReport, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(45, 45, 45)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(reprint)
                            .addComponent(jLabel5)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(totalSales, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(30, 30, 30))
        );

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/user (3).png"))); // NOI18N
        jLabel3.setText("Admin Dashboard");

        dashboard.setBackground(new java.awt.Color(153, 153, 153));
        dashboard.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        dashboard.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/home.png"))); // NOI18N
        dashboard.setText("Dashboard");
        dashboard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dashboardActionPerformed(evt);
            }
        });

        manage.setBackground(new java.awt.Color(153, 153, 153));
        manage.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        manage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/user (1).png"))); // NOI18N
        manage.setText("Manage Users");
        manage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageActionPerformed(evt);
            }
        });

        products.setBackground(new java.awt.Color(153, 153, 153));
        products.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        products.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/product.png"))); // NOI18N
        products.setText("Products");
        products.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                productsActionPerformed(evt);
            }
        });

        sales.setBackground(new java.awt.Color(153, 153, 153));
        sales.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        sales.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/sales_1.png"))); // NOI18N
        sales.setText("Sales Reports");
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
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(settings, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sales, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(products, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(manage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(dashboard, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
                    .addComponent(logout, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addComponent(jLabel3)
                .addGap(28, 28, 28)
                .addComponent(dashboard, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(manage, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(products, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(sales, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(settings, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 212, Short.MAX_VALUE)
                .addComponent(logout, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
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
        adminDash ut = new adminDash();
        ut.setVisible(true);
        this.dispose();         // TODO add your handling code here:
    }//GEN-LAST:event_dashboardActionPerformed

    private void manageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageActionPerformed
        manageUsers ut = new manageUsers();
        ut.setVisible(true);
        this.dispose();         // TODO add your handling code here:
    }//GEN-LAST:event_manageActionPerformed

    private void productsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_productsActionPerformed
        products ut = new products();
        ut.setVisible(true);
        this.dispose();// TODO add your handling code here:
    }//GEN-LAST:event_productsActionPerformed

    private void salesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_salesActionPerformed
        salesReports ut = new salesReports();
        ut.setVisible(true);
        this.dispose();        // TODO add your handling code here:
    }//GEN-LAST:event_salesActionPerformed

    private void settingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsActionPerformed
        settings ut = new settings();
        ut.setVisible(true);
        this.dispose();        // TODO add your handling code here:
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

            // balik sa login page
            loginPage login = new loginPage();
            login.setVisible(true);

            this.dispose(); // close user profile     // TODO add your handling code here:
        }            // TODO add your handling code here:
    }//GEN-LAST:event_logoutActionPerformed

    private void generateReportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateReportActionPerformed
         java.util.Date start = jDateChooser1.getDate();
        java.util.Date end = jDateChooser2.getDate();

        if (start == null || end == null) {
            JOptionPane.showMessageDialog(null, "Please select both start and end dates!");
            return;
        }

        if (start.after(end)) {
            JOptionPane.showMessageDialog(null, "Start date cannot be after end date!");
            return;
        }

        displayData(start, end); // load table with selected date range
    
        // TODO add your handling code here:
    }//GEN-LAST:event_generateReportActionPerformed

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
            java.util.logging.Logger.getLogger(salesReports.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(salesReports.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(salesReports.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(salesReports.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new salesReports().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton dashboard;
    private javax.swing.JButton generateReport;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private com.toedter.calendar.JDateChooser jDateChooser2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton logout;
    private javax.swing.JButton manage;
    private javax.swing.JButton products;
    private javax.swing.JButton reprint;
    private javax.swing.JButton sales;
    private javax.swing.JTable salesReports;
    private javax.swing.JButton settings;
    private javax.swing.JLabel totalSales;
    // End of variables declaration//GEN-END:variables
}

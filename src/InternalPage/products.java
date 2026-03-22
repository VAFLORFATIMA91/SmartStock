/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package InternalPage;

import config.config;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.sql.ResultSet;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import main.ImagePanel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import main.adminDash;
import java.awt.BorderLayout;
import InternalPage.BlurPanel;
import config.DBConnection;
import java.awt.Font;
import main.loginPage;
import util.Session;

public class products extends javax.swing.JFrame {

    /**
     * Creates new form products
     */
    public products() {
       initComponents();
       ImagePanel background = new ImagePanel("/images/background.png");
        setContentPane(background);
        background.setLayout(new BorderLayout());

        background.add(jPanel1, BorderLayout.CENTER);

        jPanel1.setOpaque(false);

        // GLASS EFFECT PANEL
        jPanel2.setBackground(new Color(255,255,255,30));
        jPanel2.setOpaque(true);

        setupTable();
        displayData();

    
        setLocationRelativeTo(null);
        setupTable();
        displayData();
    }
    // =========================
    // TABLE SETUP
    // =========================
    private void setupTable() {
        DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Name", "Category", "Price", "Stocks", "Date", "Actions"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 6;
        }
    };

    manageProducts.setModel(model);
    
    // TABLE STYLE
     manageProducts.setFont(new Font("Tahoma", Font.PLAIN, 16)); 
     manageProducts.setRowHeight(35);

    // HEADER STYLE
    manageProducts.getTableHeader().setBackground(new Color(0,102,204));
    manageProducts.getTableHeader().setForeground(Color.BLUE);
    manageProducts.getTableHeader().setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 14));

    // Hide ID column
    manageProducts.getColumnModel().getColumn(0).setMinWidth(0);
    manageProducts.getColumnModel().getColumn(0).setMaxWidth(0);
    manageProducts.getColumnModel().getColumn(0).setWidth(0);

    // Actions buttons
    manageProducts.getColumn("Actions").setCellRenderer(new ActionRenderer());
    manageProducts.getColumn("Actions").setCellEditor(new ActionEditor());

    manageProducts.setRowHeight(35);
}

    // =========================
    // LOAD DATA FROM DATABASE
    // =========================
    private void displayData() {
        DefaultTableModel model = (DefaultTableModel) manageProducts.getModel();
        model.setRowCount(0);

        try {
            config con = new config();
            ResultSet rs = con.select("SELECT p_id, name, category, price, stock, date FROM tbl_products");
 
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("p_id"),           // Hidden ID
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getString("price"),
                        rs.getString("stock"),
                        rs.getString("date"),
                        "Actions"
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }

    // =========================
    // ACTIONS RENDERER
    // =========================
    class ActionRenderer extends JPanel implements TableCellRenderer {
        JButton edit = new JButton("Edit");
        JButton delete = new JButton("Delete");

        public ActionRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
            edit.setBackground(new Color(66, 133, 244));
            edit.setForeground(Color.WHITE);
            edit.setFocusable(false);

            delete.setBackground(new Color(219, 68, 55));
            delete.setForeground(Color.WHITE);
            delete.setFocusable(false);

            add(edit);
            add(delete);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    // =========================
    // ACTIONS EDITOR
    // =========================
    class ActionEditor extends AbstractCellEditor implements TableCellEditor {

        JPanel panel = new JPanel();
        JButton edit = new JButton("Edit");
        JButton delete = new JButton("Delete");
        int selectedRow;

        public ActionEditor() {
            panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
            edit.setBackground(new Color(66, 133, 244)); edit.setForeground(Color.WHITE);
            delete.setBackground(new Color(219, 68, 55)); delete.setForeground(Color.WHITE);
            panel.add(edit); panel.add(delete);

            // =====================
            // EDIT BUTTON CLICK
            // =====================
            edit.addActionListener(e -> {
                int id = Integer.parseInt(manageProducts.getValueAt(selectedRow, 0).toString()); // Hidden ID
                editProduct ut = new editProduct(id);
                ut.setVisible(true); // open new window
                ut.setLocationRelativeTo(null); // Close manageUsers form
                fireEditingStopped();
            });

            // =====================
            // DELETE BUTTON CLICK
            // =====================
            delete.addActionListener(e -> {
                int id = Integer.parseInt(manageProducts.getValueAt(selectedRow, 0).toString());
                String name = manageProducts.getValueAt(selectedRow, 1).toString();

                int confirm = JOptionPane.showConfirmDialog(null,
                        "Are you sure you want to delete " + name + "?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        config con = new config();
                        con.update("DELETE FROM tbl_users WHERE id = " + id);
                        displayData();
                        JOptionPane.showMessageDialog(null, "User deleted successfully!");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Delete failed!");
                    }
                }

                fireEditingStopped();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            selectedRow = row;
            return panel;
        }

        @Override
        public Object getCellEditorValue() { return ""; }
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
        jButton1 = new javax.swing.JButton();
        search = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        manageProducts = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
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
        jLabel1.setText("Manage Product");

        jButton1.setBackground(new java.awt.Color(51, 255, 51));
        jButton1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/plus.png"))); // NOI18N
        jButton1.setText("Add Product");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addGap(25, 25, 25))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(22, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jLabel1))
                .addContainerGap())
        );

        search.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchActionPerformed(evt);
            }
        });

        manageProducts.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(manageProducts);

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/inspection.png"))); // NOI18N
        jLabel4.setText("Search Product");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 747, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(search, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(29, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(search, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 411, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(34, Short.MAX_VALUE))
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
                    .addComponent(manage, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
                    .addComponent(dashboard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(logout, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        addProduct ut = new addProduct();
        ut.setVisible(true);
        this.dispose();                        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void searchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchActionPerformed
        String keyword = search.getText().trim();

        DefaultTableModel model = (DefaultTableModel) manageProducts.getModel();
        model.setRowCount(0); // clear existing rows

        if (keyword.isEmpty()) {
            displayData(); // show all users if empty
            return;
        }

        try {
            Connection conn = DBConnection.connectDB();
            PreparedStatement pst = conn.prepareStatement(
                "SELECT * FROM tbl_products WHERE p_id LIKE ? OR name LIKE ? OR category LIKE ? "
            );
            pst.setString(1, "%" + keyword + "%");
            pst.setString(2, "%" + keyword + "%");
            pst.setString(3, "%" + keyword + "%");
            

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("p_id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getDouble("price"),
                    rs.getInt("stock"),
                    rs.getString("date")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());

        
    }                                      

    }//GEN-LAST:event_searchActionPerformed

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
        // TODO add your handling code here:
    }//GEN-LAST:event_productsActionPerformed

    private void salesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_salesActionPerformed
        salesReports ut = new salesReports();
        ut.setVisible(true);
        this.dispose();         // TODO add your handling code here:
    }//GEN-LAST:event_salesActionPerformed

    private void settingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsActionPerformed
        settings ut = new settings();
        ut.setVisible(true);
        ut.setLocationRelativeTo(null); // optional para i-center ang window
        this.dispose();      // TODO add your handling code here:
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
            java.util.logging.Logger.getLogger(products.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(products.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(products.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(products.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new products().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton dashboard;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton logout;
    private javax.swing.JButton manage;
    private javax.swing.JTable manageProducts;
    private javax.swing.JButton products;
    private javax.swing.JButton sales;
    private javax.swing.JTextField search;
    private javax.swing.JButton settings;
    // End of variables declaration//GEN-END:variables
}

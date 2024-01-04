import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;

import java.sql.*;


public class History extends JFrame {
	
    private Connection connection;
    int wins;
    int losses;
    public History(String username) {
        super("History");

        //try connect to database
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:javabook.db");

            JLabel label = PlayField.formatLabel("Game History", 18);

            //create query for wins and losses for user
            String wlquery = "SELECT wins, losses FROM User WHERE username = ?";
            PreparedStatement wlstatement = connection.prepareStatement(wlquery);
            wlstatement.setString(1, username);
            ResultSet wlrs = wlstatement.executeQuery();

            //check if user exists
            if (wlrs.next()) {
                //get wins and losses
                wins = wlrs.getInt("wins");
                losses = wlrs.getInt("losses");
            }
            else {
                wins = 0;
                losses = 0;
            }
            //create label for wins and losses
            JLabel wlLabel = PlayField.formatLabel("Users: " + username +  " Wins: " + wins + " Losses: " + losses, 14);

            //create query for games
            String query = "SELECT * FROM Game WHERE players LIKE ? OR players LIKE ? OR players LIKE ? OR players = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, "%," + username + ",%");
            statement.setString(2, username + ",%");
            statement.setString(3, "%," + username);
            statement.setString(4, username);
            ResultSet rs = statement.executeQuery();

            //get row count
            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
            }

            //reset result set
            rs = statement.executeQuery();

            //build table model
            DefaultTableModel model = buildTable(rs, rowCount);
            
            //create table
            JTable table = new JTable(model);

            //add label and table to frame
            add(label, BorderLayout.NORTH);
            add(wlLabel, BorderLayout.SOUTH);
            add(new JScrollPane(table), BorderLayout.CENTER);

            setSize(500, 200);
            setVisible(true);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            //disconnect when window closed
            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to database.");
            System.exit(1);
        }
    }
        
    public static DefaultTableModel buildTable(ResultSet rs, int rowCount) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();

        //get column names
        int colCount = metaData.getColumnCount();
        String[] columns = new String[colCount];
        for (int i = 1; i <= colCount; i++) {
            columns[i - 1] = metaData.getColumnName(i);
        }

        //define our data table
        Object[][] data = new Object[rowCount][colCount];
        int idx = 0;

        //fill data table
        while (rs.next()) {
            for (int i = 1; i <= colCount; i++) {
                data[idx][i - 1] = rs.getObject(i);
            }
            idx++;
        }

        //create table model
        return new DefaultTableModel(data, columns);
    }

}

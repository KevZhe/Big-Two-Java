import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;

public class UserLogin extends JFrame {
	//salt for hashing passwords
    private static final String PEPPER = "javafinalproject2023"; 
    
    private Connection connection;
    
    //text fields
    JTextField username;
    JPasswordField password;
    
    //callback for login success
    private Player.LoginCallback callback;
    
    public UserLogin(Player.LoginCallback callback) {
        super("Login");
        this.callback = callback;
        
        //try connect to database
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:javabook.db");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to database.");
            System.exit(0);
        }
		
        //layout for panels
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        
        //create panels/labels to add
		JLabel label = PlayField.formatLabel("Big Two Login", 18);
        JPanel buttons = createButtons();
        JPanel fields = createFields();
        label.setAlignmentX(CENTER_ALIGNMENT);
        buttons.setAlignmentX(CENTER_ALIGNMENT);
        
        //add to our frame
        this.add(label);
        this.add(fields);
        this.add(buttons);
        
        //pack and display
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        
    }

    //create panel for text fields
    private JPanel createFields() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        //create text fields
        username = new JTextField(20);
        password = new JPasswordField(20);
        //add labels and text fields to panel
        panel.add(new JLabel("Username:"));
        panel.add(username);
        panel.add(new JLabel("Password:"));
        panel.add(password);
        return panel;
    }

    //create panel for buttons
    private JPanel createButtons () {
        JPanel panel = new JPanel();
        JButton login = new JButton("Login");
        JButton signup = new JButton("Sign Up");
        
        //add action listeners
        login.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login(username.getText(), new String(password.getPassword()));
            }
        });

        signup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                signup(username.getText(), new String(password.getPassword()));
            }
        });

        panel.add(login);
        panel.add(signup);
        
        return panel;
    }

    //generate a random salt
    private byte[] generateSalt() {
    	SecureRandom random =  new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    //hash a password given a salt
    private String hashPassword(String password, byte[] salt) {
        try {
            //use SHA-512
            MessageDigest mdigest = MessageDigest.getInstance("SHA-512");
            mdigest.update(salt); //add salt
            byte[] passwordBytes = mdigest.digest((password + PEPPER).getBytes()); //add pepper
            String hashedPassword = Base64.getEncoder().encodeToString(passwordBytes);
            return hashedPassword;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    //checks if username exists in database
    private boolean exists(String inputUsername) {
        try {
        	String query = "SELECT * FROM User WHERE username = ?";
        	PreparedStatement statement = connection.prepareStatement(query);
        	statement.setString(1, inputUsername);
            ResultSet res = statement.executeQuery();
            return res.next();
        } catch (Exception e) {
        	return false;
        }
    }
    
    private void signup(String inputUsername, String inputPassword) {
        try {
        	//check if username exists
            if (exists(inputUsername)) {
                JOptionPane.showMessageDialog(this, "This username already exists.");
                return;
            }

            byte[] salt = generateSalt();

            //hash password
            String hashedPassword = hashPassword(inputPassword, salt);

            //insert user into database with prepared statement
            try {
            	String query = "INSERT INTO User (username, salt, hashedpassword, wins, losses) VALUES (?, ?, ?, ?, ? )";
            	PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, inputUsername);
                statement.setString(2, Base64.getEncoder().encodeToString(salt));
                statement.setString(3, hashedPassword);
                statement.setInt(4, 0);
                statement.setInt(5, 0);
                statement.executeUpdate();
            } finally {  //success
            	JOptionPane.showMessageDialog(this, "Sign up was successful.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error signing up to Big Two.");
        }
        
        //clear text fields
        username.setText("");
        password.setText("");
    }

    private void login(String inputUsername, String inputPassword) {
        try {
            //query for login
        	String query = "SELECT salt, hashedpassword FROM User WHERE username = ?";
	    	PreparedStatement statement = connection.prepareStatement(query);
	    	statement.setString(1, inputUsername);
	        ResultSet res = statement.executeQuery();
	        //check if user exists
	        if (res.next()) {
	        	//fetch salt from result
	            byte[] salt = Base64.getDecoder().decode(res.getString("salt"));
	            //fetch hashed password from result
	            String hashedPassword = res.getString("hashedpassword");
	            //hash input to compare
	            String hashedInput = hashPassword(inputPassword, salt);
	            //compare the hashed passwords
	            if (hashedInput.equals(hashedPassword)) {
	                JOptionPane.showMessageDialog(this, "Passwords match, login successful!");
	                callback.success(inputUsername); //send username back to caller
                    //close connection
                    connection.close();
	                //dispose of this window
	                dispose();
	            } else {
	                JOptionPane.showMessageDialog(this, "Invalid username or password.");
	            }
	        } else {
	            JOptionPane.showMessageDialog(this, "Invalid username or password.");
	        }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error logging in. Try again.");
        }
        
        //clear textfields
        username.setText("");
        password.setText("");
    }
    

    
}

import java.sql.*;
import java.util.Date;


public class BigTwoDatabase {
	
    //checks if a table exists in the database
    private static boolean exists(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        ResultSet resultSet = metadata.getTables(null, null, tableName, null);
        return resultSet.next();
    }

    //creates our user table
    private static void createUser(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String query = "CREATE TABLE User (" +
				                    "uid INTEGER PRIMARY KEY AUTOINCREMENT," +
				                    "username TEXT NOT NULL," +
				                    "salt BLOB NOT NULL," +
				                    "hashedpassword TEXT NOT NULL," +
                                    "wins INTEGER NOT NULL," +
                                    "losses INTEGER NOT NULL);";
            statement.executeUpdate(query);
        }
    }
    //creates our game table
    private static void createGame(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String query = "CREATE TABLE Game (" +
                                    "gid INTEGER PRIMARY KEY AUTOINCREMENT," +
                                    "numplayers INTEGER NOT NULL," +
                                    "players TEXT NOT NULL," +
                                    "winner TEXT NOT NULL," +
                                    "datetime DATETIME NOT NULL)";

            statement.executeUpdate(query);
        }
    }
    
    
    public static void main(String[] args) {
    	

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:javabook.db")) {
        	/*
        	Statement statement = connection.createStatement();
            String drop = "DROP TABLE IF EXISTS User";
            statement.executeUpdate(drop);
            
        	Statement statement2 = connection.createStatement();
            String drop2 = "DROP TABLE IF EXISTS Game";
            statement2.executeUpdate(drop2);
            */
            //check if User exists
            if (!exists(connection, "User")) {
                createUser(connection);
                System.out.println("User Table Created");
            }
            else {
            	System.out.println("User already exists");
            }
            //check if Game exists
            if (!exists(connection, "Game")) {
                createGame(connection);
                System.out.println("Game Table Created");
            }
            else {
            	System.out.println("Game already exists");
            }
            /*
			String query = "INSERT INTO Game (numplayers, players, winner, datetime) VALUES (?, ?, ?, ?)";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, 4);
			statement.setString(2, "a,b");
			statement.setString(3, "a");
			statement.setString(4, new Date().toString());
			statement.executeUpdate();
			*/
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

}

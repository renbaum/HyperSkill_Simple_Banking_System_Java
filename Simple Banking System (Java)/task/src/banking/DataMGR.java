package banking;

import org.w3c.dom.html.HTMLImageElement;

import java.sql.*;
import java.util.Map;
import java.util.Random;

class Account {
    String cardNumber;
    String pin;
    Random random = new Random();

    public Account(String cardNumber, String pin) {
        this.cardNumber = cardNumber;
        this.pin = pin;
    }

    public Account(){
        createNewCard();
    }

    private void createNewCard() {

        cardNumber = String.format("400000%09d", random.nextInt(1_000_000_000));
        cardNumber = String.format("%s%d", cardNumber, getLuhnChecksum(cardNumber));
        pin = String.format("%04d", random.nextInt(10_000));
    }

    public boolean checkLuhnChecksum(String cardNumber){
        int checksum = getLuhnChecksum(cardNumber);
        char lastChar = cardNumber.charAt(cardNumber.length() - 1);
        if(Character.isDigit(lastChar)) {
            int intValue = Character.getNumericValue(lastChar);
            if(checksum == intValue) return true;
        }
        return false;
    }

    public int getLuhnChecksum(String cardNumber){
        if(cardNumber.length() >= 16){
            cardNumber = cardNumber.substring(0, 15);
        }
        int sum = 0;
        for(int i = 0; i < cardNumber.length(); i++){
            int digit = Character.getNumericValue(cardNumber.charAt(i));
            if(i % 2 == 0){
                digit *= 2;
                if(digit > 9){
                    digit -= 9;
                }
            }
            sum += digit;
        }
        return sum % 10 == 0 ? 0 : 10 - sum % 10;
    }

    public boolean checkCard(String cardNumber, String pin) {
        return this.cardNumber.equals(cardNumber) && this.pin.equals(pin);
    }
}

public class DataMGR{
    static DataMGR instance = null;
    String fileName = "";
    Connection connection = null;
    Statement statement = null;
    Account account = null;

    private DataMGR(){
        account = null;
    }

    public static DataMGR getInstance(){
        if(instance == null){
            instance = new DataMGR();
        }
        return instance;
    }

    Account createAccount(){
        Account account = new Account();
        saveAccount(account);
        return account;
    }



    public boolean checkAccount(String cardNumber, String pin) {
        Account account = getAccount(cardNumber);
        this.account = null;

        if(account == null){return false;}
        if(account.checkCard(cardNumber, pin)){
            this.account = account;
            return true;
        }
        return false;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
        openDB();
    }

    public boolean openDB(){
        if(fileName.isEmpty()){return false;}

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(String.format("jdbc:sqlite:%s", fileName));
            //System.out.println("Connection to SQLite has been established.");
            // create table
            statement = connection.createStatement();
            String createTableSQL = "CREATE TABLE IF NOT EXISTS card (" +
                                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                    "number TEXT NOT NULL," +
                                    "pin TEXT NOT NULL," +
                                    "balance INTEGER DEFAULT 0)";
            statement.executeUpdate(createTableSQL);

        } catch (ClassNotFoundException e) {
            System.err.println("Failed to load SQLite JDBC driver.");
        }catch(SQLException e){
            System.err.println("SQLite connection error.");
            return false;
        }
        return true;
    }

    private void saveAccount(Account account) {
        try {
            String sql = String.format("INSERT INTO card (number, pin) VALUES ('%s', '%s')", account.cardNumber, account.pin);
            statement.executeUpdate(sql);
        }catch(SQLException e){
            System.err.println("Error while inserting data in database");
        }
    }

    private Account getAccount(String cardNumber) {
        Account account = null;

        try{
            ResultSet resultSet = statement.executeQuery(String.format("SELECT * FROM card WHERE number='%s'", cardNumber));
            while(resultSet.next()) {
                account = new Account(resultSet.getString("number"),
                                    resultSet.getString("pin"));
                resultSet.close();
            }
        }catch(SQLException e){
            System.err.println("Error while geting data from db");
        }
        return account;
    }

    public void showBalance() {
        if(account == null){
            System.out.println("there is no account");
            return;
        }
        try {
            ResultSet resultSet = statement.executeQuery(String.format("SELECT * FROM card WHERE number='%s'", account.cardNumber));
            while(resultSet.next()) {
                System.out.println(String.format("Balance: %d", resultSet.getInt("balance")));
            }
            resultSet.close();
        }catch(SQLException e){
            System.err.println("Error while geting balance from db");
        }
    }

    public void addIncome(int amount) {
        if(account == null){
            System.out.println("there is no account");
            return;
        }
        if(changeBalance(account.cardNumber, amount) == true){
            System.out.println("Income was added!");
        }
    }

    public boolean changeBalance(String cardNumber, int amount){
        try {
            String sql = "UPDATE card SET balance = balance + ? where number = ? and balance + ? >= 0";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, amount);
            preparedStatement.setString(2, cardNumber);
            preparedStatement.setInt(3, amount);
            int rowsAffected = preparedStatement.executeUpdate();
            preparedStatement.close();
            if(rowsAffected == 0){
                return false;
            }
        }catch(SQLException e){
            System.err.println("Error while add income balance from db");
            return false;
        }
        return true;
    }

    public boolean existsAccount(String cardNumber) {
        try {
            ResultSet resultSet = statement.executeQuery(String.format("SELECT * FROM card WHERE number='%s'", cardNumber));
            if(resultSet.next()) {
                return true;
            }
            resultSet.close();
        }catch(SQLException e){
            System.err.println("Error while geting balance from db");
        }
        return false;
    }

    public void closeDB(){
        try {
            statement.close();
            connection.close();
        }catch(SQLException e){
            System.err.println("Error while closing database connection");
        }
    }

    public void logOut() {
        account = null;
    }

    public boolean checkCard(String cardNumber) {
        if(account == null){
            return false;
        }
        if(account.cardNumber.equals(cardNumber)){
            System.out.println("You can't transfer money to the same account!");
            return false;
        }
        if(account.checkLuhnChecksum(cardNumber) == false){
            System.out.println("Probably you made a mistake in the card number.");
            return false;
        }
        return true;
    }

    public boolean closeActiveAccount() {
        if(account == null){
            System.out.println("There is no active account");
            return false;
        }
        try {
            String sql = String.format("DELETE FROM card WHERE number='%s'", account.cardNumber);
            statement.executeUpdate(sql);
            return true;
        }catch(SQLException e){
            System.err.println("Error while closing active account");
        }
        return false;
    }
}

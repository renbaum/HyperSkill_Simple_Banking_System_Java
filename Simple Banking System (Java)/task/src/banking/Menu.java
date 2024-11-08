package banking;

import java.util.Scanner;

public class Menu{
    enum State {
        START,
        CREATE_ACCOUNT,
        LOG_IN,
        LOG_OUT,
        BALANCE,
        EXIT
    }

    State state;
    Scanner scanner = new Scanner(System.in);
    DataMGR dataMgr = null;

    public Menu(){
        dataMgr = DataMGR.getInstance();
        state = State.START;

        while (state != State.EXIT) {
            showMenu();

        }
        System.out.println();
        System.out.println("Bye!");
    }

    private void showMenu() {
        switch (state){
            case START:
                showEntryMenu();
                break;
            case CREATE_ACCOUNT:
                showCreateAccountMenu();
                break;
            case LOG_IN:
                showLogin();
                break;
            case BALANCE:
                showBalance();
                break;
            default:
                System.out.println("Unknown state: " + state);
                state = State.EXIT;
                break;
        }
    }

    private void showBalance() {
        System.out.println("1. Balance");
        System.out.println("2. Add income");
        System.out.println("3. Do transfer");
        System.out.println("4. Close account");
        System.out.println("5. Log out");
        System.out.println("0. Exit");
        int choice = scanner.nextInt();
        scanner.nextLine();
        switch (choice){
            case 1:
                System.out.println();
                dataMgr.showBalance();
                break;
            case 2:
                System.out.println();
                System.out.println("Enter income:");
                int income = scanner.nextInt();
                scanner.nextLine();
                dataMgr.addIncome(income);
                break;
            case 3:
                System.out.println();
                System.out.println("Enter card number:");
                String cardNumber = scanner.nextLine();
                if(!dataMgr.checkCard(cardNumber)) return;
                if(dataMgr.existsAccount(cardNumber)){
                    System.out.println("Enter how much money you want to transfer:");
                    int amount = scanner.nextInt();
                    scanner.nextLine();
                    if(!dataMgr.changeBalance(dataMgr.account.cardNumber, amount*(-1))){
                        System.out.println("Not enough money!");
                        return;
                    }
                    dataMgr.changeBalance(cardNumber, amount);
                    System.out.println("Success!");
                }else {
                    System.out.println("Such a card does not exist");
                }
                break;
            case 4:
                System.out.println();
                if(dataMgr.closeActiveAccount()){
                    System.out.println("The account has been closed!");
                    System.out.println();
                    dataMgr.logOut();
                    state = State.START;
                }
                break;
            case 5:
                System.out.println();
                dataMgr.logOut();
                System.out.println("You have successfully logged out!");
                System.out.println();
                state = State.START;
                break;
            case 0:
                state = State.EXIT;
        }
    }

    private void showLogin() {
        System.out.println();
        scanner.nextLine();
        System.out.println("Enter your card number:");
        String cardNumber = scanner.nextLine();
        System.out.println("Enter your PIN:");
        String pin = scanner.nextLine();
        System.out.println();

        if(!dataMgr.checkAccount(cardNumber, pin)){
            System.out.println("Wrong card number or PIN!");
            System.out.println();
            state = State.START;
            return;
        }

        System.out.println("You have successfully logged in!");
        System.out.println();
        state = State.BALANCE;
    }

    private void showCreateAccountMenu() {
        System.out.println();
        Account account = dataMgr.createAccount();
        System.out.println("Your card has been created");
        System.out.println("Your card number:");
        System.out.println(account.cardNumber);
        System.out.println("Your card PIN:");
        System.out.println(account.pin);
        System.out.println();

        state = State.START;
    }


    private void showEntryMenu() {
        System.out.println("1. Create an account");
        System.out.println("2. Log into account");
        System.out.println("0. Exit");
        int choice = scanner.nextInt();
        switch (choice){
            case 1:
                state = State.CREATE_ACCOUNT;
                break;
            case 2:
                state = State.LOG_IN;
                break;
            case 0:
                state = State.EXIT;
        }
    }

}

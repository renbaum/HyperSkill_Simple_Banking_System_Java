package banking;

public class Main {
    public static void main(String[] args) {
        CommandLineParser parser = new CommandLineParser();
        parser.addParameter("-fileName", "db.s3db");
        parser.add(args);
        String fileName = parser.getValue("-fileName");

        DataMGR.getInstance().setFileName(fileName);

        Menu menu = new Menu();
        DataMGR.getInstance().closeDB();
    }
}
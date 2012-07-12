package controller;

import au.com.bytecode.opencsv.CSVReader;
import com.kentcdodds.javahelper.helpers.IOHelper;
import com.kentcdodds.javahelper.helpers.OtherHelper;
import com.kentcdodds.javahelper.helpers.PrinterHelper;
import com.kentcdodds.javahelper.helpers.StringHelper;
import java.io.*;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Recipient;

/**
 * This simply takes in a username and password for a Google Voice account and a CSV file with the first two columns
 * being phone number, message.
 *
 * @author kentcdodds
 */
public class Controller {

  public static boolean hidePass = true;
  private static String username;
  private static String password;
  private static String csvLocation;

  /**
   * @param args the command line arguments. 0: Properties file
   */
  public static void main(String[] args) {
    PrinterHelper.setInstancePrint(true);
    if (args.length != 1) {
      PrinterHelper.println("Please give only one argument: The location of a properties file.");
      PrinterHelper.println("This file must have:" + StringHelper.newline
              + "\t" + StringHelper.splitBy(StringHelper.newline + "\t",
              "username",
              "password",
              "csv_location*",
              "descramble_pass (optional, true/false, Descramble the password using the Java Helper Library Descrabler)",
              "hide_pass (optional, true/false, hide the password on failure to login.",
              "\t*Can be on disk or a URL to a csv file online. This file must have at least 3 columns: 1 - phone number/email address; 2 - message; 3 - subject (will be ignored if not an e-mail)"));
      return;
    }
    Properties props = new Properties();
    try {
      props.load(new FileReader(args[0]));
    } catch (IOException ex) {
      PrinterHelper.println("Problem reading the properties file.");
      Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
      return;
    }
    username = props.getProperty("username");
    password = props.getProperty("password");
    csvLocation = props.getProperty("csv_location");
    if (StringHelper.isNullOrEmpty(username, password, csvLocation)) {
      PrinterHelper.println("You must have valid username, password, and csv_location elements in the properties file.");
      return;
    }
    hidePass = !isFalse(props.getProperty("hide_pass"));
    if (isTrue(props.getProperty("descramble_pass"))) {
      password = OtherHelper.descrambleString(password);
    }
    
    
    GVMessenger gvMessenger = GVMessenger.getInstance();
    gvMessenger.setUsername(username);
    gvMessenger.setPassword(password);
    
    EmailMessenger emailMessenger = EmailMessenger.getInstance();
    emailMessenger.setUsername(username);
    emailMessenger.setPassword(password);
    
    Reader inReader;
    File csvFile = new File(csvLocation);
    if (!csvFile.exists()) {
      try {
        inReader = IOHelper.getBufferedReader(csvLocation);
      } catch (MalformedURLException ex) {
        PrinterHelper.println("Problem reading the input as URL (tried because it's not a file: " + csvLocation + ")");
        PrinterHelper.println("Note, you must either use a double backslash (\\\\) or a slash (/) for file location");
        Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        return;
      } catch (IOException ex) {
        PrinterHelper.println("Problem reading the input as URL (tried because it's not a file: " + csvLocation + ")");
        PrinterHelper.println("Note, you must either use a double backslash (\\\\) or a slash (/) for file location");
        Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        return;
      }
    } else {
      try {
        inReader = new FileReader(csvFile);
      } catch (FileNotFoundException ex) {
        PrinterHelper.println("Problem reading the file" + csvLocation);
        Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        return;
      }
    }
    PrinterHelper.println("Successfully read the input...");

    CSVReader reader = new CSVReader(inReader);

    List<Recipient> recipients = new ArrayList<>();
    try {
      String[] row;
      while ((row = reader.readNext()) != null) {
        recipients.add(new Recipient(row[0], row[1].replace("\n", StringHelper.newline), row[2]));
      }
    } catch (IOException ex) {
      Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
    }

    PrinterHelper.println("Successfully read the " + recipients.size() + " recipients...");

    gvMessenger.setRecipients(recipients);
    emailMessenger.setRecipients(recipients);
    
    

    PrinterHelper.println("Executing the controllers...");
    try {
      BlockingQueue q = new ArrayBlockingQueue<>(2);
      ThreadPoolExecutor tpe = new ThreadPoolExecutor(2, 2, 5, TimeUnit.MINUTES, q);
      tpe.execute(gvMessenger);
      tpe.execute(emailMessenger);
      tpe.shutdown();
      int cycles = 0;
      while (!tpe.awaitTermination(3, TimeUnit.SECONDS) && cycles < 500) {
        cycles++;
      }
    } catch (InterruptedException ex) {
      Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
    }

    PrinterHelper.println("Printing the final status");
    
    //Print status
    PrinterHelper.println(StringHelper.newline);
    PrinterHelper.println(StringHelper.repeatString("*", 75));
    PrinterHelper.println("Successes: ");
    int successes = printRecipients(recipients, Recipient.SUCCESS);
    PrinterHelper.println("Total successes: " + successes);

    PrinterHelper.println(StringHelper.newline);
    PrinterHelper.println(StringHelper.repeatString("*", 75));
    PrinterHelper.println("Failures: ");
    int failures = printRecipients(recipients, Recipient.FAILURE);
    PrinterHelper.println("Total failures: " + failures);

    PrinterHelper.println(StringHelper.newline);
    PrinterHelper.println(StringHelper.repeatString("*", 75));
    PrinterHelper.println("Never started: ");
    int waiting = printRecipients(recipients, Recipient.WAITING);
    PrinterHelper.println("Total never started: " + waiting);

    PrinterHelper.println(StringHelper.newline);
    PrinterHelper.println(StringHelper.repeatString("*", 75));
    PrinterHelper.println("Never sent: ");
    int sending = printRecipients(recipients, Recipient.SENDING);
    PrinterHelper.println("Total never sent: " + sending);

    PrinterHelper.println(StringHelper.newline);
    PrinterHelper.println(StringHelper.repeatString("*", 75));

    //Close input streams
    try {
      reader.close();
      inReader.close();
    } catch (IOException ex) {
      Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
    }
    System.exit(1);
  }

  /**
   * Prints the list in a readable fashion if they have the given status
   *
   * @param list
   */
  private static int printRecipients(List<Recipient> list, int status) {
    int count = 0;
    for (Recipient recipient : list) {
      if (recipient.isStatus(status)) {
        PrinterHelper.println(recipient.getAddress() + (recipient.hasEmail() ? " (" + recipient.getSubject() + "):" : ":") + StringHelper.newline
                + "\t" + recipient.getMessage().replace(StringHelper.newline, StringHelper.newline + "\t"));
        count++;
      }
    }
    return count;
  }

  /**
   * Convenience method.
   *
   * @param falseString
   * @return (falseString == null || !falseString.equalsIgnoreCase("false")) ? false : true;
   */
  public static boolean isFalse(String falseString) {
    return (falseString == null || !falseString.equalsIgnoreCase("false")) ? false : true;
  }

  /**
   * Convenience method.
   *
   * @param trueString
   * @return (trueString == null || !trueString.equalsIgnoreCase("true")) ? false : true;
   */
  public static boolean isTrue(String trueString) {
    return (trueString == null || !trueString.equalsIgnoreCase("true")) ? false : true;
  }
}

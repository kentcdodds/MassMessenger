package controller;

import com.kentcdodds.javahelper.helpers.PrinterHelper;
import com.techventus.server.voice.Voice;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Recipient;

/**
 *
 * @author kentcdodds
 */
public class GVMessenger implements Runnable {

  private List<Recipient> recipients;
  private String username;
  private String password;
  private Voice voice;
  //<editor-fold defaultstate="collapsed" desc="Singleton Pattern Methods">
  private static GVMessenger instance;

  /**
   * private Constructor
   */
  private GVMessenger() {
  }

  /**
   * If the instance of this GVMessenger is null, this will create a new GVMessenger
   *
   * @return instance of this GVMessenger
   */
  public static GVMessenger getInstance() {
    if (instance == null) {
      instance = new GVMessenger();
    }
    return instance;
  }

  /**
   * Sets the instance to null and returns getInstance()
   *
   * @return instance of this GVMessenger
   */
  public static GVMessenger resetInstance() {
    instance = null;
    return getInstance();
  }

  /**
   * Sets the instance to the given newInstance and returns getInstance()
   *
   * @param newInstance the newInstance of GVMessenger
   * @return instance of this GVMessenger
   */
  public static GVMessenger resetInstance(GVMessenger newInstance) {
    instance = newInstance;
    return getInstance();
  }
  //</editor-fold>

  @Override
  public void run() {
    for (Recipient recipient : recipients) {
      if (recipient.hasPhoneNumber()) {
        try {
          recipient.setSending();
          getVoice().sendSMS(recipient.getAddress(), recipient.getMessage());
          recipient.setSucceeded();
        } catch (IOException ex) {
          recipient.setFailed();
          Logger.getLogger(GVMessenger.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }
  }

  public Voice getVoice() {
    if (voice == null) {
      try {
        PrinterHelper.println("Logging into Google Voice...");
        voice = new Voice(username, password);
      } catch (IOException ex) {
        PrinterHelper.print("Failed logging into Google Voice with " + username + ": " + ((Controller.hidePass) ? "##password hidden##" : password));
        Logger.getLogger(GVMessenger.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    return voice;
  }

  /**
   * @return the recipients
   */
  public List<Recipient> getRecipients() {
    return recipients;
  }

  /**
   * @param recipients the recipients to set
   */
  public void setRecipients(List<Recipient> recipients) {
    this.recipients = recipients;
  }

  /**
   * @return the username
   */
  public String getUsername() {
    return username;
  }

  /**
   * @param username the username to set
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * @return the password
   */
  public String getPassword() {
    return password;
  }

  /**
   * @param password the password to set
   */
  public void setPassword(String password) {
    this.password = password;
  }
}

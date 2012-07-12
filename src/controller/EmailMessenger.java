package controller;

import com.kentcdodds.javahelper.helpers.EmailHelper;
import com.kentcdodds.javahelper.model.Email;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.mail.Session;
import model.Recipient;

/**
 *
 * @author kentcdodds
 */
public class EmailMessenger implements Runnable {

  private List<Recipient> recipients;
  private String username;
  private String password;
  private Session session;
  //<editor-fold defaultstate="collapsed" desc="Singleton Pattern Methods">
  private static EmailMessenger instance;

  /**
   * private Constructor
   */
  private EmailMessenger() {
  }

  /**
   * If the instance of this EmailMessenger is null, this will create a new EmailMessenger
   *
   * @return instance of this EmailMessenger
   */
  public static EmailMessenger getInstance() {
    if (instance == null) {
      instance = new EmailMessenger();
    }
    return instance;
  }

  /**
   * Sets the instance to null and returns getInstance()
   *
   * @return instance of this EmailMessenger
   */
  public static EmailMessenger resetInstance() {
    instance = null;
    return getInstance();
  }

  /**
   * Sets the instance to the given newInstance and returns getInstance()
   *
   * @param newInstance the newInstance of EmailMessenger
   * @return instance of this EmailMessenger
   */
  public static EmailMessenger resetInstance(EmailMessenger newInstance) {
    instance = newInstance;
    return getInstance();
  }
  //</editor-fold>

  @Override
  public void run() {
    for (Recipient recipient : recipients) {
      if (recipient.hasEmail()) {
        try {
          recipient.setSending();
          Email email = new Email("kentcdodds@ldschurch.org", recipient.getAddress(), recipient.getSubject(), recipient.getMessage());
          EmailHelper.sendEmail(getSession(), email);
          recipient.setSucceeded();
        } catch (MessagingException ex) {
          recipient.setFailed();
          Logger.getLogger(EmailMessenger.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }
  }

  public Session getSession() {
    if (session == null) {
      session = EmailHelper.getGoogleSession(username, password);
    }
    return session;
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

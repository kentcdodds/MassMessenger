package model;

import com.kentcdodds.javahelper.helpers.NumberHelper;
import com.kentcdodds.javahelper.helpers.PrinterHelper;

/**
 * Javabean which has an address, message, and status.
 *
 * @author kentcdodds
 */
public class Recipient {

  private String address;
  private String message;
  private String subject;
  public static final int WAITING = -1, SENDING = 0, SUCCESS = 1, FAILURE = 2;
  private int status = WAITING;

  public Recipient(String address, String message, String subject) {
    this.address = address;
    this.message = message;
    this.subject = subject;
  }

  /**
   * @return whether the address (after being cleaned of all non-digit characters) is 10 digits and whether it's a valid
   * long.
   */
  public boolean hasPhoneNumber() {
    String cleanNumber = NumberHelper.cleanNumber(address).replace(".", "");
    return NumberHelper.isValidNumber(long.class, cleanNumber) && cleanNumber.length() == 10;
  }

  /**
   * @return whether the address contains an @ symbol.
   */
  public boolean hasEmail() {
    return address.contains("@");
  }

  /**
   * @return whether the status is SENDING
   */
  public boolean sending() {
    return isStatus(SENDING);
  }

  /**
   * @return whether the status is WAITING
   */
  public boolean waiting() {
    return isStatus(WAITING);
  }

  /**
   * @return whether the status is FAILURE
   */
  public boolean failed() {
    return isStatus(FAILURE);
  }

  /**
   * @return whether the status is SUCCESS
   */
  public boolean succeeded() {
    return isStatus(SUCCESS);
  }

  /**
   * @param status
   * @return whether the status of the object is equal to the given status
   */
  public boolean isStatus(int status) {
    return this.status == status;
  }

  /**
   * Prints
   */
  public void setSending() {
    PrinterHelper.println(address + " sending");
    status = SENDING;
  }

  public void setSucceeded() {
    PrinterHelper.println(address + " succeeded");
    status = SUCCESS;
  }

  public void setFailed() {
    PrinterHelper.println(address + " failed");
    status = FAILURE;
  }

  public void setWaiting() {
    PrinterHelper.println(address + " waiting");
    status = WAITING;
  }

  /**
   * @return the address
   */
  public String getAddress() {
    return address;
  }

  /**
   * @param address the address to set
   */
  public void setAddress(String address) {
    this.address = address;
  }

  /**
   * @return the message
   */
  public String getMessage() {
    return message;
  }

  /**
   * @param message the message to set
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * @return the subject
   */
  public String getSubject() {
    return subject;
  }

  /**
   * @param subject the subject to set
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }
}

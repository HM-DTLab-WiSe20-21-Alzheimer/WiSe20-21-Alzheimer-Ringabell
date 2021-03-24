package ringabell.handlers.helper;

/**
 * Entry.
 *
 * @author Anonymous Student
 */

public class Entry {
  private String value;
  private String day;
  private String time;
  private boolean temporary;

  Entry() { }

  /**
   * Constructor to create a new Entry.
   *
   * @param value the value String.
   * @param day the entry day.
   * @param time the entry time.
   * @param temporary true if entry is temporary.
   */
  public Entry(String value, String day, String time, boolean temporary) {
    this.value = value;
    this.day = day;
    this.time = time;
    this.temporary = temporary;
  }

  public String getValue() {
    return value;
  }

  public String getDay() {
    return day;
  }

  public String getTime() {
    return time;
  }

  public boolean getTemporary() {
    return temporary;
  }
}

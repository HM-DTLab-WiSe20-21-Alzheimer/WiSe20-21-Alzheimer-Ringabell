package ringabell.handlers.helper;

import com.amazon.ask.attributes.AttributesManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * EntryRepository.
 *
 * @author Anonymous Student
 */

public class EntryRepository {

  private static final String TABLE_KEY = "entries";

  private final AttributesManager attributesManager;

  private final ObjectMapper mapper;

  public EntryRepository(AttributesManager attributesManager) {
    this.attributesManager = attributesManager;
    this.mapper = new ObjectMapper();
  }

  /**
   * Adds one Entry to the database.
   *
   * @param entry the entry.
   * @return true if entry is added. False when entry already exists.
   * @throws JsonProcessingException when the json mapper cannot map to List.
   */
  public boolean addEntry(Entry entry) throws JsonProcessingException {
    List<Entry> entryList = this.getEntries();

    boolean exists = this.findEntry(entryList, entry).isPresent();

    if (exists) {
      return false;
    }

    entryList.add(entry);
    this.saveDatabase(entryList);
    return true;
  }

  /**
   * Gets all Entries for the date and after the time.
   *
   * @param date the date from the entries.
   * @param time all entries must be after this time.
   * @return the List of entries.
   * @throws JsonProcessingException when the json mapper cannot map to List.
   */
  public List<Entry> getDateEntries(LocalDate date, LocalTime time) throws JsonProcessingException {
    final List<Entry> entryList = this.getEntries();
    final List<Entry> dateEntries = new ArrayList<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    for (Entry entry : entryList) {
      if (entry.getDay() == null) {
        dateEntries.add(entry);
        continue;
      }

      LocalDate entryDate = LocalDate.parse(entry.getDay());
      Entry thisEntry = entry;

      if (date.isAfter(entryDate)) {
        var todayEntry = this.deleteOldValue(thisEntry, date, entryDate);

        if (todayEntry.isPresent()) {
          thisEntry = todayEntry.get();
          entryDate = LocalDate.parse(thisEntry.getDay());
        }
      }

      if (date.isEqual(entryDate) && thisEntry.getTime() == null) {
        dateEntries.add(thisEntry);
      } else if (date.isEqual(entryDate)) {
        LocalTime entryTime = LocalTime.parse(thisEntry.getTime(), formatter);

        if (time.isBefore(entryTime)) {
          dateEntries.add(thisEntry);
        }
      }
    }

    return dateEntries;
  }

  /**
   * Get the entries from now on. Delete the old.
   *
   * @return the entries.
   * @throws JsonProcessingException when the object mapper has an error.
   */
  public List<Entry> getDateEntries() throws JsonProcessingException {
    LocalDate localDate = java.time.LocalDate.now();
    LocalTime localTime = java.time.LocalTime.now();

    return this.getDateEntries(localDate, localTime);
  }

  /**
   * Gets all Entries from the database.
   *
   * @return the List of all Entries.
   * @throws JsonProcessingException when the json mapper cannot map to List.
   */
  public List<Entry> getEntries() throws JsonProcessingException {
    final Map<String, Object> attributes = this.attributesManager.getPersistentAttributes();
    final String jsonString = (String) attributes.get(TABLE_KEY);
    List<Entry> entryList = null;

    if (jsonString != null) {
      entryList = this.mapper.readValue(jsonString, new TypeReference<List<Entry>>() {
      });
    }

    if (entryList == null) {
      entryList = new ArrayList<>();
    }

    return entryList;
  }

  /**
   * Deletes one Entry in the database.
   *
   * @param entry the Entry you want to delete.
   * @return true if it could be deleted.
   * @throws JsonProcessingException when there is an error in the object mapping.
   */
  public boolean deleteEntry(Entry entry) throws JsonProcessingException {
    final List<Entry> entryList = this.getEntries();

    var foundEntry = this.findEntry(entryList, entry);

    if (foundEntry.isEmpty()) {
      return false;
    }

    entryList.remove(foundEntry.get());
    this.saveDatabase(entryList);
    return true;
  }

  private Optional<Entry> findEntry(List<Entry> entryList, Entry entry) {
    return entryList.stream()
        .filter(item -> entry.getValue().equals(item.getValue())
            && entry.getTemporary() == item.getTemporary()
            && ((entry.getDay() == null && item.getDay() == null)
            || (entry.getDay() != null && entry.getDay().equals(item.getDay())))
            && ((entry.getTime() == null && item.getTime() == null)
            || (entry.getTime() != null && entry.getTime().equals(item.getTime()))))
        .findAny();
  }

  private void saveDatabase(List<Entry> entries) throws JsonProcessingException {
    String inputData = entries.isEmpty() ? null : this.mapper.writeValueAsString(entries);

    Map<String, Object> persistentAttributes = new HashMap<>();
    persistentAttributes.put(TABLE_KEY, inputData);
    attributesManager.setPersistentAttributes(persistentAttributes);

    attributesManager.savePersistentAttributes();
  }

  private Optional<Entry> deleteOldValue(Entry entry, LocalDate date, LocalDate entryDate)
      throws JsonProcessingException {

    this.deleteEntry(entry);

    if (!entry.getTemporary()) {
      while (date.isAfter(entryDate)) {
        entryDate = entryDate.plusWeeks(1);
      }

      Entry nextWeekEntry = new Entry(entry.getValue(), entryDate.toString(),
          entry.getTime(), entry.getTemporary());

      this.addEntry(nextWeekEntry);

      if (date.isEqual(entryDate)) {
        return Optional.of(nextWeekEntry);
      }
    }

    return Optional.empty();
  }
}

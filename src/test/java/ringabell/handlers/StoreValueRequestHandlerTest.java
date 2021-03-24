package ringabell.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.RequestEnvelope;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anonymous Student
 */
class StoreValueRequestHandlerTest extends BaseHandlerTest {

  @Override
  String getName() {
    return "StoreIntent";
  }

  @Override
  RequestHandler getRequestHandler() {
    return new StoreValueRequestHandler();
  }

  @Test
  void nullSlotTest() {
    var intent = this.getMockIntent();
    var persistentData = this.getMockPersistenceAd();
    persistentData.saveAttributes(RequestEnvelope.builder().build(), new HashMap<>());
    var handlerInput = this.getHandlerInput(intent, persistentData);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getOutputSpeech().toString().contains("Es ist ein Fehler aufgetreten. Kein Wert."));
  }

  @Test
  void storePermanentValueTest() {
    var testEntry = "Test";
    var intent = this.getMockIntent(this.createSlots(Map.of("toStore", testEntry, "temporary", "Nein")));
    var persistentData = this.getMockPersistenceAd();
    var handlerInput = this.getHandlerInput(intent, persistentData);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getOutputSpeech().toString().contains(String.format("Ich habe mir den wiederkehrenden Eintrag: %s gemerkt.", testEntry)));

    var databaseValues = persistentData.getAttributes(null).get();
    assertEquals(1, databaseValues.size());
    String actualString = (String) databaseValues.get("entries");
    String expectedString = "[{\"value\":\"" + testEntry + "\",\"day\":null,\"time\":null,\"temporary\":false}]";
    assertEquals(expectedString, actualString);
  }

  @Test
  void storeSamePermanentValueTest() {
    var testEntry = "Test";
    var intent = this.getMockIntent(this.createSlots(Map.of("toStore", testEntry, "temporary", "Nein")));
    var persistentData = this.getMockPersistenceAd();
    var handlerInput = this.getHandlerInput(intent, persistentData);

    this.getRequestHandler().handle(handlerInput);
    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getOutputSpeech().toString().contains("Das habe ich mir schonmal gemerkt."));

    var databaseValues = persistentData.getAttributes(null).get();
    assertEquals(1, databaseValues.size());
    String actualString = (String) databaseValues.get("entries");
    String expectedString = "[{\"value\":\"" + testEntry + "\",\"day\":null,\"time\":null,\"temporary\":false}]";
    assertEquals(expectedString, actualString);
  }

  @Test
  void storeTemporaryValueTest() {
    var testEntry = "Test";
    var testDay = java.time.LocalDate.now().toString();
    var testTime = java.time.LocalTime.now().plusMinutes(10).toString();
    Map<String, String> testMap = Map.of("toStore", testEntry, "day", testDay, "time", testTime, "temporary", "ja");
    var intent = this.getMockIntent(this.createSlots(testMap));
    var persistentData = this.getMockPersistenceAd();
    var handlerInput = this.getHandlerInput(intent, persistentData);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getOutputSpeech().toString().contains(String.format("Ich habe mir den temporären Eintrag: %s gemerkt.", testEntry)));

    var databaseValues = persistentData.getAttributes(null).get();
    assertEquals(1, databaseValues.size());
    String actualString = (String) databaseValues.get("entries");
    String expectedString = "[{\"value\":\"" + testEntry + "\",\"day\":\"" + testDay + "\",\"time\":\"" + testTime + "\",\"temporary\":true}]";
    assertEquals(expectedString, actualString);
  }

  @Test
  void storeSameTemporaryValueTest() {
    var testEntry = "Test";
    var testDay = java.time.LocalDate.now().toString();
    var testTime = java.time.LocalTime.now().plusMinutes(10).toString();
    Map<String, String> testMap = Map.of("toStore", testEntry, "day", testDay, "time", testTime, "temporary", "Ja");
    var intent = this.getMockIntent(this.createSlots(testMap));
    var persistentData = this.getMockPersistenceAd();
    var handlerInput = this.getHandlerInput(intent, persistentData);

    this.getRequestHandler().handle(handlerInput);
    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getOutputSpeech().toString().contains("Das habe ich mir schonmal gemerkt."));

    var databaseValues = persistentData.getAttributes(null).get();
    assertEquals(1, databaseValues.size());
  }

  @Test
  void storeNoTimeTemporaryValueTest() {
    var testEntry = "Test";
    var testDay = java.time.LocalDate.now().toString();
    Map<String, String> testMap = Map.of("toStore", testEntry, "day", testDay, "temporary", "ja");
    var intent = this.getMockIntent(this.createSlots(testMap));
    var persistentData = this.getMockPersistenceAd();
    var handlerInput = this.getHandlerInput(intent, persistentData);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());

    var databaseValues = persistentData.getAttributes(null).get();
    String actualString = (String) databaseValues.get("entries");
    String expectedString = "[{\"value\":\"" + testEntry + "\",\"day\":\"" + testDay + "\",\"time\":null,\"temporary\":true}]";
    assertEquals(expectedString, actualString);
  }

  @Test
  void storeNoDayTemporaryValueTest() {
    var testEntry = "Test";
    var testTime = java.time.LocalTime.now().plusMinutes(10).toString();
    Map<String, String> testMap = Map.of("toStore", testEntry, "time", testTime, "temporary", "ja");
    var intent = this.getMockIntent(this.createSlots(testMap));
    var persistentData = this.getMockPersistenceAd();
    var handlerInput = this.getHandlerInput(intent, persistentData);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());

    var databaseValues = persistentData.getAttributes(null).get();
    var expectedDay = java.time.LocalDate.now().toString();
    String actualString = (String) databaseValues.get("entries");
    String expectedString = "[{\"value\":\"" + testEntry + "\",\"day\":\"" + expectedDay + "\",\"time\":\"" + testTime + "\",\"temporary\":true}]";
    assertEquals(expectedString, actualString);
  }

  @Test
  void storeTodayTemporaryValueTest() {
    var testEntry = "Test";
    Map<String, String> testMap = Map.of("toStore", testEntry, "temporary", "ja");
    var intent = this.getMockIntent(this.createSlots(testMap));
    var persistentData = this.getMockPersistenceAd();
    var handlerInput = this.getHandlerInput(intent, persistentData);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());

    var databaseValues = persistentData.getAttributes(null).get();
    var expectedDay = java.time.LocalDate.now().toString();
    String actualString = (String) databaseValues.get("entries");
    String expectedString = "[{\"value\":\"" + testEntry + "\",\"day\":\"" + expectedDay + "\",\"time\":null,\"temporary\":true}]";
    assertEquals(expectedString, actualString);
  }

  @Test
  void storeNoTimeRecurringValueTest() {
    var testEntry = "Test";
    var testDay = java.time.LocalDate.now().toString();
    Map<String, String> testMap = Map.of("toStore", testEntry, "day", testDay, "temporary", "Nein");
    var intent = this.getMockIntent(this.createSlots(testMap));
    var persistentData = this.getMockPersistenceAd();
    var handlerInput = this.getHandlerInput(intent, persistentData);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());

    var databaseValues = persistentData.getAttributes(null).get();
    String actualString = (String) databaseValues.get("entries");
    String expectedString = "[{\"value\":\"" + testEntry + "\",\"day\":\"" + testDay + "\",\"time\":null,\"temporary\":false}]";
    assertEquals(expectedString, actualString);
  }

  @Test
  void storeNoDayRecurringValueTest() {
    var testEntry = "Test";
    var testTime = java.time.LocalTime.now().plusMinutes(10).toString();
    Map<String, String> testMap = Map.of("toStore", testEntry, "time", testTime, "temporary", "Nein");
    var intent = this.getMockIntent(this.createSlots(testMap));
    var persistentData = this.getMockPersistenceAd();
    var handlerInput = this.getHandlerInput(intent, persistentData);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());

    var databaseValues = persistentData.getAttributes(null).get();
    var expectedDay = java.time.LocalDate.now().toString();
    String actualString = (String) databaseValues.get("entries");
    String expectedString = "[{\"value\":\"" + testEntry + "\",\"day\":\"" + expectedDay + "\",\"time\":\"" + testTime + "\",\"temporary\":false}]";
    assertEquals(expectedString, actualString);
  }

  @Test
  void storeRecurringValueTest() {
    var testEntry = "Test";
    var testDay = java.time.LocalDate.now().toString();
    var testTime = java.time.LocalTime.now().plusMinutes(10).toString();
    Map<String, String> testMap = Map.of("toStore", testEntry, "day", testDay, "time", testTime, "temporary", "Nein");
    var intent = this.getMockIntent(this.createSlots(testMap));
    var persistentData = this.getMockPersistenceAd();
    var handlerInput = this.getHandlerInput(intent, persistentData);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());

    var databaseValues = persistentData.getAttributes(null).get();
    String actualString = (String) databaseValues.get("entries");
    String expectedString = "[{\"value\":\"" + testEntry + "\",\"day\":\"" + testDay + "\",\"time\":\"" + testTime + "\",\"temporary\":false}]";
    assertEquals(expectedString, actualString);
  }

  @Test
  void oldDateErrorTest() {
    var testEntry = "Test";
    var testDay = "2020-01-12";
    Map<String, String> testMap = Map.of("toStore", testEntry, "day", testDay,"temporary", "Nein");
    var intent = this.getMockIntent(this.createSlots(testMap));
    var persistentData = this.getMockPersistenceAd();
    var handlerInput = this.getHandlerInput(intent, persistentData);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getOutputSpeech().toString().contains("Es können nur Erinnerungen erstellt werden, welche in der Zukunft liegen."));
  }

  @Test
  void databaseErrorTest() {
    var testEntry = "Test";
    Map<String, String> testMap = Map.of("toStore", testEntry, "temporary", "Ja");
    var intent = this.getMockIntent(this.createSlots(testMap));
    var persistentData = this.getMockPersistenceAd();
    String values = "[{\"value\":\"Test1\",\"day\":[\"hallo\"],\"time\":\"12:15\"}]";
    Map<String, Object> data = Map.of("entries", values);
    persistentData.saveAttributes(RequestEnvelope.builder().build(), data);
    var handlerInput = this.getHandlerInput(intent, persistentData);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getOutputSpeech().toString().contains("Es ist ein Datenbank Fehler aufgetreten."));
  }

  @Test
  void storeWrongTime() {
    var testEntry = "Test";
    var testDay = java.time.LocalDate.now().toString();
    var testTime = java.time.LocalTime.now().minusMinutes(10).toString();
    Map<String, String> testMap = Map.of("toStore", testEntry, "day", testDay, "time", testTime, "temporary", "ja");
    var intent = this.getMockIntent(this.createSlots(testMap));
    var persistentData = this.getMockPersistenceAd();
    var handlerInput = this.getHandlerInput(intent, persistentData);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getOutputSpeech().toString().contains("Es können nur Erinnerungen erstellt werden, welche in der Zukunft liegen."));
  }

  @Test
  void storeWrongTimeWithoutDay() {
    var testEntry = "Test";
    var testTime = java.time.LocalTime.now().minusMinutes(10).toString();
    Map<String, String> testMap = Map.of("toStore", testEntry, "time", testTime, "temporary", "ja");
    var intent = this.getMockIntent(this.createSlots(testMap));
    var persistentData = this.getMockPersistenceAd();
    var handlerInput = this.getHandlerInput(intent, persistentData);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getOutputSpeech().toString().contains("Es können nur Erinnerungen erstellt werden, welche in der Zukunft liegen."));
  }

  @Test
  void storeWrongWithoutValue() {
    var testEntry = "Test";
    var testDay = java.time.LocalDate.now().toString();
    var testTime = java.time.LocalTime.now().minusMinutes(10).toString();
    Map<String, String> testMap = Map.of( "day", testDay, "time", testTime, "temporary", "ja");
    var intent = this.getMockIntent(this.createSlots(testMap));
    var persistentData = this.getMockPersistenceAd();
    var handlerInput = this.getHandlerInput(intent, persistentData);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getOutputSpeech().toString().contains("Es ist ein Fehler aufgetreten. Kein Wert."));
  }

  @Test
  void storeWrongWithoutTemp() {
    var testEntry = "Test";
    var testDay = java.time.LocalDate.now().toString();
    var testTime = java.time.LocalTime.now().minusMinutes(10).toString();
    Map<String, String> testMap = Map.of("toStore", testEntry, "day", testDay, "time", testTime);
    var intent = this.getMockIntent(this.createSlots(testMap));
    var persistentData = this.getMockPersistenceAd();
    var handlerInput = this.getHandlerInput(intent, persistentData);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getOutputSpeech().toString().contains("Es ist ein Fehler aufgetreten. Kein Wert."));
  }


}
package ringabell.handlers;

import static org.junit.jupiter.api.Assertions.*;

import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.RequestEnvelope;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
/**
 * @author Anonymous Student
 */
class DeleteValueRequestHandlerTest extends BaseHandlerTest {

  @Override
  String getName() {
    return "DeleteIntent";
  }

  @Override
  RequestHandler getRequestHandler() {
    return new DeleteValueRequestHandler();
  }

  @Test
  void nullSlotTest() {
    var intent = this.getMockIntent();
    var persistentData = this.getMockPersistenceAd();
    persistentData.saveAttributes(RequestEnvelope.builder().build(), new HashMap<>());
    var handlerInput = this.getHandlerInput(intent, persistentData);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getOutputSpeech().toString().contains("Es ist ein Fehler beim Löschen aufgetreten. Kein Wert."));
  }

  @Test
  void nullValue() {
    Map<String, String> testMap = Map.of( "day", "2022-12-1", "temporary", "ja");
    var intent = this.getMockIntent(this.createSlots(testMap));
    var persistentData = this.getMockPersistenceAd();
    persistentData.saveAttributes(RequestEnvelope.builder().build(), new HashMap<>());
    var handlerInput = this.getHandlerInput(intent, persistentData);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getOutputSpeech().toString().contains("Es ist ein Fehler beim Löschen aufgetreten. Kein Wert."));
  }

  @Test
  void nullTemp() {
    Map<String, String> testMap = Map.of( "toDelete", "something", "day", "2022-12-1");
    var intent = this.getMockIntent(this.createSlots(testMap));
    var persistentData = this.getMockPersistenceAd();
    persistentData.saveAttributes(RequestEnvelope.builder().build(), new HashMap<>());
    var handlerInput = this.getHandlerInput(intent, persistentData);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getOutputSpeech().toString().contains("Es ist ein Fehler beim Löschen aufgetreten. Kein Wert."));
  }


  @Test
  void deletePermanentValueTest() {
    var testEntry = "Test";
    var intent = this.getMockIntent(this.createSlots(Map.of("toDelete", testEntry, "temporary", "Nein")));
    var persistentData = this.getMockPersistenceAd();
    String values = "[{\"value\":\"" + testEntry + "\",\"day\":null,\"time\":null,\"temporary\":false}]";
    Map<String, Object> data = Map.of("entries", values);
    persistentData.saveAttributes(RequestEnvelope.builder().build(), data);
    var handlerInput = this.getHandlerInput(intent, persistentData);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getOutputSpeech().toString().contains(String.format("Ich habe den wiederkehrenden Eintrag: %s gelöscht.", testEntry)));

    var databaseValues = persistentData.getAttributes(null).get();
    String actualString = (String) databaseValues.get("entries");
    assertNull(actualString);
  }

  @Test
  void deleteTemporaryValueTest() {
    var testEntry = "Test";
    var testDay = java.time.LocalDate.now().toString();
    var testTime = "12:15";
    Map<String, String> testMap = Map.of("toDelete", testEntry, "day", testDay, "time", testTime, "temporary", "ja");
    var intent = this.getMockIntent(this.createSlots(testMap));
    String values = "[{\"value\":\"" + testEntry + "\",\"day\":\"" + testDay + "\",\"time\":\"" + testTime + "\",\"temporary\":true}," +
        "{\"value\":\"Test2\",\"day\":\"2022-12-1\",\"time\":null,\"temporary\":true}]";
    Map<String, Object> data = Map.of("entries", values);
    var persistentData = this.getMockPersistenceAd();
    persistentData.saveAttributes(RequestEnvelope.builder().build(), data);
    var handlerInput = this.getHandlerInput(intent, persistentData);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getOutputSpeech().toString().contains(String.format("Ich habe den temporären Eintrag: %s gelöscht.", testEntry)));

    var databaseValues = persistentData.getAttributes(null).get();
    String actualString = (String) databaseValues.get("entries");
    String expectedString = "[{\"value\":\"Test2\",\"day\":\"2022-12-1\",\"time\":null,\"temporary\":true}]";
    assertEquals(expectedString, actualString);
  }

  @Test
  void deleteNoTimeTemporaryValueTest() {
    var testEntry = "Test";
    var testDay = java.time.LocalDate.now().toString();
    Map<String, String> testMap = Map.of("toDelete", testEntry, "day", testDay, "temporary", "ja");
    var intent = this.getMockIntent(this.createSlots(testMap));
    String values = "[{\"value\":\"" + testEntry + "\",\"day\":\"" + testDay + "\",\"time\":null,\"temporary\":true}," +
        "{\"value\":\"Test2\",\"day\":null,\"time\":null,\"temporary\":false}]";
    Map<String, Object> data = Map.of("entries", values);
    var persistentData = this.getMockPersistenceAd();
    persistentData.saveAttributes(RequestEnvelope.builder().build(), data);
    var handlerInput = this.getHandlerInput(intent, persistentData);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getOutputSpeech().toString().contains(String.format("Ich habe den temporären Eintrag: %s gelöscht.", testEntry)));

    var databaseValues = persistentData.getAttributes(null).get();
    String actualString = (String) databaseValues.get("entries");
    String expectedString = "[{\"value\":\"Test2\",\"day\":null,\"time\":null,\"temporary\":false}]";
    assertEquals(expectedString, actualString);
  }

  @Test
  void deleteNoDayTemporaryValueTest() {
    var testEntry = "Test";
    var testTime = "12:15";
    var testDay = java.time.LocalDate.now().toString();
    Map<String, String> testMap = Map.of("toDelete", testEntry, "time", testTime, "temporary", "ja");
    var intent = this.getMockIntent(this.createSlots(testMap));
    String values = "[{\"value\":\"" + testEntry + "\",\"day\":\"" + testDay + "\",\"time\":\"" + testTime + "\",\"temporary\":true}," +
        "{\"value\":\"Test2\",\"day\":null,\"time\":null,\"temporary\":false}]";
    Map<String, Object> data = Map.of("entries", values);
    var persistentData = this.getMockPersistenceAd();
    persistentData.saveAttributes(RequestEnvelope.builder().build(), data);
    var handlerInput = this.getHandlerInput(intent, persistentData);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getOutputSpeech().toString().contains(String.format("Ich habe den temporären Eintrag: %s gelöscht.", testEntry)));

    var databaseValues = persistentData.getAttributes(null).get();
    String actualString = (String) databaseValues.get("entries");
    String expectedString = "[{\"value\":\"Test2\",\"day\":null,\"time\":null,\"temporary\":false}]";
    assertEquals(expectedString, actualString);
  }

  @Test
  void noValueFoundTest() {
    Map<String, String> testMap = Map.of("toDelete", "Test2", "day", "2021-12-2", "temporary", "Ja");
    var intent = this.getMockIntent(this.createSlots(testMap));
    String values = "[{\"value\":\"Test2\",\"day\":null,\"time\":null,\"temporary\":false}]";
    Map<String, Object> data = Map.of("entries", values);
    var persistentData = this.getMockPersistenceAd();
    persistentData.saveAttributes(RequestEnvelope.builder().build(), data);
    var handlerInput = this.getHandlerInput(intent, persistentData);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getOutputSpeech().toString().contains("Die Erinnerung konnte nicht gefunden werden."));

    var databaseValues = persistentData.getAttributes(null).get();
    String actualString = (String) databaseValues.get("entries");
    String expectedString = "[{\"value\":\"Test2\",\"day\":null,\"time\":null,\"temporary\":false}]";
    assertEquals(expectedString, actualString);
  }

  @Test
  void databaseErrorTest() {
    var testEntry = "Test";
    Map<String, String> testMap = Map.of("toDelete", testEntry, "temporary", "Ja");
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
  void deleteDayNullTimeNullTempTrue() {
    var testEntry = "Test";
    var testDay = java.time.LocalDate.now().toString();
    Map<String, String> testMap = Map.of("toDelete", testEntry,"temporary", "ja");
    var intent = this.getMockIntent(this.createSlots(testMap));
    String values = "[{\"value\":\"" + testEntry + "\",\"day\":\"" + testDay + "\",\"time\":null,\"temporary\":true}," +
          "{\"value\":\"Test2\",\"day\":null,\"time\":null,\"temporary\":false}]";
    Map<String, Object> data = Map.of("entries", values);
    var persistentData = this.getMockPersistenceAd();
    persistentData.saveAttributes(RequestEnvelope.builder().build(), data);
    var handlerInput = this.getHandlerInput(intent, persistentData);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getOutputSpeech().toString().contains(String.format("Ich habe den temporären Eintrag: %s gelöscht.", testEntry)));

    var databaseValues = persistentData.getAttributes(null).get();
    String actualString = (String) databaseValues.get("entries");
    String expectedString = "[{\"value\":\"Test2\",\"day\":null,\"time\":null,\"temporary\":false}]";
    assertEquals(expectedString, actualString);
  }

  @Test
  void deleteDayNullTimeNotNullTempFalse() {
    var testEntry = "Test";
    var testTime = "12:15";
    var testDay = java.time.LocalDate.now().toString();
    Map<String, String> testMap = Map.of("toDelete", testEntry, "time", testTime, "temporary", "nein");
    var intent = this.getMockIntent(this.createSlots(testMap));
    String values = "[{\"value\":\"" + testEntry + "\",\"day\":\"" + testDay + "\",\"time\":\"" + testTime + "\",\"temporary\":false}," +
          "{\"value\":\"Test2\",\"day\":null,\"time\":null,\"temporary\":false}]";
    Map<String, Object> data = Map.of("entries", values);
    var persistentData = this.getMockPersistenceAd();
    persistentData.saveAttributes(RequestEnvelope.builder().build(), data);
    var handlerInput = this.getHandlerInput(intent, persistentData);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getOutputSpeech().toString().contains(String.format("Ich habe den wiederkehrenden Eintrag: %s gelöscht.", testEntry)));

    var databaseValues = persistentData.getAttributes(null).get();
    String actualString = (String) databaseValues.get("entries");
    String expectedString = "[{\"value\":\"Test2\",\"day\":null,\"time\":null,\"temporary\":false}]";
    assertEquals(expectedString, actualString);
  }
}
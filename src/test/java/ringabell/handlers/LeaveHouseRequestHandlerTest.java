package ringabell.handlers;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.*;
import com.amazon.ask.model.interfaces.geolocation.Coordinate;
import com.amazon.ask.model.interfaces.geolocation.GeolocationInterface;
import com.amazon.ask.model.interfaces.geolocation.GeolocationState;
import com.amazon.ask.model.interfaces.system.SystemState;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * @author Anonymous Student
 */
class LeaveHouseRequestHandlerTest extends BaseHandlerTest {

  @Override
  String getName() {
    return "LeaveIntent";
  }

  @Override
  RequestHandler getRequestHandler() {
    return new LeaveHouseRequestHandler();
  }

  HandlerInput getHandlerInputWithCoordinates(Intent intent, Coordinate coordinate, boolean hasPersission) {
    var intentRequest = IntentRequest.builder().withIntent(intent).build();
    var permissionValue = hasPersission ? PermissionStatus.GRANTED : PermissionStatus.DENIED;
    var scope = Scope.builder().withStatus(permissionValue).build();
    Map<String, Scope> permission = Map.of("alexa::devices:all:geolocation:read", scope);

    var requestEnvelope = RequestEnvelope.builder()
        .withRequest(intentRequest).withContext(Context.builder()
            .withSystem(SystemState.builder().withDevice(
                Device.builder()
                    .withSupportedInterfaces(
                        SupportedInterfaces.builder()
                            .withGeolocation(
                                GeolocationInterface.builder().build())
                            .build())
                    .build())
                .build())
            .withGeolocation(
                GeolocationState.builder()
                    .withCoordinate(coordinate).build()
            )
            .build())
        .withSession(Session.builder()
            .withUser(User.builder()
                .withPermissions(Permissions.builder()
                    .withScopes(permission).build()).build()).build())
        .build();

    return HandlerInput.builder()
        .withPersistenceAdapter(this.getMockPersistenceAd())
        .withRequestEnvelope(requestEnvelope).build();
  }

  @Test
  void noValueTest() {
    var intent = this.getMockIntent();
    var handlerInput = this.getHandlerInput(intent);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getOutputSpeech().toString().contains("Du hast heute keine Erinnerungen."));
  }

  @Test
  void onlyPermanentValuesTest() {
    var intent = this.getMockIntent();
    var persistentData = this.getMockPersistenceAd();
    String values = "[{\"value\":\"Test1\",\"day\":null,\"time\":null,\"temporary\":false}," +
        "{\"value\":\"Test2\",\"day\":null,\"time\":null,\"temporary\":false}]";
    Map<String, Object> data = Map.of("entries", values);
    persistentData.saveAttributes(RequestEnvelope.builder().build(), data);
    var handlerInput = this.getHandlerInput(intent, persistentData);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getDirectives().get(0).toString().contains("{primaryText=Test1}, {primaryText=Test2}"));
    assertTrue(response.get().getOutputSpeech().toString().contains("Test1"));
    assertTrue(response.get().getOutputSpeech().toString().contains("Test2"));
  }

  @Test
  void onlyRecurringValuesTest() {
    var intent = this.getMockIntent();
    var persistentData = this.getMockPersistenceAd();
    var today = java.time.LocalDate.now().toString();
    var inTenMinutes = java.time.LocalTime.now().plusMinutes(10);
    var inTenMinutesString = inTenMinutes.format(DateTimeFormatter.ofPattern("HH:mm"));
    String values = "[{\"value\":\"Test1\",\"day\":\"" + today + "\",\"time\":\"" + inTenMinutesString + "\",\"temporary\":false}," +
        "{\"value\":\"Test2\",\"day\":\"" + today + "\",\"time\":null,\"temporary\":false}]";
    Map<String, Object> data = Map.of("entries", values);
    persistentData.saveAttributes(RequestEnvelope.builder().build(), data);
    var handlerInput = this.getHandlerInput(intent, persistentData);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getDirectives().get(0).toString().contains("{primaryText=Test1, secondaryText=um " + inTenMinutesString + " Uhr}, {primaryText=Test2}"));
    assertTrue(response.get().getOutputSpeech().toString().contains("um " + inTenMinutesString + " Uhr: Test1"));
    assertTrue(response.get().getOutputSpeech().toString().contains("Test2"));
  }

  @Test
  void onlyTemporaryValuesTest() {
    var intent = this.getMockIntent();
    var persistentData = this.getMockPersistenceAd();
    var today = java.time.LocalDate.now().toString();
    var inTenMinutes = java.time.LocalTime.now().plusMinutes(10);
    var inTenMinutesString = inTenMinutes.format(DateTimeFormatter.ofPattern("HH:mm"));
    String values = "[{\"value\":\"Test1\",\"day\":\"" + today + "\",\"time\":\"" + inTenMinutesString + "\",\"temporary\":true}]";
    Map<String, Object> data = Map.of("entries", values);
    persistentData.saveAttributes(RequestEnvelope.builder().build(), data);
    var handlerInput = this.getHandlerInput(intent, persistentData);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getDirectives().get(0).toString().contains("{primaryText=Test1, secondaryText=um " + inTenMinutesString + " Uhr}"));
    assertTrue(response.get().getOutputSpeech().toString().contains("um " + inTenMinutesString + " Uhr: Test1"));
  }

  @Test
  void otherDateTest() {
    var intent = this.getMockIntent();
    var persistentData = this.getMockPersistenceAd();
    var testDate = "2222-10-12";
    String values = "[{\"value\":\"Test1\",\"day\":\"" + testDate + "\",\"time\":\"12:15\",\"temporary\":true}," +
        "{\"value\":\"Test2\",\"day\":\"" + testDate + "\",\"time\":\"13:00\",\"temporary\":false}]";
    Map<String, Object> data = Map.of("entries", values);
    persistentData.saveAttributes(RequestEnvelope.builder().build(), data);
    var handlerInput = this.getHandlerInput(intent, persistentData);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getDirectives().get(0).toString().contains("{primaryText=Keine Erinnerungen, secondaryText=Auf wiedersehen}"));
    assertTrue(response.get().getOutputSpeech().toString().contains("Du hast heute keine Erinnerungen."));
  }

  @Test
  void oldValueDelete() {
    var intent = this.getMockIntent();
    var persistentData = this.getMockPersistenceAd();
    var testDate = "2021-01-12";
    String values = "[{\"value\":\"Test1\",\"day\":\"" + testDate + "\",\"time\":\"12:15\",\"temporary\":true}]";
    Map<String, Object> data = Map.of("entries", values);
    persistentData.saveAttributes(RequestEnvelope.builder().build(), data);
    var handlerInput = this.getHandlerInput(intent, persistentData);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getDirectives().get(0).toString().contains("{primaryText=Keine Erinnerungen, secondaryText=Auf wiedersehen}"));
    assertTrue(response.get().getOutputSpeech().toString().contains("Du hast heute keine Erinnerungen."));
    var databaseValues = persistentData.getAttributes(null).get();
    String actualString = (String) databaseValues.get("entries");
    assertNull(actualString);
  }

  @Test
  void oldRecurringValueDelete() {
    var intent = this.getMockIntent();
    var persistentData = this.getMockPersistenceAd();
    var testDate = java.time.LocalDate.now().minusWeeks(1).toString();
    var inTenMinutes = java.time.LocalTime.now().plusMinutes(10);
    var inTenMinutesString = inTenMinutes.format(DateTimeFormatter.ofPattern("HH:mm"));
    String values = "[{\"value\":\"Test1\",\"day\":\"" + testDate + "\",\"time\":\"" + inTenMinutesString + "\",\"temporary\":false}]";
    Map<String, Object> data = Map.of("entries", values);
    persistentData.saveAttributes(RequestEnvelope.builder().build(), data);
    var handlerInput = this.getHandlerInput(intent, persistentData);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getDirectives().get(0).toString().contains("{primaryText=Test1, secondaryText=um " + inTenMinutesString + " Uhr}"));
    assertTrue(response.get().getOutputSpeech().toString().contains("um " + inTenMinutesString + " Uhr: Test1"));
  }

  @Test
  void onlyPermanentWithoutAPL() {
    var intent = this.getMockIntent();
    var persistentData = this.getMockPersistenceAd();
    String values = "[{\"value\":\"Test1\",\"day\":null,\"time\":null,\"temporary\":false}," +
        "{\"value\":\"Test2\",\"day\":null,\"time\":null,\"temporary\":false}]";
    Map<String, Object> data = Map.of("entries", values);
    persistentData.saveAttributes(RequestEnvelope.builder().build(), data);
    var handlerInput = this.getHandlerInputWithoutAPL(intent, persistentData);


    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertSame(0, response.get().getDirectives().size());
    assertTrue(response.get().getOutputSpeech().toString().contains("Test1"));
    assertTrue(response.get().getOutputSpeech().toString().contains("Test2"));
  }

  @Test
  void onlyTemporaryValuesTestWithoutDateAndTime() {
    var intent = this.getMockIntent();
    var persistentData = this.getMockPersistenceAd();
    String values = "[{\"value\":\"Test1\",\"day\":null,\"time\":null,\"temporary\":true}]";
    Map<String, Object> data = Map.of("entries", values);
    persistentData.saveAttributes(RequestEnvelope.builder().build(), data);
    var handlerInput = this.getHandlerInput(intent, persistentData);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getDirectives().get(0).toString().contains("{primaryText=Test1}"));
    assertTrue(response.get().getOutputSpeech().toString().contains("Zudem hast du folgende temporären Erinnerungen: Test1."));
  }

  @Test
  void weatherTestGranted() {
    var intent = this.getMockIntent();
    Coordinate testLocation = Coordinate.builder().withLatitudeInDegrees(48.137172)
        .withLongitudeInDegrees(11.575242).build();
    var handlerInput = this.getHandlerInputWithCoordinates(intent, testLocation, true);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getOutputSpeech().toString().contains("Du hast heute keine Erinnerungen."));
  }

  @Test
  void weatherTestNotGranted() {
    var intent = this.getMockIntent();
    Coordinate testLocation = Coordinate.builder().withLatitudeInDegrees(48.137172)
        .withLongitudeInDegrees(11.575242).build();
    var handlerInput = this.getHandlerInputWithCoordinates(intent, testLocation, false);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getOutputSpeech().toString().contains("Du hast heute keine Erinnerungen."));
  }

  @Test
  void databaseErrorTest() {
    var intent = this.getMockIntent();
    var persistentData = this.getMockPersistenceAd();
    var testDate = java.time.LocalDate.now().minusWeeks(1).toString();
    var inTenMinutes = java.time.LocalTime.now().plusMinutes(10);
    var inTenMinutesString = inTenMinutes.format(DateTimeFormatter.ofPattern("HH:mm"));
    String values = "[{\"value\":\"Test1\",\"day\":[\"" + testDate + "\"],\"time\":\"" + inTenMinutesString + "\",\"temporary\":false}]";
    Map<String, Object> data = Map.of("entries", values);
    persistentData.saveAttributes(RequestEnvelope.builder().build(), data);
    var handlerInput = this.getHandlerInput(intent, persistentData);

    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getOutputSpeech().toString().contains("Fehler beim Laden der Datenbank oder beim Löschen eines Eintrages."));
  }

}
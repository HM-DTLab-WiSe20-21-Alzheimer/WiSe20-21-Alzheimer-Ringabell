package ringabell.handlers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

import com.amazon.ask.attributes.persistence.PersistenceAdapter;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.exception.PersistenceException;
import com.amazon.ask.model.*;
import com.amazon.ask.model.interfaces.alexa.presentation.apl.AlexaPresentationAplInterface;
import com.amazon.ask.model.interfaces.alexa.presentation.apl.RenderedDocumentState;
import com.amazon.ask.model.interfaces.alexa.presentation.apl.Runtime;
import com.amazon.ask.model.interfaces.geolocation.Coordinate;
import com.amazon.ask.model.interfaces.geolocation.GeolocationState;
import com.amazon.ask.model.interfaces.system.SystemState;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Anonymous Student
 */
abstract class BaseHandlerTest {

  abstract String getName();

  abstract RequestHandler getRequestHandler();

  Intent getMockIntent() {
    var intent = mock(Intent.class);
    when(intent.getName()).thenReturn(this.getName());

    return intent;
  }

  Intent getMockIntent(Map<String, Slot> slots) {
    var intent = this.getMockIntent();
    when(intent.getSlots()).thenReturn(slots);

    return intent;
  }

  PersistenceAdapter getMockPersistenceAd() {
    return new PersistenceAdapterMock();
  }

  HandlerInput baseHandlerInput(Intent intent, PersistenceAdapter persistenceAdapter, boolean supportAPL) {
    var intentRequest = IntentRequest.builder().withIntent(intent).build();
    AlexaPresentationAplInterface aplInterface;
    if (supportAPL) {
      aplInterface = AlexaPresentationAplInterface.builder().build();
    } else {
      aplInterface = null;
    }
    var requestEnvelope = RequestEnvelope.builder()
        .withRequest(intentRequest).withContext(Context.builder()
            .withAlexaPresentationAPL(RenderedDocumentState.builder()
                .build()).withSystem(SystemState.builder()
                .withDevice(Device.builder()
                    .withSupportedInterfaces(SupportedInterfaces.builder().
                        withAlexaPresentationAPL(aplInterface)
                        .build())
                    .build())
                .build())
            .build())
        .build();

    return HandlerInput.builder()
        .withPersistenceAdapter(persistenceAdapter)
        .withRequestEnvelope(requestEnvelope).build();
  }

  HandlerInput getHandlerInputWithoutAPL(Intent intent, PersistenceAdapter persistenceAdapter) {
    return baseHandlerInput(intent, persistenceAdapter, false);
  }

  HandlerInput getHandlerInput(Intent intent, PersistenceAdapter persistenceAdapter) {
    return baseHandlerInput(intent, persistenceAdapter, true);
  }

  HandlerInput getHandlerInput(Intent intent) {
    return getHandlerInput(intent, this.getMockPersistenceAd());
  }

  Map<String, Slot> createSlots(Map<String, String> keyValues) {
    Map<String, Slot> slots = new HashMap<>();

    for (Map.Entry<String, String> entry : keyValues.entrySet()) {
      slots.put(entry.getKey(), Slot.builder().withValue(entry.getValue()).build());
    }

    return slots;
  }

  Map<String, Slot> createSlots(String key, String value) {
    return this.createSlots(Map.of(key, value));
  }

  @Test
  void canHandle() {
    var intent = this.getMockIntent();
    var handlerInput = this.getHandlerInput(intent);
    assertTrue(this.getRequestHandler().canHandle(handlerInput));
    verify(intent, times(1)).getName();
  }

  @Test
  void testCantHandleRandomRequest() {
    var intent = Intent.builder().withName("TestIntent").build();
    var handlerInput = this.getHandlerInput(intent);
    assertFalse(this.getRequestHandler().canHandle(handlerInput));
  }

  private static class PersistenceAdapterMock implements PersistenceAdapter {

    private final Map<String, Object> attributes;

    PersistenceAdapterMock() {
      this.attributes = new HashMap<>();
    }

    @Override
    public Optional<Map<String, Object>> getAttributes(RequestEnvelope requestEnvelope) throws PersistenceException {
      return Optional.of(attributes);
    }

    @Override
    public void saveAttributes(RequestEnvelope requestEnvelope, Map<String, Object> map) throws PersistenceException {
      this.attributes.putAll(map);
    }

    @Override
    public void deleteAttributes(RequestEnvelope requestEnvelope) throws PersistenceException {

    }
  }
}

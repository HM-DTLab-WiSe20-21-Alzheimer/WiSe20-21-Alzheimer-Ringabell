package ringabell.handlers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.Response;
import org.junit.jupiter.api.Test;
/**
 * @author Anonymous Student
 */
class CustomLaunchRequestHandlerTest extends BaseHandlerTest {

  @Override
  String getName() {
    return null;
  }

  @Override
  RequestHandler getRequestHandler() {
    return new CustomLaunchRequestHandler();
  }

  @Override
  @Test
  void canHandle() {
    LaunchRequest launchRequest = LaunchRequest.builder().build();
    RequestEnvelope requestEnvelope = RequestEnvelope.builder().withRequest(launchRequest).build();
    HandlerInput handlerInput = HandlerInput.builder().withRequestEnvelope(requestEnvelope).build();
    assertTrue(this.getRequestHandler().canHandle(handlerInput));
  }

  @Test
  void handleTest() {
    LaunchRequest launchRequest = LaunchRequest.builder().build();
    RequestEnvelope requestEnvelope = RequestEnvelope.builder().withRequest(launchRequest).build();
    HandlerInput handlerInput = HandlerInput.builder().withRequestEnvelope(requestEnvelope).build();
    var response = getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getOutputSpeech().toString().contains("Dieser Skill merkt sich Dinge und erinnert sie daran beim Verlassen des Hauses."));
  }
}
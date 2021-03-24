package ringabell.handlers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.RequestEnvelope;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Optional;
/**
 * @author Anonymous Student
 */
class FallbackIntentHandlerTest extends BaseHandlerTest {

  @Override
  String getName() {
    return "AMAZON.FallbackIntent";
  }

  @Override
  RequestHandler getRequestHandler() {
    return new FallbackIntentHandler();
  }

  @Test
  void handleTest() {
    var intent = this.getMockIntent();
    var handlerInput = this.getHandlerInput(intent);
    var response = this.getRequestHandler().handle(handlerInput);

    assertTrue(response.isPresent());
    assertTrue(response.get().getOutputSpeech().toString().contains("Der Ring-a-bell Skill hat dich leider nicht verstanden."));
  }
}
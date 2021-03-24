package ringabell.handlers;

import static com.amazon.ask.request.Predicates.intentName;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import java.util.Optional;
/**
 * FallbackIntentHandler.
 *
 * @author Anonymous Student
 */

public class FallbackIntentHandler implements RequestHandler {

  @Override
  public boolean canHandle(final HandlerInput handlerInput) {
    return handlerInput.matches(intentName("AMAZON.FallbackIntent"));
  }

  @Override
  public Optional<Response> handle(final HandlerInput handlerInput) {
    return handlerInput.getResponseBuilder()
        .withSpeech("Der Ring-a-bell Skill hat dich leider nicht verstanden.")
        .build();
  }
}


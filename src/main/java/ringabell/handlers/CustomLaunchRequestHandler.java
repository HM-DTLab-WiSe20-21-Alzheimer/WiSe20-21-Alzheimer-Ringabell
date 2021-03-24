package ringabell.handlers;

import static com.amazon.ask.request.Predicates.requestType;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.LaunchRequestHandler;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.Response;
import java.util.Optional;
/**
 * CustomLaunchRequestHandler.
 *
 * @author Anonymous Student
 */

public class CustomLaunchRequestHandler implements LaunchRequestHandler {

  @Override
  public boolean canHandle(HandlerInput input, LaunchRequest launchRequest) {
    return input.matches(requestType(LaunchRequest.class));
  }

  @Override
  public Optional<Response> handle(HandlerInput input, LaunchRequest launchRequest) {
    return input.getResponseBuilder()
        .withSpeech(
            "Dieser Skill merkt sich Dinge und erinnert sie daran beim Verlassen des Hauses.")
        .build();
  }
}
package ringabell.handlers;

import static com.amazon.ask.request.Predicates.intentName;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import com.amazon.ask.response.ResponseBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import ringabell.handlers.helper.EntryRepository;

/**
 * StoreValueRequestHandler.
 *
 * @author Anonymous Student
 */

public class StoreValueRequestHandler extends BaseDatastoreHandler {

  @Override
  public boolean canHandle(HandlerInput handlerInput) {
    return handlerInput.matches(intentName("StoreIntent"));
  }

  @Override
  public Optional<Response> handle(HandlerInput handlerInput) {
    var entryRepository = new EntryRepository(handlerInput.getAttributesManager());
    var responseBuilder = handlerInput.getResponseBuilder();
    var optStoreEntry = this.createEntry(handlerInput, "toStore");

    if (optStoreEntry.isEmpty()) {
      return responseBuilder
          .withSpeech("Es ist ein Fehler aufgetreten. Kein Wert.")
          .build();
    }

    var storeEntry = optStoreEntry.get();

    // Check if the value is today and after the current time.
    Optional<Response> checkResult = checkIfIsRealTime(storeEntry.getTime(), storeEntry.getDay());
    if (checkResult.isPresent()) {
      return checkResult;
    }

    var isNewEntry = false;

    try {
      isNewEntry = entryRepository.addEntry(storeEntry);
    } catch (JsonProcessingException e) {
      return responseBuilder
            .withSpeech("Es ist ein Datenbank Fehler aufgetreten.")
            .build();
    }

    var storeSpeech = "Das habe ich mir schonmal gemerkt.";
    if (isNewEntry) {
      if (storeEntry.getTemporary()) {
        storeSpeech = "Ich habe mir den temporären Eintrag: " + storeEntry.getValue() + " gemerkt.";
      } else {
        storeSpeech = "Ich habe mir den wiederkehrenden Eintrag: " + storeEntry.getValue()
            + " gemerkt.";
      }
    }

    return responseBuilder
          .withSpeech(storeSpeech)
          .build();
  }


  private Optional<Response> checkIfIsRealTime(String time, String day) {
    var thisDay = java.time.LocalDate.now();
    var now = java.time.LocalTime.now();

    if (day != null && (LocalDate.parse(day).isBefore(thisDay)
        || (LocalDate.parse(day).isEqual(thisDay)
        && time != null && LocalTime.parse(time).isBefore(now)))) {
      return new ResponseBuilder()
          .withSpeech("Es können nur Erinnerungen erstellt werden, welche in der Zukunft liegen.")
          .build();
    }

    return Optional.empty();
  }
}





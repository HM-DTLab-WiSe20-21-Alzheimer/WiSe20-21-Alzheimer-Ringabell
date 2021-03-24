package ringabell.handlers;

import static com.amazon.ask.request.Predicates.intentName;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Optional;
import ringabell.handlers.helper.Entry;
import ringabell.handlers.helper.EntryRepository;

/**
 * DeleteValueRequestHandler.
 *
 * @author Anonymous Student
 */

public class DeleteValueRequestHandler extends BaseDatastoreHandler {

  @Override
  public boolean canHandle(HandlerInput handlerInput) {
    return handlerInput.matches(intentName("DeleteIntent"));
  }

  @Override
  public Optional<Response> handle(HandlerInput handlerInput) {
    var entryRepository = new EntryRepository(handlerInput.getAttributesManager());
    var responseBuilder = handlerInput.getResponseBuilder();
    var optDeleteEntry = this.createEntry(handlerInput, "toDelete");

    if (optDeleteEntry.isEmpty()) {
      return responseBuilder
          .withSpeech("Es ist ein Fehler beim Löschen aufgetreten. Kein Wert.")
          .build();
    }

    boolean isDeleted;
    Entry deleteEntry = optDeleteEntry.get();

    try {
      isDeleted = entryRepository.deleteEntry(deleteEntry);
    } catch (JsonProcessingException e) {
      return responseBuilder
          .withSpeech("Es ist ein Datenbank Fehler aufgetreten.")
          .build();
    }

    String deleteSpeech = "Die Erinnerung konnte nicht gefunden werden.";
    if (isDeleted) {
      if (deleteEntry.getTemporary()) {
        deleteSpeech = "Ich habe den temporären Eintrag: " + deleteEntry.getValue() + " gelöscht.";
      } else {
        deleteSpeech = "Ich habe den wiederkehrenden Eintrag: " + deleteEntry.getValue()
            + " gelöscht.";
      }
    }

    return responseBuilder
        .withSpeech(deleteSpeech)
        .build();
  }
}

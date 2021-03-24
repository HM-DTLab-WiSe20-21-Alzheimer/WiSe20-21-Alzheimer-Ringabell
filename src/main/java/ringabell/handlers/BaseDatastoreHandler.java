package ringabell.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Slot;
import java.util.Optional;
import ringabell.handlers.helper.Entry;

/**
 * DeleteValueRequestHandler.
 *
 * @author Anonymous Student
 */

abstract class BaseDatastoreHandler implements RequestHandler {

  protected Optional<Entry> createEntry(HandlerInput handlerInput, String valueSlotName) {
    IntentRequest intent = (IntentRequest) handlerInput.getRequestEnvelope().getRequest();
    Slot valueSlot = intent.getIntent().getSlots().get(valueSlotName);
    Slot tempSlot = intent.getIntent().getSlots().get("temporary");
    if (valueSlot == null || tempSlot == null) {
      return Optional.empty();
    }

    String value = valueSlot.getValue();
    Slot timeSlot = intent.getIntent().getSlots().get("time");
    String time = timeSlot == null ? null : timeSlot.getValue();
    Slot daySlot = intent.getIntent().getSlots().get("day");
    String day = daySlot == null ? null : daySlot.getValue();
    boolean temp = tempSlot.getValue().equals("ja");

    if (day == null && (time != null || temp)) {
      day = java.time.LocalDate.now().toString();
    }

    return Optional.of(new Entry(value, day, time, temp));
  }
}

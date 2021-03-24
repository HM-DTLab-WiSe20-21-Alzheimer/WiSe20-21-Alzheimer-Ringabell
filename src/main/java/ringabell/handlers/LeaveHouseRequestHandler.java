package ringabell.handlers;

import static com.amazon.ask.request.Predicates.intentName;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.exception.AskSdkException;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.interfaces.alexa.presentation.apl.RenderDocumentDirective;
import com.amazon.ask.request.RequestHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import ringabell.handlers.helper.Entry;
import ringabell.handlers.helper.EntryRepository;
import ringabell.handlers.helper.WeatherHandler;

/**
 * LeaveHouseRequestHandler.
 *
 * @author Anonymous Student
 */

public class LeaveHouseRequestHandler implements RequestHandler {

  @Override
  public boolean canHandle(HandlerInput handlerInput) {
    return handlerInput.matches(intentName("LeaveIntent"));
  }

  @Override
  public Optional<Response> handle(HandlerInput handlerInput) {
    var entryRepository = new EntryRepository(handlerInput.getAttributesManager());
    var responseBuilder = handlerInput.getResponseBuilder();

    List<Entry> entryList;
    String speechText;
    Map<String, Object> thingsToDisplay = new HashMap<>();
    try {
      entryList = entryRepository.getDateEntries();
      speechText = createResponseString(entryList, thingsToDisplay, handlerInput);
    } catch (JsonProcessingException e) {
      return responseBuilder
            .withSpeech("Fehler beim Laden der Datenbank oder beim Löschen eines Eintrages.")
            .build();
    }

    var weatherHandler = new WeatherHandler();

    // for tests with alexa developer use getTestWeatherResponse()
    String weatherText = weatherHandler.getWeatherResponse(handlerInput);

    if (weatherText != null) {
      speechText += weatherText;
    }

    if (RequestHelper.forHandlerInput(handlerInput)
          .getSupportedInterfaces().getAlexaPresentationAPL() != null) {
      try {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<HashMap<String, Object>> documentMapType = new TypeReference<>() {
        };

        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("leaveFrontend.json");

        Map<String, Object> document = mapper.readValue(
              new File(Objects.requireNonNull(resource).toURI()),
              documentMapType);

        RenderDocumentDirective renderDocumentDirective = RenderDocumentDirective.builder()
              .withDocument(document).putDatasourcesItem("temp", thingsToDisplay).build();

        return responseBuilder
              .addDirective(renderDocumentDirective)
              .withSpeech(speechText)
              .build();
      } catch (URISyntaxException | IOException e) {
        throw new AskSdkException("Unable to read or deserialize the base json document");
      }
    } else {
      return responseBuilder
            .withSpeech(speechText)
            .build();
    }
  }

  private String createResponseString(List<Entry> entryList, Map<String, Object> mapOfPoints,
                                      HandlerInput handlerInput) throws JsonProcessingException {
    var entryRepository = new EntryRepository(handlerInput.getAttributesManager());
    Set<String> permanent = new HashSet<>();
    Set<String> permanentWithTime = new HashSet<>();
    Set<String> tempWithTime = new HashSet<>();
    Set<String> temp = new HashSet<>();

    final String primary = "primaryText";
    final String secondary = "secondaryText";

    List<Map<String, String>> allPoints = new ArrayList<>();

    for (Entry entry : entryList) {
      Map<String, String> mapOfCurrentThing = new HashMap<>();

      if (entry.getDay() != null && entry.getTime() != null) {
        if (entry.getTemporary()) {
          tempWithTime.add(String.format("um %s Uhr: %s", entry.getTime(),
                entry.getValue()));
          entryRepository.deleteEntry(entry);
        } else {
          permanentWithTime.add(String.format("um %s Uhr: %s", entry.getTime(),
                entry.getValue()));
        }
        mapOfCurrentThing.put(primary, entry.getValue());
        mapOfCurrentThing.put(secondary, String.format("um %s Uhr", entry.getTime()));
        allPoints.add(mapOfCurrentThing);

      } else {
        if (entry.getTemporary()) {
          temp.add(entry.getValue());
          entryRepository.deleteEntry(entry);
        } else {
          permanent.add(entry.getValue());
        }
        mapOfCurrentThing.put(primary, entry.getValue());
        allPoints.add(mapOfCurrentThing);
      }
    }

    String responseString = finishStringBuilding(permanent, permanentWithTime,
          tempWithTime, temp, allPoints);

    Map<String, Object> innerMap = new HashMap<>();
    innerMap.put("listItems", allPoints);
    mapOfPoints.put("textListData", innerMap);

    return responseString;
  }

  private String finishStringBuilding(Set<String> permanent, Set<String> permanentWithTime,
                                      Set<String> tempWithTime, Set<String> temp,
                                      List<Map<String, String>> allPoints) {
    String responseString = "";
    final String primary = "primaryText";
    final String secondary = "secondaryText";

    if (permanent.isEmpty() && tempWithTime.isEmpty() && temp.isEmpty()
          && permanentWithTime.isEmpty()) {
      Map<String, String> emptyListOfThings = new HashMap<>();
      emptyListOfThings.put(primary, "Keine Erinnerungen");
      emptyListOfThings.put(secondary, "Auf wiedersehen");
      allPoints.add(emptyListOfThings);
      responseString = "Du hast heute keine Erinnerungen.";
    } else {

      responseString = permanent.isEmpty() ? "" : responseString
            + "Vergiss folgende dauerhaften Erinnerungen nicht: "
            + String.join(", ", permanent)
            + ".";

      if (!permanentWithTime.isEmpty()) {
        responseString = responseString
              + String.format(" Erinnerungen für heute %s",
              String.join(", ", permanentWithTime));
      }
      if (!temp.isEmpty()) {
        responseString = responseString
              + "Zudem hast du folgende temporären Erinnerungen: "
              + String.join(", ", temp) + ".";
      }
      if (!tempWithTime.isEmpty()) {
        responseString = responseString + (String.format(" Erinnerungen für heute %s",
              String.join(", ", tempWithTime)));
      }
    }

    return responseString;
  }
}

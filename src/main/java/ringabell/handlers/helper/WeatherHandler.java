package ringabell.handlers.helper;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.PermissionStatus;
import com.amazon.ask.model.Permissions;
import com.amazon.ask.model.interfaces.geolocation.Coordinate;
import com.amazon.ask.model.interfaces.geolocation.GeolocationState;
import java.util.Optional;
import net.aksingh.owmjapis.api.APIException;
import net.aksingh.owmjapis.core.OWM;
import net.aksingh.owmjapis.model.CurrentWeather;

/**
 * WeatherHandler.
 *
 * @author Anonymous Student
 */

public class WeatherHandler {

  private static final String GEO_PERMISSION = "alexa::devices:all:geolocation:read";

  /**
   * gets the weather response for testing with alexa developer console.
   *
   * @return the response String for the weather in munich.
   */
  public String getTestWeatherResponse() {
    Coordinate testLocation = Coordinate.builder().withLatitudeInDegrees(48.137172)
        .withLongitudeInDegrees(11.575242).build();

    try {
      return getWeatherOnLocation(testLocation);
    } catch (APIException e) {
      return null;
    }
  }

  /**
   * gets the weather string for the device coordinates.
   *
   * @param input the handlerInput for the device coordinates.
   * @return the weather string.
   */
  public String getWeatherResponse(HandlerInput input) {
    if (input.getRequestEnvelope() == null || input.getRequestEnvelope().getContext() == null
        || !deviceSupportsGeolocation(input)) {
      return null;
    }

    if (!hasGeolocationPermission(input)) {
      return null;
    }

    var location = getCurrentLocation(input);
    if (location.isEmpty()) {
      return null;
    }

    try {
      return getWeatherOnLocation(location.get());
    } catch (APIException e) {
      return null;
    }
  }

  private boolean deviceSupportsGeolocation(HandlerInput input) {
    return input.getRequestEnvelope().getContext().getSystem().getDevice().getSupportedInterfaces()
        .getGeolocation() != null;
  }

  private boolean hasGeolocationPermission(HandlerInput input) {
    Permissions permissions = input.getRequestEnvelope().getSession().getUser().getPermissions();
    return permissions != null && permissions.getScopes() != null
        && permissions.getScopes().get(GEO_PERMISSION).getStatus() == PermissionStatus.GRANTED;
  }

  private Optional<Coordinate> getCurrentLocation(HandlerInput input) {
    GeolocationState geo = input.getRequestEnvelope().getContext().getGeolocation();
    return Optional.ofNullable(geo.getCoordinate());
  }

  private String getWeatherOnLocation(Coordinate location) throws APIException {
    // Here can be placed any weather Api.
    var lat = location.getLatitudeInDegrees();
    var lon = location.getLongitudeInDegrees();

    // declaring object of "OWM" class.
    // you have to create in OpenWheater https://openweathermap.org/ a new account with a new apiKey.
    OWM owm = new OWM("enteryourowntokenhere");
    // set unit and language
    var unit = OWM.Unit.METRIC;
    owm.setUnit(unit);
    var lang = OWM.Language.GERMAN;
    owm.setLanguage(lang);

    // getting current weather data
    CurrentWeather cwd = owm.currentWeatherByCoords(lat, lon);

    String responseMessage = null;

    // checking data retrieval was successful or not
    if (cwd.hasRespCode() && cwd.getRespCode() == 200 && cwd.hasWeatherList()) {
      for (var weather : cwd.getWeatherList()) {
        if (weather.hasConditionId() && weather.getConditionId() >= 500
            && weather.getConditionId() <= 531) {
          responseMessage = " Zieh eine Jacke an es regnet.";
        }
      }

      if (responseMessage == null && cwd.hasMainData()
          && cwd.getMainData().hasTemp() && cwd.getMainData().getTemp() < 10) {
        responseMessage = " Zieh dich warm an es hat nur " + Math.round(cwd.getMainData().getTemp())
            + " Grad in " + cwd.getCityName() + ".";
      }
    }

    return responseMessage;
  }
}

package ringabell.handlers;

import com.amazon.ask.SkillStreamHandler;
import com.amazon.ask.Skills;
/**
 * Our SkillStreamHandler.
 *
 * @author Anonymous Student
 */

public class AlexaSkillStreamHandler extends SkillStreamHandler {

  /**
   * Stream Handler for our Alexa Skill.
   * All Request Handlers need to be added here.
   * **/
  public AlexaSkillStreamHandler() {
    super(Skills.standard()
        .addRequestHandlers(new CustomLaunchRequestHandler(),
            new StoreValueRequestHandler(),
            new FallbackIntentHandler(),
            new LeaveHouseRequestHandler(),
            new DeleteValueRequestHandler())
        .withTableName("RingABellData")
        .withAutoCreateTable(true)
        .build());
  }
}

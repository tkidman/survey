package tkidman.tracar.domain;

import java.util.ArrayList;
import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class SurveyTest {

   public void testSurvey() {
       Observation observation7Min = new Observation(1, 7 * Survey.MILLIS_IN_MINUTE, true, 100, 1);
       Observation observation32Min = new Observation(1, 32 * Survey.MILLIS_IN_MINUTE, true, 100, 1);
       ArrayList<Observation> observations = new ArrayList<>(Arrays.asList(observation7Min, observation32Min));
       Survey survey = new Survey(observations);
       ArrayList<ObservationGroup> groups = survey.getGroups(ObservationGroup.FiveMinuteObservationGroup.class);

       // we should have 7 five minute groups, with 1 observation in the 2nd and 7th groups
       Assert.assertEquals(groups.size(), 7);
       for (int i = 0; i < groups.size(); i++) {
           if (i == 1 || i == 6) {
               Assert.assertTrue(groups.get(i).getCarCount() == 1);
           } else {
               Assert.assertTrue(groups.get(i).getCarCount() == 0);
           }
       }

       groups = survey.getGroups(ObservationGroup.FifteenMinuteObservationGroup.class);
       // we should have 3 fifteen minute groups, with 1 observation in the 1nd and 3rd groups
       Assert.assertEquals(groups.size(), 3);
       for (int i = 0; i < groups.size(); i++) {
           if (i == 1) {
               Assert.assertTrue(groups.get(i).getCarCount() == 0);
           } else {
               Assert.assertTrue(groups.get(i).getCarCount() == 1);
           }
       }
   }
}

package tkidman.tracar.app;

import java.util.ArrayList;
import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

import tkidman.tracar.domain.Observation;
import tkidman.tracar.domain.ObservationGroup;
import tkidman.tracar.domain.Survey;

@Test
public class TracarTest {

    public void testLoadSurvey() {
        // the second direction A car is on the next day as its time is less than the previous time
        String[] lines = {"A100", "A200", "A500", "B501", "A600", "B601", "A100", "A200"};

        Survey survey = new Tracar().loadSurvey(Arrays.asList(lines).stream());
        ArrayList<ObservationGroup> observationGroups = survey.getObservationGroups(ObservationGroup.ObservationGroupType.DAY);
        Assert.assertEquals(2, observationGroups.size());

        // on the first day there should be 2 observations, 1 in direction A, 1 in direction B
        ObservationGroup firstDay = observationGroups.get(0);
        Assert.assertEquals(2, firstDay.getCarCount());
        Assert.assertEquals(1, firstDay.getCarCountA());
        Assert.assertEquals(1, firstDay.getCarCountB());

        // on the second day there should be 1 observation, 1 in direction A, 0 in direction B
        ObservationGroup secondDay = observationGroups.get(1);
        Assert.assertEquals(1, secondDay.getCarCount());
        Assert.assertEquals(1, secondDay.getCarCountA());
        Assert.assertEquals(0, secondDay.getCarCountB());
    }

    public void testSpeedAndDistance() {
        Observation previous = new Observation(0, 0, true, 60, 0);
        final Tracar.RawObservation rawObservation = new Tracar.RawObservation();
        rawObservation.firstA = "A" + Integer.toString(Survey.MILLIS_IN_HOUR);
        rawObservation.secondA = "A" + Integer.toString(Survey.MILLIS_IN_HOUR + 150); // 150 millis time difference over 2.5 metres should give a speed of 60 kmph.

        Observation observation = rawObservation.createObservation(previous);
        Assert.assertEquals(observation.getSpeedInKmph(), 60.0);
        // 1 hour behind the previous observation, so we should travel 60000 metres at 60 kmph.
        Assert.assertEquals(observation.getMetresBehind(), 60000.0);
    }
}

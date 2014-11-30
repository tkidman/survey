package tkidman.tracar.domain;

import java.util.ArrayList;
import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class SurveyTest {

    public void testSurvey() {
        Observation observation7Min = new Observation(0, 7 * Survey.MILLIS_IN_MINUTE, true, 100, 1);
        Observation observation32Min = new Observation(0, 32 * Survey.MILLIS_IN_MINUTE, true, 100, 1);
        ArrayList<Observation> observations = new ArrayList<>(Arrays.asList(observation7Min, observation32Min));
        Survey survey = new Survey(observations);
        ArrayList<ObservationGroup> groups = survey.getObservationGroups(ObservationGroup.ObservationGroupType.FIFTEEN_MINUTES);

        // we should have 3 fifteen minute groups, with 1 observation in the 1st and 3rd groups.
        Assert.assertEquals(groups.size(), 3);
        for (int i = 1; i < groups.size(); i++) {
            if (i == 1) {
                Assert.assertTrue(groups.get(i).getCarCount() == 0);
            } else {
                Assert.assertTrue(groups.get(i).getCarCount() == 1);
            }
        }

        groups = survey.getObservationGroups(ObservationGroup.ObservationGroupType.TWENTY_MINUTES);
        // we should have 2 twenty minute groups, with 1 observation in each
        Assert.assertEquals(groups.size(), 2);
        for (ObservationGroup group : groups) {
            Assert.assertTrue(group.getCarCount() == 1);
        }
    }

    public void testAverageAcrossDays() {
        Observation observation1 = new Observation(0, 10 * Survey.MILLIS_IN_MINUTE, true, 100, 1);
        Observation observation2 = new Observation(1, 10 * Survey.MILLIS_IN_MINUTE, true, 50, 3);
        ArrayList<Observation> observations = new ArrayList<>(Arrays.asList(observation1, observation2));
        Survey survey = new Survey(observations);
        ArrayList<ObservationGroup> groups = survey.getAverageOverDaysObservationGroups(ObservationGroup.ObservationGroupType.ONE_HOUR);

        ObservationGroup firstHourAverage = groups.get(0);
        Assert.assertEquals(firstHourAverage.getCarCount(), 1);
        Assert.assertEquals(firstHourAverage.getCarCountA(), 1);
        Assert.assertEquals(firstHourAverage.getCarCountB(), 0);
        Assert.assertEquals(firstHourAverage.getAverageSpeed(), 75.0);
        Assert.assertEquals(firstHourAverage.getAverageMetresBehind(), 2.0);
    }

    public void testMorningEvening() {
        Observation observationMorning = new Observation(0, 8 * Survey.MILLIS_IN_HOUR, true, 100, 1);
        Observation observationInvalid = new Observation(0, 3 * Survey.MILLIS_IN_HOUR, true, 100, 1);
        Observation observationEvening = new Observation(0, 18 * Survey.MILLIS_IN_HOUR, false, 100, 1);
        ArrayList<Observation> observations = new ArrayList<>(Arrays.asList(observationMorning, observationInvalid, observationEvening));
        Survey survey = new Survey(observations);

        final ArrayList<ObservationGroup> morningEveningObservationGroups = survey.getMorningEveningObservationGroups();
        Assert.assertEquals(morningEveningObservationGroups.size(), 2);

        final ObservationGroup morningGroup = morningEveningObservationGroups.get(0);
        Assert.assertEquals(morningGroup.getName(), "Morning");
        Assert.assertEquals(morningGroup.getCarCountA(), 1);

        final ObservationGroup eveningGroup = morningEveningObservationGroups.get(1);
        Assert.assertEquals(eveningGroup.getName(), "Evening");
        Assert.assertEquals(eveningGroup.getCarCountB(), 1);
    }

    public void testPeaks(){
        // in first 15 minute period
        Observation observation1 = new Observation(0, 10 * Survey.MILLIS_IN_MINUTE, true, 100, 1);
        // in second 15 minute period
        Observation observation2 = new Observation(0, 20 * Survey.MILLIS_IN_MINUTE, false, 50, 3);
        Observation observation3 = new Observation(0, 25 * Survey.MILLIS_IN_MINUTE, false, 50, 3);

        ArrayList<Observation> observations = new ArrayList<>(Arrays.asList(observation1, observation2, observation3));
        Survey survey = new Survey(observations);

        ArrayList<ObservationGroup> peaks = survey.getPeaks(ObservationGroup.ObservationGroupType.FIFTEEN_MINUTES, Survey.DirectionOption.BOTH);
        // Our peak should be the second 15 minute period
        Assert.assertEquals(peaks.size(), 1);
        ObservationGroup peak = peaks.get(0);
        Assert.assertEquals(peak.getCarCount(), 2);
        Assert.assertEquals(peak.getPeriodStartMillis(), 15 * Survey.MILLIS_IN_MINUTE);

        peaks = survey.getPeaks(ObservationGroup.ObservationGroupType.FIFTEEN_MINUTES, Survey.DirectionOption.A);
        // Our peak should be the first 15 minute period for the 1 car that travelled in direction A.
        Assert.assertEquals(peaks.size(), 1);
        peak = peaks.get(0);
        Assert.assertEquals(peak.getCarCount(), 1);
        Assert.assertEquals(peak.getPeriodStartMillis(), 0);
    }
}

package tkidman.tracar.domain;


import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class ObservationGroupTest {
    public void testAdd() {
        ObservationGroup observationGroup = new ObservationGroup(ObservationGroup.ObservationGroupType.FIFTEEN_MINUTES);
        Observation observationValid = new Observation(0, 100, true, 100, 1);
        Observation observationValid2 = new Observation(0, 200, false, 100, 1);
        Observation observationAfter = new Observation(0, observationGroup.getPeriodLengthMillis(), true, 100, 1);
        Observation observationNextDay = new Observation(1, 0, true, 100, 1);

        Assert.assertTrue(observationGroup.add(observationValid));
        Assert.assertTrue(observationGroup.add(observationValid2));
        Assert.assertFalse(observationGroup.add(observationAfter));
        Assert.assertFalse(observationGroup.add(observationNextDay));

        // test that the values have been updated successfully - we've added 2 observations to the group
        Assert.assertEquals(observationGroup.getCarCount(), 2);
        Assert.assertEquals(observationGroup.getCarCountA(), 1);
        Assert.assertEquals(observationGroup.getCarCountB(), 1);
        Assert.assertEquals(observationGroup.getAverageSpeed(), 100.0);
        Assert.assertEquals(observationGroup.getAverageMetresBehind(), 1.0);
    }

    public void testGetTimeReadable() {
        ObservationGroup observationGroup = new ObservationGroup(0, Survey.MILLIS_IN_MINUTE * 15, Survey.MILLIS_IN_MINUTE * 15, "test");
        Assert.assertEquals(observationGroup.getTimeReadable(), "M 00:15:00");
    }

    public void testCompare() {
        ObservationGroup first = new ObservationGroup(0, Survey.MILLIS_IN_MINUTE * 15, Survey.MILLIS_IN_MINUTE * 15, "test");
        ObservationGroup second = new ObservationGroup(0, Survey.MILLIS_IN_MINUTE * 30, Survey.MILLIS_IN_MINUTE * 15, "test");
        ObservationGroup nextDay = new ObservationGroup(1, Survey.MILLIS_IN_MINUTE * 15, Survey.MILLIS_IN_MINUTE * 15, "test");
        Assert.assertEquals(first.compareTo(second), -1);
        Assert.assertEquals(first.compareTo(nextDay), -1);
        Assert.assertEquals(nextDay.compareTo(second), 1);
        Assert.assertEquals(nextDay.compareTo(nextDay), 0);
    }
}

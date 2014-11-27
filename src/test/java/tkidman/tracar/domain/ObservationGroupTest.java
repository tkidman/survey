package tkidman.tracar.domain;


import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class ObservationGroupTest {
    public void testObservationGroup() {
        ObservationGroup.FiveMinuteObservationGroup observationGroup = new ObservationGroup.FiveMinuteObservationGroup(null);
        Observation observation1 = new Observation(1, 0, true, 100, 1);
        Observation observation2 = new Observation(1, observationGroup.getPeriodLengthMillis(), true, 100, 1);

        Assert.assertTrue(observationGroup.add(observation1));
        Assert.assertFalse(observationGroup.add(observation2));
        Assert.assertEquals(true, true);
    }
}

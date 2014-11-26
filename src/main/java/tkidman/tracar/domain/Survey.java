package tkidman.tracar.domain;

import java.util.ArrayList;

public class Survey {
    private ArrayList<Observation> observations;

    public Survey(ArrayList<Observation> observations) {
        this.observations = observations;
        ObservationGroup.FiveMinuteObservationGroup previousfiveMinuteObservationGroup = null;
        ObservationGroup.FiveMinuteObservationGroup fiveMinuteObservationGroup = new ObservationGroup.FiveMinuteObservationGroup(1, 0, previousfiveMinuteObservationGroup);
        for (Observation observation : observations) {
            if (!fiveMinuteObservationGroup.add(observation)) {
                // we need to create a new group to handle this observation.
                previousfiveMinuteObservationGroup = fiveMinuteObservationGroup;
                fiveMinuteObservationGroup = new ObservationGroup.FiveMinuteObservationGroup(1, 0, previousfiveMinuteObservationGroup);
            }
        }
    }
}

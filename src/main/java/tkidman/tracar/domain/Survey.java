package tkidman.tracar.domain;

import java.util.ArrayList;

public class Survey {
    public static final int MILLIS_IN_SECOND = 1000;
    public static final int MILLIS_IN_MINUTE = MILLIS_IN_SECOND * 60;
    public static final int MILLIS_IN_HOUR = MILLIS_IN_MINUTE * 60;
    public static final int MILLIS_IN_DAY = MILLIS_IN_HOUR * 24;

    private ArrayList<Observation> observations;
    private ObservationGroup.FiveMinuteObservationGroup surveyRoot;

    public Survey(ArrayList<Observation> observations) {

        this.observations = observations;
        ObservationGroup.FiveMinuteObservationGroup fiveMinuteObservationGroup = new ObservationGroup.FiveMinuteObservationGroup(null);
        this.surveyRoot = fiveMinuteObservationGroup;
        ObservationGroup.FiveMinuteObservationGroup previousFiveMinuteObservationGroup;

        for (Observation observation : observations) {
            while (!fiveMinuteObservationGroup.add(observation)) {
                // we need to create a new group to handle this observation.
                previousFiveMinuteObservationGroup = fiveMinuteObservationGroup;
                fiveMinuteObservationGroup = new ObservationGroup.FiveMinuteObservationGroup(previousFiveMinuteObservationGroup);
            }
        }
    }

    public ObservationGroup.FiveMinuteObservationGroup getSurveyRoot() {
        return surveyRoot;
    }

    public ArrayList<ObservationGroup> getGroups(Class<? extends ObservationGroup> groupType) {
        ArrayList<ObservationGroup> observationGroups = new ArrayList<>();
        ObservationGroup startGroup = surveyRoot;

        while (startGroup.getClass() != groupType) {
            startGroup = startGroup.getParent();
            if (startGroup == null) {
                throw new RuntimeException("ObservationGroup type not found: " + groupType);
            }
        }

        do {
            observationGroups.add(startGroup);
            startGroup = startGroup.getNext();
        } while (startGroup != null);

        return observationGroups;
    }
}

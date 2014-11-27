package tkidman.tracar.domain;

import java.util.ArrayList;
import java.util.HashMap;

public class Survey {
    public static final int MILLIS_IN_SECOND = 1000;
    public static final int MILLIS_IN_MINUTE = MILLIS_IN_SECOND * 60;
    public static final int MILLIS_IN_HOUR = MILLIS_IN_MINUTE * 60;
    public static final int MILLIS_IN_DAY = MILLIS_IN_HOUR * 24;

    private ArrayList<Observation> observations;
    private ArrayList<ObservationGroup> fifteenMinuteGroups = new ArrayList<>();
    private ArrayList<ObservationGroup> twentyMinuteGroups = new ArrayList<>();
    private ArrayList<ObservationGroup> thirtyMinuteGroups = new ArrayList<>();
    private ArrayList<ObservationGroup> hourGroups = new ArrayList<>();
    private ArrayList<ObservationGroup> dayGroups = new ArrayList<>();

    private HashMap<ObservationGroup.ObservationGroupType, ArrayList<ObservationGroup>> allGroups = new HashMap<>();

    public Survey(ArrayList<Observation> observations) {
        fifteenMinuteGroups.add(new ObservationGroup(ObservationGroup.ObservationGroupType.FIFTEEN_MINUTES));
        allGroups.put(ObservationGroup.ObservationGroupType.FIFTEEN_MINUTES, fifteenMinuteGroups);

        twentyMinuteGroups.add(new ObservationGroup(ObservationGroup.ObservationGroupType.TWENTY_MINUTES));
        allGroups.put(ObservationGroup.ObservationGroupType.TWENTY_MINUTES, twentyMinuteGroups);
//
//        fifteenMinuteGroups.add(new ObservationGroup(ObservationGroup.ObservationGroupType.FIFTEEN_MINUTES));
//        allGroups.add(fifteenMinuteGroups);
//
//        fifteenMinuteGroups.add(new ObservationGroup(ObservationGroup.ObservationGroupType.FIFTEEN_MINUTES));
//        allGroups.add(fifteenMinuteGroups);
//
//        fifteenMinuteGroups.add(new ObservationGroup(ObservationGroup.ObservationGroupType.FIFTEEN_MINUTES));
//        allGroups.add(fifteenMinuteGroups);

        this.observations = observations;
        for (Observation observation : observations) {
            // add our observations to all the groups
            for (ArrayList<ObservationGroup> groups : allGroups.values()) {
                ObservationGroup latestObservationGroup = groups.get(groups.size() - 1);
                while (!latestObservationGroup.add(observation)) {
                    // we need to create a new group to handle this observation.
                    ObservationGroup observationGroup = new ObservationGroup(latestObservationGroup);
                    groups.add(observationGroup);
                    latestObservationGroup = observationGroup;
                }
            }
        }
    }

    public ArrayList<ObservationGroup> getObservationGroups(ObservationGroup.ObservationGroupType groupType) {
        return allGroups.get(groupType);
    }
}

package tkidman.tracar.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Survey {
    public static final int MILLIS_IN_SECOND = 1000;
    public static final int MILLIS_IN_MINUTE = MILLIS_IN_SECOND * 60;
    public static final int MILLIS_IN_HOUR = MILLIS_IN_MINUTE * 60;
    public static final int MILLIS_IN_DAY = MILLIS_IN_HOUR * 24;

    private ArrayList<Observation> observations;
    private HashMap<ObservationGroup.ObservationGroupType, ArrayList<ObservationGroup>> allGroups = new HashMap<>();

    public Survey(ArrayList<Observation> observations) {
        for (ObservationGroup.ObservationGroupType observationGroupType : ObservationGroup.ObservationGroupType.values()) {
            ArrayList<ObservationGroup> groups = new ArrayList<>();
            groups.add(new ObservationGroup(observationGroupType));
            allGroups.put(observationGroupType, groups);

        }

        this.observations = observations;
        for (Observation observation : observations) {
            // add our observations to all the groups
            for (ArrayList<ObservationGroup> groups : allGroups.values()) {
                ObservationGroup latestObservationGroup = groups.get(groups.size() - 1);
                while (!latestObservationGroup.add(observation)) {
                    // we need to create a new group to handle this observation.
                    ObservationGroup observationGroup = new ObservationGroup(latestObservationGroup);
                    System.out.println("Creating new " + observationGroup.getObservationGroupType() + " group for observation: " + observation.getDay() + " " + observation.getObservationTimeMillis() + ", new group start: " + observationGroup.getTimeReadable());
                    groups.add(observationGroup);
                    latestObservationGroup = observationGroup;
                }
            }
        }
    }

    public ArrayList<ObservationGroup> getObservationGroups(ObservationGroup.ObservationGroupType groupType) {
        return allGroups.get(groupType);
    }

    public ArrayList<ObservationGroup> getAverageOverDaysObservationGroups(ObservationGroup.ObservationGroupType groupType) {
        ArrayList<ObservationGroup> observationGroups = allGroups.get(groupType);
        HashMap<Integer, ObservationGroup> summedObservationGroups = new HashMap<>();
        // get the number of days, which we'll need to calculate the average across all days.  Just use the last observation group for this.
        int numDays = observationGroups.get(observationGroups.size() - 1).getDay() + 1;
        for (ObservationGroup observationGroup : observationGroups) {
            ObservationGroup summedObservationGroup = summedObservationGroups.get(observationGroup.getPeriodStartMillis());
            if (summedObservationGroup == null) {
                summedObservationGroup = new ObservationGroup(observationGroup.getObservationGroupType());
                summedObservationGroups.put(observationGroup.getPeriodStartMillis(), summedObservationGroup);
            }
            summedObservationGroup.add(observationGroup);
        }

        ArrayList<ObservationGroup> averageObservationGroups = new ArrayList<>();
        for (ObservationGroup observationGroup : summedObservationGroups.values()) {
            averageObservationGroups.add(observationGroup.divide(numDays));
        }
        Collections.sort(averageObservationGroups);
        return averageObservationGroups;
    }


}

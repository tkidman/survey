package tkidman.tracar.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * This represents the API of the survey, and provides methods to interact with observations and groups.
 */
public class Survey {
    public enum DirectionOption {
        A, B, BOTH;
    }

    public static final int MILLIS_IN_SECOND = 1000;
    public static final int MILLIS_IN_MINUTE = MILLIS_IN_SECOND * 60;
    public static final int MILLIS_IN_HOUR = MILLIS_IN_MINUTE * 60;
    public static final int MILLIS_IN_DAY = MILLIS_IN_HOUR * 24;

    private ArrayList<Observation> observations;
    private HashMap<ObservationGroup.ObservationGroupType, ArrayList<ObservationGroup>> allGroups = new HashMap<>();

    /**
     * Creates all default groups, and adds observations to them.
     */
    public Survey(ArrayList<Observation> observations) {
        Collections.sort(observations);
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
                    // commenting out for now - would have used LOG.debug if external libraries were allowed.
//                    System.out.println("Creating new " + observationGroup.getName() + " group for observation: " + observation.getDay() + " " + observation.getObservationTimeMillis() + ", new group start: " + observationGroup.getTimeReadable());
                    groups.add(observationGroup);
                    latestObservationGroup = observationGroup;
                }
            }
        }
    }

    /**
     *  Returns the ordered groups of the type specified.
     */
    public ArrayList<ObservationGroup> getObservationGroups(ObservationGroup.ObservationGroupType groupType) {
        return allGroups.get(groupType);
    }

    /**
     * Creates groups representing the average for the same period of each day over the length of the survey.
     */
    public ArrayList<ObservationGroup> getAverageOverDaysObservationGroups(ObservationGroup.ObservationGroupType groupType) {
        ArrayList<ObservationGroup> observationGroups = allGroups.get(groupType);
        HashMap<Integer, ObservationGroup> summedObservationGroups = new HashMap<>();
        for (ObservationGroup observationGroup : observationGroups) {
            ObservationGroup summedObservationGroup = summedObservationGroups.get(observationGroup.getPeriodStartMillis());
            if (summedObservationGroup == null) {
                summedObservationGroup = new ObservationGroup().initialiseForAverage(observationGroup);
                summedObservationGroups.put(observationGroup.getPeriodStartMillis(), summedObservationGroup);
            }
            summedObservationGroup.plus(observationGroup);
        }

        ArrayList<ObservationGroup> averageObservationGroups = new ArrayList<>();
        for (ObservationGroup observationGroup : summedObservationGroups.values()) {
            observationGroup.divide(getSurveyLengthDays());
            averageObservationGroups.add(observationGroup);
        }
        Collections.sort(averageObservationGroups);
        return averageObservationGroups;
    }

    /**
     * returns groups for 2 custom time periods - morning (7:00 - 10:00) and evening (16:00 - 19:00)
     */
    public ArrayList<ObservationGroup> getMorningEveningObservationGroups() {
        ArrayList<ObservationGroup> observationGroups = new ArrayList<>();
        for (int i = 0; i < getSurveyLengthDays(); i++) {
            observationGroups.add(new ObservationGroup(i, MILLIS_IN_HOUR * 7, MILLIS_IN_HOUR * 3, "Morning"));
            observationGroups.add(new ObservationGroup(i, MILLIS_IN_HOUR * 16, MILLIS_IN_HOUR * 3, "Evening"));
        }

        for (Observation observation : observations) {
            for (ObservationGroup observationGroup : observationGroups) {
                observationGroup.add(observation);
            }
        }
        return observationGroups;
    }

    private int getSurveyLengthDays() {
        // get the number of days in the survey.  Just use the last observation group for this.
        ArrayList<ObservationGroup> observationGroups = allGroups.get(ObservationGroup.ObservationGroupType.FIFTEEN_MINUTES);
        return observationGroups.get(observationGroups.size() - 1).getDay() + 1;
    }

    /**
     * Returns the peak observation group per day in the survey for the direction option provided.
     */
    public ArrayList<ObservationGroup> getPeaks(ObservationGroup.ObservationGroupType groupType, DirectionOption directionOption) {
        final ArrayList<ObservationGroup> observationGroups = allGroups.get(groupType);
        final ArrayList<ObservationGroup> peakGroups = new ArrayList<>();
        ObservationGroup peak = null;
        int day = 0;
        for (ObservationGroup observationGroup : observationGroups) {
            if (observationGroup.getDay() > day) {
                day++;
                peakGroups.add(peak);
                peak = null;
            }
            if (peak == null || getDirectionCount(observationGroup, directionOption) > getDirectionCount(peak, directionOption)) {
                peak = observationGroup;
            }
        }
        if (peak != null && !peakGroups.contains(peak)) {
            peakGroups.add(peak);
        }
        return peakGroups;
    }

    private long getDirectionCount(ObservationGroup group, DirectionOption directionOption) {
        long count;
        switch (directionOption) {
            case A: count = group.getCarCountA();
                break;
            case B: count = group.getCarCountB();
                break;
            case BOTH: count = group.getCarCount();
                break;
            default: throw new RuntimeException("Invalid DirectionOption");
        }
        return count;
    }
}

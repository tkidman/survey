package tkidman.tracar.domain;

import java.util.ArrayList;

/**
 * Used to aggregate observations over a time period.
 */
public class ObservationGroup {

    public ObservationGroupType getObservationGroupType() {
        return observationGroupType;
    }

    public long getCarCount() {
        return carCount;
    }

    public enum ObservationGroupType {
        FIFTEEN_MINUTES(Survey.MILLIS_IN_MINUTE * 15),
        TWENTY_MINUTES(Survey.MILLIS_IN_MINUTE * 20);

        private int periodLengthMillis;

        ObservationGroupType(final int periodLengthMillis) {
            this.periodLengthMillis = periodLengthMillis;
        }
    }

    private int day;
    private int periodStartMillis;
    private long carCount;
    private long carCountA;
    private double speedTotal;
    private ArrayList<Observation> observations = new ArrayList<>();
    private ObservationGroupType observationGroupType;

    public ObservationGroup(final ObservationGroupType observationGroupType) {
        this.observationGroupType = observationGroupType;
        this.day = 1;
        this.periodStartMillis = 0;
    }

    public ObservationGroup(final ObservationGroup previous) {
        this.observationGroupType = previous.observationGroupType;
        this.periodStartMillis = previous.getPeriodEndMillis() + 1;
        this.day = previous.day;
        if (this.periodStartMillis >= Survey.MILLIS_IN_DAY) {
            this.day++;
            // TODO does this have to be 0?
            this.periodStartMillis = 0;
        }
    }

    public int getPeriodLengthMillis() {
        return observationGroupType.periodLengthMillis;
    }

    protected int getPeriodEndMillis() {
        return periodStartMillis + getPeriodLengthMillis() - 1;
    }

    private boolean endsBefore(final ObservationGroup observationGroup) {
        return day < observationGroup.day || (day == observationGroup.day && getPeriodEndMillis() < observationGroup.getPeriodEndMillis());
    }

    /**
     * Returns false if this observation can't be added to this group.
     */
    public boolean add(Observation observation) {
        if (observation.getDay() == this.day && observation.getObservationTimeMillis() <= getPeriodEndMillis()) {
            observations.add(observation);
            carCount++;

            speedTotal += observation.getSpeedInKPH();
            return true;
        }
        return false;
    }
}

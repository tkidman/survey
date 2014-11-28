package tkidman.tracar.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Used to aggregate observations over a time period.
 */
public class ObservationGroup implements Comparable<ObservationGroup> {

    public enum ObservationGroupType {
        FIFTEEN_MINUTES(Survey.MILLIS_IN_MINUTE * 15),
        TWENTY_MINUTES(Survey.MILLIS_IN_MINUTE * 20),
        // We'll track 3 hour periods to represent the morning and evening peaks
        MORNING_AND_EVENING(Survey.MILLIS_IN_HOUR * 3),
        DAY(Survey.MILLIS_IN_DAY);

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
    private double minMetresBehind = -1;
    private double maxMetresBehind = -1;
    private double metresBehindTotal;
    private ArrayList<Observation> observations = new ArrayList<>();
    private ObservationGroupType observationGroupType;

    public ObservationGroup(final ObservationGroupType observationGroupType) {
        this.observationGroupType = observationGroupType;
        this.day = 0;
        this.periodStartMillis = 0;
    }

    public ObservationGroup(final ObservationGroup previous) {
        this.observationGroupType = previous.observationGroupType;
        this.periodStartMillis = previous.getPeriodEndMillis() + 1;
        this.day = previous.day;
        if (this.periodStartMillis >= Survey.MILLIS_IN_DAY) {
            this.day++;
            this.periodStartMillis -= Survey.MILLIS_IN_DAY;
        }
    }

    public ObservationGroupType getObservationGroupType() {
        return observationGroupType;
    }

    public long getCarCount() {
        return carCount;
    }

    public String getTimeReadable() {
        LocalTime startTime = LocalTime.ofNanoOfDay(0).plus(periodStartMillis, ChronoUnit.MILLIS);
        String timeDisplay = startTime.format(DateTimeFormatter.ISO_TIME);
        String dayDisplay = DayOfWeek.of((day % 7) + 1).getDisplayName(TextStyle.NARROW, Locale.ENGLISH);
        return dayDisplay + " " + timeDisplay;
    }

    public long getCarCountA() {
        return carCountA;
    }

    public long getCarCountB() {
        return carCount - carCountA;
    }

    public void add(final ObservationGroup observationGroup) {
        carCountA += observationGroup.carCountA;
        carCount += observationGroup.carCount;
        speedTotal += observationGroup.speedTotal;
    }

    public int getDay() {
        return day;
    }

    public ObservationGroup divide(final int numDays) {
        carCountA /= numDays;
        carCount /= numDays;
        speedTotal /= numDays;
        return this;
    }

    public double getAverageSpeed() {
        if (observations.size() == 0) {
            return 0;
        }
        return speedTotal / observations.size();
    }

    public double getAverageMetresBehind() {
        if (observations.size() == 0) {
            return 0;
        }
        return metresBehindTotal / observations.size();
    }

    public double getMinMetresBehind() {
        return minMetresBehind;
    }

    public double getMaxMetresBehind() {
        return maxMetresBehind;
    }

    public int getPeriodLengthMillis() {
        return observationGroupType.periodLengthMillis;
    }

    protected int getPeriodEndMillis() {
        return periodStartMillis + getPeriodLengthMillis() - 1;
    }

    public int getPeriodStartMillis() {
        return periodStartMillis;
    }

    /**
     * Returns false if this observation can't be added to this group.
     */
    public boolean add(Observation observation) {
        if (observation.getDay() == this.day && observation.getObservationTimeMillis() <= getPeriodEndMillis()) {
            observations.add(observation);
            carCount++;
            if (observation.isDirectionA()) {
                carCountA++;
            }
            speedTotal += observation.getSpeedInKmph();
            metresBehindTotal += observation.getMetresBehind();
            if (minMetresBehind == -1 || minMetresBehind > observation.getMetresBehind()) {
                minMetresBehind = observation.getMetresBehind();
            }
            if (maxMetresBehind == -1 || maxMetresBehind < observation.getMetresBehind()) {
                maxMetresBehind = observation.getMetresBehind();
            }
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(final ObservationGroup o) {
        return new Integer(day * Survey.MILLIS_IN_DAY + periodStartMillis).compareTo(o.day * Survey.MILLIS_IN_DAY + o.periodStartMillis);
    }
}

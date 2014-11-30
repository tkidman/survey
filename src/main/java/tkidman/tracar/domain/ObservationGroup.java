package tkidman.tracar.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * Used to aggregate observations over a time period.
 */
public class ObservationGroup implements Comparable<ObservationGroup> {
    private static final int UNINITIALISED = -1;

    public enum ObservationGroupType {
        FIFTEEN_MINUTES(Survey.MILLIS_IN_MINUTE * 15),
        TWENTY_MINUTES(Survey.MILLIS_IN_MINUTE * 20),
        THIRTY_MINUTES(Survey.MILLIS_IN_MINUTE * 30),
        ONE_HOUR(Survey.MILLIS_IN_MINUTE * 60),
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
    private double minMetresBehind = UNINITIALISED;
    private double maxMetresBehind = UNINITIALISED;
    private double metresBehindTotal;
    private int periodLengthMillis;
    private String name;
    private boolean averageAcrossDays = false;

    public ObservationGroup() {}

    public ObservationGroup(final ObservationGroupType observationGroupType) {
        this(0, 0, observationGroupType.periodLengthMillis, observationGroupType.name().toLowerCase());
        if (averageAcrossDays) {
            this.day = UNINITIALISED;
        }
    }

    public ObservationGroup(final ObservationGroup previous) {
        this.periodStartMillis = previous.getPeriodEndMillis() + 1;
        this.day = previous.day;
        if (this.periodStartMillis >= Survey.MILLIS_IN_DAY) {
            this.day++;
            this.periodStartMillis -= Survey.MILLIS_IN_DAY;
        }
        this.name = previous.name;
        this.periodLengthMillis = previous.periodLengthMillis;
    }

    public ObservationGroup(int day, int periodStartMillis, int periodLengthMillis, String name) {
        this.day = day;
        this.periodStartMillis = periodStartMillis;
        this.periodLengthMillis = periodLengthMillis;
        this.name = name;
    }

    public ObservationGroup initialiseForAverage(final ObservationGroup observationGroup) {
        this.name = observationGroup.name;
        this.periodStartMillis = observationGroup.periodStartMillis;
        this.periodLengthMillis = observationGroup.periodLengthMillis;
        this.day = -1;
        this.averageAcrossDays = true;
        return this;
    }


    public long getCarCount() {
        return carCount;
    }

    public String getTimeReadable() {
        LocalTime startTime = LocalTime.ofNanoOfDay(0).plus(periodStartMillis, ChronoUnit.MILLIS);
        String timeDisplay = startTime.format(DateTimeFormatter.ISO_TIME);
        if (!averageAcrossDays) {
            String dayDisplay = DayOfWeek.of((day % 7) + 1).getDisplayName(TextStyle.NARROW, Locale.ENGLISH);
            timeDisplay = dayDisplay + " " + timeDisplay;
        }
        return timeDisplay;
    }

    public long getCarCountA() {
        return carCountA;
    }

    public long getCarCountB() {
        return carCount - carCountA;
    }

    public void plus(final ObservationGroup observationGroup) {
        carCountA += observationGroup.carCountA;
        carCount += observationGroup.carCount;
        speedTotal += observationGroup.speedTotal;
        metresBehindTotal += observationGroup.metresBehindTotal;
    }

    public int getDay() {
        return day;
    }

    public void divide(final int numDays) {
        carCountA /= numDays;
        carCount /= numDays;
        speedTotal /= numDays;
        metresBehindTotal /= numDays;
    }

    public double getAverageSpeed() {
        if (carCount == 0) {
            return 0;
        }
        return speedTotal / carCount;
    }

    public double getAverageMetresBehind() {
        if (carCount == 0) {
            return 0;
        }
        return metresBehindTotal / carCount;
    }

    public double getMinMetresBehind() {
        return minMetresBehind;
    }

    public double getMaxMetresBehind() {
        return maxMetresBehind;
    }

    public int getPeriodLengthMillis() {
        return periodLengthMillis;
    }

    protected int getPeriodEndMillis() {
        return periodStartMillis + getPeriodLengthMillis() - 1;
    }

    public int getPeriodStartMillis() {
        return periodStartMillis;
    }

    public String getName() {
        return name;
    }

    /**
     * Returns false if this observation can't be added to this group.
     */
    public boolean add(Observation observation) {
        if (observation.getDay() == this.day && observation.getObservationTimeMillis() <= getPeriodEndMillis() && observation.getObservationTimeMillis() >= getPeriodStartMillis()) {
            carCount++;
            if (observation.isDirectionA()) {
                carCountA++;
            }
            speedTotal += observation.getSpeedInKmph();
            metresBehindTotal += observation.getMetresBehind();
            if (minMetresBehind == UNINITIALISED || minMetresBehind > observation.getMetresBehind()) {
                minMetresBehind = observation.getMetresBehind();
            }
            if (maxMetresBehind == UNINITIALISED || maxMetresBehind < observation.getMetresBehind()) {
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

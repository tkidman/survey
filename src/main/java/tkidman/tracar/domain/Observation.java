package tkidman.tracar.domain;

public class Observation implements Comparable<Observation> {
    private final int day;
    private final int observationTimeMillis;
    private final boolean directionA;
    private final double speedInKmph;
    private final double metresBehind;

    public Observation(final int day, final int observationTimeMillis, final boolean directionA, final double speedInKmph, final double metresBehindPrevious) {
        this.day = day;
        this.observationTimeMillis = observationTimeMillis;
        this.directionA = directionA;
        this.speedInKmph = speedInKmph;
        this.metresBehind = metresBehindPrevious;
    }

    public long getObservationTimeMillis() {
        return observationTimeMillis;
    }

    public int getDay() {
        return day;
    }

    public double getSpeedInKmph() {
        return speedInKmph;
    }

    public boolean isDirectionA() {
        return directionA;
    }

    public double getMetresBehind() {
        return metresBehind;
    }


    @Override
    public int compareTo(final Observation o) {
        return new Integer(day * Survey.MILLIS_IN_DAY + observationTimeMillis).compareTo(o.day * Survey.MILLIS_IN_DAY + o.observationTimeMillis);
    }
}

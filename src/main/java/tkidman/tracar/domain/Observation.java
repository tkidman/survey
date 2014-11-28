package tkidman.tracar.domain;

public class Observation {
    private final int day;
    private final long observationTimeMillis;
    private final boolean directionA;
    private final double speedInKmph;
    private final double metresBehind;

    public Observation(final int day, final long observationTimeMillis, final boolean directionA, final double speedInKmph, final double metresBehindPrevious) {
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
}

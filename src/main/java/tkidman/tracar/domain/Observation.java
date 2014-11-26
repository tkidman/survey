package tkidman.tracar.domain;

public class Observation {
    private final int day;
    private final long observationTimeMillis;
    private final boolean directionA;
    private final double speedInKPH;
    private Observation previous;
    private Observation next;

    public Observation(final int day, final long observationTimeMillis, final boolean directionA, final double speedInKPH, final Observation previous) {
        this.day = day;
        this.observationTimeMillis = observationTimeMillis;
        this.directionA = directionA;
        this.speedInKPH = speedInKPH;
        this.previous = previous;
        if (previous != null) {
            previous.next = this;
        }
    }

    public long getObservationTimeMillis() {
        return observationTimeMillis;
    }

    public int getDay() {
        return day;
    }

    public double getSpeedInKPH() {
        return speedInKPH;
    }
}

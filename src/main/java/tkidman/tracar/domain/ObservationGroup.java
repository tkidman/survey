package tkidman.tracar.domain;

import java.util.ArrayList;

public abstract class ObservationGroup {
    private static final int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;

    protected int day;
    protected int periodStartMillis;
    protected long carCount;
    protected double speedTotal;

    protected ObservationGroup previous;
    protected ObservationGroup parent;
    protected ArrayList<ObservationGroup> children = new ArrayList<>();

    public ObservationGroup(final ObservationGroup previous) {
        if (previous == null) {
            this.day = 1;
            this.periodStartMillis = 0;
        } else {
            this.periodStartMillis = previous.getPeriodEndMillis() + 1;
            this.day = previous.day;
            if (this.periodStartMillis >= MILLIS_IN_DAY) {
                this.day++;
                this.periodStartMillis = 0;
            }
        }

        this.previous = previous;
        if (previous == null || previous.parent.endsBefore(this)) {
            // need to create a new parent
            parent = createParent();
        } else {
            parent = previous.parent;
        }
        parent.children.add(this);
    }

    public abstract ObservationGroup createParent();

    public abstract int getPeriodLengthMillis();

    protected int getPeriodEndMillis() {
        return periodStartMillis + getPeriodLengthMillis() - 1;
    }

    private boolean endsBefore(final ObservationGroup observationGroup) {
        return day < observationGroup.day || (day == observationGroup.day && getPeriodEndMillis() < observationGroup.getPeriodEndMillis());
    }

    public static final class FiveMinuteObservationGroup extends ObservationGroup {
        private static final int PERIOD_LENGTH_MILLIS = 5 * 60 * 1000;
        private ArrayList<Observation> observations = new ArrayList<>();

        public FiveMinuteObservationGroup(ObservationGroup previous) {
            super(previous);

        }

        @Override
        public ObservationGroup createParent() {
            return new FifteenMinuteObservationGroup(this.previous);
        }

        @Override
        public int getPeriodLengthMillis() {
            return PERIOD_LENGTH_MILLIS;
        }

        public boolean add(Observation observation) {
            observations.add(observation);
            if (observation.getDay() == this.day && observation.getObservationTimeMillis() <= getPeriodEndMillis()) {
                carCount++;
                speedTotal += observation.getSpeedInKPH();
                return true;
            }
            return false;
        }

    }

    public static final class FifteenMinuteObservationGroup extends ObservationGroup {
        private static final int PERIOD_LENGTH_MILLIS = 15 * 60 * 1000;

        public FifteenMinuteObservationGroup(ObservationGroup previous) {
            super(previous);

        }

        @Override
        public ObservationGroup createParent() {
            return null; //new FifteenMinuteObservationGroup;
        }

        @Override
        public int getPeriodLengthMillis() {
            return PERIOD_LENGTH_MILLIS;
        }
    }
}

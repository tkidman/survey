package tkidman.tracar.domain;

import java.util.ArrayList;

public abstract class ObservationGroup {

    protected int day;
    protected int periodStartMillis;
    protected long carCount;
    protected double speedTotal;

    protected ObservationGroup previous;
    protected ObservationGroup next;
    protected ObservationGroup parent;
    protected ArrayList<ObservationGroup> children = new ArrayList<>();

    protected boolean initialised = false;

    public ObservationGroup(final ObservationGroup previous) {
        if (previous == null) {
            this.day = 1;
            this.periodStartMillis = 0;
        } else {
            previous.next = this;
            this.periodStartMillis = previous.getPeriodEndMillis() + 1;
            this.day = previous.day;
            if (this.periodStartMillis >= Survey.MILLIS_IN_DAY) {
                this.day++;
                // TODO does this have to be 0?
                this.periodStartMillis = 0;
            }
        }

        this.previous = previous;
        if (shouldHaveParent()) {
            if (previous == null || previous.parent.endsBefore(this)) {
                // need to create a new parent
                parent = createParent();
            } else {
                parent = previous.parent;
            }
            parent.children.add(this);
        }
    }

    public abstract ObservationGroup createParent();

    protected abstract int getPeriodLengthMillis();

    protected int getPeriodEndMillis() {
        return periodStartMillis + getPeriodLengthMillis() - 1;
    }

    private boolean endsBefore(final ObservationGroup observationGroup) {
        return day < observationGroup.day || (day == observationGroup.day && getPeriodEndMillis() < observationGroup.getPeriodEndMillis());
    }

    public ObservationGroup getNext() {
        return next;
    }

    public ObservationGroup getParent() {
        return parent;
    }

    protected ObservationGroup getPreviousParent() {
        return this.previous == null ? null : this.previous.parent;
    }

    protected boolean shouldHaveParent() {
        return true;
    }

    protected void initialise() {
        if (!initialised) {
            for (ObservationGroup child : children) {
                child.initialise();
                this.carCount += child.carCount;
                this.speedTotal += child.speedTotal;
            }
        }
        initialised = true;
    }

    public long getCarCount() {
        if (!initialised) {
            initialise();
        }
        return carCount;
    }

    public double getSpeedTotal() {
        if (!initialised) {
            initialise();
        }
        return speedTotal;
    }

    public static final class FiveMinuteObservationGroup extends ObservationGroup {
        private static final int PERIOD_LENGTH_MILLIS = Survey.MILLIS_IN_MINUTE * 5;
        private ArrayList<Observation> observations = new ArrayList<>();

        public FiveMinuteObservationGroup(ObservationGroup previous) {
            super(previous);
            initialised = true;
        }

        @Override
        public ObservationGroup createParent() {
            return new FifteenMinuteObservationGroup(getPreviousParent());
        }

        @Override
        protected int getPeriodLengthMillis() {
            return PERIOD_LENGTH_MILLIS;
        }

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

    public static final class FifteenMinuteObservationGroup extends ObservationGroup {
        private static final int PERIOD_LENGTH_MILLIS = Survey.MILLIS_IN_MINUTE * 15;

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

        @Override
        protected boolean shouldHaveParent() {
            return false;
        }
    }

    public static final class ThirtyMinuteObservationGroup extends ObservationGroup {
        private static final int PERIOD_LENGTH_MILLIS = Survey.MILLIS_IN_MINUTE * 30;

        public ThirtyMinuteObservationGroup(ObservationGroup previous) {
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

        @Override
        protected boolean shouldHaveParent() {
            return false;
        }
    }
}

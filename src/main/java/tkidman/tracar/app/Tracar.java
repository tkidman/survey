package tkidman.tracar.app;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import tkidman.tracar.domain.Observation;
import tkidman.tracar.domain.Survey;

public class Tracar {
    private static final int MILLIS_IN_HOUR = 3600000;

    public Survey loadSurvey() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("filename"));
            RawObservation rawObservation = new RawObservation();
            ArrayList<Observation> observations = new ArrayList<>();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (rawObservation.processLine(line)) {
                    // raw observation is complete, we can create a proper observation now.
                    final Observation previous = observations.size() > 0 ? observations.get(observations.size() - 1) : null;
                    observations.add(rawObservation.createObservation(previous));
                }
            }

            // create our survey
            return new Survey(observations);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final class RawObservation {
        private String firstA, secondA, firstB, secondB;

        /**
         * Returns true when the raw observation is complete
         */
        private boolean processLine(String line) {
            if (line.startsWith("A")) {
                if (firstA == null) {
                    firstA = line;
                } else {
                    secondA = line;
                }
            } else {
                if (firstB == null) {
                    firstB = line;
                } else {
                    secondB = null;
                }
            }
            if (firstA != null && secondA != null) {
                if ((firstB == null && secondB == null) || (firstB != null && secondB != null)) {
                    return true;
                }
            }
            return false;
        }

        private Observation createObservation(Observation previous) {
            boolean directionA = firstB == null;
            int millisSinceMidnightFirstA = Integer.parseInt(firstA.substring(1));
            int millisSinceMidnightSecondA = Integer.parseInt(secondA.substring(1));
            int day;

            if (previous == null) {
                day = 1;
            } else {
                if (previous.getObservationTimeMillis() >= millisSinceMidnightFirstA) {
                    day = previous.getDay() + 1;
                } else {
                    day = previous.getDay();
                }
            }

            // perform our approximate speed calculation
            // millis to travel 2.5 meters.
            double speedInKPH = (MILLIS_IN_HOUR / (millisSinceMidnightSecondA - millisSinceMidnightFirstA)) * 0.0025;
            return new Observation(day, millisSinceMidnightFirstA, directionA, speedInKPH, previous);
        }
    }
}

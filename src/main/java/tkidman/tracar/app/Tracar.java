package tkidman.tracar.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

import tkidman.tracar.domain.Observation;
import tkidman.tracar.domain.Survey;

public class Tracar {

    public static void main(String[] args) {
        new Tracar().createReport();
    }

    public void createReport() {
        try {
            Stream<String> lines = Files.lines(Paths.get("Vehicle Survey Coding Challenge sample data.txt"));
            Survey survey = loadSurvey(lines);
            new CsvReport().report(survey);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Survey loadSurvey(Stream<String> lines) {
        RawObservationReference rawObservationReference = new RawObservationReference();
        rawObservationReference.rawObservation = new RawObservation();
        ArrayList<Observation> observations = new ArrayList<>();
        lines.forEach(line -> processLine(line, rawObservationReference, observations));

        // create our survey
        return new Survey(observations);
    }

    private void processLine(String line, RawObservationReference rawObservationReference, ArrayList<Observation> observations) {
        if (rawObservationReference.rawObservation.processLine(line)) {
            // raw observation is complete, we can create a proper observation now.
            final Observation previous = observations.size() > 0 ? observations.get(observations.size() - 1) : null;
            Observation newObservation = rawObservationReference.rawObservation.createObservation(previous);
            observations.add(newObservation);
            rawObservationReference.rawObservation = new RawObservation();
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
                    secondB = line;
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
            int day = 0;

            if  (previous != null) {
                if (previous.getObservationTimeMillis() >= millisSinceMidnightFirstA) {
                    day = previous.getDay() + 1;
                } else {
                    day = previous.getDay();
                }
            }

            // perform our approximate speed calculation
            // millis to travel 2.5 meters.
            double speedInKmph = (Survey.MILLIS_IN_HOUR / (millisSinceMidnightSecondA - millisSinceMidnightFirstA)) * 0.0025;

            // now calculate our distance from the car in front
            double metresBehindPrevious = 0;
            if (previous != null) {
                long differenceInTimeMillis = millisSinceMidnightFirstA - previous.getObservationTimeMillis() + (day - previous.getDay() * Survey.MILLIS_IN_DAY);
                // so how far would be travelled over the difference in time by the second car at the speed when it made the obs?
                metresBehindPrevious = (speedInKmph / (Survey.MILLIS_IN_HOUR / differenceInTimeMillis)) * 1000;
            }

            return new Observation(day, millisSinceMidnightFirstA, directionA, speedInKmph, metresBehindPrevious);
        }
    }

    private class RawObservationReference {
        private RawObservation rawObservation;
    }
}

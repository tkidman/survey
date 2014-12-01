package tkidman.tracar.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

import tkidman.tracar.domain.Observation;
import tkidman.tracar.domain.Survey;

/**
 * Tracar (Track - Car, the Car Tracker!)
 * The entry point into the application, responsible for parsing the input file and turning it into
 * a sensible format for the domain.
 */
public class Tracar {

    public static void main(String[] args) {
        String filePath = "sample_data.txt";
        if (args.length == 1) {
            filePath = args[0];
        }
        new Tracar().createReport(filePath);
    }

    void createReport(String filePath) {
        try {
            Stream<String> lines = Files.lines(Paths.get(filePath));
            Survey survey = loadSurvey(lines);
            new CsvReport().report(survey);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    Survey loadSurvey(Stream<String> lines) {
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

    // Package private for tests
    static final class RawObservation {
        private static final double DISTANCE_BETWEEN_WHEELS_KILOMETRES = 0.0025;
        private static final int METRES_IN_KILOMETRE = 1000;

        String firstA, secondA, firstB, secondB;

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

        Observation createObservation(Observation previous) {
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
            double speedInKmph = (Survey.MILLIS_IN_HOUR / (millisSinceMidnightSecondA - millisSinceMidnightFirstA)) * DISTANCE_BETWEEN_WHEELS_KILOMETRES;

            // now calculate our distance from the car in front
            double metresBehindPrevious = 0;
            if (previous != null) {
                long differenceInTimeMillis = millisSinceMidnightFirstA - previous.getObservationTimeMillis() + ((day - previous.getDay()) * Survey.MILLIS_IN_DAY);
                // so how far would be travelled over the difference in time by the second car at the speed when it made the observation?
                metresBehindPrevious = (speedInKmph / (Survey.MILLIS_IN_HOUR / differenceInTimeMillis)) * METRES_IN_KILOMETRE;
            }

            return new Observation(day, millisSinceMidnightFirstA, directionA, speedInKmph, metresBehindPrevious);
        }
    }

    private class RawObservationReference {
        private RawObservation rawObservation;
    }
}

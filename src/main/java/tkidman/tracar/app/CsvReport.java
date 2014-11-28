package tkidman.tracar.app;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import tkidman.tracar.domain.ObservationGroup;
import tkidman.tracar.domain.Survey;

/**
 * A simple client of the survey that can output data in a csv format.
 */
public class CsvReport {
    private static final char SEPARATOR = '\t';

    public void report(Survey survey) {
        for (ObservationGroup.ObservationGroupType observationGroupType : ObservationGroup.ObservationGroupType.values()) {
            writeTotalCarsByPeriodReport(survey, observationGroupType);
        }
    }

    private void writeTotalCarsByPeriodReport(final Survey survey, final ObservationGroup.ObservationGroupType observationGroupType) {
        try {
            PrintWriter printWriter = new PrintWriter("report_totals_" + observationGroupType.name().toLowerCase() + ".csv", "UTF-8");

            // header row
            printWriter.println(
                    "Time" + SEPARATOR +
                    "Total Cars" + SEPARATOR +
                    "Cars A" + SEPARATOR +
                    "Cars B" + SEPARATOR +
                    "Average speed" + SEPARATOR +
                    "Average distance" + SEPARATOR +
                    "Min distance" + SEPARATOR +
                    "Max distance"
            );
            ArrayList<ObservationGroup> observationGroups = survey.getObservationGroups(observationGroupType);
            for (ObservationGroup observationGroup : observationGroups) {
                printWriter.println(
                        observationGroup.getTimeReadable() + SEPARATOR +
                        observationGroup.getCarCount() + SEPARATOR +
                        observationGroup.getCarCountA() + SEPARATOR +
                        observationGroup.getCarCountB() + SEPARATOR +
                        observationGroup.getAverageSpeed() + SEPARATOR +
                        observationGroup.getAverageMetresBehind() + SEPARATOR +
                        observationGroup.getMinMetresBehind() + SEPARATOR +
                        observationGroup.getMaxMetresBehind()
                );
            }
            printWriter.close();

        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}

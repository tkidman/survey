package tkidman.tracar.app;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.ArrayList;

import tkidman.tracar.domain.ObservationGroup;
import tkidman.tracar.domain.Survey;

/**
 * A simple client of the survey that can output data in a csv format.
 */
public class CsvReport {
    private static final char SEPARATOR = '\t';
    private static final String OUTPUT_DIR = "output/";

    public void report(Survey survey) {
        for (ObservationGroup.ObservationGroupType observationGroupType : ObservationGroup.ObservationGroupType.values()) {
            writeTotalCarsByPeriodReport(survey, observationGroupType);
        }
        writeMorningEvening(survey);
        writeAverages(survey);
        writePeaks(survey);
    }

    private void writeTotalCarsByPeriodReport(final Survey survey, final ObservationGroup.ObservationGroupType observationGroupType) {
        ArrayList<ObservationGroup> observationGroups = survey.getObservationGroups(observationGroupType);
        writeObservationGroups(observationGroups, "totals_" + observationGroupType.name().toLowerCase() + ".csv");
    }

    private void writeObservationGroups(ArrayList<ObservationGroup> observationGroups, String reportName) {
        try {
            PrintWriter printWriter = new PrintWriter(OUTPUT_DIR + reportName, "UTF-8");

            // header row
            printWriter.println("Time" + SEPARATOR +
                            "Total Cars" + SEPARATOR +
                            "Cars A" + SEPARATOR +
                            "Cars B" + SEPARATOR +
                            "Average speed (kmph)" + SEPARATOR +
                            "Average distance (m)" + SEPARATOR +
                            "Min distance (m)" + SEPARATOR +
                            "Max distance (m)"
            );

            for (ObservationGroup observationGroup : observationGroups) {
                printWriter.println(observationGroup.getTimeReadable() + SEPARATOR +
                                observationGroup.getCarCount() + SEPARATOR +
                                observationGroup.getCarCountA() + SEPARATOR +
                                observationGroup.getCarCountB() + SEPARATOR +
                                formatDouble(observationGroup.getAverageSpeed()) + SEPARATOR +
                                formatDouble(observationGroup.getAverageMetresBehind()) + SEPARATOR +
                                formatDouble(observationGroup.getMinMetresBehind()) + SEPARATOR +
                                formatDouble(observationGroup.getMaxMetresBehind())
                );
            }
            printWriter.close();

        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeMorningEvening(Survey survey) {
        ArrayList<ObservationGroup> observationGroups = survey.getMorningEveningObservationGroups();
        writeObservationGroups(observationGroups, "morning_evening_3_hourly.csv");
    }

    private void writeAverages(Survey survey) {
        for (ObservationGroup.ObservationGroupType observationGroupType : ObservationGroup.ObservationGroupType.values()) {
            ArrayList<ObservationGroup> observationGroups = survey.getAverageOverDaysObservationGroups(observationGroupType);
            writeObservationGroups(observationGroups, "averages_" + observationGroupType.name().toLowerCase() + ".csv");
        }
    }

    private void writePeaks(Survey survey) {
        // Just writing out one peak type as an example.
        ArrayList<ObservationGroup> observationGroups = survey.getPeaks(ObservationGroup.ObservationGroupType.THIRTY_MINUTES, Survey.DirectionOption.B);
        writeObservationGroups(observationGroups, "thirty_minute_daily_peak_direction_b.csv");
    }

    private String formatDouble(double num) {
        return NumberFormat.getNumberInstance().format(num);
    }
}

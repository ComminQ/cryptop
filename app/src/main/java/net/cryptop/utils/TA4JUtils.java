package net.cryptop.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.logging.Level;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;

import net.cryptop.App;
import net.cryptop.config.Config.CryptoPair;
import net.cryptop.utils.file.CSVUtils;

public class TA4JUtils {

    private TA4JUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static BarSeries loadCsv(CryptoPair cryptoPair, String fileName) {
        return ChronoUtils.time("Load file from " + fileName + " (TA4J)", () -> {
            var series = new BaseBarSeries(cryptoPair.symbolWithDash());

            try {
                FileReader fileReader = new FileReader(fileName);
                BufferedReader bufferedReader = new BufferedReader(fileReader);

                bufferedReader.readLine().split(CSVUtils.COMMA_DELIMITER);

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    var values = line.split(CSVUtils.COMMA_DELIMITER);
                    // date is the first field (milliseconds since epoch)
                    long date = Long.parseLong(values[0]);
                    ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(
                            java.time.Instant.ofEpochMilli(date), ZoneId.systemDefault());
                    double open = Double.parseDouble(values[1]);
                    double high = Double.parseDouble(values[2]);
                    double low = Double.parseDouble(values[3]);
                    double close = Double.parseDouble(values[4]);
                    double volume = Double.parseDouble(values[5]);

                    series.addBar(zonedDateTime, open, high, low, close, volume);
                }

                bufferedReader.close();
            } catch (Exception e) {
                App.getLogger()
                        .log(Level.SEVERE, "Error occured", e);
            }

            return series;
        });
    }

}

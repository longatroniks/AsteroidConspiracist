package dte.masteriot.mdp.asteroidconspiracist.util.date;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    // Generates a timestamp in the format "yyyy-MM-dd HH:mm:ss"
    public static String generateTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
}

package io.ningyuan.gpslogger;

import android.location.Location;

class GpxUtils {
    final static int LATITUDE = 0;
    final static int LONGITUDE = 1;

    static String getDMS (double degrees, int TYPE) {
        boolean is_negative;
        if (degrees < 0) {
            is_negative = true;
        } else {
            is_negative = false;
        }

        String raw_DMS = Location.convert(degrees, Location.FORMAT_SECONDS);
        raw_DMS = raw_DMS.replaceFirst("-", "");
        raw_DMS = raw_DMS.replaceFirst(":", "°");
        raw_DMS = raw_DMS.replaceFirst(":", "'");
        raw_DMS += "\"";

        if (TYPE == LATITUDE && !is_negative) {
            raw_DMS += "N";
        } else if (TYPE == LATITUDE && is_negative) {
            raw_DMS += "S";
        } else if (TYPE == LONGITUDE && !is_negative) {
            raw_DMS += "E";
        } else if (TYPE == LONGITUDE && is_negative) {
            raw_DMS += "W";
        }

        return raw_DMS;
    }
}

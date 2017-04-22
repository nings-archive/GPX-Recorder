package io.ningyuan.gpslogger;

import android.location.Location;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

class GpxParser {
    public String timeStamp;
    private LinkedList<trkpt> gpsCoords;
    private File ouputFile;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    GpxParser () {
        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()) + ".txt";
        ouputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), timeStamp);
        gpsCoords = new LinkedList();
    }

    void addTrkpt (Location location){
        gpsCoords.add(new trkpt(location));
    }

    void save () {
        try {
            FileOutputStream outputStream = new FileOutputStream(ouputFile, true);

            outputStream.write("<gpx><trk><trkseg>\n".getBytes());
            for (trkpt pt : gpsCoords)
                outputStream.write(pt.formatTag().getBytes());
            outputStream.write("</trkseg></trk></gpx>".getBytes());

            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class trkpt {
        double lon, lat;
        String time;

        trkpt (Location location) {
            lon = location.getLongitude();
            lat = location.getLatitude();
            time = dateFormat.format(new Date());
        }

        String formatTag () {
            return String.format("<trkpt lat=\"%f\" lon=\"%f\">", lat, lon)
                    + String.format("<time>%s</time>", time)
                    + "</trkpt>\n";
        }
    }
}

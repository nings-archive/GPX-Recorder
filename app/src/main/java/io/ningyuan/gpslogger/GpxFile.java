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
    private File outputDirectory;
    private File outputFile;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    GpxParser () {
        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()) + ".gpx";
        outputDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if(!outputDirectory.isDirectory()) {
            boolean success = outputDirectory.mkdirs();
            if (!success) {
                throw new IllegalStateException("Couldn't create output directory: " + outputDirectory);
            }
        }
        outputFile = new File(outputDirectory, timeStamp);
        try {
            FileOutputStream outputStream = new FileOutputStream(outputFile, true);
            outputStream.write("Placeholder file, contents will be written when GPS Logger is stopped.".getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't write to output file: " + outputFile);
        }
        gpsCoords = new LinkedList();
    }

    void addTrkpt (Location location){
        gpsCoords.add(new trkpt(location));
    }

    void save () throws IllegalStateException {
        try {
            FileOutputStream outputStream = new FileOutputStream(outputFile, true);

            outputStream.write("<gpx><trk><trkseg>\n".getBytes());
            for (trkpt pt : gpsCoords)
                outputStream.write(pt.formatTag().getBytes());
            outputStream.write("</trkseg></trk></gpx>".getBytes());

            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't write to output file: " + outputFile);
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

    public String getOutputPath() {
        return outputFile.getAbsolutePath();
    }
}

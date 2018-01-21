package controllers;

import play.libs.Json;
import play.mvc.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VideoController extends Controller {

    private static final String videoPath = "./sample.avi";

    public Result index() {
        return ok(views.html.index.render());
    }

    /*
    Get a screenshot of a frame of the video
     */
    public Result videoPoster() {
        ProcessBuilder builder = new ProcessBuilder(
                "ffmpeg",
                "-ss", "1.0",
                "-i", videoPath,
                "-vframes", "1",
                "-f", "mjpeg",
                "pipe:1"
        );
        try {
            final Process process = builder.start();
            return ok(process.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return internalServerError();
        }
    }

    /*
    Get the duration of the video
     */
    public Result videoMetadata() {
        ProcessBuilder builder = new ProcessBuilder(
                "ffmpeg",
                "-i", videoPath
        );
        try {
            final Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String str;
            Pattern durationPattern = Pattern.compile("Duration: (..):(..):(..)\\...");
            Map<String, String> metadata = new HashMap<>();

            while ((str = reader.readLine()) != null) {
                Matcher durationMatcher = durationPattern.matcher(str);
                while (durationMatcher.find()) {
                    int hours = Integer.parseInt(durationMatcher.group(1));
                    int minutes = Integer.parseInt(durationMatcher.group(2));
                    int seconds = Integer.parseInt(durationMatcher.group(3));
                    int duration = hours * 3600 + minutes * 60 + seconds + 1;
                    metadata.put("duration", Integer.toString(duration));
                }
            }
            return ok(Json.toJson(metadata));
        } catch (IOException e) {
            e.printStackTrace();
            return internalServerError();
        }
    }

    /*
    Stream the video on-the-fly with ffmpeg. Pass 't' query paramter for start time while seeking.
     */
    public Result streamedVideo() {
        // The timestamp to start at, in case we're seeking in the video.
        String timestamp = request().queryString().getOrDefault("t", new String[]{"0"})[0];

        response().setHeader("Content-Disposition", "inline");
        response().setHeader("Content-Transfer-Encoding", "binary");

        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "ffmpeg",
                    "-ss", timestamp,
                    "-i", videoPath,
                    "-f", "mp4",
                    "-vcodec", "h264",
                    "-acodec", "aac",
                    "-strict", "experimental",
                    "-preset", "ultrafast",
                    "-movflags", "frag_keyframe+empty_moov+faststart",
                    "pipe:1"
            );

            final Process process = builder.start();

            // (optional) in case we want to read the stderr of ffmpeg
            new Thread(() -> {
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String str;
                try {
                    while ((str = reader.readLine()) != null) {
                        System.out.println(str);
                    }
                } catch (IOException e) {
                    System.out.println("io exception");
                }

            }).start();

            // stream the output from ffmpeg
            return ok(process.getInputStream());

        } catch (IOException e) {
            e.printStackTrace();
            return internalServerError();
        }
    }

}

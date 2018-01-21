# Play + ffmpeg on-the-fly streamer

This is an experiment trying to port [derolf/transcode](https://github.com/derolf/transcoder) from Python/Flask to Java/Play. This simple web app transcodes a sample video (`sample.avi`) into h264 format so that it's playable in an html5 `<video>` element. The transcoding happens on-the-fly so that the video doesn't have to be fully converted before it's served to the client. It also supports seeking through the video.

The backend app has 4 routes (`conf/routes`), all of which are handled by `app/controllers/VideoController`:

* `/` - serves the HTML/JavaScript client which plays the video and performs seeking
* `/video.mp4` - streams the video with ffmpeg with optional `?t=10` query paramter for setting the seek time in seconds.
* `/video.jpg` - gets a screenshot of a frame to be used as the poster of the `<video>`
* `/video.metadata` - gets the duration of the video in seconds.

The frontend app can be found at `app/views/index.scala.html` and `public/javascripts/main.js`. Seeking may seem a little strange. It doesn't use the built-in `<video>` scrubber but rather a `<input type="range"/>` element. I tried using the native scrubber but the issue was that the full duration isn't passed as part of the stream so you couldn't do any useful calculations for seconds to seek.

Hopefully you find this sample project useful!

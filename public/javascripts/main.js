loadVideoMetadata();
setInterval(tick, 1000);

var httpRequest;
var video = {
    duration: 0,
    elapsedSeconds: 0,
    paused: true
};

var videoEl = document.getElementById("video");
var seekerEl = document.getElementById("videoSeeker");
var durationEl = document.getElementById("videoDuration");
var elapsedTimeEl = document.getElementById("elapsedTime");
var playBtn = document.getElementById("playPause");

seekerEl.addEventListener('change', seek);
videoEl.addEventListener('waiting', waiting);
videoEl.addEventListener('playing', playing);
playBtn.addEventListener('click', playVideo);

// load the duration of the video
function loadVideoMetadata(){
    httpRequest = new XMLHttpRequest();
    httpRequest.onreadystatechange = setVideoMetadata;
    httpRequest.open('GET', '/video.metadata');
    httpRequest.send();
}

// update the timestamps for duration and set min/max for seeker bar
function setVideoMetadata() {
    if (httpRequest.readyState === XMLHttpRequest.DONE) {
        if (httpRequest.status === 200) {
            var videoMeta = JSON.parse(httpRequest.responseText);
            video.duration = videoMeta.duration;
            durationEl.innerText = formatTime(video.duration);
            elapsedTimeEl.innerText = formatTime(0);
            seekerEl.min = 0;
            seekerEl.max = video.duration;
            seekerEl.value = 0;
            videoEl.src = "/video.mp4?t=0";
        } else {
            alert('There was a problem with the request.');
        }
    }
}

// start the video after the metadata has been fetched
function playVideo() {
    video.paused = !video.paused;
    video.paused ? videoEl.pause() : videoEl.play();
}

// triggers every second to update seeker bar
function tick(){
    if(!video.paused && video.elapsedSeconds < video.duration){
        video.elapsedSeconds += 1;
        elapsedTimeEl.innerText = formatTime(video.elapsedSeconds);
        seekerEl.value = video.elapsedSeconds;
    }
}

// triggered when the seek bar is seeked
function seek(event){
    videoEl.pause();
    video.elapsedSeconds = +event.target.value;
    videoEl.src = "/video.mp4?t=" + video.elapsedSeconds;
    videoEl.play();
}

// triggered when the video is loading and not playing
function waiting(){
    video.paused = true;
}

// triggered when the video has resumed playing
function playing() {
    video.paused = false;
}

// helper to pretty print elapsed time and duration
function formatTime(time){
    var hours = Math.floor(time / 3600);
    time = time - hours * 3600;
    var minutes = Math.floor(time / 60);
    var seconds = time - minutes * 60;
    return pad2(hours) + ':' + pad2(minutes) + ':' + pad2(seconds);
}

// pad with 0
function pad2(string) {
    return (new Array(3).join('0')+string).slice(-2);
}

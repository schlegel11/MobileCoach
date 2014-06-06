#!/bin/sh
gitstats -c project_name="MobileCoach (MC)" . ~/GIT/Statistics/MC
gource --date-format "%Y-%m-%d %H:%M:%S" --hide progress,mouse --title "MobileCoach (MC)" --auto-skip-seconds 1 --seconds-per-day 3 --file-idle-time 0 --camera-mode track -1280x720 -o - | ffmpeg -y -r 60 -f image2pipe -vcodec ppm -i - -vcodec libx264 -preset ultrafast -pix_fmt yuv420p -crf 1 -threads 0 -bf 0 ~/Desktop/MC-Statistics.mp4

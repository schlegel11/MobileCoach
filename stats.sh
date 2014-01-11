#!/bin/sh
gitstats -c project_name="Mobile Health Coach (MHC)" . ~/GIT/Statistics/MHC
gource --date-format "%Y-%m-%d %H:%M:%S" --hide progress,mouse --title "Mobile Health Coach (MHC)" --auto-skip-seconds 1 --seconds-per-day 3 --camera-mode track -1280x720 -o - | ffmpeg -y -r 60 -f image2pipe -vcodec ppm -i - -vcodec libx264 -preset ultrafast -pix_fmt yuv420p -crf 1 -threads 0 -bf 0 gource.mp4

#!/bin/bash
# Request sudo if not already running as root
if [ "$(id -u)" != "0" ]; then
    echo "This script must be run as root. Requesting sudo access..."
    exec sudo "$0" "$@"
fi

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
sudo java -jar "$DIR/${project.artifactId}-${project.version}-jar-with-dependencies.jar"

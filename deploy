#!/bin/sh

if [ ! -e target ]; then
  echo No target directory found, running install
  mvn clean install -f pom.xml
fi

echo Deploying to server
scp target/simplify-truths.war pi@ssh.martials.no:~/sites-updates
echo war file deployed

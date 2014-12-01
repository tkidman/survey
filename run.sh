#!/bin/sh

# ensure we have an empty output directory
mkdir -p output
rm -f output/*

# run the app with our sample data
java -cp target/tracar-1.0-SNAPSHOT.jar tkidman.tracar.app.Tracar sample_data.txt
echo "reports created in ./output/"

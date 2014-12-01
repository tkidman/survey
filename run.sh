#!/bin/sh

rm output/*
java -cp target/tracar-1.0-SNAPSHOT.jar tkidman.tracar.app.Tracar sample_data.txt

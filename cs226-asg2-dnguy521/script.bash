#!/bin/bash
cd Desktop/cs226-asg2-dnguy521
mvn package
spark-submit --class edu.ucr.cs.cs226.dnguy521.RDDJobs target/assignment2-1.0-SNAPSHOT.jar nasa.tsv

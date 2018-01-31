# msr18-arch

This repository is the source code for the "On the Relationship Between Architectural Changes and Continuous Integration Build Outcome" paper. 

# Results

All found results can be found and recalculated from results/. The first analysis, using only ten projects, is stored under firstResults, while the real analysis of all projects is stored under results/final.

# Setup

## Get Database

Download TravisTorrent: https://travistorrent.testroots.org/dumps/travistorrent_8_2_2017.sql.gz

Create new SQL Database / User

```
create user 'archi'@'localhost' identified by 'archi';
create database archi;
grant all privileges on archi.* to archi@'localhost';
flush privileges;

zcat travistorrent_8_2_2017.sql.gz | mysql -u archi -p archi
```

Password: archi

Wait a little bit

## Add Local Maven Dependencies

Go to folder source

```
mvn install:install-file -Dfile=local-mvn-repo/arcade.jar -DgroupId=edu.usc.softarch -DartifactId=arcade -Dversion=0.1.0 -Dpackaging=jar
mvn install:install-file -Dfile=local-mvn-repo/HUSACCT.jar -DgroupId=nl.hu.husacct -DartifactId=husacct -Dversion=5.4.0 -Dpackaging=jar
```

## Build Jar

```
mvn clean install
```

Use *-shaded.jar. 

Important: The folders arcadepy and cfg must be in the same folder as the *.jar file

# Usage

The already created binaries are in the bin/ folder.

## Extraction

Invoke with 
```
java -jar architecture-0.0.1-SNAPSHOT-shaded.jar PROJECT_NAME VALUE
```

If VALUE is 0 all project versions will be analyzed, if set to a positive integer this many versions will be analyzed. If set to -1 a JSON database file for this project is created out of the MySQL database (if script needs to run on a machine without MySQL)

Results will be in the extracted/ folder.

Altnernatively you can create all database files and run on all projects using the two shell scripts in the bin/ folder. The results for every project will be stored in the extracted/ folder.

## Analysis

With the combine.sh script, all necessary files get copied into one directory which can be used afterwards for simpler usage. Some basic information about the extracted projects can be received from the scripts.sh script.
To calculate the correlations, plots and interesting informations, invoke the readjson.py script in the bin/ folder. 

# Add more Analysis Tools

To extend the analysis with another extractor, reconstructor or metric calculator, follow these steps:

1. Create Subclass of the corrsesponding abstract class "AbstractClassGraphExtractor", "AbstractArchitectureReconstructor" or "AbstractArchitectureSimilarityComputer". 
2. Implement the necessary methods according to their description. If you don't know what to do, just try follow the steps of the package reconstruction.
3. Change the Factory to include your new Tool.
4. Done


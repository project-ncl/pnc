Contains the DBMS schema and Java sources related to connecting to the database.
The current schema SQL is located in src/main/sql

## Contributing Database Schema Updates

If you would like to submit a change to the database schema such as adding a new table, 
or altering an existing table, the first step is to make the changes either to either 
a local postgres database or your personal openshift postgres database.  Next, locate 
the current db schema script in the project sources (src/main/sql).  Backup the existing 
schema file.
```
$ cp newcastle_schema.sql newcastle_schema_old.sql
```
Dump the new schema for your database to overwrite the existing schema text file.
```
$ pg_dump -h localhost -U <username> -s newcastle > newcastle_schema.sql
```
Next, use the apgdiff tool (http://apgdiff.com/) to create a schema diff file which 
can be run against the existing database.
```
$ java -jar apgdiff.jar newcastle_schema_old.sql newcastle_schema.sql > upgrade_schema.sql
```
Then commit the changes to the sql files, along with any required changes to the Java 
sources, to the git repository, and then submit a pull request.  At this point it will 
be up to you or someone else on the team to update the shared database schema using 
the upgrade script (schema_upgrade.sql) and deploy the updated webapp to the 
development server.
```
$ psql -h localhost -U <username> -f upgrade_schema.sql newcastle
```

## How to use PostgreSQL Database

You can use a PostgreSQL database with the `Pnc` application. As shown below, the application server must first be configured for PostgreSQL and the `newcastle` PostgreSQL database must exist before the application server can be configured with the associated `Pnc` datasource.

### Create the Database and User

1.  Make sure the PostgreSQL bin directory is in your PATH. Open a command line and change to the root directory psql. If you see an error that 'psql' is not a recognized command, you need to add the PostgreSQL bin directory to your PATH environment variable.
2.  Switch to the postgres user:
```
su - postgres
```
3.  Create the `newcastle` database:
```
createdb newcastle
```
4.  Start the PostgreSQL interactive terminal:
```
psql -U postgres
```
5.  Create `newcastle` user and grant all privileges on `newcastle` database.
```
create user newcastle with password 'newcastle';
grant all privileges on database newcastle to newcastle;
```
6.  Quit the interactive terminal and exit:
```
\q
exit
```

### Add the PostgreSQL Module to the JBoss server

To complete these prerequisite processes and configure the application server for the `newcastle` PostgreSQL database, complete the following steps:

1.  Create the following directory structure:
```
mkdir -p $JBOSS_HOME/modules/org/postgresql/main
```
2.  Add the PostgreSQL JDBC driver into the directory you created in the previous step:
```
mvn dependency:copy -Dartifact=org.postgresql:postgresql:9.2-1004-jdbc41 -DoutputDirectory=$JBOSS_HOME/modules/org/postgresql/main/
```
3.  In the same directory, create a file named `module.xml`. Copy the following contents into the file:
```
<?xml version="1.0" encoding="UTF-8"?>
<module xmlns="urn:jboss:module:1.0" name="org.postgresql">
    <resources>
        <resource-root path="postgresql-9.2-1004-jdbc41.jar"/>
    </resources>
    <dependencies>
        <module name="javax.api"/>
        <module name="javax.transaction.api"/>
    </dependencies>
</module>
```

### Import the database script

Find the import schema script in folder `$PNC_SRC_FOLDER/datastore/src/main/sql/newcastle_schema.sql` and run the import command (providing password `newcastle` when prompted)
```
psql -h localhost -U newcastle -f `$PNC_SRC_FOLDER/datastore/src/main/sql/newcastle_schema.sql` newcastle
```

### Add the Driver Configuration to the JBoss server

#### WildFly Server

1. Start the WildFly Server by typing the following:
```
For Linux:  $JBOSS_HOME_SERVER_1/bin/standalone.sh
For Windows:  $JBOSS_HOME_SERVER_1\bin\standalone.bat
```
        
2. Open a new command line, navigate to the folder `$PNC_SRC_FOLDER/datastore/src` and run the following command:
```
$JBOSS_HOME/bin/jboss-cli.sh --file=/postgresql-database-config-wildfly.cli
```

This script adds the PostgreSQL driver to the datasources subsystem in the server configuration. You should see the following result when you run the script:
```
The batch executed successfully
```

#### EAP Server

1. Start the EAP Server by typing the following:
```
For Linux:  $JBOSS_HOME_SERVER_1/bin/standalone.sh
For Windows:  $JBOSS_HOME_SERVER_1\bin\standalone.bat
```
        
2. Open a new command line, navigate to the folder `$PNC_SRC_FOLDER/datastore/src` and run the following command:
```
$JBOSS_HOME/bin/jboss-cli.sh --file=/postgresql-database-config.cli
```

This script adds the PostgreSQL driver to the datasources subsystem in the server configuration. You should see the following result when you run the script:
```
The batch executed successfully
```

### Deploy the application
You can now safely deploy the application `Pnc`, as it should be able to connect to the newly created PostgreSQL database




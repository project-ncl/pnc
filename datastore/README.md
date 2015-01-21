Contains the DBMS schema and Java sources related to connecting to the database.
The current schema SQL is located in src/main/sql

Installing Postgres for Development
------------------------------------

Basic installation instructions for Postgres on recent versions of Fedora can be found on the Fedora wiki: https://fedoraproject.org/wiki/PostgreSQL

(Optional) In addition to the postgresql server, it's useful to install the gui admin tool pgadmin3 (yum install pgadmin3).

Once postgresql is installed and started, you can create and modify the databases using the psql command.

Connect to postgres

    $ psql -h localhost newcastle -U <ROOT_USERNAME>

Create the user "newcastle
Create user

    postgres=# CREATE USER newcastle WITH PASSWORD newcastle;

Create the newcastle database
Create newcastle database

    postgres=# CREATE DATABASE newcastle OWNER newcastle;

Once the database is created, the schema can be built using the SQL files included with the newcastle source code, or it can be created automatically if it doesn't exist.

Configuring the Datasource
--------------------------
You will need to download and install the PostgreSQL JDBC driver into Wildfly (https://jdbc.postgresql.org/download.html).  Copy the postgresql jdbc driver jar into the standalone/deployments directory of your JBoss server.  Check the server log to see whether the driver deployed successfully.

From the EAP/Wildfly admin console, select Configuration-->Connector-->Datasources.
Click the "Add" button to add a new datasource and set the required fields.

    Name: NewcastleDS
    JNDI Name: java:jboss/datasources/NewcastleDS
    JDBC Driver: postgresql-9.3.jar
    Connection URL: jdbc:postgresql://localhost:5432/newcastle
    Username: newcastle
    Password: newcastle

You can test the connection before saving the datasource settings.

Contributing Database Schema Updates
------------------------------------

If you would like to submit a change to the database schema such as adding a new table, 
or altering an existing table, the first step is to make the changes either to either 
a local postgres database or your personal openshift postgres database.  Next, locate 
the current db schema script in the project sources (src/main/sql).  Backup the existing 
schema file.

    $ cp newcastle_schema.sql newcastle_schema_old.sql

Dump the new schema for your database to overwrite the existing schema text file.

    $ pg_dump -h localhost -U <username> -s newcastle > newcastle_schema.sql

Next, use the apgdiff tool (http://apgdiff.com/) to create a schema diff file which 
can be run against the existing database.

    $ java -jar apgdiff.jar newcastle_schema_old.sql newcastle_schema.sql > upgrade_schema.sql

Then commit the changes to the sql files, along with any required changes to the Java 
sources, to the git repository, and then submit a pull request.  At this point it will 
be up to you or someone else on the team to update the shared database schema using 
the upgrade script (schema_upgrade.sql) and deploy the updated webapp to the 
development server.

    $ psql -h localhost -U <username> -f upgrade_schema.sql newcastle


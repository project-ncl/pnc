Contains the DBMS schema and Java sources related to connecting to the database.
The current schema SQL is located in src/main/sql

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
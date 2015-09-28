HAT 2.0
=======

Starting afresh with the new schema...

Starting with a fairly auto-generated data access layer for the HAT

You will need to set up a PostgreSQL database with the HAT2.0 schema, files for which can be found in `src/sql`.

As simple as:

    psql databasename < src/sql/HAT-V2.0.sql

Configuration in *both* `codegen/src/main/resources/application.conf` and `src/main/resources/application.conf` must reflect your database configuration (can be different ones for model generation and for operation), and look similar to:

    devdb = {
      dataSourceClass = "org.postgresql.ds.PGSimpleDataSource"
      properties = {
        databaseName = "database"
        user = "dbuser"
        password = "dbpass"
      }
    }

## Auto-generated code recompilation

You should have the right code for database access already in your code, but if you want to regenerate it (for example because you have changed your database structure), you can rerun it with:

    sbt
    #Enters sbt console
    > clean
    > gen-tables

*Note*: for some reason simply running `sbt clean gen-tables` does not regenerate the tables

## Testing

Functionality is all tested via provided tests. To run this code, setup sbt and simply type

	sbt test

Which will run tests checking the db and autogenerated code. All tests should finish successfully.

## API

The API code lives in `src/main/scala/hatdex/hat/dalapi`



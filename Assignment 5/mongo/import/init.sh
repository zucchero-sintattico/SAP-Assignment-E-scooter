#!/bin/bash

# if env var is set to true then import the databases
if [ ! -d /Users/giacomototaro/Desktop/mongo/import/import_d ]; then
    echo "Importing databases"
    # import all mongo db from folder db
    for f in /databases/db/*; do
        # name without extension and path
        db_name="unoProgetto"
        db_coll="cards"
        mongoimport --host mongo --db $db_name --collection $db_coll --file $f --jsonArray
        echo "Imported $db_name"
    done
    mkdir -p /Users/giacomototaro/Desktop/mongo/import/import_d
else
    echo "Skipping database import"
fi
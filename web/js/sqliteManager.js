var database;
var EXPIRE_TIME = 1000 * 10;

function connectDB() {
    try {
        if (!window.openDatabase) {
            alert('not supported');
        }
        else {
            // setting for our database
            var shortName = 'recital_pianos';
            var version = '1.0';
            var displayName = 'Recital Pianos';
            var maxSize = 65536; // in bytes

            database = openDatabase(shortName, version, displayName, maxSize);
        }
    } catch (e) {
        // Error handling code goes here.
        if (e == 2) {
            // Version number mismatch.
            alert("Invalid database version.");
        } else {
            alert("Unknown error " + e + ".");
        }
    }
}
;

function myTransactionErrorCallback(error) {
    console.log('Oops.  Error was ' + error.message + ' (Code ' + error.code + ')');
    return true;
}

function myTransactionSuccessCallback() {
    console.log("transaction successful");
}

// Create table
var pianosTable = {
    NAME: "Pianos",
    COL_PIANO_ID: "pianoId",
    COL_IS_UPRIGHT: "isUpright",
    COL_MODEL: "model",
    COL_MANUFACTURER: "manufacturers",
    COL_COUNTRY: "country",
    COL_SIZE: "size",
    COL_COLOR: "color",
    COL_FINISH: "finish",
    COL_YEAR: "year",
    COL_IS_NEW: "isNew",
    COL_IMG_URL: "imgUrl"
};

var benchesTable = {
    NAME: "Benches",
    COL_BENCH_ID: "benchId",
    COL_MODEL: "model",
    COL_IS_ADJUSTABLE: "isAdjustable",
    COL_COLOR: "color",
    COL_FABRIC: "fabric",
    COL_IMG_URL: "imgUrl"
};

var settingsTable = {
    NAME: "Settings",
    COL_ID: "settingId",
    COL_LAST_UPDATE_DATE: "lastUpdate"
};

function initDB() {
    console.log("initDB > started");

    database.transaction(
            function (tx) {

                //tx.executeSql("DROP TABLE " + settingsTable.NAME + ";");

                var createSettingsTableQuery = "CREATE TABLE IF NOT EXISTS " + settingsTable.NAME + " ("
                        + settingsTable.COL_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                        + settingsTable.COL_LAST_UPDATE_DATE + " INTEGER NOT NULL);";
                tx.executeSql(createSettingsTableQuery);

                var query = "SELECT * FROM " + settingsTable.NAME + " WHERE " + settingsTable.COL_ID + " = 1;";
                tx.executeSql(query, [],
                        function (tx, results) {
                            if (results.rows.length === 0 || results.rows.item(0)[settingsTable.COL_LAST_UPDATE_DATE] + EXPIRE_TIME <= Date.now()) {
                                console.log("initDB > updateing DB.");

                                updatePianos();
                                updateBenches();

                                var updateSettingsTimeQuery = "INSERT OR REPLACE INTO " + settingsTable.NAME + " ("
                                        + settingsTable.COL_ID + ", "
                                        + settingsTable.COL_LAST_UPDATE_DATE + ") VALUES (1, " + Date.now() + ");";

                                tx.executeSql(updateSettingsTimeQuery);

                            } else {
                                console.log("initDB > db is up to date.");
                            }
                        });
            }, myTransactionErrorCallback, function () {
        console.log("initDB > complete");
    });
}


function updatePianos() {
    console.log("updatePianos > started");

    $.ajax({
        url: "api/catalog/pianos",
        dataType: 'json',
        success: function (data) {
            database.transaction(
                    function (tx) {

                        // Create table if don't exist
                        var createPianosTableQuery = "CREATE TABLE IF NOT EXISTS " + pianosTable.NAME + " ("
                                + pianosTable.COL_PIANO_ID + " INTEGER NOT NULL PRIMARY KEY, "
                                + pianosTable.COL_IS_UPRIGHT + " INTEGER NOT NULL, "
                                + pianosTable.COL_MODEL + " TEXT, "
                                + pianosTable.COL_MANUFACTURER + " TEXT, "
                                + pianosTable.COL_COUNTRY + " TEXT, "
                                + pianosTable.COL_SIZE + " INTEGER NOT NULL, "
                                + pianosTable.COL_COLOR + " TEXT NOT NULL, "
                                + pianosTable.COL_FINISH + " TEXT NOT NULL, "
                                + pianosTable.COL_YEAR + " INTEGER, "
                                + pianosTable.COL_IS_NEW + " INTEGER NOT NULL, "
                                + pianosTable.COL_IMG_URL + " TEXT NOT NULL);";

                        tx.executeSql(createPianosTableQuery);

                        // Clear table
                        emptyTable(tx, pianosTable.NAME);

                        // Build bulk insert query
                        if (0 < data.length) {
                            var insertPianosQuery = "INSERT INTO " + pianosTable.NAME + " "
                                    + "SELECT "
                                    + data[0].id + " AS " + pianosTable.COL_PIANO_ID + ", "
                                    + convertBooleanToNumber(data[0].isUpright) + " AS " + pianosTable.COL_IS_UPRIGHT + ", "
                                    + "'" + data[0].model + "'" + " AS " + pianosTable.COL_MODEL + ", "
                                    + "'" + data[0].manufacturer + "'" + " AS " + pianosTable.COL_MANUFACTURER + ", "
                                    + "'" + data[0].country + "'" + " AS " + pianosTable.COL_COUNTRY + ", "
                                    + data[0].size + " AS " + pianosTable.COL_SIZE + ", "
                                    + "'" + data[0].color + "'" + " AS " + pianosTable.COL_COLOR + ", "
                                    + "'" + data[0].finish + "'" + " AS " + pianosTable.COL_FINISH + ", "
                                    + data[0].year + " AS " + pianosTable.COL_YEAR + ", "
                                    + convertBooleanToNumber(data[0].isNew) + " AS " + pianosTable.COL_IS_NEW + ", "
                                    + "'" + data[0].img + "'" + " AS " + pianosTable.COL_IMG_URL;

                            for (var i = 1; i < data.length; i++) {
                                insertPianosQuery += " UNION SELECT "
                                        + data[i].id + ", "
                                        + convertBooleanToNumber(data[i].isUpright) + ", "
                                        + "'" + data[i].model + "'" + ", "
                                        + "'" + data[i].manufacturer + "'" + ", "
                                        + "'" + data[i].country + "'" + ", "
                                        + data[i].size + ", "
                                        + "'" + data[i].color + "', "
                                        + "'" + data[i].finish + "',"
                                        + data[i].year + ", "
                                        + convertBooleanToNumber(data[i].isNew) + ", "
                                        + "'" + data[i].img + "'";
                            }
                            insertPianosQuery += ";";

                            // Execute bulk insert query
                            tx.executeSql(insertPianosQuery);
                        }

                    }, myTransactionErrorCallback, function () {
                console.log("updatePianos > complete");
            });
        },
        error: function (error) {
            console.log("updatePianos > ajax error: " + error.statusText + " (" + error.status + ")");
        }
    });
}

function updateBenches() {
    console.log("updateBenches > started");

    $.ajax({
        url: "api/catalog/benches",
        dataType: 'json',
        success: function (data) {
            database.transaction(
                    function (tx) {

                        // Create table if don't exist
                        var createBenchesTableQuery = "CREATE TABLE IF NOT EXISTS " + benchesTable.NAME + " ("
                                + benchesTable.COL_BENCH_ID + " INTEGER NOT NULL PRIMARY KEY, "
                                + benchesTable.COL_MODEL + " TEXT NOT NULL, "
                                + benchesTable.COL_IS_ADJUSTABLE + " INTEGER NOT NULL, "
                                + benchesTable.COL_COLOR + " TEXT NOT NULL, "
                                + benchesTable.COL_FABRIC + " TEXT NOT NULL, "
                                + benchesTable.COL_IMG_URL + " TEXT NOT NULL);";

                        tx.executeSql(createBenchesTableQuery);

                        // Clear table
                        emptyTable(tx, benchesTable.NAME);

                        // Build bulk insert query
                        if (0 < data.length) {
                            var insertBenchesQuery = "INSERT INTO " + benchesTable.NAME + " "
                                    + "SELECT "
                                    + data[0].id + " AS " + benchesTable.COL_BENCH_ID + ", "
                                    + "'" + data[0].model + "'" + " AS " + benchesTable.COL_MODEL + ", "
                                    + convertBooleanToNumber(data[0].isAdjustable) + " AS " + benchesTable.COL_IS_ADJUSTABLE + ", "
                                    + "'" + data[0].color + "'" + " AS " + benchesTable.COL_COLOR + ", "
                                    + "'" + data[0].fabric + "'" + " AS " + benchesTable.COL_FABRIC + ", "
                                    + "'" + data[0].img + "'" + " AS " + benchesTable.COL_IMG_URL;
                            for (var i = 1; i < data.length; i++) {
                                insertBenchesQuery += " UNION SELECT "
                                        + data[i].id + ", "
                                        + "'" + data[0].model + "', "
                                        + convertBooleanToNumber(data[i].isAdjustable) + ", "
                                        + "'" + data[i].color + "', "
                                        + "'" + data[i].fabric + "', "
                                        + "'" + data[i].img + "'";
                            }
                            insertBenchesQuery += ";";

                            // Execute bulk insert query
                            tx.executeSql(insertBenchesQuery);
                        }

                    }, myTransactionErrorCallback, function () {
                console.log("updateBenches > complete");
            });
        },
        error: function (error) {
            console.log("updateBenches > ajax error: " + error.statusText + " (" + error.status + ")");
        }
    });
}

// Get Table Action
var getPianos = function (callback) {
    getTable(pianosTable.NAME, createPianoObject, callback);
};

var getBenches = function (callback) {
    getTable(benchesTable.NAME, createBencheObject, callback);
};

var getTable = function (tableName, createObjFunc, callback) {
    database.transaction(
            function (transaction) {
                transaction.executeSql(
                        "SELECT * FROM " + tableName + ";",
                        [],
                        function (transaction, results) {
                            var data = [];

                            for (var i = 0; i < results.rows.length; i++) {
                                data.push(createObjFunc(results.rows.item(i)));
                            }

                            callback(data);
                        }
                );
            }
    );
};

function emptyTable(tx, tableName) {
    var emptyTableQuery = "DELETE FROM " + tableName + ";";
    tx.executeSql(emptyTableQuery);
}

function cleanDB() {
    dropTable(pianosTable.NAME);
    dropTable(benchesTable.NAME);
    dropTable(settingsTable.NAME);
}

function dropTable(tableName) {
    database.transaction(
            function (transaction) {
                transaction.executeSql("DROP TABLE " + tableName + ";");
            }, myTransactionErrorCallback, function () {
        console.log("table " + tableName + " deleted");
    });
}

function createPianoObject(row) {
    var Piano = {};

    Piano.id = row[pianosTable.COL_PIANO_ID];
    Piano.isUpright = convertNumberToBoolean(row[pianosTable.COL_IS_UPRIGHT]);
    Piano.model = row[pianosTable.COL_MODEL];
    Piano.manufacturer = row[pianosTable.COL_MANUFACTURER];
    Piano.country = row[pianosTable.COL_COUNTRY];
    Piano.size = row[pianosTable.COL_SIZE];
    Piano.color = row[pianosTable.COL_COLOR];
    Piano.finish = row[pianosTable.COL_FINISH];
    Piano.year = row[pianosTable.COL_YEAR];
    Piano.isNew = convertNumberToBoolean(row[pianosTable.COL_IS_NEW]);
    Piano.img = row[pianosTable.COL_IMG_URL];

    return Piano;
}

function createBencheObject(row) {
    var benche = {};

    benche.id = row[benchesTable.COL_BENCH_ID];
    benche.model = row[benchesTable.COL_MODEL];
    benche.isAdjustable = convertNumberToBoolean(row[benchesTable.COL_IS_ADJUSTABLE]);
    benche.color = row[benchesTable.COL_COLOR];
    benche.fabric = row[benchesTable.COL_FABRIC];
    benche.img = row[benchesTable.COL_IMG_URL];

    return benche;
}

function convertBooleanToNumber(boolean) {
    return boolean == true ? 1 : 0;
}
function convertNumberToBoolean(number) {
    return number == 0 ? false : true;
}

connectDB();
//cleanDB();
initDB();

getBenches(function (benches) {
    console.log(JSON.stringify(benches));
});

getPianos(function (pianos) {
    console.log(JSON.stringify(pianos));
});

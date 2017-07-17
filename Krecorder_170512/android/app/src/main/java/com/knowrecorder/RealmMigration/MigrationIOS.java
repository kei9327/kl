package com.knowrecorder.RealmMigration;

import android.util.Log;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * Created by we160303 on 2017-02-20.
 */

public class MigrationIOS implements RealmMigration {

    @Override
    public int hashCode() {
        return 37;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof MigrationIOS);
    }

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        Log.d("Migratioin", "oldVersion : " + oldVersion + "  newVersion : " + newVersion);
        RealmSchema schema = realm.getSchema();

        // Migrate from version 0 to version 1
        if (oldVersion == 0) {
            RealmObjectSchema noteSchema = schema.get("Note");
            if(!noteSchema.hasField("info"))
                noteSchema.addField("info", String.class);
        }

        if(oldVersion == 1) {

            RealmObjectSchema pageSchema = schema.get("Page");
            if(!pageSchema.hasField("runtime"))
                pageSchema.addField("runtime", float.class);

            RealmObjectSchema noteSchema = schema.get("Note");
            if (!noteSchema.hasField("totaltime"))
                noteSchema.addField("totaltime", String.class);

            RealmObjectSchema packetObjectSchema = schema.get("PacketObject");
            if(!packetObjectSchema.hasField("runtime"))
                packetObjectSchema.addField("runtime", float.class);

            RealmObjectSchema youTubeData = schema.get("YouTubeData");
            if(!youTubeData.hasField("totalTime"))
                youTubeData.addField("totalTime", float.class);

            oldVersion++;
        }

        if(oldVersion == 2) {
            RealmObjectSchema noteSchema = schema.get("Note");
            if(!noteSchema.hasField("info"))
                noteSchema.addField("info", String.class);
            oldVersion++;
        }

        if(oldVersion == 3) {

            if(!schema.contains("Notes")) {
                schema.create("Notes")
                        .addField("noteName", String.class)
                        .addField("title", String.class)
                        .addField("createDate", String.class)
                        .addField("totaltime", float.class);
            }
            oldVersion++;
        }

        if(oldVersion == 4) {
            if(!schema.contains("TimeLine")) {
                schema.create("TimeLine")
                        .addField("mid", long.class)
                        .addField("startRun", float.class)
                        .addField("endRun", float.class)
                        .addField("type", String.class)
                        .addField("remarks", String.class);
            }
            oldVersion++;
        }
    }
}

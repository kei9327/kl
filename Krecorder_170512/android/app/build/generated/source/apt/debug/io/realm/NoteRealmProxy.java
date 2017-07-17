package io.realm;


import android.annotation.TargetApi;
import android.os.Build;
import android.util.JsonReader;
import android.util.JsonToken;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.ColumnInfo;
import io.realm.internal.LinkView;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Row;
import io.realm.internal.SharedRealm;
import io.realm.internal.Table;
import io.realm.internal.TableOrView;
import io.realm.internal.android.JsonUtils;
import io.realm.log.RealmLog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NoteRealmProxy extends com.knowrecorder.develop.model.realm.Note
    implements RealmObjectProxy, NoteRealmProxyInterface {

    static final class NoteColumnInfo extends ColumnInfo
        implements Cloneable {

        public long idIndex;
        public long createDateIndex;
        public long titleIndex;
        public long totaltimeIndex;
        public long isCurrentUsingIndex;
        public long infoIndex;

        NoteColumnInfo(String path, Table table) {
            final Map<String, Long> indicesMap = new HashMap<String, Long>(6);
            this.idIndex = getValidColumnIndex(path, table, "Note", "id");
            indicesMap.put("id", this.idIndex);
            this.createDateIndex = getValidColumnIndex(path, table, "Note", "createDate");
            indicesMap.put("createDate", this.createDateIndex);
            this.titleIndex = getValidColumnIndex(path, table, "Note", "title");
            indicesMap.put("title", this.titleIndex);
            this.totaltimeIndex = getValidColumnIndex(path, table, "Note", "totaltime");
            indicesMap.put("totaltime", this.totaltimeIndex);
            this.isCurrentUsingIndex = getValidColumnIndex(path, table, "Note", "isCurrentUsing");
            indicesMap.put("isCurrentUsing", this.isCurrentUsingIndex);
            this.infoIndex = getValidColumnIndex(path, table, "Note", "info");
            indicesMap.put("info", this.infoIndex);

            setIndicesMap(indicesMap);
        }

        @Override
        public final void copyColumnInfoFrom(ColumnInfo other) {
            final NoteColumnInfo otherInfo = (NoteColumnInfo) other;
            this.idIndex = otherInfo.idIndex;
            this.createDateIndex = otherInfo.createDateIndex;
            this.titleIndex = otherInfo.titleIndex;
            this.totaltimeIndex = otherInfo.totaltimeIndex;
            this.isCurrentUsingIndex = otherInfo.isCurrentUsingIndex;
            this.infoIndex = otherInfo.infoIndex;

            setIndicesMap(otherInfo.getIndicesMap());
        }

        @Override
        public final NoteColumnInfo clone() {
            return (NoteColumnInfo) super.clone();
        }

    }
    private NoteColumnInfo columnInfo;
    private ProxyState<com.knowrecorder.develop.model.realm.Note> proxyState;
    private static final List<String> FIELD_NAMES;
    static {
        List<String> fieldNames = new ArrayList<String>();
        fieldNames.add("id");
        fieldNames.add("createDate");
        fieldNames.add("title");
        fieldNames.add("totaltime");
        fieldNames.add("isCurrentUsing");
        fieldNames.add("info");
        FIELD_NAMES = Collections.unmodifiableList(fieldNames);
    }

    NoteRealmProxy() {
        if (proxyState == null) {
            injectObjectContext();
        }
        proxyState.setConstructionFinished();
    }

    private void injectObjectContext() {
        final BaseRealm.RealmObjectContext context = BaseRealm.objectContext.get();
        this.columnInfo = (NoteColumnInfo) context.getColumnInfo();
        this.proxyState = new ProxyState<com.knowrecorder.develop.model.realm.Note>(com.knowrecorder.develop.model.realm.Note.class, this);
        proxyState.setRealm$realm(context.getRealm());
        proxyState.setRow$realm(context.getRow());
        proxyState.setAcceptDefaultValue$realm(context.getAcceptDefaultValue());
        proxyState.setExcludeFields$realm(context.getExcludeFields());
    }

    @SuppressWarnings("cast")
    public long realmGet$id() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (long) proxyState.getRow$realm().getLong(columnInfo.idIndex);
    }

    public void realmSet$id(long value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        if (proxyState.isUnderConstruction()) {
            // default value of the primary key is always ignored.
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        throw new io.realm.exceptions.RealmException("Primary key field 'id' cannot be changed after object was created.");
    }

    @SuppressWarnings("cast")
    public String realmGet$createDate() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.createDateIndex);
    }

    public void realmSet$createDate(String value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.createDateIndex, row.getIndex(), true);
                return;
            }
            row.getTable().setString(columnInfo.createDateIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.createDateIndex);
            return;
        }
        proxyState.getRow$realm().setString(columnInfo.createDateIndex, value);
    }

    @SuppressWarnings("cast")
    public String realmGet$title() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.titleIndex);
    }

    public void realmSet$title(String value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.titleIndex, row.getIndex(), true);
                return;
            }
            row.getTable().setString(columnInfo.titleIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.titleIndex);
            return;
        }
        proxyState.getRow$realm().setString(columnInfo.titleIndex, value);
    }

    @SuppressWarnings("cast")
    public float realmGet$totaltime() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (float) proxyState.getRow$realm().getFloat(columnInfo.totaltimeIndex);
    }

    public void realmSet$totaltime(float value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setFloat(columnInfo.totaltimeIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setFloat(columnInfo.totaltimeIndex, value);
    }

    @SuppressWarnings("cast")
    public boolean realmGet$isCurrentUsing() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (boolean) proxyState.getRow$realm().getBoolean(columnInfo.isCurrentUsingIndex);
    }

    public void realmSet$isCurrentUsing(boolean value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setBoolean(columnInfo.isCurrentUsingIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setBoolean(columnInfo.isCurrentUsingIndex, value);
    }

    @SuppressWarnings("cast")
    public String realmGet$info() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.infoIndex);
    }

    public void realmSet$info(String value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.infoIndex, row.getIndex(), true);
                return;
            }
            row.getTable().setString(columnInfo.infoIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.infoIndex);
            return;
        }
        proxyState.getRow$realm().setString(columnInfo.infoIndex, value);
    }

    public static RealmObjectSchema createRealmObjectSchema(RealmSchema realmSchema) {
        if (!realmSchema.contains("Note")) {
            RealmObjectSchema realmObjectSchema = realmSchema.create("Note");
            realmObjectSchema.add(new Property("id", RealmFieldType.INTEGER, Property.PRIMARY_KEY, Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("createDate", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED));
            realmObjectSchema.add(new Property("title", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED));
            realmObjectSchema.add(new Property("totaltime", RealmFieldType.FLOAT, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("isCurrentUsing", RealmFieldType.BOOLEAN, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("info", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED));
            return realmObjectSchema;
        }
        return realmSchema.get("Note");
    }

    public static Table initTable(SharedRealm sharedRealm) {
        if (!sharedRealm.hasTable("class_Note")) {
            Table table = sharedRealm.getTable("class_Note");
            table.addColumn(RealmFieldType.INTEGER, "id", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.STRING, "createDate", Table.NULLABLE);
            table.addColumn(RealmFieldType.STRING, "title", Table.NULLABLE);
            table.addColumn(RealmFieldType.FLOAT, "totaltime", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.BOOLEAN, "isCurrentUsing", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.STRING, "info", Table.NULLABLE);
            table.addSearchIndex(table.getColumnIndex("id"));
            table.setPrimaryKey("id");
            return table;
        }
        return sharedRealm.getTable("class_Note");
    }

    public static NoteColumnInfo validateTable(SharedRealm sharedRealm, boolean allowExtraColumns) {
        if (sharedRealm.hasTable("class_Note")) {
            Table table = sharedRealm.getTable("class_Note");
            final long columnCount = table.getColumnCount();
            if (columnCount != 6) {
                if (columnCount < 6) {
                    throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field count is less than expected - expected 6 but was " + columnCount);
                }
                if (allowExtraColumns) {
                    RealmLog.debug("Field count is more than expected - expected 6 but was %1$d", columnCount);
                } else {
                    throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field count is more than expected - expected 6 but was " + columnCount);
                }
            }
            Map<String, RealmFieldType> columnTypes = new HashMap<String, RealmFieldType>();
            for (long i = 0; i < columnCount; i++) {
                columnTypes.put(table.getColumnName(i), table.getColumnType(i));
            }

            final NoteColumnInfo columnInfo = new NoteColumnInfo(sharedRealm.getPath(), table);

            if (!table.hasPrimaryKey()) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Primary key not defined for field 'id' in existing Realm file. @PrimaryKey was added.");
            } else {
                if (table.getPrimaryKey() != columnInfo.idIndex) {
                    throw new RealmMigrationNeededException(sharedRealm.getPath(), "Primary Key annotation definition was changed, from field " + table.getColumnName(table.getPrimaryKey()) + " to field id");
                }
            }

            if (!columnTypes.containsKey("id")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'id' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("id") != RealmFieldType.INTEGER) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'long' for field 'id' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.idIndex) && table.findFirstNull(columnInfo.idIndex) != TableOrView.NO_MATCH) {
                throw new IllegalStateException("Cannot migrate an object with null value in field 'id'. Either maintain the same type for primary key field 'id', or remove the object with null value before migration.");
            }
            if (!table.hasSearchIndex(table.getColumnIndex("id"))) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Index not defined for field 'id' in existing Realm file. Either set @Index or migrate using io.realm.internal.Table.removeSearchIndex().");
            }
            if (!columnTypes.containsKey("createDate")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'createDate' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("createDate") != RealmFieldType.STRING) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'String' for field 'createDate' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.createDateIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'createDate' is required. Either set @Required to field 'createDate' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("title")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'title' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("title") != RealmFieldType.STRING) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'String' for field 'title' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.titleIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'title' is required. Either set @Required to field 'title' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("totaltime")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'totaltime' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("totaltime") != RealmFieldType.FLOAT) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'float' for field 'totaltime' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.totaltimeIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'totaltime' does support null values in the existing Realm file. Use corresponding boxed type for field 'totaltime' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("isCurrentUsing")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'isCurrentUsing' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("isCurrentUsing") != RealmFieldType.BOOLEAN) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'boolean' for field 'isCurrentUsing' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.isCurrentUsingIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'isCurrentUsing' does support null values in the existing Realm file. Use corresponding boxed type for field 'isCurrentUsing' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("info")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'info' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("info") != RealmFieldType.STRING) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'String' for field 'info' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.infoIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'info' is required. Either set @Required to field 'info' or migrate using RealmObjectSchema.setNullable().");
            }
            return columnInfo;
        } else {
            throw new RealmMigrationNeededException(sharedRealm.getPath(), "The 'Note' class is missing from the schema for this Realm.");
        }
    }

    public static String getTableName() {
        return "class_Note";
    }

    public static List<String> getFieldNames() {
        return FIELD_NAMES;
    }

    @SuppressWarnings("cast")
    public static com.knowrecorder.develop.model.realm.Note createOrUpdateUsingJsonObject(Realm realm, JSONObject json, boolean update)
        throws JSONException {
        final List<String> excludeFields = Collections.<String> emptyList();
        com.knowrecorder.develop.model.realm.Note obj = null;
        if (update) {
            Table table = realm.getTable(com.knowrecorder.develop.model.realm.Note.class);
            long pkColumnIndex = table.getPrimaryKey();
            long rowIndex = TableOrView.NO_MATCH;
            if (!json.isNull("id")) {
                rowIndex = table.findFirstLong(pkColumnIndex, json.getLong("id"));
            }
            if (rowIndex != TableOrView.NO_MATCH) {
                final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get();
                try {
                    objectContext.set(realm, table.getUncheckedRow(rowIndex), realm.schema.getColumnInfo(com.knowrecorder.develop.model.realm.Note.class), false, Collections.<String> emptyList());
                    obj = new io.realm.NoteRealmProxy();
                } finally {
                    objectContext.clear();
                }
            }
        }
        if (obj == null) {
            if (json.has("id")) {
                if (json.isNull("id")) {
                    obj = (io.realm.NoteRealmProxy) realm.createObjectInternal(com.knowrecorder.develop.model.realm.Note.class, null, true, excludeFields);
                } else {
                    obj = (io.realm.NoteRealmProxy) realm.createObjectInternal(com.knowrecorder.develop.model.realm.Note.class, json.getLong("id"), true, excludeFields);
                }
            } else {
                throw new IllegalArgumentException("JSON object doesn't have the primary key field 'id'.");
            }
        }
        if (json.has("createDate")) {
            if (json.isNull("createDate")) {
                ((NoteRealmProxyInterface) obj).realmSet$createDate(null);
            } else {
                ((NoteRealmProxyInterface) obj).realmSet$createDate((String) json.getString("createDate"));
            }
        }
        if (json.has("title")) {
            if (json.isNull("title")) {
                ((NoteRealmProxyInterface) obj).realmSet$title(null);
            } else {
                ((NoteRealmProxyInterface) obj).realmSet$title((String) json.getString("title"));
            }
        }
        if (json.has("totaltime")) {
            if (json.isNull("totaltime")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'totaltime' to null.");
            } else {
                ((NoteRealmProxyInterface) obj).realmSet$totaltime((float) json.getDouble("totaltime"));
            }
        }
        if (json.has("isCurrentUsing")) {
            if (json.isNull("isCurrentUsing")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'isCurrentUsing' to null.");
            } else {
                ((NoteRealmProxyInterface) obj).realmSet$isCurrentUsing((boolean) json.getBoolean("isCurrentUsing"));
            }
        }
        if (json.has("info")) {
            if (json.isNull("info")) {
                ((NoteRealmProxyInterface) obj).realmSet$info(null);
            } else {
                ((NoteRealmProxyInterface) obj).realmSet$info((String) json.getString("info"));
            }
        }
        return obj;
    }

    @SuppressWarnings("cast")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static com.knowrecorder.develop.model.realm.Note createUsingJsonStream(Realm realm, JsonReader reader)
        throws IOException {
        boolean jsonHasPrimaryKey = false;
        com.knowrecorder.develop.model.realm.Note obj = new com.knowrecorder.develop.model.realm.Note();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("id")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'id' to null.");
                } else {
                    ((NoteRealmProxyInterface) obj).realmSet$id((long) reader.nextLong());
                }
                jsonHasPrimaryKey = true;
            } else if (name.equals("createDate")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((NoteRealmProxyInterface) obj).realmSet$createDate(null);
                } else {
                    ((NoteRealmProxyInterface) obj).realmSet$createDate((String) reader.nextString());
                }
            } else if (name.equals("title")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((NoteRealmProxyInterface) obj).realmSet$title(null);
                } else {
                    ((NoteRealmProxyInterface) obj).realmSet$title((String) reader.nextString());
                }
            } else if (name.equals("totaltime")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'totaltime' to null.");
                } else {
                    ((NoteRealmProxyInterface) obj).realmSet$totaltime((float) reader.nextDouble());
                }
            } else if (name.equals("isCurrentUsing")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'isCurrentUsing' to null.");
                } else {
                    ((NoteRealmProxyInterface) obj).realmSet$isCurrentUsing((boolean) reader.nextBoolean());
                }
            } else if (name.equals("info")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((NoteRealmProxyInterface) obj).realmSet$info(null);
                } else {
                    ((NoteRealmProxyInterface) obj).realmSet$info((String) reader.nextString());
                }
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        if (!jsonHasPrimaryKey) {
            throw new IllegalArgumentException("JSON object doesn't have the primary key field 'id'.");
        }
        obj = realm.copyToRealm(obj);
        return obj;
    }

    public static com.knowrecorder.develop.model.realm.Note copyOrUpdate(Realm realm, com.knowrecorder.develop.model.realm.Note object, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().threadId != realm.threadId) {
            throw new IllegalArgumentException("Objects which belong to Realm instances in other threads cannot be copied into this Realm instance.");
        }
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return object;
        }
        final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get();
        RealmObjectProxy cachedRealmObject = cache.get(object);
        if (cachedRealmObject != null) {
            return (com.knowrecorder.develop.model.realm.Note) cachedRealmObject;
        } else {
            com.knowrecorder.develop.model.realm.Note realmObject = null;
            boolean canUpdate = update;
            if (canUpdate) {
                Table table = realm.getTable(com.knowrecorder.develop.model.realm.Note.class);
                long pkColumnIndex = table.getPrimaryKey();
                long rowIndex = table.findFirstLong(pkColumnIndex, ((NoteRealmProxyInterface) object).realmGet$id());
                if (rowIndex != TableOrView.NO_MATCH) {
                    try {
                        objectContext.set(realm, table.getUncheckedRow(rowIndex), realm.schema.getColumnInfo(com.knowrecorder.develop.model.realm.Note.class), false, Collections.<String> emptyList());
                        realmObject = new io.realm.NoteRealmProxy();
                        cache.put(object, (RealmObjectProxy) realmObject);
                    } finally {
                        objectContext.clear();
                    }
                } else {
                    canUpdate = false;
                }
            }

            if (canUpdate) {
                return update(realm, realmObject, object, cache);
            } else {
                return copy(realm, object, update, cache);
            }
        }
    }

    public static com.knowrecorder.develop.model.realm.Note copy(Realm realm, com.knowrecorder.develop.model.realm.Note newObject, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        RealmObjectProxy cachedRealmObject = cache.get(newObject);
        if (cachedRealmObject != null) {
            return (com.knowrecorder.develop.model.realm.Note) cachedRealmObject;
        } else {
            // rejecting default values to avoid creating unexpected objects from RealmModel/RealmList fields.
            com.knowrecorder.develop.model.realm.Note realmObject = realm.createObjectInternal(com.knowrecorder.develop.model.realm.Note.class, ((NoteRealmProxyInterface) newObject).realmGet$id(), false, Collections.<String>emptyList());
            cache.put(newObject, (RealmObjectProxy) realmObject);
            ((NoteRealmProxyInterface) realmObject).realmSet$createDate(((NoteRealmProxyInterface) newObject).realmGet$createDate());
            ((NoteRealmProxyInterface) realmObject).realmSet$title(((NoteRealmProxyInterface) newObject).realmGet$title());
            ((NoteRealmProxyInterface) realmObject).realmSet$totaltime(((NoteRealmProxyInterface) newObject).realmGet$totaltime());
            ((NoteRealmProxyInterface) realmObject).realmSet$isCurrentUsing(((NoteRealmProxyInterface) newObject).realmGet$isCurrentUsing());
            ((NoteRealmProxyInterface) realmObject).realmSet$info(((NoteRealmProxyInterface) newObject).realmGet$info());
            return realmObject;
        }
    }

    public static long insert(Realm realm, com.knowrecorder.develop.model.realm.Note object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(com.knowrecorder.develop.model.realm.Note.class);
        long tableNativePtr = table.getNativeTablePointer();
        NoteColumnInfo columnInfo = (NoteColumnInfo) realm.schema.getColumnInfo(com.knowrecorder.develop.model.realm.Note.class);
        long pkColumnIndex = table.getPrimaryKey();
        long rowIndex = TableOrView.NO_MATCH;
        Object primaryKeyValue = ((NoteRealmProxyInterface) object).realmGet$id();
        if (primaryKeyValue != null) {
            rowIndex = Table.nativeFindFirstInt(tableNativePtr, pkColumnIndex, ((NoteRealmProxyInterface) object).realmGet$id());
        }
        if (rowIndex == TableOrView.NO_MATCH) {
            rowIndex = table.addEmptyRowWithPrimaryKey(((NoteRealmProxyInterface) object).realmGet$id(), false);
        } else {
            Table.throwDuplicatePrimaryKeyException(primaryKeyValue);
        }
        cache.put(object, rowIndex);
        String realmGet$createDate = ((NoteRealmProxyInterface)object).realmGet$createDate();
        if (realmGet$createDate != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.createDateIndex, rowIndex, realmGet$createDate, false);
        }
        String realmGet$title = ((NoteRealmProxyInterface)object).realmGet$title();
        if (realmGet$title != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.titleIndex, rowIndex, realmGet$title, false);
        }
        Table.nativeSetFloat(tableNativePtr, columnInfo.totaltimeIndex, rowIndex, ((NoteRealmProxyInterface)object).realmGet$totaltime(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.isCurrentUsingIndex, rowIndex, ((NoteRealmProxyInterface)object).realmGet$isCurrentUsing(), false);
        String realmGet$info = ((NoteRealmProxyInterface)object).realmGet$info();
        if (realmGet$info != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.infoIndex, rowIndex, realmGet$info, false);
        }
        return rowIndex;
    }

    public static void insert(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(com.knowrecorder.develop.model.realm.Note.class);
        long tableNativePtr = table.getNativeTablePointer();
        NoteColumnInfo columnInfo = (NoteColumnInfo) realm.schema.getColumnInfo(com.knowrecorder.develop.model.realm.Note.class);
        long pkColumnIndex = table.getPrimaryKey();
        com.knowrecorder.develop.model.realm.Note object = null;
        while (objects.hasNext()) {
            object = (com.knowrecorder.develop.model.realm.Note) objects.next();
            if(!cache.containsKey(object)) {
                if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                    cache.put(object, ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex());
                    continue;
                }
                long rowIndex = TableOrView.NO_MATCH;
                Object primaryKeyValue = ((NoteRealmProxyInterface) object).realmGet$id();
                if (primaryKeyValue != null) {
                    rowIndex = Table.nativeFindFirstInt(tableNativePtr, pkColumnIndex, ((NoteRealmProxyInterface) object).realmGet$id());
                }
                if (rowIndex == TableOrView.NO_MATCH) {
                    rowIndex = table.addEmptyRowWithPrimaryKey(((NoteRealmProxyInterface) object).realmGet$id(), false);
                } else {
                    Table.throwDuplicatePrimaryKeyException(primaryKeyValue);
                }
                cache.put(object, rowIndex);
                String realmGet$createDate = ((NoteRealmProxyInterface)object).realmGet$createDate();
                if (realmGet$createDate != null) {
                    Table.nativeSetString(tableNativePtr, columnInfo.createDateIndex, rowIndex, realmGet$createDate, false);
                }
                String realmGet$title = ((NoteRealmProxyInterface)object).realmGet$title();
                if (realmGet$title != null) {
                    Table.nativeSetString(tableNativePtr, columnInfo.titleIndex, rowIndex, realmGet$title, false);
                }
                Table.nativeSetFloat(tableNativePtr, columnInfo.totaltimeIndex, rowIndex, ((NoteRealmProxyInterface)object).realmGet$totaltime(), false);
                Table.nativeSetBoolean(tableNativePtr, columnInfo.isCurrentUsingIndex, rowIndex, ((NoteRealmProxyInterface)object).realmGet$isCurrentUsing(), false);
                String realmGet$info = ((NoteRealmProxyInterface)object).realmGet$info();
                if (realmGet$info != null) {
                    Table.nativeSetString(tableNativePtr, columnInfo.infoIndex, rowIndex, realmGet$info, false);
                }
            }
        }
    }

    public static long insertOrUpdate(Realm realm, com.knowrecorder.develop.model.realm.Note object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(com.knowrecorder.develop.model.realm.Note.class);
        long tableNativePtr = table.getNativeTablePointer();
        NoteColumnInfo columnInfo = (NoteColumnInfo) realm.schema.getColumnInfo(com.knowrecorder.develop.model.realm.Note.class);
        long pkColumnIndex = table.getPrimaryKey();
        long rowIndex = TableOrView.NO_MATCH;
        Object primaryKeyValue = ((NoteRealmProxyInterface) object).realmGet$id();
        if (primaryKeyValue != null) {
            rowIndex = Table.nativeFindFirstInt(tableNativePtr, pkColumnIndex, ((NoteRealmProxyInterface) object).realmGet$id());
        }
        if (rowIndex == TableOrView.NO_MATCH) {
            rowIndex = table.addEmptyRowWithPrimaryKey(((NoteRealmProxyInterface) object).realmGet$id(), false);
        }
        cache.put(object, rowIndex);
        String realmGet$createDate = ((NoteRealmProxyInterface)object).realmGet$createDate();
        if (realmGet$createDate != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.createDateIndex, rowIndex, realmGet$createDate, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.createDateIndex, rowIndex, false);
        }
        String realmGet$title = ((NoteRealmProxyInterface)object).realmGet$title();
        if (realmGet$title != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.titleIndex, rowIndex, realmGet$title, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.titleIndex, rowIndex, false);
        }
        Table.nativeSetFloat(tableNativePtr, columnInfo.totaltimeIndex, rowIndex, ((NoteRealmProxyInterface)object).realmGet$totaltime(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.isCurrentUsingIndex, rowIndex, ((NoteRealmProxyInterface)object).realmGet$isCurrentUsing(), false);
        String realmGet$info = ((NoteRealmProxyInterface)object).realmGet$info();
        if (realmGet$info != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.infoIndex, rowIndex, realmGet$info, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.infoIndex, rowIndex, false);
        }
        return rowIndex;
    }

    public static void insertOrUpdate(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(com.knowrecorder.develop.model.realm.Note.class);
        long tableNativePtr = table.getNativeTablePointer();
        NoteColumnInfo columnInfo = (NoteColumnInfo) realm.schema.getColumnInfo(com.knowrecorder.develop.model.realm.Note.class);
        long pkColumnIndex = table.getPrimaryKey();
        com.knowrecorder.develop.model.realm.Note object = null;
        while (objects.hasNext()) {
            object = (com.knowrecorder.develop.model.realm.Note) objects.next();
            if(!cache.containsKey(object)) {
                if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                    cache.put(object, ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex());
                    continue;
                }
                long rowIndex = TableOrView.NO_MATCH;
                Object primaryKeyValue = ((NoteRealmProxyInterface) object).realmGet$id();
                if (primaryKeyValue != null) {
                    rowIndex = Table.nativeFindFirstInt(tableNativePtr, pkColumnIndex, ((NoteRealmProxyInterface) object).realmGet$id());
                }
                if (rowIndex == TableOrView.NO_MATCH) {
                    rowIndex = table.addEmptyRowWithPrimaryKey(((NoteRealmProxyInterface) object).realmGet$id(), false);
                }
                cache.put(object, rowIndex);
                String realmGet$createDate = ((NoteRealmProxyInterface)object).realmGet$createDate();
                if (realmGet$createDate != null) {
                    Table.nativeSetString(tableNativePtr, columnInfo.createDateIndex, rowIndex, realmGet$createDate, false);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.createDateIndex, rowIndex, false);
                }
                String realmGet$title = ((NoteRealmProxyInterface)object).realmGet$title();
                if (realmGet$title != null) {
                    Table.nativeSetString(tableNativePtr, columnInfo.titleIndex, rowIndex, realmGet$title, false);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.titleIndex, rowIndex, false);
                }
                Table.nativeSetFloat(tableNativePtr, columnInfo.totaltimeIndex, rowIndex, ((NoteRealmProxyInterface)object).realmGet$totaltime(), false);
                Table.nativeSetBoolean(tableNativePtr, columnInfo.isCurrentUsingIndex, rowIndex, ((NoteRealmProxyInterface)object).realmGet$isCurrentUsing(), false);
                String realmGet$info = ((NoteRealmProxyInterface)object).realmGet$info();
                if (realmGet$info != null) {
                    Table.nativeSetString(tableNativePtr, columnInfo.infoIndex, rowIndex, realmGet$info, false);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.infoIndex, rowIndex, false);
                }
            }
        }
    }

    public static com.knowrecorder.develop.model.realm.Note createDetachedCopy(com.knowrecorder.develop.model.realm.Note realmObject, int currentDepth, int maxDepth, Map<RealmModel, CacheData<RealmModel>> cache) {
        if (currentDepth > maxDepth || realmObject == null) {
            return null;
        }
        CacheData<RealmModel> cachedObject = cache.get(realmObject);
        com.knowrecorder.develop.model.realm.Note unmanagedObject;
        if (cachedObject != null) {
            // Reuse cached object or recreate it because it was encountered at a lower depth.
            if (currentDepth >= cachedObject.minDepth) {
                return (com.knowrecorder.develop.model.realm.Note)cachedObject.object;
            } else {
                unmanagedObject = (com.knowrecorder.develop.model.realm.Note)cachedObject.object;
                cachedObject.minDepth = currentDepth;
            }
        } else {
            unmanagedObject = new com.knowrecorder.develop.model.realm.Note();
            cache.put(realmObject, new RealmObjectProxy.CacheData<RealmModel>(currentDepth, unmanagedObject));
        }
        ((NoteRealmProxyInterface) unmanagedObject).realmSet$id(((NoteRealmProxyInterface) realmObject).realmGet$id());
        ((NoteRealmProxyInterface) unmanagedObject).realmSet$createDate(((NoteRealmProxyInterface) realmObject).realmGet$createDate());
        ((NoteRealmProxyInterface) unmanagedObject).realmSet$title(((NoteRealmProxyInterface) realmObject).realmGet$title());
        ((NoteRealmProxyInterface) unmanagedObject).realmSet$totaltime(((NoteRealmProxyInterface) realmObject).realmGet$totaltime());
        ((NoteRealmProxyInterface) unmanagedObject).realmSet$isCurrentUsing(((NoteRealmProxyInterface) realmObject).realmGet$isCurrentUsing());
        ((NoteRealmProxyInterface) unmanagedObject).realmSet$info(((NoteRealmProxyInterface) realmObject).realmGet$info());
        return unmanagedObject;
    }

    static com.knowrecorder.develop.model.realm.Note update(Realm realm, com.knowrecorder.develop.model.realm.Note realmObject, com.knowrecorder.develop.model.realm.Note newObject, Map<RealmModel, RealmObjectProxy> cache) {
        ((NoteRealmProxyInterface) realmObject).realmSet$createDate(((NoteRealmProxyInterface) newObject).realmGet$createDate());
        ((NoteRealmProxyInterface) realmObject).realmSet$title(((NoteRealmProxyInterface) newObject).realmGet$title());
        ((NoteRealmProxyInterface) realmObject).realmSet$totaltime(((NoteRealmProxyInterface) newObject).realmGet$totaltime());
        ((NoteRealmProxyInterface) realmObject).realmSet$isCurrentUsing(((NoteRealmProxyInterface) newObject).realmGet$isCurrentUsing());
        ((NoteRealmProxyInterface) realmObject).realmSet$info(((NoteRealmProxyInterface) newObject).realmGet$info());
        return realmObject;
    }

    @Override
    public String toString() {
        if (!RealmObject.isValid(this)) {
            return "Invalid object";
        }
        StringBuilder stringBuilder = new StringBuilder("Note = [");
        stringBuilder.append("{id:");
        stringBuilder.append(realmGet$id());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{createDate:");
        stringBuilder.append(realmGet$createDate() != null ? realmGet$createDate() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{title:");
        stringBuilder.append(realmGet$title() != null ? realmGet$title() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{totaltime:");
        stringBuilder.append(realmGet$totaltime());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{isCurrentUsing:");
        stringBuilder.append(realmGet$isCurrentUsing());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{info:");
        stringBuilder.append(realmGet$info() != null ? realmGet$info() : "null");
        stringBuilder.append("}");
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    @Override
    public ProxyState realmGet$proxyState() {
        return proxyState;
    }

    @Override
    public int hashCode() {
        String realmName = proxyState.getRealm$realm().getPath();
        String tableName = proxyState.getRow$realm().getTable().getName();
        long rowIndex = proxyState.getRow$realm().getIndex();

        int result = 17;
        result = 31 * result + ((realmName != null) ? realmName.hashCode() : 0);
        result = 31 * result + ((tableName != null) ? tableName.hashCode() : 0);
        result = 31 * result + (int) (rowIndex ^ (rowIndex >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoteRealmProxy aNote = (NoteRealmProxy)o;

        String path = proxyState.getRealm$realm().getPath();
        String otherPath = aNote.proxyState.getRealm$realm().getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) return false;

        String tableName = proxyState.getRow$realm().getTable().getName();
        String otherTableName = aNote.proxyState.getRow$realm().getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false;

        if (proxyState.getRow$realm().getIndex() != aNote.proxyState.getRow$realm().getIndex()) return false;

        return true;
    }

}

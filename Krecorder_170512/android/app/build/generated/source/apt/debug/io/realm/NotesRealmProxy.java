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

public class NotesRealmProxy extends com.knowrecorder.develop.model.realm.Notes
    implements RealmObjectProxy, NotesRealmProxyInterface {

    static final class NotesColumnInfo extends ColumnInfo
        implements Cloneable {

        public long noteNameIndex;
        public long titleIndex;
        public long createDateIndex;
        public long totaltimeIndex;

        NotesColumnInfo(String path, Table table) {
            final Map<String, Long> indicesMap = new HashMap<String, Long>(4);
            this.noteNameIndex = getValidColumnIndex(path, table, "Notes", "noteName");
            indicesMap.put("noteName", this.noteNameIndex);
            this.titleIndex = getValidColumnIndex(path, table, "Notes", "title");
            indicesMap.put("title", this.titleIndex);
            this.createDateIndex = getValidColumnIndex(path, table, "Notes", "createDate");
            indicesMap.put("createDate", this.createDateIndex);
            this.totaltimeIndex = getValidColumnIndex(path, table, "Notes", "totaltime");
            indicesMap.put("totaltime", this.totaltimeIndex);

            setIndicesMap(indicesMap);
        }

        @Override
        public final void copyColumnInfoFrom(ColumnInfo other) {
            final NotesColumnInfo otherInfo = (NotesColumnInfo) other;
            this.noteNameIndex = otherInfo.noteNameIndex;
            this.titleIndex = otherInfo.titleIndex;
            this.createDateIndex = otherInfo.createDateIndex;
            this.totaltimeIndex = otherInfo.totaltimeIndex;

            setIndicesMap(otherInfo.getIndicesMap());
        }

        @Override
        public final NotesColumnInfo clone() {
            return (NotesColumnInfo) super.clone();
        }

    }
    private NotesColumnInfo columnInfo;
    private ProxyState<com.knowrecorder.develop.model.realm.Notes> proxyState;
    private static final List<String> FIELD_NAMES;
    static {
        List<String> fieldNames = new ArrayList<String>();
        fieldNames.add("noteName");
        fieldNames.add("title");
        fieldNames.add("createDate");
        fieldNames.add("totaltime");
        FIELD_NAMES = Collections.unmodifiableList(fieldNames);
    }

    NotesRealmProxy() {
        if (proxyState == null) {
            injectObjectContext();
        }
        proxyState.setConstructionFinished();
    }

    private void injectObjectContext() {
        final BaseRealm.RealmObjectContext context = BaseRealm.objectContext.get();
        this.columnInfo = (NotesColumnInfo) context.getColumnInfo();
        this.proxyState = new ProxyState<com.knowrecorder.develop.model.realm.Notes>(com.knowrecorder.develop.model.realm.Notes.class, this);
        proxyState.setRealm$realm(context.getRealm());
        proxyState.setRow$realm(context.getRow());
        proxyState.setAcceptDefaultValue$realm(context.getAcceptDefaultValue());
        proxyState.setExcludeFields$realm(context.getExcludeFields());
    }

    @SuppressWarnings("cast")
    public String realmGet$noteName() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.noteNameIndex);
    }

    public void realmSet$noteName(String value) {
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
                row.getTable().setNull(columnInfo.noteNameIndex, row.getIndex(), true);
                return;
            }
            row.getTable().setString(columnInfo.noteNameIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.noteNameIndex);
            return;
        }
        proxyState.getRow$realm().setString(columnInfo.noteNameIndex, value);
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

    public static RealmObjectSchema createRealmObjectSchema(RealmSchema realmSchema) {
        if (!realmSchema.contains("Notes")) {
            RealmObjectSchema realmObjectSchema = realmSchema.create("Notes");
            realmObjectSchema.add(new Property("noteName", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED));
            realmObjectSchema.add(new Property("title", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED));
            realmObjectSchema.add(new Property("createDate", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED));
            realmObjectSchema.add(new Property("totaltime", RealmFieldType.FLOAT, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            return realmObjectSchema;
        }
        return realmSchema.get("Notes");
    }

    public static Table initTable(SharedRealm sharedRealm) {
        if (!sharedRealm.hasTable("class_Notes")) {
            Table table = sharedRealm.getTable("class_Notes");
            table.addColumn(RealmFieldType.STRING, "noteName", Table.NULLABLE);
            table.addColumn(RealmFieldType.STRING, "title", Table.NULLABLE);
            table.addColumn(RealmFieldType.STRING, "createDate", Table.NULLABLE);
            table.addColumn(RealmFieldType.FLOAT, "totaltime", Table.NOT_NULLABLE);
            table.setPrimaryKey("");
            return table;
        }
        return sharedRealm.getTable("class_Notes");
    }

    public static NotesColumnInfo validateTable(SharedRealm sharedRealm, boolean allowExtraColumns) {
        if (sharedRealm.hasTable("class_Notes")) {
            Table table = sharedRealm.getTable("class_Notes");
            final long columnCount = table.getColumnCount();
            if (columnCount != 4) {
                if (columnCount < 4) {
                    throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field count is less than expected - expected 4 but was " + columnCount);
                }
                if (allowExtraColumns) {
                    RealmLog.debug("Field count is more than expected - expected 4 but was %1$d", columnCount);
                } else {
                    throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field count is more than expected - expected 4 but was " + columnCount);
                }
            }
            Map<String, RealmFieldType> columnTypes = new HashMap<String, RealmFieldType>();
            for (long i = 0; i < columnCount; i++) {
                columnTypes.put(table.getColumnName(i), table.getColumnType(i));
            }

            final NotesColumnInfo columnInfo = new NotesColumnInfo(sharedRealm.getPath(), table);

            if (table.hasPrimaryKey()) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Primary Key defined for field " + table.getColumnName(table.getPrimaryKey()) + " was removed.");
            }

            if (!columnTypes.containsKey("noteName")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'noteName' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("noteName") != RealmFieldType.STRING) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'String' for field 'noteName' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.noteNameIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'noteName' is required. Either set @Required to field 'noteName' or migrate using RealmObjectSchema.setNullable().");
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
            if (!columnTypes.containsKey("createDate")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'createDate' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("createDate") != RealmFieldType.STRING) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'String' for field 'createDate' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.createDateIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'createDate' is required. Either set @Required to field 'createDate' or migrate using RealmObjectSchema.setNullable().");
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
            return columnInfo;
        } else {
            throw new RealmMigrationNeededException(sharedRealm.getPath(), "The 'Notes' class is missing from the schema for this Realm.");
        }
    }

    public static String getTableName() {
        return "class_Notes";
    }

    public static List<String> getFieldNames() {
        return FIELD_NAMES;
    }

    @SuppressWarnings("cast")
    public static com.knowrecorder.develop.model.realm.Notes createOrUpdateUsingJsonObject(Realm realm, JSONObject json, boolean update)
        throws JSONException {
        final List<String> excludeFields = Collections.<String> emptyList();
        com.knowrecorder.develop.model.realm.Notes obj = realm.createObjectInternal(com.knowrecorder.develop.model.realm.Notes.class, true, excludeFields);
        if (json.has("noteName")) {
            if (json.isNull("noteName")) {
                ((NotesRealmProxyInterface) obj).realmSet$noteName(null);
            } else {
                ((NotesRealmProxyInterface) obj).realmSet$noteName((String) json.getString("noteName"));
            }
        }
        if (json.has("title")) {
            if (json.isNull("title")) {
                ((NotesRealmProxyInterface) obj).realmSet$title(null);
            } else {
                ((NotesRealmProxyInterface) obj).realmSet$title((String) json.getString("title"));
            }
        }
        if (json.has("createDate")) {
            if (json.isNull("createDate")) {
                ((NotesRealmProxyInterface) obj).realmSet$createDate(null);
            } else {
                ((NotesRealmProxyInterface) obj).realmSet$createDate((String) json.getString("createDate"));
            }
        }
        if (json.has("totaltime")) {
            if (json.isNull("totaltime")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'totaltime' to null.");
            } else {
                ((NotesRealmProxyInterface) obj).realmSet$totaltime((float) json.getDouble("totaltime"));
            }
        }
        return obj;
    }

    @SuppressWarnings("cast")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static com.knowrecorder.develop.model.realm.Notes createUsingJsonStream(Realm realm, JsonReader reader)
        throws IOException {
        com.knowrecorder.develop.model.realm.Notes obj = new com.knowrecorder.develop.model.realm.Notes();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("noteName")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((NotesRealmProxyInterface) obj).realmSet$noteName(null);
                } else {
                    ((NotesRealmProxyInterface) obj).realmSet$noteName((String) reader.nextString());
                }
            } else if (name.equals("title")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((NotesRealmProxyInterface) obj).realmSet$title(null);
                } else {
                    ((NotesRealmProxyInterface) obj).realmSet$title((String) reader.nextString());
                }
            } else if (name.equals("createDate")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((NotesRealmProxyInterface) obj).realmSet$createDate(null);
                } else {
                    ((NotesRealmProxyInterface) obj).realmSet$createDate((String) reader.nextString());
                }
            } else if (name.equals("totaltime")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'totaltime' to null.");
                } else {
                    ((NotesRealmProxyInterface) obj).realmSet$totaltime((float) reader.nextDouble());
                }
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        obj = realm.copyToRealm(obj);
        return obj;
    }

    public static com.knowrecorder.develop.model.realm.Notes copyOrUpdate(Realm realm, com.knowrecorder.develop.model.realm.Notes object, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().threadId != realm.threadId) {
            throw new IllegalArgumentException("Objects which belong to Realm instances in other threads cannot be copied into this Realm instance.");
        }
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return object;
        }
        final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get();
        RealmObjectProxy cachedRealmObject = cache.get(object);
        if (cachedRealmObject != null) {
            return (com.knowrecorder.develop.model.realm.Notes) cachedRealmObject;
        } else {
            return copy(realm, object, update, cache);
        }
    }

    public static com.knowrecorder.develop.model.realm.Notes copy(Realm realm, com.knowrecorder.develop.model.realm.Notes newObject, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        RealmObjectProxy cachedRealmObject = cache.get(newObject);
        if (cachedRealmObject != null) {
            return (com.knowrecorder.develop.model.realm.Notes) cachedRealmObject;
        } else {
            // rejecting default values to avoid creating unexpected objects from RealmModel/RealmList fields.
            com.knowrecorder.develop.model.realm.Notes realmObject = realm.createObjectInternal(com.knowrecorder.develop.model.realm.Notes.class, false, Collections.<String>emptyList());
            cache.put(newObject, (RealmObjectProxy) realmObject);
            ((NotesRealmProxyInterface) realmObject).realmSet$noteName(((NotesRealmProxyInterface) newObject).realmGet$noteName());
            ((NotesRealmProxyInterface) realmObject).realmSet$title(((NotesRealmProxyInterface) newObject).realmGet$title());
            ((NotesRealmProxyInterface) realmObject).realmSet$createDate(((NotesRealmProxyInterface) newObject).realmGet$createDate());
            ((NotesRealmProxyInterface) realmObject).realmSet$totaltime(((NotesRealmProxyInterface) newObject).realmGet$totaltime());
            return realmObject;
        }
    }

    public static long insert(Realm realm, com.knowrecorder.develop.model.realm.Notes object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(com.knowrecorder.develop.model.realm.Notes.class);
        long tableNativePtr = table.getNativeTablePointer();
        NotesColumnInfo columnInfo = (NotesColumnInfo) realm.schema.getColumnInfo(com.knowrecorder.develop.model.realm.Notes.class);
        long rowIndex = Table.nativeAddEmptyRow(tableNativePtr, 1);
        cache.put(object, rowIndex);
        String realmGet$noteName = ((NotesRealmProxyInterface)object).realmGet$noteName();
        if (realmGet$noteName != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.noteNameIndex, rowIndex, realmGet$noteName, false);
        }
        String realmGet$title = ((NotesRealmProxyInterface)object).realmGet$title();
        if (realmGet$title != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.titleIndex, rowIndex, realmGet$title, false);
        }
        String realmGet$createDate = ((NotesRealmProxyInterface)object).realmGet$createDate();
        if (realmGet$createDate != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.createDateIndex, rowIndex, realmGet$createDate, false);
        }
        Table.nativeSetFloat(tableNativePtr, columnInfo.totaltimeIndex, rowIndex, ((NotesRealmProxyInterface)object).realmGet$totaltime(), false);
        return rowIndex;
    }

    public static void insert(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(com.knowrecorder.develop.model.realm.Notes.class);
        long tableNativePtr = table.getNativeTablePointer();
        NotesColumnInfo columnInfo = (NotesColumnInfo) realm.schema.getColumnInfo(com.knowrecorder.develop.model.realm.Notes.class);
        com.knowrecorder.develop.model.realm.Notes object = null;
        while (objects.hasNext()) {
            object = (com.knowrecorder.develop.model.realm.Notes) objects.next();
            if(!cache.containsKey(object)) {
                if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                    cache.put(object, ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex());
                    continue;
                }
                long rowIndex = Table.nativeAddEmptyRow(tableNativePtr, 1);
                cache.put(object, rowIndex);
                String realmGet$noteName = ((NotesRealmProxyInterface)object).realmGet$noteName();
                if (realmGet$noteName != null) {
                    Table.nativeSetString(tableNativePtr, columnInfo.noteNameIndex, rowIndex, realmGet$noteName, false);
                }
                String realmGet$title = ((NotesRealmProxyInterface)object).realmGet$title();
                if (realmGet$title != null) {
                    Table.nativeSetString(tableNativePtr, columnInfo.titleIndex, rowIndex, realmGet$title, false);
                }
                String realmGet$createDate = ((NotesRealmProxyInterface)object).realmGet$createDate();
                if (realmGet$createDate != null) {
                    Table.nativeSetString(tableNativePtr, columnInfo.createDateIndex, rowIndex, realmGet$createDate, false);
                }
                Table.nativeSetFloat(tableNativePtr, columnInfo.totaltimeIndex, rowIndex, ((NotesRealmProxyInterface)object).realmGet$totaltime(), false);
            }
        }
    }

    public static long insertOrUpdate(Realm realm, com.knowrecorder.develop.model.realm.Notes object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(com.knowrecorder.develop.model.realm.Notes.class);
        long tableNativePtr = table.getNativeTablePointer();
        NotesColumnInfo columnInfo = (NotesColumnInfo) realm.schema.getColumnInfo(com.knowrecorder.develop.model.realm.Notes.class);
        long rowIndex = Table.nativeAddEmptyRow(tableNativePtr, 1);
        cache.put(object, rowIndex);
        String realmGet$noteName = ((NotesRealmProxyInterface)object).realmGet$noteName();
        if (realmGet$noteName != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.noteNameIndex, rowIndex, realmGet$noteName, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.noteNameIndex, rowIndex, false);
        }
        String realmGet$title = ((NotesRealmProxyInterface)object).realmGet$title();
        if (realmGet$title != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.titleIndex, rowIndex, realmGet$title, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.titleIndex, rowIndex, false);
        }
        String realmGet$createDate = ((NotesRealmProxyInterface)object).realmGet$createDate();
        if (realmGet$createDate != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.createDateIndex, rowIndex, realmGet$createDate, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.createDateIndex, rowIndex, false);
        }
        Table.nativeSetFloat(tableNativePtr, columnInfo.totaltimeIndex, rowIndex, ((NotesRealmProxyInterface)object).realmGet$totaltime(), false);
        return rowIndex;
    }

    public static void insertOrUpdate(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(com.knowrecorder.develop.model.realm.Notes.class);
        long tableNativePtr = table.getNativeTablePointer();
        NotesColumnInfo columnInfo = (NotesColumnInfo) realm.schema.getColumnInfo(com.knowrecorder.develop.model.realm.Notes.class);
        com.knowrecorder.develop.model.realm.Notes object = null;
        while (objects.hasNext()) {
            object = (com.knowrecorder.develop.model.realm.Notes) objects.next();
            if(!cache.containsKey(object)) {
                if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                    cache.put(object, ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex());
                    continue;
                }
                long rowIndex = Table.nativeAddEmptyRow(tableNativePtr, 1);
                cache.put(object, rowIndex);
                String realmGet$noteName = ((NotesRealmProxyInterface)object).realmGet$noteName();
                if (realmGet$noteName != null) {
                    Table.nativeSetString(tableNativePtr, columnInfo.noteNameIndex, rowIndex, realmGet$noteName, false);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.noteNameIndex, rowIndex, false);
                }
                String realmGet$title = ((NotesRealmProxyInterface)object).realmGet$title();
                if (realmGet$title != null) {
                    Table.nativeSetString(tableNativePtr, columnInfo.titleIndex, rowIndex, realmGet$title, false);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.titleIndex, rowIndex, false);
                }
                String realmGet$createDate = ((NotesRealmProxyInterface)object).realmGet$createDate();
                if (realmGet$createDate != null) {
                    Table.nativeSetString(tableNativePtr, columnInfo.createDateIndex, rowIndex, realmGet$createDate, false);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.createDateIndex, rowIndex, false);
                }
                Table.nativeSetFloat(tableNativePtr, columnInfo.totaltimeIndex, rowIndex, ((NotesRealmProxyInterface)object).realmGet$totaltime(), false);
            }
        }
    }

    public static com.knowrecorder.develop.model.realm.Notes createDetachedCopy(com.knowrecorder.develop.model.realm.Notes realmObject, int currentDepth, int maxDepth, Map<RealmModel, CacheData<RealmModel>> cache) {
        if (currentDepth > maxDepth || realmObject == null) {
            return null;
        }
        CacheData<RealmModel> cachedObject = cache.get(realmObject);
        com.knowrecorder.develop.model.realm.Notes unmanagedObject;
        if (cachedObject != null) {
            // Reuse cached object or recreate it because it was encountered at a lower depth.
            if (currentDepth >= cachedObject.minDepth) {
                return (com.knowrecorder.develop.model.realm.Notes)cachedObject.object;
            } else {
                unmanagedObject = (com.knowrecorder.develop.model.realm.Notes)cachedObject.object;
                cachedObject.minDepth = currentDepth;
            }
        } else {
            unmanagedObject = new com.knowrecorder.develop.model.realm.Notes();
            cache.put(realmObject, new RealmObjectProxy.CacheData<RealmModel>(currentDepth, unmanagedObject));
        }
        ((NotesRealmProxyInterface) unmanagedObject).realmSet$noteName(((NotesRealmProxyInterface) realmObject).realmGet$noteName());
        ((NotesRealmProxyInterface) unmanagedObject).realmSet$title(((NotesRealmProxyInterface) realmObject).realmGet$title());
        ((NotesRealmProxyInterface) unmanagedObject).realmSet$createDate(((NotesRealmProxyInterface) realmObject).realmGet$createDate());
        ((NotesRealmProxyInterface) unmanagedObject).realmSet$totaltime(((NotesRealmProxyInterface) realmObject).realmGet$totaltime());
        return unmanagedObject;
    }

    @Override
    public String toString() {
        if (!RealmObject.isValid(this)) {
            return "Invalid object";
        }
        StringBuilder stringBuilder = new StringBuilder("Notes = [");
        stringBuilder.append("{noteName:");
        stringBuilder.append(realmGet$noteName() != null ? realmGet$noteName() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{title:");
        stringBuilder.append(realmGet$title() != null ? realmGet$title() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{createDate:");
        stringBuilder.append(realmGet$createDate() != null ? realmGet$createDate() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{totaltime:");
        stringBuilder.append(realmGet$totaltime());
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
        NotesRealmProxy aNotes = (NotesRealmProxy)o;

        String path = proxyState.getRealm$realm().getPath();
        String otherPath = aNotes.proxyState.getRealm$realm().getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) return false;

        String tableName = proxyState.getRow$realm().getTable().getName();
        String otherTableName = aNotes.proxyState.getRow$realm().getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false;

        if (proxyState.getRow$realm().getIndex() != aNotes.proxyState.getRow$realm().getIndex()) return false;

        return true;
    }

}

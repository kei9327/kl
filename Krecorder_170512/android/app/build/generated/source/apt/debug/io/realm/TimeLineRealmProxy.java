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

public class TimeLineRealmProxy extends com.knowrecorder.develop.model.realm.TimeLine
    implements RealmObjectProxy, TimeLineRealmProxyInterface {

    static final class TimeLineColumnInfo extends ColumnInfo
        implements Cloneable {

        public long midIndex;
        public long startRunIndex;
        public long endRunIndex;
        public long typeIndex;
        public long remarksIndex;

        TimeLineColumnInfo(String path, Table table) {
            final Map<String, Long> indicesMap = new HashMap<String, Long>(5);
            this.midIndex = getValidColumnIndex(path, table, "TimeLine", "mid");
            indicesMap.put("mid", this.midIndex);
            this.startRunIndex = getValidColumnIndex(path, table, "TimeLine", "startRun");
            indicesMap.put("startRun", this.startRunIndex);
            this.endRunIndex = getValidColumnIndex(path, table, "TimeLine", "endRun");
            indicesMap.put("endRun", this.endRunIndex);
            this.typeIndex = getValidColumnIndex(path, table, "TimeLine", "type");
            indicesMap.put("type", this.typeIndex);
            this.remarksIndex = getValidColumnIndex(path, table, "TimeLine", "remarks");
            indicesMap.put("remarks", this.remarksIndex);

            setIndicesMap(indicesMap);
        }

        @Override
        public final void copyColumnInfoFrom(ColumnInfo other) {
            final TimeLineColumnInfo otherInfo = (TimeLineColumnInfo) other;
            this.midIndex = otherInfo.midIndex;
            this.startRunIndex = otherInfo.startRunIndex;
            this.endRunIndex = otherInfo.endRunIndex;
            this.typeIndex = otherInfo.typeIndex;
            this.remarksIndex = otherInfo.remarksIndex;

            setIndicesMap(otherInfo.getIndicesMap());
        }

        @Override
        public final TimeLineColumnInfo clone() {
            return (TimeLineColumnInfo) super.clone();
        }

    }
    private TimeLineColumnInfo columnInfo;
    private ProxyState<com.knowrecorder.develop.model.realm.TimeLine> proxyState;
    private static final List<String> FIELD_NAMES;
    static {
        List<String> fieldNames = new ArrayList<String>();
        fieldNames.add("mid");
        fieldNames.add("startRun");
        fieldNames.add("endRun");
        fieldNames.add("type");
        fieldNames.add("remarks");
        FIELD_NAMES = Collections.unmodifiableList(fieldNames);
    }

    TimeLineRealmProxy() {
        if (proxyState == null) {
            injectObjectContext();
        }
        proxyState.setConstructionFinished();
    }

    private void injectObjectContext() {
        final BaseRealm.RealmObjectContext context = BaseRealm.objectContext.get();
        this.columnInfo = (TimeLineColumnInfo) context.getColumnInfo();
        this.proxyState = new ProxyState<com.knowrecorder.develop.model.realm.TimeLine>(com.knowrecorder.develop.model.realm.TimeLine.class, this);
        proxyState.setRealm$realm(context.getRealm());
        proxyState.setRow$realm(context.getRow());
        proxyState.setAcceptDefaultValue$realm(context.getAcceptDefaultValue());
        proxyState.setExcludeFields$realm(context.getExcludeFields());
    }

    @SuppressWarnings("cast")
    public long realmGet$mid() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (long) proxyState.getRow$realm().getLong(columnInfo.midIndex);
    }

    public void realmSet$mid(long value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setLong(columnInfo.midIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setLong(columnInfo.midIndex, value);
    }

    @SuppressWarnings("cast")
    public float realmGet$startRun() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (float) proxyState.getRow$realm().getFloat(columnInfo.startRunIndex);
    }

    public void realmSet$startRun(float value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setFloat(columnInfo.startRunIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setFloat(columnInfo.startRunIndex, value);
    }

    @SuppressWarnings("cast")
    public float realmGet$endRun() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (float) proxyState.getRow$realm().getFloat(columnInfo.endRunIndex);
    }

    public void realmSet$endRun(float value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setFloat(columnInfo.endRunIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setFloat(columnInfo.endRunIndex, value);
    }

    @SuppressWarnings("cast")
    public String realmGet$type() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.typeIndex);
    }

    public void realmSet$type(String value) {
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
                row.getTable().setNull(columnInfo.typeIndex, row.getIndex(), true);
                return;
            }
            row.getTable().setString(columnInfo.typeIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.typeIndex);
            return;
        }
        proxyState.getRow$realm().setString(columnInfo.typeIndex, value);
    }

    @SuppressWarnings("cast")
    public String realmGet$remarks() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.remarksIndex);
    }

    public void realmSet$remarks(String value) {
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
                row.getTable().setNull(columnInfo.remarksIndex, row.getIndex(), true);
                return;
            }
            row.getTable().setString(columnInfo.remarksIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.remarksIndex);
            return;
        }
        proxyState.getRow$realm().setString(columnInfo.remarksIndex, value);
    }

    public static RealmObjectSchema createRealmObjectSchema(RealmSchema realmSchema) {
        if (!realmSchema.contains("TimeLine")) {
            RealmObjectSchema realmObjectSchema = realmSchema.create("TimeLine");
            realmObjectSchema.add(new Property("mid", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("startRun", RealmFieldType.FLOAT, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("endRun", RealmFieldType.FLOAT, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("type", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED));
            realmObjectSchema.add(new Property("remarks", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED));
            return realmObjectSchema;
        }
        return realmSchema.get("TimeLine");
    }

    public static Table initTable(SharedRealm sharedRealm) {
        if (!sharedRealm.hasTable("class_TimeLine")) {
            Table table = sharedRealm.getTable("class_TimeLine");
            table.addColumn(RealmFieldType.INTEGER, "mid", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.FLOAT, "startRun", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.FLOAT, "endRun", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.STRING, "type", Table.NULLABLE);
            table.addColumn(RealmFieldType.STRING, "remarks", Table.NULLABLE);
            table.setPrimaryKey("");
            return table;
        }
        return sharedRealm.getTable("class_TimeLine");
    }

    public static TimeLineColumnInfo validateTable(SharedRealm sharedRealm, boolean allowExtraColumns) {
        if (sharedRealm.hasTable("class_TimeLine")) {
            Table table = sharedRealm.getTable("class_TimeLine");
            final long columnCount = table.getColumnCount();
            if (columnCount != 5) {
                if (columnCount < 5) {
                    throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field count is less than expected - expected 5 but was " + columnCount);
                }
                if (allowExtraColumns) {
                    RealmLog.debug("Field count is more than expected - expected 5 but was %1$d", columnCount);
                } else {
                    throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field count is more than expected - expected 5 but was " + columnCount);
                }
            }
            Map<String, RealmFieldType> columnTypes = new HashMap<String, RealmFieldType>();
            for (long i = 0; i < columnCount; i++) {
                columnTypes.put(table.getColumnName(i), table.getColumnType(i));
            }

            final TimeLineColumnInfo columnInfo = new TimeLineColumnInfo(sharedRealm.getPath(), table);

            if (table.hasPrimaryKey()) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Primary Key defined for field " + table.getColumnName(table.getPrimaryKey()) + " was removed.");
            }

            if (!columnTypes.containsKey("mid")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'mid' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("mid") != RealmFieldType.INTEGER) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'long' for field 'mid' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.midIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'mid' does support null values in the existing Realm file. Use corresponding boxed type for field 'mid' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("startRun")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'startRun' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("startRun") != RealmFieldType.FLOAT) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'float' for field 'startRun' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.startRunIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'startRun' does support null values in the existing Realm file. Use corresponding boxed type for field 'startRun' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("endRun")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'endRun' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("endRun") != RealmFieldType.FLOAT) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'float' for field 'endRun' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.endRunIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'endRun' does support null values in the existing Realm file. Use corresponding boxed type for field 'endRun' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("type")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'type' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("type") != RealmFieldType.STRING) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'String' for field 'type' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.typeIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'type' is required. Either set @Required to field 'type' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("remarks")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'remarks' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("remarks") != RealmFieldType.STRING) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'String' for field 'remarks' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.remarksIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'remarks' is required. Either set @Required to field 'remarks' or migrate using RealmObjectSchema.setNullable().");
            }
            return columnInfo;
        } else {
            throw new RealmMigrationNeededException(sharedRealm.getPath(), "The 'TimeLine' class is missing from the schema for this Realm.");
        }
    }

    public static String getTableName() {
        return "class_TimeLine";
    }

    public static List<String> getFieldNames() {
        return FIELD_NAMES;
    }

    @SuppressWarnings("cast")
    public static com.knowrecorder.develop.model.realm.TimeLine createOrUpdateUsingJsonObject(Realm realm, JSONObject json, boolean update)
        throws JSONException {
        final List<String> excludeFields = Collections.<String> emptyList();
        com.knowrecorder.develop.model.realm.TimeLine obj = realm.createObjectInternal(com.knowrecorder.develop.model.realm.TimeLine.class, true, excludeFields);
        if (json.has("mid")) {
            if (json.isNull("mid")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'mid' to null.");
            } else {
                ((TimeLineRealmProxyInterface) obj).realmSet$mid((long) json.getLong("mid"));
            }
        }
        if (json.has("startRun")) {
            if (json.isNull("startRun")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'startRun' to null.");
            } else {
                ((TimeLineRealmProxyInterface) obj).realmSet$startRun((float) json.getDouble("startRun"));
            }
        }
        if (json.has("endRun")) {
            if (json.isNull("endRun")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'endRun' to null.");
            } else {
                ((TimeLineRealmProxyInterface) obj).realmSet$endRun((float) json.getDouble("endRun"));
            }
        }
        if (json.has("type")) {
            if (json.isNull("type")) {
                ((TimeLineRealmProxyInterface) obj).realmSet$type(null);
            } else {
                ((TimeLineRealmProxyInterface) obj).realmSet$type((String) json.getString("type"));
            }
        }
        if (json.has("remarks")) {
            if (json.isNull("remarks")) {
                ((TimeLineRealmProxyInterface) obj).realmSet$remarks(null);
            } else {
                ((TimeLineRealmProxyInterface) obj).realmSet$remarks((String) json.getString("remarks"));
            }
        }
        return obj;
    }

    @SuppressWarnings("cast")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static com.knowrecorder.develop.model.realm.TimeLine createUsingJsonStream(Realm realm, JsonReader reader)
        throws IOException {
        com.knowrecorder.develop.model.realm.TimeLine obj = new com.knowrecorder.develop.model.realm.TimeLine();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("mid")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'mid' to null.");
                } else {
                    ((TimeLineRealmProxyInterface) obj).realmSet$mid((long) reader.nextLong());
                }
            } else if (name.equals("startRun")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'startRun' to null.");
                } else {
                    ((TimeLineRealmProxyInterface) obj).realmSet$startRun((float) reader.nextDouble());
                }
            } else if (name.equals("endRun")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'endRun' to null.");
                } else {
                    ((TimeLineRealmProxyInterface) obj).realmSet$endRun((float) reader.nextDouble());
                }
            } else if (name.equals("type")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((TimeLineRealmProxyInterface) obj).realmSet$type(null);
                } else {
                    ((TimeLineRealmProxyInterface) obj).realmSet$type((String) reader.nextString());
                }
            } else if (name.equals("remarks")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((TimeLineRealmProxyInterface) obj).realmSet$remarks(null);
                } else {
                    ((TimeLineRealmProxyInterface) obj).realmSet$remarks((String) reader.nextString());
                }
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        obj = realm.copyToRealm(obj);
        return obj;
    }

    public static com.knowrecorder.develop.model.realm.TimeLine copyOrUpdate(Realm realm, com.knowrecorder.develop.model.realm.TimeLine object, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().threadId != realm.threadId) {
            throw new IllegalArgumentException("Objects which belong to Realm instances in other threads cannot be copied into this Realm instance.");
        }
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return object;
        }
        final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get();
        RealmObjectProxy cachedRealmObject = cache.get(object);
        if (cachedRealmObject != null) {
            return (com.knowrecorder.develop.model.realm.TimeLine) cachedRealmObject;
        } else {
            return copy(realm, object, update, cache);
        }
    }

    public static com.knowrecorder.develop.model.realm.TimeLine copy(Realm realm, com.knowrecorder.develop.model.realm.TimeLine newObject, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        RealmObjectProxy cachedRealmObject = cache.get(newObject);
        if (cachedRealmObject != null) {
            return (com.knowrecorder.develop.model.realm.TimeLine) cachedRealmObject;
        } else {
            // rejecting default values to avoid creating unexpected objects from RealmModel/RealmList fields.
            com.knowrecorder.develop.model.realm.TimeLine realmObject = realm.createObjectInternal(com.knowrecorder.develop.model.realm.TimeLine.class, false, Collections.<String>emptyList());
            cache.put(newObject, (RealmObjectProxy) realmObject);
            ((TimeLineRealmProxyInterface) realmObject).realmSet$mid(((TimeLineRealmProxyInterface) newObject).realmGet$mid());
            ((TimeLineRealmProxyInterface) realmObject).realmSet$startRun(((TimeLineRealmProxyInterface) newObject).realmGet$startRun());
            ((TimeLineRealmProxyInterface) realmObject).realmSet$endRun(((TimeLineRealmProxyInterface) newObject).realmGet$endRun());
            ((TimeLineRealmProxyInterface) realmObject).realmSet$type(((TimeLineRealmProxyInterface) newObject).realmGet$type());
            ((TimeLineRealmProxyInterface) realmObject).realmSet$remarks(((TimeLineRealmProxyInterface) newObject).realmGet$remarks());
            return realmObject;
        }
    }

    public static long insert(Realm realm, com.knowrecorder.develop.model.realm.TimeLine object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(com.knowrecorder.develop.model.realm.TimeLine.class);
        long tableNativePtr = table.getNativeTablePointer();
        TimeLineColumnInfo columnInfo = (TimeLineColumnInfo) realm.schema.getColumnInfo(com.knowrecorder.develop.model.realm.TimeLine.class);
        long rowIndex = Table.nativeAddEmptyRow(tableNativePtr, 1);
        cache.put(object, rowIndex);
        Table.nativeSetLong(tableNativePtr, columnInfo.midIndex, rowIndex, ((TimeLineRealmProxyInterface)object).realmGet$mid(), false);
        Table.nativeSetFloat(tableNativePtr, columnInfo.startRunIndex, rowIndex, ((TimeLineRealmProxyInterface)object).realmGet$startRun(), false);
        Table.nativeSetFloat(tableNativePtr, columnInfo.endRunIndex, rowIndex, ((TimeLineRealmProxyInterface)object).realmGet$endRun(), false);
        String realmGet$type = ((TimeLineRealmProxyInterface)object).realmGet$type();
        if (realmGet$type != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.typeIndex, rowIndex, realmGet$type, false);
        }
        String realmGet$remarks = ((TimeLineRealmProxyInterface)object).realmGet$remarks();
        if (realmGet$remarks != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.remarksIndex, rowIndex, realmGet$remarks, false);
        }
        return rowIndex;
    }

    public static void insert(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(com.knowrecorder.develop.model.realm.TimeLine.class);
        long tableNativePtr = table.getNativeTablePointer();
        TimeLineColumnInfo columnInfo = (TimeLineColumnInfo) realm.schema.getColumnInfo(com.knowrecorder.develop.model.realm.TimeLine.class);
        com.knowrecorder.develop.model.realm.TimeLine object = null;
        while (objects.hasNext()) {
            object = (com.knowrecorder.develop.model.realm.TimeLine) objects.next();
            if(!cache.containsKey(object)) {
                if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                    cache.put(object, ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex());
                    continue;
                }
                long rowIndex = Table.nativeAddEmptyRow(tableNativePtr, 1);
                cache.put(object, rowIndex);
                Table.nativeSetLong(tableNativePtr, columnInfo.midIndex, rowIndex, ((TimeLineRealmProxyInterface)object).realmGet$mid(), false);
                Table.nativeSetFloat(tableNativePtr, columnInfo.startRunIndex, rowIndex, ((TimeLineRealmProxyInterface)object).realmGet$startRun(), false);
                Table.nativeSetFloat(tableNativePtr, columnInfo.endRunIndex, rowIndex, ((TimeLineRealmProxyInterface)object).realmGet$endRun(), false);
                String realmGet$type = ((TimeLineRealmProxyInterface)object).realmGet$type();
                if (realmGet$type != null) {
                    Table.nativeSetString(tableNativePtr, columnInfo.typeIndex, rowIndex, realmGet$type, false);
                }
                String realmGet$remarks = ((TimeLineRealmProxyInterface)object).realmGet$remarks();
                if (realmGet$remarks != null) {
                    Table.nativeSetString(tableNativePtr, columnInfo.remarksIndex, rowIndex, realmGet$remarks, false);
                }
            }
        }
    }

    public static long insertOrUpdate(Realm realm, com.knowrecorder.develop.model.realm.TimeLine object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(com.knowrecorder.develop.model.realm.TimeLine.class);
        long tableNativePtr = table.getNativeTablePointer();
        TimeLineColumnInfo columnInfo = (TimeLineColumnInfo) realm.schema.getColumnInfo(com.knowrecorder.develop.model.realm.TimeLine.class);
        long rowIndex = Table.nativeAddEmptyRow(tableNativePtr, 1);
        cache.put(object, rowIndex);
        Table.nativeSetLong(tableNativePtr, columnInfo.midIndex, rowIndex, ((TimeLineRealmProxyInterface)object).realmGet$mid(), false);
        Table.nativeSetFloat(tableNativePtr, columnInfo.startRunIndex, rowIndex, ((TimeLineRealmProxyInterface)object).realmGet$startRun(), false);
        Table.nativeSetFloat(tableNativePtr, columnInfo.endRunIndex, rowIndex, ((TimeLineRealmProxyInterface)object).realmGet$endRun(), false);
        String realmGet$type = ((TimeLineRealmProxyInterface)object).realmGet$type();
        if (realmGet$type != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.typeIndex, rowIndex, realmGet$type, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.typeIndex, rowIndex, false);
        }
        String realmGet$remarks = ((TimeLineRealmProxyInterface)object).realmGet$remarks();
        if (realmGet$remarks != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.remarksIndex, rowIndex, realmGet$remarks, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.remarksIndex, rowIndex, false);
        }
        return rowIndex;
    }

    public static void insertOrUpdate(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(com.knowrecorder.develop.model.realm.TimeLine.class);
        long tableNativePtr = table.getNativeTablePointer();
        TimeLineColumnInfo columnInfo = (TimeLineColumnInfo) realm.schema.getColumnInfo(com.knowrecorder.develop.model.realm.TimeLine.class);
        com.knowrecorder.develop.model.realm.TimeLine object = null;
        while (objects.hasNext()) {
            object = (com.knowrecorder.develop.model.realm.TimeLine) objects.next();
            if(!cache.containsKey(object)) {
                if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                    cache.put(object, ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex());
                    continue;
                }
                long rowIndex = Table.nativeAddEmptyRow(tableNativePtr, 1);
                cache.put(object, rowIndex);
                Table.nativeSetLong(tableNativePtr, columnInfo.midIndex, rowIndex, ((TimeLineRealmProxyInterface)object).realmGet$mid(), false);
                Table.nativeSetFloat(tableNativePtr, columnInfo.startRunIndex, rowIndex, ((TimeLineRealmProxyInterface)object).realmGet$startRun(), false);
                Table.nativeSetFloat(tableNativePtr, columnInfo.endRunIndex, rowIndex, ((TimeLineRealmProxyInterface)object).realmGet$endRun(), false);
                String realmGet$type = ((TimeLineRealmProxyInterface)object).realmGet$type();
                if (realmGet$type != null) {
                    Table.nativeSetString(tableNativePtr, columnInfo.typeIndex, rowIndex, realmGet$type, false);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.typeIndex, rowIndex, false);
                }
                String realmGet$remarks = ((TimeLineRealmProxyInterface)object).realmGet$remarks();
                if (realmGet$remarks != null) {
                    Table.nativeSetString(tableNativePtr, columnInfo.remarksIndex, rowIndex, realmGet$remarks, false);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.remarksIndex, rowIndex, false);
                }
            }
        }
    }

    public static com.knowrecorder.develop.model.realm.TimeLine createDetachedCopy(com.knowrecorder.develop.model.realm.TimeLine realmObject, int currentDepth, int maxDepth, Map<RealmModel, CacheData<RealmModel>> cache) {
        if (currentDepth > maxDepth || realmObject == null) {
            return null;
        }
        CacheData<RealmModel> cachedObject = cache.get(realmObject);
        com.knowrecorder.develop.model.realm.TimeLine unmanagedObject;
        if (cachedObject != null) {
            // Reuse cached object or recreate it because it was encountered at a lower depth.
            if (currentDepth >= cachedObject.minDepth) {
                return (com.knowrecorder.develop.model.realm.TimeLine)cachedObject.object;
            } else {
                unmanagedObject = (com.knowrecorder.develop.model.realm.TimeLine)cachedObject.object;
                cachedObject.minDepth = currentDepth;
            }
        } else {
            unmanagedObject = new com.knowrecorder.develop.model.realm.TimeLine();
            cache.put(realmObject, new RealmObjectProxy.CacheData<RealmModel>(currentDepth, unmanagedObject));
        }
        ((TimeLineRealmProxyInterface) unmanagedObject).realmSet$mid(((TimeLineRealmProxyInterface) realmObject).realmGet$mid());
        ((TimeLineRealmProxyInterface) unmanagedObject).realmSet$startRun(((TimeLineRealmProxyInterface) realmObject).realmGet$startRun());
        ((TimeLineRealmProxyInterface) unmanagedObject).realmSet$endRun(((TimeLineRealmProxyInterface) realmObject).realmGet$endRun());
        ((TimeLineRealmProxyInterface) unmanagedObject).realmSet$type(((TimeLineRealmProxyInterface) realmObject).realmGet$type());
        ((TimeLineRealmProxyInterface) unmanagedObject).realmSet$remarks(((TimeLineRealmProxyInterface) realmObject).realmGet$remarks());
        return unmanagedObject;
    }

    @Override
    public String toString() {
        if (!RealmObject.isValid(this)) {
            return "Invalid object";
        }
        StringBuilder stringBuilder = new StringBuilder("TimeLine = [");
        stringBuilder.append("{mid:");
        stringBuilder.append(realmGet$mid());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{startRun:");
        stringBuilder.append(realmGet$startRun());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{endRun:");
        stringBuilder.append(realmGet$endRun());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{type:");
        stringBuilder.append(realmGet$type() != null ? realmGet$type() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{remarks:");
        stringBuilder.append(realmGet$remarks() != null ? realmGet$remarks() : "null");
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
        TimeLineRealmProxy aTimeLine = (TimeLineRealmProxy)o;

        String path = proxyState.getRealm$realm().getPath();
        String otherPath = aTimeLine.proxyState.getRealm$realm().getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) return false;

        String tableName = proxyState.getRow$realm().getTable().getName();
        String otherTableName = aTimeLine.proxyState.getRow$realm().getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false;

        if (proxyState.getRow$realm().getIndex() != aTimeLine.proxyState.getRow$realm().getIndex()) return false;

        return true;
    }

}

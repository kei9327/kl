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

public class YouTubeDataRealmProxy extends com.knowrecorder.develop.model.realm.YouTubeData
    implements RealmObjectProxy, YouTubeDataRealmProxyInterface {

    static final class YouTubeDataColumnInfo extends ColumnInfo
        implements Cloneable {

        public long youtubeIdIndex;
        public long noteidIndex;
        public long createDateIndex;
        public long titleIndex;
        public long totalTimeIndex;

        YouTubeDataColumnInfo(String path, Table table) {
            final Map<String, Long> indicesMap = new HashMap<String, Long>(5);
            this.youtubeIdIndex = getValidColumnIndex(path, table, "YouTubeData", "youtubeId");
            indicesMap.put("youtubeId", this.youtubeIdIndex);
            this.noteidIndex = getValidColumnIndex(path, table, "YouTubeData", "noteid");
            indicesMap.put("noteid", this.noteidIndex);
            this.createDateIndex = getValidColumnIndex(path, table, "YouTubeData", "createDate");
            indicesMap.put("createDate", this.createDateIndex);
            this.titleIndex = getValidColumnIndex(path, table, "YouTubeData", "title");
            indicesMap.put("title", this.titleIndex);
            this.totalTimeIndex = getValidColumnIndex(path, table, "YouTubeData", "totalTime");
            indicesMap.put("totalTime", this.totalTimeIndex);

            setIndicesMap(indicesMap);
        }

        @Override
        public final void copyColumnInfoFrom(ColumnInfo other) {
            final YouTubeDataColumnInfo otherInfo = (YouTubeDataColumnInfo) other;
            this.youtubeIdIndex = otherInfo.youtubeIdIndex;
            this.noteidIndex = otherInfo.noteidIndex;
            this.createDateIndex = otherInfo.createDateIndex;
            this.titleIndex = otherInfo.titleIndex;
            this.totalTimeIndex = otherInfo.totalTimeIndex;

            setIndicesMap(otherInfo.getIndicesMap());
        }

        @Override
        public final YouTubeDataColumnInfo clone() {
            return (YouTubeDataColumnInfo) super.clone();
        }

    }
    private YouTubeDataColumnInfo columnInfo;
    private ProxyState<com.knowrecorder.develop.model.realm.YouTubeData> proxyState;
    private static final List<String> FIELD_NAMES;
    static {
        List<String> fieldNames = new ArrayList<String>();
        fieldNames.add("youtubeId");
        fieldNames.add("noteid");
        fieldNames.add("createDate");
        fieldNames.add("title");
        fieldNames.add("totalTime");
        FIELD_NAMES = Collections.unmodifiableList(fieldNames);
    }

    YouTubeDataRealmProxy() {
        if (proxyState == null) {
            injectObjectContext();
        }
        proxyState.setConstructionFinished();
    }

    private void injectObjectContext() {
        final BaseRealm.RealmObjectContext context = BaseRealm.objectContext.get();
        this.columnInfo = (YouTubeDataColumnInfo) context.getColumnInfo();
        this.proxyState = new ProxyState<com.knowrecorder.develop.model.realm.YouTubeData>(com.knowrecorder.develop.model.realm.YouTubeData.class, this);
        proxyState.setRealm$realm(context.getRealm());
        proxyState.setRow$realm(context.getRow());
        proxyState.setAcceptDefaultValue$realm(context.getAcceptDefaultValue());
        proxyState.setExcludeFields$realm(context.getExcludeFields());
    }

    @SuppressWarnings("cast")
    public String realmGet$youtubeId() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.youtubeIdIndex);
    }

    public void realmSet$youtubeId(String value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        if (proxyState.isUnderConstruction()) {
            // default value of the primary key is always ignored.
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        throw new io.realm.exceptions.RealmException("Primary key field 'youtubeId' cannot be changed after object was created.");
    }

    @SuppressWarnings("cast")
    public long realmGet$noteid() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (long) proxyState.getRow$realm().getLong(columnInfo.noteidIndex);
    }

    public void realmSet$noteid(long value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setLong(columnInfo.noteidIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setLong(columnInfo.noteidIndex, value);
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
    public float realmGet$totalTime() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (float) proxyState.getRow$realm().getFloat(columnInfo.totalTimeIndex);
    }

    public void realmSet$totalTime(float value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setFloat(columnInfo.totalTimeIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setFloat(columnInfo.totalTimeIndex, value);
    }

    public static RealmObjectSchema createRealmObjectSchema(RealmSchema realmSchema) {
        if (!realmSchema.contains("YouTubeData")) {
            RealmObjectSchema realmObjectSchema = realmSchema.create("YouTubeData");
            realmObjectSchema.add(new Property("youtubeId", RealmFieldType.STRING, Property.PRIMARY_KEY, Property.INDEXED, !Property.REQUIRED));
            realmObjectSchema.add(new Property("noteid", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("createDate", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED));
            realmObjectSchema.add(new Property("title", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED));
            realmObjectSchema.add(new Property("totalTime", RealmFieldType.FLOAT, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            return realmObjectSchema;
        }
        return realmSchema.get("YouTubeData");
    }

    public static Table initTable(SharedRealm sharedRealm) {
        if (!sharedRealm.hasTable("class_YouTubeData")) {
            Table table = sharedRealm.getTable("class_YouTubeData");
            table.addColumn(RealmFieldType.STRING, "youtubeId", Table.NULLABLE);
            table.addColumn(RealmFieldType.INTEGER, "noteid", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.STRING, "createDate", Table.NULLABLE);
            table.addColumn(RealmFieldType.STRING, "title", Table.NULLABLE);
            table.addColumn(RealmFieldType.FLOAT, "totalTime", Table.NOT_NULLABLE);
            table.addSearchIndex(table.getColumnIndex("youtubeId"));
            table.setPrimaryKey("youtubeId");
            return table;
        }
        return sharedRealm.getTable("class_YouTubeData");
    }

    public static YouTubeDataColumnInfo validateTable(SharedRealm sharedRealm, boolean allowExtraColumns) {
        if (sharedRealm.hasTable("class_YouTubeData")) {
            Table table = sharedRealm.getTable("class_YouTubeData");
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

            final YouTubeDataColumnInfo columnInfo = new YouTubeDataColumnInfo(sharedRealm.getPath(), table);

            if (!table.hasPrimaryKey()) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Primary key not defined for field 'youtubeId' in existing Realm file. @PrimaryKey was added.");
            } else {
                if (table.getPrimaryKey() != columnInfo.youtubeIdIndex) {
                    throw new RealmMigrationNeededException(sharedRealm.getPath(), "Primary Key annotation definition was changed, from field " + table.getColumnName(table.getPrimaryKey()) + " to field youtubeId");
                }
            }

            if (!columnTypes.containsKey("youtubeId")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'youtubeId' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("youtubeId") != RealmFieldType.STRING) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'String' for field 'youtubeId' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.youtubeIdIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(),"@PrimaryKey field 'youtubeId' does not support null values in the existing Realm file. Migrate using RealmObjectSchema.setNullable(), or mark the field as @Required.");
            }
            if (!table.hasSearchIndex(table.getColumnIndex("youtubeId"))) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Index not defined for field 'youtubeId' in existing Realm file. Either set @Index or migrate using io.realm.internal.Table.removeSearchIndex().");
            }
            if (!columnTypes.containsKey("noteid")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'noteid' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("noteid") != RealmFieldType.INTEGER) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'long' for field 'noteid' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.noteidIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'noteid' does support null values in the existing Realm file. Use corresponding boxed type for field 'noteid' or migrate using RealmObjectSchema.setNullable().");
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
            if (!columnTypes.containsKey("totalTime")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'totalTime' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("totalTime") != RealmFieldType.FLOAT) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'float' for field 'totalTime' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.totalTimeIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'totalTime' does support null values in the existing Realm file. Use corresponding boxed type for field 'totalTime' or migrate using RealmObjectSchema.setNullable().");
            }
            return columnInfo;
        } else {
            throw new RealmMigrationNeededException(sharedRealm.getPath(), "The 'YouTubeData' class is missing from the schema for this Realm.");
        }
    }

    public static String getTableName() {
        return "class_YouTubeData";
    }

    public static List<String> getFieldNames() {
        return FIELD_NAMES;
    }

    @SuppressWarnings("cast")
    public static com.knowrecorder.develop.model.realm.YouTubeData createOrUpdateUsingJsonObject(Realm realm, JSONObject json, boolean update)
        throws JSONException {
        final List<String> excludeFields = Collections.<String> emptyList();
        com.knowrecorder.develop.model.realm.YouTubeData obj = null;
        if (update) {
            Table table = realm.getTable(com.knowrecorder.develop.model.realm.YouTubeData.class);
            long pkColumnIndex = table.getPrimaryKey();
            long rowIndex = TableOrView.NO_MATCH;
            if (json.isNull("youtubeId")) {
                rowIndex = table.findFirstNull(pkColumnIndex);
            } else {
                rowIndex = table.findFirstString(pkColumnIndex, json.getString("youtubeId"));
            }
            if (rowIndex != TableOrView.NO_MATCH) {
                final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get();
                try {
                    objectContext.set(realm, table.getUncheckedRow(rowIndex), realm.schema.getColumnInfo(com.knowrecorder.develop.model.realm.YouTubeData.class), false, Collections.<String> emptyList());
                    obj = new io.realm.YouTubeDataRealmProxy();
                } finally {
                    objectContext.clear();
                }
            }
        }
        if (obj == null) {
            if (json.has("youtubeId")) {
                if (json.isNull("youtubeId")) {
                    obj = (io.realm.YouTubeDataRealmProxy) realm.createObjectInternal(com.knowrecorder.develop.model.realm.YouTubeData.class, null, true, excludeFields);
                } else {
                    obj = (io.realm.YouTubeDataRealmProxy) realm.createObjectInternal(com.knowrecorder.develop.model.realm.YouTubeData.class, json.getString("youtubeId"), true, excludeFields);
                }
            } else {
                throw new IllegalArgumentException("JSON object doesn't have the primary key field 'youtubeId'.");
            }
        }
        if (json.has("noteid")) {
            if (json.isNull("noteid")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'noteid' to null.");
            } else {
                ((YouTubeDataRealmProxyInterface) obj).realmSet$noteid((long) json.getLong("noteid"));
            }
        }
        if (json.has("createDate")) {
            if (json.isNull("createDate")) {
                ((YouTubeDataRealmProxyInterface) obj).realmSet$createDate(null);
            } else {
                ((YouTubeDataRealmProxyInterface) obj).realmSet$createDate((String) json.getString("createDate"));
            }
        }
        if (json.has("title")) {
            if (json.isNull("title")) {
                ((YouTubeDataRealmProxyInterface) obj).realmSet$title(null);
            } else {
                ((YouTubeDataRealmProxyInterface) obj).realmSet$title((String) json.getString("title"));
            }
        }
        if (json.has("totalTime")) {
            if (json.isNull("totalTime")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'totalTime' to null.");
            } else {
                ((YouTubeDataRealmProxyInterface) obj).realmSet$totalTime((float) json.getDouble("totalTime"));
            }
        }
        return obj;
    }

    @SuppressWarnings("cast")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static com.knowrecorder.develop.model.realm.YouTubeData createUsingJsonStream(Realm realm, JsonReader reader)
        throws IOException {
        boolean jsonHasPrimaryKey = false;
        com.knowrecorder.develop.model.realm.YouTubeData obj = new com.knowrecorder.develop.model.realm.YouTubeData();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("youtubeId")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((YouTubeDataRealmProxyInterface) obj).realmSet$youtubeId(null);
                } else {
                    ((YouTubeDataRealmProxyInterface) obj).realmSet$youtubeId((String) reader.nextString());
                }
                jsonHasPrimaryKey = true;
            } else if (name.equals("noteid")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'noteid' to null.");
                } else {
                    ((YouTubeDataRealmProxyInterface) obj).realmSet$noteid((long) reader.nextLong());
                }
            } else if (name.equals("createDate")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((YouTubeDataRealmProxyInterface) obj).realmSet$createDate(null);
                } else {
                    ((YouTubeDataRealmProxyInterface) obj).realmSet$createDate((String) reader.nextString());
                }
            } else if (name.equals("title")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((YouTubeDataRealmProxyInterface) obj).realmSet$title(null);
                } else {
                    ((YouTubeDataRealmProxyInterface) obj).realmSet$title((String) reader.nextString());
                }
            } else if (name.equals("totalTime")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'totalTime' to null.");
                } else {
                    ((YouTubeDataRealmProxyInterface) obj).realmSet$totalTime((float) reader.nextDouble());
                }
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        if (!jsonHasPrimaryKey) {
            throw new IllegalArgumentException("JSON object doesn't have the primary key field 'youtubeId'.");
        }
        obj = realm.copyToRealm(obj);
        return obj;
    }

    public static com.knowrecorder.develop.model.realm.YouTubeData copyOrUpdate(Realm realm, com.knowrecorder.develop.model.realm.YouTubeData object, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().threadId != realm.threadId) {
            throw new IllegalArgumentException("Objects which belong to Realm instances in other threads cannot be copied into this Realm instance.");
        }
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return object;
        }
        final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get();
        RealmObjectProxy cachedRealmObject = cache.get(object);
        if (cachedRealmObject != null) {
            return (com.knowrecorder.develop.model.realm.YouTubeData) cachedRealmObject;
        } else {
            com.knowrecorder.develop.model.realm.YouTubeData realmObject = null;
            boolean canUpdate = update;
            if (canUpdate) {
                Table table = realm.getTable(com.knowrecorder.develop.model.realm.YouTubeData.class);
                long pkColumnIndex = table.getPrimaryKey();
                String value = ((YouTubeDataRealmProxyInterface) object).realmGet$youtubeId();
                long rowIndex = TableOrView.NO_MATCH;
                if (value == null) {
                    rowIndex = table.findFirstNull(pkColumnIndex);
                } else {
                    rowIndex = table.findFirstString(pkColumnIndex, value);
                }
                if (rowIndex != TableOrView.NO_MATCH) {
                    try {
                        objectContext.set(realm, table.getUncheckedRow(rowIndex), realm.schema.getColumnInfo(com.knowrecorder.develop.model.realm.YouTubeData.class), false, Collections.<String> emptyList());
                        realmObject = new io.realm.YouTubeDataRealmProxy();
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

    public static com.knowrecorder.develop.model.realm.YouTubeData copy(Realm realm, com.knowrecorder.develop.model.realm.YouTubeData newObject, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        RealmObjectProxy cachedRealmObject = cache.get(newObject);
        if (cachedRealmObject != null) {
            return (com.knowrecorder.develop.model.realm.YouTubeData) cachedRealmObject;
        } else {
            // rejecting default values to avoid creating unexpected objects from RealmModel/RealmList fields.
            com.knowrecorder.develop.model.realm.YouTubeData realmObject = realm.createObjectInternal(com.knowrecorder.develop.model.realm.YouTubeData.class, ((YouTubeDataRealmProxyInterface) newObject).realmGet$youtubeId(), false, Collections.<String>emptyList());
            cache.put(newObject, (RealmObjectProxy) realmObject);
            ((YouTubeDataRealmProxyInterface) realmObject).realmSet$noteid(((YouTubeDataRealmProxyInterface) newObject).realmGet$noteid());
            ((YouTubeDataRealmProxyInterface) realmObject).realmSet$createDate(((YouTubeDataRealmProxyInterface) newObject).realmGet$createDate());
            ((YouTubeDataRealmProxyInterface) realmObject).realmSet$title(((YouTubeDataRealmProxyInterface) newObject).realmGet$title());
            ((YouTubeDataRealmProxyInterface) realmObject).realmSet$totalTime(((YouTubeDataRealmProxyInterface) newObject).realmGet$totalTime());
            return realmObject;
        }
    }

    public static long insert(Realm realm, com.knowrecorder.develop.model.realm.YouTubeData object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(com.knowrecorder.develop.model.realm.YouTubeData.class);
        long tableNativePtr = table.getNativeTablePointer();
        YouTubeDataColumnInfo columnInfo = (YouTubeDataColumnInfo) realm.schema.getColumnInfo(com.knowrecorder.develop.model.realm.YouTubeData.class);
        long pkColumnIndex = table.getPrimaryKey();
        String primaryKeyValue = ((YouTubeDataRealmProxyInterface) object).realmGet$youtubeId();
        long rowIndex = TableOrView.NO_MATCH;
        if (primaryKeyValue == null) {
            rowIndex = Table.nativeFindFirstNull(tableNativePtr, pkColumnIndex);
        } else {
            rowIndex = Table.nativeFindFirstString(tableNativePtr, pkColumnIndex, primaryKeyValue);
        }
        if (rowIndex == TableOrView.NO_MATCH) {
            rowIndex = table.addEmptyRowWithPrimaryKey(primaryKeyValue, false);
        } else {
            Table.throwDuplicatePrimaryKeyException(primaryKeyValue);
        }
        cache.put(object, rowIndex);
        Table.nativeSetLong(tableNativePtr, columnInfo.noteidIndex, rowIndex, ((YouTubeDataRealmProxyInterface)object).realmGet$noteid(), false);
        String realmGet$createDate = ((YouTubeDataRealmProxyInterface)object).realmGet$createDate();
        if (realmGet$createDate != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.createDateIndex, rowIndex, realmGet$createDate, false);
        }
        String realmGet$title = ((YouTubeDataRealmProxyInterface)object).realmGet$title();
        if (realmGet$title != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.titleIndex, rowIndex, realmGet$title, false);
        }
        Table.nativeSetFloat(tableNativePtr, columnInfo.totalTimeIndex, rowIndex, ((YouTubeDataRealmProxyInterface)object).realmGet$totalTime(), false);
        return rowIndex;
    }

    public static void insert(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(com.knowrecorder.develop.model.realm.YouTubeData.class);
        long tableNativePtr = table.getNativeTablePointer();
        YouTubeDataColumnInfo columnInfo = (YouTubeDataColumnInfo) realm.schema.getColumnInfo(com.knowrecorder.develop.model.realm.YouTubeData.class);
        long pkColumnIndex = table.getPrimaryKey();
        com.knowrecorder.develop.model.realm.YouTubeData object = null;
        while (objects.hasNext()) {
            object = (com.knowrecorder.develop.model.realm.YouTubeData) objects.next();
            if(!cache.containsKey(object)) {
                if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                    cache.put(object, ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex());
                    continue;
                }
                String primaryKeyValue = ((YouTubeDataRealmProxyInterface) object).realmGet$youtubeId();
                long rowIndex = TableOrView.NO_MATCH;
                if (primaryKeyValue == null) {
                    rowIndex = Table.nativeFindFirstNull(tableNativePtr, pkColumnIndex);
                } else {
                    rowIndex = Table.nativeFindFirstString(tableNativePtr, pkColumnIndex, primaryKeyValue);
                }
                if (rowIndex == TableOrView.NO_MATCH) {
                    rowIndex = table.addEmptyRowWithPrimaryKey(primaryKeyValue, false);
                } else {
                    Table.throwDuplicatePrimaryKeyException(primaryKeyValue);
                }
                cache.put(object, rowIndex);
                Table.nativeSetLong(tableNativePtr, columnInfo.noteidIndex, rowIndex, ((YouTubeDataRealmProxyInterface)object).realmGet$noteid(), false);
                String realmGet$createDate = ((YouTubeDataRealmProxyInterface)object).realmGet$createDate();
                if (realmGet$createDate != null) {
                    Table.nativeSetString(tableNativePtr, columnInfo.createDateIndex, rowIndex, realmGet$createDate, false);
                }
                String realmGet$title = ((YouTubeDataRealmProxyInterface)object).realmGet$title();
                if (realmGet$title != null) {
                    Table.nativeSetString(tableNativePtr, columnInfo.titleIndex, rowIndex, realmGet$title, false);
                }
                Table.nativeSetFloat(tableNativePtr, columnInfo.totalTimeIndex, rowIndex, ((YouTubeDataRealmProxyInterface)object).realmGet$totalTime(), false);
            }
        }
    }

    public static long insertOrUpdate(Realm realm, com.knowrecorder.develop.model.realm.YouTubeData object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(com.knowrecorder.develop.model.realm.YouTubeData.class);
        long tableNativePtr = table.getNativeTablePointer();
        YouTubeDataColumnInfo columnInfo = (YouTubeDataColumnInfo) realm.schema.getColumnInfo(com.knowrecorder.develop.model.realm.YouTubeData.class);
        long pkColumnIndex = table.getPrimaryKey();
        String primaryKeyValue = ((YouTubeDataRealmProxyInterface) object).realmGet$youtubeId();
        long rowIndex = TableOrView.NO_MATCH;
        if (primaryKeyValue == null) {
            rowIndex = Table.nativeFindFirstNull(tableNativePtr, pkColumnIndex);
        } else {
            rowIndex = Table.nativeFindFirstString(tableNativePtr, pkColumnIndex, primaryKeyValue);
        }
        if (rowIndex == TableOrView.NO_MATCH) {
            rowIndex = table.addEmptyRowWithPrimaryKey(primaryKeyValue, false);
        }
        cache.put(object, rowIndex);
        Table.nativeSetLong(tableNativePtr, columnInfo.noteidIndex, rowIndex, ((YouTubeDataRealmProxyInterface)object).realmGet$noteid(), false);
        String realmGet$createDate = ((YouTubeDataRealmProxyInterface)object).realmGet$createDate();
        if (realmGet$createDate != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.createDateIndex, rowIndex, realmGet$createDate, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.createDateIndex, rowIndex, false);
        }
        String realmGet$title = ((YouTubeDataRealmProxyInterface)object).realmGet$title();
        if (realmGet$title != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.titleIndex, rowIndex, realmGet$title, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.titleIndex, rowIndex, false);
        }
        Table.nativeSetFloat(tableNativePtr, columnInfo.totalTimeIndex, rowIndex, ((YouTubeDataRealmProxyInterface)object).realmGet$totalTime(), false);
        return rowIndex;
    }

    public static void insertOrUpdate(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(com.knowrecorder.develop.model.realm.YouTubeData.class);
        long tableNativePtr = table.getNativeTablePointer();
        YouTubeDataColumnInfo columnInfo = (YouTubeDataColumnInfo) realm.schema.getColumnInfo(com.knowrecorder.develop.model.realm.YouTubeData.class);
        long pkColumnIndex = table.getPrimaryKey();
        com.knowrecorder.develop.model.realm.YouTubeData object = null;
        while (objects.hasNext()) {
            object = (com.knowrecorder.develop.model.realm.YouTubeData) objects.next();
            if(!cache.containsKey(object)) {
                if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                    cache.put(object, ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex());
                    continue;
                }
                String primaryKeyValue = ((YouTubeDataRealmProxyInterface) object).realmGet$youtubeId();
                long rowIndex = TableOrView.NO_MATCH;
                if (primaryKeyValue == null) {
                    rowIndex = Table.nativeFindFirstNull(tableNativePtr, pkColumnIndex);
                } else {
                    rowIndex = Table.nativeFindFirstString(tableNativePtr, pkColumnIndex, primaryKeyValue);
                }
                if (rowIndex == TableOrView.NO_MATCH) {
                    rowIndex = table.addEmptyRowWithPrimaryKey(primaryKeyValue, false);
                }
                cache.put(object, rowIndex);
                Table.nativeSetLong(tableNativePtr, columnInfo.noteidIndex, rowIndex, ((YouTubeDataRealmProxyInterface)object).realmGet$noteid(), false);
                String realmGet$createDate = ((YouTubeDataRealmProxyInterface)object).realmGet$createDate();
                if (realmGet$createDate != null) {
                    Table.nativeSetString(tableNativePtr, columnInfo.createDateIndex, rowIndex, realmGet$createDate, false);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.createDateIndex, rowIndex, false);
                }
                String realmGet$title = ((YouTubeDataRealmProxyInterface)object).realmGet$title();
                if (realmGet$title != null) {
                    Table.nativeSetString(tableNativePtr, columnInfo.titleIndex, rowIndex, realmGet$title, false);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.titleIndex, rowIndex, false);
                }
                Table.nativeSetFloat(tableNativePtr, columnInfo.totalTimeIndex, rowIndex, ((YouTubeDataRealmProxyInterface)object).realmGet$totalTime(), false);
            }
        }
    }

    public static com.knowrecorder.develop.model.realm.YouTubeData createDetachedCopy(com.knowrecorder.develop.model.realm.YouTubeData realmObject, int currentDepth, int maxDepth, Map<RealmModel, CacheData<RealmModel>> cache) {
        if (currentDepth > maxDepth || realmObject == null) {
            return null;
        }
        CacheData<RealmModel> cachedObject = cache.get(realmObject);
        com.knowrecorder.develop.model.realm.YouTubeData unmanagedObject;
        if (cachedObject != null) {
            // Reuse cached object or recreate it because it was encountered at a lower depth.
            if (currentDepth >= cachedObject.minDepth) {
                return (com.knowrecorder.develop.model.realm.YouTubeData)cachedObject.object;
            } else {
                unmanagedObject = (com.knowrecorder.develop.model.realm.YouTubeData)cachedObject.object;
                cachedObject.minDepth = currentDepth;
            }
        } else {
            unmanagedObject = new com.knowrecorder.develop.model.realm.YouTubeData();
            cache.put(realmObject, new RealmObjectProxy.CacheData<RealmModel>(currentDepth, unmanagedObject));
        }
        ((YouTubeDataRealmProxyInterface) unmanagedObject).realmSet$youtubeId(((YouTubeDataRealmProxyInterface) realmObject).realmGet$youtubeId());
        ((YouTubeDataRealmProxyInterface) unmanagedObject).realmSet$noteid(((YouTubeDataRealmProxyInterface) realmObject).realmGet$noteid());
        ((YouTubeDataRealmProxyInterface) unmanagedObject).realmSet$createDate(((YouTubeDataRealmProxyInterface) realmObject).realmGet$createDate());
        ((YouTubeDataRealmProxyInterface) unmanagedObject).realmSet$title(((YouTubeDataRealmProxyInterface) realmObject).realmGet$title());
        ((YouTubeDataRealmProxyInterface) unmanagedObject).realmSet$totalTime(((YouTubeDataRealmProxyInterface) realmObject).realmGet$totalTime());
        return unmanagedObject;
    }

    static com.knowrecorder.develop.model.realm.YouTubeData update(Realm realm, com.knowrecorder.develop.model.realm.YouTubeData realmObject, com.knowrecorder.develop.model.realm.YouTubeData newObject, Map<RealmModel, RealmObjectProxy> cache) {
        ((YouTubeDataRealmProxyInterface) realmObject).realmSet$noteid(((YouTubeDataRealmProxyInterface) newObject).realmGet$noteid());
        ((YouTubeDataRealmProxyInterface) realmObject).realmSet$createDate(((YouTubeDataRealmProxyInterface) newObject).realmGet$createDate());
        ((YouTubeDataRealmProxyInterface) realmObject).realmSet$title(((YouTubeDataRealmProxyInterface) newObject).realmGet$title());
        ((YouTubeDataRealmProxyInterface) realmObject).realmSet$totalTime(((YouTubeDataRealmProxyInterface) newObject).realmGet$totalTime());
        return realmObject;
    }

    @Override
    public String toString() {
        if (!RealmObject.isValid(this)) {
            return "Invalid object";
        }
        StringBuilder stringBuilder = new StringBuilder("YouTubeData = [");
        stringBuilder.append("{youtubeId:");
        stringBuilder.append(realmGet$youtubeId() != null ? realmGet$youtubeId() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{noteid:");
        stringBuilder.append(realmGet$noteid());
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
        stringBuilder.append("{totalTime:");
        stringBuilder.append(realmGet$totalTime());
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
        YouTubeDataRealmProxy aYouTubeData = (YouTubeDataRealmProxy)o;

        String path = proxyState.getRealm$realm().getPath();
        String otherPath = aYouTubeData.proxyState.getRealm$realm().getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) return false;

        String tableName = proxyState.getRow$realm().getTable().getName();
        String otherTableName = aYouTubeData.proxyState.getRow$realm().getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false;

        if (proxyState.getRow$realm().getIndex() != aYouTubeData.proxyState.getRow$realm().getIndex()) return false;

        return true;
    }

}

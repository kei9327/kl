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

public class PacketObjectRealmProxy extends com.knowrecorder.develop.model.realm.PacketObject
    implements RealmObjectProxy, PacketObjectRealmProxyInterface {

    static final class PacketObjectColumnInfo extends ColumnInfo
        implements Cloneable {

        public long idIndex;
        public long midIndex;
        public long noteidIndex;
        public long pageidIndex;
        public long typeIndex;
        public long bodyIndex;
        public long runtimeIndex;
        public long isStaticIndex;
        public long isPDFPageIndex;
        public long isEditingModeIndex;
        public long isAddPageIndex;
        public long isDrawnIndex;

        PacketObjectColumnInfo(String path, Table table) {
            final Map<String, Long> indicesMap = new HashMap<String, Long>(12);
            this.idIndex = getValidColumnIndex(path, table, "PacketObject", "id");
            indicesMap.put("id", this.idIndex);
            this.midIndex = getValidColumnIndex(path, table, "PacketObject", "mid");
            indicesMap.put("mid", this.midIndex);
            this.noteidIndex = getValidColumnIndex(path, table, "PacketObject", "noteid");
            indicesMap.put("noteid", this.noteidIndex);
            this.pageidIndex = getValidColumnIndex(path, table, "PacketObject", "pageid");
            indicesMap.put("pageid", this.pageidIndex);
            this.typeIndex = getValidColumnIndex(path, table, "PacketObject", "type");
            indicesMap.put("type", this.typeIndex);
            this.bodyIndex = getValidColumnIndex(path, table, "PacketObject", "body");
            indicesMap.put("body", this.bodyIndex);
            this.runtimeIndex = getValidColumnIndex(path, table, "PacketObject", "runtime");
            indicesMap.put("runtime", this.runtimeIndex);
            this.isStaticIndex = getValidColumnIndex(path, table, "PacketObject", "isStatic");
            indicesMap.put("isStatic", this.isStaticIndex);
            this.isPDFPageIndex = getValidColumnIndex(path, table, "PacketObject", "isPDFPage");
            indicesMap.put("isPDFPage", this.isPDFPageIndex);
            this.isEditingModeIndex = getValidColumnIndex(path, table, "PacketObject", "isEditingMode");
            indicesMap.put("isEditingMode", this.isEditingModeIndex);
            this.isAddPageIndex = getValidColumnIndex(path, table, "PacketObject", "isAddPage");
            indicesMap.put("isAddPage", this.isAddPageIndex);
            this.isDrawnIndex = getValidColumnIndex(path, table, "PacketObject", "isDrawn");
            indicesMap.put("isDrawn", this.isDrawnIndex);

            setIndicesMap(indicesMap);
        }

        @Override
        public final void copyColumnInfoFrom(ColumnInfo other) {
            final PacketObjectColumnInfo otherInfo = (PacketObjectColumnInfo) other;
            this.idIndex = otherInfo.idIndex;
            this.midIndex = otherInfo.midIndex;
            this.noteidIndex = otherInfo.noteidIndex;
            this.pageidIndex = otherInfo.pageidIndex;
            this.typeIndex = otherInfo.typeIndex;
            this.bodyIndex = otherInfo.bodyIndex;
            this.runtimeIndex = otherInfo.runtimeIndex;
            this.isStaticIndex = otherInfo.isStaticIndex;
            this.isPDFPageIndex = otherInfo.isPDFPageIndex;
            this.isEditingModeIndex = otherInfo.isEditingModeIndex;
            this.isAddPageIndex = otherInfo.isAddPageIndex;
            this.isDrawnIndex = otherInfo.isDrawnIndex;

            setIndicesMap(otherInfo.getIndicesMap());
        }

        @Override
        public final PacketObjectColumnInfo clone() {
            return (PacketObjectColumnInfo) super.clone();
        }

    }
    private PacketObjectColumnInfo columnInfo;
    private ProxyState<com.knowrecorder.develop.model.realm.PacketObject> proxyState;
    private static final List<String> FIELD_NAMES;
    static {
        List<String> fieldNames = new ArrayList<String>();
        fieldNames.add("id");
        fieldNames.add("mid");
        fieldNames.add("noteid");
        fieldNames.add("pageid");
        fieldNames.add("type");
        fieldNames.add("body");
        fieldNames.add("runtime");
        fieldNames.add("isStatic");
        fieldNames.add("isPDFPage");
        fieldNames.add("isEditingMode");
        fieldNames.add("isAddPage");
        fieldNames.add("isDrawn");
        FIELD_NAMES = Collections.unmodifiableList(fieldNames);
    }

    PacketObjectRealmProxy() {
        if (proxyState == null) {
            injectObjectContext();
        }
        proxyState.setConstructionFinished();
    }

    private void injectObjectContext() {
        final BaseRealm.RealmObjectContext context = BaseRealm.objectContext.get();
        this.columnInfo = (PacketObjectColumnInfo) context.getColumnInfo();
        this.proxyState = new ProxyState<com.knowrecorder.develop.model.realm.PacketObject>(com.knowrecorder.develop.model.realm.PacketObject.class, this);
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
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setLong(columnInfo.idIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setLong(columnInfo.idIndex, value);
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
    public long realmGet$pageid() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (long) proxyState.getRow$realm().getLong(columnInfo.pageidIndex);
    }

    public void realmSet$pageid(long value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setLong(columnInfo.pageidIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setLong(columnInfo.pageidIndex, value);
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
    public String realmGet$body() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.bodyIndex);
    }

    public void realmSet$body(String value) {
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
                row.getTable().setNull(columnInfo.bodyIndex, row.getIndex(), true);
                return;
            }
            row.getTable().setString(columnInfo.bodyIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.bodyIndex);
            return;
        }
        proxyState.getRow$realm().setString(columnInfo.bodyIndex, value);
    }

    @SuppressWarnings("cast")
    public float realmGet$runtime() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (float) proxyState.getRow$realm().getFloat(columnInfo.runtimeIndex);
    }

    public void realmSet$runtime(float value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setFloat(columnInfo.runtimeIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setFloat(columnInfo.runtimeIndex, value);
    }

    @SuppressWarnings("cast")
    public boolean realmGet$isStatic() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (boolean) proxyState.getRow$realm().getBoolean(columnInfo.isStaticIndex);
    }

    public void realmSet$isStatic(boolean value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setBoolean(columnInfo.isStaticIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setBoolean(columnInfo.isStaticIndex, value);
    }

    @SuppressWarnings("cast")
    public boolean realmGet$isPDFPage() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (boolean) proxyState.getRow$realm().getBoolean(columnInfo.isPDFPageIndex);
    }

    public void realmSet$isPDFPage(boolean value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setBoolean(columnInfo.isPDFPageIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setBoolean(columnInfo.isPDFPageIndex, value);
    }

    @SuppressWarnings("cast")
    public boolean realmGet$isEditingMode() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (boolean) proxyState.getRow$realm().getBoolean(columnInfo.isEditingModeIndex);
    }

    public void realmSet$isEditingMode(boolean value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setBoolean(columnInfo.isEditingModeIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setBoolean(columnInfo.isEditingModeIndex, value);
    }

    @SuppressWarnings("cast")
    public boolean realmGet$isAddPage() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (boolean) proxyState.getRow$realm().getBoolean(columnInfo.isAddPageIndex);
    }

    public void realmSet$isAddPage(boolean value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setBoolean(columnInfo.isAddPageIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setBoolean(columnInfo.isAddPageIndex, value);
    }

    @SuppressWarnings("cast")
    public boolean realmGet$isDrawn() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (boolean) proxyState.getRow$realm().getBoolean(columnInfo.isDrawnIndex);
    }

    public void realmSet$isDrawn(boolean value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setBoolean(columnInfo.isDrawnIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setBoolean(columnInfo.isDrawnIndex, value);
    }

    public static RealmObjectSchema createRealmObjectSchema(RealmSchema realmSchema) {
        if (!realmSchema.contains("PacketObject")) {
            RealmObjectSchema realmObjectSchema = realmSchema.create("PacketObject");
            realmObjectSchema.add(new Property("id", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("mid", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("noteid", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("pageid", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("type", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED));
            realmObjectSchema.add(new Property("body", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED));
            realmObjectSchema.add(new Property("runtime", RealmFieldType.FLOAT, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("isStatic", RealmFieldType.BOOLEAN, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("isPDFPage", RealmFieldType.BOOLEAN, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("isEditingMode", RealmFieldType.BOOLEAN, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("isAddPage", RealmFieldType.BOOLEAN, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("isDrawn", RealmFieldType.BOOLEAN, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            return realmObjectSchema;
        }
        return realmSchema.get("PacketObject");
    }

    public static Table initTable(SharedRealm sharedRealm) {
        if (!sharedRealm.hasTable("class_PacketObject")) {
            Table table = sharedRealm.getTable("class_PacketObject");
            table.addColumn(RealmFieldType.INTEGER, "id", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.INTEGER, "mid", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.INTEGER, "noteid", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.INTEGER, "pageid", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.STRING, "type", Table.NULLABLE);
            table.addColumn(RealmFieldType.STRING, "body", Table.NULLABLE);
            table.addColumn(RealmFieldType.FLOAT, "runtime", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.BOOLEAN, "isStatic", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.BOOLEAN, "isPDFPage", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.BOOLEAN, "isEditingMode", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.BOOLEAN, "isAddPage", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.BOOLEAN, "isDrawn", Table.NOT_NULLABLE);
            table.setPrimaryKey("");
            return table;
        }
        return sharedRealm.getTable("class_PacketObject");
    }

    public static PacketObjectColumnInfo validateTable(SharedRealm sharedRealm, boolean allowExtraColumns) {
        if (sharedRealm.hasTable("class_PacketObject")) {
            Table table = sharedRealm.getTable("class_PacketObject");
            final long columnCount = table.getColumnCount();
            if (columnCount != 12) {
                if (columnCount < 12) {
                    throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field count is less than expected - expected 12 but was " + columnCount);
                }
                if (allowExtraColumns) {
                    RealmLog.debug("Field count is more than expected - expected 12 but was %1$d", columnCount);
                } else {
                    throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field count is more than expected - expected 12 but was " + columnCount);
                }
            }
            Map<String, RealmFieldType> columnTypes = new HashMap<String, RealmFieldType>();
            for (long i = 0; i < columnCount; i++) {
                columnTypes.put(table.getColumnName(i), table.getColumnType(i));
            }

            final PacketObjectColumnInfo columnInfo = new PacketObjectColumnInfo(sharedRealm.getPath(), table);

            if (table.hasPrimaryKey()) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Primary Key defined for field " + table.getColumnName(table.getPrimaryKey()) + " was removed.");
            }

            if (!columnTypes.containsKey("id")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'id' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("id") != RealmFieldType.INTEGER) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'long' for field 'id' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.idIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'id' does support null values in the existing Realm file. Use corresponding boxed type for field 'id' or migrate using RealmObjectSchema.setNullable().");
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
            if (!columnTypes.containsKey("noteid")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'noteid' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("noteid") != RealmFieldType.INTEGER) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'long' for field 'noteid' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.noteidIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'noteid' does support null values in the existing Realm file. Use corresponding boxed type for field 'noteid' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("pageid")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'pageid' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("pageid") != RealmFieldType.INTEGER) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'long' for field 'pageid' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.pageidIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'pageid' does support null values in the existing Realm file. Use corresponding boxed type for field 'pageid' or migrate using RealmObjectSchema.setNullable().");
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
            if (!columnTypes.containsKey("body")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'body' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("body") != RealmFieldType.STRING) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'String' for field 'body' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.bodyIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'body' is required. Either set @Required to field 'body' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("runtime")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'runtime' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("runtime") != RealmFieldType.FLOAT) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'float' for field 'runtime' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.runtimeIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'runtime' does support null values in the existing Realm file. Use corresponding boxed type for field 'runtime' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("isStatic")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'isStatic' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("isStatic") != RealmFieldType.BOOLEAN) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'boolean' for field 'isStatic' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.isStaticIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'isStatic' does support null values in the existing Realm file. Use corresponding boxed type for field 'isStatic' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("isPDFPage")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'isPDFPage' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("isPDFPage") != RealmFieldType.BOOLEAN) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'boolean' for field 'isPDFPage' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.isPDFPageIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'isPDFPage' does support null values in the existing Realm file. Use corresponding boxed type for field 'isPDFPage' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("isEditingMode")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'isEditingMode' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("isEditingMode") != RealmFieldType.BOOLEAN) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'boolean' for field 'isEditingMode' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.isEditingModeIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'isEditingMode' does support null values in the existing Realm file. Use corresponding boxed type for field 'isEditingMode' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("isAddPage")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'isAddPage' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("isAddPage") != RealmFieldType.BOOLEAN) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'boolean' for field 'isAddPage' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.isAddPageIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'isAddPage' does support null values in the existing Realm file. Use corresponding boxed type for field 'isAddPage' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("isDrawn")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'isDrawn' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("isDrawn") != RealmFieldType.BOOLEAN) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'boolean' for field 'isDrawn' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.isDrawnIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'isDrawn' does support null values in the existing Realm file. Use corresponding boxed type for field 'isDrawn' or migrate using RealmObjectSchema.setNullable().");
            }
            return columnInfo;
        } else {
            throw new RealmMigrationNeededException(sharedRealm.getPath(), "The 'PacketObject' class is missing from the schema for this Realm.");
        }
    }

    public static String getTableName() {
        return "class_PacketObject";
    }

    public static List<String> getFieldNames() {
        return FIELD_NAMES;
    }

    @SuppressWarnings("cast")
    public static com.knowrecorder.develop.model.realm.PacketObject createOrUpdateUsingJsonObject(Realm realm, JSONObject json, boolean update)
        throws JSONException {
        final List<String> excludeFields = Collections.<String> emptyList();
        com.knowrecorder.develop.model.realm.PacketObject obj = realm.createObjectInternal(com.knowrecorder.develop.model.realm.PacketObject.class, true, excludeFields);
        if (json.has("id")) {
            if (json.isNull("id")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'id' to null.");
            } else {
                ((PacketObjectRealmProxyInterface) obj).realmSet$id((long) json.getLong("id"));
            }
        }
        if (json.has("mid")) {
            if (json.isNull("mid")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'mid' to null.");
            } else {
                ((PacketObjectRealmProxyInterface) obj).realmSet$mid((long) json.getLong("mid"));
            }
        }
        if (json.has("noteid")) {
            if (json.isNull("noteid")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'noteid' to null.");
            } else {
                ((PacketObjectRealmProxyInterface) obj).realmSet$noteid((long) json.getLong("noteid"));
            }
        }
        if (json.has("pageid")) {
            if (json.isNull("pageid")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'pageid' to null.");
            } else {
                ((PacketObjectRealmProxyInterface) obj).realmSet$pageid((long) json.getLong("pageid"));
            }
        }
        if (json.has("type")) {
            if (json.isNull("type")) {
                ((PacketObjectRealmProxyInterface) obj).realmSet$type(null);
            } else {
                ((PacketObjectRealmProxyInterface) obj).realmSet$type((String) json.getString("type"));
            }
        }
        if (json.has("body")) {
            if (json.isNull("body")) {
                ((PacketObjectRealmProxyInterface) obj).realmSet$body(null);
            } else {
                ((PacketObjectRealmProxyInterface) obj).realmSet$body((String) json.getString("body"));
            }
        }
        if (json.has("runtime")) {
            if (json.isNull("runtime")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'runtime' to null.");
            } else {
                ((PacketObjectRealmProxyInterface) obj).realmSet$runtime((float) json.getDouble("runtime"));
            }
        }
        if (json.has("isStatic")) {
            if (json.isNull("isStatic")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'isStatic' to null.");
            } else {
                ((PacketObjectRealmProxyInterface) obj).realmSet$isStatic((boolean) json.getBoolean("isStatic"));
            }
        }
        if (json.has("isPDFPage")) {
            if (json.isNull("isPDFPage")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'isPDFPage' to null.");
            } else {
                ((PacketObjectRealmProxyInterface) obj).realmSet$isPDFPage((boolean) json.getBoolean("isPDFPage"));
            }
        }
        if (json.has("isEditingMode")) {
            if (json.isNull("isEditingMode")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'isEditingMode' to null.");
            } else {
                ((PacketObjectRealmProxyInterface) obj).realmSet$isEditingMode((boolean) json.getBoolean("isEditingMode"));
            }
        }
        if (json.has("isAddPage")) {
            if (json.isNull("isAddPage")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'isAddPage' to null.");
            } else {
                ((PacketObjectRealmProxyInterface) obj).realmSet$isAddPage((boolean) json.getBoolean("isAddPage"));
            }
        }
        if (json.has("isDrawn")) {
            if (json.isNull("isDrawn")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'isDrawn' to null.");
            } else {
                ((PacketObjectRealmProxyInterface) obj).realmSet$isDrawn((boolean) json.getBoolean("isDrawn"));
            }
        }
        return obj;
    }

    @SuppressWarnings("cast")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static com.knowrecorder.develop.model.realm.PacketObject createUsingJsonStream(Realm realm, JsonReader reader)
        throws IOException {
        com.knowrecorder.develop.model.realm.PacketObject obj = new com.knowrecorder.develop.model.realm.PacketObject();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("id")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'id' to null.");
                } else {
                    ((PacketObjectRealmProxyInterface) obj).realmSet$id((long) reader.nextLong());
                }
            } else if (name.equals("mid")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'mid' to null.");
                } else {
                    ((PacketObjectRealmProxyInterface) obj).realmSet$mid((long) reader.nextLong());
                }
            } else if (name.equals("noteid")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'noteid' to null.");
                } else {
                    ((PacketObjectRealmProxyInterface) obj).realmSet$noteid((long) reader.nextLong());
                }
            } else if (name.equals("pageid")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'pageid' to null.");
                } else {
                    ((PacketObjectRealmProxyInterface) obj).realmSet$pageid((long) reader.nextLong());
                }
            } else if (name.equals("type")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((PacketObjectRealmProxyInterface) obj).realmSet$type(null);
                } else {
                    ((PacketObjectRealmProxyInterface) obj).realmSet$type((String) reader.nextString());
                }
            } else if (name.equals("body")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((PacketObjectRealmProxyInterface) obj).realmSet$body(null);
                } else {
                    ((PacketObjectRealmProxyInterface) obj).realmSet$body((String) reader.nextString());
                }
            } else if (name.equals("runtime")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'runtime' to null.");
                } else {
                    ((PacketObjectRealmProxyInterface) obj).realmSet$runtime((float) reader.nextDouble());
                }
            } else if (name.equals("isStatic")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'isStatic' to null.");
                } else {
                    ((PacketObjectRealmProxyInterface) obj).realmSet$isStatic((boolean) reader.nextBoolean());
                }
            } else if (name.equals("isPDFPage")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'isPDFPage' to null.");
                } else {
                    ((PacketObjectRealmProxyInterface) obj).realmSet$isPDFPage((boolean) reader.nextBoolean());
                }
            } else if (name.equals("isEditingMode")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'isEditingMode' to null.");
                } else {
                    ((PacketObjectRealmProxyInterface) obj).realmSet$isEditingMode((boolean) reader.nextBoolean());
                }
            } else if (name.equals("isAddPage")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'isAddPage' to null.");
                } else {
                    ((PacketObjectRealmProxyInterface) obj).realmSet$isAddPage((boolean) reader.nextBoolean());
                }
            } else if (name.equals("isDrawn")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'isDrawn' to null.");
                } else {
                    ((PacketObjectRealmProxyInterface) obj).realmSet$isDrawn((boolean) reader.nextBoolean());
                }
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        obj = realm.copyToRealm(obj);
        return obj;
    }

    public static com.knowrecorder.develop.model.realm.PacketObject copyOrUpdate(Realm realm, com.knowrecorder.develop.model.realm.PacketObject object, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().threadId != realm.threadId) {
            throw new IllegalArgumentException("Objects which belong to Realm instances in other threads cannot be copied into this Realm instance.");
        }
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return object;
        }
        final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get();
        RealmObjectProxy cachedRealmObject = cache.get(object);
        if (cachedRealmObject != null) {
            return (com.knowrecorder.develop.model.realm.PacketObject) cachedRealmObject;
        } else {
            return copy(realm, object, update, cache);
        }
    }

    public static com.knowrecorder.develop.model.realm.PacketObject copy(Realm realm, com.knowrecorder.develop.model.realm.PacketObject newObject, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        RealmObjectProxy cachedRealmObject = cache.get(newObject);
        if (cachedRealmObject != null) {
            return (com.knowrecorder.develop.model.realm.PacketObject) cachedRealmObject;
        } else {
            // rejecting default values to avoid creating unexpected objects from RealmModel/RealmList fields.
            com.knowrecorder.develop.model.realm.PacketObject realmObject = realm.createObjectInternal(com.knowrecorder.develop.model.realm.PacketObject.class, false, Collections.<String>emptyList());
            cache.put(newObject, (RealmObjectProxy) realmObject);
            ((PacketObjectRealmProxyInterface) realmObject).realmSet$id(((PacketObjectRealmProxyInterface) newObject).realmGet$id());
            ((PacketObjectRealmProxyInterface) realmObject).realmSet$mid(((PacketObjectRealmProxyInterface) newObject).realmGet$mid());
            ((PacketObjectRealmProxyInterface) realmObject).realmSet$noteid(((PacketObjectRealmProxyInterface) newObject).realmGet$noteid());
            ((PacketObjectRealmProxyInterface) realmObject).realmSet$pageid(((PacketObjectRealmProxyInterface) newObject).realmGet$pageid());
            ((PacketObjectRealmProxyInterface) realmObject).realmSet$type(((PacketObjectRealmProxyInterface) newObject).realmGet$type());
            ((PacketObjectRealmProxyInterface) realmObject).realmSet$body(((PacketObjectRealmProxyInterface) newObject).realmGet$body());
            ((PacketObjectRealmProxyInterface) realmObject).realmSet$runtime(((PacketObjectRealmProxyInterface) newObject).realmGet$runtime());
            ((PacketObjectRealmProxyInterface) realmObject).realmSet$isStatic(((PacketObjectRealmProxyInterface) newObject).realmGet$isStatic());
            ((PacketObjectRealmProxyInterface) realmObject).realmSet$isPDFPage(((PacketObjectRealmProxyInterface) newObject).realmGet$isPDFPage());
            ((PacketObjectRealmProxyInterface) realmObject).realmSet$isEditingMode(((PacketObjectRealmProxyInterface) newObject).realmGet$isEditingMode());
            ((PacketObjectRealmProxyInterface) realmObject).realmSet$isAddPage(((PacketObjectRealmProxyInterface) newObject).realmGet$isAddPage());
            ((PacketObjectRealmProxyInterface) realmObject).realmSet$isDrawn(((PacketObjectRealmProxyInterface) newObject).realmGet$isDrawn());
            return realmObject;
        }
    }

    public static long insert(Realm realm, com.knowrecorder.develop.model.realm.PacketObject object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(com.knowrecorder.develop.model.realm.PacketObject.class);
        long tableNativePtr = table.getNativeTablePointer();
        PacketObjectColumnInfo columnInfo = (PacketObjectColumnInfo) realm.schema.getColumnInfo(com.knowrecorder.develop.model.realm.PacketObject.class);
        long rowIndex = Table.nativeAddEmptyRow(tableNativePtr, 1);
        cache.put(object, rowIndex);
        Table.nativeSetLong(tableNativePtr, columnInfo.idIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$id(), false);
        Table.nativeSetLong(tableNativePtr, columnInfo.midIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$mid(), false);
        Table.nativeSetLong(tableNativePtr, columnInfo.noteidIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$noteid(), false);
        Table.nativeSetLong(tableNativePtr, columnInfo.pageidIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$pageid(), false);
        String realmGet$type = ((PacketObjectRealmProxyInterface)object).realmGet$type();
        if (realmGet$type != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.typeIndex, rowIndex, realmGet$type, false);
        }
        String realmGet$body = ((PacketObjectRealmProxyInterface)object).realmGet$body();
        if (realmGet$body != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.bodyIndex, rowIndex, realmGet$body, false);
        }
        Table.nativeSetFloat(tableNativePtr, columnInfo.runtimeIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$runtime(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.isStaticIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$isStatic(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.isPDFPageIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$isPDFPage(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.isEditingModeIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$isEditingMode(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.isAddPageIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$isAddPage(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.isDrawnIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$isDrawn(), false);
        return rowIndex;
    }

    public static void insert(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(com.knowrecorder.develop.model.realm.PacketObject.class);
        long tableNativePtr = table.getNativeTablePointer();
        PacketObjectColumnInfo columnInfo = (PacketObjectColumnInfo) realm.schema.getColumnInfo(com.knowrecorder.develop.model.realm.PacketObject.class);
        com.knowrecorder.develop.model.realm.PacketObject object = null;
        while (objects.hasNext()) {
            object = (com.knowrecorder.develop.model.realm.PacketObject) objects.next();
            if(!cache.containsKey(object)) {
                if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                    cache.put(object, ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex());
                    continue;
                }
                long rowIndex = Table.nativeAddEmptyRow(tableNativePtr, 1);
                cache.put(object, rowIndex);
                Table.nativeSetLong(tableNativePtr, columnInfo.idIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$id(), false);
                Table.nativeSetLong(tableNativePtr, columnInfo.midIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$mid(), false);
                Table.nativeSetLong(tableNativePtr, columnInfo.noteidIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$noteid(), false);
                Table.nativeSetLong(tableNativePtr, columnInfo.pageidIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$pageid(), false);
                String realmGet$type = ((PacketObjectRealmProxyInterface)object).realmGet$type();
                if (realmGet$type != null) {
                    Table.nativeSetString(tableNativePtr, columnInfo.typeIndex, rowIndex, realmGet$type, false);
                }
                String realmGet$body = ((PacketObjectRealmProxyInterface)object).realmGet$body();
                if (realmGet$body != null) {
                    Table.nativeSetString(tableNativePtr, columnInfo.bodyIndex, rowIndex, realmGet$body, false);
                }
                Table.nativeSetFloat(tableNativePtr, columnInfo.runtimeIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$runtime(), false);
                Table.nativeSetBoolean(tableNativePtr, columnInfo.isStaticIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$isStatic(), false);
                Table.nativeSetBoolean(tableNativePtr, columnInfo.isPDFPageIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$isPDFPage(), false);
                Table.nativeSetBoolean(tableNativePtr, columnInfo.isEditingModeIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$isEditingMode(), false);
                Table.nativeSetBoolean(tableNativePtr, columnInfo.isAddPageIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$isAddPage(), false);
                Table.nativeSetBoolean(tableNativePtr, columnInfo.isDrawnIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$isDrawn(), false);
            }
        }
    }

    public static long insertOrUpdate(Realm realm, com.knowrecorder.develop.model.realm.PacketObject object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(com.knowrecorder.develop.model.realm.PacketObject.class);
        long tableNativePtr = table.getNativeTablePointer();
        PacketObjectColumnInfo columnInfo = (PacketObjectColumnInfo) realm.schema.getColumnInfo(com.knowrecorder.develop.model.realm.PacketObject.class);
        long rowIndex = Table.nativeAddEmptyRow(tableNativePtr, 1);
        cache.put(object, rowIndex);
        Table.nativeSetLong(tableNativePtr, columnInfo.idIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$id(), false);
        Table.nativeSetLong(tableNativePtr, columnInfo.midIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$mid(), false);
        Table.nativeSetLong(tableNativePtr, columnInfo.noteidIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$noteid(), false);
        Table.nativeSetLong(tableNativePtr, columnInfo.pageidIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$pageid(), false);
        String realmGet$type = ((PacketObjectRealmProxyInterface)object).realmGet$type();
        if (realmGet$type != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.typeIndex, rowIndex, realmGet$type, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.typeIndex, rowIndex, false);
        }
        String realmGet$body = ((PacketObjectRealmProxyInterface)object).realmGet$body();
        if (realmGet$body != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.bodyIndex, rowIndex, realmGet$body, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.bodyIndex, rowIndex, false);
        }
        Table.nativeSetFloat(tableNativePtr, columnInfo.runtimeIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$runtime(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.isStaticIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$isStatic(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.isPDFPageIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$isPDFPage(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.isEditingModeIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$isEditingMode(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.isAddPageIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$isAddPage(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.isDrawnIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$isDrawn(), false);
        return rowIndex;
    }

    public static void insertOrUpdate(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(com.knowrecorder.develop.model.realm.PacketObject.class);
        long tableNativePtr = table.getNativeTablePointer();
        PacketObjectColumnInfo columnInfo = (PacketObjectColumnInfo) realm.schema.getColumnInfo(com.knowrecorder.develop.model.realm.PacketObject.class);
        com.knowrecorder.develop.model.realm.PacketObject object = null;
        while (objects.hasNext()) {
            object = (com.knowrecorder.develop.model.realm.PacketObject) objects.next();
            if(!cache.containsKey(object)) {
                if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                    cache.put(object, ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex());
                    continue;
                }
                long rowIndex = Table.nativeAddEmptyRow(tableNativePtr, 1);
                cache.put(object, rowIndex);
                Table.nativeSetLong(tableNativePtr, columnInfo.idIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$id(), false);
                Table.nativeSetLong(tableNativePtr, columnInfo.midIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$mid(), false);
                Table.nativeSetLong(tableNativePtr, columnInfo.noteidIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$noteid(), false);
                Table.nativeSetLong(tableNativePtr, columnInfo.pageidIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$pageid(), false);
                String realmGet$type = ((PacketObjectRealmProxyInterface)object).realmGet$type();
                if (realmGet$type != null) {
                    Table.nativeSetString(tableNativePtr, columnInfo.typeIndex, rowIndex, realmGet$type, false);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.typeIndex, rowIndex, false);
                }
                String realmGet$body = ((PacketObjectRealmProxyInterface)object).realmGet$body();
                if (realmGet$body != null) {
                    Table.nativeSetString(tableNativePtr, columnInfo.bodyIndex, rowIndex, realmGet$body, false);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.bodyIndex, rowIndex, false);
                }
                Table.nativeSetFloat(tableNativePtr, columnInfo.runtimeIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$runtime(), false);
                Table.nativeSetBoolean(tableNativePtr, columnInfo.isStaticIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$isStatic(), false);
                Table.nativeSetBoolean(tableNativePtr, columnInfo.isPDFPageIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$isPDFPage(), false);
                Table.nativeSetBoolean(tableNativePtr, columnInfo.isEditingModeIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$isEditingMode(), false);
                Table.nativeSetBoolean(tableNativePtr, columnInfo.isAddPageIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$isAddPage(), false);
                Table.nativeSetBoolean(tableNativePtr, columnInfo.isDrawnIndex, rowIndex, ((PacketObjectRealmProxyInterface)object).realmGet$isDrawn(), false);
            }
        }
    }

    public static com.knowrecorder.develop.model.realm.PacketObject createDetachedCopy(com.knowrecorder.develop.model.realm.PacketObject realmObject, int currentDepth, int maxDepth, Map<RealmModel, CacheData<RealmModel>> cache) {
        if (currentDepth > maxDepth || realmObject == null) {
            return null;
        }
        CacheData<RealmModel> cachedObject = cache.get(realmObject);
        com.knowrecorder.develop.model.realm.PacketObject unmanagedObject;
        if (cachedObject != null) {
            // Reuse cached object or recreate it because it was encountered at a lower depth.
            if (currentDepth >= cachedObject.minDepth) {
                return (com.knowrecorder.develop.model.realm.PacketObject)cachedObject.object;
            } else {
                unmanagedObject = (com.knowrecorder.develop.model.realm.PacketObject)cachedObject.object;
                cachedObject.minDepth = currentDepth;
            }
        } else {
            unmanagedObject = new com.knowrecorder.develop.model.realm.PacketObject();
            cache.put(realmObject, new RealmObjectProxy.CacheData<RealmModel>(currentDepth, unmanagedObject));
        }
        ((PacketObjectRealmProxyInterface) unmanagedObject).realmSet$id(((PacketObjectRealmProxyInterface) realmObject).realmGet$id());
        ((PacketObjectRealmProxyInterface) unmanagedObject).realmSet$mid(((PacketObjectRealmProxyInterface) realmObject).realmGet$mid());
        ((PacketObjectRealmProxyInterface) unmanagedObject).realmSet$noteid(((PacketObjectRealmProxyInterface) realmObject).realmGet$noteid());
        ((PacketObjectRealmProxyInterface) unmanagedObject).realmSet$pageid(((PacketObjectRealmProxyInterface) realmObject).realmGet$pageid());
        ((PacketObjectRealmProxyInterface) unmanagedObject).realmSet$type(((PacketObjectRealmProxyInterface) realmObject).realmGet$type());
        ((PacketObjectRealmProxyInterface) unmanagedObject).realmSet$body(((PacketObjectRealmProxyInterface) realmObject).realmGet$body());
        ((PacketObjectRealmProxyInterface) unmanagedObject).realmSet$runtime(((PacketObjectRealmProxyInterface) realmObject).realmGet$runtime());
        ((PacketObjectRealmProxyInterface) unmanagedObject).realmSet$isStatic(((PacketObjectRealmProxyInterface) realmObject).realmGet$isStatic());
        ((PacketObjectRealmProxyInterface) unmanagedObject).realmSet$isPDFPage(((PacketObjectRealmProxyInterface) realmObject).realmGet$isPDFPage());
        ((PacketObjectRealmProxyInterface) unmanagedObject).realmSet$isEditingMode(((PacketObjectRealmProxyInterface) realmObject).realmGet$isEditingMode());
        ((PacketObjectRealmProxyInterface) unmanagedObject).realmSet$isAddPage(((PacketObjectRealmProxyInterface) realmObject).realmGet$isAddPage());
        ((PacketObjectRealmProxyInterface) unmanagedObject).realmSet$isDrawn(((PacketObjectRealmProxyInterface) realmObject).realmGet$isDrawn());
        return unmanagedObject;
    }

    @Override
    public String toString() {
        if (!RealmObject.isValid(this)) {
            return "Invalid object";
        }
        StringBuilder stringBuilder = new StringBuilder("PacketObject = [");
        stringBuilder.append("{id:");
        stringBuilder.append(realmGet$id());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{mid:");
        stringBuilder.append(realmGet$mid());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{noteid:");
        stringBuilder.append(realmGet$noteid());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{pageid:");
        stringBuilder.append(realmGet$pageid());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{type:");
        stringBuilder.append(realmGet$type() != null ? realmGet$type() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{body:");
        stringBuilder.append(realmGet$body() != null ? realmGet$body() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{runtime:");
        stringBuilder.append(realmGet$runtime());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{isStatic:");
        stringBuilder.append(realmGet$isStatic());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{isPDFPage:");
        stringBuilder.append(realmGet$isPDFPage());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{isEditingMode:");
        stringBuilder.append(realmGet$isEditingMode());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{isAddPage:");
        stringBuilder.append(realmGet$isAddPage());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{isDrawn:");
        stringBuilder.append(realmGet$isDrawn());
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
        PacketObjectRealmProxy aPacketObject = (PacketObjectRealmProxy)o;

        String path = proxyState.getRealm$realm().getPath();
        String otherPath = aPacketObject.proxyState.getRealm$realm().getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) return false;

        String tableName = proxyState.getRow$realm().getTable().getName();
        String otherTableName = aPacketObject.proxyState.getRow$realm().getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false;

        if (proxyState.getRow$realm().getIndex() != aPacketObject.proxyState.getRow$realm().getIndex()) return false;

        return true;
    }

}

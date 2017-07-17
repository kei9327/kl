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

public class PageRealmProxy extends com.knowrecorder.develop.model.realm.Page
    implements RealmObjectProxy, PageRealmProxyInterface {

    static final class PageColumnInfo extends ColumnInfo
        implements Cloneable {

        public long idIndex;
        public long noteidIndex;
        public long pagenumIndex;
        public long runtimeIndex;
        public long isStaticIndex;
        public long isPDFPageIndex;
        public long scaleIndex;
        public long focalPointXIndex;
        public long focalPointYIndex;

        PageColumnInfo(String path, Table table) {
            final Map<String, Long> indicesMap = new HashMap<String, Long>(9);
            this.idIndex = getValidColumnIndex(path, table, "Page", "id");
            indicesMap.put("id", this.idIndex);
            this.noteidIndex = getValidColumnIndex(path, table, "Page", "noteid");
            indicesMap.put("noteid", this.noteidIndex);
            this.pagenumIndex = getValidColumnIndex(path, table, "Page", "pagenum");
            indicesMap.put("pagenum", this.pagenumIndex);
            this.runtimeIndex = getValidColumnIndex(path, table, "Page", "runtime");
            indicesMap.put("runtime", this.runtimeIndex);
            this.isStaticIndex = getValidColumnIndex(path, table, "Page", "isStatic");
            indicesMap.put("isStatic", this.isStaticIndex);
            this.isPDFPageIndex = getValidColumnIndex(path, table, "Page", "isPDFPage");
            indicesMap.put("isPDFPage", this.isPDFPageIndex);
            this.scaleIndex = getValidColumnIndex(path, table, "Page", "scale");
            indicesMap.put("scale", this.scaleIndex);
            this.focalPointXIndex = getValidColumnIndex(path, table, "Page", "focalPointX");
            indicesMap.put("focalPointX", this.focalPointXIndex);
            this.focalPointYIndex = getValidColumnIndex(path, table, "Page", "focalPointY");
            indicesMap.put("focalPointY", this.focalPointYIndex);

            setIndicesMap(indicesMap);
        }

        @Override
        public final void copyColumnInfoFrom(ColumnInfo other) {
            final PageColumnInfo otherInfo = (PageColumnInfo) other;
            this.idIndex = otherInfo.idIndex;
            this.noteidIndex = otherInfo.noteidIndex;
            this.pagenumIndex = otherInfo.pagenumIndex;
            this.runtimeIndex = otherInfo.runtimeIndex;
            this.isStaticIndex = otherInfo.isStaticIndex;
            this.isPDFPageIndex = otherInfo.isPDFPageIndex;
            this.scaleIndex = otherInfo.scaleIndex;
            this.focalPointXIndex = otherInfo.focalPointXIndex;
            this.focalPointYIndex = otherInfo.focalPointYIndex;

            setIndicesMap(otherInfo.getIndicesMap());
        }

        @Override
        public final PageColumnInfo clone() {
            return (PageColumnInfo) super.clone();
        }

    }
    private PageColumnInfo columnInfo;
    private ProxyState<com.knowrecorder.develop.model.realm.Page> proxyState;
    private static final List<String> FIELD_NAMES;
    static {
        List<String> fieldNames = new ArrayList<String>();
        fieldNames.add("id");
        fieldNames.add("noteid");
        fieldNames.add("pagenum");
        fieldNames.add("runtime");
        fieldNames.add("isStatic");
        fieldNames.add("isPDFPage");
        fieldNames.add("scale");
        fieldNames.add("focalPointX");
        fieldNames.add("focalPointY");
        FIELD_NAMES = Collections.unmodifiableList(fieldNames);
    }

    PageRealmProxy() {
        if (proxyState == null) {
            injectObjectContext();
        }
        proxyState.setConstructionFinished();
    }

    private void injectObjectContext() {
        final BaseRealm.RealmObjectContext context = BaseRealm.objectContext.get();
        this.columnInfo = (PageColumnInfo) context.getColumnInfo();
        this.proxyState = new ProxyState<com.knowrecorder.develop.model.realm.Page>(com.knowrecorder.develop.model.realm.Page.class, this);
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
    public int realmGet$pagenum() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (int) proxyState.getRow$realm().getLong(columnInfo.pagenumIndex);
    }

    public void realmSet$pagenum(int value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setLong(columnInfo.pagenumIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setLong(columnInfo.pagenumIndex, value);
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
    public float realmGet$scale() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (float) proxyState.getRow$realm().getFloat(columnInfo.scaleIndex);
    }

    public void realmSet$scale(float value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setFloat(columnInfo.scaleIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setFloat(columnInfo.scaleIndex, value);
    }

    @SuppressWarnings("cast")
    public float realmGet$focalPointX() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (float) proxyState.getRow$realm().getFloat(columnInfo.focalPointXIndex);
    }

    public void realmSet$focalPointX(float value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setFloat(columnInfo.focalPointXIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setFloat(columnInfo.focalPointXIndex, value);
    }

    @SuppressWarnings("cast")
    public float realmGet$focalPointY() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (float) proxyState.getRow$realm().getFloat(columnInfo.focalPointYIndex);
    }

    public void realmSet$focalPointY(float value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setFloat(columnInfo.focalPointYIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setFloat(columnInfo.focalPointYIndex, value);
    }

    public static RealmObjectSchema createRealmObjectSchema(RealmSchema realmSchema) {
        if (!realmSchema.contains("Page")) {
            RealmObjectSchema realmObjectSchema = realmSchema.create("Page");
            realmObjectSchema.add(new Property("id", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("noteid", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("pagenum", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("runtime", RealmFieldType.FLOAT, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("isStatic", RealmFieldType.BOOLEAN, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("isPDFPage", RealmFieldType.BOOLEAN, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("scale", RealmFieldType.FLOAT, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("focalPointX", RealmFieldType.FLOAT, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("focalPointY", RealmFieldType.FLOAT, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            return realmObjectSchema;
        }
        return realmSchema.get("Page");
    }

    public static Table initTable(SharedRealm sharedRealm) {
        if (!sharedRealm.hasTable("class_Page")) {
            Table table = sharedRealm.getTable("class_Page");
            table.addColumn(RealmFieldType.INTEGER, "id", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.INTEGER, "noteid", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.INTEGER, "pagenum", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.FLOAT, "runtime", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.BOOLEAN, "isStatic", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.BOOLEAN, "isPDFPage", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.FLOAT, "scale", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.FLOAT, "focalPointX", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.FLOAT, "focalPointY", Table.NOT_NULLABLE);
            table.setPrimaryKey("");
            return table;
        }
        return sharedRealm.getTable("class_Page");
    }

    public static PageColumnInfo validateTable(SharedRealm sharedRealm, boolean allowExtraColumns) {
        if (sharedRealm.hasTable("class_Page")) {
            Table table = sharedRealm.getTable("class_Page");
            final long columnCount = table.getColumnCount();
            if (columnCount != 9) {
                if (columnCount < 9) {
                    throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field count is less than expected - expected 9 but was " + columnCount);
                }
                if (allowExtraColumns) {
                    RealmLog.debug("Field count is more than expected - expected 9 but was %1$d", columnCount);
                } else {
                    throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field count is more than expected - expected 9 but was " + columnCount);
                }
            }
            Map<String, RealmFieldType> columnTypes = new HashMap<String, RealmFieldType>();
            for (long i = 0; i < columnCount; i++) {
                columnTypes.put(table.getColumnName(i), table.getColumnType(i));
            }

            final PageColumnInfo columnInfo = new PageColumnInfo(sharedRealm.getPath(), table);

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
            if (!columnTypes.containsKey("noteid")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'noteid' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("noteid") != RealmFieldType.INTEGER) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'long' for field 'noteid' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.noteidIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'noteid' does support null values in the existing Realm file. Use corresponding boxed type for field 'noteid' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("pagenum")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'pagenum' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("pagenum") != RealmFieldType.INTEGER) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'int' for field 'pagenum' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.pagenumIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'pagenum' does support null values in the existing Realm file. Use corresponding boxed type for field 'pagenum' or migrate using RealmObjectSchema.setNullable().");
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
            if (!columnTypes.containsKey("scale")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'scale' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("scale") != RealmFieldType.FLOAT) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'float' for field 'scale' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.scaleIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'scale' does support null values in the existing Realm file. Use corresponding boxed type for field 'scale' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("focalPointX")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'focalPointX' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("focalPointX") != RealmFieldType.FLOAT) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'float' for field 'focalPointX' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.focalPointXIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'focalPointX' does support null values in the existing Realm file. Use corresponding boxed type for field 'focalPointX' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("focalPointY")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'focalPointY' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("focalPointY") != RealmFieldType.FLOAT) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'float' for field 'focalPointY' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.focalPointYIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'focalPointY' does support null values in the existing Realm file. Use corresponding boxed type for field 'focalPointY' or migrate using RealmObjectSchema.setNullable().");
            }
            return columnInfo;
        } else {
            throw new RealmMigrationNeededException(sharedRealm.getPath(), "The 'Page' class is missing from the schema for this Realm.");
        }
    }

    public static String getTableName() {
        return "class_Page";
    }

    public static List<String> getFieldNames() {
        return FIELD_NAMES;
    }

    @SuppressWarnings("cast")
    public static com.knowrecorder.develop.model.realm.Page createOrUpdateUsingJsonObject(Realm realm, JSONObject json, boolean update)
        throws JSONException {
        final List<String> excludeFields = Collections.<String> emptyList();
        com.knowrecorder.develop.model.realm.Page obj = realm.createObjectInternal(com.knowrecorder.develop.model.realm.Page.class, true, excludeFields);
        if (json.has("id")) {
            if (json.isNull("id")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'id' to null.");
            } else {
                ((PageRealmProxyInterface) obj).realmSet$id((long) json.getLong("id"));
            }
        }
        if (json.has("noteid")) {
            if (json.isNull("noteid")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'noteid' to null.");
            } else {
                ((PageRealmProxyInterface) obj).realmSet$noteid((long) json.getLong("noteid"));
            }
        }
        if (json.has("pagenum")) {
            if (json.isNull("pagenum")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'pagenum' to null.");
            } else {
                ((PageRealmProxyInterface) obj).realmSet$pagenum((int) json.getInt("pagenum"));
            }
        }
        if (json.has("runtime")) {
            if (json.isNull("runtime")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'runtime' to null.");
            } else {
                ((PageRealmProxyInterface) obj).realmSet$runtime((float) json.getDouble("runtime"));
            }
        }
        if (json.has("isStatic")) {
            if (json.isNull("isStatic")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'isStatic' to null.");
            } else {
                ((PageRealmProxyInterface) obj).realmSet$isStatic((boolean) json.getBoolean("isStatic"));
            }
        }
        if (json.has("isPDFPage")) {
            if (json.isNull("isPDFPage")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'isPDFPage' to null.");
            } else {
                ((PageRealmProxyInterface) obj).realmSet$isPDFPage((boolean) json.getBoolean("isPDFPage"));
            }
        }
        if (json.has("scale")) {
            if (json.isNull("scale")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'scale' to null.");
            } else {
                ((PageRealmProxyInterface) obj).realmSet$scale((float) json.getDouble("scale"));
            }
        }
        if (json.has("focalPointX")) {
            if (json.isNull("focalPointX")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'focalPointX' to null.");
            } else {
                ((PageRealmProxyInterface) obj).realmSet$focalPointX((float) json.getDouble("focalPointX"));
            }
        }
        if (json.has("focalPointY")) {
            if (json.isNull("focalPointY")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'focalPointY' to null.");
            } else {
                ((PageRealmProxyInterface) obj).realmSet$focalPointY((float) json.getDouble("focalPointY"));
            }
        }
        return obj;
    }

    @SuppressWarnings("cast")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static com.knowrecorder.develop.model.realm.Page createUsingJsonStream(Realm realm, JsonReader reader)
        throws IOException {
        com.knowrecorder.develop.model.realm.Page obj = new com.knowrecorder.develop.model.realm.Page();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("id")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'id' to null.");
                } else {
                    ((PageRealmProxyInterface) obj).realmSet$id((long) reader.nextLong());
                }
            } else if (name.equals("noteid")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'noteid' to null.");
                } else {
                    ((PageRealmProxyInterface) obj).realmSet$noteid((long) reader.nextLong());
                }
            } else if (name.equals("pagenum")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'pagenum' to null.");
                } else {
                    ((PageRealmProxyInterface) obj).realmSet$pagenum((int) reader.nextInt());
                }
            } else if (name.equals("runtime")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'runtime' to null.");
                } else {
                    ((PageRealmProxyInterface) obj).realmSet$runtime((float) reader.nextDouble());
                }
            } else if (name.equals("isStatic")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'isStatic' to null.");
                } else {
                    ((PageRealmProxyInterface) obj).realmSet$isStatic((boolean) reader.nextBoolean());
                }
            } else if (name.equals("isPDFPage")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'isPDFPage' to null.");
                } else {
                    ((PageRealmProxyInterface) obj).realmSet$isPDFPage((boolean) reader.nextBoolean());
                }
            } else if (name.equals("scale")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'scale' to null.");
                } else {
                    ((PageRealmProxyInterface) obj).realmSet$scale((float) reader.nextDouble());
                }
            } else if (name.equals("focalPointX")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'focalPointX' to null.");
                } else {
                    ((PageRealmProxyInterface) obj).realmSet$focalPointX((float) reader.nextDouble());
                }
            } else if (name.equals("focalPointY")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'focalPointY' to null.");
                } else {
                    ((PageRealmProxyInterface) obj).realmSet$focalPointY((float) reader.nextDouble());
                }
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        obj = realm.copyToRealm(obj);
        return obj;
    }

    public static com.knowrecorder.develop.model.realm.Page copyOrUpdate(Realm realm, com.knowrecorder.develop.model.realm.Page object, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().threadId != realm.threadId) {
            throw new IllegalArgumentException("Objects which belong to Realm instances in other threads cannot be copied into this Realm instance.");
        }
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return object;
        }
        final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get();
        RealmObjectProxy cachedRealmObject = cache.get(object);
        if (cachedRealmObject != null) {
            return (com.knowrecorder.develop.model.realm.Page) cachedRealmObject;
        } else {
            return copy(realm, object, update, cache);
        }
    }

    public static com.knowrecorder.develop.model.realm.Page copy(Realm realm, com.knowrecorder.develop.model.realm.Page newObject, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        RealmObjectProxy cachedRealmObject = cache.get(newObject);
        if (cachedRealmObject != null) {
            return (com.knowrecorder.develop.model.realm.Page) cachedRealmObject;
        } else {
            // rejecting default values to avoid creating unexpected objects from RealmModel/RealmList fields.
            com.knowrecorder.develop.model.realm.Page realmObject = realm.createObjectInternal(com.knowrecorder.develop.model.realm.Page.class, false, Collections.<String>emptyList());
            cache.put(newObject, (RealmObjectProxy) realmObject);
            ((PageRealmProxyInterface) realmObject).realmSet$id(((PageRealmProxyInterface) newObject).realmGet$id());
            ((PageRealmProxyInterface) realmObject).realmSet$noteid(((PageRealmProxyInterface) newObject).realmGet$noteid());
            ((PageRealmProxyInterface) realmObject).realmSet$pagenum(((PageRealmProxyInterface) newObject).realmGet$pagenum());
            ((PageRealmProxyInterface) realmObject).realmSet$runtime(((PageRealmProxyInterface) newObject).realmGet$runtime());
            ((PageRealmProxyInterface) realmObject).realmSet$isStatic(((PageRealmProxyInterface) newObject).realmGet$isStatic());
            ((PageRealmProxyInterface) realmObject).realmSet$isPDFPage(((PageRealmProxyInterface) newObject).realmGet$isPDFPage());
            ((PageRealmProxyInterface) realmObject).realmSet$scale(((PageRealmProxyInterface) newObject).realmGet$scale());
            ((PageRealmProxyInterface) realmObject).realmSet$focalPointX(((PageRealmProxyInterface) newObject).realmGet$focalPointX());
            ((PageRealmProxyInterface) realmObject).realmSet$focalPointY(((PageRealmProxyInterface) newObject).realmGet$focalPointY());
            return realmObject;
        }
    }

    public static long insert(Realm realm, com.knowrecorder.develop.model.realm.Page object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(com.knowrecorder.develop.model.realm.Page.class);
        long tableNativePtr = table.getNativeTablePointer();
        PageColumnInfo columnInfo = (PageColumnInfo) realm.schema.getColumnInfo(com.knowrecorder.develop.model.realm.Page.class);
        long rowIndex = Table.nativeAddEmptyRow(tableNativePtr, 1);
        cache.put(object, rowIndex);
        Table.nativeSetLong(tableNativePtr, columnInfo.idIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$id(), false);
        Table.nativeSetLong(tableNativePtr, columnInfo.noteidIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$noteid(), false);
        Table.nativeSetLong(tableNativePtr, columnInfo.pagenumIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$pagenum(), false);
        Table.nativeSetFloat(tableNativePtr, columnInfo.runtimeIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$runtime(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.isStaticIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$isStatic(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.isPDFPageIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$isPDFPage(), false);
        Table.nativeSetFloat(tableNativePtr, columnInfo.scaleIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$scale(), false);
        Table.nativeSetFloat(tableNativePtr, columnInfo.focalPointXIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$focalPointX(), false);
        Table.nativeSetFloat(tableNativePtr, columnInfo.focalPointYIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$focalPointY(), false);
        return rowIndex;
    }

    public static void insert(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(com.knowrecorder.develop.model.realm.Page.class);
        long tableNativePtr = table.getNativeTablePointer();
        PageColumnInfo columnInfo = (PageColumnInfo) realm.schema.getColumnInfo(com.knowrecorder.develop.model.realm.Page.class);
        com.knowrecorder.develop.model.realm.Page object = null;
        while (objects.hasNext()) {
            object = (com.knowrecorder.develop.model.realm.Page) objects.next();
            if(!cache.containsKey(object)) {
                if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                    cache.put(object, ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex());
                    continue;
                }
                long rowIndex = Table.nativeAddEmptyRow(tableNativePtr, 1);
                cache.put(object, rowIndex);
                Table.nativeSetLong(tableNativePtr, columnInfo.idIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$id(), false);
                Table.nativeSetLong(tableNativePtr, columnInfo.noteidIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$noteid(), false);
                Table.nativeSetLong(tableNativePtr, columnInfo.pagenumIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$pagenum(), false);
                Table.nativeSetFloat(tableNativePtr, columnInfo.runtimeIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$runtime(), false);
                Table.nativeSetBoolean(tableNativePtr, columnInfo.isStaticIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$isStatic(), false);
                Table.nativeSetBoolean(tableNativePtr, columnInfo.isPDFPageIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$isPDFPage(), false);
                Table.nativeSetFloat(tableNativePtr, columnInfo.scaleIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$scale(), false);
                Table.nativeSetFloat(tableNativePtr, columnInfo.focalPointXIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$focalPointX(), false);
                Table.nativeSetFloat(tableNativePtr, columnInfo.focalPointYIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$focalPointY(), false);
            }
        }
    }

    public static long insertOrUpdate(Realm realm, com.knowrecorder.develop.model.realm.Page object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(com.knowrecorder.develop.model.realm.Page.class);
        long tableNativePtr = table.getNativeTablePointer();
        PageColumnInfo columnInfo = (PageColumnInfo) realm.schema.getColumnInfo(com.knowrecorder.develop.model.realm.Page.class);
        long rowIndex = Table.nativeAddEmptyRow(tableNativePtr, 1);
        cache.put(object, rowIndex);
        Table.nativeSetLong(tableNativePtr, columnInfo.idIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$id(), false);
        Table.nativeSetLong(tableNativePtr, columnInfo.noteidIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$noteid(), false);
        Table.nativeSetLong(tableNativePtr, columnInfo.pagenumIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$pagenum(), false);
        Table.nativeSetFloat(tableNativePtr, columnInfo.runtimeIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$runtime(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.isStaticIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$isStatic(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.isPDFPageIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$isPDFPage(), false);
        Table.nativeSetFloat(tableNativePtr, columnInfo.scaleIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$scale(), false);
        Table.nativeSetFloat(tableNativePtr, columnInfo.focalPointXIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$focalPointX(), false);
        Table.nativeSetFloat(tableNativePtr, columnInfo.focalPointYIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$focalPointY(), false);
        return rowIndex;
    }

    public static void insertOrUpdate(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(com.knowrecorder.develop.model.realm.Page.class);
        long tableNativePtr = table.getNativeTablePointer();
        PageColumnInfo columnInfo = (PageColumnInfo) realm.schema.getColumnInfo(com.knowrecorder.develop.model.realm.Page.class);
        com.knowrecorder.develop.model.realm.Page object = null;
        while (objects.hasNext()) {
            object = (com.knowrecorder.develop.model.realm.Page) objects.next();
            if(!cache.containsKey(object)) {
                if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                    cache.put(object, ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex());
                    continue;
                }
                long rowIndex = Table.nativeAddEmptyRow(tableNativePtr, 1);
                cache.put(object, rowIndex);
                Table.nativeSetLong(tableNativePtr, columnInfo.idIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$id(), false);
                Table.nativeSetLong(tableNativePtr, columnInfo.noteidIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$noteid(), false);
                Table.nativeSetLong(tableNativePtr, columnInfo.pagenumIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$pagenum(), false);
                Table.nativeSetFloat(tableNativePtr, columnInfo.runtimeIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$runtime(), false);
                Table.nativeSetBoolean(tableNativePtr, columnInfo.isStaticIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$isStatic(), false);
                Table.nativeSetBoolean(tableNativePtr, columnInfo.isPDFPageIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$isPDFPage(), false);
                Table.nativeSetFloat(tableNativePtr, columnInfo.scaleIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$scale(), false);
                Table.nativeSetFloat(tableNativePtr, columnInfo.focalPointXIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$focalPointX(), false);
                Table.nativeSetFloat(tableNativePtr, columnInfo.focalPointYIndex, rowIndex, ((PageRealmProxyInterface)object).realmGet$focalPointY(), false);
            }
        }
    }

    public static com.knowrecorder.develop.model.realm.Page createDetachedCopy(com.knowrecorder.develop.model.realm.Page realmObject, int currentDepth, int maxDepth, Map<RealmModel, CacheData<RealmModel>> cache) {
        if (currentDepth > maxDepth || realmObject == null) {
            return null;
        }
        CacheData<RealmModel> cachedObject = cache.get(realmObject);
        com.knowrecorder.develop.model.realm.Page unmanagedObject;
        if (cachedObject != null) {
            // Reuse cached object or recreate it because it was encountered at a lower depth.
            if (currentDepth >= cachedObject.minDepth) {
                return (com.knowrecorder.develop.model.realm.Page)cachedObject.object;
            } else {
                unmanagedObject = (com.knowrecorder.develop.model.realm.Page)cachedObject.object;
                cachedObject.minDepth = currentDepth;
            }
        } else {
            unmanagedObject = new com.knowrecorder.develop.model.realm.Page();
            cache.put(realmObject, new RealmObjectProxy.CacheData<RealmModel>(currentDepth, unmanagedObject));
        }
        ((PageRealmProxyInterface) unmanagedObject).realmSet$id(((PageRealmProxyInterface) realmObject).realmGet$id());
        ((PageRealmProxyInterface) unmanagedObject).realmSet$noteid(((PageRealmProxyInterface) realmObject).realmGet$noteid());
        ((PageRealmProxyInterface) unmanagedObject).realmSet$pagenum(((PageRealmProxyInterface) realmObject).realmGet$pagenum());
        ((PageRealmProxyInterface) unmanagedObject).realmSet$runtime(((PageRealmProxyInterface) realmObject).realmGet$runtime());
        ((PageRealmProxyInterface) unmanagedObject).realmSet$isStatic(((PageRealmProxyInterface) realmObject).realmGet$isStatic());
        ((PageRealmProxyInterface) unmanagedObject).realmSet$isPDFPage(((PageRealmProxyInterface) realmObject).realmGet$isPDFPage());
        ((PageRealmProxyInterface) unmanagedObject).realmSet$scale(((PageRealmProxyInterface) realmObject).realmGet$scale());
        ((PageRealmProxyInterface) unmanagedObject).realmSet$focalPointX(((PageRealmProxyInterface) realmObject).realmGet$focalPointX());
        ((PageRealmProxyInterface) unmanagedObject).realmSet$focalPointY(((PageRealmProxyInterface) realmObject).realmGet$focalPointY());
        return unmanagedObject;
    }

    @Override
    public String toString() {
        if (!RealmObject.isValid(this)) {
            return "Invalid object";
        }
        StringBuilder stringBuilder = new StringBuilder("Page = [");
        stringBuilder.append("{id:");
        stringBuilder.append(realmGet$id());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{noteid:");
        stringBuilder.append(realmGet$noteid());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{pagenum:");
        stringBuilder.append(realmGet$pagenum());
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
        stringBuilder.append("{scale:");
        stringBuilder.append(realmGet$scale());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{focalPointX:");
        stringBuilder.append(realmGet$focalPointX());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{focalPointY:");
        stringBuilder.append(realmGet$focalPointY());
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
        PageRealmProxy aPage = (PageRealmProxy)o;

        String path = proxyState.getRealm$realm().getPath();
        String otherPath = aPage.proxyState.getRealm$realm().getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) return false;

        String tableName = proxyState.getRow$realm().getTable().getName();
        String otherTableName = aPage.proxyState.getRow$realm().getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false;

        if (proxyState.getRow$realm().getIndex() != aPage.proxyState.getRow$realm().getIndex()) return false;

        return true;
    }

}

package io.realm;


import android.util.JsonReader;
import io.realm.RealmObjectSchema;
import io.realm.internal.ColumnInfo;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.Row;
import io.realm.internal.SharedRealm;
import io.realm.internal.Table;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;

@io.realm.annotations.RealmModule
class DefaultRealmModuleMediator extends RealmProxyMediator {

    private static final Set<Class<? extends RealmModel>> MODEL_CLASSES;
    static {
        Set<Class<? extends RealmModel>> modelClasses = new HashSet<Class<? extends RealmModel>>();
        modelClasses.add(com.knowrecorder.develop.model.realm.Page.class);
        modelClasses.add(com.knowrecorder.develop.model.realm.TimeLine.class);
        modelClasses.add(com.knowrecorder.develop.model.realm.PacketObject.class);
        modelClasses.add(com.knowrecorder.develop.model.realm.YouTubeData.class);
        modelClasses.add(com.knowrecorder.develop.model.realm.Note.class);
        modelClasses.add(com.knowrecorder.develop.model.realm.Notes.class);
        MODEL_CLASSES = Collections.unmodifiableSet(modelClasses);
    }

    @Override
    public Table createTable(Class<? extends RealmModel> clazz, SharedRealm sharedRealm) {
        checkClass(clazz);

        if (clazz.equals(com.knowrecorder.develop.model.realm.Page.class)) {
            return io.realm.PageRealmProxy.initTable(sharedRealm);
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.TimeLine.class)) {
            return io.realm.TimeLineRealmProxy.initTable(sharedRealm);
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.PacketObject.class)) {
            return io.realm.PacketObjectRealmProxy.initTable(sharedRealm);
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.YouTubeData.class)) {
            return io.realm.YouTubeDataRealmProxy.initTable(sharedRealm);
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.Note.class)) {
            return io.realm.NoteRealmProxy.initTable(sharedRealm);
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.Notes.class)) {
            return io.realm.NotesRealmProxy.initTable(sharedRealm);
        } else {
            throw getMissingProxyClassException(clazz);
        }
    }

    @Override
    public RealmObjectSchema createRealmObjectSchema(Class<? extends RealmModel> clazz, RealmSchema realmSchema) {
        checkClass(clazz);

        if (clazz.equals(com.knowrecorder.develop.model.realm.Page.class)) {
            return io.realm.PageRealmProxy.createRealmObjectSchema(realmSchema);
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.TimeLine.class)) {
            return io.realm.TimeLineRealmProxy.createRealmObjectSchema(realmSchema);
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.PacketObject.class)) {
            return io.realm.PacketObjectRealmProxy.createRealmObjectSchema(realmSchema);
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.YouTubeData.class)) {
            return io.realm.YouTubeDataRealmProxy.createRealmObjectSchema(realmSchema);
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.Note.class)) {
            return io.realm.NoteRealmProxy.createRealmObjectSchema(realmSchema);
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.Notes.class)) {
            return io.realm.NotesRealmProxy.createRealmObjectSchema(realmSchema);
        } else {
            throw getMissingProxyClassException(clazz);
        }
    }

    @Override
    public ColumnInfo validateTable(Class<? extends RealmModel> clazz, SharedRealm sharedRealm, boolean allowExtraColumns) {
        checkClass(clazz);

        if (clazz.equals(com.knowrecorder.develop.model.realm.Page.class)) {
            return io.realm.PageRealmProxy.validateTable(sharedRealm, allowExtraColumns);
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.TimeLine.class)) {
            return io.realm.TimeLineRealmProxy.validateTable(sharedRealm, allowExtraColumns);
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.PacketObject.class)) {
            return io.realm.PacketObjectRealmProxy.validateTable(sharedRealm, allowExtraColumns);
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.YouTubeData.class)) {
            return io.realm.YouTubeDataRealmProxy.validateTable(sharedRealm, allowExtraColumns);
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.Note.class)) {
            return io.realm.NoteRealmProxy.validateTable(sharedRealm, allowExtraColumns);
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.Notes.class)) {
            return io.realm.NotesRealmProxy.validateTable(sharedRealm, allowExtraColumns);
        } else {
            throw getMissingProxyClassException(clazz);
        }
    }

    @Override
    public List<String> getFieldNames(Class<? extends RealmModel> clazz) {
        checkClass(clazz);

        if (clazz.equals(com.knowrecorder.develop.model.realm.Page.class)) {
            return io.realm.PageRealmProxy.getFieldNames();
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.TimeLine.class)) {
            return io.realm.TimeLineRealmProxy.getFieldNames();
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.PacketObject.class)) {
            return io.realm.PacketObjectRealmProxy.getFieldNames();
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.YouTubeData.class)) {
            return io.realm.YouTubeDataRealmProxy.getFieldNames();
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.Note.class)) {
            return io.realm.NoteRealmProxy.getFieldNames();
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.Notes.class)) {
            return io.realm.NotesRealmProxy.getFieldNames();
        } else {
            throw getMissingProxyClassException(clazz);
        }
    }

    @Override
    public String getTableName(Class<? extends RealmModel> clazz) {
        checkClass(clazz);

        if (clazz.equals(com.knowrecorder.develop.model.realm.Page.class)) {
            return io.realm.PageRealmProxy.getTableName();
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.TimeLine.class)) {
            return io.realm.TimeLineRealmProxy.getTableName();
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.PacketObject.class)) {
            return io.realm.PacketObjectRealmProxy.getTableName();
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.YouTubeData.class)) {
            return io.realm.YouTubeDataRealmProxy.getTableName();
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.Note.class)) {
            return io.realm.NoteRealmProxy.getTableName();
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.Notes.class)) {
            return io.realm.NotesRealmProxy.getTableName();
        } else {
            throw getMissingProxyClassException(clazz);
        }
    }

    @Override
    public <E extends RealmModel> E newInstance(Class<E> clazz, Object baseRealm, Row row, ColumnInfo columnInfo, boolean acceptDefaultValue, List<String> excludeFields) {
        final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get();
        try {
            objectContext.set((BaseRealm) baseRealm, row, columnInfo, acceptDefaultValue, excludeFields);
            checkClass(clazz);

            if (clazz.equals(com.knowrecorder.develop.model.realm.Page.class)) {
                return clazz.cast(new io.realm.PageRealmProxy());
            } else if (clazz.equals(com.knowrecorder.develop.model.realm.TimeLine.class)) {
                return clazz.cast(new io.realm.TimeLineRealmProxy());
            } else if (clazz.equals(com.knowrecorder.develop.model.realm.PacketObject.class)) {
                return clazz.cast(new io.realm.PacketObjectRealmProxy());
            } else if (clazz.equals(com.knowrecorder.develop.model.realm.YouTubeData.class)) {
                return clazz.cast(new io.realm.YouTubeDataRealmProxy());
            } else if (clazz.equals(com.knowrecorder.develop.model.realm.Note.class)) {
                return clazz.cast(new io.realm.NoteRealmProxy());
            } else if (clazz.equals(com.knowrecorder.develop.model.realm.Notes.class)) {
                return clazz.cast(new io.realm.NotesRealmProxy());
            } else {
                throw getMissingProxyClassException(clazz);
            }
        } finally {
            objectContext.clear();
        }
    }

    @Override
    public Set<Class<? extends RealmModel>> getModelClasses() {
        return MODEL_CLASSES;
    }

    @Override
    public <E extends RealmModel> E copyOrUpdate(Realm realm, E obj, boolean update, Map<RealmModel, RealmObjectProxy> cache) {
        // This cast is correct because obj is either
        // generated by RealmProxy or the original type extending directly from RealmObject
        @SuppressWarnings("unchecked") Class<E> clazz = (Class<E>) ((obj instanceof RealmObjectProxy) ? obj.getClass().getSuperclass() : obj.getClass());

        if (clazz.equals(com.knowrecorder.develop.model.realm.Page.class)) {
            return clazz.cast(io.realm.PageRealmProxy.copyOrUpdate(realm, (com.knowrecorder.develop.model.realm.Page) obj, update, cache));
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.TimeLine.class)) {
            return clazz.cast(io.realm.TimeLineRealmProxy.copyOrUpdate(realm, (com.knowrecorder.develop.model.realm.TimeLine) obj, update, cache));
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.PacketObject.class)) {
            return clazz.cast(io.realm.PacketObjectRealmProxy.copyOrUpdate(realm, (com.knowrecorder.develop.model.realm.PacketObject) obj, update, cache));
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.YouTubeData.class)) {
            return clazz.cast(io.realm.YouTubeDataRealmProxy.copyOrUpdate(realm, (com.knowrecorder.develop.model.realm.YouTubeData) obj, update, cache));
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.Note.class)) {
            return clazz.cast(io.realm.NoteRealmProxy.copyOrUpdate(realm, (com.knowrecorder.develop.model.realm.Note) obj, update, cache));
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.Notes.class)) {
            return clazz.cast(io.realm.NotesRealmProxy.copyOrUpdate(realm, (com.knowrecorder.develop.model.realm.Notes) obj, update, cache));
        } else {
            throw getMissingProxyClassException(clazz);
        }
    }

    @Override
    public void insert(Realm realm, RealmModel object, Map<RealmModel, Long> cache) {
        // This cast is correct because obj is either
        // generated by RealmProxy or the original type extending directly from RealmObject
        @SuppressWarnings("unchecked") Class<RealmModel> clazz = (Class<RealmModel>) ((object instanceof RealmObjectProxy) ? object.getClass().getSuperclass() : object.getClass());

        if (clazz.equals(com.knowrecorder.develop.model.realm.Page.class)) {
            io.realm.PageRealmProxy.insert(realm, (com.knowrecorder.develop.model.realm.Page) object, cache);
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.TimeLine.class)) {
            io.realm.TimeLineRealmProxy.insert(realm, (com.knowrecorder.develop.model.realm.TimeLine) object, cache);
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.PacketObject.class)) {
            io.realm.PacketObjectRealmProxy.insert(realm, (com.knowrecorder.develop.model.realm.PacketObject) object, cache);
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.YouTubeData.class)) {
            io.realm.YouTubeDataRealmProxy.insert(realm, (com.knowrecorder.develop.model.realm.YouTubeData) object, cache);
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.Note.class)) {
            io.realm.NoteRealmProxy.insert(realm, (com.knowrecorder.develop.model.realm.Note) object, cache);
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.Notes.class)) {
            io.realm.NotesRealmProxy.insert(realm, (com.knowrecorder.develop.model.realm.Notes) object, cache);
        } else {
            throw getMissingProxyClassException(clazz);
        }
    }

    @Override
    public void insert(Realm realm, Collection<? extends RealmModel> objects) {
        Iterator<? extends RealmModel> iterator = objects.iterator();
        RealmModel object = null;
        Map<RealmModel, Long> cache = new HashMap<RealmModel, Long>(objects.size());
        if (iterator.hasNext()) {
            //  access the first element to figure out the clazz for the routing below
            object = iterator.next();
            // This cast is correct because obj is either
            // generated by RealmProxy or the original type extending directly from RealmObject
            @SuppressWarnings("unchecked") Class<RealmModel> clazz = (Class<RealmModel>) ((object instanceof RealmObjectProxy) ? object.getClass().getSuperclass() : object.getClass());

            if (clazz.equals(com.knowrecorder.develop.model.realm.Page.class)) {
                io.realm.PageRealmProxy.insert(realm, (com.knowrecorder.develop.model.realm.Page) object, cache);
            } else if (clazz.equals(com.knowrecorder.develop.model.realm.TimeLine.class)) {
                io.realm.TimeLineRealmProxy.insert(realm, (com.knowrecorder.develop.model.realm.TimeLine) object, cache);
            } else if (clazz.equals(com.knowrecorder.develop.model.realm.PacketObject.class)) {
                io.realm.PacketObjectRealmProxy.insert(realm, (com.knowrecorder.develop.model.realm.PacketObject) object, cache);
            } else if (clazz.equals(com.knowrecorder.develop.model.realm.YouTubeData.class)) {
                io.realm.YouTubeDataRealmProxy.insert(realm, (com.knowrecorder.develop.model.realm.YouTubeData) object, cache);
            } else if (clazz.equals(com.knowrecorder.develop.model.realm.Note.class)) {
                io.realm.NoteRealmProxy.insert(realm, (com.knowrecorder.develop.model.realm.Note) object, cache);
            } else if (clazz.equals(com.knowrecorder.develop.model.realm.Notes.class)) {
                io.realm.NotesRealmProxy.insert(realm, (com.knowrecorder.develop.model.realm.Notes) object, cache);
            } else {
                throw getMissingProxyClassException(clazz);
            }
            if (iterator.hasNext()) {
                if (clazz.equals(com.knowrecorder.develop.model.realm.Page.class)) {
                    io.realm.PageRealmProxy.insert(realm, iterator, cache);
                } else if (clazz.equals(com.knowrecorder.develop.model.realm.TimeLine.class)) {
                    io.realm.TimeLineRealmProxy.insert(realm, iterator, cache);
                } else if (clazz.equals(com.knowrecorder.develop.model.realm.PacketObject.class)) {
                    io.realm.PacketObjectRealmProxy.insert(realm, iterator, cache);
                } else if (clazz.equals(com.knowrecorder.develop.model.realm.YouTubeData.class)) {
                    io.realm.YouTubeDataRealmProxy.insert(realm, iterator, cache);
                } else if (clazz.equals(com.knowrecorder.develop.model.realm.Note.class)) {
                    io.realm.NoteRealmProxy.insert(realm, iterator, cache);
                } else if (clazz.equals(com.knowrecorder.develop.model.realm.Notes.class)) {
                    io.realm.NotesRealmProxy.insert(realm, iterator, cache);
                } else {
                    throw getMissingProxyClassException(clazz);
                }
            }
        }
    }

    @Override
    public void insertOrUpdate(Realm realm, RealmModel obj, Map<RealmModel, Long> cache) {
        // This cast is correct because obj is either
        // generated by RealmProxy or the original type extending directly from RealmObject
        @SuppressWarnings("unchecked") Class<RealmModel> clazz = (Class<RealmModel>) ((obj instanceof RealmObjectProxy) ? obj.getClass().getSuperclass() : obj.getClass());

        if (clazz.equals(com.knowrecorder.develop.model.realm.Page.class)) {
            io.realm.PageRealmProxy.insertOrUpdate(realm, (com.knowrecorder.develop.model.realm.Page) obj, cache);
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.TimeLine.class)) {
            io.realm.TimeLineRealmProxy.insertOrUpdate(realm, (com.knowrecorder.develop.model.realm.TimeLine) obj, cache);
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.PacketObject.class)) {
            io.realm.PacketObjectRealmProxy.insertOrUpdate(realm, (com.knowrecorder.develop.model.realm.PacketObject) obj, cache);
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.YouTubeData.class)) {
            io.realm.YouTubeDataRealmProxy.insertOrUpdate(realm, (com.knowrecorder.develop.model.realm.YouTubeData) obj, cache);
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.Note.class)) {
            io.realm.NoteRealmProxy.insertOrUpdate(realm, (com.knowrecorder.develop.model.realm.Note) obj, cache);
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.Notes.class)) {
            io.realm.NotesRealmProxy.insertOrUpdate(realm, (com.knowrecorder.develop.model.realm.Notes) obj, cache);
        } else {
            throw getMissingProxyClassException(clazz);
        }
    }

    @Override
    public void insertOrUpdate(Realm realm, Collection<? extends RealmModel> objects) {
        Iterator<? extends RealmModel> iterator = objects.iterator();
        RealmModel object = null;
        Map<RealmModel, Long> cache = new HashMap<RealmModel, Long>(objects.size());
        if (iterator.hasNext()) {
            //  access the first element to figure out the clazz for the routing below
            object = iterator.next();
            // This cast is correct because obj is either
            // generated by RealmProxy or the original type extending directly from RealmObject
            @SuppressWarnings("unchecked") Class<RealmModel> clazz = (Class<RealmModel>) ((object instanceof RealmObjectProxy) ? object.getClass().getSuperclass() : object.getClass());

            if (clazz.equals(com.knowrecorder.develop.model.realm.Page.class)) {
                io.realm.PageRealmProxy.insertOrUpdate(realm, (com.knowrecorder.develop.model.realm.Page) object, cache);
            } else if (clazz.equals(com.knowrecorder.develop.model.realm.TimeLine.class)) {
                io.realm.TimeLineRealmProxy.insertOrUpdate(realm, (com.knowrecorder.develop.model.realm.TimeLine) object, cache);
            } else if (clazz.equals(com.knowrecorder.develop.model.realm.PacketObject.class)) {
                io.realm.PacketObjectRealmProxy.insertOrUpdate(realm, (com.knowrecorder.develop.model.realm.PacketObject) object, cache);
            } else if (clazz.equals(com.knowrecorder.develop.model.realm.YouTubeData.class)) {
                io.realm.YouTubeDataRealmProxy.insertOrUpdate(realm, (com.knowrecorder.develop.model.realm.YouTubeData) object, cache);
            } else if (clazz.equals(com.knowrecorder.develop.model.realm.Note.class)) {
                io.realm.NoteRealmProxy.insertOrUpdate(realm, (com.knowrecorder.develop.model.realm.Note) object, cache);
            } else if (clazz.equals(com.knowrecorder.develop.model.realm.Notes.class)) {
                io.realm.NotesRealmProxy.insertOrUpdate(realm, (com.knowrecorder.develop.model.realm.Notes) object, cache);
            } else {
                throw getMissingProxyClassException(clazz);
            }
            if (iterator.hasNext()) {
                if (clazz.equals(com.knowrecorder.develop.model.realm.Page.class)) {
                    io.realm.PageRealmProxy.insertOrUpdate(realm, iterator, cache);
                } else if (clazz.equals(com.knowrecorder.develop.model.realm.TimeLine.class)) {
                    io.realm.TimeLineRealmProxy.insertOrUpdate(realm, iterator, cache);
                } else if (clazz.equals(com.knowrecorder.develop.model.realm.PacketObject.class)) {
                    io.realm.PacketObjectRealmProxy.insertOrUpdate(realm, iterator, cache);
                } else if (clazz.equals(com.knowrecorder.develop.model.realm.YouTubeData.class)) {
                    io.realm.YouTubeDataRealmProxy.insertOrUpdate(realm, iterator, cache);
                } else if (clazz.equals(com.knowrecorder.develop.model.realm.Note.class)) {
                    io.realm.NoteRealmProxy.insertOrUpdate(realm, iterator, cache);
                } else if (clazz.equals(com.knowrecorder.develop.model.realm.Notes.class)) {
                    io.realm.NotesRealmProxy.insertOrUpdate(realm, iterator, cache);
                } else {
                    throw getMissingProxyClassException(clazz);
                }
            }
        }
    }

    @Override
    public <E extends RealmModel> E createOrUpdateUsingJsonObject(Class<E> clazz, Realm realm, JSONObject json, boolean update)
        throws JSONException {
        checkClass(clazz);

        if (clazz.equals(com.knowrecorder.develop.model.realm.Page.class)) {
            return clazz.cast(io.realm.PageRealmProxy.createOrUpdateUsingJsonObject(realm, json, update));
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.TimeLine.class)) {
            return clazz.cast(io.realm.TimeLineRealmProxy.createOrUpdateUsingJsonObject(realm, json, update));
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.PacketObject.class)) {
            return clazz.cast(io.realm.PacketObjectRealmProxy.createOrUpdateUsingJsonObject(realm, json, update));
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.YouTubeData.class)) {
            return clazz.cast(io.realm.YouTubeDataRealmProxy.createOrUpdateUsingJsonObject(realm, json, update));
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.Note.class)) {
            return clazz.cast(io.realm.NoteRealmProxy.createOrUpdateUsingJsonObject(realm, json, update));
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.Notes.class)) {
            return clazz.cast(io.realm.NotesRealmProxy.createOrUpdateUsingJsonObject(realm, json, update));
        } else {
            throw getMissingProxyClassException(clazz);
        }
    }

    @Override
    public <E extends RealmModel> E createUsingJsonStream(Class<E> clazz, Realm realm, JsonReader reader)
        throws IOException {
        checkClass(clazz);

        if (clazz.equals(com.knowrecorder.develop.model.realm.Page.class)) {
            return clazz.cast(io.realm.PageRealmProxy.createUsingJsonStream(realm, reader));
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.TimeLine.class)) {
            return clazz.cast(io.realm.TimeLineRealmProxy.createUsingJsonStream(realm, reader));
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.PacketObject.class)) {
            return clazz.cast(io.realm.PacketObjectRealmProxy.createUsingJsonStream(realm, reader));
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.YouTubeData.class)) {
            return clazz.cast(io.realm.YouTubeDataRealmProxy.createUsingJsonStream(realm, reader));
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.Note.class)) {
            return clazz.cast(io.realm.NoteRealmProxy.createUsingJsonStream(realm, reader));
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.Notes.class)) {
            return clazz.cast(io.realm.NotesRealmProxy.createUsingJsonStream(realm, reader));
        } else {
            throw getMissingProxyClassException(clazz);
        }
    }

    @Override
    public <E extends RealmModel> E createDetachedCopy(E realmObject, int maxDepth, Map<RealmModel, RealmObjectProxy.CacheData<RealmModel>> cache) {
        // This cast is correct because obj is either
        // generated by RealmProxy or the original type extending directly from RealmObject
        @SuppressWarnings("unchecked") Class<E> clazz = (Class<E>) realmObject.getClass().getSuperclass();

        if (clazz.equals(com.knowrecorder.develop.model.realm.Page.class)) {
            return clazz.cast(io.realm.PageRealmProxy.createDetachedCopy((com.knowrecorder.develop.model.realm.Page) realmObject, 0, maxDepth, cache));
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.TimeLine.class)) {
            return clazz.cast(io.realm.TimeLineRealmProxy.createDetachedCopy((com.knowrecorder.develop.model.realm.TimeLine) realmObject, 0, maxDepth, cache));
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.PacketObject.class)) {
            return clazz.cast(io.realm.PacketObjectRealmProxy.createDetachedCopy((com.knowrecorder.develop.model.realm.PacketObject) realmObject, 0, maxDepth, cache));
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.YouTubeData.class)) {
            return clazz.cast(io.realm.YouTubeDataRealmProxy.createDetachedCopy((com.knowrecorder.develop.model.realm.YouTubeData) realmObject, 0, maxDepth, cache));
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.Note.class)) {
            return clazz.cast(io.realm.NoteRealmProxy.createDetachedCopy((com.knowrecorder.develop.model.realm.Note) realmObject, 0, maxDepth, cache));
        } else if (clazz.equals(com.knowrecorder.develop.model.realm.Notes.class)) {
            return clazz.cast(io.realm.NotesRealmProxy.createDetachedCopy((com.knowrecorder.develop.model.realm.Notes) realmObject, 0, maxDepth, cache));
        } else {
            throw getMissingProxyClassException(clazz);
        }
    }

}

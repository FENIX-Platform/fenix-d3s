package org.fao.fenix.d3s.server.tools.orient;

import java.util.*;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

public abstract class OrientDao {
	
	public OGraphDatabase getDatabase(OrientDatabase database) {
		switch (database) {
			case msd: return OrientServer.getMsdDatabase();
			case cacheL1: return OrientServer.getCacheL1Database();
			case cacheL2: return OrientServer.getCacheL2Database();
			default: return null;
		}
	}
	
	protected Collection<OClass> getClassList(OGraphDatabase database) throws Exception {
		return database.getMetadata().getSchema().getClasses();
	}
	
	
	//Utils
	protected int deleteGraph (ODocument vertex, String[] boundary) { return deleteGraph(vertex, Arrays.asList(boundary)); }
	protected int deleteGraph (ODocument vertex, Collection<String> boundary) { return deleteGraph(vertex, boundary!=null ? new HashSet<String>(boundary) : new HashSet<String>(), new TreeSet<ORID>()); }
	@SuppressWarnings("unchecked")
	private final int deleteGraph (ODocument vertex, Set<String> boundary, Set<ORID> deleted) {
		if (vertex==null || deleted.contains(vertex.getIdentity()) || boundary.contains(vertex.getClassName()))
			return 0;

		int count = 1;
		deleted.add(vertex.getIdentity());

		OProperty field = null;
		OClass vertexClass = vertex.getSchemaClass();
		for (String fieldName : vertex.fieldNames()) {
			for (OClass fieldsClass = vertexClass; fieldsClass!=null && (field = fieldsClass.getProperty(fieldName))==null; fieldsClass = fieldsClass.getSuperClass());
			Object fieldValue = vertex.field(fieldName);
			if (field!=null && fieldValue!=null)
				switch (field.getType()) {
					case LINK: 
						count += deleteGraph((ODocument)fieldValue, boundary, deleted);
						break;
					case LINKLIST:
					case LINKSET: 
						for (ODocument child : (Collection<ODocument>)fieldValue)
							count += deleteGraph(child, boundary, deleted);
						break;
					case LINKMAP: 
						for (ODocument child : ((Map<Object,ODocument>)vertex.field(fieldName)).values())
							count += deleteGraph(child, boundary, deleted);
						break;
				}
		}
		
		vertex.delete();
		
		return count;
	}

	public ODocument getDocument (ORID rid) {
        return getDatabase(OrientDatabase.msd).getRecord(rid);
    }
	public static ODocument getDocument (ORID rid, OGraphDatabase database) {
        return database.getRecord(rid);
    }

	public static String toString (ORID rid) {
		return rid.getClusterId()+"_"+rid.getClusterPosition();
	}
	public static ORID toRID(String rid) {
		if (rid!=null) {
			int splitIndex = rid.indexOf('_');
			rid = splitIndex>0 ? '#'+rid.substring(0, splitIndex)+':'+rid.substring(splitIndex+1) : rid;
		}
		return new ORecordId(rid);
	}

    public long countClass (String className, OrientDatabase database) throws Exception {
        return getDatabase(database).countClass(className);
    }
    public Iterable<ODocument> browseClass (String className, OrientDatabase database) throws Exception {
        final OGraphDatabase db = getDatabase(database);
        try {
            final Iterator<ODocument> producerO = db.browseElements(className,true).iterator();
            return new Iterable<ODocument>() {
                @Override
                public Iterator<ODocument> iterator() {
                    return new Iterator<ODocument>() {
                        private boolean open = true;
                        @Override public void remove() { throw new UnsupportedOperationException(); }
                        @Override public boolean hasNext() { return producerO.hasNext(); }

                        @Override
                        public ODocument next() {
                            if (producerO.hasNext())
                                return producerO.next();
                            else
                                if (open) {
                                    db.close();
                                    open = false;
                                }
                            return null;
                        }
                    };
                }
            };

        } catch (Exception ex){
            if (db!=null)
                db.close();
            throw ex;
        }
    }

    public static Date lastUpdate(String key, OGraphDatabase database) {
        List<ODocument> syncDataList = database.query(new OSQLSynchQuery<ODocument>("SELECT FROM Synchro"));
        ODocument syncData = syncDataList.size()>0 ? syncDataList.iterator().next() : null;
        return syncData!=null ? (Date)syncData.field(key) : null;
    }

    public static void lastUpdate(String key, Date value, OGraphDatabase database) {
        List<ODocument> syncDataList =  database.query(new OSQLSynchQuery<ODocument>("SELECT FROM Synchro"));
        ODocument syncData = syncDataList.size()>0 ? syncDataList.iterator().next() : null;
        if (syncData==null)
            syncData = new ODocument("Synchro");

        syncData.field(key,value);
        database.save(syncData);
    }


    public void toMap(Map<String,Object> data, ODocument recordO, Set<String> classes, Integer levels) {
        toMap(data, recordO, 0, classes, levels, new Stack<ORID>());
    }
    private void toMap(Map<String,Object> data, ODocument recordO, int level, Set<String> classes, Integer levels, Stack<ORID> done) {
        if (recordO!=null && !done.contains(recordO.getIdentity()) && (classes==null || classes.isEmpty() || classes.contains(recordO.getClassName())) && (levels==null || level<=levels) ) {

            done.push(recordO.getIdentity());

            for (String fieldName : recordO.fieldNames()) {
                OType fieldType = recordO.fieldType(fieldName);
                if (fieldType==null)
                    fieldType = recordO.getSchemaClass().getProperty(fieldName).getType();
                if (fieldType==null)
                    fieldType = OType.BINARY;

                switch (fieldType) {
                    case LINK:
                        ODocument childO = recordO.field(fieldName);
                        if (childO!=null) {
                            Map<String,Object> record = new HashMap<>();
                            toMap(record, childO, level+1, classes, levels, done);
                            if (record.size()>0)
                                data.put(fieldName, record);
                        }
                        break;
                    case LINKLIST:
                        List<ODocument> childrenListO = recordO.field(fieldName);
                        if (childrenListO!=null) {
                            List<Map<String,Object>> records = new LinkedList<>();
                            for (ODocument cO : childrenListO) {
                                Map<String,Object> record = new HashMap<>();
                                toMap(record, cO, level+1, classes, levels, done);
                                if (record.size()>0)
                                    records.add(record);
                            }
                            if (records.size()>0)
                                data.put(fieldName, records);
                        }
                        break;
                    case LINKSET:
                        Set<ODocument> childrenSetO = recordO.field(fieldName);
                        if (childrenSetO!=null) {
                            Set<Map<String,Object>> records = new HashSet<>();
                            for (ODocument cO : childrenSetO) {
                                Map<String,Object> record = new HashMap<>();
                                toMap(record, cO, level+1, classes, levels, done);
                                if (record.size()>0)
                                    records.add(record);
                            }
                            if (records.size()>0)
                                data.put(fieldName, records);
                        }
                        break;
                    case LINKMAP:
                        Map<String, ODocument> childrenMapO = recordO.field(fieldName);
                        if (childrenMapO!=null) {
                            Map<String,Map<String,Object>> records = new HashMap<>();
                            for (Map.Entry<String, ODocument> ceO : childrenMapO.entrySet()) {
                                Map<String,Object> record = new HashMap<>();
                                toMap(record, ceO.getValue(), level+1, classes, levels, done);
                                if (record.size()>0)
                                    records.put(ceO.getKey(), record);
                            }
                            if (records.size()>0)
                                data.put(fieldName, records);
                        }
                        break;
                    default:
                        data.put(fieldName,recordO.field(fieldName));
                }
            }

            done.pop();
        }
    }


}
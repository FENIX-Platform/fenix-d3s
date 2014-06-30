package org.fao.fenix.d3s.server.tools.orient;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;

import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.exception.OSerializationException;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.fao.fenix.commons.msd.dto.JSONdto;

import javax.inject.Inject;
import javax.persistence.Embedded;
import javax.ws.rs.core.NoContentException;

public abstract class OrientDao {
    @Inject private DatabaseStandards dbParameters;
    @Inject private OrientServer client;

    protected <T extends ODatabase> T getConnection() {
        return dbParameters.getConnection();
    }



	//DELETE UTILS

    protected int deleteGraphInclude (ODocument document, String[] boundary) { return deleteGraph(document, Arrays.asList(boundary)); }
	protected int deleteGraphInclude (ODocument document, Collection<String> boundary) { return deleteGraph(document, boundary!=null ? new HashSet<>(boundary) : new HashSet<String>(), new TreeSet<ORID>(),true); }
	protected int deleteGraph (ODocument document, String[] boundary) { return deleteGraph(document, Arrays.asList(boundary)); }
	protected int deleteGraph (ODocument document, Collection<String> boundary) { return deleteGraph(document, boundary!=null ? new HashSet<>(boundary) : new HashSet<String>(), new TreeSet<ORID>(),false); }
	@SuppressWarnings("unchecked")
	private final int deleteGraph (ODocument document, Set<String> boundary, Set<ORID> deleted, boolean include) {
		if (document==null || deleted.contains(document.getIdentity()) || (include && !boundary.contains(document.getClassName())) || (!include && boundary.contains(document.getClassName())))
			return 0;

		int count = 1;
		deleted.add(document.getIdentity());

		OProperty field = null;
		OClass vertexClass = document.getSchemaClass();
		for (String fieldName : document.fieldNames()) {
			for (OClass fieldsClass = vertexClass; fieldsClass!=null && (field = fieldsClass.getProperty(fieldName))==null; fieldsClass = fieldsClass.getSuperClass());
			Object fieldValue = document.field(fieldName);
			if (field!=null && fieldValue!=null)
				switch (field.getType()) {
					case LINK: 
						count += deleteGraph((ODocument)fieldValue, boundary, deleted,include);
						break;
					case LINKLIST:
					case LINKSET: 
						for (ODocument child : (Collection<ODocument>)fieldValue)
							count += deleteGraph(child, boundary, deleted,include);
						break;
					case LINKMAP: 
						for (ODocument child : ((Map<Object,ODocument>)document.field(fieldName)).values())
							count += deleteGraph(child, boundary, deleted,include);
						break;
				}
		}
		
		document.delete();
		
		return count;
	}

    //LOAD UTILS
    private static Map<String,OSQLSynchQuery> queries = new TreeMap<>();

    private <T> OSQLSynchQuery<T> getSelect(String query, Class<T> type) {
        query += dbParameters.getOrderingInfo().toSQL();
        query += dbParameters.getPaginationInfo().toSQL();

        OSQLSynchQuery queryO = queries.get(type.getSimpleName()+query);
        if (queryO==null)
            queries.put(type.getSimpleName()+query, queryO = new OSQLSynchQuery<>(query));

        queryO.reset();
        queryO.resetPagination();

        return queryO;
    }

    public synchronized Collection<ODocument> select(String query, Object... params) throws Exception {
        return ((ODatabaseDocumentTx)getConnection()).query(getSelect(query,ODocument.class), params);
    }
    public synchronized <T> Collection<T> select(Class<T> type, String query, Object... params) throws Exception {
        try {
            return (Collection<T>) ((OObjectDatabaseTx)getConnection()).query(getSelect(query,type), params);
        } catch (OSerializationException ex) {
            client.registerPersistentEntities();
            return select(type,query,params);
        }
    }

    public ODocument load (String rid) throws Exception {
        return load(JSONdto.toRID(rid));
    }
    public ODocument load (ORID orid) throws Exception {
        return ((ODatabaseDocumentTx)getConnection()).load(orid);
    }

    public <T> Iterator<T> browse(Class<T> type) throws Exception {
        return ((OObjectDatabaseTx)getConnection()).browseClass(type);
    }
    public Iterable<ODocument> browse (String className) throws Exception {
        final Iterator<ODocument> producerO = ((ODatabaseDocumentTx)dbParameters.getConnection()).browseClass(className).iterator();
        return new Iterable<ODocument>() {
            @Override
            public Iterator<ODocument> iterator() {
                return new Iterator<ODocument>() {
                    @Override public void remove() { throw new UnsupportedOperationException(); }
                    @Override public boolean hasNext() { return producerO.hasNext(); }

                    @Override
                    public ODocument next() {
                        return producerO.hasNext() ? producerO.next() : null;
                    }
                };
            }
        };
    }
    public long count (String className) throws Exception {
        return ((ODatabaseDocumentTx)dbParameters.getConnection()).countClass(className);
    }


    //SAVE UTILS

    class MethodGetSet {
        Method get, set;
        MethodGetSet(Method get, Method set) {
            this.get = get;
            this.set = set;
        }
    }
    private static final Set<Class> entityClass = new HashSet<>();
    private static final Map<Class,Collection<MethodGetSet>> standardGetSet = new HashMap<>();
    private static final Map<Class,Collection<MethodGetSet>> entityGetSet = new HashMap<>();
    private static final Map<Class,Collection<MethodGetSet>> entityCollectionGetSet = new HashMap<>();


    public <T extends JSONdto> T newCustomEntity(T bean, boolean ... checks) {
        boolean cycleCheck = checks==null || checks.length==0 || checks[0]; //true by default
        try {
            bean.setRID(null); //Ignore bean ORID
            return saveCustomEntity(bean, false, cycleCheck); //Save in append mode for connected entities
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public <T extends JSONdto> T saveCustomEntity(T bean, boolean ... checks) throws Exception {
        boolean overwrite = checks!=null && checks.length>0 && checks[0]; //false by default
        boolean cycleCheck = checks==null || checks.length<2 || checks[1]; //true by default

        OObjectDatabaseTx connection = null;
        try {
            connection = getConnection();
            connection.begin();
            bean = saveCustomEntity(bean, overwrite, cycleCheck ? new HashMap<>() : null, (OObjectDatabaseTx)getConnection());
            connection.commit();
            return bean;
        } catch (OSerializationException e) {
            if (connection!=null)
                connection.rollback();
            client.registerPersistentEntities();
            return saveCustomEntity(bean, overwrite);
        } catch (Exception e) {
            if (connection!=null)
                connection.rollback();
            throw e;
        } finally {
            if (connection!=null)
                connection.close();
        }
    }
    private <T extends JSONdto> T saveCustomEntity(T bean, boolean overwrite, Map<Object,Object> buffer, OObjectDatabaseTx connection) throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException, NoContentException {
        //Avoid cycle and useless proxy create/load
        if (bean==null)
            return null;
        if (buffer!=null && buffer.containsKey(bean))
            return (T)buffer.get(bean);

        //Init recursion information about this entity
        Class<T> beanClass = (Class<T>) bean.getClass();
        if (!entityClass.contains(beanClass))
            initEntityRecursionInformation(beanClass);

        //Load/create proxy bean
        ORID orid = bean.getORID();
        T beanProxy = orid!=null ? (T)connection.load(orid) : connection.newInstance(beanClass);
        if (beanProxy==null)
            throw new NoContentException("Cannot find bean '"+bean.getRID()+'\'');
        if (buffer!=null)
            buffer.put(bean,beanProxy);

        //Retrieve fields value and apply recursion
        boolean empty = true;
        Set<Method> nullFields = new HashSet<>();

        Collection<? extends JSONdto> collectionFieldValue;
        Object fieldValue;

        for (MethodGetSet methodGetSet : entityCollectionGetSet.get(beanClass))
            if ((collectionFieldValue = (Collection) methodGetSet.get.invoke(bean))!=null && collectionFieldValue.size()>0) {
                //Collect new proxy entities
                empty = false;
                Collection<JSONdto> proxyCollectionFieldValue = new HashSet<>();
                for (JSONdto elementValue : collectionFieldValue)
                    proxyCollectionFieldValue.add(saveCustomEntity(elementValue, overwrite, buffer, connection));
                //In append mode add old proxy entities (duplicates are avoided by default by Java HashSet)
                if (!overwrite) {
                    Collection<? extends JSONdto> existingProxyCollectionFieldValue = (Collection)methodGetSet.get.invoke(beanProxy);
                    if (existingProxyCollectionFieldValue!=null && existingProxyCollectionFieldValue.size()>0)
                        for (Object existingValue : existingProxyCollectionFieldValue)
                            if (existingValue!=null) //Avoid removed links
                                proxyCollectionFieldValue.add((JSONdto) existingValue);
                }
                //Set new value
                methodGetSet.set.invoke(beanProxy,proxyCollectionFieldValue);
            } else if (overwrite) //In overwrite mode maintain nullable fields for the last step
                nullFields.add(methodGetSet.set);

        for (MethodGetSet methodGetSet : entityGetSet.get(beanClass))
            if ((fieldValue=methodGetSet.get.invoke(bean)) != null) {
                empty = false;
                methodGetSet.set.invoke(beanProxy, saveCustomEntity((JSONdto) fieldValue, overwrite, buffer, connection));
            } else if (overwrite)
                nullFields.add(methodGetSet.set);

        for (MethodGetSet methodGetSet : standardGetSet.get(beanClass))
            if ((fieldValue=methodGetSet.get.invoke(bean)) != null) {
                empty = false;
                methodGetSet.set.invoke(beanProxy, fieldValue);
            } else if (overwrite)
                nullFields.add(methodGetSet.set);

        //Set null field values of non empty bean if in overwrite mode
        if (overwrite && !empty)
            for (Method set : nullFields)
                set.invoke(beanProxy,new Object[] {null});

        //Return updated proxy bean
        return connection.save(beanProxy);
    }

    private synchronized <T extends JSONdto> void initEntityRecursionInformation (Class<T> beanClass) throws NoSuchMethodException {
        Collection<MethodGetSet> standardGetSetCollection = new LinkedList<>();
        Collection<MethodGetSet> entityGetSetCollection = new LinkedList<>();
        Collection<MethodGetSet> entityCollectionGetSetCollection = new LinkedList<>();
        standardGetSet.put(beanClass,standardGetSetCollection);
        entityGetSet.put(beanClass,entityGetSetCollection);
        entityCollectionGetSet.put(beanClass,entityCollectionGetSetCollection);

        for (Class<? extends JSONdto> c = beanClass; !c.equals(JSONdto.class); c = (Class<? extends JSONdto>) c.getSuperclass())
            for (Method getter : c.getDeclaredMethods())
                if (getter.getName().startsWith("get") && !getter.getName().equals("getRID") && !getter.getName().equals("getORID")) {
                    Class returnClass = getter.getReturnType();
                    MethodGetSet getSet = new MethodGetSet(getter, beanClass.getMethod('s'+getter.getName().substring(1),returnClass));
                    boolean embedded = getSet.set.isAnnotationPresent(Embedded.class);
                    if (!embedded && Collection.class.isAssignableFrom(returnClass)) {
                        Class elementClass = (Class) ((ParameterizedType) getter.getGenericReturnType()).getActualTypeArguments()[0];
                        if (JSONdto.class.isAssignableFrom(elementClass))
                            entityCollectionGetSetCollection.add(getSet);
                        else
                            standardGetSetCollection.add(getSet);
                    } else if (!embedded && JSONdto.class.isAssignableFrom(returnClass))
                        entityGetSetCollection.add(getSet);
                    else
                        standardGetSetCollection.add(getSet);
                }
        entityClass.add(beanClass);
    }



    //DOCUMENT DATABASE UTILS

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

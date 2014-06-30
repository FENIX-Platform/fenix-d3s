package org.fao.fenix.d3s.wds;

import java.util.*;

import org.fao.fenix.commons.msd.dto.templates.canc.cl.Code;
import org.fao.fenix.commons.msd.dto.templates.canc.cl.CodeSystem;
import org.fao.fenix.commons.msd.dto.templates.canc.dm.DM;
import org.fao.fenix.commons.msd.dto.type.dm.DMCopyrightType;
import org.fao.fenix.commons.msd.dto.type.dm.DMDataKind;
import org.fao.fenix.commons.msd.dto.type.dm.DMDataType;
import org.fao.fenix.commons.msd.dto.templates.canc.dsd.DSD;
import org.fao.fenix.commons.msd.dto.templates.canc.dsd.DSDColumn;
import org.fao.fenix.commons.msd.dto.templates.canc.dsd.DSDContextSystem;
import org.fao.fenix.commons.msd.dto.templates.canc.dsd.DSDDimension;
import org.fao.fenix.commons.msd.dto.type.dsd.DSDDataType;
import org.fao.fenix.commons.search.dto.filter.ResourceFilter;
import org.fao.fenix.d3s.search.SearchStep;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.fao.fenix.d3s.wds.impl.countryStat.CountryStatDAO;

public abstract class Dao extends SearchStep {
    private static Map<String,Class<? extends Dao>> daoMapping = new HashMap<>();
    static {
        daoMapping.put("CountrySTAT", CountryStatDAO.class);
    }

    public static Class<? extends Dao> getDaoClass(String daoName) {
        return daoMapping.get(daoName);
    }


	//Init
    protected Map<String, String> properties;

    public void init(Map<String,String> properties) {
        this.properties = properties;
    }
    //Interface
	public abstract void load(ResourceFilter filter, ODocument dataset) throws Exception;
	public abstract void store(Iterable<Object[]> data, ODocument dataset) throws Exception;


    //Utils
    public DM createMetadata (ODocument dataset, Collection<ODocument> columnsO) {
        Date date = new Date();

        DSDContextSystem cs = new DSDContextSystem();
        cs.setName("cstat");

        DSD dsd = new DSD();
        dsd.setContextSystem(cs);
        dsd.setStartDate(date);
        dsd.setEndDate(date);

        Collection<DSDColumn> columns = new LinkedList<DSDColumn>();
        for (ODocument columnO : columnsO)
			columns.add(createColumnMetadata(columnO));
        dsd.setColumns(columns);

        DM dm = new DM();
        dm.setDataKind(DMDataKind.automated);
        dm.setCopyright(DMCopyrightType.privatePolicy);
        dm.setDataType(DMDataType.getByCode((String)dataset.field("datatype")));
        dm.setUid((String)dataset.field("uid"));
        dm.setCreationDate(date);
        dm.setDsd(dsd);

        return dm;
    }
    
    @SuppressWarnings("unchecked")
	private DSDColumn createColumnMetadata (ODocument columnO) {
    	String dimensionName = ((ODocument)columnO.field("dimension")).field("name");

    	DSDColumn column = new DSDColumn();
		column.setColumnId(dimensionName);
		column.setTitle((Map<String,String>)columnO.field("title",Map.class));
		column.setCodesLevel((Integer)columnO.field("codesLevel"));
		column.setDataType(DSDDataType.getByCode((String)columnO.field("datatype")));
		column.setGeoLyer((String)columnO.field("geoLayer"));
		column.setVirtualColumn((String)columnO.field("virtualColumn"));
		//Values only if single value
		Collection<Object> values = columnO.field("values");
		if (values!=null && values.size()==1) {
			Object value = values.iterator().next();
			if (value instanceof ORID && DSDDataType.code==column.getDataType()) {
				value = getConnection().load((ORID)value);
				value = new Code((String)((ODocument)value).field("system.system"),(String)((ODocument)value).field("system.version"),(String)((ODocument)value).field("code"));
			}
			column.addValue(value);
		}
		//Connected elements
		column.setDimension(new DSDDimension(dimensionName));
		ODocument codeSystemO = (ODocument)columnO.field("codeSystem");
		if (codeSystemO!=null)
			column.setCodeSystem(new CodeSystem((String)codeSystemO.field("system"),(String)codeSystemO.field("version")));
		//Return column
		return column;
    }

    

}

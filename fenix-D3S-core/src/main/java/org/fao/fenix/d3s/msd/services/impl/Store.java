package org.fao.fenix.d3s.msd.services.impl;

import java.util.Collection;

import com.orientechnologies.orient.core.record.impl.ODocument;
import org.fao.fenix.d3s.msd.dao.canc.cl.CodeListIndex;
import org.fao.fenix.d3s.msd.dao.canc.cl.CodeListLinkStore;
import org.fao.fenix.d3s.msd.dao.canc.cl.CodeListStore;
import org.fao.fenix.d3s.msd.dao.canc.common.CommonsStore;
import org.fao.fenix.d3s.msd.dao.canc.dm.DMIndexStore;
import org.fao.fenix.d3s.msd.dao.canc.dm.DMStore;
import org.fao.fenix.d3s.msd.dao.canc.dsd.DSDStore;
import org.fao.fenix.commons.msd.dto.templates.canc.cl.Code;
import org.fao.fenix.commons.msd.dto.templates.canc.cl.CodeConversion;
import org.fao.fenix.commons.msd.dto.templates.canc.cl.CodePropaedeutic;
import org.fao.fenix.commons.msd.dto.templates.canc.cl.CodeRelationship;
import org.fao.fenix.commons.msd.dto.templates.canc.cl.CodeSystem;
import org.fao.fenix.commons.msd.dto.templates.canc.common.ContactIdentity;
import org.fao.fenix.commons.msd.dto.templates.canc.common.Publication;
import org.fao.fenix.commons.msd.dto.templates.canc.dm.DM;
import org.fao.fenix.commons.msd.dto.templates.canc.dm.DMMeta;
import org.fao.fenix.commons.msd.dto.templates.canc.dsd.DSDColumn;
import org.fao.fenix.commons.msd.dto.templates.canc.dsd.DSDContextSystem;
import org.fao.fenix.commons.msd.dto.templates.canc.dsd.DSDDimension;
import org.fao.fenix.d3s.server.tools.orient.OrientDao;

import javax.inject.Inject;

public class Store {
	@Inject private CommonsStore cmStoreDAO;
	@Inject private DSDStore dsdStoreDAO;
    @Inject private DMStore dmStoreDAO;
    @Inject private DMIndexStore dmIndexStoreDAO;
	@Inject private CodeListStore clStoreDAO;
    @Inject private CodeListIndex clIndexStoreDAO;
	@Inject private CodeListLinkStore clLinkStoreDAO;

	// STORE
	public String newContactIdentity(ContactIdentity contactIdentity) throws Exception {
		return OrientDao.toString(cmStoreDAO.storeContactIdentity(contactIdentity).getIdentity());
	}

	public String newPublication(Publication publication) throws Exception {
		return OrientDao.toString(cmStoreDAO.storePublication(publication).getIdentity());
	}

	public String newCodeList(CodeSystem cl) throws Exception {
        clStoreDAO.storeCodeList(cl.normalize());
        return cl.toString();
	}

    public String newDatasetMetadata(DM dm) throws Exception {
        ODocument datasetO = dmStoreDAO.storeDatasetMetadata(dm);
        return datasetO!=null ? OrientDao.toString(datasetO.getIdentity()) : null;
    }

    public String newMetadataStructure(DMMeta mm) throws Exception {
        ODocument datasetO = dmStoreDAO.storeMetadataStructure(mm);
        return datasetO!=null ? OrientDao.toString(datasetO.getIdentity()) : null;
    }

    public void newKeyword(String keyword) throws Exception {
		clStoreDAO.storeKeyword(keyword);
	}

	public void newDimension(DSDDimension dimension) throws Exception {
		dsdStoreDAO.storeDimension(dimension);
	}

	public void newContextSystem(DSDContextSystem context) throws Exception {
		cmStoreDAO.storeContext(context);
	}

	public void newRelationship(Collection<CodeRelationship> relations) throws Exception {
		clLinkStoreDAO.storeCodeRelationship(relations);
	}

	public void newRelationship(CodeRelationship relation) throws Exception {
		clLinkStoreDAO.storeCodeRelationship(relation);
	}

	public void newConversion(Collection<CodeConversion> conversions)
			throws Exception {
		clLinkStoreDAO.storeCodeConversion(conversions);
	}

	public void newConversion(CodeConversion conversion) throws Exception {
		clLinkStoreDAO.storeCodeConversion(conversion);
	}

	public void newPropaedeutic(Collection<CodePropaedeutic> propaedeutics) throws Exception {
		clLinkStoreDAO.storeCodePropaedeutic(propaedeutics);
	}

	public void newPropaedeutic(CodePropaedeutic propaedeutic) throws Exception {
		clLinkStoreDAO.storeCodePropaedeutic(propaedeutic);
	}

	// UPDATE
	public int updateContactIdentity(ContactIdentity contactIdentity, boolean append) throws Exception {
		return cmStoreDAO.updateContactIdentity(contactIdentity, append);
	}

	public int updateCodeList(CodeSystem cl, boolean append, boolean all) throws Exception {
		return clStoreDAO.updateCodeList(cl, append) + (all ? updateCodeListCodes(cl,append) : 0);
	}
	public int updateCodeListCodes(CodeSystem cl, boolean append) throws Exception {
        return clStoreDAO.updateCodes(cl.normalize(), append);
	}

	public int updateCode(Code code) throws Exception {
        ODocument clO = clStoreDAO.updateCode(code);
		return clO!=null ? 1 : 0;
	}

    public int updateDatasetMetadata(DM dm, boolean append) throws Exception {
        return dmStoreDAO.updateDatasetMetadata(dm, append);
    }

    public int updateMetadataStructure(DMMeta mm, boolean append) throws Exception {
        return dmStoreDAO.updateMetadataStructure(mm, append);
    }

	public int updateColumn(String datasetUID, DSDColumn column)
			throws Exception {
		return dsdStoreDAO.updateColumn(datasetUID, column);
	}

	public int updateDimension(DSDDimension dimension) throws Exception {
		return dsdStoreDAO.updateDimension(dimension);
	}

	public int updateConversion(CodeConversion conversion) throws Exception {
		return clLinkStoreDAO.updateCodeConversion(conversion);
	}

	public int addCategoriesToDataset(String datasetUID,
			Collection<Code> listOfCodes) throws Exception {
		return dmStoreDAO.addCategoriesToDataset(datasetUID, listOfCodes);
	}

    //INDEX
    public int codeListIndex(String system, String version) throws Exception {
        return clIndexStoreDAO.rebuildIndex(new CodeSystem(system, version));
    }

    public int datasetIndex(String uid) throws Exception {
        if (uid!=null)
            return dmIndexStoreDAO.indexDatasetMetadata(uid);
        dmIndexStoreDAO.rebuildIndexes();
        return 1;
    }

}

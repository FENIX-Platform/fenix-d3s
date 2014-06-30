package org.fao.fenix.d3s.msd.services.rest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.NoContentException;

import org.fao.fenix.commons.msd.dto.templates.canc.common.ContactIdentity;
import org.fao.fenix.commons.msd.dto.templates.canc.common.Publication;
import org.fao.fenix.d3s.msd.services.impl.Store;
import org.fao.fenix.d3s.msd.services.impl.Delete;

@Path("msd/cm")
public class StoreCommons implements org.fao.fenix.d3s.msd.services.spi.StoreCommons {
    @Context HttpServletRequest request;
    @Inject private Store store;
    @Inject private Delete delete;

	@Override
	public String newContactIdentity(ContactIdentity contactIdentity) throws Exception {
		return store.newContactIdentity(contactIdentity);
	}
	@Override
	public void updateContactIdentity(ContactIdentity contactIdentity) throws Exception {
        if (store.updateContactIdentity(contactIdentity, false)<=0)
            throw new NoContentException("");
	}
	@Override
	public void appendContactIdentity(ContactIdentity contactIdentity) throws Exception {
        if (store.updateContactIdentity(contactIdentity, true)<=0)
            throw new NoContentException("");
	}
	@Override
	public void deleteContactIdentity(String contactID) throws Exception {
        if (delete.deleteContactIdentity(contactID)<=0)
            throw new NoContentException("");
	}

    @Override
    public String newPublication(Publication publication) throws Exception {
        return store.newPublication(publication);
    }

}

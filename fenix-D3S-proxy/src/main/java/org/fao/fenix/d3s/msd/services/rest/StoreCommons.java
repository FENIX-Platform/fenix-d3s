package org.fao.fenix.d3s.msd.services.rest;

import org.fao.fenix.commons.msd.dto.templates.canc.common.ContactIdentity;
import org.fao.fenix.commons.msd.dto.templates.canc.common.Publication;
import org.fao.fenix.d3s.server.services.rest.Service;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

@Path("msd/cm")
public class StoreCommons extends Service implements org.fao.fenix.d3s.msd.services.spi.canc.StoreCommons {
    @Context HttpServletRequest request;

    @Override
    public String newContactIdentity(ContactIdentity contactIdentity) throws Exception {
        return getProxy(org.fao.fenix.d3s.msd.services.spi.canc.StoreCommons.class).newContactIdentity(contactIdentity);
    }

    @Override
    public void updateContactIdentity(ContactIdentity contactIdentity) throws Exception {
        getProxy(org.fao.fenix.d3s.msd.services.spi.canc.StoreCommons.class).updateContactIdentity(contactIdentity);
    }

    @Override
    public void appendContactIdentity(ContactIdentity contactIdentity) throws Exception {
        getProxy(org.fao.fenix.d3s.msd.services.spi.canc.StoreCommons.class).appendContactIdentity(contactIdentity);
    }

    @Override
    public void deleteContactIdentity(String contactID) throws Exception {
        getProxy(org.fao.fenix.d3s.msd.services.spi.canc.StoreCommons.class).deleteContactIdentity(contactID);
    }

    @Override
    public String newPublication(Publication publication) throws Exception {
        return getProxy(org.fao.fenix.d3s.msd.services.spi.canc.StoreCommons.class).newPublication(publication);
    }
}

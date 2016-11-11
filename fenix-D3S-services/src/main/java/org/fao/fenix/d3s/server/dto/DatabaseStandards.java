package org.fao.fenix.d3s.server.dto;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.fao.fenix.commons.utils.Language;
import org.fao.fenix.commons.utils.Order;
import org.fao.fenix.commons.utils.Page;

import javax.servlet.http.HttpServletRequest;

public class DatabaseStandards {
    public static ThreadLocal<HttpServletRequest> request = new ThreadLocal<>();
    public static ThreadLocal<OObjectDatabaseTx> connection = new ThreadLocal<>();
    public static ThreadLocal<Page> paginationInfo = new ThreadLocal<>();
    public static ThreadLocal<Order> orderingInfo = new ThreadLocal<>();
    public static ThreadLocal<Language[]> languageInfo = new ThreadLocal<>();
    public static ThreadLocal<Integer> limit = new ThreadLocal<>();


    public OObjectDatabaseTx getConnection() {
        return connection.get();
    }

    public void setConnection(OObjectDatabaseTx c) {
        connection.set(c);
    }

    public Page getPaginationInfo() {
        return paginationInfo.get();
    }

    public void setPaginationInfo(Page p) {
        paginationInfo.set(p);
    }

    public Order getOrderingInfo() {
        return orderingInfo.get();
    }

    public void setOrderingInfo(Order o) {
        orderingInfo.set(o);
    }

    public HttpServletRequest getRequest() {
        return request.get();
    }

    public void setRequest(HttpServletRequest r) {
        request.set(r);
    }

    public Integer getLimit() {
        return limit.get();
    }

    public void setLimit(String limit) {
        try {
            this.limit.set(limit != null ? new Integer(limit) : null);
        } catch (NumberFormatException ex) {}
    }

    public static Language[] getLanguageInfo() {
        return languageInfo.get();
    }

    public static void setLanguageInfo(Language[] l) {
        languageInfo.set(l);
    }


    //Utils
    public void clone(DatabaseStandards clone) {
        clone.setConnection(getConnection());
        clone.setLimit(getLimit()!=null?String.valueOf(getLimit()):null);
        clone.setOrderingInfo(getOrderingInfo());
        clone.setPaginationInfo(getPaginationInfo());
        clone.setRequest(getRequest());
    }

}

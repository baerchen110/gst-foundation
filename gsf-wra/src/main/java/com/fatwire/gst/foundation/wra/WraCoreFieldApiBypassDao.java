/*
 * Copyright 2010 FatWire Corporation. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fatwire.gst.foundation.wra;

import COM.FutureTense.Interfaces.ICS;
import com.fatwire.assetapi.data.AssetData;
import com.fatwire.assetapi.data.AssetId;
import com.fatwire.cs.core.db.PreparedStmt;
import com.fatwire.cs.core.db.StatementParam;
import com.fatwire.gst.foundation.facade.assetapi.DirectSqlAccessTools;
import com.fatwire.gst.foundation.facade.ics.ICSFactory;
import com.fatwire.gst.foundation.facade.sql.Row;
import com.fatwire.gst.foundation.facade.sql.SqlHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collections;

/**
 * Backdoor implementation of WraCoreFieldDao that does not utilize
 * any Asset APIs.  This class should be used sparingly and may result
 * in some dependencies, that would ordinarily be recorded, being skipped.
 * <p/>
 * User: Tony Field
 * Date: 2011-05-06
 */
public class WraCoreFieldApiBypassDao extends WraCoreFieldDao {
    public static WraCoreFieldApiBypassDao getBackdoorInstance(ICS ics) {
        if (ics == null) {
            ics = ICSFactory.getOrCreateICS();
        }

        Object o = ics.GetObj(WraCoreFieldApiBypassDao.class.getName());
        if (o == null) {
            o = new WraCoreFieldApiBypassDao(ics);
            ics.SetObj(WraCoreFieldApiBypassDao.class.getName(), o);
        }
        return (WraCoreFieldApiBypassDao) o;
    }

    private final ICS ics;
    private final DirectSqlAccessTools directSqlAccessTools;

    private WraCoreFieldApiBypassDao(ICS ics) {
        this.ics = ics;
        directSqlAccessTools = new DirectSqlAccessTools(ics);
    }

    private static final Log LOG = LogFactory.getLog(WraCoreFieldApiBypassDao.class);

    /**
     * @throws UnsupportedOperationException - not possible in this implementation
     */
    public AssetData getAsAssetData(AssetId id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Method to test whether or not an asset is web-referenceable. todo: low
     * priority: optimize as this will be called at runtime (assest api incache
     * will mitigate the performance issue)
     *
     * @param id asset ID to check
     * @return true if the asset is a valid web-referenceable asset, false if it
     *         is not
     */
    public boolean isWebReferenceable(AssetId id) {
        try {
            WebReferenceableAsset wra = getWra(id);
            boolean b = StringUtils.isNotBlank(wra.getPath());
            if (LOG.isTraceEnabled())
                LOG.trace("Asset " + id + (b ? " is " : " is not ") + "web-referenceable, as determinted by the presence of a path attribute.");
            return b;
        } catch (RuntimeException e) {
            if (LOG.isTraceEnabled()) LOG.trace("Asset " + id + " is not web-referenceable: " + e, e);
            return false;
        }
    }

    /**
     * @throws UnsupportedOperationException - not possible in this implementation
     */
    public boolean isWebReferenceable(WebReferenceableAsset wra) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * @throws UnsupportedOperationException - not possible in this implementation
     */
    public boolean hasPathAttribute(AssetData data) {
        throw new UnsupportedOperationException("Not implemented");

    }

    /**
     * Return a web referenceable asset bean given an input id. Required fields
     * must be set or an exception is thrown.
     *
     * @param id asset id
     * @return WebReferenceableAsset, never null
     * @see #isWebReferenceable(AssetId)
     */
    public WebReferenceableAsset getWra(AssetId id) {
        if (directSqlAccessTools.isFlex(id)) {
            // todo: medium: optimize as this is very inefficient for flex assets
            PreparedStmt basicFields = new PreparedStmt("SELECT id,name,description,subtype,status,path,template,startdate,enddate" +
                    " FROM " + id.getType() +
                    " WHERE id = ?", Collections.singletonList(id.getType()));
            basicFields.setElement(0, id.getType(), "id");

            StatementParam param = basicFields.newParam();
            param.setLong(0, id.getId());
            Row row = SqlHelper.selectSingle(ics, basicFields, param);

            WraBeanImpl wra = new WraBeanImpl();
            wra.setId(id);
            wra.setName(row.getString("name"));
            wra.setDescription(row.getString("description"));
            wra.setSubtype(row.getString("subtype"));
            wra.setPath(row.getString("path"));
            wra.setTemplate(row.getString("template"));
            if (StringUtils.isNotBlank(row.getString("startdate")))
                wra.setStartDate(row.getDate("startdate"));
            if (StringUtils.isNotBlank(row.getString("enddate")))
                wra.setEndDate(row.getDate("enddate"));

            wra.setMetaTitle(directSqlAccessTools.getFlexAttributeValue(id, "metatitle"));
            wra.setMetaDescription(directSqlAccessTools.getFlexAttributeValue(id, "metadescription"));
            wra.setMetaKeyword(directSqlAccessTools.getFlexAttributeValue(id, "metakeywords"));
            wra.setH1Title(directSqlAccessTools.getFlexAttributeValue(id, "h1title"));
            wra.setLinkText(directSqlAccessTools.getFlexAttributeValue(id, "linktext"));

            return wra;
        } else {
            PreparedStmt basicFields = new PreparedStmt("SELECT id,name,description,subtype,status,path,template,startdate,enddate," +
                    "metatitle,metadescription,metakeyword,h1title,linktext" +
                    " FROM " + id.getType() +
                    " WHERE id = ?", Collections.singletonList(id.getType()));
            basicFields.setElement(0, id.getType(), "id");

            StatementParam param = basicFields.newParam();
            param.setLong(0, id.getId());
            Row row = SqlHelper.selectSingle(ics, basicFields, param);

            WraBeanImpl wra = new WraBeanImpl();
            wra.setId(id);
            wra.setName(row.getString("name"));
            wra.setDescription(row.getString("description"));
            wra.setSubtype(row.getString("subtype"));
            wra.setMetaTitle(row.getString("metatitle"));
            wra.setMetaDescription(row.getString("metadescription"));
            wra.setMetaKeyword(row.getString("metakeywords"));
            wra.setH1Title(row.getString("h1title"));
            wra.setLinkText(row.getString("linktext"));
            wra.setPath(row.getString("path"));
            wra.setTemplate(row.getString("template"));
            if (StringUtils.isNotBlank(row.getString("startdate")))
                wra.setStartDate(row.getDate("startdate"));
            if (StringUtils.isNotBlank(row.getString("enddate")))
                wra.setEndDate(row.getDate("enddate"));
            return wra;
        }
    }

    /**
     * @throws UnsupportedOperationException - not possible in this implementation
     */
    public WebReferenceableAsset getWra(AssetData data) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
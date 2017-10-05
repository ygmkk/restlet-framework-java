/**
 * Copyright 2005-2017 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.ext.odata.deepexpand.model;

import java.util.Date;

/**
 * Generated by the generator tool for the OData extension for the Restlet
 * framework.<br>
 * 
 * @see <a
 *      href="http://praktiki.metal.ntua.gr/CoopOData/CoopOData.svc/$metadata">Metadata
 *      of the target OData service</a>
 * 
 */
public class Tracking {

    private Date created;

    private Date modified;

    /**
     * Constructor without parameter.
     * 
     */
    public Tracking() {
        super();
    }

    /**
     * Returns the value of the "created" attribute.
     * 
     * @return The value of the "created" attribute.
     */
    public Date getCreated() {
        return created;
    }

    /**
     * Returns the value of the "modified" attribute.
     * 
     * @return The value of the "modified" attribute.
     */
    public Date getModified() {
        return modified;
    }

    /**
     * Sets the value of the "created" attribute.
     * 
     * @param created
     *            The value of the "created" attribute.
     */
    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * Sets the value of the "modified" attribute.
     * 
     * @param modified
     *            The value of the "modified" attribute.
     */
    public void setModified(Date modified) {
        this.modified = modified;
    }

}
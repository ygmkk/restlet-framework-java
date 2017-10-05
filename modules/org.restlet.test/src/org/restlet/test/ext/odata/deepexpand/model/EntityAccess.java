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

import org.restlet.test.ext.odata.deepexpand.model.Permission;

/**
 * Generated by the generator tool for the OData extension for the Restlet
 * framework.<br>
 * 
 * @see <a
 *      href="http://praktiki.metal.ntua.gr/CoopOData/CoopOData.svc/$metadata">Metadata
 *      of the target OData service</a>
 * 
 */
public class EntityAccess {

    private boolean accessingAllDepartments;

    private String entityName;

    private int id;

    private boolean ownReadable;

    private boolean ownWritable;

    private boolean readable;

    private boolean writable;

    private Permission permission;

    /**
     * Constructor without parameter.
     * 
     */
    public EntityAccess() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param id
     *            The identifiant value of the entity.
     */
    public EntityAccess(int id) {
        this();
        this.id = id;
    }

    /**
     * Returns the value of the "accessingAllDepartments" attribute.
     * 
     * @return The value of the "accessingAllDepartments" attribute.
     */
    public boolean getAccessingAllDepartments() {
        return accessingAllDepartments;
    }

    /**
     * Returns the value of the "entityName" attribute.
     * 
     * @return The value of the "entityName" attribute.
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * Returns the value of the "id" attribute.
     * 
     * @return The value of the "id" attribute.
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the value of the "ownReadable" attribute.
     * 
     * @return The value of the "ownReadable" attribute.
     */
    public boolean getOwnReadable() {
        return ownReadable;
    }

    /**
     * Returns the value of the "ownWritable" attribute.
     * 
     * @return The value of the "ownWritable" attribute.
     */
    public boolean getOwnWritable() {
        return ownWritable;
    }

    /**
     * Returns the value of the "readable" attribute.
     * 
     * @return The value of the "readable" attribute.
     */
    public boolean getReadable() {
        return readable;
    }

    /**
     * Returns the value of the "writable" attribute.
     * 
     * @return The value of the "writable" attribute.
     */
    public boolean getWritable() {
        return writable;
    }

    /**
     * Returns the value of the "permission" attribute.
     * 
     * @return The value of the "permission" attribute.
     */
    public Permission getPermission() {
        return permission;
    }

    /**
     * Sets the value of the "accessingAllDepartments" attribute.
     * 
     * @param accessingAllDepartments
     *            The value of the "accessingAllDepartments" attribute.
     */
    public void setAccessingAllDepartments(boolean accessingAllDepartments) {
        this.accessingAllDepartments = accessingAllDepartments;
    }

    /**
     * Sets the value of the "entityName" attribute.
     * 
     * @param entityName
     *            The value of the "entityName" attribute.
     */
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    /**
     * Sets the value of the "id" attribute.
     * 
     * @param id
     *            The value of the "id" attribute.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the value of the "ownReadable" attribute.
     * 
     * @param ownReadable
     *            The value of the "ownReadable" attribute.
     */
    public void setOwnReadable(boolean ownReadable) {
        this.ownReadable = ownReadable;
    }

    /**
     * Sets the value of the "ownWritable" attribute.
     * 
     * @param ownWritable
     *            The value of the "ownWritable" attribute.
     */
    public void setOwnWritable(boolean ownWritable) {
        this.ownWritable = ownWritable;
    }

    /**
     * Sets the value of the "readable" attribute.
     * 
     * @param readable
     *            The value of the "readable" attribute.
     */
    public void setReadable(boolean readable) {
        this.readable = readable;
    }

    /**
     * Sets the value of the "writable" attribute.
     * 
     * @param writable
     *            The value of the "writable" attribute.
     */
    public void setWritable(boolean writable) {
        this.writable = writable;
    }

    /**
     * Sets the value of the "permission" attribute.
     * 
     * @param permission
     *            " The value of the "permission" attribute.
     */
    public void setPermission(Permission permission) {
        this.permission = permission;
    }

}
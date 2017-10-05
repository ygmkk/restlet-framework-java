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

package org.restlet.ext.sip.internal;

import java.util.List;

import org.restlet.engine.header.HeaderWriter;
import org.restlet.ext.sip.OptionTag;

/**
 * Option tag like header writer.
 * 
 * @author Thierry Boileau
 * @deprecated Will be removed to focus on Web APIs.
 */
@Deprecated
public class OptionTagWriter extends HeaderWriter<OptionTag> {

    /**
     * Writes a list of option tags.
     * 
     * @param optionTags
     *            The list of option tags.
     * @return The formatted list of option tags.
     */
    public static String write(List<OptionTag> optionTags) {
        return new OptionTagWriter().append(optionTags).toString();
    }

    /**
     * Writes an option tag.
     * 
     * @param optionTag
     *            The option tag.
     * @return The formatted option tag.
     */
    public static String write(OptionTag optionTag) {
        return new OptionTagWriter().append(optionTag).toString();
    }

    @Override
    public HeaderWriter<OptionTag> append(OptionTag value) {
        append(value.getTag());

        return this;
    }

}

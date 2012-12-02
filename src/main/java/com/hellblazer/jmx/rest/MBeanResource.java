/* 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 2008 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL")(collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://opendmk.dev.java.net/legal_notices/licenses.txt or in the 
 * LEGAL_NOTICES folder that accompanied this code. See the License for the 
 * specific language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file found at
 *     http://opendmk.dev.java.net/legal_notices/licenses.txt
 * or in the LEGAL_NOTICES folder that accompanied this code.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.
 * 
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * 
 *       "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding
 * 
 *       "[Contributor] elects to include this software in this distribution
 *        under the [CDDL or GPL Version 2] license."
 * 
 * If you don't indicate a single choice of license, a recipient has the option
 * to distribute your version of this file under either the CDDL or the GPL
 * Version 2, or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the
 * GPL Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 * 
 */

package com.hellblazer.jmx.rest;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.sun.jersey.api.NotFoundException;

/**
 * RESTful JMX MBean Resource. Based on JSR 311 API. Expose MBean attributes
 * only. Missing pieces : SET, INVOKE and NOTIFICATIONS.
 * 
 * @author jfdenise
 */
@Path("jmx")
public class MBeanResource {

    /**
     * Array Sub Resource.
     */
    public class ArrayResource {
        private Object array;

        public ArrayResource(Object array) {
            this.array = array;
        }

        public int discoverAllURIs(String uri, StringBuilder buffer)
                                                                    throws Exception {
            int length = Array.getLength(array);
            int added = 0;
            for (int i = 0; i < length; i++) {
                Object obj = Array.get(array, i);
                String newUri = uri + "/" + i;
                added = added
                        + MBeanResource.this.discoverAllURIs(obj,
                                                                    newUri,
                                                                    buffer);
            }
            return added;
        }

        @Path("{index}")
        public Object getItem(@PathParam("index") String index)
                                                               throws Exception {
            int idx = Integer.valueOf(index).intValue();
            try {
                return findResource(Array.get(array, idx));
            } catch (ArrayIndexOutOfBoundsException ex) {
                throw new NotFoundException(ex.toString());
            }
        }

        @GET
        @Produces("text/html")
        public Response getItemURIs() throws Exception {
            enablesHtml(true);
            return getURIs();
        }

        @GET
        @Produces("text/xml")
        public Response getXMLItemURIs() throws Exception {
            return getURIs();
        }

        private Response getURIs() throws Exception {
            int length = Array.getLength(array);
            StringBuilder buffer = startList();
            for (int i = 0; i < length; i++) {
                addItem(buffer, uriInfo.getAbsolutePath().toString() + "/" + i);
            }
            return Response.ok(endList(buffer), currentMediaType()).build();
        }
    }

    /**
     * Collection Sub Resource.
     */
    public class CollectionResource {
        private Collection<?> collection;

        public CollectionResource(Object collection) {
            this.collection = (Collection<?>) collection;
        }

        public int discoverAllURIs(String uri, StringBuilder buffer)
                                                                    throws Exception {
            int added = 0;
            int i = 0;
            for (Object obj : collection) {
                String newUri = uri + "/" + i;
                added = added
                        + MBeanResource.this.discoverAllURIs(obj,
                                                                    newUri,
                                                                    buffer);
                i++;
            }
            return added;
        }

        @Path("{index}")
        public Object getField(@PathParam("index") String index)
                                                                throws Exception {
            int idx = Integer.valueOf(index).intValue();
            return findResource(getRow(idx, collection.iterator()));
        }

        @GET
        @Produces("text/html")
        public Response getItemURIs() throws Exception {
            enablesHtml(true);
            return getURIs();
        }

        @GET
        @Produces("text/xml")
        public Response getXMLItemURIs() throws Exception {
            return getURIs();
        }

        private Response getURIs() throws Exception {
            StringBuilder buffer = startList();
            Iterator<?> it = collection.iterator();
            int i = 0;
            while (it.hasNext()) {
                addItem(buffer, uriInfo.getAbsolutePath().toString() + "/" + i);
                i++;
            }
            return Response.ok(endList(buffer), currentMediaType()).build();
        }
    }

    /**
     * CompositeData Sub Resource.
     */
    public class CompositeResource {
        private CompositeData cd;

        public CompositeResource(Object cd) {
            this.cd = (CompositeData) cd;
        }

        public int discoverAllURIs(String uri, StringBuilder buffer)
                                                                    throws Exception {
            int added = 0;
            for (String key : cd.getCompositeType().keySet()) { // Casting for JDK5
                String newUri = uri + "/" + key;
                added = added
                        + MBeanResource.this.discoverAllURIs(cd.get(key),
                                                                    newUri,
                                                                    buffer);
            }
            return added;
        }

        @Path("{fieldName}")
        public Object getField(@PathParam("fieldName") String fieldName)
                                                                        throws Exception {
            try {
                return findResource(cd.get(fieldName));
            } catch (Exception ex) {
                throw new NotFoundException(ex.toString());
            }
        }

        @GET
        @Produces("text/html")
        public Response getFieldURIs() throws Exception {
            enablesHtml(true);
            return getURIs();
        }

        @GET
        @Produces("text/xml")
        public Response getXMLFieldURIs() throws Exception {
            return getURIs();
        }

        private Response getURIs() throws Exception {
            StringBuilder buffer = startList();
            for (String key : cd.getCompositeType().keySet()) { // Casting for JDK5
                addItem(buffer, uriInfo.getAbsolutePath().toString() + "/"
                                + key);
            }
            return Response.ok(endList(buffer), currentMediaType()).build();
        }
    }

    /**
     * Map Sub Resource.
     */
    public class MapResource {
        private Map<?, ?> map;

        public MapResource(Object map) {
            this.map = (Map<?, ?>) map;
        }

        public int discoverAllURIs(String uri, StringBuilder buffer)
                                                                    throws Exception {
            int added = 0;
            for (Object key : map.keySet()) {
                String newUri = uri + "/" + key;
                added = added
                        + MBeanResource.this.discoverAllURIs(map.get(key),
                                                                    newUri,
                                                                    buffer);
            }
            return added;
        }

        @Path("{key}")
        public Object getValue(@PathParam("key") String key) throws Exception {
            // We then need to findout what is the key.toString() equals to 
            // the received key
            String decodedKey = URLDecoder.decode(key, "UTF-8");
            for (Object obj : map.entrySet()) {
                Entry<?, ?> entry = (Entry<?, ?>) obj;
                if (decodedKey.equals(entry.getKey().toString())) {
                    return findResource(entry.getValue());
                }
            }
            throw new NotFoundException("CompositeData key " + decodedKey
                                        + " is unknown");
        }

        @GET
        @Produces("text/html")
        public Response getValuesURIs() throws Exception {
            enablesHtml(true);
            return getURIs();
        }

        @GET
        @Produces("text/xml")
        public Response getXMLValuesURIs() throws Exception {
            return getURIs();
        }

        private Response getURIs() throws Exception {
            StringBuilder buffer = startList();
            for (Object key : map.keySet()) {
                addItem(buffer, uriInfo.getAbsolutePath().toString() + "/"
                                + URLEncoder.encode(key.toString(), "UTF-8"));
            }
            return Response.ok(endList(buffer), currentMediaType()).build();
        }
    }

    /**
     * Plain text Sub Resource
     */
    public class PlainResource {
        private Object value;

        public PlainResource(Object value) {
            this.value = value;
        }

        @GET
        @Produces({ "text/html", "text/xml", "text/plain" })
        public String getValue() throws Exception {
            return value == null ? "null" : value.toString();
        }
    }

    /**
     * Tabular Sub Resource.
     */
    public class TabularResource {
        private TabularData td;

        public TabularResource(Object td) {
            this.td = (TabularData) td;
        }

        public int discoverAllURIs(String uri, StringBuilder buffer)
                                                                    throws Exception {
            Collection<?> cds = td.values();
            int i = 0;
            int added = 0;
            for (Object obj : cds) {
                String newUri = uri + "/" + i;
                added = added
                        + MBeanResource.this.discoverAllURIs(obj,
                                                                    newUri,
                                                                    buffer);
                i++;
            }
            return added;
        }

        @Path("{index}")
        public Object getField(@PathParam("index") String index)
                                                                throws Exception {
            int idx = Integer.valueOf(index).intValue();
            CompositeData cd = getRow(idx, td.values().iterator());
            return findResource(cd);
        }

        @GET
        @Produces("text/html")
        public Response getFieldURIs() throws Exception {
            enablesHtml(true);
            return getURIs();
        }

        @GET
        @Produces("text/xml")
        public Response getXMLFieldURIs() throws Exception {
            return getURIs();
        }

        private Response getURIs() throws Exception {
            Collection<?> cds = td.values();
            StringBuilder buffer = startList();
            for (int i = 0; i < cds.size(); i++) {
                addItem(buffer, uriInfo.getAbsolutePath().toString() + "/" + i);
            }
            return Response.ok(endList(buffer), currentMediaType()).build();
        }
    }

    private static String LIST_BEGIN_TAG      = "<ul>\n";
    private static String LIST_END_TAG        = "</ul>\n";
    private static String LIST_ITEM_BEGIN_TAG = "<li>\n";
    private static String LIST_ITEM_END_TAG   = "</li>\n";

    /**
     * API for Resource handler. Call this operation to end a list that is used
     * to contain URI. Formating (HTML or XML) is handled by this operation.
     * 
     * @return
     */
    public static String endList(StringBuilder buffer) {
        buffer.append(LIST_END_TAG);
        return buffer.toString();
    }

    /**
     * Ability to customize the way lists are composed. By default HTML list
     * tags are used :ul, li, /li, /ul;
     * 
     * @param listBegin
     * @param listEnd
     * @param itemBegin
     * @param itemEnd
     */
    public static void setListXMLTags(String listBegin, String listEnd,
                                      String itemBegin, String itemEnd) {
        LIST_BEGIN_TAG = listBegin;
        LIST_END_TAG = listEnd;
        LIST_ITEM_BEGIN_TAG = itemBegin;
        LIST_ITEM_END_TAG = itemEnd;
    }

    /**
     * API for Resource handler. Call this operation to construct a list that
     * will be used to contain URI. Formating (HTML or XML) is handled by this
     * operation.
     * 
     * @return
     */
    public static StringBuilder startList() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(LIST_BEGIN_TAG);
        return buffer;
    }

    private static CompositeData getRow(int idx, Iterator<?> it) {
        try {
            for (int i = 0; i < idx; i++) {
                it.next();
            }
        } catch (NoSuchElementException ex) {
            throw new NotFoundException(ex.toString());
        }
        return (CompositeData) it.next();
    }

    private boolean                       isHtml;

    private final MBeanServer             mbeanServer;

    private final Map<Class<?>, Class<?>> resources = new LinkedHashMap<Class<?>, Class<?>>();
    @Context
    private UriInfo                       uriInfo;

    /** Creates a new instance of JMXMBeanResource */
    public MBeanResource(MBeanServer mbeanServer) {
        this.mbeanServer = mbeanServer;
        resources.put(CompositeData.class, CompositeResource.class);
        resources.put(TabularData.class, TabularResource.class);
        resources.put(Collection.class, CollectionResource.class);
        resources.put(Map.class, MapResource.class); // TabularData should be before Map 
    }

    /**
     * API for Resource handler. Call this operation to add an item to a list.
     * Formating (HTML or XML) is handled by this operation.
     * 
     * @param buffer
     * @param uri
     */
    public void addItem(StringBuilder buffer, String uri) {
        buffer.append(LIST_ITEM_BEGIN_TAG);
        buffer.append(formatURI(uri));
        buffer.append(LIST_ITEM_END_TAG);
    }

    public int discoverAllURIs(Object value, String uri, StringBuilder buffer)
                                                                              throws Exception {
        Object res = findResource(value);
        if (res instanceof PlainResource) {
            buffer.append(LIST_ITEM_BEGIN_TAG);
            buffer.append(formatURI(uri));
            buffer.append(LIST_ITEM_END_TAG);
            return 1;
        } else {
            Method m = res.getClass().getMethod("discoverAllURIs",
                                                String.class,
                                                StringBuilder.class);
            return (Integer) m.invoke(res, uri, buffer);
        }
    }

    /**
     * API for Resource handler. Call this operation when the handled GET
     * request has a ProvideMime of type Html. By default XML is the formating.
     * 
     * @return
     */
    public void enablesHtml(boolean b) {
        isHtml = b;
    }

    // PRIVATE METHODS

    /**
     * API for Resource handler. Call this operation to delegate to your sub
     * nodes handle the request.
     * 
     * @return
     */
    public Object findResource(Object value) throws Exception {
        if (value == null) {
            return new PlainResource("null");
        }
        Class<? extends Object> clazz = value.getClass();
        if (clazz.isArray()) {
            return new ArrayResource(value);
        }
        for (Entry<Class<?>, Class<?>> e : resources.entrySet()) {
            if (e.getKey().isAssignableFrom(clazz)) {
                // First try inners:
                Constructor<?> ctr = null;
                try {
                    ctr = e.getValue().getConstructor(MBeanResource.class,
                                                      Object.class);
                    return ctr.newInstance(this, value);
                } catch (NoSuchMethodException ex) {
                    // Then non inner
                    ctr = e.getValue().getConstructor(MBeanResource.class,
                                                      UriInfo.class,
                                                      Object.class);
                    return ctr.newInstance(uriInfo, value);
                }
            }
        }
        return new PlainResource(value);
    }

    /**
     * Returns an attribute value.
     * 
     * @param objName
     * @param attributeName
     * @return
     * @throws java.lang.Exception
     */
    @Path("{objName}/{attributeName}")
    public Object getAttribute(@PathParam("objName") String objName,
                               @PathParam("attributeName") String attributeName)
                                                                                throws Exception {

        return mbeanAttribute(objName, attributeName);
    }

    /**
     * Returns the list of data leaves URI. This presents an HTML flattened view
     * of all information located in an MBeanServer Pattern can be used to
     * filter out MBeans.
     */
    @GET
    @Produces("text/html")
    @Path("all")
    public String listAllLeafURI(@QueryParam("pattern") String pattern)
                                                                       throws Exception {
        isHtml = true;
        return mbeanLeafURIs(pattern);
    }

    /**
     * Return all the attributes of an MBean. If some values are CompositeData,
     * TabularData or Array, they are returned as URI.
     * 
     * @param objName
     * @return
     * @throws java.lang.Exception
     */
    @GET
    @Path("{objName}")
    @Produces("text/html")
    public String listMBeanAttributesURI(@PathParam("objName") String objName)
                                                                              throws Exception {
        isHtml = true;
        return mbeanAttributesURIs(objName);
    }

    /**
     * Returns the list of MBean URI.
     */
    @GET
    @Produces("text/html")
    public Object listMBeanURIs(@QueryParam("pattern") String pattern)
                                                                      throws Exception {
        isHtml = true;
        return mbeanURIs(pattern);
    }

    /**
     * Returns the list of data leaves URI. This presents an HTML flattened view
     * of all information located in an MBeanServer Pattern can be used to
     * filter out MBeans.
     */
    @GET
    @Produces("text/xml")
    @Path("all")
    public String listXMLAllLeafURI(@QueryParam("pattern") String pattern)
                                                                          throws Exception {
        return mbeanLeafURIs(pattern);
    }

    /**
     * Return all the attributes of an MBean. If some values are CompositeData,
     * TabularData or Array, they are returned as URI.
     * 
     * @param objName
     * @return
     * @throws java.lang.Exception
     */
    @GET
    @Path("{objName}")
    @Produces("text/xml")
    public String listXMLMBeanAttributesURI(@PathParam("objName") String objName)
                                                                                 throws Exception {
        return mbeanAttributesURIs(objName);
    }

    /**
     * Returns the list of MBean URI.
     */
    @GET
    @Produces("text/xml")
    public Object listXMLMBeanURI(@QueryParam("pattern") String pattern)
                                                                        throws Exception {
        return mbeanURIs(pattern);
    }

    public String mbeanLeafURIs(String pat) throws Exception {
        ObjectName pattern = pat == null ? null : new ObjectName(pat);
        Set<?> s = mbeanServer.queryNames(pattern, null);
        StringBuilder buffer = startList();
        int num = 0;
        for (Object obj : s) {
            ObjectName name = (ObjectName) obj;
            MBeanInfo info = mbeanServer.getMBeanInfo(name);
            MBeanAttributeInfo[] attrs = info.getAttributes();
            for (MBeanAttributeInfo attr : attrs) {

                Object value = null;
                try {
                    value = mbeanServer.getAttribute(name, attr.getName());
                } catch (Exception ex) {
                    value = ex;
                }
                String uri = uriInfo.getBaseUri() + "jmx/" + name + "/"
                             + attr.getName();
                num = num + discoverAllURIs(value, uri, buffer);
            }
        }

        String lst = endList(buffer);
        if (isHtml) {
            return "<h2> Number of MBean data leaves : " + num + "</h2>" + lst;
        } else {
            return lst;
        }
    }

    /**
     * Register Resource Handler. A resource must have a constructor with a
     * UriInfo, Object parameters.
     */
    public void registerResourceHandler(Class<?> clazz, Class<?> handler) {
        resources.put(clazz, handler);
    }

    private String currentMediaType() {
        return isHtml ? "text/html" : "text/xml";
    }

    private String formatURI(String uri) {
        if (isHtml) {
            return "<a href=\"" + uri + "\">" + uri + "</a>";
        } else {
            return uri;
        }
    }

    private Object mbeanAttribute(String objName, String attributeName)
                                                                       throws Exception {
        ObjectName name = new ObjectName(objName);
        Object value = null;
        try {
            value = mbeanServer.getAttribute(name, attributeName);
        } catch (AttributeNotFoundException ex) {
            throw new NotFoundException(ex.toString());
        } catch (InstanceNotFoundException ex) {
            throw new NotFoundException(ex.toString());
        } catch (Exception ex) {
            value = ex.toString();
        }
        return findResource(value);
    }

    private String mbeanAttributesURIs(String objName) throws Exception {
        StringBuilder buffer = startList();
        ObjectName name = new ObjectName(objName);
        MBeanInfo info;
        try {
            info = mbeanServer.getMBeanInfo(name);
        } catch (InstanceNotFoundException ex) {
            throw new NotFoundException(ex.toString());
        }
        MBeanAttributeInfo[] attrs = info.getAttributes();
        for (MBeanAttributeInfo attr : attrs) {
            buffer.append(LIST_ITEM_BEGIN_TAG);
            buffer.append(formatURI(uriInfo.getAbsolutePath() + "/"
                                    + attr.getName()));
            buffer.append(LIST_ITEM_END_TAG);
        }
        return endList(buffer);
    }

    private Object mbeanURIs(String pat) throws Exception {
        ObjectName pattern = pat == null ? null : new ObjectName(pat);
        Set<?> s = mbeanServer.queryNames(pattern, null);
        StringBuilder buffer = startList();
        for (Object obj : s) {
            ObjectName n = (ObjectName) obj;
            buffer.append(LIST_ITEM_BEGIN_TAG);
            String mbeanURI = uriInfo.getAbsolutePath() + "/" + n.toString();
            buffer.append(formatURI(mbeanURI));
            buffer.append(LIST_ITEM_END_TAG);
        }

        return endList(buffer);
    }
}
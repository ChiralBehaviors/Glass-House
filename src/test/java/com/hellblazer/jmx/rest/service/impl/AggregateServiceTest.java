package com.hellblazer.jmx.rest.service.impl;

import static com.hellblazer.jmx.cascading.proxy.ProxyCascadingAgent.getTargetName;
import static com.hellblazer.jmx.rest.service.impl.AggregateServiceImpl.getNodeWildcardName;
import static junit.framework.Assert.assertEquals;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.hellblazer.jmx.rest.AbstractMockitoTest;
import com.hellblazer.jmx.rest.domain.jaxb.jmx.MBeanAttributeJaxBeans;
import com.hellblazer.jmx.rest.domain.jaxb.jmx.MBeanAttributeValueJaxBean;
import com.hellblazer.jmx.rest.domain.jaxb.jmx.MBeanAttributeValueJaxBeans;
import com.hellblazer.jmx.rest.domain.jaxb.jmx.MBeanOperationJaxBeans;
import com.hellblazer.jmx.rest.domain.jaxb.jmx.MBeanShortJaxBeans;
import com.hellblazer.jmx.rest.service.AggregateService;

public class AggregateServiceTest extends AbstractMockitoTest {

    public static final String  MEMORY_MXBEAN                             = "java.lang:type=Memory";
    public static final String  THREADING_MXBEAN                          = "java.lang:type=Threading";
    public static final String  LOGGING_MBEAN                             = "java.util.logging:type=logging";
    public static final String  JETTY_SERVER_MBEAN                        = "org.eclipse.jetty.server:type=server";
    public static final String  MEMORY_MXBEAN_HEAP                        = "HeapMemoryUsage";
    public static final String  MEMORY_MXBEAN_NONHEAP                     = "NonHeapMemoryUsage";
    public static final String  MEMORY_MXBEAN_OBJECT_PENDING_FINALIZATION = "ObjectPendingFinalizationCount";
    public static final String  MEMORY_MXBEAN_VERBOSE                     = "Verbose";

    private static final String NODE1                                     = "localhost|8888";
    private static final String NODE2                                     = "localhost|9999";
    private static final String NODE3                                     = "localhost|7777";

    @Mock
    MBeanServer                 mbs;
    @Mock
    UriInfo                     _uriInfo;
    @Mock
    UriBuilder                  _uriBuilder;

    // class under test 
    AggregateService            _aggregateServiceImpl;

    // test data
    Collection<String>          _jmxNodes;
    CompositeDataSupport        _compositeDataHeapNode1;
    CompositeDataSupport        _compositeDataHeapNode2;
    CompositeDataSupport        _compositeDataHeapNode3;
    CompositeDataSupport        _compositeDataNonHeapNode1;
    CompositeDataSupport        _compositeDataNonHeapNode2;
    CompositeDataSupport        _compositeDataNonHeapNode3;

    int                         _nodes                                    = 2;

    final long                  _init                                     = 0L;
    long                        _usedNode1                                = 14122104L;
    long                        _comittedNode1                            = 851000192L;
    long                        _max                                      = 129957888L;
    long                        _usedNode2                                = 15622104L;
    long                        _comittedNode2                            = 955000192L;
    long                        _maxNode2                                 = 134957888L;
    boolean                     _verboseNode1                             = true;
    boolean                     _verboseNode2                             = false;
    boolean                     _verboseNode3                             = false;
    int                         _objectsPendingFinalizationNode1          = 0;
    int                         _objectsPendingFinalizationNode2          = 1;
    int                         _objectsPendingFinalizationNode3          = 1;
    private String              _jettyServerBeanPrefix                    = JETTY_SERVER_MBEAN.replaceFirst(AggregateServiceImpl.ID_REPLACE_REGEX,
                                                                                                            "");
    private Set<ObjectName>     _jettyServerObjectNames                   = new TreeSet<ObjectName>();

    @Before
    public void setUp() throws Exception {
        _aggregateServiceImpl = new AggregateServiceImpl(mbs);
        _jmxNodes = new HashSet<String>();
        _jmxNodes.add(NODE1);
        _jmxNodes.add(NODE2);
        _jmxNodes.add(NODE3);

        String[] names = new String[] { "init", "committed", "max", "used" };

        @SuppressWarnings("rawtypes")
        OpenType[] itemTypes = new OpenType[] { SimpleType.LONG,
                SimpleType.LONG, SimpleType.LONG, SimpleType.LONG };

        CompositeType compositeType = new CompositeType("mem", "mem", names,
                                                        names, itemTypes);
        _compositeDataHeapNode1 = new CompositeDataSupport(
                                                           compositeType,
                                                           fillCompositeDataMap(_init,
                                                                                _usedNode1,
                                                                                _comittedNode1,
                                                                                _max));
        _compositeDataHeapNode2 = new CompositeDataSupport(
                                                           compositeType,
                                                           fillCompositeDataMap(_init,
                                                                                _usedNode2,
                                                                                _comittedNode2,
                                                                                _maxNode2));
        _compositeDataHeapNode3 = _compositeDataHeapNode2;
        _compositeDataNonHeapNode1 = new CompositeDataSupport(
                                                              compositeType,
                                                              fillCompositeDataMap(_init,
                                                                                   123L,
                                                                                   123L,
                                                                                   123L));
        _compositeDataNonHeapNode2 = new CompositeDataSupport(
                                                              compositeType,
                                                              fillCompositeDataMap(_init,
                                                                                   234L,
                                                                                   234L,
                                                                                   234L));
        _compositeDataNonHeapNode3 = _compositeDataNonHeapNode2;

        _jettyServerObjectNames.add(ObjectName.getInstance(JETTY_SERVER_MBEAN));
    }

    @Test
    public void testGetAllAttributeValues() throws Exception {
        setExpectationsForJmxServiceGetAttributes();
        setExpectationsForJmxServiceGetMemoryAttribute();
        ObjectName node1MemoryBean = getTargetName(NODE1, MEMORY_MXBEAN);
        ObjectName node2MemoryBean = getTargetName(NODE2, MEMORY_MXBEAN);
        ObjectName node3MemoryBean = getTargetName(NODE3, MEMORY_MXBEAN);
        when(mbs.getAttribute(node1MemoryBean, MEMORY_MXBEAN_NONHEAP)).thenReturn(_compositeDataNonHeapNode1);
        when(mbs.getAttribute(node2MemoryBean, MEMORY_MXBEAN_NONHEAP)).thenReturn(_compositeDataNonHeapNode2);
        when(mbs.getAttribute(node3MemoryBean, MEMORY_MXBEAN_NONHEAP)).thenReturn(_compositeDataNonHeapNode3);
        when(
             mbs.getAttribute(node1MemoryBean,
                              MEMORY_MXBEAN_OBJECT_PENDING_FINALIZATION)).thenReturn(_objectsPendingFinalizationNode1);
        when(
             mbs.getAttribute(node2MemoryBean,
                              MEMORY_MXBEAN_OBJECT_PENDING_FINALIZATION)).thenReturn(_objectsPendingFinalizationNode2);
        when(
             mbs.getAttribute(node3MemoryBean,
                              MEMORY_MXBEAN_OBJECT_PENDING_FINALIZATION)).thenReturn(_objectsPendingFinalizationNode3);
        when(mbs.getAttribute(node1MemoryBean, MEMORY_MXBEAN_VERBOSE)).thenReturn(_objectsPendingFinalizationNode1);
        when(mbs.getAttribute(node2MemoryBean, MEMORY_MXBEAN_VERBOSE)).thenReturn(_objectsPendingFinalizationNode2);
        when(mbs.getAttribute(node3MemoryBean, MEMORY_MXBEAN_VERBOSE)).thenReturn(_objectsPendingFinalizationNode3);

        MBeanAttributeValueJaxBeans attributes = _aggregateServiceImpl.getAllAttributeValues(_jmxNodes,
                                                                                             MEMORY_MXBEAN);

        assertEquals("expected four values for each of two nodes and the local node",
                     12, attributes.mBeanAttributeValueJaxBeans.size());
    }

    @Test
    public void testGetAttributesMetaData() throws Exception {
        setExpectationsForGetAttributesMetaDataTests();
        MBeanAttributeJaxBeans mBeanAttributesInfoJaxBean = _aggregateServiceImpl.getAttributesMetaData(_uriInfo,
                                                                                                        _jmxNodes,
                                                                                                        JETTY_SERVER_MBEAN);

        assertEquals(1,
                     mBeanAttributesInfoJaxBean.mBeanAttributeJaxBeans.size());
    }

    @Test
    public void testGetAttributesMetaDataWithBeanAvailableOnlyOnOneOfTwoNodes()
                                                                               throws Exception {
        setExpectationsForJmxServiceGetAttributes();
        when(mbs.getAttributes(getTargetName(NODE2, MEMORY_MXBEAN), null)).thenReturn(null);

        MBeanAttributeJaxBeans mBeanAttributesInfoJaxBean = _aggregateServiceImpl.getAttributesMetaData(_uriInfo,
                                                                                                        _jmxNodes,
                                                                                                        MEMORY_MXBEAN);

        assertEquals(0,
                     mBeanAttributesInfoJaxBean.mBeanAttributeJaxBeans.size());

    }

    @Test
    public void testGetAttributesMetaDataWithMissingAttributeOnOneNode()
                                                                        throws Exception {
        setExpectationsForGetAttributesMetaDataTests();
        when(mbs.getAttributes(getTargetName(NODE1, JETTY_SERVER_MBEAN), null)).thenReturn(null);
        MBeanAttributeJaxBeans mBeanAttributesInfoJaxBean = _aggregateServiceImpl.getAttributesMetaData(_uriInfo,
                                                                                                        _jmxNodes,
                                                                                                        JETTY_SERVER_MBEAN);

        assertEquals(0,
                     mBeanAttributesInfoJaxBean.mBeanAttributeJaxBeans.size());
    }

    @Test
    public void testGetAttributesMetaDataWithSlightlyDifferentMetadataOnBothNodes()
                                                                                   throws Exception {
        setExpectationsForJmxServiceGetAttributes();
        MBeanInfo info = mock(MBeanInfo.class);
        when(info.getAttributes()).thenReturn(prepareMBeanAttributeInfoArrayForMemoryMXBean(false));
        when(mbs.getMBeanInfo(getTargetName(NODE2, MEMORY_MXBEAN))).thenReturn(info);

        MBeanAttributeJaxBeans mBeanAttributesInfoJaxBean = _aggregateServiceImpl.getAttributesMetaData(_uriInfo,
                                                                                                        _jmxNodes,
                                                                                                        MEMORY_MXBEAN);

        assertEquals(0,
                     mBeanAttributesInfoJaxBean.mBeanAttributeJaxBeans.size());
    }

    @Test
    public void testGetAttributeValues() throws Exception {
        setExpectationsForJmxServiceGetMemoryAttribute();
        MBeanAttributeValueJaxBeans mBeanAttributeValuesJaxBean = _aggregateServiceImpl.getAttributeValues(_jmxNodes,
                                                                                                           MEMORY_MXBEAN,
                                                                                                           MEMORY_MXBEAN_HEAP);
        assertEquals(_jmxNodes.size(),
                     mBeanAttributeValuesJaxBean.mBeanAttributeValueJaxBeans.size());
        for (MBeanAttributeValueJaxBean mBeanAttributeValueJaxBean : mBeanAttributeValuesJaxBean.mBeanAttributeValueJaxBeans) {
            if (NODE1.equals(mBeanAttributeValueJaxBean.nodeName)) {
                assertEquals(_compositeDataHeapNode1.toString(),
                             mBeanAttributeValueJaxBean.value);
            }
        }
    }

    @Test
    public void testGetObjectNames() throws Exception {
        ObjectName aThirdCommonObjectName = new ObjectName(JETTY_SERVER_MBEAN);
        Set<ObjectName> commonObjectNames = getCommonObjectNameSet();
        commonObjectNames.add(aThirdCommonObjectName);

        addThirdNodeToLocalCloudtide();

        setCommonExpectationsForGetObjectNameTests(commonObjectNames);
        when(mbs.queryNames(getNodeWildcardName(NODE3), null)).thenReturn(commonObjectNames);

        MBeanShortJaxBeans mBeanShortJaxBeans = _aggregateServiceImpl.getMBeanShortJaxBeans(_uriInfo,
                                                                                            _jmxNodes);

        assertEquals("Expected to get three common mBeanShortJaxBeans as they exist on all nodes",
                     3, mBeanShortJaxBeans.mbeans.size());
    }

    @Test
    public void testGetObjectNamesWithAnAdditionalObjectNameOnOneNode()
                                                                       throws Exception {
        ObjectName onlyOnOneNodeObjectName = new ObjectName(JETTY_SERVER_MBEAN);

        Set<ObjectName> commonObjectNames = getCommonObjectNameSet();
        Set<ObjectName> commonObjectNamesPlusThreading = new HashSet<ObjectName>();
        commonObjectNamesPlusThreading.addAll(commonObjectNames);
        commonObjectNamesPlusThreading.add(onlyOnOneNodeObjectName);

        addThirdNodeToLocalCloudtide();

        setCommonExpectationsForGetObjectNameTests(commonObjectNames);
        when(mbs.queryNames(any(ObjectName.class), any(QueryExp.class))).thenReturn(commonObjectNamesPlusThreading);

        MBeanShortJaxBeans mBeanShortJaxBeans = _aggregateServiceImpl.getMBeanShortJaxBeans(_uriInfo,
                                                                                            _jmxNodes);

        assertEquals("Expected to get two common mBeanShortJaxBeans as the THREADING_MXBEAN only exists on one node",
                     2, mBeanShortJaxBeans.mbeans.size());
    }

    @Test
    public void testGetOperationsMetaData() throws Exception {
        setDefaultGetOperationsExpectations();

        MBeanInfo info = mock(MBeanInfo.class);
        when(info.getOperations()).thenReturn(prepareMBeanOperationInfoArray("operation1",
                                                                             "desc 1"));
        when(mbs.getMBeanInfo(getTargetName(NODE3, MEMORY_MXBEAN))).thenReturn(info);

        MBeanOperationJaxBeans mBeanOperationsInfoJaxBean = _aggregateServiceImpl.getOperationsMetaData(_uriInfo,
                                                                                                        _jmxNodes,
                                                                                                        MEMORY_MXBEAN);

        assertEquals(1,
                     mBeanOperationsInfoJaxBean.mBeanOperationJaxBeans.size());
    }

    @Test
    public void testGetOperationsMetaDataForServerBean() throws Exception {
        Set<ObjectName> names = new HashSet<ObjectName>();
        names.add(getTargetName(NODE1, JETTY_SERVER_MBEAN));
        names.add(getTargetName(NODE2, JETTY_SERVER_MBEAN));
        names.add(getTargetName(NODE3, JETTY_SERVER_MBEAN));
        when(mbs.queryNames(any(ObjectName.class), any(QueryExp.class))).thenReturn(names);

        MBeanOperationInfo[] mBeanOperationInfos = prepareMBeanOperationInfoArray("operation1",
                                                                                  "desc 1");
        setUriInfoExpectations();
        setExpectationsForGetObjectNames();
        MBeanInfo info = mock(MBeanInfo.class);
        when(info.getOperations()).thenReturn(mBeanOperationInfos);
        when(mbs.getMBeanInfo(getTargetName(NODE1, JETTY_SERVER_MBEAN))).thenReturn(info);
        when(mbs.getMBeanInfo(getTargetName(NODE2, JETTY_SERVER_MBEAN))).thenReturn(info);
        when(mbs.getMBeanInfo(getTargetName(NODE3, JETTY_SERVER_MBEAN))).thenReturn(info);

        MBeanOperationJaxBeans mBeanOperationsInfoJaxBean = _aggregateServiceImpl.getOperationsMetaData(_uriInfo,
                                                                                                        _jmxNodes,
                                                                                                        JETTY_SERVER_MBEAN);

        assertEquals(1,
                     mBeanOperationsInfoJaxBean.mBeanOperationJaxBeans.size());
    }

    @Test
    public void testGetOperationsMetaDataWithSlightlyDifferencesOnAThirdNode()
                                                                              throws Exception {
        setDefaultGetOperationsExpectations();
        MBeanInfo info = mock(MBeanInfo.class);
        when(info.getOperations()).thenReturn(prepareMBeanOperationInfoArray("operation1",
                                                                             "desc is different"));
        when(mbs.getMBeanInfo(getTargetName(NODE3, MEMORY_MXBEAN))).thenReturn(info);

        MBeanOperationJaxBeans mBeanOperationsInfoJaxBean = _aggregateServiceImpl.getOperationsMetaData(_uriInfo,
                                                                                                        _jmxNodes,
                                                                                                        MEMORY_MXBEAN);

        assertEquals(0,
                     mBeanOperationsInfoJaxBean.mBeanOperationJaxBeans.size());
    }

    @Test
    public void testInvokeOperation() throws Exception {
        Set<ObjectName> names = new HashSet<ObjectName>();
        names.add(getTargetName(NODE1, MEMORY_MXBEAN));
        names.add(getTargetName(NODE2, MEMORY_MXBEAN));
        names.add(getTargetName(NODE3, MEMORY_MXBEAN));
        when(mbs.queryNames(any(ObjectName.class), any(QueryExp.class))).thenReturn(names);
        _aggregateServiceImpl.invokeOperation(_jmxNodes, MEMORY_MXBEAN, "gc");

        verify(mbs).invoke(eq(getTargetName(NODE1, MEMORY_MXBEAN)), eq("gc"),
                           aryEq(new Object[] {}), aryEq(new String[] {}));
        verify(mbs).invoke(eq(getTargetName(NODE2, MEMORY_MXBEAN)), eq("gc"),
                           aryEq(new Object[] {}), aryEq(new String[] {}));
        verify(mbs).invoke(eq(getTargetName(NODE3, MEMORY_MXBEAN)), eq("gc"),
                           aryEq(new Object[] {}), aryEq(new String[] {}));
    }

    @Test
    public void testInvokeOperationWithParameters() throws Exception {
        Set<ObjectName> names = new HashSet<ObjectName>();
        names.add(getTargetName(NODE1, LOGGING_MBEAN));
        names.add(getTargetName(NODE2, LOGGING_MBEAN));
        names.add(getTargetName(NODE3, LOGGING_MBEAN));
        when(mbs.queryNames(any(ObjectName.class), any(QueryExp.class))).thenReturn(names);

        String stringSignature = "java.lang.String";

        Object[] params = new Object[] { "com.intalio", "FINEST" };

        String[] signature = new String[] { stringSignature, stringSignature };

        _aggregateServiceImpl.invokeOperation(_jmxNodes, LOGGING_MBEAN,
                                              "setLoggerLevel", params,
                                              signature);

        verify(mbs).invoke(eq(getTargetName(NODE1, LOGGING_MBEAN)),
                           eq("setLoggerLevel"), aryEq(params),
                           aryEq(signature));
        verify(mbs).invoke(eq(getTargetName(NODE2, LOGGING_MBEAN)),
                           eq("setLoggerLevel"), aryEq(params),
                           aryEq(signature));
        verify(mbs).invoke(eq(getTargetName(NODE3, LOGGING_MBEAN)),
                           eq("setLoggerLevel"), aryEq(params),
                           aryEq(signature));
    }

    private void addThirdNodeToLocalCloudtide() {
        _jmxNodes.add(NODE3);
    }

    private Map<String, Long> fillCompositeDataMap(Long init, Long used,
                                                   Long committed, Long max) {
        Map<String, Long> map = new TreeMap<String, Long>();
        map.put("init", init);
        map.put("committed", committed);
        map.put("max", max);
        map.put("used", used);
        return map;
    }

    private Set<ObjectName> getCommonObjectNameSet()
                                                    throws MalformedObjectNameException {
        ObjectName commonToAllNodesObjectName = new ObjectName(MEMORY_MXBEAN);
        ObjectName anotherCommonToAllNodesObjectName = new ObjectName(
                                                                      THREADING_MXBEAN);
        Set<ObjectName> commonObjectNames = new HashSet<ObjectName>();
        commonObjectNames.add(commonToAllNodesObjectName);
        commonObjectNames.add(anotherCommonToAllNodesObjectName);
        return commonObjectNames;
    }

    private MBeanAttributeInfo[] prepareMBeanAttributeInfoArrayForJettyServerMBean() {
        MBeanAttributeInfo mBeanAttributeInfo = new MBeanAttributeInfo(
                                                                       "Name",
                                                                       "Type",
                                                                       "description",
                                                                       true,
                                                                       false,
                                                                       false);
        return new MBeanAttributeInfo[] { mBeanAttributeInfo };
    }

    private MBeanAttributeInfo[] prepareMBeanAttributeInfoArrayForMemoryMXBean(boolean isReadable) {
        MBeanAttributeInfo mBeanAttributeInfoHeap = new MBeanAttributeInfo(
                                                                           MEMORY_MXBEAN_HEAP,
                                                                           "type",
                                                                           "description",
                                                                           isReadable,
                                                                           false,
                                                                           false);
        MBeanAttributeInfo mBeanAttributeInfoNonHeap = new MBeanAttributeInfo(
                                                                              MEMORY_MXBEAN_NONHEAP,
                                                                              "type",
                                                                              "description",
                                                                              isReadable,
                                                                              false,
                                                                              false);
        MBeanAttributeInfo mBeanAttributeInfoObjectPending = new MBeanAttributeInfo(
                                                                                    MEMORY_MXBEAN_OBJECT_PENDING_FINALIZATION,
                                                                                    "type",
                                                                                    "description",
                                                                                    isReadable,
                                                                                    false,
                                                                                    false);
        MBeanAttributeInfo mBeanAttributeInfoVerbose = new MBeanAttributeInfo(
                                                                              MEMORY_MXBEAN_VERBOSE,
                                                                              "type",
                                                                              "description",
                                                                              isReadable,
                                                                              false,
                                                                              false);
        MBeanAttributeInfo[] mBeanAttributeInfos = new MBeanAttributeInfo[] {
                mBeanAttributeInfoHeap, mBeanAttributeInfoNonHeap,
                mBeanAttributeInfoObjectPending, mBeanAttributeInfoVerbose };
        return mBeanAttributeInfos;
    }

    private MBeanOperationInfo[] prepareMBeanOperationInfoArray(String name,
                                                                String description) {
        MBeanOperationInfo mBeanOperationInfo = new MBeanOperationInfo(
                                                                       name,
                                                                       description,
                                                                       null,
                                                                       "String",
                                                                       1);
        MBeanOperationInfo[] mBeanOperationInfos = new MBeanOperationInfo[] { mBeanOperationInfo };
        return mBeanOperationInfos;
    }

    private void setCommonExpectationsForGetObjectNameTests(Set<ObjectName> commonObjectNames)
                                                                                              throws Exception {
        setUriInfoExpectations();
        when(mbs.queryNames(getNodeWildcardName(NODE1), null)).thenReturn(commonObjectNames);
        when(mbs.queryNames(getNodeWildcardName(NODE3), null)).thenReturn(commonObjectNames);
    }

    private void setDefaultGetOperationsExpectations() throws Exception {
        Set<ObjectName> names = new HashSet<ObjectName>();
        names.add(getTargetName(NODE1, MEMORY_MXBEAN));
        names.add(getTargetName(NODE3, MEMORY_MXBEAN));
        when(mbs.queryNames(any(ObjectName.class), any(QueryExp.class))).thenReturn(names);
        MBeanOperationInfo[] mBeanOperationInfos = prepareMBeanOperationInfoArray("operation1",
                                                                                  "desc 1");
        MBeanInfo info = mock(MBeanInfo.class);
        when(info.getOperations()).thenReturn(mBeanOperationInfos);
        addThirdNodeToLocalCloudtide();

        setUriInfoExpectations();
        when(mbs.getMBeanInfo(getTargetName(NODE1, MEMORY_MXBEAN))).thenReturn(info);

    }

    private void setExpectationsForGetAttributesMetaDataTests()
                                                               throws Exception {
        Set<ObjectName> names = new HashSet<ObjectName>();
        names.add(getTargetName(NODE1, JETTY_SERVER_MBEAN));
        names.add(getTargetName(NODE2, JETTY_SERVER_MBEAN));
        names.add(getTargetName(NODE3, JETTY_SERVER_MBEAN));
        when(mbs.queryNames(any(ObjectName.class), any(QueryExp.class))).thenReturn(names);
        MBeanAttributeInfo[] mBeanAttributeInfos = prepareMBeanAttributeInfoArrayForJettyServerMBean();
        setExpectationsForGetObjectNames();
        setUriInfoExpectations();
        MBeanInfo info = mock(MBeanInfo.class);
        when(info.getAttributes()).thenReturn(mBeanAttributeInfos);
        when(mbs.getMBeanInfo(getTargetName(NODE1, JETTY_SERVER_MBEAN))).thenReturn(info);
        when(mbs.getMBeanInfo(getTargetName(NODE2, JETTY_SERVER_MBEAN))).thenReturn(info);
        when(mbs.getMBeanInfo(getTargetName(NODE3, JETTY_SERVER_MBEAN))).thenReturn(info);
    }

    private void setExpectationsForGetObjectNames() throws Exception {
        when(mbs.queryNames(getTargetName(NODE1, _jettyServerBeanPrefix), null)).thenReturn(_jettyServerObjectNames);
        when(mbs.queryNames(getTargetName(NODE2, _jettyServerBeanPrefix), null)).thenReturn(_jettyServerObjectNames);
        when(mbs.queryNames(getTargetName(NODE3, _jettyServerBeanPrefix), null)).thenReturn(_jettyServerObjectNames);
    }

    private void setExpectationsForJmxServiceGetAttributes() throws Exception {
        MBeanAttributeInfo[] mBeanAttributeInfos = prepareMBeanAttributeInfoArrayForMemoryMXBean(true);

        MBeanInfo info = mock(MBeanInfo.class);
        when(info.getAttributes()).thenReturn(mBeanAttributeInfos);
        setUriInfoExpectations();
        when(mbs.getMBeanInfo(getTargetName(NODE1, MEMORY_MXBEAN))).thenReturn(info);
        when(mbs.getMBeanInfo(getTargetName(NODE2, MEMORY_MXBEAN))).thenReturn(info);
        when(mbs.getMBeanInfo(getTargetName(NODE3, MEMORY_MXBEAN))).thenReturn(info);
    }

    private void setExpectationsForJmxServiceGetMemoryAttribute()
                                                                 throws Exception {
        Set<ObjectName> names = new HashSet<ObjectName>();
        names.add(getTargetName(NODE1, MEMORY_MXBEAN));
        names.add(getTargetName(NODE2, MEMORY_MXBEAN));
        names.add(getTargetName(NODE3, MEMORY_MXBEAN));
        when(mbs.queryNames(any(ObjectName.class), any(QueryExp.class))).thenReturn(names);
        when(
             mbs.getAttribute(getTargetName(NODE1, MEMORY_MXBEAN),
                              MEMORY_MXBEAN_HEAP)).thenReturn(_compositeDataHeapNode1);
        when(
             mbs.getAttribute(getTargetName(NODE2, MEMORY_MXBEAN),
                              MEMORY_MXBEAN_HEAP)).thenReturn(_compositeDataHeapNode2);
        when(
             mbs.getAttribute(getTargetName(NODE3, MEMORY_MXBEAN),
                              MEMORY_MXBEAN_HEAP)).thenReturn(_compositeDataHeapNode3);
    }

    private void setUriInfoExpectations() throws URISyntaxException {
        when(_uriInfo.getAbsolutePathBuilder()).thenReturn(_uriBuilder);
        when(_uriBuilder.path(any(String.class))).thenReturn(_uriBuilder);
        when(_uriBuilder.build()).thenReturn(new URI("http://testuri/test"));
    }

}

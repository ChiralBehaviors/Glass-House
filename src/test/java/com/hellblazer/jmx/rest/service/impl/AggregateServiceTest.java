package com.hellblazer.jmx.rest.service.impl;

import static junit.framework.Assert.assertEquals;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
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

import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.hellblazer.jmx.rest.AbstractMockitoTest;
import com.hellblazer.jmx.rest.domain.jaxb.jmx.MBeanAttributeJaxBeans;
import com.hellblazer.jmx.rest.domain.jaxb.jmx.MBeanAttributeValueJaxBean;
import com.hellblazer.jmx.rest.domain.jaxb.jmx.MBeanAttributeValueJaxBeans;
import com.hellblazer.jmx.rest.domain.jaxb.jmx.MBeanOperationJaxBeans;
import com.hellblazer.jmx.rest.domain.jaxb.jmx.MBeanShortJaxBeans;
import com.hellblazer.jmx.rest.service.AggregateService;
import com.hellblazer.jmx.rest.service.JMXService;

public class AggregateServiceTest extends AbstractMockitoTest {
    private static final String NODE1                            = "localhost:8888";
    private static final String NODE2                            = "localhost:9999";
    private static final String NODE3                            = "localhost:7777";

    @Mock
    JMXService                  _jmxService;
    @Mock
    UriInfo                     _uriInfo;
    @Mock
    UriBuilder                  _uriBuilder;

    // class under test
    @InjectMocks
    AggregateService            _aggregateServiceImpl            = new AggregateServiceImpl(
                                                                                            _jmxService);

    // test data
    Collection<String>          _jmxNodes;
    CompositeDataSupport        _compositeDataHeapNode1;
    CompositeDataSupport        _compositeDataHeapNode2;
    CompositeDataSupport        _compositeDataHeapNode3;
    CompositeDataSupport        _compositeDataNonHeapNode1;
    CompositeDataSupport        _compositeDataNonHeapNode2;
    CompositeDataSupport        _compositeDataNonHeapNode3;

    int                         _nodes                           = 2;

    final long                  _init                            = 0L;
    long                        _usedNode1                       = 14122104L;
    long                        _comittedNode1                   = 851000192L;
    long                        _max                             = 129957888L;
    long                        _usedNode2                       = 15622104L;
    long                        _comittedNode2                   = 955000192L;
    long                        _maxNode2                        = 134957888L;
    boolean                     _verboseNode1                    = true;
    boolean                     _verboseNode2                    = false;
    boolean                     _verboseNode3                    = false;
    int                         _objectsPendingFinalizationNode1 = 0;
    int                         _objectsPendingFinalizationNode2 = 1;
    int                         _objectsPendingFinalizationNode3 = 1;
    private String              _jettyServerBeanPrefix           = JMXServiceImpl.JETTY_SERVER_MBEAN.replaceFirst(AggregateServiceImpl.ID_REPLACE_REGEX,
                                                                                                                  "");
    private Set<String>         _jettyServerObjectNames          = new TreeSet<String>();

    @Before
    public void setUp() throws Exception {
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

        _jettyServerObjectNames.add(JMXServiceImpl.JETTY_SERVER_MBEAN);
        _jettyServerObjectNames.add(JMXServiceImpl.JETTY_SERVER_MBEANID1);
    }

    @Test
    public void testGetAllAttributeValues() throws Exception {
        setExpectationsForJmxServiceGetAttributes();
        setExpectationsForJmxServiceGetMemoryAttribute();
        when(
             _jmxService.getAttribute(NODE1, JMXServiceImpl.MEMORY_MXBEAN,
                                      JMXServiceImpl.MEMORY_MXBEAN_NONHEAP)).thenReturn(_compositeDataNonHeapNode1);
        when(
             _jmxService.getAttribute(NODE2, JMXServiceImpl.MEMORY_MXBEAN,
                                      JMXServiceImpl.MEMORY_MXBEAN_NONHEAP)).thenReturn(_compositeDataNonHeapNode2);
        when(
             _jmxService.getAttribute(NODE3, JMXServiceImpl.MEMORY_MXBEAN,
                                      JMXServiceImpl.MEMORY_MXBEAN_NONHEAP)).thenReturn(_compositeDataNonHeapNode3);
        when(
             _jmxService.getAttribute(NODE1,
                                      JMXServiceImpl.MEMORY_MXBEAN,
                                      JMXServiceImpl.MEMORY_MXBEAN_OBJECT_PENDING_FINALIZATION)).thenReturn(_objectsPendingFinalizationNode1);
        when(
             _jmxService.getAttribute(NODE2,
                                      JMXServiceImpl.MEMORY_MXBEAN,
                                      JMXServiceImpl.MEMORY_MXBEAN_OBJECT_PENDING_FINALIZATION)).thenReturn(_objectsPendingFinalizationNode2);
        when(
             _jmxService.getAttribute(NODE3,
                                      JMXServiceImpl.MEMORY_MXBEAN,
                                      JMXServiceImpl.MEMORY_MXBEAN_OBJECT_PENDING_FINALIZATION)).thenReturn(_objectsPendingFinalizationNode3);
        when(
             _jmxService.getAttribute(NODE1, JMXServiceImpl.MEMORY_MXBEAN,
                                      JMXServiceImpl.MEMORY_MXBEAN_VERBOSE)).thenReturn(_objectsPendingFinalizationNode1);
        when(
             _jmxService.getAttribute(NODE2, JMXServiceImpl.MEMORY_MXBEAN,
                                      JMXServiceImpl.MEMORY_MXBEAN_VERBOSE)).thenReturn(_objectsPendingFinalizationNode2);
        when(
             _jmxService.getAttribute(NODE3, JMXServiceImpl.MEMORY_MXBEAN,
                                      JMXServiceImpl.MEMORY_MXBEAN_VERBOSE)).thenReturn(_objectsPendingFinalizationNode3);

        MBeanAttributeValueJaxBeans attributes = _aggregateServiceImpl.getAllAttributeValues(_jmxNodes,
                                                                                             JMXServiceImpl.MEMORY_MXBEAN);

        assertEquals("expected four values for each of two nodes and the local node",
                     12, attributes.mBeanAttributeValueJaxBeans.size());
    }

    @Test
    public void testGetAttributesMetaData() throws Exception {
        setExpectationsForGetAttributesMetaDataTests();
        MBeanAttributeJaxBeans mBeanAttributesInfoJaxBean = _aggregateServiceImpl.getAttributesMetaData(_uriInfo,
                                                                                                        _jmxNodes,
                                                                                                        JMXServiceImpl.JETTY_SERVER_MBEAN);

        assertEquals(1,
                     mBeanAttributesInfoJaxBean.mBeanAttributeJaxBeans.size());
    }

    @Test
    public void testGetAttributesMetaDataWithBeanAvailableOnlyOnOneOfTwoNodes()
                                                                               throws Exception {
        setExpectationsForJmxServiceGetAttributes();
        when(_jmxService.getAttributes(NODE2, JMXServiceImpl.MEMORY_MXBEAN)).thenReturn(null);

        MBeanAttributeJaxBeans mBeanAttributesInfoJaxBean = _aggregateServiceImpl.getAttributesMetaData(_uriInfo,
                                                                                                        _jmxNodes,
                                                                                                        JMXServiceImpl.MEMORY_MXBEAN);

        assertEquals(0,
                     mBeanAttributesInfoJaxBean.mBeanAttributeJaxBeans.size());

    }

    @Test
    public void testGetAttributesMetaDataWithMissingAttributeOnOneNode()
                                                                        throws Exception {
        setExpectationsForGetAttributesMetaDataTests();
        when(
             _jmxService.getAttributes(eq(NODE1),
                                       eq(JMXServiceImpl.JETTY_SERVER_MBEAN))).thenReturn(null);
        MBeanAttributeJaxBeans mBeanAttributesInfoJaxBean = _aggregateServiceImpl.getAttributesMetaData(_uriInfo,
                                                                                                        _jmxNodes,
                                                                                                        JMXServiceImpl.JETTY_SERVER_MBEAN);

        assertEquals(0,
                     mBeanAttributesInfoJaxBean.mBeanAttributeJaxBeans.size());
    }

    @Test
    public void testGetAttributesMetaDataWithMissingIdInObjectName()
                                                                    throws Exception {
        setExpectationsForGetAttributesMetaDataTests();
        when(
             _jmxService.getAttributes(any(String.class),
                                       eq(_jettyServerBeanPrefix))).thenThrow(new InstanceNotFoundException());
        MBeanAttributeJaxBeans mBeanAttributesInfoJaxBean = _aggregateServiceImpl.getAttributesMetaData(_uriInfo,
                                                                                                        _jmxNodes,
                                                                                                        _jettyServerBeanPrefix);

        assertEquals(1,
                     mBeanAttributesInfoJaxBean.mBeanAttributeJaxBeans.size());
    }

    @Test
    public void testGetAttributesMetaDataWithSlightlyDifferentMetadataOnBothNodes()
                                                                                   throws Exception {
        setExpectationsForJmxServiceGetAttributes();
        when(_jmxService.getAttributes(NODE2, JMXServiceImpl.MEMORY_MXBEAN)).thenReturn(prepareMBeanAttributeInfoArrayForMemoryMXBean(false));

        MBeanAttributeJaxBeans mBeanAttributesInfoJaxBean = _aggregateServiceImpl.getAttributesMetaData(_uriInfo,
                                                                                                        _jmxNodes,
                                                                                                        JMXServiceImpl.MEMORY_MXBEAN);

        assertEquals(0,
                     mBeanAttributesInfoJaxBean.mBeanAttributeJaxBeans.size());
    }

    @Test
    public void testGetAttributeValues() throws Exception {
        setExpectationsForJmxServiceGetMemoryAttribute();
        MBeanAttributeValueJaxBeans mBeanAttributeValuesJaxBean = _aggregateServiceImpl.getAttributeValues(_jmxNodes,
                                                                                                           JMXServiceImpl.MEMORY_MXBEAN,
                                                                                                           JMXServiceImpl.MEMORY_MXBEAN_HEAP);
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
    public void testGetAttributeValuesForJettyServerMBeanWithMultipleIds()
                                                                          throws Exception {
        setExpectationsForGetObjectNamesByPrefix();
        when(
             _jmxService.getAttribute(any(String.class),
                                      eq(JMXServiceImpl.JETTY_SERVER_MBEAN),
                                      eq("someAttributeName"))).thenReturn("someValue");
        when(
             _jmxService.getAttribute(any(String.class),
                                      eq(JMXServiceImpl.JETTY_SERVER_MBEANID1),
                                      eq("someAttributeName"))).thenReturn("someOtherValue");
        MBeanAttributeValueJaxBeans mBeanAttributeValuesJaxBean = _aggregateServiceImpl.getAttributeValues(_jmxNodes,
                                                                                                           JMXServiceImpl.JETTY_SERVER_MBEAN,
                                                                                                           "someAttributeName");
        assertEquals(_jmxNodes.size() * _jettyServerObjectNames.size(),
                     mBeanAttributeValuesJaxBean.mBeanAttributeValueJaxBeans.size());
    }

    @Test
    public void testGetObjectNames() throws Exception {
        ObjectName aThirdCommonObjectName = new ObjectName(
                                                           JMXServiceImpl.JETTY_SERVER_MBEAN);
        Set<ObjectName> commonObjectNames = getCommonObjectNameSet();
        commonObjectNames.add(aThirdCommonObjectName);

        addThirdNodeToLocalCloudtide();

        setCommonExpectationsForGetObjectNameTests(commonObjectNames);
        when(_jmxService.getObjectNames(NODE2)).thenReturn(commonObjectNames);

        MBeanShortJaxBeans mBeanShortJaxBeans = _aggregateServiceImpl.getMBeanShortJaxBeans(_uriInfo,
                                                                                            _jmxNodes);

        assertEquals("Expected to get three common mBeanShortJaxBeans as they exist on all nodes",
                     3, mBeanShortJaxBeans.mbeans.size());
    }

    @Test
    public void testGetObjectNamesWithAnAdditionalObjectNameOnOneNode()
                                                                       throws Exception {
        ObjectName onlyOnOneNodeObjectName = new ObjectName(
                                                            JMXServiceImpl.JETTY_SERVER_MBEAN);

        Set<ObjectName> commonObjectNames = getCommonObjectNameSet();
        Set<ObjectName> commonObjectNamesPlusThreading = new HashSet<ObjectName>();
        commonObjectNamesPlusThreading.addAll(commonObjectNames);
        commonObjectNamesPlusThreading.add(onlyOnOneNodeObjectName);

        addThirdNodeToLocalCloudtide();

        setCommonExpectationsForGetObjectNameTests(commonObjectNames);
        when(_jmxService.getObjectNames(NODE2)).thenReturn(commonObjectNamesPlusThreading);

        MBeanShortJaxBeans mBeanShortJaxBeans = _aggregateServiceImpl.getMBeanShortJaxBeans(_uriInfo,
                                                                                            _jmxNodes);

        assertEquals("Expected to get two common mBeanShortJaxBeans as the THREADING_MXBEAN only exists on one node",
                     2, mBeanShortJaxBeans.mbeans.size());
    }

    @Test
    public void testGetOperationsMetaData() throws Exception {
        setDefaultGetOperationsExpectations();

        when(_jmxService.getOperations(NODE3, JMXServiceImpl.MEMORY_MXBEAN)).thenReturn(prepareMBeanOperationInfoArray("operation1",
                                                                                                                       "desc 1"));

        MBeanOperationJaxBeans mBeanOperationsInfoJaxBean = _aggregateServiceImpl.getOperationsMetaData(_uriInfo,
                                                                                                        _jmxNodes,
                                                                                                        JMXServiceImpl.MEMORY_MXBEAN);

        assertEquals(1,
                     mBeanOperationsInfoJaxBean.mBeanOperationJaxBeans.size());
    }

    @Test
    public void testGetOperationsMetaDataForServerBean() throws Exception {
        MBeanOperationInfo[] mBeanOperationInfos = prepareMBeanOperationInfoArray("operation1",
                                                                                  "desc 1");
        setUriInfoExpectations();
        setExpectationsForGetObjectNamesByPrefix();
        when(
             _jmxService.getOperations(any(String.class),
                                       eq(JMXServiceImpl.JETTY_SERVER_MBEAN))).thenReturn(mBeanOperationInfos);
        when(
             _jmxService.getOperations(any(String.class),
                                       eq(JMXServiceImpl.JETTY_SERVER_MBEANID1))).thenReturn(null);

        MBeanOperationJaxBeans mBeanOperationsInfoJaxBean = _aggregateServiceImpl.getOperationsMetaData(_uriInfo,
                                                                                                        _jmxNodes,
                                                                                                        JMXServiceImpl.JETTY_SERVER_MBEAN);

        assertEquals(0,
                     mBeanOperationsInfoJaxBean.mBeanOperationJaxBeans.size());
    }

    @Test
    public void testGetOperationsMetaDataForServerBeanWithMissingId()
                                                                     throws Exception {
        MBeanOperationInfo[] mBeanOperationInfos = prepareMBeanOperationInfoArray("operation1",
                                                                                  "desc 1");
        setUriInfoExpectations();
        setExpectationsForGetObjectNamesByPrefix();
        when(
             _jmxService.getOperations(any(String.class),
                                       eq(_jettyServerBeanPrefix))).thenThrow(new InstanceNotFoundException());
        when(
             _jmxService.getOperations(any(String.class),
                                       eq(JMXServiceImpl.JETTY_SERVER_MBEAN))).thenReturn(mBeanOperationInfos);
        when(
             _jmxService.getOperations(any(String.class),
                                       eq(JMXServiceImpl.JETTY_SERVER_MBEANID1))).thenReturn(mBeanOperationInfos);

        MBeanOperationJaxBeans mBeanOperationsInfoJaxBean = _aggregateServiceImpl.getOperationsMetaData(_uriInfo,
                                                                                                        _jmxNodes,
                                                                                                        _jettyServerBeanPrefix);

        assertEquals(1,
                     mBeanOperationsInfoJaxBean.mBeanOperationJaxBeans.size());
    }

    @Test
    public void testGetOperationsMetaDataWithSlightlyDifferencesOnAThirdNode()
                                                                              throws Exception {
        setDefaultGetOperationsExpectations();
        when(_jmxService.getOperations(NODE3, JMXServiceImpl.MEMORY_MXBEAN)).thenReturn(prepareMBeanOperationInfoArray("operation1",
                                                                                                                       "desc is different"));

        MBeanOperationJaxBeans mBeanOperationsInfoJaxBean = _aggregateServiceImpl.getOperationsMetaData(_uriInfo,
                                                                                                        _jmxNodes,
                                                                                                        JMXServiceImpl.MEMORY_MXBEAN);

        assertEquals(0,
                     mBeanOperationsInfoJaxBean.mBeanOperationJaxBeans.size());
    }

    @Test
    public void testInvokeOperation() throws Exception {
        _aggregateServiceImpl.invokeOperation(_jmxNodes,
                                              JMXServiceImpl.MEMORY_MXBEAN,
                                              "gc");
        verify(_jmxService, times(3)).invoke(any(String.class),
                                             eq(JMXServiceImpl.MEMORY_MXBEAN),
                                             eq("gc"), any(Object[].class),
                                             any(String[].class));
    }

    @Test
    public void testInvokeOperationWithParameters() throws Exception {
        String stringSignature = "java.lang.String";

        Object[] params = new Object[] { "com.intalio", "FINEST" };

        String[] signature = new String[] { stringSignature, stringSignature };

        _aggregateServiceImpl.invokeOperation(_jmxNodes,
                                              JMXServiceImpl.LOGGING_MBEAN,
                                              "setLoggerLevel", params,
                                              signature);
        verify(_jmxService, times(3)).invoke(any(String.class),
                                             eq(JMXServiceImpl.LOGGING_MBEAN),
                                             eq("setLoggerLevel"),
                                             aryEq(params), aryEq(signature));
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
        ObjectName commonToAllNodesObjectName = new ObjectName(
                                                               JMXServiceImpl.MEMORY_MXBEAN);
        ObjectName anotherCommonToAllNodesObjectName = new ObjectName(
                                                                      JMXServiceImpl.THREADING_MXBEAN);
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
                                                                           JMXServiceImpl.MEMORY_MXBEAN_HEAP,
                                                                           "type",
                                                                           "description",
                                                                           isReadable,
                                                                           false,
                                                                           false);
        MBeanAttributeInfo mBeanAttributeInfoNonHeap = new MBeanAttributeInfo(
                                                                              JMXServiceImpl.MEMORY_MXBEAN_NONHEAP,
                                                                              "type",
                                                                              "description",
                                                                              isReadable,
                                                                              false,
                                                                              false);
        MBeanAttributeInfo mBeanAttributeInfoObjectPending = new MBeanAttributeInfo(
                                                                                    JMXServiceImpl.MEMORY_MXBEAN_OBJECT_PENDING_FINALIZATION,
                                                                                    "type",
                                                                                    "description",
                                                                                    isReadable,
                                                                                    false,
                                                                                    false);
        MBeanAttributeInfo mBeanAttributeInfoVerbose = new MBeanAttributeInfo(
                                                                              JMXServiceImpl.MEMORY_MXBEAN_VERBOSE,
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
        when(_jmxService.getObjectNames(NODE1)).thenReturn(commonObjectNames);
        when(_jmxService.getObjectNames(NODE3)).thenReturn(commonObjectNames);
    }

    private void setDefaultGetOperationsExpectations() throws Exception {
        MBeanOperationInfo[] mBeanOperationInfos = prepareMBeanOperationInfoArray("operation1",
                                                                                  "desc 1");
        addThirdNodeToLocalCloudtide();

        setUriInfoExpectations();
        when(_jmxService.getOperations(NODE1, JMXServiceImpl.MEMORY_MXBEAN)).thenReturn(mBeanOperationInfos);
        when(_jmxService.getOperations(NODE2, JMXServiceImpl.MEMORY_MXBEAN)).thenReturn(mBeanOperationInfos);

    }

    private void setExpectationsForGetAttributesMetaDataTests()
                                                               throws Exception {
        MBeanAttributeInfo[] mBeanAttributeInfos = prepareMBeanAttributeInfoArrayForJettyServerMBean();
        setExpectationsForGetObjectNamesByPrefix();
        setUriInfoExpectations();
        when(
             _jmxService.getAttributes(any(String.class),
                                       eq(JMXServiceImpl.JETTY_SERVER_MBEAN))).thenReturn(mBeanAttributeInfos);
        when(
             _jmxService.getAttributes(any(String.class),
                                       eq(JMXServiceImpl.JETTY_SERVER_MBEANID1))).thenReturn(mBeanAttributeInfos);
    }

    private void setExpectationsForGetObjectNamesByPrefix() throws Exception {
        when(
             _jmxService.getObjectNamesByPrefix(any(String.class),
                                                eq(_jettyServerBeanPrefix))).thenReturn(_jettyServerObjectNames);
    }

    private void setExpectationsForJmxServiceGetAttributes() throws Exception {
        MBeanAttributeInfo[] mBeanAttributeInfos = prepareMBeanAttributeInfoArrayForMemoryMXBean(true);

        setUriInfoExpectations();
        when(
             _jmxService.getAttributes(any(String.class),
                                       eq(JMXServiceImpl.MEMORY_MXBEAN))).thenReturn(mBeanAttributeInfos);
    }

    private void setExpectationsForJmxServiceGetMemoryAttribute()
                                                                 throws Exception {
        when(
             _jmxService.getAttribute(NODE1, JMXServiceImpl.MEMORY_MXBEAN,
                                      JMXServiceImpl.MEMORY_MXBEAN_HEAP)).thenReturn(_compositeDataHeapNode1);
        when(
             _jmxService.getAttribute(NODE2, JMXServiceImpl.MEMORY_MXBEAN,
                                      JMXServiceImpl.MEMORY_MXBEAN_HEAP)).thenReturn(_compositeDataHeapNode2);
        when(
             _jmxService.getAttribute(NODE3, JMXServiceImpl.MEMORY_MXBEAN,
                                      JMXServiceImpl.MEMORY_MXBEAN_HEAP)).thenReturn(_compositeDataHeapNode3);
    }

    private void setUriInfoExpectations() throws URISyntaxException {
        when(_uriInfo.getAbsolutePathBuilder()).thenReturn(_uriBuilder);
        when(_uriBuilder.path(any(String.class))).thenReturn(_uriBuilder);
        when(_uriBuilder.build()).thenReturn(new URI("http://testuri/test"));
    }

}

package com.hellblazer.jmx.rest.domain.jaxb.jmx;

import java.net.URI;
import java.util.Set;
import java.util.TreeSet;

import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Operation")
public class MBeanOperationJaxBean implements Comparable<MBeanOperationJaxBean> {
    @XmlElement(name = "Name")
    public String                    operationName;
    @XmlElement(name = "Description")
    public String                    description;
    @XmlElement(name = "Parameter")
    public Set<MBeanParamterJaxBean> parameters;
    @XmlElement(name = "ReturnType")
    public String                    returnType;
    @XmlElement(name = "URL")
    public URI                       url;

    public MBeanOperationJaxBean() {
    }

    public MBeanOperationJaxBean(UriInfo uriInfo,
                                 MBeanOperationInfo mBeanOperationInfo) {
        operationName = mBeanOperationInfo.getName();
        description = mBeanOperationInfo.getDescription();
        parameters = getOperationParameters(mBeanOperationInfo);
        returnType = mBeanOperationInfo.getReturnType();
        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
        uriBuilder.path("/operations/" + operationName);
        url = uriBuilder.build();
    }

    public int compareTo(MBeanOperationJaxBean o) {
        return operationName.compareTo(o.operationName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MBeanOperationJaxBean other = (MBeanOperationJaxBean) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (operationName == null) {
            if (other.operationName != null) {
                return false;
            }
        } else if (!operationName.equals(other.operationName)) {
            return false;
        }
        if (parameters == null) {
            if (other.parameters != null) {
                return false;
            }
        } else if (!parameters.equals(other.parameters)) {
            return false;
        }
        if (returnType == null) {
            if (other.returnType != null) {
                return false;
            }
        } else if (!returnType.equals(other.returnType)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                 + (description == null ? 0 : description.hashCode());
        result = prime * result
                 + (operationName == null ? 0 : operationName.hashCode());
        result = prime * result
                 + (parameters == null ? 0 : parameters.hashCode());
        result = prime * result
                 + (returnType == null ? 0 : returnType.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MBeanOperationJaxBean [operationName=");
        builder.append(operationName);
        builder.append(", description=");
        builder.append(description);
        builder.append(", parameters=");
        builder.append(parameters);
        builder.append(", returnType=");
        builder.append(returnType);
        builder.append("]");
        return builder.toString();
    }

    private Set<MBeanParamterJaxBean> getOperationParameters(MBeanOperationInfo mBeanOperationInfo) {
        MBeanParameterInfo[] mBeanParameterInfos = mBeanOperationInfo.getSignature();
        Set<MBeanParamterJaxBean> parameters = new TreeSet<MBeanParamterJaxBean>();
        for (MBeanParameterInfo mBeanParameterInfo : mBeanParameterInfos) {
            parameters.add(new MBeanParamterJaxBean(
                                                    mBeanParameterInfo.getName(),
                                                    mBeanOperationInfo.getDescription(),
                                                    mBeanParameterInfo.getType()));
        }
        return parameters;
    }

}
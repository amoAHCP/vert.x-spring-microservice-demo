package org.jacpfx.model.common;

/**
 * Created by amo on 27.10.14.
 */
public class ServiceInfo {
    private String serviceName;
    private Operation[] operations;

    public ServiceInfo(String serviceName, Operation ...operations) {
        this.serviceName = serviceName;
        this.operations = operations;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Operation[] getOperations() {
        return operations;
    }

    public void setOperations(Operation[] operations) {
        this.operations = operations;
    }
}

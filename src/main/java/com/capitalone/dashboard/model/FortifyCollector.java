package com.capitalone.dashboard.model;

import com.capitalone.dashboard.model.Collector;
import com.capitalone.dashboard.model.CollectorType;

public class FortifyCollector extends Collector {
    private String fortifyServer = "";

    public String getFortifyServer() {
        return fortifyServer;
    }

    public void setFortifyServer(String server) {
        this.fortifyServer = server;
    }
    
    public static FortifyCollector prototype(String server) {
    	FortifyCollector protoType = new FortifyCollector();
        protoType.setName("Fortify");
        protoType.setCollectorType(CollectorType.StaticSecurityScan);
        protoType.setOnline(true);
        protoType.setEnabled(true);
        protoType.setFortifyServer(server);;
        return protoType;
    }
}

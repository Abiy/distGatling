package com.walmart.store.location;

import com.google.common.collect.Lists;

import com.walmart.store.location.endpoint.v1.Attribute;

import java.util.List;

/**
 *
 */
public class Zone {

    public String version;
    public List<Attribute> template;


    public Zone(String version) {

        this.version = version;
        this.template =  Lists.newArrayList(
                new Attribute("fromTemprature","int",true,"(v > 10 && v <= 100)",null),
                new Attribute("toTemprature","int",true,"(v > 10 && v <= 100)",null) ,
                new Attribute("usageType","String", true, "eval.contains(m,v)", new String[]{"HAZMAT", "TIRE", "DOG"}),
                new Attribute("weight","double",true,"(v > 10.59 && v <= 100.78)",null),
                new Attribute("somefixedSizeString","String",true,"(v.length() > 10 && v.length() <= 15)",null)
        );

    }


}

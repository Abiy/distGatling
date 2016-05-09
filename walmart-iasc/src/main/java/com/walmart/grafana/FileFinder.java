package com.walmart.grafana;

/**
 * Created by walmart
 */
public class FileFinder {
    public String getFile(String fileName){
        return  this.getClass().getResource(fileName).getFile();
    }
}

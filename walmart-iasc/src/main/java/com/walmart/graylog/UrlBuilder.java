package com.walmart.graylog;

import java.net.URI;
import java.net.URL;

/**
 * Created by walmart
 */
public interface UrlBuilder {
    URI apply(String host, String port);
}

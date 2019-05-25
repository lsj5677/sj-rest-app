package com.sj.ws.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component // can be autowired to object to read properties
public class AppProperties {

    @Autowired
    private Environment env;
    
    public String getTokenSecret()
    {
        return env.getProperty("tokenSecret");
    }
}
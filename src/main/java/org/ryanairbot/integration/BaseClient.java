package org.ryanairbot.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

public abstract class BaseClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseClient.class);

    protected RestTemplate template;
    protected String baseUrl;

}

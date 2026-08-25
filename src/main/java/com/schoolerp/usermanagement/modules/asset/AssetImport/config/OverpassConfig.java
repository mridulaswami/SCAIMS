package com.schoolerp.usermanagement.modules.asset.AssetImport.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class OverpassConfig {

    @Bean
    public RestClient overpassRestClient() {
        return RestClient.builder()
               .baseUrl("https://overpass-api.de/api/interpreter")
               // .baseUrl("https://overpass.kumi.systems/api/interpreter")
                .build();
    }

    @Bean
    public ObjectMapper objectJsonMapper() {
        return new ObjectMapper().configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}

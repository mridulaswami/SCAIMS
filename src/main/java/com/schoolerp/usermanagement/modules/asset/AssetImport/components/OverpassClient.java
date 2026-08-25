package com.schoolerp.usermanagement.modules.asset.AssetImport.components;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class OverpassClient {

    private final RestClient restClient;

    public OverpassClient(RestClient overpassRestClient) {
        this.restClient = overpassRestClient;
    }

//    public String fetchRaw(String overpassQl) {
//        String raw =  restClient.post()
//                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
//                .body("data=" + URLEncoder.encode(overpassQl, StandardCharsets.UTF_8))
//                .retrieve()
//                .body(String.class);
//        return raw;
//    }

    public String fetchRaw(String overpassQl) {
        return restClient.post()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("data=" + URLEncoder.encode(overpassQl, StandardCharsets.UTF_8))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    String body = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    log.error("Overpass error {}: {}", res.getStatusCode(), body);
                    throw new IllegalStateException("Overpass request failed: " + res.getStatusCode() + " - " + body);
                })
                .body(String.class);
    }


}

package com.schoolerp.usermanagement.modules.asset.AssetImport.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.schoolerp.usermanagement.modules.asset.AssetImport.dto.OverPassResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class OverpassJsonParser {

    public final ObjectMapper objectMapper;

    public OverpassJsonParser(ObjectMapper objectJsonMapper) {
        this.objectMapper = objectJsonMapper;
    }

    public OverPassResponseDto parse(String raw)  {

        try{
            OverPassResponseDto jsondata =  objectMapper.readValue(raw, OverPassResponseDto.class);
            return jsondata;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse Overpass JSON response", e);
        }
    }

}

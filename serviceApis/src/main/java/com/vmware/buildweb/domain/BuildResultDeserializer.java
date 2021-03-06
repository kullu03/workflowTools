package com.vmware.buildweb.domain;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.vmware.BuildResult;

import java.lang.reflect.Type;

/**
 * Deserializes build result into enum
 */
public class BuildResultDeserializer implements JsonDeserializer<BuildResult> {

    @Override
    public BuildResult deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return BuildResult.fromValue(jsonElement.getAsString());
    }
}

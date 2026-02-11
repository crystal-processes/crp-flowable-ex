package org.crp.flowable.ai.outputConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.crp.flowable.ai.AiException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.ai.converter.StructuredOutputConverter;
import org.springframework.lang.NonNull;

import java.util.HashMap;

public class JsonOutputConverter implements StructuredOutputConverter<JsonNode> {

    static private final Logger LOG = LoggerFactory.getLogger(JsonOutputConverter.class);

    protected final ObjectMapper objectMapper;

    public JsonOutputConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JsonNode convert(@NonNull String text) {
        try {
            if (text.startsWith("```json") && text.endsWith("```")) {
                text = text.substring(7, text.length() - 3);
            }

            return objectMapper.readValue(text, ObjectNode.class);
        } catch (JsonProcessingException e) {
            LOG.error("Unable to parse answer from chatClient.", e);
            throw new AiException("Unable to parse chatClient response to json", e);
        }
    }

    public String getFormat() {
        String raw = "Your response should be in JSON format.\nThe data structure for the JSON should match this Java class: %s\nDo not include any explanations, only provide a RFC8259 compliant JSON response following this format without deviation.\nRemove the ```json markdown surrounding the output including the trailing \"```\".\n";
        return String.format(raw, HashMap.class.getName());
    }
}

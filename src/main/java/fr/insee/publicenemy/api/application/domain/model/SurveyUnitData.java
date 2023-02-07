package fr.insee.publicenemy.api.application.domain.model;

import java.util.*;

public class SurveyUnitData {

    private final Map<String, Object> attributes;

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public SurveyUnitData (List<Map.Entry<String, String>> fields) {
        this.attributes = getAttributesFromFields(fields);
    }

    public void addAttribute(String key, String value) {
        attributes.put(key, value);
    }

    private Map<String, Object> getAttributesFromFields(List<Map.Entry<String, String>> fields) {
        Map<String, Object> attrs = new HashMap<>();
        Map<String, List<String>> fieldsList = new TreeMap<>();
        var sortedFields = fields
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .toList();

        for(Map.Entry<String, String> field : sortedFields) {
            String key = field.getKey();
            String value = field.getValue();

            // if key doesn't end with _1, _2, ... this is a simple attribute
            String regexpList = "_\\d+$";
            if(!key.matches(".*" + regexpList)) {
                attrs.put(key, value);
                continue;
            }

            // Otherwise this is a list, get rid of index in the key name and create/update the list
            key = key.replaceFirst(regexpList, "");
            List<String> values = new ArrayList<>();
            if(fieldsList.containsKey(key)) {
                values = fieldsList.get(key);
            } else {
                fieldsList.put(key, values);
            }
            values.add(value);
        }

        // Inject all list fields to json fields
        attrs.putAll(fieldsList);

        return attrs;
    }
}

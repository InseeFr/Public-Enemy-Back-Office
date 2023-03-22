package fr.insee.publicenemy.api.application.domain.model.surveyunit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SurveyUnitData {

    private final Map<String, ISurveyUnitObjectData> attributes;

    public Map<String, ISurveyUnitObjectData> getAttributes() {
        return attributes;
    }

    public SurveyUnitData (List<Map.Entry<String, String>> fields) {
        this.attributes = getAttributesFromFields(fields);
    }

    /**
     * This method permits to transform fields into attributes. Fields can contain field names as NAME_1, NAME_2, ... with
     * a string value attached to thoses names. The corresponding attribute will be an attribute name like NAME with
     * a list of object data corresponding to the field values string
     * @param fields containging names and values for each field
     * @return attributes map corresponding
     */
    private Map<String, ISurveyUnitObjectData> getAttributesFromFields(List<Map.Entry<String, String>> fields) {
        Map<String, ISurveyUnitObjectData> attrs = new HashMap<>();
        Map<String, SurveyUnitListData> fieldsList = new TreeMap<>();
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
                attrs.put(key, new SurveyUnitStringData(value));
                continue;
            }

            // Otherwise this is a list, get rid of index in the key name and create/update the list
            key = key.replaceFirst(regexpList, "");
            SurveyUnitListData values = new SurveyUnitListData();
            if(fieldsList.containsKey(key)) {
                values = fieldsList.get(key);
            } else {
                fieldsList.put(key, values);
            }
            values.addValue(value);
        }

        // Inject all list fields to json fields
        attrs.putAll(fieldsList);

        return attrs;
    }
}

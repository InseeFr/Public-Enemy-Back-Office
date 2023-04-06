package fr.insee.publicenemy.api.application.domain.model.surveyunit;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Data
@AllArgsConstructor
/**
 * Transform fields (key/value) from a csv file containing survey units data to data attributes for a survey unit
 */
public class SurveyUnitData {

    private final Map<String, ISurveyUnitDataAttributeValue<?>> attributes;

    public SurveyUnitData(List<Map.Entry<String, String>> fields) {
        this.attributes = getAttributesFromFields(fields);
    }

    /**
     * This method permits to transform fields into attributes. Fields can contain field names as NAME_1, NAME_2, ... with
     * a string value attached to those names. The corresponding attribute will be an attribute name like NAME with
     * a list of object data corresponding to the field values string
     *
     * @param fields containing names and values for each field. As fields are coming from a csv file, each name corresponds to a csv header
     *               and each value corresponds to a string value
     * @return attributes map corresponding
     */
    private Map<String, ISurveyUnitDataAttributeValue<?>> getAttributesFromFields(List<Map.Entry<String, String>> fields) {
        Map<String, ISurveyUnitDataAttributeValue<?>> attrs = new HashMap<>();
        /*
         map that will contain attribute name as key and attribute value. The attribute value
         can either be an object or a list of object
        */
        Map<String, SurveyUnitDataAttributeValueList> fieldsList = new TreeMap<>();
        var sortedFields = fields
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .toList();

        for (Map.Entry<String, String> field : sortedFields) {
            String key = field.getKey();
            String value = field.getValue();

            // if key doesn't end with _1, _2, ... this is a simple attribute
            String regexpList = "_\\d+$";
            if (!key.matches(".*" + regexpList)) {
                attrs.put(key, new SurveyUnitDataAttributeValue(value));
                continue;
            }

            // Otherwise this is a list, get rid of index in the key name and create/update the list
            key = key.replaceFirst(regexpList, "");
            SurveyUnitDataAttributeValueList values = new SurveyUnitDataAttributeValueList();
            if (fieldsList.containsKey(key)) {
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

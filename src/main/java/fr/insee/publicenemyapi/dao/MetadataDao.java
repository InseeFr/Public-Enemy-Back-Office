package fr.insee.publicenemyapi.dao;

import fr.insee.publicenemyapi.model.SurveyMetadata;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.sql.SQLException;

public interface MetadataDao {

    /**
     * Create a Data in database
     * @param metadata
     * @throws SQLException
     */
    void createMetadata(SurveyMetadata metadata) throws SQLException;

    /**
     * Update data for a campaign
     * @param metadata
     */
    void updateMetadata(SurveyMetadata metadata) throws SQLException;

    boolean exist(String campaignId);

    public SurveyMetadata findById(String id);
    public JSONObject getMetaDataByCampaignId(String campaignId) throws ParseException;

}

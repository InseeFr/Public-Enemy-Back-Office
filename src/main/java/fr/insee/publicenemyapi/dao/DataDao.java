package fr.insee.publicenemyapi.dao;


import fr.insee.publicenemyapi.model.SurveyData;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.sql.SQLException;



public interface DataDao {

    /**
     * Create a Data in database
     * @param data
     * @throws SQLException
     */
    void createData(SurveyData data) throws SQLException;

    /**
     * Update data for a campaign
     * @param data
     */
    void updateData(SurveyData data) throws SQLException;

    boolean exist(String campaignId);

    public SurveyData findById(String id);

    public JSONObject getDataByCampaignId(String campaignId) throws ParseException;
}

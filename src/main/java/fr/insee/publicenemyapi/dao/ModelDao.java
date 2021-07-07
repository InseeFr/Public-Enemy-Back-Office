package fr.insee.publicenemyapi.dao;

import fr.insee.publicenemyapi.model.SurveyModel;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.sql.SQLException;

public interface ModelDao {

        /**
         * Create a Data in database
         * @param model
         * @throws SQLException
         */
        void createModel(SurveyModel model) throws SQLException;

        /**
         * Update data for a campaign
         * @param model
         */
        void updateModel(SurveyModel model) throws SQLException;

        boolean exist(String campaignId);

        public SurveyModel findById(String id);
        public JSONObject getModelByCampaignId(String campaignId) throws ParseException;

}

package fr.insee.publicenemyapi.dao.impl.jpa;

import fr.insee.publicenemyapi.dao.ModelDao;
import fr.insee.publicenemyapi.model.SurveyModel;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class ModelDaoJpaImpl implements ModelDao {

        @Autowired
        JdbcTemplate jdbcTemplate;
        /**
         * Create a Data in database
         * @param model
         * @throws SQLException
         */
        @Override
        public void createModel(SurveyModel model) throws SQLException {
                String qString = "INSERT INTO model (id, value) VALUES (?, ?)";
                PGobject value = new PGobject();
                value.setType("json");
                value.setValue(model.getValue().toJSONString());
                jdbcTemplate.update(qString, model.getId(), value);

        }

        /**
         * Update data for a campaign
         * @param model
         */
        @Override
        public void updateModel(SurveyModel model) throws SQLException {
                String qString = "UPDATE model SET value = ? WHERE id= ?";
                PGobject value = new PGobject();
                value.setType("json");
                value.setValue(model.getValue().toJSONString());
                jdbcTemplate.update(qString, value, model.getId());

        }

        @Override
        public boolean exist(String campaignId) {
                String qString = "SELECT COUNT(id) FROM model WHERE id=?";
                Long nbRes = jdbcTemplate.queryForObject(qString, new Object[]{campaignId}, Long.class);
                return nbRes>0;
        }

        @Override
        public SurveyModel findById(String id) {
                return null;
        }

        @Override
        public JSONObject getModelByCampaignId(String campaignId) throws ParseException {
                String qString="SELECT value FROM model WHERE id=?";

                PGobject data =  jdbcTemplate.queryForObject(qString, new Object[]{campaignId}, PGobject.class);
                JSONParser parser = new JSONParser();
                return (JSONObject) parser.parse(data.getValue());
        }
}

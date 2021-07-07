package fr.insee.publicenemyapi.dao.impl.jpa;

import fr.insee.publicenemyapi.dao.DataDao;
import fr.insee.publicenemyapi.model.SurveyData;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class DataDaoJpaImpl implements DataDao {

    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     * Create a Data in database
     * @param data
     * @throws SQLException
     */
    @Override
    public void createData(SurveyData data) throws SQLException {
        String qString = "INSERT INTO data (id, value) VALUES (?, ?)";
        PGobject value = new PGobject();
        value.setType("json");
        value.setValue(data.getValue().toJSONString());
        jdbcTemplate.update(qString, data.getId(), value);
    }
    /**
     * Update data for a campaign
     * @param data
     */
    @Override
    public void updateData(SurveyData data) throws SQLException {

        String qString = "UPDATE data SET value = ? WHERE id= ?";
        PGobject value = new PGobject();
        value.setType("json");
        value.setValue(data.getValue().toJSONString());
        jdbcTemplate.update(qString, value, data.getId());
    }

    @Override
    public boolean exist(String campaignId) {
        String qString = "SELECT COUNT(id) FROM data WHERE id=?";
        Long nbRes = jdbcTemplate.queryForObject(qString, new Object[]{campaignId}, Long.class);
        return nbRes>0;
    }

    @Override
    public SurveyData findById(String id) {
        return null;
    }

    @Override
    public JSONObject getDataByCampaignId(String campaignId) throws ParseException {
        String qString="SELECT value FROM data WHERE id=?";
        PGobject data =  jdbcTemplate.queryForObject(qString, new Object[]{campaignId}, PGobject.class);
        JSONParser parser = new JSONParser();
        return (JSONObject) parser.parse(data.getValue());
    }
}

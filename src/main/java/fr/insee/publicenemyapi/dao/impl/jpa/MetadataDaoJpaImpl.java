package fr.insee.publicenemyapi.dao.impl.jpa;

import fr.insee.publicenemyapi.dao.MetadataDao;
import fr.insee.publicenemyapi.model.SurveyMetadata;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class MetadataDaoJpaImpl implements MetadataDao {

    @Autowired
    JdbcTemplate jdbcTemplate;


    /**
     * Create a Data in database
     * @param metadata
     * @throws SQLException
     */
    @Override
    public void createMetadata(SurveyMetadata metadata) throws SQLException {
        String qString = "INSERT INTO metadata (id, value) VALUES (?, ?)";
        PGobject value = new PGobject();
        value.setType("json");
        value.setValue(metadata.getValue().toJSONString());
        jdbcTemplate.update(qString, metadata.getId(), value);

    }

    /**
     * Update data for a campaign
     * @param metadata
     */
    @Override
    public void updateMetadata(SurveyMetadata metadata) throws SQLException {
        String qString = "UPDATE metadata SET value = ? WHERE id= ?";
        PGobject value = new PGobject();
        value.setType("json");
        value.setValue(metadata.getValue().toJSONString());
        jdbcTemplate.update(qString, value, metadata.getId());

    }

    @Override
    public boolean exist(String campaignId) {
        String qString = "SELECT COUNT(id) FROM metadata WHERE id=?";
        Long nbRes = jdbcTemplate.queryForObject(qString, new Object[]{campaignId}, Long.class);
        return nbRes>0;
    }

    @Override
    public SurveyMetadata findById(String id) {
        return null;
    }

    @Override
    public JSONObject getMetaDataByCampaignId(String campaignId) throws ParseException {
        String qString="SELECT value FROM metadata WHERE id=?";
        PGobject data =  jdbcTemplate.queryForObject(qString, new Object[]{campaignId}, PGobject.class);
        JSONParser parser = new JSONParser();
        return (JSONObject) parser.parse(data.getValue());
    }
}

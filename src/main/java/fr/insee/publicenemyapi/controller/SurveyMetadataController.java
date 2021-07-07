package fr.insee.publicenemyapi.controller;

import fr.insee.publicenemyapi.dao.MetadataDao;
import fr.insee.publicenemyapi.dao.ModelDao;
import fr.insee.publicenemyapi.dao.impl.jpa.MetadataDaoJpaImpl;
import fr.insee.publicenemyapi.dao.impl.jpa.ModelDaoJpaImpl;
import fr.insee.publicenemyapi.model.EmptyJsonBody;
import io.swagger.annotations.ApiOperation;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class SurveyMetadataController {
    @Autowired
    MetadataDaoJpaImpl metadataDao;
    private static final Logger LOGGER = LoggerFactory.getLogger(SurveyMetadataController.class);

    @ApiOperation(value = "Get metadata by campaign Id ")
    @GetMapping(path = "/survey/{id}/metadata")
    public ResponseEntity<Object> getData(@PathVariable(value = "id") String id, HttpServletRequest request) {


        JSONObject metadata=null;

        if (!metadataDao.exist(id)) {
            LOGGER.warn("GET metadata for campaign id {} resulting in not found", id);
            return new ResponseEntity<>(new EmptyJsonBody(),HttpStatus.OK);
        } else {
            try {
                metadata = metadataDao.getMetaDataByCampaignId(id);
            } catch (ParseException e) {
                e.printStackTrace();
                return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
            }
            return new ResponseEntity<>(metadata, HttpStatus.OK);

        }
    }
}

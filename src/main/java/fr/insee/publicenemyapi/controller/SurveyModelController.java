package fr.insee.publicenemyapi.controller;

import fr.insee.publicenemyapi.dao.ModelDao;
import fr.insee.publicenemyapi.dao.impl.jpa.ModelDaoJpaImpl;
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
public class SurveyModelController {
    @Autowired
    ModelDaoJpaImpl modelDao;
    private static final Logger LOGGER = LoggerFactory.getLogger(SurveyModelController.class);

    @ApiOperation(value = "Get model by campaign Id ")
    @GetMapping(path = "/survey/{id}/model")
    public ResponseEntity<Object> getData(@PathVariable(value = "id") String id, HttpServletRequest request) {



    JSONObject model=null;

            if (!modelDao.exist(id)) {
        LOGGER.error("GET model for reporting unit with id {} resulting in 404", id);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
                try {
                    model = modelDao.getModelByCampaignId(id);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
                }
                return new ResponseEntity<>(model, HttpStatus.OK);
            }
    }
}

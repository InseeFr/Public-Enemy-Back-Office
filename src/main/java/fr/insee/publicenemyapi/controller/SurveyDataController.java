    package fr.insee.publicenemyapi.controller;

    import fr.insee.publicenemyapi.dao.DataDao;
    import fr.insee.publicenemyapi.dao.ModelDao;
    import fr.insee.publicenemyapi.dao.impl.jpa.DataDaoJpaImpl;
    import fr.insee.publicenemyapi.dao.impl.jpa.ModelDaoJpaImpl;
    import fr.insee.publicenemyapi.model.SurveyData;
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
    import java.util.Optional;

    @RestController
    public class SurveyDataController {
        @Autowired
        DataDaoJpaImpl dataDao;
        private static final Logger LOGGER = LoggerFactory.getLogger(SurveyDataController.class);

        @ApiOperation(value = "Get data by campaign Id ")
        @GetMapping(path = "/survey/{id}/data")
        public ResponseEntity<Object> getData(@PathVariable(value = "id") String id, HttpServletRequest request) {


            JSONObject data=null;

            if (!dataDao.exist(id)) {
                LOGGER.error("GET data for campaign id {} resulting in 404", id);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                try {
                    data = dataDao.getDataByCampaignId(id);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
                }
                return new ResponseEntity<>(data, HttpStatus.OK);

            }
        }
    }


package fr.insee.publicenemyapi.services;

import fr.insee.publicenemyapi.dao.impl.jpa.DataDaoJpaImpl;
import fr.insee.publicenemyapi.dao.impl.jpa.MetadataDaoJpaImpl;
import fr.insee.publicenemyapi.dao.impl.jpa.ModelDaoJpaImpl;
import fr.insee.publicenemyapi.model.SurveyData;
import fr.insee.publicenemyapi.model.SurveyMetadata;
import fr.insee.publicenemyapi.model.SurveyModel;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Service
public class SurveyItemsServices {
    @Autowired
    ModelDaoJpaImpl modelDao;
    @Autowired
    DataDaoJpaImpl dataDao;
    @Autowired
    MetadataDaoJpaImpl metadataDao;

    public void persistSurveyItems(String survey,MultipartFile model,MultipartFile data, MultipartFile metadata, String mode) throws SQLException, IOException, ParseException {

        InputStream isModel = model.getInputStream();
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonModel = (JSONObject)jsonParser.parse(
                new InputStreamReader(isModel, "UTF-8"));

        InputStream isData = data.getInputStream();
        JSONObject jsonData = (JSONObject)jsonParser.parse(
                new InputStreamReader(isData, "UTF-8"));

        JSONObject jsonMetadata=null;
        if(null!=metadata) {
            InputStream isMetadata = metadata.getInputStream();

            jsonMetadata = (JSONObject) jsonParser.parse(
                    new InputStreamReader(isMetadata, "UTF-8"));
        }

        SurveyModel sModel =new SurveyModel(survey, jsonModel);
        SurveyData sData = new SurveyData(survey, jsonData);
        SurveyMetadata sMetadata=null;
        if(null!=metadata) {
            sMetadata = new SurveyMetadata(survey, jsonMetadata);
        }


        if(modelDao.exist(survey))
        {
            modelDao.updateModel(sModel);
        }else{
            modelDao.createModel(sModel);
        }

        if(dataDao.exist(survey))
        {
            dataDao.updateData(sData);
        }else{
            dataDao.createData(sData);
        }

        if(null!= metadata && mode=="cawi" && metadataDao.exist(survey))
        {
            metadataDao.updateMetadata(sMetadata);
        }else{
            if(null!=metadata) {
                metadataDao.createMetadata(sMetadata);
            }
        }


    }

    public String buildVisualizationUrl(String domain, String survey, String mode)
    {
        String url = domain+"?questionnaire={modelUrl}&data={dataUrl}";
        if (mode=="cawi")
        {
            url = url.concat("&metadata={metadataUrl}");
        }
// URI (URL) parameters
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("modelUrl", "http://localhost:8080/survey/"+survey+"/model");
        urlParams.put("dataUrl", "http://localhost:8080/survey/"+survey+"/data");
      if(mode=="cawi"){
          urlParams.put("metadataUrl", "http://localhost:8080/survey/"+survey+"/metadata");

      }

        URI uri = UriComponentsBuilder.fromUriString(url)
                .buildAndExpand(urlParams)
                .toUri();
        return uri.toString();
    }
}

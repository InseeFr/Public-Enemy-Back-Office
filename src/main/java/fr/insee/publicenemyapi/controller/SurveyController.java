package fr.insee.publicenemyapi.controller;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;

import fr.insee.publicenemyapi.PublicEnemyApiApplication;
import fr.insee.publicenemyapi.config.ApplicationContext;
import fr.insee.publicenemyapi.dao.DataDao;
import fr.insee.publicenemyapi.dao.MetadataDao;
import fr.insee.publicenemyapi.dao.ModelDao;
import fr.insee.publicenemyapi.dao.impl.jpa.DataDaoJpaImpl;
import fr.insee.publicenemyapi.dao.impl.jpa.MetadataDaoJpaImpl;
import fr.insee.publicenemyapi.dao.impl.jpa.ModelDaoJpaImpl;
import fr.insee.publicenemyapi.model.SurveyData;
import fr.insee.publicenemyapi.model.SurveyMetadata;
import fr.insee.publicenemyapi.model.SurveyModel;
import fr.insee.publicenemyapi.services.SurveyItemsServices;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.publicenemyapi.services.SurveyServices;
import org.springframework.web.util.UriComponentsBuilder;

@RestController

public class SurveyController {

	@Autowired
	SurveyServices qs;

	@Autowired
	SurveyItemsServices itemsService;

	public AnnotationConfigApplicationContext context;


	@RequestMapping(value = "/survey-papi", produces = MediaType.APPLICATION_OCTET_STREAM, consumes = MediaType.MULTIPART_FORM_DATA, method = {
			RequestMethod.POST })
	public ResponseEntity<ByteArrayResource> sendPdf(@RequestParam("survey") String format,
			@RequestParam("data") MultipartFile data, @RequestParam("model") MultipartFile model,
			HttpServletRequest request) throws IOException {

		String modelFileName = model.getOriginalFilename();
		String dataFileName = data.getOriginalFilename();

		InputStream model2 = new BufferedInputStream(model.getInputStream());
		InputStream data2 = new BufferedInputStream(data.getInputStream());

		Path path = Paths.get(qs.publiPostage(model2, modelFileName, data2, dataFileName).getAbsolutePath());
		ByteArrayResource pdfFile = new ByteArrayResource(Files.readAllBytes(path));

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + path.getFileName().toString())
				.contentLength(pdfFile.contentLength()).body(pdfFile);

	}

	@RequestMapping(value = "/perso-papi", produces = MediaType.APPLICATION_OCTET_STREAM, consumes = MediaType.MULTIPART_FORM_DATA, method = {
			RequestMethod.POST })
	public ResponseEntity<ByteArrayResource> sendPersoFile(@RequestParam("survey") String enquete,
			@RequestParam("model") MultipartFile model, HttpServletRequest request) throws Exception {

		String modelFileName = model.getOriginalFilename();

		InputStream model2 = new BufferedInputStream(model.getInputStream());

		Path path = Paths.get(qs.personalisationFile(model2, modelFileName).getAbsolutePath());
		ByteArrayResource xmlPersoFile = new ByteArrayResource(Files.readAllBytes(path));

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + path.getFileName().toString())
				.contentLength(xmlPersoFile.contentLength()).body(xmlPersoFile);

	}

	@RequestMapping(value = "/perso-cawi-capi", produces = MediaType.APPLICATION_OCTET_STREAM, consumes = MediaType.MULTIPART_FORM_DATA, method = {
			RequestMethod.POST })
	public ResponseEntity<ByteArrayResource> sendPersoFileWeb(@RequestParam("survey") String enquete,
			@RequestParam("model") MultipartFile model, HttpServletRequest request) throws Exception {

		String modelFileName = model.getOriginalFilename();

		InputStream model2 = new BufferedInputStream(model.getInputStream());

		Path path = Paths.get(qs.personalisationFileWeb(model2, modelFileName).getAbsolutePath());
		ByteArrayResource xmlPersoFile = new ByteArrayResource(Files.readAllBytes(path));

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + path.getFileName().toString())
				.contentLength(xmlPersoFile.contentLength()).body(xmlPersoFile);

	}

	@RequestMapping(value = "/survey-cawi-capi", consumes = MediaType.MULTIPART_FORM_DATA, method = { RequestMethod.POST })
	public @ResponseBody String sendUrl(@RequestParam("survey") String survey, @RequestParam("data") MultipartFile data,
			@RequestParam("model") MultipartFile model) throws Exception {

		String modelFileName = model.getOriginalFilename();
		String dataFileName = data.getOriginalFilename();

		InputStream model2 = new DataInputStream(model.getInputStream());
		InputStream data2 = new DataInputStream(data.getInputStream());

		return (qs.publiPostageWeb(survey, model2, modelFileName, data2, dataFileName));

	}

	@RequestMapping(value = "/survey-cawiv2", consumes = MediaType.MULTIPART_FORM_DATA, method = { RequestMethod.POST })
	public @ResponseBody String sendUrlCawi(@RequestParam("survey") String survey, @RequestParam("data") MultipartFile data,
										@RequestParam("model") MultipartFile model, @RequestParam("metadata") MultipartFile metadata) throws Exception {


		itemsService.persistSurveyItems(survey,model,data,metadata,"cawi");
		String stromaeVisualization=null;
		try (InputStream input = PublicEnemyApiApplication.class.getClassLoader()
				.getResourceAsStream("application.properties")) {

			Properties prop = new Properties();

			// load a properties file
			prop.load(input);
			stromaeVisualization = prop.getProperty("fr.insee.publicenemyapi.services.visualize.stromae");
		}catch (IOException ex) {
			ex.printStackTrace();
		}

		return itemsService.buildVisualizationUrl(stromaeVisualization,survey,"cawi");

	}

	@RequestMapping(value = "/survey-capi", consumes = MediaType.MULTIPART_FORM_DATA, method = { RequestMethod.POST })
	public @ResponseBody String sendUrlCapi(@RequestParam("survey") String survey, @RequestParam("data") MultipartFile data,
										@RequestParam("model") MultipartFile model) throws Exception {


		itemsService.persistSurveyItems(survey,model,data,null,"capi");
		String queenVisualization=null;
		try (InputStream input = PublicEnemyApiApplication.class.getClassLoader()
				.getResourceAsStream("application.properties")) {

			Properties prop = new Properties();

			// load a properties file
			prop.load(input);
			queenVisualization = prop.getProperty("fr.insee.publicenemyapi.services.visualize.queen");
		}catch (IOException ex) {
			ex.printStackTrace();
		}
		return itemsService.buildVisualizationUrl(queenVisualization,survey,"capi");

	}

}

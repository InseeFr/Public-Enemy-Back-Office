package fr.insee.publicenemyapi.services;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.publicenemyapi.PublicEnemyApiApplication;
import fr.insee.publicenemyapi.pdf.Fo2PDFGenerator;
import fr.insee.publicenemyapi.pdf.PDFGenerator;
import fr.insee.publicenemyapi.util.xsl.XSLTransform;
import fr.insee.publicenemyapi.vtl.FOPublispostGenerator;
import fr.insee.publicenemyapi.vtl.PublipostGenerator;

@Service
public class SurveyServices {



	final static Logger logger = LogManager.getLogger(SurveyServices.class);

	private PDFGenerator pdfGenerator;

	private PublipostGenerator publipostageGenerator;

	public File publiPostage(InputStream model, String modelFileName, InputStream data, String dataFileName)
			throws IOException {

		publipostageGenerator = new FOPublispostGenerator();
		publipostageGenerator.initializeGenerator(model, modelFileName);
		pdfGenerator = new Fo2PDFGenerator();
		pdfGenerator.initializeGenerator();

		try {

			InputStream foUPdate = publipostageGenerator.generateDocument(data, dataFileName);
			File out = new File(pdfGenerator.generatePDF(foUPdate, modelFileName));

			return out;

		} catch (Exception e) {

			logger.error(e.getMessage(), e);
			throw e;
		}

	}

	public File personalisationFile(InputStream model, String modelFileName) throws Exception {

		String XSLT_FILE_PERSO = "xsl/fo_to_perso.xsl";

		File outFile = File.createTempFile("temp", "outPerso.xml");
		// new output stream via buffered method
		FileOutputStream out = new FileOutputStream(outFile);
		OutputStream outPersoFile = new BufferedOutputStream(out);
		XSLTransform transformation = new XSLTransform();

		try {

			transformation.transform(model, outPersoFile, XSLT_FILE_PERSO);
			out.close();
			File outputFile = new File(outFile.getAbsolutePath());

			return outputFile;

		} catch (Exception e) {

			logger.error(e.getMessage(), e);
			throw e;
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException ioe) {
				System.out.println("Error in closing the Stream");
			}

		}
	}

	public File personalisationFileWeb(InputStream model, String modelFileName) throws Exception {

		String XSLT_FILE_PERSO_WEB = "xsl/fo_to_perso_web.xsl";

		File outFile = File.createTempFile("temp", "outPersoWeb.xml");
		// new output stream via buffered method
		FileOutputStream out = new FileOutputStream(outFile);
		OutputStream outPersoFile = new BufferedOutputStream(out);
		XSLTransform transformation = new XSLTransform();

		try {

			transformation.transform(model, outPersoFile, XSLT_FILE_PERSO_WEB);
			out.close();
			File outputFile = new File(outFile.getAbsolutePath());

			return outputFile;

		} catch (Exception e) {

			logger.error(e.getMessage(), e);
			throw e;
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException ioe) {
				System.out.println("Error in closing the Stream");
			}

		}

	}

	public String publiPostageWeb(String survey, InputStream model, String modelFileName, InputStream data,
			String dataFileName) throws Exception {

		String urlResponse = "";
		String stromaeModel = "";
		String stromaeData = "";
		String stromaeUserName = "";
		String stromaePassword = "";
		String stromaeDataColl = "";

		try (InputStream input = PublicEnemyApiApplication.class.getClassLoader()
				.getResourceAsStream("application.properties")) {

			Properties prop = new Properties();

			// load a properties file
			prop.load(input);

			stromaeModel = prop.getProperty("fr.insee.publicenemyapi.services.stromae.url.model");
			stromaeData = prop.getProperty("fr.insee.publicenemyapi.services.stromae.url.data");
			stromaeUserName = prop.getProperty("fr.insee.publicenemyapi.services.stromae.username");
			stromaePassword = prop.getProperty("fr.insee.publicenemyapi.services.stromae.password");
			stromaeDataColl = prop.getProperty("fr.insee.publicenemyapi.services.stromae.datacoll");

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		// bloc d'authentif
		String authString = stromaeUserName + ":" + stromaePassword;
		byte[] authBytes = authString.getBytes(StandardCharsets.UTF_8);
		String encodedAuthString = Base64.getEncoder().encodeToString(authBytes);

		// conversion des fichiers en String utf-8
		byte[] bytesModel = IOUtils.toByteArray(model);
		ByteArrayOutputStream parmsModel = new ByteArrayOutputStream();
		parmsModel.write(bytesModel);
		String codeModel = parmsModel.toString(StandardCharsets.UTF_8.name());

		byte[] bytesData = IOUtils.toByteArray(data);
		ByteArrayOutputStream parmsData = new ByteArrayOutputStream();
		parmsData.write(bytesData);
		String codeData = parmsData.toString(StandardCharsets.UTF_8.name());

		RestTemplate stromaeClient = new RestTemplate();
		String stromaeClientUrlModel = stromaeModel + stromaeDataColl + "/" + survey.toLowerCase();
		

		HttpHeaders headers = new HttpHeaders();

		headers.add("Content-Type", MediaType.APPLICATION_XML_VALUE);
		headers.add("Authorization", "Basic " + encodedAuthString);
		headers.add("Accept", MediaType.APPLICATION_XML_VALUE);

		stromaeClient.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));

		HttpEntity<String> entityModel = new HttpEntity<String>(codeModel, headers);
		HttpEntity<String> entityData = new HttpEntity<String>(codeData, headers);

		String stromaeClientUrlData = stromaeData + stromaeDataColl + "/" + survey.toLowerCase();
		

		ResponseEntity<String> response;

		try {
			response = stromaeClient.postForEntity(stromaeClientUrlModel, entityModel, String.class);
		} catch (HttpStatusCodeException e) {
			response = ResponseEntity.status(e.getRawStatusCode()).headers(e.getResponseHeaders())
					.body(e.getResponseBodyAsString());
		}

		try {
			response = stromaeClient.postForEntity(stromaeClientUrlData, entityData, String.class);
			urlResponse = response.getBody();

		} catch (HttpStatusCodeException e) {
			response = ResponseEntity.status(e.getRawStatusCode()).headers(e.getResponseHeaders())
					.body(e.getResponseBodyAsString());
		}

		return urlResponse;

	}

	public String urlPostage(String format, MultipartFile data, MultipartFile model) {
		return "http://www.insee.fr";
	}

}

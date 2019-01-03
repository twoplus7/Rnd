
package com.example;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.http.HttpStatus;
import org.json.simple.*;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageSource;
import com.google.protobuf.ByteString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.apache.log4j.Logger;

@SuppressWarnings("unused")
@RestController
public class VisionController {
	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private ImageAnnotatorClient imageAnnotatorClient;

	Logger logger = Logger.getLogger(VisionController.class);
	JSONObject json = new JSONObject();
	JSONArray gVisionArray = new JSONArray();
	String imageUrl;

	@SuppressWarnings({ "unchecked", "static-access" })
	@RequestMapping("/vision")
	public Vision vision(String imageUrl) throws Exception {

		String imgIndex = imageUrl.substring(0, 4);
		String http1 = "http";
		ByteString imageBytes;
		if (imgIndex.equals(http1)) {
			URL url = new URL(imageUrl);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("User-Agent", "Firefox");
			try (InputStream inputStream = conn.getInputStream()) {
				int n = 0;
				byte[] buffer = new byte[1024];
				while (-1 != (n = inputStream.read(buffer))) {
					baos.write(buffer, 0, n);
				}
			}

			byte[] img = baos.toByteArray();
			imageBytes = ByteString.copyFrom(img);

		} else {
			imageBytes = ByteString.readFrom(new FileInputStream(imageUrl));
		}
		BatchAnnotateImagesResponse responses;

		Image image = Image.newBuilder().setContent(imageBytes).build();

		// Sets the type of request to label detection, to detect broad sets of
		// categories in an image.
		Feature feature = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
		AnnotateImageRequest request = AnnotateImageRequest.newBuilder().setImage(image).addFeatures(feature).build();
		logger.info("AnnotateImageRequest is builded");
		responses = this.imageAnnotatorClient.batchAnnotateImages(Collections.singletonList(request));

		StringBuilder responseBuilder = new StringBuilder("<table border=\"1\">");
		responseBuilder.append("<tr><th>Image URL</th><td>" + imageUrl
				+ "</td></tr><tr><th>description</th><th>score</th><th>col1</th></tr>");

		// We're only expecting one response.

		if (responses.getResponsesCount() == 1) {
			AnnotateImageResponse response = responses.getResponses(0);

			if (response.hasError()) {
				logger.info("AnnotateImageResponse Has an Error");
				throw new Exception(response.getError().getMessage());
			}

			for (EntityAnnotation annotation : response.getLabelAnnotationsList()) {
				responseBuilder.append("<tr><td>").append(annotation.getDescription()).append("</td><td>")
						.append(annotation.getScore()).append("</td><td>").append(annotation.getDescriptor())
						.append("</td></tr>");
			}
		}

		responseBuilder.append("</table>");

		AnnotateImageResponse response = responses.getResponses(0);
		System.out.println("Response is " + response);

		for (EntityAnnotation annotation : response.getLabelAnnotationsList()) {
			json.put("Description", annotation.getDescription());
			json.put("score", annotation.getScore());
			json.put("Locale", annotation.getLocale());
			json.put("Mid", annotation.getMid());
			json.put("Confidence", annotation.getConfidence());
			json.put("Topicality", annotation.getTopicality());
			gVisionArray.add(json);
		}
		logger.info("Output Converted into json");
		System.out.println("Json Array" + gVisionArray);

		responseBuilder.append("</br><p>");
		responseBuilder.append("<img alt=\"image\" src='" + imageUrl + "'/><p>");
		//System.out.println("ResponseBuilder toString() " + responseBuilder.toString());
		String respBuilder = new String();
		respBuilder = responseBuilder.toString();
		logger.info("VisionAPI Builded");
		return new Vision(gVisionArray, imageUrl);

	}

}

package com.testing;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ValueMatchingStrategy;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestingApplicationTests {
	
	String stringResponse;
	HttpResponse httpResponse;
	WireMockServer wireMock;
	
	/**
	 * We can use a Rule to essentially manage i.e start amd stop our wire mock server
	 * An alternative is to do this programmatically.
	 * This means you will explicitly need to instantiate the WireMock server
	 */
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(7070);
	
	@Rule
	public WireMockRule wireMockRule2 = new WireMockRule(7071);
	
	/**
	 * We create our stubs first
	 */
	@BeforeClass
	public void generateStubForWireMockService() {
		stubFor(get(urlPathMatching("/metadatas/.*")).
				willReturn(aResponse().
						withStatus(200).
						withHeader("Content-Type", "application/json").
						withBody("")));
		stubFor(
				get(urlEqualTo("/metadatas2")).
				willReturn(aResponse().
						withBody("This is a test call")));
		stubFor(post(urlEqualTo("/texts")).
				withRequestBody(equalToJson("{ \"houseNumber\": 4, \"postcode\": \"N1 1ZZ\" }")).
				willReturn(aResponse().
						withBody("")));
		stubFor(get(urlPathEqualTo("/metadatas/invalidHeader")) //stubbing invalid header and a 503 response
				  .withHeader("Accept", matching("text/.*"))
				  .willReturn(aResponse()
				  .withStatus(503)
				  .withHeader("Content-Type", "text/html")
				  .withBody("!!! Service Unavailable !!!")));
				
		
	}
	
	/**
	 * 
	 * @param url
	 */
	public void helperMethod(String url) {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpGet request = new HttpGet(url);

		try {
			httpResponse = httpClient.execute(request);
			stringResponse = convertHttpResponseToString(httpResponse);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

//	@Before
//	public void startServer() {
//		wireMock = new WireMockServer(8080);
//		wireMock.start();
//		configureFor("localhost", 8080);
//	}
	
	/**
	 * In this test, we will create a http client and then execute a request and receive a response
	 * Illustrating HTTP interaction
	 * @throws IOException 
	 */
	@Test
	public void checkReturendeBody() throws IOException {				
		helperMethod("http://localhost:8080/metadatas");
		//tests
		verify(getRequestedFor(urlEqualTo("/http://localhost:7071/metadatas")));
		assertEquals(200, httpResponse.getStatusLine().getStatusCode());
		assertEquals("application/json", httpResponse.getFirstHeader("Content-Type").getValue());
		assertEquals("\"testing-library\": \"WireMock\"", stringResponse);
	}
	
	/**
	 * Mocking the case where the header is invalid
	 * Again we use a set of helper methods
	 * @throws IOException 
	 */
	@Test
	public void testingForInvalidHeader() throws IOException {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpGet request = new HttpGet("http://localhost/metadatas/invalidHeader");
		request.addHeader("Accept", "text/html");
		HttpResponse httpResponse = httpClient.execute(request);
		String stringResponse = convertHttpResponseToString(httpResponse);
		
		//perform assertion
		verify(getRequestedFor(urlEqualTo("/baeldung/wiremock")));
		assertEquals(503, httpResponse.getStatusLine().getStatusCode());
		assertEquals("text/html", httpResponse.getFirstHeader("Content-Type").getValue());
		assertEquals("!!! Service Unavailable !!!", stringResponse);

	}
	
	private String convertHttpResponseToString(HttpResponse httpResponse) throws IOException {
	    InputStream inputStream = httpResponse.getEntity().getContent();
	    return convertInputStreamToString(inputStream);
	}
	
	private String convertInputStreamToString(InputStream inputStream) {
	    Scanner scanner = new Scanner(inputStream, "UTF-8");
	    String string = scanner.useDelimiter("\\Z").next();
	    scanner.close();
	    return string;
	}
	
	
	@Test
	public void verifyThatURLApiCallIsMade() {
		verify(getRequestedFor(urlEqualTo("/metadatas")));
		assertEquals("Body", "some response string");
	}
	
	@After
	public void stopServer() {
		wireMock.stop();
	}
}

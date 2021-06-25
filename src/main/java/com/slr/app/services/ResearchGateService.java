package com.slr.app.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slr.app.helpers.ResearchGateWebElements;
import com.slr.app.models.Publications;

@Service 
public class ResearchGateService {

	private static final Logger LOG = LogManager.getLogger(ResearchGateService.class);
	
	@Autowired
	private PublicationsServices publication_service;
	
	public static int CANTIDAD_RESULTADOS = 1;
	// "doi", "title"
	private String updated_type;
	
	public void test(Publications publication) throws InterruptedException {
		WebDriver driver = setup();
		ResearchGateWebElements e = new ResearchGateWebElements();
		
		
		String title = publication.getTitle();
		title = title.endsWith(".") ? title.substring(0, title.length()-1) :title;
		String url = "https://www.researchgate.net/search.Search.html?type=publication&query=";
		String doi = publication.hasDoi() ? publication.extractDOI() : "";
		
		driver.get(url);
		WebElement input_form = driver
				.findElement(By.cssSelector("div[class='search-container__form-container search-container__form-container--tabs'] form"))
				.findElement(By.cssSelector("input[class='search-container__form-input']"));
		
		
		if(!doi.isEmpty())//publicacion tiene DOI con formato https://doi.org/..
		{
			input_form.sendKeys(doi);
			Thread.sleep(2000);
			input_form.sendKeys(Keys.RETURN);
			Thread.sleep(2000);
			
			e.obtainPublicationAbstract(driver);
			
		}
		driver.close();
		driver.quit();
	}
	public Publications researchgateUpdatePublications(Publications publication) {
		
		WebDriver driver = setup();
		try {
			LOG.info("Publication id :"+publication.getId());
			JSONArray array = beginScrapPublicationFromResearchGate(publication, driver);
			Thread.sleep(3000);
			
			
			for(int i=0;i<array.length();i++)
			{
				JSONObject json = array.getJSONObject(i);
				json.put("publication_id", publication.getId());
				
				System.out.println(json.toString());
				
				
			}
			
			driver.close();
			driver.quit();
			
		}//catch (JSONException | InterruptedException | NullPointerException e) {
		catch(Exception e) {
			LOG.error("Function : researchgateUpdatePublications(), "+e.getMessage(),e);
		}

		return null;
	}
	
	/*
	 * Función que obtiene la data desde researchgate
	 * busca en base a DOI o titulo de una publicación
	 * @return: JSONArray con la data extraida
	 */
	public JSONArray beginScrapPublicationFromResearchGate(Publications publication,WebDriver driver) 
	{
		JSONArray response = new JSONArray();
		
		String title = publication.getTitle();
		title = title.endsWith(".") ? title.substring(0, title.length()-1) :title;
		String url = "https://www.researchgate.net/search.Search.html?type=publication&query=";
		String doi = publication.hasDoi() ? publication.extractDOI() : "";
		
		try 
		{
			driver.get(url);
			WebElement input_form = driver
					.findElement(By.cssSelector("div[class='search-container__form-container search-container__form-container--tabs'] form"))
					.findElement(By.cssSelector("input[class='search-container__form-input']"));
			
			
			if(!doi.isEmpty())//publicacion tiene DOI con formato https://doi.org/..
			{
				input_form.sendKeys(doi);
				Thread.sleep(2000);
				input_form.sendKeys(Keys.RETURN);
				Thread.sleep(2000);
				
				WebElement div_content = driver.findElement(By.cssSelector("div[id='content']"))
						.findElement(By.xpath("./div[@class='react-container']/div"));
				
				String div_class = div_content.getAttribute("class");
				
				switch (div_class) 
				{
					//no hay coincidencia por doi, entonces busca por title
					case "search":
						setUpdated_type("title");
						LOG.info("Search researchgate publication by title: "+title);
						System.out.println("Search researchgate publication by title: "+title);
						Thread.sleep(2000);
						response = searchResearchGatePublicationByTitle(title, driver);
					break;
					//encontro busqueda por DOI del documento
					case "research-detail":
						setUpdated_type("doi");
						LOG.info("Search researchgate publication by DOI: "+doi);
						System.out.println("Search researchgate publication by DOI: "+doi);
						ResearchGateWebElements rg = new ResearchGateWebElements();
						
						JSONObject rg_data = rg.obtainReseachGatePublicationData(driver);
						Thread.sleep(2000);
						rg_data.put("document_abstract", rg.obtainPublicationAbstract(driver));
						
						response.put(rg_data);
						
						//inicio busqueda de información del autor de la publicaion
						for (int i = 0; i < response.length(); i++) 
						{
							JSONObject obj = response.getJSONObject(i);
							JSONArray  array = obj.getJSONArray("document_authors");
							
							for(int j = 0; j < array.length(); j++) {
								
								JSONObject json = array.getJSONObject(j);
								JSONObject affiliation = new JSONObject();
								
								if(json.getBoolean("has_profile")){
									affiliation = obtainDisciplineSkillAffiliation(json.getString("href"), driver);
								}
								
								json.put("affiliation_data", affiliation);
							}	
							
						}
					break;
					
					default :
						LOG.error("Error en la busqueda del elemento, "+div_class+" "+div_content.getTagName());
						break;
				}
				
				
			}
			else {
				setUpdated_type("title");
				response = searchResearchGatePublicationByTitle(title, driver); 
			}
			
		}// catch (JSONException | NullPointerException | InterruptedException e) {
		catch (Exception e) {
			LOG.error("Function scrapPublicationFromResearchGate(), "+e.getMessage(),e);
			System.err.println("Function scrapPublicationFromResearchGate(), "+e.getMessage());
		}
		
		return response;
		
	}
	
	/* 
	 * funcion que obtiene los skills,disciplines y affiliation de un profile en researchgate
	 {
	    "skills": [],
	    "country": " Argentina",
	    "institution_url": "https://www.researchgate.net/institution/Austral_University_Argentina",
	    "disciplines": [ ],
	    "department_url": "https://www.researchgate.net/institution/Austral_University_Argentina/department/Facultad_de_Ingenieria",
	    "position": "Research Assistant",
	    "region": "Buenos Aires",
	    "department": "Facultad de Ingeniería",
	    "institution_name": "Austral University (Argentina)"
	 }
	 
	 */
	public JSONObject obtainDisciplineSkillAffiliation(String url, WebDriver driver) throws InterruptedException
	{
		JSONObject response = new JSONObject();
		Map< String, List<String> > res = new HashMap<>();
		ResearchGateWebElements rg = new ResearchGateWebElements();
		driver.get(url);
		Thread.sleep(3000);
		
		try 
		{
			WebElement menu = driver.findElement(By.cssSelector("div[class='profile-wrapper']"))
									.findElement(By.cssSelector("div[class='profile-menu-new'] > nav"));
			List<WebElement> overview_options = menu.findElements(By.cssSelector("div[class='nova-c-nav__items'] > a"));
			WebElement over_view = rg.findInProfileOptions(overview_options, "Overview");
			
			if( Objects.nonNull(over_view) ) 
			{
				WebElement top_box = driver.findElement(By.cssSelector("[class='profile-overview__box-top'] *"));
			
				//lista box profile researcher
				List<WebElement> about = top_box.findElements(By.cssSelector("[class='nova-o-stack nova-o-stack--gutter-xl nova-o-stack--spacing-none nova-o-stack--no-gutter-outside'] > div"));
				WebElement about_box = rg.findWebElementByAttribute("class", "nova-o-stack__item", about);
				
				if(Objects.nonNull( about_box ) )//existe about
				{
					res = rg.obtainProfileDisciplinesSkills(about_box);
					
				}else //no existe about -> busca opcion research
				{
					System.out.println("Profile sin about");
					res.put("disciplines", new ArrayList<String>(0));
					res.put("skills", new ArrayList<String>(0));
				}
				
				//inicio funcion para extraer la afiliacion del author
				WebElement afilitaion_box = driver.findElement(By.cssSelector("div[class='profile-overview__box-right'] > *"));
				response =  rg.obtainAuthorAffiliation(afilitaion_box);
				
				response.put("disciplines", res.get("disciplines"));
				response.put("skills", res.get("skills"));
				
			}

		}catch (Exception e) {
			System.err.println("Function :obtainDataProfileResearchGate(),"+e.getMessage() );
			e.printStackTrace();
		}
		
		return response;
	}
	
	
	/*
	 * search publication by title
	 * @params: @String title
	 * @return:JsonArray,[{
		    "document_title": "Mobile Ad Hoc Networks","document_extrainfo": "","document_eventdate": "May 2016",
		    "document_detail": "","document_abstract": "",
		    "document_authors": [{	"names": "Mehran Misaghi",
		            				"href": "https://www.researchgate.net/profile/Mehran_Misaghi?_sg%5B0%5D=VxC5pxjWE1_ogNysrFTuYxXJlgv79ni-xqWKDXeSdbd6E_UGO6JTlc6An1awaM_6sZCfKsI.xjJ55mzqxCTakW9Od-NTlnjXid5K_kJdilDEExnHmDzoPKvIvLsvK6dKc7mleugChcwyJ6e75QCLrj-d76NztA&_sg%5B1%5D=7k92Cq8UY7G0IzPMxYZZLVimskuEsnqYoaAAinLcruDB2SHG-wpdFqaBIzsvJcFQlXOvIKk.sakkPTI4VDtpb7-FkwNxD-pR8hOHWrTs20vxhZDZKIFTQyay60S2CkJAsHMPkLbr9wiWx-VWtFCHuOozEgpUuQ",
		            				"has_profile": true},
		            			{	"names": "Eduardo da Silva",
									"href": "https://www.researchgate.net/profile/Eduardo_Da_Silva8?_sg%5B0%5D=VxC5pxjWE1_ogNysrFTuYxXJlgv79ni-xqWKDXeSdbd6E_UGO6JTlc6An1awaM_6sZCfKsI.xjJ55mzqxCTakW9Od-NTlnjXid5K_kJdilDEExnHmDzoPKvIvLsvK6dKc7mleugChcwyJ6e75QCLrj-d76NztA&_sg%5B1%5D=7k92Cq8UY7G0IzPMxYZZLVimskuEsnqYoaAAinLcruDB2SHG-wpdFqaBIzsvJcFQlXOvIKk.sakkPTI4VDtpb7-FkwNxD-pR8hOHWrTs20vxhZDZKIFTQyay60S2CkJAsHMPkLbr9wiWx-VWtFCHuOozEgpUuQ",
		            				"has_profile": true
		            			},
		    ],
		    "document_type": "Data",
		    "document_doi": ""
	 
	 * }]
	 */
	public JSONArray searchResearchGatePublicationByTitle(String title, WebDriver driver) 
	{
		JSONArray response = new JSONArray();
		driver.get("https://www.researchgate.net/search.Search.html?type=publication&query=");
		
		try {
			ResearchGateWebElements rg = new ResearchGateWebElements();
			Thread.sleep(2000);
			
			WebElement input_form = driver
					.findElement(By.cssSelector("div[class='search-container__form-container search-container__form-container--tabs'] form"))
					.findElement(By.cssSelector("input[class='search-container__form-input']"));
			
			input_form.sendKeys(title);
			Thread.sleep(4000);
			input_form.sendKeys(Keys.RETURN);
			Thread.sleep(2000);
			
			List<WebElement> results = driver.findElement(By.cssSelector("div[class='search-container'] > div"))
					.findElements(By.xpath("./div[@class='nova-o-stack__item']"));
			
			WebElement div_result = rg.findByChildAttributes("class", 
					"nova-l-flex__item nova-l-flex nova-l-flex--gutter-s nova-l-flex--direction-row@s-up nova-l-flex--align-items-stretch@s-up nova-l-flex--justify-content-flex-start@s-up nova-l-flex--wrap-nowrap@s-up",
					results);
			
			if(!Objects.isNull( div_result )) {
				WebElement box_results = div_result.findElement(By.xpath("./div/div[@class='nova-l-flex__item nova-l-flex__item--grow']/div[@class='search-box search-box--tabs']/div"));
				
				if(box_results.getAttribute("class").equalsIgnoreCase("search-box__noresults")) {
					System.err.println("La busqueda tiene 0 resultados :"+title);
					throw new IOException("La busqueda tiene 0 resultados :"+title);
				}else {
					List<WebElement> result_list = box_results.findElements(By.cssSelector("div[class='search-box-publication'] > div"));
					JSONObject rg_data = new JSONObject();
					
					if( !result_list.isEmpty()) 
					{
						//obtiene las URL de los resultados de búsqueda
						List<WebElement> urls = rg.obtainResearchGateResultsByTitle(CANTIDAD_RESULTADOS, result_list);
						List<String> hrefs = new ArrayList<>();
						urls.forEach(u->{
							hrefs.add(u.getAttribute("href"));
						});
						
						for (String href : hrefs) 
						{
							driver.get(href);
							Thread.sleep(2000);
							
							rg_data = rg.obtainReseachGatePublicationData(driver);
							Thread.sleep(2000);
							rg_data.put("document_abstract", rg.obtainPublicationAbstract(driver));
							
							response.put(rg_data);
						}
						
						//inicio busqueda de información del autor de la publicaion
						for (int i = 0; i < response.length(); i++) 
						{
							JSONObject obj = response.getJSONObject(i);
							JSONArray  array = obj.getJSONArray("document_authors");
							
							for(int j = 0; j < array.length(); j++) {
								
								JSONObject json = array.getJSONObject(j);
								JSONObject affiliation = new JSONObject();
								
								if(json.getBoolean("has_profile")){
									affiliation = obtainDisciplineSkillAffiliation(json.getString("href"), driver);
								}
								
								//response.getJSONObject(i).getJSONArray("document_authors").getJSONObject(j).put("affiliation_data", affiliation);
								json.put("affiliation_data", affiliation);
							}	
						}
						
					}
					else 
						LOG.info("Verificar la cantidad de resultados :"+result_list.size()+" para,"+title);

				}
				
			}
	
		} 
		//catch (NoSuchElementException | NullPointerException | InterruptedException | IOException | JSONException e) {
		catch(Exception e) {
			LOG.error("Function: searchResearchGatePublicationByTitle(), "+e.getMessage(),e);
			System.err.println("Function: searchResearchGatePublicationByTitle(), "+e.getMessage());
		}
		return response;
	}
	
	public String getUpdated_type() {
		return updated_type;
	}

	public void setUpdated_type(String updated_type) {
		this.updated_type = updated_type;
	}
	
	/*
	 * Función que retorna sel SET de cookies de researchgate Set<Cookies>,
	 * reemplaza a la funcion login, para evitar el logeo en researchgate cada
	 * vez que se invoca alguna función
	 */
	public Set<Cookie> addResearchGateCookies(WebDriver driver) 
	{
		Set<Cookie> cookies = new HashSet<>();
		driver.manage().window().maximize();
		driver.get("https://www.researchgate.net");
		File file = new File("../slrApp/src/main/resources/static/seleniumChromeDrivers/ResearchGateCookies.data");
		try 
		{
			if(file.exists())
			{
				FileReader fr = new FileReader(file);
				
				BufferedReader br = new BufferedReader(fr);							
		        String strline;			
		        while((strline = br.readLine())!=null)
		        {
		        	StringTokenizer token = new StringTokenizer(strline,";");									
		            while(token.hasMoreTokens())
		            {					
		            	String name = token.nextToken();					
		            	String value = token.nextToken();					
		            	String domain = token.nextToken();					
		            	String path = token.nextToken();					
		            	Date expiry = null;					
		            		
		            	String val;			
		                if(Objects.nonNull(val=token.nextToken()))
		        		{		
		                	if(!Objects.equals(val, "null") ){
		                		DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
		                		expiry =  dateFormat.parse(val);
		                	}
		                }		
		                Boolean isSecure = new Boolean(token.nextToken()).booleanValue();		
		                cookies.add(new Cookie(name,value,domain,path,expiry,isSecure) );
		                
//		                driver.manage().addCookie(ck);
		            }
		        }
		        br.close();
		        fr.close();
		        
			}
			else 
			{
				driver.manage().window().maximize();
				driver.get("https://www.researchgate.net/login");
				Thread.sleep(2000);
				driver.findElement(By.id("input-login")).sendKeys("evelasqu@dcc.uchile.cl");
				driver.findElement(By.id("input-password")).sendKeys("vegetawc123");
				Thread.sleep(1000);
				driver.findElement(By.xpath("/html/body/div[1]/div[1]/div[1]/div/div/div/div/form/div/div[4]/button")).click();
				
				FileWriter fw = new FileWriter(file);
				BufferedWriter bw = new BufferedWriter(fw);
				
				for(Cookie ck : driver.manage().getCookies()) {
					bw.write((ck.getName()+";"+ck.getValue()+";"+ck.getDomain()+";"+ck.getPath()+";"+ck.getExpiry()+";"+ck.isSecure()));																									
		            bw.newLine();
				}
				
				bw.close();
				fw.close();
				
				return driver.manage().getCookies();
			}
		}catch (Exception e) {
			System.err.println("Error con el archivo que almacena cookies, "+e.getMessage());
		}
		
		return cookies;
	}
	
	public WebDriver setup() {
		WebDriver res = null;
		try 
		{
			System.setProperty("webdriver.chrome.driver", "../slrApp/src/main/resources/static/seleniumChromeDrivers/chromedriver");
			
			ChromeOptions options = new ChromeOptions();
			options.addArguments("headless");
			DesiredCapabilities capabilities = DesiredCapabilities.chrome();
			capabilities.setCapability(ChromeOptions.CAPABILITY, options);
			
			//res = new ChromeDriver(capabilities);
			res = new ChromeDriver();
			//session cookies
			Set<Cookie> cookies = addResearchGateCookies(res);
//			cookies.forEach(cook->{
//				res.manage().addCookie(cook);
//			});
			
			for (Cookie cookie : cookies) 
				res.manage().addCookie(cookie);
		  
		} catch (Exception e) {
			System.err.println("Function setup(): "+e.getMessage());
		}  
		
		return res;
	}
}

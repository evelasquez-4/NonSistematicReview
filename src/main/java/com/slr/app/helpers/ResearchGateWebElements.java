package com.slr.app.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Component;

@Component
public class ResearchGateWebElements {
	
	private static final Logger LOG = LogManager.getLogger(ResearchGateWebElements.class);
	
	private List<String> options = Arrays.asList("Overview","Contributions","Departments","Members","Member stats");
	private List<String> profile_options = Arrays.asList("Overview","Research");
	
	//verifica si dado la cabecera de opciones, la opcion department esta habilitada
	//WebElement->driver.findElements(By.cssSelector("[class='tab-bar-plain-inner'] > a"))
	public boolean isDepartmentOptionEnable(List<WebElement> elements)
	{
		boolean res = true;
		try {
			for (WebElement web : elements) {
				if( web.getAttribute("class").equalsIgnoreCase("btn btn-large btn-inactive")
						&& web.getText().trim().equalsIgnoreCase(options.get(2))) {
					res = false; break; 
				}
			}
		}catch (NoSuchElementException e) {
			LOG.error("Function :isDepartmentOptionEnable()"+e.getMessage(),e);
		}
		return res;
	}
	
	
	//verifica si la lista de Instituciones, tiene personas como miembros
	//Webelement -> driver.findElements(By.cssSelector("[class='list'] > div")
	public boolean isDepartmentHasMembers(WebElement element) throws NoSuchElementException
	{
		List<WebElement> stats = element.findElements(By.cssSelector("[class='stats'] > div"));
		//System.out.println(stats.size());
		return stats.size() > 1;
	}
	
	//dado un nombre de opcion en una lista de WebElements, retorna WebElement que concide con TEXT
	//se condiciona a que webelement tenga TEXT
	public WebElement getWebElementByName(String name, List<WebElement> elments)
	{
		WebElement res = null;
		try {
			for (WebElement web : elments) {
				if(web.getText().trim().equalsIgnoreCase(name.trim())){	
					res = web; break;
				}
			}
		}catch (NoSuchElementException e) {
			LOG.error("Function : getWebElementByName(), "+e.getMessage(),e);
		}
		return res;
	}
	
	public List<Integer> splitNumber(int parts, int number)
	{
		List<Integer> res = new ArrayList<Integer>();
		if(number > parts)
		{
			int num =  (int) Math.floor(number / parts);
			for(int i=1;i<=parts-1;i++)
				res.add(num*i);
			res.add( number );
		}else
			res.add(number);
		
		return res;
	}

	public int getPartes(int position)
	{
		int res = 0;
		if(position > 0 && position < 1000)
			res = 2;
		else if(position > 1000 && position < 2000)
			res = 3;
		else if(position > 2000 && position < 3000)
			res = 4;
		else if(position > 3000 && position < 4000)
			res = 5;
		else if(position > 4000 && position < 5000)
			res = 6;
		else if(position > 5000 && position < 6000)
			res = 7;
		else if(position > 6000 && position < 7000)
			res = 8;
		else if(position > 8000 && position < 9000)
			res = 9;
		else if(position > 9000 && position < 10000)
			res = 10;
		else
			res = 15;
		
		return res;
	}
	
	
	// verifica si dado la cabecera de opciones, la opcion members
	// WebElement->driver.findElements(By.cssSelector("[class='tab-bar-plain-inner'] > a"))
	public boolean isMemberOptionEnable(List<WebElement> elements) {
		boolean res = false;
		for (WebElement web : elements) {
			if (web.getText().trim().equalsIgnoreCase(options.get(3)) && "btn btn-large ajax-page-load members".trim()
					.equalsIgnoreCase(web.getAttribute("class").trim())) {
				res = true;
				break;
			}

		}
		return res;
	}
	
	/*
	 * funcion que busca por texto y funcion css en una lista de Webelement
	 * @param : List<WebElement> options; lista donde hace busqueda
	 * @param : String texo, el texto a buscar
	 * @param : String css_class, el atributo css de Webelement
	 * return: WebElement 
	 */
	public WebElement fidnElementByTextAttValAtt(List<WebElement> options, String text, String css_att,String css_att_val)
	{
		WebElement res = null;
		for (WebElement web : options) {
			
			if( text.trim().equalsIgnoreCase( web.getText().trim() ) &&
				css_att_val.trim().equalsIgnoreCase( web.getAttribute(css_att) )	)
			{
				res = web; break;
			}
			
		}
		return res;
	}
	
	public WebElement findWebElementByAttribute(String attribute, String value,List<WebElement> elements)
	{
		WebElement res = null;
		try {
			for (WebElement web : elements) {
				if (web.getAttribute(attribute).trim().equalsIgnoreCase(value.trim())) {
					res = web;
					break;
				}
			}
			
		}catch (NoSuchElementException e) {
			LOG.error("Function: findWebElementByAttribute(), Verifique que el elemento tenga los atts enviados"+attribute+" = "+value,e);
		}
		return res;
	}
	
	/*
	 * funcion que busca los datos de afiliacion de un author
	 * @params: Webelement -> box con la data de afiliacion
	 * Webelement -> div[class='profile-overview__box-right'] > *
	 * return:
	  {
	  		"institution_name": "Austral University (Argentina)",
			"institution_url" : "https://www.abc.com",
			"country": "Argentina",
			"region": "Buenos Aires",
			"department": "Facultad de Ingeniería",
			"position": "Research Assistant"
	  }
	 * 
	 */
	public JSONObject obtainAuthorAffiliation(WebElement webElement)
	{
		JSONObject response = new JSONObject();
		
		String institution_name = "";
		String institution_url = "";
		String institution_location = "";
		String institution_department = "";
		String department_url = "";
		String author_position = "";
		String country = "";
		String country_region = "";
		
		try {
			List<WebElement> affilation_box = webElement.findElements(By.cssSelector("div[class='nova-c-card nova-c-card--spacing-m nova-c-card--elevation-1-above profile-affiliation-box'] > div"));
			WebElement affilation_body = findWebElementByAttribute("class", "nova-c-card__body nova-c-card__body--spacing-none", affilation_box);
			
			
			List<WebElement> divs = affilation_body.findElements(By.xpath("./div/div[@class='nova-o-stack__item']"));
			
			for (WebElement div : divs) 
			{
				WebElement node = div.findElement(By.xpath("./node()"));
				if(node.getTagName().equals("div")
						&& node.getAttribute("class").equals("nova-o-stack nova-o-stack--gutter-m nova-o-stack--spacing-none nova-o-stack--no-gutter-outside") )
				{
					List<WebElement> datas = div.findElements(By.xpath("./div/div[@class='nova-o-stack__item']/div/div[@class='nova-v-institution-item__body']/div/div[@class='nova-v-institution-item__stack-item']"));
																						  																					   
					for (WebElement data : datas) {
						WebElement child = data.findElement(By.xpath("./node()"));
						
						switch (child.getAttribute("class")) 
						{
							//title class
							case "nova-e-text nova-e-text--size-l nova-e-text--family-sans-serif nova-e-text--spacing-none nova-e-text--color-inherit nova-v-institution-item__title":
								WebElement title = data.findElement(By.tagName("a"));
								institution_name = title.getText();
								institution_url = title.getAttribute("href");
								
								break;
	
							case "nova-v-institution-item__info-section":
								WebElement header = data.findElement(By.cssSelector("div[class='nova-e-text nova-e-text--size-s nova-e-text--family-sans-serif nova-e-text--spacing-none nova-e-text--color-inherit nova-v-institution-item__info-section-title']"));
								WebElement body = data.findElement(By.cssSelector("div[class='nova-e-text nova-e-text--size-m nova-e-text--family-sans-serif nova-e-text--spacing-none nova-e-text--color-inherit']"));
								
								switch (header.getText()) 
								{
									case "Location":
										institution_location = body.getText();
									break;
									
									case "Department":
										institution_department = body.getText();
										//department_url = body.findElement(By.cssSelector("a[class='nova-e-link nova-e-link--color-inherit nova-e-link--theme-bare']")).getAttribute("href");
										department_url = body.findElement(By.xpath("//a")).getAttribute("href");
									break;
									
									case "Position":
										author_position = body.getText();
									break;
	
								}
								
								
								break;
								
						}
					}
				}
			}
			
			if( institution_location.contains(",") )
			{
				String[] split = institution_location.trim().split(",");
				country_region  = split[0];
				country = split[1];
			}else
				country = institution_location.trim();
			
			System.out.println("insitution name: "+institution_name);
			System.out.println("institution url: "+institution_url);
			System.out.println("institution country: "+country);
			System.out.println("institution region: "+country_region);
			System.out.println("department: "+institution_department);
			System.out.println("department_url: "+department_url);
			System.out.println("position: "+author_position);
			
			response.put("institution_name", institution_name)
					.put("institution_url", institution_url)
					.put("country", country)
					.put("region", country_region)
					.put("department", institution_department)
					.put("department_url", department_url)
					.put("position", author_position);
		
		}catch(NoSuchElementException | NullPointerException e) {
			LOG.error("Function obtainAuthorAffiliation(), "+e.getMessage(),e);
		}
		
		return response;	
	}
	
	//funcion que dado la caja de about de profile busca la caja disciplines
	//se considera que el profile ya verifico que tiene la caja about != null
	/*
	 "skills" : [skill1,skill2]
	 "disciplines" : [discipline1,discipline2]
	 * */
	public Map< String, List<String> > obtainProfileDisciplinesSkills(WebElement about_box)
	{
		Map<String, List<String> > res = new HashMap<>();
		
		List<String> disciplines_list = new ArrayList<>();
		List<String> skills_list = new ArrayList<>();
		
		List<WebElement> box_options = about_box.findElements(By.cssSelector("[class='nova-c-card nova-c-card--spacing-none nova-c-card--elevation-1-above'] > div"));																  
		WebElement box_body = findWebElementByAttribute("class", "nova-c-card__body nova-c-card__body--spacing-inherit", box_options);
		
		try {
		
			if( Objects.nonNull(box_body) ) {
				List<WebElement> div_items = box_body.findElements(By.xpath("./div/div[@class='nova-o-stack__item']/*"));
				
				if(div_items.size() > 0)//tiene disciplinas o expertise
				{
					for (WebElement div : div_items) 
					{
						String about = div.findElement(By.tagName("strong")).getText();
						
						if(about.contains("Disciplines")) //existe disciplinas
						{
							List<WebElement> disciplines = div.findElements(By.xpath("./div[2]/div//div[@class='nova-l-flex__item']/*"));
							for (WebElement dis : disciplines) {
								
								if( dis.getTagName().equalsIgnoreCase("a") ){
									disciplines_list.add( dis.findElement(By.xpath(".//span")).getText() );
								}
							}
							
							System.out.println("Total disciplines->"+disciplines_list.size());
						
						}else if(about.contains("Skills") ){//existen skills
							
							List<WebElement> skills = div.findElements(By.xpath("./div[2]/div//div[@class='nova-l-flex__item']/*"));
							//view all existe
							if( "button".equals( skills.get(skills.size()-1).getTagName() ) )
							{
								WebElement view_button = div.findElement(By.tagName("button"));
								view_button.click();
								
								skills = div.findElements(By.xpath("./div[2]/div//div[@class='nova-l-flex__item']/*"));
								System.out.println("View all button exists.");
							}
							
							for (WebElement ski : skills) {
								if(ski.getTagName().equalsIgnoreCase("a"))
									skills_list.add(ski.getText());
							}
							System.out.println("Total skills->"+skills_list.size());
						}
					}
				}
		
			}else System.out.println("null box body");
			
			res.put("disciplines", disciplines_list);
			res.put("skills", skills_list);
			
		}catch (NoSuchElementException | NullPointerException e) {
			LOG.error("Function: obtainProfileDisciplinesSkills(), "+e.getMessage(),e);
		}
		return res;
	}
	
	
	public List<ResearchGateProfileContributions> findContributionsResearchGate(WebElement contribution_item)
	{
		List<ResearchGateProfileContributions> res = new ArrayList<>();
		
		try {
			List<WebElement> divs = contribution_item.findElements(By.cssSelector("[class='nova-c-card nova-c-card--spacing-xl nova-c-card--elevation-1-above'] > div"));
			WebElement body = findWebElementByAttribute("class", "nova-c-card__body nova-c-card__body--spacing-inherit", divs);
		
			if(Objects.nonNull(body)) {
				
				List<WebElement> spinner = body.findElements(By.cssSelector("[class='nova-o-stack nova-o-stack--gutter-xl nova-o-stack--spacing-none nova-o-stack--no-gutter-outside'] > div"));
				if(spinner.size() > 1)
					throw new NoSuchElementException("Error al cargar el profile, verifique que toda la pagina haya cargado"+contribution_item.getAttribute("class"));
				
				List<WebElement> items = body.findElements(By.xpath("./div/div/div/div[@class='nova-o-stack__item']"));
				int cantidad = 1;
				
				for (WebElement web : items) 
				{
					List<WebElement> publication_items = web.findElements(By.xpath("./div/div[1]/div/div[@class='nova-v-publication-item__body']/div/div[@class='nova-v-publication-item__stack-item']"));
					ResearchGateProfileContributions contributions = new ResearchGateProfileContributions();
					
					for (WebElement pub : publication_items) 
					{
						WebElement type = pub.findElement(By.xpath("./node()"));
						String att = type.getAttribute("class");
						
						switch (att) 
						{
						//head
						case "nova-e-text nova-e-text--size-l nova-e-text--family-sans-serif nova-e-text--spacing-none nova-e-text--color-inherit nova-v-publication-item__title":
							WebElement publication_head = pub.findElement(By.xpath("./div/a"));
							System.out.println(cantidad+": "+publication_head.getText());
							
							contributions.setPub_title(publication_head.getText());
							contributions.setPub_url(publication_head.getAttribute("href"));
							break;
						//body
						case "nova-v-publication-item__meta":
							//List<WebElement> body_opts = pub.findElements(By.cssSelector("[class='nova-v-publication-item__meta'] div")); 
							List<WebElement> body_opts = pub.findElements(By.xpath("./div/div[*]"));
														
							for (WebElement bod : body_opts) {
								
								WebElement child = bod.findElement(By.cssSelector("*:first-child"));
								
								if( bod.getAttribute("class").trim()
										.equalsIgnoreCase("nova-v-publication-item__meta-left") 
										&& child.getTagName().equalsIgnoreCase("span")
								  ) 
								{
										WebElement left_opts = bod.findElement(By.xpath("./node()"));
										
										if(Objects.equals(left_opts.getTagName(), "span")) {
											//retorna tipo publicacion
											String tipo_public = left_opts.getText();
											System.out.println("Document Type:"+tipo_public);
											
											contributions.setPub_type(tipo_public);
										}						
								}	
								else if( bod.getAttribute("class").trim()
										.equalsIgnoreCase("nova-v-publication-item__meta-right") 
										&& child.getTagName().equalsIgnoreCase("ul")
										)
								{
									//List<WebElement> rigth_opts = bod.findElements(By.xpath("./ul/li[@class='nova-e-list__item nova-v-publication-item__meta-data-item']"));
									List<WebElement> rigth_opts = bod.findElements(By.xpath("./ul/li[*]"));
									String date = "";
									String event_name = "";
																		
									if(rigth_opts.size() == 1)
										date = rigth_opts.get(0).findElement(By.xpath("./* [last()]") ).getText();
									else if(rigth_opts.size() == 2 || rigth_opts.size() == 3) {
										date = rigth_opts.get(0).findElement(By.xpath("./* [last()]") ).getText();
										event_name= rigth_opts.get(1).findElement(By.xpath("./* [last()]") ).getText();
										
//										for (WebElement right : rigth_opts) {
//											//WebElement r = right.findElement(By.cssSelector("li *:last-child"));
//											WebElement r = right.findElement(By.xpath("./* [last()]") );
//											System.err.println("TagName ->"+r.getTagName());
//											if(r.getTagName().equalsIgnoreCase("time"))
//												System.out.println("TIME->"+r.getText() );
//											else if(r.getTagName().equalsIgnoreCase("span"))
//													System.out.println("SPAN->"+r.getText() );
//										}
									}
									System.out.println("Date: "+date);
									System.out.println("Event Name:"+event_name);
									
									contributions.setPub_date(date);
									contributions.setPub_event(event_name);
								}
							}
							break;
						//footer
						case "nova-e-list nova-e-list--size-m nova-e-list--type-inline nova-e-list--spacing-none nova-v-publication-item__person-list":
							List<WebElement> authors = pub.findElements(By.xpath("./ul/li[@class='nova-e-list__item']"));
							boolean isAuthorsComplete = true;
							List<String> author_names = new ArrayList<>(); 
							
							for (WebElement aut : authors) {
								WebElement parts = aut.findElement(By.xpath("./node()"));
								
								if( parts.getTagName().equalsIgnoreCase("span"))
									isAuthorsComplete = false;
								else if(parts.getTagName().equalsIgnoreCase("a"))
								{
									WebElement fullname = aut.findElement(By.xpath("./a/span[@class='nova-v-person-inline-item__fullname']"));
									System.out.println("Author->"+fullname.getText());
									author_names.add(fullname.getText());
								}
							}
							System.out.println("All authors: "+isAuthorsComplete);
							contributions.setHasAllAuthors(isAuthorsComplete);
							contributions.setAuthors(author_names);
							
							break;
						default:
							System.err.println("Error option : "+att);
							break;
						}
					}
					res.add(contributions);
					cantidad++;
					System.out.println("================================");
				}
			}
		}catch (NoSuchElementException e) {
			LOG.error("function :findContributionsResearchGate(): Error al buscar el elemento :"+e.getMessage(),e);
		}
		return res;
	}
	
	//busqueda de la cantidad de contribuciones del researcher
	public int findContributionNumberResearchGate(WebElement menu_item)
	{
		String CONTRIBUTIONS = "All";
		int contribution_number = 0;
		
		try 
		{
			WebElement nav = menu_item.findElement(By.tagName("nav"));
			List<WebElement> buttoms = nav.findElements(By.xpath("./div/div//button"));
			
			for (WebElement web : buttoms) {
				if(web.getAttribute("class").trim()
						.equalsIgnoreCase("nova-c-nav__item profile-contributions-menu__tab-second-level".trim()) ) {
					
					String texto = web.findElement(By.xpath("./div/div")).getText();
					if(texto.indexOf(CONTRIBUTIONS) > -1 ){
						contribution_number = Integer.parseInt( texto.replaceAll("[^0-9?!\\.]","") );
					}
				}
			}
		}catch(NoSuchElementException e) {
			LOG.error("Function: findContributionNumberResearchGate() :Attribute exception "+e.getMessage(),e);
		}
		
		return contribution_number;
	}
	
	//solo busca en opciones de profile researcher
	public WebElement findInProfileOptions(List<WebElement> options, String text)
	{
		WebElement res = null;
		try {
			for (WebElement web : options) {
				if(profile_options.contains(text))
				{
					if(
							web.findElement(By.xpath("./div/div")).getText().toString().trim().equalsIgnoreCase( text)
							&& web.getAttribute("aria-disabled").equalsIgnoreCase("false")
					  ) 
					{
						res = web;
						break;
					}
				}	
			}
		}catch (NullPointerException n) {
			LOG.error("Function: findInProfileOptions(),"+"Verifique que el elemento tenga los atts enviados"+text,n);
		}
		return res;
	}

	public WebElement findByChildAttributes(String attribute, String value, List<WebElement> elements) {
		WebElement response = null;
		try {
			for (WebElement element : elements) {
				WebElement web = element.findElement(By.xpath("./div"));
				
				if( !Objects.isNull(web) && 
						web.getAttribute(attribute).equalsIgnoreCase(value) ) 
				{
					response = element;
					break;
				}
			}
		} catch (NullPointerException | NoSuchElementException e) {
			LOG.error("Function: findByChildAttributes(), "+e.getMessage(),e);
		}
		
		return response;
	}
	
	/*
	 * funcion que retorna cantidad de publicaiones como resultado de un busqueda (titulo y link de la publicacion)
	 * @params: cantidad -> numero de publicaiones a retornar
	 * 			List<WebElement> -> lista de elementos, donde se busca informacion
	 */
	public List<WebElement> obtainResearchGateResultsByTitle(int cantidad,List<WebElement> items)
	{	
		List<WebElement> res = new ArrayList<>(cantidad);
		try {
			if(cantidad <= items.size())
			{
				for (int i = 0; i < cantidad; i++) {
					
					//List<WebElement> partes = items.get(i).findElements(By.xpath("./div/div[@class='nova-o-stack__item']/div[not(@class)]/div/div[@class='nova-v-publication-item__body']/div/div[@class='nova-v-publication-item__stack-item']"));
					WebElement item_body = items.get(i).findElement(By.cssSelector("div[class='nova-v-publication-item__body']"));
					List<WebElement> partes = item_body.findElements(By.xpath("./div/div[@class='nova-v-publication-item__stack-item']"));
					
					for (WebElement parte : partes) {
						WebElement title = parte.findElement(By.xpath("./node()"));
						
						if( title.getAttribute("class")
							.equalsIgnoreCase("nova-e-text nova-e-text--size-l nova-e-text--family-sans-serif nova-e-text--spacing-none nova-e-text--color-inherit nova-e-text--clamp-3 nova-v-publication-item__title")
							) {
							res.add( title.findElement(By.xpath("./a")) );
							break;
						}
					}
					
				}
				System.out.println("CantUrls:"+res.size());
			}
			
			if(res.size() < cantidad)
				System.out.println("verifique la cantidad de resultados de la busqueda :"+res.size());
		
		} catch (NoSuchElementException | NullPointerException e) {
			LOG.error("Function: obtainResearchGateResultsByTitle(), "+e.getMessage(),e);
		}
		return res;
	}

	public boolean verifyAttributeElement(String att, WebElement web)
	{
		boolean res = false;
		try {
			if( Objects.nonNull(web.getAttribute(att)) )
				res =true;
			
		} catch (NoSuchElementException | NullPointerException e) {
			LOG.error("Function:verifyAttributeElement(),"+e.getMessage(),e );
		}
		
		return res;
	}
	
	/*
	 * funcion que retorna el tipo de documento de una publicacion
	   {
		    "document_title": "Mobile Ad Hoc Networks",
		    "document_extrainfo": "",
		    "document_eventdate": "May 2016",
		    "document_detail": "",
		    "document_abstract": "",
		    "document_authors": [
		        {
		            "names": "Mehran Misaghi",
		            "href": "https://www.researchgate.net/profile/Mehran_Misaghi?_sg%5B0%5D=VxC5pxjWE1_ogNysrFTuYxXJlgv79ni-xqWKDXeSdbd6E_UGO6JTlc6An1awaM_6sZCfKsI.xjJ55mzqxCTakW9Od-NTlnjXid5K_kJdilDEExnHmDzoPKvIvLsvK6dKc7mleugChcwyJ6e75QCLrj-d76NztA&_sg%5B1%5D=7k92Cq8UY7G0IzPMxYZZLVimskuEsnqYoaAAinLcruDB2SHG-wpdFqaBIzsvJcFQlXOvIKk.sakkPTI4VDtpb7-FkwNxD-pR8hOHWrTs20vxhZDZKIFTQyay60S2CkJAsHMPkLbr9wiWx-VWtFCHuOozEgpUuQ",
		            "has_profile": true
		        },
		        {
		            "names": "Eduardo da Silva",
		            "href": "https://www.researchgate.net/profile/Eduardo_Da_Silva8?_sg%5B0%5D=VxC5pxjWE1_ogNysrFTuYxXJlgv79ni-xqWKDXeSdbd6E_UGO6JTlc6An1awaM_6sZCfKsI.xjJ55mzqxCTakW9Od-NTlnjXid5K_kJdilDEExnHmDzoPKvIvLsvK6dKc7mleugChcwyJ6e75QCLrj-d76NztA&_sg%5B1%5D=7k92Cq8UY7G0IzPMxYZZLVimskuEsnqYoaAAinLcruDB2SHG-wpdFqaBIzsvJcFQlXOvIKk.sakkPTI4VDtpb7-FkwNxD-pR8hOHWrTs20vxhZDZKIFTQyay60S2CkJAsHMPkLbr9wiWx-VWtFCHuOozEgpUuQ",
		            "has_profile": true
		        },
		    ],
		    "document_type": "Data",
		    "document_doi": ""
	 }
	 */
	public JSONObject obtainReseachGatePublicationData(/*WebElement header,*/WebDriver driver)
	{
		String publication_type="";
		String title = "";
		String date_event = "";
		String doi = "";
		String document_detail = "";
		String extra_info="";
		List<String> authors_list = new ArrayList<>();
		//ResearchGatePublications res = null;
		List<JSONObject> pub_authors =new ArrayList<>();
		
		JSONObject response = new JSONObject();
		
		try {
			
			WebElement header = driver.findElement(By.cssSelector("[class='content-grid content-grid--spacing-xl-top'"));
			
			List<WebElement> elements = header.findElements(By.xpath("./div[@class='content-grid__columns']/div[@class='content-grid__columns--wide']/*"));
			
			for (WebElement element : elements) {
				
				switch (element.getAttribute("class")) 
				{
					case "content-page-header__badges"://class fila 1
						List<WebElement> types = element.findElements(By.xpath("./div/div[@class='nova-l-flex__item']"));
						for (WebElement type : types) {
							WebElement span = type.findElement(By.xpath("./node()"));
							if(Objects.nonNull(span) && span.getTagName().equalsIgnoreCase("span") ) {
								publication_type = span.getText();
							}
						}
						break;
					case "nova-e-text nova-e-text--size-xl nova-e-text--family-sans-serif nova-e-text--spacing-xs nova-e-text--color-inherit":
						title = Objects.nonNull(element.getText())?element.getText():"";
						break;
					
					case "content-page-header__meta":
						List<WebElement> divs = element.findElements(By.xpath("./div[@class='research-detail-meta']/*[*]"));
						for (WebElement item : divs) {
							if(Objects.nonNull(item)) 
							{
								WebElement child = item.findElement(By.xpath("./node()"));//div,ul,etc
								if(item.getTagName().equalsIgnoreCase("div")) {
									
									switch (item.getAttribute("class")) {
									case "nova-e-text nova-e-text--size-m nova-e-text--family-sans-serif nova-e-text--spacing-xxs nova-e-text--color-grey-600":
										//has date and event name
										if(child.getAttribute("class")
												.equalsIgnoreCase("nova-e-list nova-e-list--size-m nova-e-list--type-inline nova-e-list--spacing-none")) 
										{
											List<WebElement> date = child.findElements(By.xpath("./li[@class='nova-e-list__item']"));
											List<String> aux = new ArrayList<>();
											date.forEach(d->{
												aux.add(d.getText());
											});
											date_event = String.join("-", aux.toArray(new String[0]));
										}
										//has doi
										else if(child.getAttribute("class")
												.equalsIgnoreCase("nova-e-list nova-e-list--size-m nova-e-list--type-inline nova-e-list--spacing-none research-detail-meta__item-list")) {
											List<WebElement> doi_list = child.findElements(By.xpath("./li[@class='nova-e-list__item']"));
											List<String> aux = new ArrayList<>();
											doi_list.forEach(d->{
												aux.add(d.getText());
											});
											
											doi = aux.contains("DOI:")?String.join(" ", aux.toArray(new String[0])):"";
											
										}
										break;
									case "nova-e-text nova-e-text--size-m nova-e-text--family-sans-serif nova-e-text--spacing-xxs nova-e-text--color-grey-700":
										if(child.getAttribute("class")
												.equalsIgnoreCase("nova-e-list nova-e-list--size-m nova-e-list--type-inline nova-e-list--spacing-none")) {
											List<WebElement> info = child.findElements(By.xpath("./li[@class='nova-e-list__item']"));
											List<String> aux = new ArrayList<>();
											info.forEach(i->{
												aux.add(i.getText());
											});
											document_detail = aux.size()>0 ? String.join("|", aux.toArray(new String[0])):"";
										}
										break;
									}
									
								}else if(item.getTagName().equalsIgnoreCase("ul")) {
									List<WebElement> extra = item.findElements(By.xpath("./li[@class='nova-e-list__item']"));
									List<String> aux = new ArrayList<>();
									
									extra.forEach(e->{
										aux.add(e.getText());
									});
									extra_info = aux.size()>0?String.join(" ", aux.toArray(new String[0])):"";
									
								}else 
									throw new NoSuchElementException("Error elemento desconocido en header :"+item.getTagName());
								
							}
						}

						
						break;
					case "nova-e-list nova-e-list--size-m nova-e-list--type-inline nova-e-list--spacing-xl"://authors
						
						List<WebElement> authors = element.findElements(By.xpath("./li[@class='nova-e-list__item']//button"));
						
						if(authors.size() > 0) //has button element
						{
							System.out.println("has button element");
							
							driver.findElement(By.xpath("//li[@class='nova-e-list__item']/button[@class='nova-e-link nova-e-link--color-inherit nova-e-link--theme-decorated']")).click();
							Thread.sleep(2000);
							//driver.get("http://localhost:8888/researchgate/modal2.html");
							
							WebElement window_modal = driver.findElement(By.cssSelector("[class='nova-c-modal__window']"));
							WebElement close_modal = window_modal.findElement(By.xpath("./button[@aria-label='Close dialog']"));
							
							List<WebElement> names = window_modal.findElements(By.xpath("./div[@class='nova-c-modal__body nova-c-modal__body--spacing-inherit']/div/div[@class='research-detail-authors-modal__list']/div/div[@class='nova-o-stack__item']"));
							for (WebElement name : names) {
								WebElement detail = name.findElement(
										By.xpath("./div/div/div[@class='nova-l-flex__item nova-l-flex__item--grow nova-v-person-list-item__body']/div/div/div/div/div/a"));
								if(Objects.nonNull(detail))
								{
									authors_list.add(detail.getText());
									
									pub_authors.add( new JSONObject()
													.put("names", detail.getText())
													.put("href", detail.getAttribute("href")) 
													.put("has_profile", detail.getAttribute("href").contains("https://www.researchgate.net/profile"))
												  );
									
								}
							}
							close_modal.click();
							Thread.sleep(2000);
							
						}else {//has not button element
							
							System.out.println("has not button element");
							List<WebElement> auths = element.findElements(By.xpath("./li[@class='nova-e-list__item']"));
							
							for (WebElement aut : auths) 
							{
								authors_list.add( aut.findElement(By.xpath("./*[last()]")).getText() );

								WebElement detail = aut.findElement(By.cssSelector("a[class='nova-e-link nova-e-link--color-inherit nova-e-link--theme-bare research-detail-author']"));
//								//guarda el link donde se guarda el detalle del autor
//								WebElement detail =aut.findElement(By.xpath(".//a[@class='nova-e-link nova-e-link--color-inherit nova-e-link--theme-bare research-detail-author']"));

								pub_authors.add(new JSONObject()
												.put("names", detail.getText())
												.put("href", detail.getAttribute("href"))
												.put("has_profile", detail.getAttribute("href").contains("https://www.researchgate.net/profile"))
												);
							}
						}
						
						break;
	
					default:
						LOG.error("Elemento desconocido:"+element.getTagName());
						break;
				}
			}
			
			System.out.println("TYPE->"+publication_type);	
			System.out.println("TITLE->"+title);
			System.out.println("DATE EVENT->"+date_event);
			System.out.println("DOI->"+doi);
			System.out.println("DOC DET->"+document_detail);
			System.out.println("EXTRA INFO->"+extra_info);
			authors_list.forEach(a->{
				System.out.println("author:"+a);
			});
			System.out.println("================================");
			
			response.put("document_type", publication_type);
			response.put("document_title", title);
			response.put("document_eventdate", date_event);
			response.put("document_doi", doi);
			response.put("document_detail", document_detail);
			response.put("document_extrainfo", extra_info);
			response.put("document_authors", pub_authors);
			
			//res = new ResearchGatePublications(title, publication_type, date_event, doi, document_detail, extra_info, "", authors_list);
			
		} catch (StaleElementReferenceException | NoSuchElementException | NullPointerException | InterruptedException | JSONException e) {
			LOG.error("Function : obtainReseachGatePublicationData(), "+e.getMessage(),e);
		}
		return response;
	}

	public String obtainPublicationAbstract(WebDriver driver)
	{	
		//"title":"titulo" ; "description":"descripcion"
		String res = "";
		String titulo = "";
		String descripcion = "";
		try{
		
			List<WebElement> publication_detail = driver.findElements(By.cssSelector("[class='research-detail'] > div"));
			
			WebElement header = findWebElementByAttribute("class", "content-page-header", publication_detail);
//			WebElement body = findWebElementByAttribute("data-adaptive-height-container", "true", publication_detail);
			WebElement body = driver.findElement(By.cssSelector("div[data-adaptive-height-container='true']"));
			//WebElement body = driver.findElement(By.cssSelector("[class='research-detail'] > div:not([class])"));
			 
			//vrifica si pestaña seleccionada es Overwiew
			if(Objects.nonNull(header)) {
				
				//WebElement nav = header.findElement(By.cssSelector("nav[class='nova-c-nav nova-c-nav--direction-horizontal nova-c-nav--theme-bare nova-c-nav--spread-auto nova-c-nav--no-spacing-outside'] div"));																			  
				WebElement nav = header .findElement(By.cssSelector("div[class='content-page-header__navigation--tabs'] nav"));
						//.findElement(By.xpath("./div/div/nav"));
				
				List<WebElement> buttons = nav.findElements(By.xpath("./div/div[@class='nova-c-nav__items']/button"));
				WebElement overview = findWebElementByAttribute("class", "nova-c-nav__item is-selected", buttons);
				
				if(overview.getText().equalsIgnoreCase("Overview") //opcion overview seleccionado
						&& Objects.nonNull(overview)) 
				{
					if(Objects.nonNull(body)) {
						
						List<WebElement> elements = body.findElements(By.xpath("./div[@class='content-layout']/div[@class='content-layout__container']/div[@class='nova-c-card nova-c-card--spacing-xl nova-c-card--elevation-1-above']/div"));
						WebElement cabecera = findWebElementByAttribute("class", "nova-c-card__header nova-c-card__header--spacing-inherit", elements);
						WebElement contenido = findWebElementByAttribute("class", "nova-c-card__body nova-c-card__body--spacing-inherit", elements);
						
						titulo = Objects.nonNull(cabecera)?cabecera.findElement(By.xpath("./node()")).getText() : "";
						System.out.println("HeaderTitle->"+titulo);
						
						//se descarto tomar titulo
						
						//List<WebElement> divs = contenido.findElements(By.xpath("./div/div[@class='nova-o-stack__item']"));
						List<WebElement> divs = contenido.findElements(By.cssSelector("div[class='nova-o-stack__item']:not(:empty)"));
						boolean has_abstract = true;
						for(WebElement div:divs) {
							WebElement empty = div.findElement(By.xpath(".//node()"));
							if(empty.getAttribute("class")
									.equalsIgnoreCase("research-detail-empty-state")) {
								has_abstract = false;
								break;
							}
						}
						
						if(!has_abstract) {
							System.out.println("No abstract on publication: "+titulo);
							descripcion = "";
						}
						else {//looking for doc abstract
							
							for(WebElement div  : divs)
							{
								WebElement  div_1 = div.findElement(By.xpath(".//node()"));
								
								if(div_1.getAttribute("class")//condicion que forza el enrutamiento al texto abstract
										.equalsIgnoreCase("nova-e-text nova-e-text--size-m nova-e-text--family-sans-serif nova-e-text--spacing-none nova-e-text--color-inherit")) 
								{
									
									WebElement read_more = div_1.findElement(By.xpath("./div/div[@class='nova-e-expandable-text__container']//div[@class]"));

									if(read_more.getAttribute("class")//read_more hidden
											.equalsIgnoreCase("nova-e-expandable-text__read-more nova-e-expandable-text__read-more--hidden")) 
									{
										
										System.out.println("read more hidden"); 
										WebElement abs = div.findElement(By.cssSelector("div[class='nova-e-expandable-text__container'] > div:not([class])"));
										descripcion = Objects.nonNull(abs) ? abs.getText():"";
										
									}
									else{ // read_more is not hidden
										System.out.println("read more not hidden");
										
										//click on button
										div.findElement(By.xpath("./div/div/div[@class='nova-e-expandable-text__container']/div[@class='nova-e-expandable-text__read-more']/button")).click();
										Thread.sleep(2000);
										
										WebElement desc = body.findElement(By.xpath("./div[@class='content-layout']/div[@class='content-layout__container']/div[@class='nova-c-card nova-c-card--spacing-xl nova-c-card--elevation-1-above']/div[@class='nova-c-card__body nova-c-card__body--spacing-inherit']/div/div[@class='nova-o-stack__item']/div/div"));
										descripcion = Objects.nonNull(desc)?desc.getText():"";
										
									}

								}
							}
							
						}

						System.out.println("ABSTRACT :"+descripcion);
						res = descripcion;
						
					}else {
						throw new NoSuchElementException("Error, verifique la existencia de body.");
					}
					
				}else 
					System.out.println("Ingresar else");
				
				
			}
		}catch (NoSuchElementException | NullPointerException | InterruptedException e) {
			LOG.error("Function : obtainPublicationAbstract(), "+e.getMessage(),e);
			System.err.println("Function : obtainPublicationAbstract(), "+e.getMessage());
		}
			
		return res;
	}
}


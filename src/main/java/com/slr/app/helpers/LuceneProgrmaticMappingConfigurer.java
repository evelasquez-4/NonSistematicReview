package com.slr.app.helpers;

import org.hibernate.search.mapper.orm.mapping.HibernateOrmMappingConfigurationContext;
import org.hibernate.search.mapper.orm.mapping.HibernateOrmSearchMappingConfigurer;
import org.hibernate.search.mapper.pojo.mapping.definition.programmatic.ProgrammaticMappingConfigurationContext;
import org.hibernate.search.mapper.pojo.mapping.definition.programmatic.TypeMappingStep;

import com.slr.app.models.Authors;
import com.slr.app.models.Countries;


public class LuceneProgrmaticMappingConfigurer implements HibernateOrmSearchMappingConfigurer{

	@Override
	public void configure(HibernateOrmMappingConfigurationContext context) {
		
		ProgrammaticMappingConfigurationContext mapping = context.programmaticMapping();
		
//		TypeMappingStep author_publications = mapping.type(AuthorPublications.class);
//		author_publications.indexed();
//		author_publications.property("authors")
//			.indexedEmbedded().structure(ObjectStructure.NESTED);
//		author_publications.property("publications")
//			.indexedEmbedded().structure(ObjectStructure.NESTED);
//		
//		
	//	slr.authors
		TypeMappingStep authors = mapping.type(Authors.class);
		authors.indexed().index("idx_authors");
		authors.property("names")
			.fullTextField("author_names")
			.analyzer("english_analyzer");
		
		authors.property("homonyns")
			.genericField("author_homonyns");
		
//		authors.property("organizationses")
//		.indexedEmbedded().structure(ObjectStructure.NESTED);
//		
////		authorsMapping.property("awards").genericField();
////		
////		authorsMapping.property("pid").genericField();
////		
////		authorsMapping.property("key").genericField();
//		
//		//slr.organizations
//		TypeMappingStep organizations = mapping.type(Organizations.class);
//		organizations.indexed();
//		organizations.property("description")
//			.fullTextField("organization_description")
//			.analyzer("english_analyzer");
//		organizations.property("countries")
//			.indexedEmbedded().structure(ObjectStructure.NESTED);
//		
//		
		//slr.countries
		TypeMappingStep countries = mapping.type(Countries.class);
		countries.indexed().index("idx_country");
		countries.property("countryName")
			.fullTextField("country_name")
			.analyzer("english_analyzer");
		
		countries.property("code")
			.genericField("country_code");
		countries.property("codeAlpha")
						.genericField("country_code_alpha"); 
						
		
//		
	}

}

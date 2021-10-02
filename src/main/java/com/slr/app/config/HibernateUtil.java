package com.slr.app.config;


import java.util.HashMap;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;

import com.slr.app.models.*;

public class HibernateUtil {

	private static SessionFactory sessionFactory = null;
	private static StandardServiceRegistry registry;
	   
    static {
        try{
            loadSessionFactory();
        }catch(Exception e){
            System.err.println("Exception while initializing hibernate util.. ");
            e.printStackTrace();
        }
    }
 
    public static void loadSessionFactory(){
    	 
        Configuration configuration = new Configuration();
        configuration.configure("/hibernate.cfg.xml");
        
        configuration.addAnnotatedClass(Authors.class);
		configuration.addAnnotatedClass(AuthorPublications.class);
		configuration.addAnnotatedClass(Books.class);
		configuration.addAnnotatedClass(BookChapters.class);
		configuration.addAnnotatedClass(Conferences.class);
		configuration.addAnnotatedClass(ConferenceEditorials.class);
		configuration.addAnnotatedClass(ConferencePapers.class);
		configuration.addAnnotatedClass(Countries.class);
		configuration.addAnnotatedClass(Organizations.class);
		configuration.addAnnotatedClass(Editions.class);
		configuration.addAnnotatedClass(Journals.class);
		configuration.addAnnotatedClass(JournalEditorials.class);
		configuration.addAnnotatedClass(JournalPapers.class);
		configuration.addAnnotatedClass(Keywords.class);
		configuration.addAnnotatedClass(Publications.class);
		configuration.addAnnotatedClass(PublicationKeywords.class);
		configuration.addAnnotatedClass(DblpPublications.class);
        
        
        ServiceRegistry srvcReg = new StandardServiceRegistryBuilder()
        		.applySettings(configuration.getProperties())
        		.build();
        sessionFactory = configuration.buildSessionFactory(srvcReg);
    }
	
    public static Session getSession() throws HibernateException {
    	 
        Session retSession=null;
            try {
                retSession = sessionFactory.openSession();
            }catch(Throwable t){
            System.err.println("Exception while getting session.. ");
            t.printStackTrace();
            }
            if(retSession == null) {
                System.err.println("session is discovered null");
            }
 
            return retSession;
    }
    
    public static void close()
    {
    	getSession().close();
    }
	
    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
           try {
              StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();

              //Configuration properties
              Map<String, Object> settings = new HashMap<>();
              settings.put(Environment.DRIVER, "org.postgresql.Driver");
              settings.put(Environment.URL, "jdbc:postgresql://localhost:5432/dbslr");
              settings.put(Environment.USER, "postgres");
              settings.put(Environment.PASS, "postgres");
             
             // settings.put(Environment.HBM2DDL_AUTO, "update");
              //Set JDBC batch size
              settings.put(Environment.STATEMENT_BATCH_SIZE, 50	);

              registryBuilder.applySettings(settings);
              registry = registryBuilder.build();
              
              MetadataSources sources = new MetadataSources(registry);
              sources.addAnnotatedClass(Publications.class);
              sources.addAnnotatedClass(Authors.class);
              sources.addAnnotatedClass(AuthorPublications.class);
              sources.addAnnotatedClass(Books.class);
              sources.addAnnotatedClass(BookChapters.class);
              sources.addAnnotatedClass(Conferences.class);
              sources.addAnnotatedClass(ConferenceEditorials.class);
              sources.addAnnotatedClass(ConferencePapers.class);
              sources.addAnnotatedClass(Countries.class);
              sources.addAnnotatedClass(Organizations.class);
              sources.addAnnotatedClass(Editions.class);
              sources.addAnnotatedClass(Journals.class);
              sources.addAnnotatedClass(JournalEditorials.class);
              sources.addAnnotatedClass(JournalPapers.class);
              sources.addAnnotatedClass(Keywords.class);
	      	  sources.addAnnotatedClass(PublicationKeywords.class);
	      	  
	      	  sources.addAnnotatedClass(DblpPublications.class);
              
              Metadata metadata = sources.getMetadataBuilder().build();
              
              sessionFactory = metadata.getSessionFactoryBuilder().build();
           } catch (Exception e) {
              if (registry != null) {
                 StandardServiceRegistryBuilder.destroy(registry);
              }
              e.printStackTrace();
           }
        }
        return sessionFactory;
     }
    
    public static void shutdown() {
        if (registry != null) {
           StandardServiceRegistryBuilder.destroy(registry);
        }
     }
    
    public static SessionFactory openHibernateSessionFactory() {
    	Configuration config = new Configuration();
    	
    	config.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        config.setProperty("hibernate.connection.url", "jdbc:postgresql://localhost:5432/dbslr");
        config.setProperty("hibernate.connection.username", "postgres");
        config.setProperty("hibernate.connection.password", "postgres");
        config.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQL92Dialect");
        config.setProperty("hibernate.jdbc.batch_size", "50");
        
        config.addAnnotatedClass(Publications.class);
        config.addAnnotatedClass(Authors.class);
        config.addAnnotatedClass(AuthorPublications.class);
        config.addAnnotatedClass(Books.class);
        config.addAnnotatedClass(BookChapters.class);
        config.addAnnotatedClass(Conferences.class);
        config.addAnnotatedClass(ConferenceEditorials.class);
        config.addAnnotatedClass(ConferencePapers.class);
        config.addAnnotatedClass(Countries.class);
        config.addAnnotatedClass(Organizations.class);
        config.addAnnotatedClass(Editions.class);
        config.addAnnotatedClass(Journals.class);
        config.addAnnotatedClass(JournalEditorials.class);
        config.addAnnotatedClass(JournalPapers.class);
        config.addAnnotatedClass(Keywords.class);
        config.addAnnotatedClass(PublicationKeywords.class);
        
        config.addAnnotatedClass(DblpPublications.class);
        
        return sessionFactory =  config.buildSessionFactory(); 
    }
}
package com.slr.app.helpers;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

//import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

@Component
//@Configuration
//@ConditionalOnProperty(name="scheduling.enabled",matchIfMissing = true)
public class ScheduledTasks {

	
	//@Scheduled(cron = "*/40 * * * * *")
	//@Scheduled(cron = "0/55 * * * * ?")
	public void getDblpPublicationsDivided() throws IOException {
		System.out.println("hola mundo!!!");
		
		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		builder.connectTimeout(30, TimeUnit.SECONDS); 
		builder.readTimeout(30, TimeUnit.SECONDS); 
		builder.writeTimeout(30, TimeUnit.SECONDS);
		
		OkHttpClient client = new OkHttpClient();
		client = builder.build();
		
		MediaType mediaType = MediaType.parse("application/json");
		@SuppressWarnings("deprecation")
		RequestBody body = RequestBody.create(mediaType, "{\n    \"doc_type\":\"article\",\n    \"updated_state\":\"1.inserted\",\n    \"limit\": 250\n}");
		Request request = new Request.Builder()
				  .url("http://localhost:8081/dblp_publication/divided_rows")
				  .method("POST", body)
				  .addHeader("Content-Type", "application/json")
				  .build();
		client.newCall(request).execute();
	}
}

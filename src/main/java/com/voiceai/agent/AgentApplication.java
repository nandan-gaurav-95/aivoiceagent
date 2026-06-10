package com.voiceai.agent;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class AgentApplication {

	  @Value("${openai.api-key}")
	    private String openAiKey;

	    @PostConstruct
	    public void init() {
	        // Deepgram la OpenAI key environment variable madhe set kara
	        System.setProperty("OPENAI_API_KEY", openAiKey);
	    }
	public static void main(String[] args) {
		SpringApplication.run(AgentApplication.class, args);
	}

}

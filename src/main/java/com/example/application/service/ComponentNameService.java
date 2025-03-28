package com.example.application.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComponentNameService {

    private final ChatClient chatClient;

    public ComponentNameService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    private static String SYSTEM_MESSAGE = """
                You are a coding assistant who helps a software developer to find a name for a component or a view. The 
                application is developed with Vaadin and the source code of the component/view is given to you as input. 
                You are supposed to make different suggestions for a suitable component or view name. The amount of 
                names will passed as a separate parameter. The suggestions should be factual and technically and reflect
                the meaning of the component. It is also allowed to generate fictional, funny and silly names. The 
                output should be a JSON array of strings ONLY, without any additional wrapper object or quote characters.
                
                The amount of names you have to generate is %d.
            """;

    public String getComponentNames(String code, Integer amountOfNames) {
        return chatClient
                .prompt()
                .system(SYSTEM_MESSAGE.formatted(amountOfNames))
                .user(code)
                .call()
                .content();
    }
}

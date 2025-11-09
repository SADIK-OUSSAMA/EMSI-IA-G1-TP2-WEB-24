package ma.emsi.sadik.tp2websadik.llm;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.data.message.SystemMessage;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LlmClient {

    // private String systemRole; // You DON'T need this field anymore
    private ChatMemory chatMemory;
    private Assistant assistant;

    interface Assistant {
        String chat(String prompt);
    }

    public LlmClient() {
        String key = System.getenv("GEMINI_API_KEY");
        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(key)
                .modelName("gemini-2.5-flash")
                .build();
        this.chatMemory = MessageWindowChatMemory.withMaxMessages(10);
        this.assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(chatMemory)
                .build();
    }

    /**
     * THIS IS THE CORRECT IMPLEMENTATION (from your professor's instructions)
     * It clears the memory and adds the role as a SystemMessage.
     */
    public void setSystemRole(String roleSysteme) {
        chatMemory.clear();
        chatMemory.add(new SystemMessage(roleSysteme));
    }

    /**
     * THIS IS THE CORRECT IMPLEMENTATION
     * You just send the user's question. LangChain4j handles the
     * system role and history because it's in the chatMemory.
     */
    public String envoyerQuestion(String question) {
        return assistant.chat(question);
    }
}
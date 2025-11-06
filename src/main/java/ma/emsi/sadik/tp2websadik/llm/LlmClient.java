package ma.emsi.sadik.tp2websadik.llm;


import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.data.message.SystemMessage;
import jakarta.enterprise.context.Dependent;

@Dependent
public class LlmClient {

    private String systemRole;
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

    public void setSystemRole(String systemRole) {
        this.systemRole = systemRole;
        chatMemory.clear();
        chatMemory.add(new SystemMessage(systemRole));
    }

    public String envoyerQuestion(String roleSysteme, String question) {
        if (this.systemRole == null || !this.systemRole.equals(roleSysteme)) {
            setSystemRole(roleSysteme);
        }
        return assistant.chat(question);
    }
}


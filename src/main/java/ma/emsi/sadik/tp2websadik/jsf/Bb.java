package ma.emsi.sadik.tp2websadik.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import ma.emsi.sadik.tp2websadik.llm.LlmClient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Backing bean pour la page JSF index.xhtml.
 * Portée view pour conserver l'état de la conversation (chat) entre l'utilisateur et l'API du LLM.
 */
@Named
@ViewScoped
public class Bb implements Serializable {

    private String roleSysteme;
    private boolean roleSystemeChangeable = true;
    private List<SelectItem> listeRolesSysteme;

    private String question;
    private String reponse;
    private StringBuilder conversation = new StringBuilder();

    @Inject
    private FacesContext facesContext;

    /**
     * Client LLM injecté pour déléguer les requêtes à l'API.
     */
    @Inject
    private LlmClient llmClient;

    public Bb() {}

    // Getters / Setters
    public String getRoleSysteme() {
        return roleSysteme;
    }

    public void setRoleSysteme(String roleSysteme) {
        this.roleSysteme = roleSysteme;
    }

    public boolean isRoleSystemeChangeable() {
        return roleSystemeChangeable;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getReponse() {
        return reponse;
    }

    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    public String getConversation() {
        return conversation.toString();
    }

    public void setConversation(String conversation) {
        this.conversation = new StringBuilder(conversation);
    }

    /**
     * Envoie la question au LLM via LlmClient et affiche la réponse.
     */
    public String envoyer() {
        if (question == null || question.isBlank()) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Texte question vide", "Veuillez saisir une question.");
            facesContext.addMessage(null, message);
            return null;
        }

        try {
            // Si première question → DÉFINIR LE RÔLE and verrouiller le rôle
            if (this.conversation.isEmpty()) {
                llmClient.setSystemRole(roleSysteme); // <-- CORRECT: Call setSystemRole HERE
                this.roleSystemeChangeable = false;
            }

            // Appel au LLM via le client
            // llmClient.setSystemRole(roleSysteme); // <-- DO NOT call it here every time

            // CORRECT: Assign to "this.reponse"
            this.reponse = llmClient.envoyerQuestion(question);

            // Met à jour la conversation
            afficherConversation();

        } catch (Exception e) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Erreur LLM", "Impossible d'obtenir une réponse : " + e.getMessage());
            facesContext.addMessage(null, message);
        }

        return null;
    }

    /**
     * Réinitialise la conversation et crée un nouveau bean.
     * @return "index" pour recharger la page.
     */
    public String nouveauChat() {
        return "index";
    }

    /**
     * Ajoute la dernière question et réponse dans le texte de la conversation.
     */
    private void afficherConversation() {
        this.conversation
                .append("== Utilisateur :\n")
                .append(question)
                .append("\n== LLM :\n")
                .append(reponse)
                .append("\n");
    }

    /**
     * Liste des rôles proposés pour le LLM.
     */
    public List<SelectItem> getRolesSysteme() {
        if (this.listeRolesSysteme == null) {
            this.listeRolesSysteme = new ArrayList<>();

            String role = """
                    You are a helpful assistant. You help the user to find the information they need.
                    If the user types a question, you answer it.
                    """;
            this.listeRolesSysteme.add(new SelectItem(role, "Assistant"));

            role = """
                    You are an interpreter. You translate from English to French and from French to English.
                    If the user types a French text, you translate it into English.
                    If the user types an English text, you translate it into French.
                    If the text contains only one to three words, give examples of usage.
                    """;
            this.listeRolesSysteme.add(new SelectItem(role, "Traducteur Anglais-Français"));

            role = """
                    You are a travel guide. If the user types the name of a country or a city,
                    you tell them the main places to visit and the average price of a meal.
                    """;
            this.listeRolesSysteme.add(new SelectItem(role, "Guide touristique"));
        }
        return this.listeRolesSysteme;
    }
}

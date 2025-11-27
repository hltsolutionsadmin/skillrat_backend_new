package com.skillrat.agent.web;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import jakarta.validation.constraints.NotBlank;
import com.skillrat.agent.state.ConversationState;
import com.skillrat.agent.service.IntentService;
import com.skillrat.agent.tools.IncidentTools;
import com.skillrat.agent.tools.IncidentTools.CreateIncidentRequest;
import com.skillrat.agent.tools.IncidentTools.CreateIncidentResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@RestController
@RequestMapping("/api/agent/chat")
@RequiredArgsConstructor
@Validated
public class ChatController {

    private final ChatClient chatClient; // auto-configured by Spring AI starter
    private final ConversationState conversationState;
    private final IntentService intentService;
    private final IncidentTools incidentTools;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ChatResponse chat(@RequestBody @Validated ChatRequest req,
                             @AuthenticationPrincipal UserDetails principal) {
        String convoKey = resolveConversationKey(req, principal);

        // If we have a pending incident proposal, check for confirmation/denial
        var pending = conversationState.getPending(convoKey);
        if (pending != null) {
            if (intentService.isAffirmative(req.getMessage())) {
                CreateIncidentRequest cir = new CreateIncidentRequest();
                cir.setCategory(pending.getCategory());
                cir.setTitle(pending.getTitle());
                cir.setDescription(pending.getDescription());
                cir.setPriority(pending.getPriority());
                cir.setAssignee(pending.getAssignee());
                cir.setTags(pending.getTags());
                cir.setIdempotencyKey(generateIdempotencyKey(convoKey, pending));
                CreateIncidentResponse created = incidentTools.createIncident(cir);
                conversationState.clear(convoKey);
                ChatResponse resp = new ChatResponse();
                if (created != null && created.getIncidentId() != null) {
                    resp.setReply("Incident created with ID " + created.getIncidentId() + (created.getUrl() != null ? " (" + created.getUrl() + ")" : "") + ".");
                } else {
                    resp.setReply("Sorry, I couldn't create the incident right now. " + (created != null && created.getMessage() != null ? created.getMessage() : ""));
                }
                return resp;
            } else if (intentService.isNegative(req.getMessage())) {
                conversationState.clear(convoKey);
                ChatResponse resp = new ChatResponse();
                resp.setReply("Okay, I won't create an incident. How else can I help?");
                return resp;
            }
            // If neither yes nor no, politely re-ask
            ChatResponse resp = new ChatResponse();
            resp.setReply("Please confirm: should I create the incident titled '" + pending.getTitle() + "'? (yes/no)");
            return resp;
        }

        // No pending: check if the user is expressing an issue that warrants creating an incident
        if (intentService.looksLikeIncidentAsk(req.getMessage())) {
            // Build a proposed incident from heuristics
            var proposal = new ConversationState.ProposedIncident();
            String category = normalizeCategory(intentService.guessCategory(req.getMessage()));
            proposal.setCategory(category);
            proposal.setTitle("Issue: " + truncate(req.getMessage(), 80));
            proposal.setDescription(req.getMessage());
            proposal.setPriority("MEDIUM");
            proposal.setAssignee(null);
            proposal.setTags(java.util.List.of("agent"));
            conversationState.setPending(convoKey, proposal);

            ChatResponse resp = new ChatResponse();
            resp.setReply("It sounds like you're facing a problem. Should I raise an incident under '" + category + "' titled '" + proposal.getTitle() + "'? (yes/no)");
            return resp;
        }

        // Fallback to LLM chat for general queries
        String reply = chatClient.prompt()
                .user(req.getMessage())
                .call()
                .content();
        ChatResponse resp = new ChatResponse();
        resp.setReply(reply);
        return resp;
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()")
    public Flux<String> chatStream(@RequestBody @Validated ChatRequest req) {
        return chatClient.prompt()
                .user(req.getMessage())
                .stream()
                .content();
    }

    private String resolveConversationKey(ChatRequest req, UserDetails principal) {
        if (req.getConversationId() != null && !req.getConversationId().isBlank()) return req.getConversationId();
        String user = principal != null ? principal.getUsername() : "anon";
        return "conv-" + user;
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    private String normalizeCategory(String guess) {
        try {
            java.util.List<String> cats = incidentTools.getIncidentCategories();
            if (cats == null || cats.isEmpty()) return guess;
            // simple case-insensitive match, else return guess
            for (String c : cats) {
                if (c != null && c.equalsIgnoreCase(guess)) return c;
            }
            return guess;
        } catch (Exception ex) {
            return guess;
        }
    }

    private String generateIdempotencyKey(String convoKey, ConversationState.ProposedIncident pi) {
        String base = convoKey + "|" + (pi.getTitle() == null ? "" : pi.getTitle()) + "|" + (pi.getDescription() == null ? "" : pi.getDescription());
        return Integer.toHexString(base.hashCode());
    }

    @Data
    public static class ChatRequest {
        @NotBlank
        private String message;
        private String conversationId; // optional client-provided conversation key
    }

    @Data
    public static class ChatResponse {
        private String reply;
    }
}

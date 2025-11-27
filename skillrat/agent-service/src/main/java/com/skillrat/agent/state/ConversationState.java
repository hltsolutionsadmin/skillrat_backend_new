package com.skillrat.agent.state;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConversationState {

    private final Map<String, ProposedIncident> pending = new ConcurrentHashMap<>();

    public void setPending(String key, ProposedIncident value) {
        if (value == null) {
            pending.remove(key);
        } else {
            pending.put(key, value);
        }
    }

    public ProposedIncident getPending(String key) {
        return pending.get(key);
    }

    public void clear(String key) {
        pending.remove(key);
    }

    @Data
    public static class ProposedIncident {
        private String category;
        private String title;
        private String description;
        private String priority;
        private String assignee;
        private java.util.List<String> tags;
    }
}

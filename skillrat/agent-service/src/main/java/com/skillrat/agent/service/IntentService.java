package com.skillrat.agent.service;

import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;

@Service
public class IntentService {

    private static final Set<String> ISSUE_MARKERS = Set.of(
            "not working", "is down", "down", "issue", "problem", "error", "can't", "cannot", "failed", "failing"
    );

    private static final Set<String> AFFIRMATIVE = Set.of(
            "yes", "y", "yep", "yeah", "sure", "please", "do it", "go ahead", "confirm", "ok", "okay"
    );

    private static final Set<String> NEGATIVE = Set.of(
            "no", "n", "nope", "don't", "do not", "cancel", "stop", "never"
    );

    public boolean looksLikeIncidentAsk(String message) {
        String m = norm(message);
        for (String marker : ISSUE_MARKERS) {
            if (m.contains(marker)) return true;
        }
        // also simple pattern: starts with "raise/create/open ticket/incident"
        return m.matches(".*(raise|create|open).*(ticket|incident).*");
    }

    public String guessCategory(String message) {
        String m = norm(message);
        if (m.contains("email") || m.contains("gmail") || m.contains("outlook")) return "EMAIL";
        if (m.contains("vpn")) return "NETWORK";
        if (m.contains("wifi") || m.contains("wi-fi")) return "NETWORK";
        if (m.contains("laptop") || m.contains("desktop") || m.contains("hardware")) return "HARDWARE";
        if (m.contains("password") || m.contains("login") || m.contains("auth")) return "ACCESS";
        return "GENERAL";
    }

    public boolean isAffirmative(String message) {
        String m = norm(message);
        return AFFIRMATIVE.stream().anyMatch(m::equals) || AFFIRMATIVE.stream().anyMatch(m::contains);
    }

    public boolean isNegative(String message) {
        String m = norm(message);
        return NEGATIVE.stream().anyMatch(m::equals) || NEGATIVE.stream().anyMatch(m::contains);
    }

    private String norm(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT).trim();
    }
}

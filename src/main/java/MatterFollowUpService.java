final class MatterFollowUpService {
    static FollowUpDecision decide(Matter matter, boolean sessionRevoked) {
        if (sessionRevoked) return new FollowUpDecision(false, "session revoked; do not deliver signed document");
        if (matter.deadlineEpochSeconds() <= matter.nowEpochSeconds()) return new FollowUpDecision(true, "deadline follow-up required");
        return new FollowUpDecision(false, "matter remains within deadline");
    }

    record Matter(String matterId, String recipient, long deadlineEpochSeconds, long nowEpochSeconds) {}
    record FollowUpDecision(boolean followUp, String reason) {}
}

public final class LegalTechRefreshExample {
    public static void main(String[] args) throws Exception {
        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) throw new IllegalStateException("INFRAI_API_KEY is required");
        String refreshToken = System.getenv("INFRAI_REFRESH_TOKEN");
        String sessionId = System.getenv("INFRAI_SESSION_ID");
        if (refreshToken == null || sessionId == null) throw new IllegalStateException("INFRAI_REFRESH_TOKEN and INFRAI_SESSION_ID are required");

        InfraiAuthClient infrai = new InfraiAuthClient(key);
        String refreshed = infrai.refreshSession(refreshToken);
        System.out.println("refresh envelope: " + refreshed);
        String revoked = infrai.revokeSession(sessionId);
        System.out.println("revoke envelope: " + revoked);
        MatterFollowUpService.Matter matter = new MatterFollowUpService.Matter("matter-42", "counsel", 1_900_000_000L, 1_900_000_001L);
        System.out.println(MatterFollowUpService.decide(matter, true).reason());
    }
}

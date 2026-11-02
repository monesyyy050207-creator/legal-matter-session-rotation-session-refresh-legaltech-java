public final class MatterFollowUpServiceTest {
    public static void main(String[] args) {
        MatterFollowUpService.Matter overdue = new MatterFollowUpService.Matter("matter-42", "counsel", 100L, 101L);
        MatterFollowUpService.FollowUpDecision decision = MatterFollowUpService.decide(overdue, false);
        if (!decision.followUp()) throw new AssertionError("overdue matter must trigger follow-up");
        if (!decision.reason().equals("deadline follow-up required")) throw new AssertionError(decision.reason());

        MatterFollowUpService.FollowUpDecision revoked = MatterFollowUpService.decide(overdue, true);
        if (revoked.followUp()) throw new AssertionError("revoked session must block delivery");
        System.out.println("MatterFollowUpServiceTest passed");
    }
}

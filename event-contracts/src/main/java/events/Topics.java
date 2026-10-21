package events;

public final class Topics {

    public static final String USER_FLOW = "user-flow";
    public static final String USER_STREAM = "user-stream";

    public static final String TASK_FLOW = "task-flow";
    public static final String TASK_STREAM = "task-stream";
    public static final String TASK_ASSIGNED = "task-assigned";
    public static final String TASK_FINISHED_FLOW = "task-finished-flow";

    public static final String PAYMENT_STREAM = "payment-stream";

    /** Топик для сообщений, которые consumer group не смогла обработать после всех повторов. */
    public static String deadLetter(String topic, String consumerGroup) {
        return topic + "." + consumerGroup + ".DLT";
    }

    private Topics() {
    }
}

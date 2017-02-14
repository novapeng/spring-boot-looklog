package novayoung.log.handler;

import java.io.Serializable;
import java.util.Date;

/**
 *
 */
public class LogDto implements Serializable {

    private static final long serialVersionUID = 1703639553933543532L;

    private String formattedMessage;

    private String traceId;

    private String time;

    private String thread;

    private String logLevel;

    private String loggerName;

    private String line;

    private String message;

    private Date createTime = new Date();


    public String getFormattedMessage() {
        return formattedMessage;
    }

    public void setFormattedMessage(String formattedMessage) {
        this.formattedMessage = formattedMessage;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    @SuppressWarnings("unused")
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @SuppressWarnings("unused")
    public String getThread() {
        return thread;
    }

    public void setThread(String thread) {
        this.thread = thread;
    }

    @SuppressWarnings("unused")
    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    @SuppressWarnings("unused")
    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    @SuppressWarnings("unused")
    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    @SuppressWarnings("unused")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @SuppressWarnings("unused")
    public Date getCreateTime() {
        return createTime;
    }

    @SuppressWarnings("unused")
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

}

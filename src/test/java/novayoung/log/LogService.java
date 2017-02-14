package novayoung.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 *
 * Created by pengchangguo on 17/1/5.
 */
@Component
public class LogService {

    private static final Logger logger = LoggerFactory.getLogger(LogService.class);

    @TraceIdJobFilter.TraceId
    public void log() {

        logger.info(new Date() + " this is a logService ! ");

    }
}

package io.keepup.cms.core.datasource.resources;

import org.apache.commons.logging.Log;
import java.io.OutputStream;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * {@link OutputStream} implementation to write FTP client logs
 */
public class LogOutputStream extends OutputStream {
    /** The logger where to log the written bytes. */
    private Log logger;


    /** The internal memory for the written bytes. */
    private String mem;

    /**
     * Creates a new log output stream which logs bytes to the specified logger with the specified
     * level.
     *
     * @param log the logger where to log the written bytes
     */
    public LogOutputStream (Log log) {
        setLogger(log);
        mem = EMPTY;
    }

    /**
     * Sets the logger where to log the bytes.
     *
     * @param log the logger
     */
    public void setLogger (Log log) {
        this.logger = log;
    }

    /**
     * Returns the logger.
     *
     * @return logger object
     */
    public Log getLogger () {
        return logger;
    }


    /**
     * Writes a byte to the output stream. This method flushes automatically at the end of a line.
     *
     * @param byteValue value to write
     */
    public void write (int byteValue) {
        byte[] bytes = new byte[1];
        bytes[0] = (byte) (byteValue & 0xff);
        mem = mem + new String(bytes);

        if (mem.endsWith ("\n")) {
            mem = mem.substring (0, mem.length () - 1);
            flush ();
        }
    }

    /**
     * Flushes the output stream.
     */
    @Override
    public void flush () {
        logger.debug(mem);
        mem = EMPTY;
    }
}

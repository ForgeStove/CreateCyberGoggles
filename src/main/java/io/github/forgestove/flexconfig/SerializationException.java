package io.github.forgestove.flexconfig;
import java.io.Serial;
public class SerializationException extends Exception {
	@Serial private static final long serialVersionUID = 1L;
	public SerializationException(Throwable cause) {
		super(cause);
	}
}

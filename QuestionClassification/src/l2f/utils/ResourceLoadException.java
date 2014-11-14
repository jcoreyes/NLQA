package l2f.utils;

/**
 * ResourceLoadException should be thrown when the system is unable
 * to load a resource, such as a Parser or a Lexicon, and hence
 * cannot proceed.
 * 
 * @author Jo�o
 */
public class ResourceLoadException extends RuntimeException {
	public ResourceLoadException(Throwable t) {
		super(t);
	}
	public ResourceLoadException(String message, Throwable t) {
		super(message, t);
	}
	private static final long serialVersionUID = 7205122371826031824L;
}

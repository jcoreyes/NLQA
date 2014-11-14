package l2f.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Utilities class for file manipulation, borrowed from LingMed.
 */
public class FileUtils {


	public static long getFileSizeMB(String filename) {
		return getFileSize(filename)/(1024*1024);
	}


	public static long getFileSize(String filename) {

		File file = new File(filename);

		if (!file.exists() || !file.isFile()) {
			System.out.println("File " + filename + " doesn\'t exist");
			return -1;
		}

		//Here we get the actual size
		return file.length();
	}

	/**
	 * Check existence, permissions on index directory.
	 * Index must be directory, with read and write permission.
	 * Creates index dir, if doesn't exist.
	 *
	 * @param dirName directory pathname
	 * @throws IOException
	 */
	public static File checkIndex(String dirName) throws IOException {
		return checkIndex(dirName, true);
	}

	/**
	 * Check existence, permissions on index directory.
	 *
	 * @param dirName directory pathname
	 * @param createIfNotExists whether or not to create new directory for pathname
	 * @throws IOException
	 */
	public static File checkIndex(String dirName, boolean createIfNotExists) throws IOException {
		File dir = new File(dirName);
		if (!dir.exists() && !createIfNotExists) {
			String msg = "Error, no such index: " + dir.getAbsolutePath();
			throw new IOException(msg);
		}
		if (!dir.exists()) {
			dir.mkdirs();
			return checkIndex(dirName, false);
		} else {
			if (!dir.isDirectory()) {
				String msg = "Error, not a directory: " + dir.getAbsolutePath();
				throw new IOException(msg);
			}
			if (!dir.canRead()) {
				String msg = "Error, cannot read index file: " + dir.getAbsolutePath();
				throw new IOException(msg);
			}
			if (!dir.canWrite()) {
				String msg = "Error, cannot write to index file: " + dir.getAbsolutePath();
				throw new IOException(msg);
			}
		}
		return dir;
	}

	/**
	 * Check existence, permissions on input file.
	 *
	 * @param name file name
	 * @throws IOException
	 */
	public static File checkInputFile(String name) {
		File file = new File(name);
		if (!(file.exists() && file.isFile() && file.canRead())) {
			String msg = "File missing or incorrect: " + name;
			throw new IllegalArgumentException(msg);
		}
		return file;
	}

	/**
	 * Check existence, permissions on input file.
	 *
	 * @param file input file
	 * @throws IOException
	 */
	public static boolean checkInputFile(File file) {
		if (!(file.exists() && file.isFile() && file.canRead())) {
			return false;
		}
		return true;
	}

	/**
	 * Check existence, permissions on output file.
	 * Create if not exists
	 *
	 * @param name file name
	 * @throws IOException
	 */
	public static File checkOutputFile(String name) throws IOException {
		File file = new File(name);
		if (!file.exists()) {
			file.createNewFile();
		}
		if (!(file.isFile() && file.canWrite())) {
			String msg = "File missing or incorrect: " + name;
			throw new IllegalArgumentException(msg);
		}
		return file;
	}

	/**
	 * Check if dir exists
	 *
	 * @param name directory name
	 * @throws IOException
	 */
	public static File checkDir(String name) throws IOException {
		File file = new File(name);
		if (!(file.exists() && file.isDirectory())) {
			String msg = "No such directory: " + name;
			throw new IllegalArgumentException(msg);
		}
		return file;
	}

	/**
	 * Check that existing file is directory, create dir if not exists.
	 *
	 * @param dir directory
	 * @throws IOException
	 */
	public static void ensureDirExists(File dir) throws IOException {
		if (dir.isDirectory()) {
			return;
		}
		if (dir.exists()) {
			String msg = "Existing file must be directory."
					+ " Found file=" + dir;
			throw new IOException(msg);
		}
		dir.mkdirs();
	}


	@SuppressWarnings("rawtypes")
	public static Class[] getClasses(String packageName)
			throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<File>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}
		ArrayList<Class> classes = new ArrayList<Class>();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}
		return classes.toArray(new Class[classes.size()]);
	}

	@SuppressWarnings("rawtypes")
	public static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
		List<Class> classes = new ArrayList<Class>();
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file, packageName + "." + file.getName()));
			} else if (file.getName().endsWith(".class")) {
				classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
			}
		}
		return classes;
	}

	private FileUtils() {}
}

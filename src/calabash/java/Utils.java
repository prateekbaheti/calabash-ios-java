/**
 * 
 */
package calabash.java;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jruby.RubyArray;
import org.jruby.RubyHash;
import org.jruby.RubyObject;

final class Utils {

	public static String getStringFromHash(RubyHash target, String key) {
		try {
			Object value = target.get(key);
			if (value != null)
				return value.toString();
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	public static Integer getIntFromHash(RubyHash target, String key) {
		String value = getStringFromHash(target, key);
		if (value != null) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				return null;
			}
		}

		return null;
	}

	public static void unzip(File zipFile, File destination)
			throws CalabashException {
		if (!zipFile.exists())
			throw new CalabashException("Zip file does not exists. "
					+ zipFile.getAbsolutePath());
		if (!zipFile.isFile())
			throw new CalabashException("Zip file should be a file. "
					+ zipFile.getAbsolutePath());

		if (!destination.exists())
			throw new CalabashException(
					"Destination directory does not exists. "
							+ destination.getAbsolutePath());
		if (!destination.isDirectory())
			throw new CalabashException("Destination is not a directory. "
					+ destination.getAbsolutePath());
		if (!destination.canWrite())
			throw new CalabashException("Destination is readonly. "
					+ destination.getAbsolutePath());

		final String[] command = { "unzip", "-uo", "-qq",
				zipFile.getAbsolutePath(), "-d", destination.getAbsolutePath() };
		try {
			Process process = Runtime.getRuntime().exec(command);
			int exitCode = process.waitFor();
			if (exitCode == 0 || exitCode == 1)
				return;
			else
				throw new CalabashException(String.format(
						"Failed to unzip %s to %s", zipFile.getAbsolutePath(),
						destination.getAbsolutePath()));

		} catch (Exception e) {
			throw new CalabashException(String.format(
					"Failed to unzip %s to %s. %s", zipFile.getAbsolutePath(),
					destination.getAbsolutePath(), e.getMessage()), e);
		}
	}

	public static void sleep(int ms) {
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
		}
	}

	public static Object[] toJavaArray(RubyArray array) {
		ArrayList<Object> result = new ArrayList<Object>();
		for (int i = 0; i < array.size(); i++) {
			Object rubyObject = array.get(i);
			Object javaObject = toJavaObject(rubyObject);
			result.add(javaObject);
		}

		return result.toArray();
	}

	public static Object toJavaObject(Object rubyObject) {
		if (rubyObject == null)
			return rubyObject;

		if (rubyObject instanceof RubyArray)
			return toJavaArray((RubyArray) rubyObject);
		if (rubyObject instanceof RubyHash)
			return toJavaHash((RubyHash) rubyObject);
		if (rubyObject instanceof RubyObject)
			return ((RubyObject) rubyObject).toJava(Object.class);

		return rubyObject.toString();
	}

	public static Map<?, ?> toJavaHash(RubyHash rubyHash) {
		HashMap<Object, Object> map = new HashMap<Object, Object>();
		Set<?> keySet = rubyHash.keySet();
		for (Object rubyKey : keySet) {
			Object rubyValue = rubyHash.get(rubyKey);
			Object javaKey = toJavaObject(rubyKey);
			Object javaValue = toJavaObject(rubyValue);
			map.put(javaKey, javaValue);
		}
		return map;
	}

	public static void inspectElement(UIElement element, int nestingLevel,
			InspectCallback callback) throws CalabashException {
		callback.onEachElement(element, nestingLevel);
		UIElements children = element.children();
		for (UIElement child : children) {
			inspectElement(child, nestingLevel + 1, callback);
		}
	}

    public static void runCommand(String[] command, String onExceptionMessage) throws CalabashException {
        int exitCode;
        try {
            Process changeDirProcess = Runtime.getRuntime().exec(command);
            exitCode = changeDirProcess.waitFor();
            if (exitCode == 0)
                return;
            else throw new CalabashException(onExceptionMessage);
        } catch (Exception e) {
            throw new CalabashException(onExceptionMessage);
        }
    }

}

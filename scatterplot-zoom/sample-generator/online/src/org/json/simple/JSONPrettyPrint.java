/**
 * Class created on 24/11/2010
 */
package org.json.simple;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class will give an indented output, the idea is to have a pretty print that looks like:
 * 
 * {
 * "k3":["lv1","lv2"],
 * "k1":"v1",
 * "k2":
 * {
 * "mk1":"mv1",
 * "mk2":["lv1","lv2"]
 * }
 * }
 * 
 * And at the same time respect the format of the application (SimpleJSON) and its performance.
 * 
 * @author Fátima Silveira <fawixfa@gmail.com>
 * 
 */
/*
 * Personal Opinion:
 * 
 * I left the array in one line because for me it seems very logic, if a future customization
 * wants it is easy to break the array to make it pretty one under the other
 * but i don't think that it will be "read-able"
 */
@SuppressWarnings("unchecked")
public class JSONPrettyPrint extends LinkedHashMap implements Map, JSONAware, JSONStreamAware {
	/**
	 * Automatically generated serial version UID
	 */
	private static final long serialVersionUID = -9168577804652055206L;
	private static int curly_brackets = 0;

	/**
	 * Encode a map into JSON text and write pretty print it.
	 * If this map is also a JSONAware or JSONStreamAware, JSONAware or JSONStreamAware specific behaviors will be
	 * ignored at this top level.
	 * 
	 * @see org.json.simple.JSONValue#writePrettyJSONString(Object, Writer)
	 * 
	 * @param map
	 * @param out
	 */
	/*
	 * The logic behind the indent is to count the brackets {} to be able to break line and to tab correctly
	 */
	public static void writeJSONString(Map map, Writer out) throws IOException {
		if (map == null) {
			out.write("null");
			return;
		}

		boolean first = true;
		String newLine = System.getProperty("line.separator");

		Iterator iter = map.entrySet().iterator();

		if (curly_brackets != 0) {
			out.write(newLine);

			for (int i = 0; i < curly_brackets; i++)
				out.write('\t');
		}

		out.write('{');
		curly_brackets++;

		out.write(newLine);

		while (iter.hasNext()) {
			if (first)
				first = false;
			else {
				out.write(',');
				out.write(newLine);
			}

			for (int i = 0; i < curly_brackets; i++)
				out.write('\t');

			Map.Entry entry = (Map.Entry) iter.next();
			out.write('\"');
			out.write(escape(String.valueOf(entry.getKey())));
			out.write('\"');
			out.write(':');

			JSONValue.writePrettyJSONString(entry.getValue(), out);
		}

		out.write(newLine);
		curly_brackets--;

		for (int i = 0; i < curly_brackets; i++)
			out.write('\t');

		out.write('}');
	}

	@Override
	public void writeJSONString(Writer out) throws IOException {
		writeJSONString(this, out);
	}

	/**
	 * Convert a map to formatted JSON text. The result is a JSON object.
	 * If this map is also a JSONAware, JSONAware specific behaviors will be omitted at this top level.
	 * 
	 * @see org.json.simple.JSONValue#toPrettyJSONString(Object)
	 * 
	 * @param map
	 * @return JSON text, or "null" if map is null.
	 */
	public static String toJSONString(Map map) {
		if (true)
			return map.toString();
		if (map == null)
			return "null";

		StringBuffer sb = new StringBuffer();
		boolean first = true;
		Iterator iter = map.entrySet().iterator();

		if (curly_brackets != 0) {
			sb.append('\n');

			for (int i = 0; i < curly_brackets; i++)
				sb.append('\t');
		}

		sb.append('{');
		curly_brackets++;

		sb.append('\n');

		while (iter.hasNext()) {
			if (first)
				first = false;
			else {
				sb.append(',');
				sb.append('\n');
			}

			for (int i = 0; i < curly_brackets; i++)
				sb.append('\t');

			Map.Entry entry = (Map.Entry) iter.next();
			toJSONString(String.valueOf(entry.getKey()), entry.getValue(), sb);
		}

		sb.append('\n');
		curly_brackets--;

		for (int i = 0; i < curly_brackets; i++)
			sb.append('\t');

		sb.append('}');
		return sb.toString();
	}

	@Override
	public String toJSONString() {
		return toJSONString(this);
	}

	private static String toJSONString(String key, Object value, StringBuffer sb) {
		sb.append('\"');
		if (key == null)
			sb.append("null");
		else
			JSONValue.escape(key, sb);
		sb.append('\"').append(':');

		sb.append(JSONValue.toPrettyJSONString(value));

		return sb.toString();
	}

	@Override
	public String toString() {
		return toJSONString();
	}

	public static String toString(String key, Object value) {
		StringBuffer sb = new StringBuffer();
		toJSONString(key, value, sb);
		return sb.toString();
	}

	/**
	 * Escape quotes, \, /, \r, \n, \b, \f, \t and other control characters (U+0000 through U+001F).
	 * It's the same as JSONValue.escape() only for compatibility here.
	 * 
	 * @see org.json.simple.JSONValue#escape(String)
	 * 
	 * @param s
	 * @return
	 */
	public static String escape(String s) {
		return JSONValue.escape(s);
	}
}

package x42t.lod.utils;

/**
 * @author grozadilla
 *
 *
 */
public enum X42TMethodtype {

	POST("POST"),
	GET("GET");

	private String methodtypestring;

	X42TMethodtype(String methodtypestring) {
		this.methodtypestring = methodtypestring;
	}

	public String methodtypevalue() {
		return methodtypestring;
	}
}
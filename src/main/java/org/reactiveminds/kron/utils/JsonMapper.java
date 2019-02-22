package org.reactiveminds.kron.utils;

import org.reactiveminds.kron.err.KronRuntimeException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonMapper {

	private JsonMapper() {
	}
	private static class Wrapper{
		private static final ObjectMapper mapper = new ObjectMapper()
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
				//.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
				.enable(SerializationFeature.INDENT_OUTPUT)
				//.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
				;
	}
	
	public static <T> String toString(T object) {
		try {
			return Wrapper.mapper.writerFor(object.getClass()).withDefaultPrettyPrinter().writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new KronRuntimeException("Json exception", e);
		}
	}
	/**
	 * Serialize to a Json string.
	 * @param <T>
	 * @param object
	 * @return
	 */
	public static <T> String serialize(T object) {
		try {
			return Wrapper.mapper.writerFor(object.getClass()).writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new KronRuntimeException("Json exception", e);
		}
	}
	/**
	 * Marshall into a bean from a json string.
	 * @param json
	 * @param type
	 * @return
	 */
	public static <T> T deserialize(String json, Class<T> type) {
		try {
			return Wrapper.mapper.readerFor(type).readValue(json);
		} catch (Exception e) {
			throw new KronRuntimeException("Json exception", e);
		}
	}
	public static <T> T deserialize(byte[] json, Class<T> type) {
		try {
			return Wrapper.mapper.readerFor(type).readValue(json);
		} catch (Exception e) {
			throw new KronRuntimeException("Json exception", e);
		}
	}
	public static <T> T deserialize(String json, String classFQN) {
		try {
			return Wrapper.mapper.readerFor(Class.forName(classFQN)).readValue(json);
		} catch (Exception e) {
			throw new KronRuntimeException("Json exception", e);
		}
	}
	
}

package org.mechaverse.simulation.common.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.mechaverse.simulation.common.model.Direction;
import org.mechaverse.simulation.common.model.SimulationModel;

/**
 * Utility methods for {@link SimulationModel}.
 */
public final class SimulationModelUtil {

  public static final Direction[] DIRECTIONS = Direction.values();

  private static final int DEFAULT_GZIP_BUFFER_SIZE = 128 * 1024;

  /**
   * Serializes the given model to json written to the given {@link OutputStream}.
   *
   * @param model the model to serialize
   * @param out the output stream to which serialized data will be written
   */
  private static void serialize(SimulationModel model, OutputStream out)
      throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setSerializationInclusion(Include.NON_NULL);
    objectMapper.writeValue(out, model);
    out.close();
  }

  public static <SIM_MODEL extends SimulationModel> SIM_MODEL deserialize(byte[] data, Class[] classesToBeBound, Class<SIM_MODEL> simulationModelClass) throws IOException {
    if (data == null) {
      return null;
    }
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.registerSubtypes(classesToBeBound);
    return objectMapper.readValue(new GZIPInputStream(new ByteArrayInputStream(data)), simulationModelClass);
  }

  /**
   * Serializes a {@link SimulationModel} to a byte array that contains the compressed json
   * representation of the model.
   */
  public static byte[] serialize(SimulationModel model) throws IOException {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    try (OutputStream out = new GZIPOutputStream(byteOut, DEFAULT_GZIP_BUFFER_SIZE)) {
      serialize(model, out);
    }
    return byteOut.toByteArray();
  }

  private SimulationModelUtil() {}
}

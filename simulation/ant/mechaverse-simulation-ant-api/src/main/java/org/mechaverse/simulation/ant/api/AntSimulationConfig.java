package org.mechaverse.simulation.ant.api;

import static java.lang.Integer.parseInt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Configuration for the ant simulation.
 */
public final class AntSimulationConfig {

  private final Properties properties;

  public static AntSimulationConfig deserialize(byte[] data) throws IOException {
    return deserialize(new ByteArrayInputStream(data));
  }

  public static AntSimulationConfig deserialize(InputStream in) throws IOException {
    return new AntSimulationConfig(load(in));
  }

  public AntSimulationConfig() {
    this(new Properties());
  }

  public AntSimulationConfig(Properties properties) {
    this.properties = properties;
  }

  public int getTargetAntCount() {
    return parseInt(properties.getProperty("targetAntCount", "500"));
  }

  public int getAntInitialEnergy() {
    return parseInt(properties.getProperty("antInitialEnergy", "1000"));
  }

  public int getMinFoodCount() {
    return parseInt(properties.getProperty("minFoodCount", "1000"));
  }

  public int getFoodClusterRadius() {
    return parseInt(properties.getProperty("foodClusterRadius", "15"));
  }

  public int getFoodInitialEnergy() {
    return parseInt(properties.getProperty("foodInitialEnergy", "300"));
  }

  public int getPheromoneInitialEnergy() {
    return parseInt(properties.getProperty("pheromoneInitialEnergy", "3600"));
  }

  public byte[] serialize() throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serialize(out);
    return out.toByteArray();
  }

  public void serialize(OutputStream out) throws IOException {
    properties.store(out, "");
  }

  private static Properties load(InputStream in) throws IOException {
    Properties properties = new Properties();
    properties.load(in);
    return properties;
  }
}

package org.mechaverse.service.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.mechaverse.service.storage.api.MechaverseStorageService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of {@link MechaverseStorageService}.
 */
public class MechaverseStorageServiceImpl implements MechaverseStorageService {

  @Autowired private StorageServiceConfig config;

  @Override
  public InputStream getState(String simulationId, String instanceId, long iteration)
      throws IOException {
    return new FileInputStream(getFilename(simulationId, instanceId, iteration));
  }

  @Override
  public InputStream getStateValue(
      String simulationId, String instanceId, long iteration, String key) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setState(String simulationId, String instanceId, long iteration,
      InputStream stateInput) throws IOException {
    // Write the state to a temporary file.
    File tempDirectory = new File(config.getTempPath());
    tempDirectory.mkdirs();
    String tempPrefix = String.format("%s-%s-%s", simulationId, instanceId, iteration);
    File tempFile = File.createTempFile(tempPrefix, null, tempDirectory);
    tempFile.deleteOnExit();
    OutputStream out = new FileOutputStream(tempFile);
    try {
      IOUtils.copy(stateInput, out);
    } finally {
      out.close();
    }

    // Move the temporary file to the proper location.
    File stateFile = new File(getFilename(simulationId, instanceId, iteration));
    stateFile.getParentFile().mkdirs();
    tempFile.renameTo(stateFile);
  }

  @Override
  public void setStateValue(String simulationId, String instanceId, long iteration, String key,
      InputStream valueInput) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteSimulation(String simulationId) throws IOException {
    File simulationDirectory = new File(getSimulationPath(simulationId));
    File tempDirectory = new File(config.getTempPath() + File.separator + simulationId);
    simulationDirectory.renameTo(tempDirectory);
    FileUtils.deleteDirectory(tempDirectory);
  }

  @Override
  public void deleteInstance(String simulationId, String instanceId) throws IOException {
    File instanceDirectory = new File(getInstancePath(simulationId, instanceId));
    File tempDirectory = new File(config.getTempPath() + File.separator + instanceId);
    instanceDirectory.renameTo(tempDirectory);
    FileUtils.deleteDirectory(tempDirectory);
  }

  private String getSimulationPath(String simulationId) {
    return config.getBasePath() + File.separator + simulationId;
  }

  private String getInstancePath(String simulationId, String instanceId) {
    // TODO(thorntonv): Balance directory structure for better performance.
    return getSimulationPath(simulationId) + File.separator + instanceId;
  }

  private String getFilename(String simulationId, String instanceId, long iteration) {
    // TODO(thorntonv): Balance directory structure for better performance.
    return getInstancePath(simulationId, instanceId) + File.separator + iteration;
  }
}

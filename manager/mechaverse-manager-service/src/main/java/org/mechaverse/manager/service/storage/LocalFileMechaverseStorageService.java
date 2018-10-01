package org.mechaverse.manager.service.storage;

import com.google.common.annotations.VisibleForTesting;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;

/**
 * Implementation of {@link MechaverseStorageService}.
 */
public class LocalFileMechaverseStorageService implements MechaverseStorageService {

  @Value("${simulationBasePath}")
  private String basePath;

  @Override
  public InputStream getState(String simulationId, String instanceId, long iteration)
      throws IOException {
    return new FileInputStream(getFilename(simulationId, instanceId, iteration));
  }

  @Override
  public void setState(String simulationId, String instanceId, long iteration,
      InputStream stateInput) throws IOException {
    // Write the state to a temporary file.
    File tempDirectory = new File(getTempPath());
    tempDirectory.mkdirs();
    if (!tempDirectory.exists()) {
      throw new IOException("Unable to create temp directory " + tempDirectory.getAbsolutePath());
    }
    String tempPrefix = String.format("%s-%s-%s", simulationId, instanceId, iteration);
    File tempFile = File.createTempFile(tempPrefix, null, tempDirectory);

    File stateFile = new File(getFilename(simulationId, instanceId, iteration));
    stateFile.getParentFile().mkdirs();
    if (!stateFile.getParentFile().exists()) {
      throw new IOException(
          "Unable to create parent directory " + stateFile.getParentFile().getAbsolutePath());
    }

    try {
      try (OutputStream out = new FileOutputStream(tempFile)) {
        IOUtils.copy(stateInput, out);
      }

      // Move the temporary file to the proper location.
      if (!tempFile.renameTo(stateFile)) {
        throw new IOException("Unable to rename temp file " + tempFile.getAbsolutePath());
      }
    } catch (Exception ex) {
      tempFile.delete();
    }
  }

  @Override
  public void deleteSimulation(String simulationId) throws IOException {
    File simulationDirectory = new File(getSimulationPath(simulationId));
    File tempDirectory = new File(getTempPath() + File.separator + simulationId);
    simulationDirectory.renameTo(tempDirectory);
    FileUtils.deleteDirectory(tempDirectory);
  }

  @Override
  public void deleteInstance(String simulationId, String instanceId) throws IOException {
    File instanceDirectory = new File(getInstancePath(simulationId, instanceId));
    File tempDirectory = new File(getTempPath() + File.separator + instanceId);
    instanceDirectory.renameTo(tempDirectory);
    FileUtils.deleteDirectory(tempDirectory);
  }

  @VisibleForTesting
  void setBasePath(String basePath) {
    this.basePath = basePath;
  }

  private String getSimulationPath(String simulationId) {
    return basePath + File.separator + simulationId;
  }

  private String getInstancePath(String simulationId, String instanceId) {
    // TODO(thorntonv): Balance directory structure for better scalability.
    return getSimulationPath(simulationId) + File.separator + instanceId;
  }

  private String getFilename(String simulationId, String instanceId, long iteration) {
    // TODO(thorntonv): Balance directory structure for better scalability.
    return getInstancePath(simulationId, instanceId) + File.separator + iteration;
  }

  private String getTempPath() {
    return basePath + File.separator + "temp";
  }
}

package org.mechaverse.service.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.cxf.helpers.IOUtils;
import org.mechaverse.service.storage.api.MechaverseStorageService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of {@link MechaverseStorageService}.
 */
public class MechaverseStorageServiceImpl implements MechaverseStorageService {

  @Autowired private StorageServiceConfig config;

  @Override
  public InputStream getState(String simulationId, String instanceId, long iteration)
      throws Exception {
    return new FileInputStream(getFilename(simulationId, instanceId, iteration));
  }

  @Override
  public InputStream getStateValue(
      String simulationId, String instanceId, long iteration, String key) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setState(String simulationId, String instanceId, long iteration,
      InputStream stateInput) throws Exception {
    // Write the state to a temporary file.
    File tempDirectory = new File(config.getTempPath());
    tempDirectory.mkdirs();
    String tempPrefix = String.format("%s-%s-%s", simulationId, instanceId, iteration);
    File tempFile = File.createTempFile(tempPrefix, null, tempDirectory);
    tempFile.deleteOnExit();
    OutputStream out = new FileOutputStream(tempFile);
    try {
      IOUtils.copyAndCloseInput(stateInput, out);
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
      InputStream valueInput) throws Exception {
    throw new UnsupportedOperationException();
  }

  private String getFilename(String simulationId, String instanceId, long iteration) {
    // TODO(thorntonv): Balance directory structure for better performance.
    return config.getBasePath() + File.separator + simulationId + File.separator + instanceId
        + File.separator + iteration;
  }
}

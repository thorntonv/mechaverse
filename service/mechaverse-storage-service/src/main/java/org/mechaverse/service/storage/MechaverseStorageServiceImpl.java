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

  // TODO(thorntonv): Support atomic read/write.

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
    File stateFile = new File(getFilename(simulationId, instanceId, iteration));
    stateFile.getParentFile().mkdirs();
    OutputStream out = new FileOutputStream(stateFile);
    try {
      IOUtils.copyAndCloseInput(stateInput, out);
    } finally {
      out.close();
    }
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

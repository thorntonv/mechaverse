package org.mechaverse.service.storage;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import org.apache.cxf.helpers.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mechaverse.service.storage.api.MechaverseStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Unit test for {@link MechaverseStorageServiceImpl}.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-context.xml")
public class MechaverseStorageServiceImplTest {

  @Autowired StorageServiceConfig config;
  @Autowired MechaverseStorageService service;
  @Rule public TemporaryFolder folder = new TemporaryFolder();

  @Test
  public void persistState() throws Exception {
    File basePath = folder.newFolder();
    config.setBasePath(basePath.getAbsolutePath());
    service.setState("1", "1", 0, new ByteArrayInputStream("This is a test.".getBytes()));
    InputStream stateIn = service.getState("1", "1", 0);

    assertEquals("This is a test.", IOUtils.readStringFromStream(stateIn));
  }
}

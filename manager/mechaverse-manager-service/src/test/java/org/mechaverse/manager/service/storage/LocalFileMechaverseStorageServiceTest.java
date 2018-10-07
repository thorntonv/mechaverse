package org.mechaverse.manager.service.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.google.common.base.Charsets;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Unit test for {@link LocalFileMechaverseStorageService}.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/storage-test-context.xml")
public class LocalFileMechaverseStorageServiceTest {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  private LocalFileMechaverseStorageService service;

  @Before
  public void setUp() {
    service = new LocalFileMechaverseStorageService();
  }

  @Test
  public void persistState() throws Exception {
    File basePath = folder.newFolder();
    service.setBasePath(basePath.getAbsolutePath());
    service.setState("1", "1", 0, new ByteArrayInputStream("This is a test.".getBytes()));
    InputStream stateIn = service.getState("1", "1", 0);

    assertEquals(5, FileUtils
        .listFilesAndDirs(basePath, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE).size());

    assertEquals("This is a test.", IOUtils.toString(stateIn, Charsets.UTF_8));
  }

  @Test
  public void removeSimulation() throws Exception {
    File basePath = folder.newFolder();
    service.setBasePath(basePath.getAbsolutePath());
    service.setState("1", "1", 0, new ByteArrayInputStream("This is a test.".getBytes()));

    assertEquals(5, FileUtils
        .listFilesAndDirs(basePath, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE).size());

    service.deleteSimulation("1");
    try {
      service.getState("1", "1", 0);
      fail("Expected exception was not thrown.");
    } catch(FileNotFoundException ex) {
      // Expected.
    }

    assertEquals(2, FileUtils
        .listFilesAndDirs(basePath, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE).size());
  }

  @Test
  public void removeInstance() throws Exception {
    File basePath = folder.newFolder();
    service.setBasePath(basePath.getAbsolutePath());
    service.setState("1", "1", 0, new ByteArrayInputStream("This is a test.".getBytes()));

    assertEquals(5, FileUtils
        .listFilesAndDirs(basePath, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE).size());

    service.deleteInstance("1", "1");
    try {
      service.getState("1", "1", 0);
      fail("Expected exception was not thrown.");
    } catch(FileNotFoundException ex) {
      // Expected.
    }

    assertEquals(3, FileUtils
        .listFilesAndDirs(basePath, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE).size());
  }
}

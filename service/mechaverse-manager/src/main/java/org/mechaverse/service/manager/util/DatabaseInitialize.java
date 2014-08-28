package org.mechaverse.service.manager.util;

import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;

/**
 * Utility to initialize the database.
 */
public class DatabaseInitialize {

  public static void main(String args[]) {
    Configuration cfg = new AnnotationConfiguration();
    cfg.configure("hibernate.cfg.xml");

    SchemaExport se = new SchemaExport(cfg);

    se.create(true, true);
  }
}

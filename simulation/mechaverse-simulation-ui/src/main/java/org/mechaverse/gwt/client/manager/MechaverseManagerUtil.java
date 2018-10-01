package org.mechaverse.gwt.client.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.mechaverse.manager.api.model.Task;

public class MechaverseManagerUtil {

  /**
   * Returns a list of tasks whose age is less than the given maximum.
   */
  public static List<Task> getActiveTasks(Iterable<Task> tasks, long taskMaxDurationSeconds) {
    List<Task> activeTasks = new ArrayList<>();
    for (Task task : tasks) {
      if (isActive(task, taskMaxDurationSeconds)) {
        activeTasks.add(task);
      }
    }
    return activeTasks;
  }

  /**
   * Returns true if the age of the given task is less than the given maximum, false otherwise.
   */
  public static boolean isActive(Task task, long taskMaxDurationSeconds) {
    long taskMaxDurationMS = taskMaxDurationSeconds * 1000;
    long now = new Date().getTime();
    return (now - task.getStartTimeMillis() < taskMaxDurationMS);
  }
}

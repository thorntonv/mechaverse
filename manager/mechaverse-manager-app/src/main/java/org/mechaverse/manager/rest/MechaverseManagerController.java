package org.mechaverse.manager.rest;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import com.google.common.base.Preconditions;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.mechaverse.manager.service.MechaverseManagerService;
import org.mechaverse.manager.service.model.SimulationConfig;
import org.mechaverse.manager.service.model.SimulationInfo;
import org.mechaverse.manager.service.model.Task;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/manager")
@Api(value = "MechaverseManager", tags = "MechaverseManager")
public class MechaverseManagerController {

  private final MechaverseManagerService managerService;

  public MechaverseManagerController(final MechaverseManagerService managerService) {
    this.managerService = Preconditions.checkNotNull(managerService);
  }

  @RequestMapping(value = "/clients/{clientId}/task",
      method = GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "getTask", nickname = "getTask")
  public Task getTask(@PathVariable String clientId) {
    return managerService.getTask(clientId);
  }

  @RequestMapping(value = "/tasks/{taskId}/result",
      method = PUT,
      consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @ApiOperation(value = "submitResult", nickname = "submitResult")
  public void submitResult(@PathVariable long taskId, @RequestBody byte[] resultData) throws Exception {
    try(InputStream in = new ByteArrayInputStream(resultData)) {
      managerService.submitResult(taskId, in);
    }
  }

  @RequestMapping(value = "/simulations", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "getAllSimulationInfo", nickname = "getAllSimulationInfo")
  public List<SimulationInfo> getAllSimulationInfo() {
    return managerService.getSimulationInfo();
  }

  @RequestMapping(value = "/simulations", method = POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "createSimulation", nickname = "createSimulation")
  public SimulationInfo createSimulation(@RequestParam String name) {
    return managerService.createSimulation(name);
  }

  @RequestMapping(value = "/simulations/{simulationId}/active", method = PUT)
  @ApiOperation(value = "setSimulationActive", nickname = "setSimulationActive")
  public void setSimulationActive(@PathVariable String simulationId, @RequestParam boolean active) {
    managerService.setSimulationActive(simulationId, active);
  }

  @RequestMapping(value = "/simulations/{simulationId}", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "getSimulationInfo", nickname = "getSimulationInfo")
  public SimulationInfo getSimulationInfo(@PathVariable String simulationId) {
    return managerService.getSimulationInfo(simulationId);
  }

  @RequestMapping(value = "/simulations/{simulationId}/instances/{instanceId}/iterations/{iteration}/state",
      method = GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @ApiOperation(value = "getState", nickname = "getState")
  public byte[] getState(@PathVariable String simulationId,
      @PathVariable String instanceId, @PathVariable long iteration) throws IOException {
    return IOUtils.toByteArray(managerService.getState(simulationId, instanceId, iteration));
  }

  @RequestMapping(value = "/simulations/config", method = POST,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "updateSimulationConfig", nickname = "updateSimulationConfig")
  public void updateSimulationConfig(@RequestBody SimulationConfig config) {
    managerService.updateSimulationConfig(config);
  }

  @RequestMapping(value = "/simulations/{simulationId}", method = DELETE)
  @ApiOperation(value = "deleteSimulation", nickname = "deleteSimulation")
  public void deleteSimulation(@PathVariable String simulationId) throws Exception {
    managerService.deleteSimulation(simulationId);
  }
}

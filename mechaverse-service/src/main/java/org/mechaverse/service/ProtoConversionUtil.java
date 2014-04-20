package org.mechaverse.service;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

import org.mechaverse.api.model.simulation.ant.Ant;
import org.mechaverse.api.model.simulation.ant.Barrier;
import org.mechaverse.api.model.simulation.ant.Conduit;
import org.mechaverse.api.model.simulation.ant.Direction;
import org.mechaverse.api.model.simulation.ant.Entity;
import org.mechaverse.api.model.simulation.ant.Environment;
import org.mechaverse.api.model.simulation.ant.Food;
import org.mechaverse.api.model.simulation.ant.InputPositions;
import org.mechaverse.api.model.simulation.ant.OutputPositions;
import org.mechaverse.api.model.simulation.ant.Pheromone;
import org.mechaverse.api.model.simulation.ant.Rock;
import org.mechaverse.api.model.simulation.ant.SimulationState;
import org.mechaverse.api.proto.simulation.ant.AntSimulation;
import org.mechaverse.api.proto.simulation.ant.AntSimulation.EntityState;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import com.google.common.base.CaseFormat;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;

/**
 * Utility methods for converting between schema generated and protocol buffer models.
 * 
 * @author thorntonv@mechaverse.org
 *
 */
public final class ProtoConversionUtil {

  private ProtoConversionUtil() {}

  /**
   * Converts a {@link AntSimulation.SimulationState} to a {@link SimulationState}.
   */
  public static SimulationState convert(AntSimulation.SimulationState stateProto) throws Exception {
    SimulationState state = new SimulationState();
    copyPropertiesFromProto(stateProto, state);
    state.setEnvironment(convert(stateProto.getEnvironment()));
    for (AntSimulation.Environment subEnvironmentProto : stateProto.getSubEnvironmentList()) {
      state.getSubEnvironments().add(convert(subEnvironmentProto));
    }
    return state;
  }

  /**
   * Converts a {@link SimulationState} to a {@link AntSimulation.SimulationState}.
   */
  public static AntSimulation.SimulationState convert(SimulationState state) throws Exception {
    AntSimulation.SimulationState.Builder stateProtoBuilder =
        AntSimulation.SimulationState.newBuilder();
    copyPropertiesToProto(state, stateProtoBuilder);
    stateProtoBuilder.setEnvironment(convert(state.getEnvironment()));
    for (Environment subEnvironment : state.getSubEnvironments()) {
      stateProtoBuilder.addSubEnvironment(convert(subEnvironment));
    }
    return stateProtoBuilder.build();
  }

  /**
   * Converts an {@link AntSimulation.Environment} to an {@link Environment}.
   */
  public static Environment convert(AntSimulation.Environment envProto) throws Exception {
    Environment env = new Environment();
    copyPropertiesFromProto(envProto, env);

    for (AntSimulation.Ant antProto : envProto.getAntList()) {
      env.getEntities().add(convert(antProto));
    }
    for (AntSimulation.Barrier barrierProto : envProto.getBarrierList()) {
      env.getEntities().add(convert(barrierProto));
    }
    for (AntSimulation.Conduit conduitProto : envProto.getConduitList()) {
      env.getEntities().add(convert(conduitProto));
    }
    for (AntSimulation.Food foodProto : envProto.getFoodList()) {
      env.getEntities().add(convert(foodProto));
    }
    for (AntSimulation.Pheromone pheromoneProto : envProto.getPheromoneList()) {
      env.getEntities().add(convert(pheromoneProto));
    }
    for (AntSimulation.Rock rockProto : envProto.getRockList()) {
      env.getEntities().add(convert(rockProto));
    }
    return env;
  }

  /**
   * Converts an {@link Environment} to an {@link AntSimulation.Environment}.
   */
  public static AntSimulation.Environment convert(Environment env) throws Exception {
    AntSimulation.Environment.Builder envProtoBuilder = AntSimulation.Environment.newBuilder();
    copyPropertiesToProto(env, envProtoBuilder);

    for (Entity entity : env.getEntities()) {
      if (entity instanceof Ant) {
        envProtoBuilder.addAnt(convert((Ant) entity));
      } else if (entity instanceof Barrier) {
        envProtoBuilder.addBarrier(convert((Barrier) entity));
      } else if (entity instanceof Conduit) {
        envProtoBuilder.addConduit(convert((Conduit) entity));
      } else if (entity instanceof Food) {
        envProtoBuilder.addFood(convert((Food) entity));
      } else if (entity instanceof Pheromone) {
        envProtoBuilder.addPheromone(convert((Pheromone) entity));
      } else if (entity instanceof Rock) {
        envProtoBuilder.addRock(convert((Rock) entity));
      }
    }
    return envProtoBuilder.build();
  }

  /**
   * Converts an {@link AntSimulation.Ant} to an {@link Ant}.
   */
  public static Ant convert(AntSimulation.Ant antProto) throws IllegalAccessException,
      InvocationTargetException {
    Ant ant = new Ant();
    setEntityState(ant, antProto.getEntityState());
    ant.setCarriedEntityId(antProto.hasCarriedEntityId() ? antProto.getCarriedEntityId() : null);
    ant.setData(antProto.hasData() ? antProto.getData().toByteArray() : null);
    if (antProto.hasInputPositions()) {
      InputPositions inputPositions = new InputPositions();
      copyPropertiesFromProto(antProto.getInputPositions(), inputPositions);
      ant.setInputPositions(inputPositions);
    }
    if (antProto.hasOutputPositions()) {
      OutputPositions outputPositions = new OutputPositions();
      copyPropertiesFromProto(antProto.getOutputPositions(), outputPositions);
      ant.setOutputPositions(outputPositions);
    }
    return ant;
  }

  /**
   * Converts an {@link Ant} to an {@link AntSimulation.Ant}.
   */
  public static AntSimulation.Ant convert(Ant ant) throws IllegalAccessException,
      InvocationTargetException {
    AntSimulation.Ant.Builder antBuilder = AntSimulation.Ant.newBuilder();
    antBuilder.setEntityState(getEntityState(ant));
    if (ant.getCarriedEntityId() != null) {
      antBuilder.setCarriedEntityId(ant.getCarriedEntityId());
    }
    if (ant.getData() != null) {
      antBuilder.setData(ByteString.copyFrom(ant.getData()));
    }
    if (ant.getInputPositions() != null) {
      AntSimulation.Ant.InputPositions.Builder inputPositionsBuilder =
          antBuilder.getInputPositionsBuilder();
      copyPropertiesToProto(ant.getInputPositions(), inputPositionsBuilder);
      antBuilder.setInputPositions(inputPositionsBuilder);
    }
    if (ant.getOutputPositions() != null) {
      AntSimulation.Ant.OutputPositions.Builder outputPositionsBuilder =
          antBuilder.getOutputPositionsBuilder();
      copyPropertiesToProto(ant.getOutputPositions(), outputPositionsBuilder);
      antBuilder.setOutputPositions(outputPositionsBuilder);
    }
    return antBuilder.build();
  }

  public static Barrier convert(AntSimulation.Barrier barrierProto) throws IllegalAccessException,
      InvocationTargetException {
    Barrier barrier = new Barrier();
    setEntityState(barrier, barrierProto.getEntityState());
    return barrier;
  }

  public static AntSimulation.Barrier convert(Barrier barrier) throws IllegalAccessException,
      InvocationTargetException {
    AntSimulation.Barrier.Builder barrierBuilder = AntSimulation.Barrier.newBuilder();
    barrierBuilder.setEntityState(getEntityState(barrier));
    return barrierBuilder.build();
  }

  public static Conduit convert(AntSimulation.Conduit conduitProto) throws IllegalAccessException,
      InvocationTargetException {
    Conduit conduit = new Conduit();
    setEntityState(conduit, conduitProto.getEntityState());
    copyPropertiesFromProto(conduitProto, conduit);
    return conduit;
  }

  public static AntSimulation.Conduit convert(Conduit conduit) throws Exception {
    AntSimulation.Conduit.Builder conduitBuilder = AntSimulation.Conduit.newBuilder();
    conduitBuilder.setEntityState(getEntityState(conduit));
    copyPropertiesToProto(conduit, conduitBuilder);
    return conduitBuilder.build();
  }

  public static Food convert(AntSimulation.Food foodProto) throws Exception {
    Food food = new Food();
    setEntityState(food, foodProto.getEntityState());
    return food;
  }

  public static AntSimulation.Food convert(Food food) throws Exception {
    AntSimulation.Food.Builder foodBuilder = AntSimulation.Food.newBuilder();
    foodBuilder.setEntityState(getEntityState(food));
    return foodBuilder.build();
  }

  public static Pheromone convert(AntSimulation.Pheromone pheromoneProto) throws Exception {
    Pheromone pheromone = new Pheromone();
    setEntityState(pheromone, pheromoneProto.getEntityState());
    copyPropertiesFromProto(pheromoneProto, pheromone);
    return pheromone;
  }

  public static AntSimulation.Pheromone convert(Pheromone pheromone) throws Exception {
    AntSimulation.Pheromone.Builder pheromoneBuilder = AntSimulation.Pheromone.newBuilder();
    pheromoneBuilder.setEntityState(getEntityState(pheromone));
    if (pheromone.getValue() != null) {
      pheromoneBuilder.setValue(pheromone.getValue());
    }
    return pheromoneBuilder.build();
  }

  public static Rock convert(AntSimulation.Rock rockProto) throws Exception {
    Rock rock = new Rock();
    setEntityState(rock, rockProto.getEntityState());
    return rock;
  }

  public static AntSimulation.Rock convert(Rock rock) throws Exception {
    AntSimulation.Rock.Builder rockBuilder = AntSimulation.Rock.newBuilder();
    rockBuilder.setEntityState(getEntityState(rock));
    return rockBuilder.build();
  }

  public static void setEntityState(Entity entity, AntSimulation.EntityState entityState)
      throws IllegalAccessException, InvocationTargetException {
    copyPropertiesFromProto(entityState, entity);

    if (entityState.hasDirection()) {
      entity.setDirection(Direction.valueOf(entityState.getDirection().name()));
    }
  }

  public static AntSimulation.EntityState getEntityState(Entity entity)
      throws IllegalAccessException, InvocationTargetException {
    EntityState.Builder entityStateBuilder = EntityState.newBuilder();
    copyPropertiesToProto(entity, entityStateBuilder);
    if (entity.getDirection() != null) {
      entityStateBuilder.setDirection(
        AntSimulation.Direction.valueOf(entity.getDirection().name()));
    }
    return entityStateBuilder.build();
  }

  protected static void copyPropertiesFromProto(Message src, Object dest) {
    BeanWrapper destWrapper = new BeanWrapperImpl(dest);

    for (PropertyDescriptor descriptor : destWrapper.getPropertyDescriptors()) {
      String protoFieldName =
          CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, descriptor.getName());
      FieldDescriptor fieldDescriptor = src.getDescriptorForType().findFieldByName(protoFieldName);
      if (fieldDescriptor != null && src.hasField(fieldDescriptor)
          && !fieldDescriptor.isRepeated()
          && !(fieldDescriptor.getJavaType() == JavaType.MESSAGE 
              || fieldDescriptor.getJavaType() == JavaType.ENUM)) {
        destWrapper.setPropertyValue(descriptor.getName(), src.getField(fieldDescriptor));
      }
    }
  }

  protected static void copyPropertiesToProto(Object src, GeneratedMessage.Builder<?> dest) {
    BeanWrapper srcWrapper = new BeanWrapperImpl(src);

    for (PropertyDescriptor descriptor : srcWrapper.getPropertyDescriptors()) {
      String protoFieldName =
          CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, descriptor.getName());
      Object value = srcWrapper.getPropertyValue(descriptor.getName());
      if (value != null) {
        FieldDescriptor fieldDescriptor =
            dest.getDescriptorForType().findFieldByName(protoFieldName);
        if (fieldDescriptor != null
            && !fieldDescriptor.isRepeated()
            && !(fieldDescriptor.getJavaType() == JavaType.MESSAGE 
                || fieldDescriptor.getJavaType() == JavaType.ENUM)) {
          dest.setField(fieldDescriptor, value);
        }
      }
    }
  }
}

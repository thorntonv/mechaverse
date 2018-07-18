package org.mechaverse.simulation.common.genetic;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.util.ArrayUtil;

/**EnvironmentModel
 * Used to store/retrieve {@link GeneticData} from an entity model.
 *
 * @author Vance Thornton <thorntonv@mechaverse.org>
 */
public class GeneticDataStore {

  private static final String DATA_KEY = "data";
  private static final String CROSSOVER_GROUPS_KEY = "crossoverGroups";
  private static final String CROSSOVER_SPLIT_POINTS_KEY = "crossoverSplitPoints";

  private static final String KEY_SEPARATOR = ".";

  private final EntityModel entityModel;
  private final String keyPrefix;

  public GeneticDataStore(final EntityModel entityModel) {
    this(entityModel, "geneticData");
  }

  public GeneticDataStore(final EntityModel entityModel, final String keyPrefix) {
    this.entityModel = entityModel;
    this.keyPrefix = keyPrefix;
  }

  public void put(String type, GeneticData geneticData) {
    entityModel.putData(getKey(type, DATA_KEY), geneticData.getData());
    entityModel.putData(getKey(type, CROSSOVER_GROUPS_KEY), ArrayUtil.toByteArray(geneticData.getCrossoverGroups()));
    entityModel.putData(getKey(type, CROSSOVER_SPLIT_POINTS_KEY),
        ArrayUtil.toByteArray(geneticData.getCrossoverSplitPoints()));
  }

  public GeneticData get(String type) {
    byte[] data = entityModel.getData(getKey(type, DATA_KEY));
    int[] crossoverGroups = ArrayUtil.toIntArray(entityModel.getData(getKey(type, CROSSOVER_GROUPS_KEY)));
    int[] crossoverSplitPoints = ArrayUtil.toIntArray(entityModel.getData(getKey(type, CROSSOVER_SPLIT_POINTS_KEY)));
    return new GeneticData(data, crossoverGroups, crossoverSplitPoints);
  }

  public void clear() {
    clear(null);
  }

  public void clear(String type) {
    entityModel.removeData(getKey(type, DATA_KEY));
    entityModel.removeData(getKey(type, CROSSOVER_GROUPS_KEY));
    entityModel.removeData(getKey(type, CROSSOVER_SPLIT_POINTS_KEY));
  }

  public boolean contains(String type) {
    return entityModel.dataContainsKey(getKey(type, DATA_KEY)) ||
        entityModel.dataContainsKey(getKey(type, CROSSOVER_GROUPS_KEY)) ||
        entityModel.dataContainsKey(getKey(type, CROSSOVER_SPLIT_POINTS_KEY));
  }

  private String getKey(String type, String key) {
    return Joiner.on(KEY_SEPARATOR).skipNulls()
        .join(Strings.emptyToNull(keyPrefix), Strings.emptyToNull(type), key);
  }
}

package com.corosus.coroutil.loader.fabric;

import com.corosus.coroutil.util.CULog;
import com.corosus.modconfig.CoroConfigRegistry;
import com.corosus.modconfig.IConfigCategory;
import com.corosus.modconfig.ModConfigData;

import java.lang.reflect.Field;

/**
 * Snapshot fallback implementation used when ForgeConfigAPIPort is not available for Fabric.
 *
 * This keeps CoroUtil's config registry functional enough for runtime defaults and local edits,
 * but does not provide persisted TOML-backed config files on Fabric snapshots.
 */
public class ModConfigDataFabric extends ModConfigData {

    public ModConfigDataFabric(String savePath, String parStr, Class parClass, IConfigCategory parConfig) {
        super(savePath, parStr, parClass, parConfig);
    }

    @Override
    public String getConfigString(String fieldName) {
        return valsString.get(fieldName);
    }

    @Override
    public Integer getConfigInteger(String fieldName) {
        return valsInteger.get(fieldName);
    }

    @Override
    public Double getConfigDouble(String fieldName) {
        return valsDouble.get(fieldName);
    }

    @Override
    public Boolean getConfigBoolean(String fieldName) {
        return valsBoolean.get(fieldName);
    }

    @Override
    public <T> void setConfig(String fieldName, T obj) {
        // Keep the in-memory mirrors in sync. Snapshot fallback does not persist to disk on Fabric.
        if (obj instanceof String) {
            valsString.put(fieldName, (String) obj);
        } else if (obj instanceof Integer) {
            valsInteger.put(fieldName, (Integer) obj);
        } else if (obj instanceof Double) {
            valsDouble.put(fieldName, (Double) obj);
        } else if (obj instanceof Boolean) {
            valsBoolean.put(fieldName, (Boolean) obj);
        }
    }

    @Override
    public void writeConfigFile(boolean resetConfig) {
        // Snapshot fallback: no ForgeConfigAPIPort-backed TOML config on Fabric 26.1 yet.
        // We still re-apply current registry values into the runtime config object to preserve behavior.
        Field[] fields = configClass.getDeclaredFields();
        for (Field field : fields) {
            String name = field.getName();
            Object obj = CoroConfigRegistry.instance().getField(configID, name);
            if (obj != null) {
                setFieldBasedOnType(name, obj);
            }
        }

        CULog.dbg("writeConfigFile (snapshot Fabric fallback, no persistence) invoked for " + this.configID
                + ", resetConfig: " + resetConfig);
    }
}

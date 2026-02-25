package com.corosus.coroutil.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.lang.reflect.Method;

public class CoroUtilCompatibility {

    private static boolean sereneSeasonsInstalled = false;
    private static boolean checksereneSeasons = true;

    private static Class<?> sereneSeasonsSeasonHooksClass = null;
    private static Method sereneSeasonsGetBiomeTemperatureMethod = null;

    /**
     * Used to contain compat with other mods, still used incase i add that back in
     * @param ent
     * @param x
     * @param y
     * @param z
     * @param speed
     * @return
     */
    public static boolean tryPathToXYZModCompat(Mob ent, int x, int y, int z, double speed) {
        return tryPathToXYZVanilla(ent, x, y, z, speed);
    }

    public static boolean tryPathToXYZVanilla(Mob ent, int x, int y, int z, double speed) {
        return ent.getNavigation().moveTo(x, y, z, speed);
    }

    public static float getAdjustedTemperature(Level world, Biome biome, BlockPos pos) {
        if (isSereneSeasonsInstalled()) {
            try {
                if (sereneSeasonsGetBiomeTemperatureMethod == null) {
                    sereneSeasonsGetBiomeTemperatureMethod = sereneSeasonsSeasonHooksClass.getDeclaredMethod("getBiomeTemperature", Level.class, Holder.class, BlockPos.class);
                }
                Holder<Biome> biomeHolder = world.getBiome(pos);
                return (float) sereneSeasonsGetBiomeTemperatureMethod.invoke(null, world, biomeHolder, pos);
            } catch (Exception ex) {
                ex.printStackTrace();
                //prevent error spam
                sereneSeasonsInstalled = false;
                return getAdjustedTemperatureFallback(world, biome, pos);
            }
        } else {
            return getAdjustedTemperatureFallback(world, biome, pos);
        }
    }

    private static float getAdjustedTemperatureFallback(Level world, Biome biome, BlockPos pos) {
        // MC 26.1 snapshot no longer exposes Biome#getTemperature(BlockPos, int) publicly.
        // CoroUtil only uses this value for the rain/snow threshold check (0.15F), so a
        // threshold-equivalent fallback is sufficient here.
        return biome.warmEnoughToRain(pos, world.getSeaLevel()) ? 0.15F : 0.0F;
    }

    /**
     * Check if Serene Seasons is installed
     *
     * @return
     */
    public static boolean isSereneSeasonsInstalled() {
        if (checksereneSeasons) {
            try {
                checksereneSeasons = false;
                sereneSeasonsSeasonHooksClass = Class.forName("sereneseasons.season.SeasonHooks");
                if (sereneSeasonsSeasonHooksClass != null) {
                    sereneSeasonsInstalled = true;
                }
            } catch (Exception ex) {
                //not installed
                //ex.printStackTrace();
            }

            CULog.log("CoroUtil detected Serene Seasons " + (sereneSeasonsInstalled ? "Installed" : "Not Installed") + " for use");
        }

        return sereneSeasonsInstalled;
    }

    public static boolean coldEnoughToSnow(Biome biome, BlockPos pos, Level levelReader) {
        return !warmEnoughToRain(biome, pos, levelReader);
    }

    public static boolean warmEnoughToRain(Biome biome, BlockPos pos, Level levelReader) {
        return getAdjustedTemperature(levelReader, biome, pos) >= 0.15F;
    }

}

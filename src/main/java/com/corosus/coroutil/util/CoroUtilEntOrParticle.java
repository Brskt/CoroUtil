package com.corosus.coroutil.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.lang.reflect.Field;

public class CoroUtilEntOrParticle {

    private static final Field PARTICLE_X_FIELD = getParticleField("x");
    private static final Field PARTICLE_Y_FIELD = getParticleField("y");
    private static final Field PARTICLE_Z_FIELD = getParticleField("z");
    private static final Field PARTICLE_XD_FIELD = getParticleField("xd");
    private static final Field PARTICLE_YD_FIELD = getParticleField("yd");
    private static final Field PARTICLE_ZD_FIELD = getParticleField("zd");
	
	public static double getPosX(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).getX();
		} else {
			return getPosXParticle(obj);
		}
	}

	private static double getPosXParticle(Object obj) {
		return getParticleDouble(obj, PARTICLE_X_FIELD);
	}
	
	public static double getPosY(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).getY();
		} else {
			return getPosYParticle(obj);
		}
	}

	private static double getPosYParticle(Object obj) {
		return getParticleDouble(obj, PARTICLE_Y_FIELD);
	}
	
	public static double getPosZ(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).getZ();
		} else {
			return getPosZParticle(obj);
		}
	}

	private static double getPosZParticle(Object obj) {
		return getParticleDouble(obj, PARTICLE_Z_FIELD);
	}
	
	public static double getMotionX(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).getDeltaMovement().x;
		} else {
			return getMotionXParticle(obj);
		}
	}

	private static double getMotionXParticle(Object obj) {
		return getParticleDouble(obj, PARTICLE_XD_FIELD);
	}
	
	public static double getMotionY(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).getDeltaMovement().y;
		} else {
			return getMotionYParticle(obj);
		}
	}
	
	private static double getMotionYParticle(Object obj) {
		return getParticleDouble(obj, PARTICLE_YD_FIELD);
	}
	
	public static double getMotionZ(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).getDeltaMovement().z;
		} else {
			return getMotionZParticle(obj);
		}
	}

	private static double getMotionZParticle(Object obj) {
		return getParticleDouble(obj, PARTICLE_ZD_FIELD);
	}
	
	public static void setMotionX(Object obj, double val) {
		if (obj instanceof Entity) {
			((Entity)obj).setDeltaMovement(val, ((Entity)obj).getDeltaMovement().y, ((Entity)obj).getDeltaMovement().z);
		} else {
			setMotionXParticle(obj, val);
		}
	}

	private static void setMotionXParticle(Object obj, double val) {
		setParticleDouble(obj, PARTICLE_XD_FIELD, val);
	}
	
	public static void setMotionY(Object obj, double val) {
		if (obj instanceof Entity) {
			((Entity)obj).setDeltaMovement(((Entity)obj).getDeltaMovement().y, val, ((Entity)obj).getDeltaMovement().z);
		} else {
			setMotionYParticle(obj, val);
		}
	}

	private static void setMotionYParticle(Object obj, double val) {
		setParticleDouble(obj, PARTICLE_YD_FIELD, val);
	}
	
	public static void setMotionZ(Object obj, double val) {
		if (obj instanceof Entity) {
			((Entity)obj).setDeltaMovement(((Entity)obj).getDeltaMovement().y, ((Entity)obj).getDeltaMovement().y, val);
		} else {
			setMotionZParticle(obj, val);
		}
	}

	private static void setMotionZParticle(Object obj, double val) {
		setParticleDouble(obj, PARTICLE_ZD_FIELD, val);
	}

	public static double getDistance(Object obj, double x, double y, double z)
	{
		double d0 = getPosX(obj) - x;
		double d1 = getPosY(obj) - y;
		double d2 = getPosZ(obj) - z;
		return Mth.sqrt((float) (d0 * d0 + d1 * d1 + d2 * d2));
	}

	public static void setPosX(Object obj, double val) {
		if (obj instanceof Entity) {
			Entity e = (Entity) obj;
			e.setPos(val, e.getY(), e.getZ());
		} else {
			setPosXParticle(obj, val);
		}
	}

	private static void setPosXParticle(Object obj, double val) {
		setParticleDouble(obj, PARTICLE_X_FIELD, val);
	}

	public static void setPosY(Object obj, double val) {
		if (obj instanceof Entity) {
			Entity e = (Entity) obj;
			e.setPos(e.getX(), val, e.getZ());
		} else {
			setPosYParticle(obj, val);
		}
	}

	private static void setPosYParticle(Object obj, double val) {
		setParticleDouble(obj, PARTICLE_Y_FIELD, val);
	}

	public static void setPosZ(Object obj, double val) {
		if (obj instanceof Entity) {
			Entity e = (Entity) obj;
			e.setPos(e.getX(), e.getY(), val);
		} else {
			setPosZParticle(obj, val);
		}
	}

	private static void setPosZParticle(Object obj, double val) {
		setParticleDouble(obj, PARTICLE_Z_FIELD, val);
	}

	public static Level getWorld(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).level();
		} else {
			return CoroUtilParticle.getWorldParticle(obj);
		}
	}

	private static Field getParticleField(String name) {
		try {
			Field field = Particle.class.getDeclaredField(name);
			field.setAccessible(true);
			return field;
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Failed to access Particle field: " + name, e);
		}
	}

	private static double getParticleDouble(Object obj, Field field) {
		try {
			return field.getDouble(obj);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Failed to read Particle field: " + field.getName(), e);
		}
	}

	private static void setParticleDouble(Object obj, Field field, double value) {
		try {
			field.setDouble(obj, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Failed to write Particle field: " + field.getName(), e);
		}
	}
	
}

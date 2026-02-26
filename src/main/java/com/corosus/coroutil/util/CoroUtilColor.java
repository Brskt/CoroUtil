package com.corosus.coroutil.util;

import com.corosus.coroutil.repack.de.androidpit.colorthief.ColorThief;
import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.block.state.BlockState;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

public class CoroUtilColor {
    private static final Field SPRITE_CONTENTS_ORIGINAL_IMAGE_FIELD = findOriginalImageField();
    private static final ConcurrentHashMap<String, Method> NO_ARG_METHOD_CACHE = new ConcurrentHashMap<>();
    private static final Set<String> NO_ARG_METHOD_MISS_CACHE = ConcurrentHashMap.newKeySet();

    public static int[] getColors(BlockState state) {
        TextureAtlasSprite sprite = getParticleSprite(state);
        if (sprite != null && !sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation())) {
            return getColors(sprite);
        }
        return IntArrays.EMPTY_ARRAY;
    }

    public static int getPixelRGBA(TextureAtlasSprite textureAtlasSprite, int frameIndex, int x, int y) {
        SpriteContents contents = textureAtlasSprite.contents();
        NativeImage originalImage = getOriginalImage(contents);

        if (contents.isAnimated()) {
            int frameRowSize = Math.max(1, originalImage.getWidth() / contents.width());
            x += (frameIndex % frameRowSize) * contents.width();
            y += (frameIndex / frameRowSize) * contents.height();
        }

        // NativeImage#getPixel returns ARGB in 26.1 snapshots.
        return originalImage.getPixel(x, y);
    }

    public static int[] getColors(TextureAtlasSprite sprite) {
        int width = sprite.contents().width();
        int height = sprite.contents().height();
        int[] frameIndices = getFrameIndices(sprite.contents());
        int frames = frameIndices.length;

        BufferedImage img = new BufferedImage(width, height * frames, BufferedImage.TYPE_4BYTE_ABGR);
        for (int i = 0; i < frames; i++) {
            int frameIndex = frameIndices[i];
        	for (int x = 0; x < width; x++) {
        		for (int y = 0; y < height; y++) {
                    int argb = getPixelRGBA(sprite, frameIndex, x, y);
                    int alpha = (argb >>> 24) & 0xFF;
                    int red = (argb >>> 16) & 0xFF;
                    int green = (argb >>> 8) & 0xFF;
                    int blue = argb & 0xFF;
                    img.setRGB(x, y + (i * height), (alpha << 24) | (red << 16) | (green << 8) | blue);
        		}
        	}
        }
        
        int[][] colorData = ColorThief.getPalette(img, 6, 5, true);
        if (colorData != null) {
            int[] ret = new int[colorData.length];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = getColor(colorData[i]);
            }
            return ret;
        }
        return IntArrays.EMPTY_ARRAY;
    }
    
    private static int getColor(int[] colorData) {
        float mr = 1F;//((multiplier >>> 16) & 0xFF) / 255f;
        float mg = 1F;//((multiplier >>> 8) & 0xFF) / 255f;
        float mb = 1F;//(multiplier & 0xFF) / 255f;

        return 0xFF000000 | (((int) (colorData[0] * mr)) << 16) | (((int) (colorData[1] * mg)) << 8) | (int) (colorData[2] * mb);
    }

    private static Field findOriginalImageField() {
        try {
            Field field = SpriteContents.class.getDeclaredField("originalImage");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to access SpriteContents.originalImage", e);
        }
    }

    private static NativeImage getOriginalImage(SpriteContents contents) {
        try {
            return (NativeImage) SPRITE_CONTENTS_ORIGINAL_IMAGE_FIELD.get(contents);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to read SpriteContents.originalImage", e);
        }
    }

    private static TextureAtlasSprite getParticleSprite(BlockState state) {
        Object blockModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        if (blockModel == null) {
            return null;
        }

        // 26.x snapshots diverge between loader toolchains here; try the known shapes.
        Object material = invokeNoArg(blockModel, "particleMaterial");
        if (material != null) {
            Object sprite = invokeNoArg(material, "sprite");
            if (sprite instanceof TextureAtlasSprite textureAtlasSprite) {
                return textureAtlasSprite;
            }
        }

        Object sprite = invokeNoArg(blockModel, "particleSprite");
        if (sprite instanceof TextureAtlasSprite textureAtlasSprite) {
            return textureAtlasSprite;
        }

        sprite = invokeNoArg(blockModel, "particleIcon");
        if (sprite instanceof TextureAtlasSprite textureAtlasSprite) {
            return textureAtlasSprite;
        }

        return null;
    }

    private static int[] getFrameIndices(SpriteContents contents) {
        if (!contents.isAnimated()) {
            return new int[]{0};
        }

        Object framesObj = invokeNoArg(contents, "getUniqueFrames");
        if (framesObj instanceof IntList intList) {
            return intList.toIntArray();
        }
        if (framesObj instanceof IntStream intStream) {
            return intStream.toArray();
        }
        if (framesObj instanceof int[] ints) {
            return ints;
        }

        return new int[]{0};
    }

    private static Object invokeNoArg(Object target, String name) {
        String cacheKey = target.getClass().getName() + "#" + name;
        if (NO_ARG_METHOD_MISS_CACHE.contains(cacheKey)) {
            return null;
        }

        try {
            Method method = NO_ARG_METHOD_CACHE.get(cacheKey);
            if (method == null) {
                method = findNoArgMethod(target.getClass(), name);
                if (method == null) {
                    NO_ARG_METHOD_MISS_CACHE.add(cacheKey);
                    return null;
                }
                NO_ARG_METHOD_CACHE.put(cacheKey, method);
            }
            return method.invoke(target);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static Method findNoArgMethod(Class<?> targetClass, String name) {
        try {
            return targetClass.getMethod(name);
        } catch (NoSuchMethodException e) {
            try {
                Method method = targetClass.getDeclaredMethod(name);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
                return null;
            }
        }
    }

}

package com.slprime.chromatictooltips.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

import com.mojang.authlib.GameProfile;

public final class FoodEffectHelper {

    private static Method ON_FOOD_EATEN = null;
    private static final Map<Item, List<PotionEffect>> CACHE = new HashMap<>();
    private static boolean methodInitialized = false;

    private static TooltipFakePlayer fakePlayer;

    private FoodEffectHelper() {}

    public static List<PotionEffect> getFoodEffects(ItemStack stack) {

        if (stack != null && stack.getItem() instanceof ItemFood food) {

            if (stack.getItem() instanceof ItemAppleGold) {
                System.out.println("FOOD: " + stack);
                return computeEffects(stack);
            }

            return CACHE.computeIfAbsent(food, i -> computeEffects(stack));
        } else {
            return Collections.emptyList();
        }
    }

    private static List<PotionEffect> computeEffects(ItemStack stack) {

        if (!FoodEffectHelper.methodInitialized) {
            FoodEffectHelper.methodInitialized = true;

            try {
                ON_FOOD_EATEN = ItemFood.class
                    .getDeclaredMethod("onFoodEaten", ItemStack.class, World.class, EntityPlayer.class);
                ON_FOOD_EATEN.setAccessible(true);
            } catch (Throwable e) {}

            if (ON_FOOD_EATEN == null) {
                try {
                    ON_FOOD_EATEN = ItemFood.class
                        .getDeclaredMethod("func_77849_c", ItemStack.class, World.class, EntityPlayer.class);
                    ON_FOOD_EATEN.setAccessible(true);
                } catch (Throwable e) {}
            }
        }

        if (ON_FOOD_EATEN != null) {
            final World world = TooltipUtils.mc().theWorld;

            if (fakePlayer == null || fakePlayer.worldObj != world) {
                fakePlayer = new TooltipFakePlayer(world);
            }

            fakePlayer.effects.clear();
            final boolean isRemote = fakePlayer.worldObj.isRemote;

            try {
                fakePlayer.worldObj.isRemote = false;
                ON_FOOD_EATEN.invoke(stack.getItem(), stack.copy(), world, fakePlayer);
            } catch (Throwable ignored) {

                if (stack.getItem() instanceof ItemAppleGold) {
                    ignored.printStackTrace();
                }

            } finally {
                fakePlayer.worldObj.isRemote = isRemote;
            }

            if (stack.getItem() instanceof ItemAppleGold) {
                System.out.println("APPLE: " + fakePlayer.effects.size());
            }

            if (!fakePlayer.effects.isEmpty()) {
                return Collections.unmodifiableList(new ArrayList<>(fakePlayer.effects));
            }
        }

        if (stack.getItem() instanceof ItemFood food && food.potionId > 0 && food.potionEffectProbability > 0.0F) {
            return Collections
                .singletonList(new PotionEffect(food.potionId, food.potionDuration * 20, food.potionAmplifier));
        }

        return Collections.emptyList();
    }

    private static class TooltipFakePlayer extends EntityPlayer {

        final List<PotionEffect> effects = new ArrayList<>();

        public TooltipFakePlayer(World world) {
            super(world, new GameProfile(UUID.nameUUIDFromBytes("food-tooltip".getBytes()), "food-tooltip"));
        }

        @Override
        public void addPotionEffect(PotionEffect effect) {
            effects.add(effect);
        }

        // --- required abstract methods ---

        @Override
        public void addChatMessage(IChatComponent component) {}

        @Override
        public boolean canCommandSenderUseCommand(int permLevel, String commandName) {
            return false;
        }

        @Override
        public ChunkCoordinates getPlayerCoordinates() {
            return new ChunkCoordinates(0, 0, 0);
        }

        @Override
        public void sendPlayerAbilities() {}

        @Override
        public void displayGUIBook(ItemStack bookStack) {}
    }
}

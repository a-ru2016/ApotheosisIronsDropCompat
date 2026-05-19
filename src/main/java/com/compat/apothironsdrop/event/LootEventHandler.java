package com.compat.apothironsdrop.event;

import com.compat.apothironsdrop.config.ModConfig;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootController;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@EventBusSubscriber(modid = "apoth_irons_drop")
public class LootEventHandler {
    private static List<Item> affixableItems = null;
    private static final Random random = new Random();

    private static synchronized List<Item> getAffixableItems() {
        if (affixableItems == null) {
            affixableItems = new ArrayList<>();
            for (Item item : BuiltInRegistries.ITEM) {
                ItemStack stack = new ItemStack(item);
                LootCategory cat = LootCategory.forItem(stack);
                if (cat != null && !cat.isNone()) {
                    affixableItems.add(item);
                }
            }
        }
        return affixableItems;
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || !(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        ModConfig config = ModConfig.load();
        DamageSource source = event.getSource();
        Player player = null;
        if (source != null && source.getEntity() instanceof Player p) {
            player = p;
        }

        // Check if player kill is required
        if (config.playerKillOnly && player == null) {
            return;
        }

        RandomSource randSource = serverLevel.getRandom();

        // 1. Apotheosis Affix Equipment Drop Logic
        if (random.nextDouble() < config.affixDropChance) {
            List<Item> pool = getAffixableItems();
            if (!pool.isEmpty()) {
                Item randomItem = pool.get(random.nextInt(pool.size()));
                ItemStack itemStack = new ItemStack(randomItem);

                // Create GenContext
                GenContext ctx;
                if (player != null) {
                    ctx = GenContext.forPlayer(player);
                } else {
                    ctx = GenContext.standalone(randSource, WorldTier.HAVEN, 0.0f, serverLevel, entity.blockPosition());
                }

                // Get a random rarity
                LootRarity rarity = LootRarity.random(ctx);
                if (rarity != null) {
                    ItemStack affixStack = LootController.createLootItem(itemStack, rarity, ctx);
                    if (!affixStack.isEmpty()) {
                        ItemEntity itemEntity = new ItemEntity(serverLevel, entity.getX(), entity.getY(), entity.getZ(), affixStack);
                        itemEntity.setDefaultPickUpDelay();
                        event.getDrops().add(itemEntity);
                    }
                }
            }
        }

        // 2. Iron's Spells 'n Spellbooks Scroll Drop Logic
        if (random.nextDouble() < config.scrollDropChance) {
            List<AbstractSpell> enabledSpells = SpellRegistry.getEnabledSpells();
            if (enabledSpells != null && !enabledSpells.isEmpty()) {
                AbstractSpell spell = enabledSpells.get(random.nextInt(enabledSpells.size()));
                int minLvl = spell.getMinLevel();
                int maxLvl = spell.getMaxLevel();
                int lvl = minLvl;
                if (maxLvl > minLvl) {
                    lvl = minLvl + random.nextInt(maxLvl - minLvl + 1);
                }

                ItemStack scrollStack = new ItemStack(ItemRegistry.SCROLL.get());
                ISpellContainer.createScrollContainer(spell, lvl, scrollStack);
                if (!scrollStack.isEmpty()) {
                    ItemEntity itemEntity = new ItemEntity(serverLevel, entity.getX(), entity.getY(), entity.getZ(), scrollStack);
                    itemEntity.setDefaultPickUpDelay();
                    event.getDrops().add(itemEntity);
                }
            }
        }
    }
}

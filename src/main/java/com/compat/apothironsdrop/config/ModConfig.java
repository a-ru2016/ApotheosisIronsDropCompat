package com.compat.apothironsdrop.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_FILE = FMLPaths.CONFIGDIR.get().resolve("apoth_irons_drop").resolve("config.json");

    // Config fields with defaults
    public double affixDropChance = 0.05; // 5% chance
    public double scrollDropChance = 0.08; // 8% chance
    public boolean playerKillOnly = true;
    /**
     * ドロップするアフィックス装備のアイテムタグ一覧。
     * 空の場合はすべてのアフィックス付与可能アイテムが対象になります。
     * 例: ["minecraft:swords", "minecraft:axes"]
     */
    public List<String> affixItemTags = new ArrayList<>();
    /**
     * ドロップするアフィックス装備のMod ID一覧。
     * タグ指定と合わせてOR条件で動作します（どちらかに一致すればドロップ候補になります）。
     * 空の場合はMod IDによる絞り込みは行いません。
     * 例: ["minecraft", "simplyswords"]
     */
    public List<String> affixItemMods = new ArrayList<>();


    public ModConfig() {
    }

    public static ModConfig load() {
        if (Files.exists(CONFIG_FILE)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
                ModConfig config = GSON.fromJson(reader, ModConfig.class);
                if (config != null) {
                    return config;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ModConfig config = new ModConfig();
        save(config);
        return config;
    }

    public static void save(ModConfig config) {
        try {
            if (!Files.exists(CONFIG_FILE.getParent())) {
                Files.createDirectories(CONFIG_FILE.getParent());
            }
            try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

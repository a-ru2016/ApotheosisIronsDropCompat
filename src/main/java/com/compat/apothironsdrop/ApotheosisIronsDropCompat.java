package com.compat.apothironsdrop;

import com.compat.apothironsdrop.config.ModConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(ApotheosisIronsDropCompat.MOD_ID)
public class ApotheosisIronsDropCompat {
    public static final String MOD_ID = "apoth_irons_drop";

    public ApotheosisIronsDropCompat(IEventBus modEventBus) {
        // Load config
        ModConfig.load();
    }
}

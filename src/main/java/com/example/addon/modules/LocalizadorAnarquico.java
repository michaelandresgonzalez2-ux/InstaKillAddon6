package com.example.addon.modules;

import meteordevelopment.meteorClient.systems.modules.Module;
import meteordevelopment.meteorClient.events.world.TickEvent;
import meteordevelopment.meteorClient.settings.Setting;
import meteordevelopment.meteorClient.settings.SettingGroup;
import meteordevelopment.meteorClient.settings.IntSetting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import com.example.addon.InstaKillAddon;

public class LocalizadorAnarquico extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> distancia = sgGeneral.add(new IntSetting.Builder()
        .name("distancia-maxima")
        .description("Distancia máxima para escanear jugadores.")
        .defaultValue(128)
        .range(16, 256)
        .build()
    );

    public LocalizadorAnarquico() {
        super(InstaKillAddon.CATEGORY, "localizador-anarquico", "Localiza jugadores cercanos en servidores anárquicos.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.world == null || mc.player == null) return;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            
            double d = mc.player.distanceTo(player);
            if (d <= distancia.get()) {
                // Jugador detectado en rango
            }
        }
    }
}

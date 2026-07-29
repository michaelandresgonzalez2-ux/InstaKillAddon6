package com.example.addon.modules;

import meteordevelopment.meteorClient.events.world.TickEvent;
import meteordevelopment.meteorClient.settings.DoubleSetting;
import meteordevelopment.meteorClient.settings.IntSetting;
import meteordevelopment.meteorClient.settings.Setting;
import meteordevelopment.meteorClient.settings.SettingGroup;
import meteordevelopment.meteorClient.systems.modules.Category;
import meteordevelopment.meteorClient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

public class InstaKillModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // Ajustes para el Instakill / Aura automática
    private final Setting<Double> rangoAura = sgGeneral.add(new DoubleSetting.Builder()
        .name("rango-aura")
        .description("Rango para atacar automáticamente.")
        .defaultValue(4.5)
        .min(1.0)
        .sliderMax(6.0)
        .build()
    );

    // Ajustes para el Localizador de jugadores lejanos (en bloques)
    private final Setting<Integer> alertaDistancia = sgGeneral.add(new IntSetting.Builder()
        .name("alerta-distancia-bloques")
        .description("Distancia en bloques para avisarte que hay jugadores lejos.")
        .defaultValue(120)
        .min(30)
        .sliderMax(256)
        .build()
    );

    private int tickCounter = 0;

    public InstaKillModule(Category category) {
        super(category, "instakill-y-radar", "InstaKill automático y radar de distancia para anárquico.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        // 1. SISTEMA DE RADAR / ALERTA DE JUGADORES A LO LEJOS
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player || player.isCreative() || player.isSpectator()) continue;

            double distancia = mc.player.distanceTo(player);
            
            // Si el jugador entra en el radio largo que configuraste
            if (distancia <= alertaDistancia.get()) {
                // Limitamos el spam en el chat para que avise cada ciertos ticks
                if (tickCounter >= 40) {
                    info("¡Alerta! Jugador detectado: " + player.getName().getString() + " a " + Math.round(distancia) + " bloques.");
                }
            }
        }

        tickCounter++;
        if (tickCounter > 40) tickCounter = 0;

        // 2. SISTEMA DE ATAQUE AUTOMÁTICO (ULTRA AURA / INSTAKILL)
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player || player.isDead() || player.isCreative() || player.isSpectator()) continue;

            // Si está dentro del rango de golpe
            if (mc.player.distanceTo(player) <= rangoAura.get()) {
                // Ataca automáticamente sin que toques la pantalla
                mc.interactionManager.attackEntity(mc.player, player);
                mc.player.swingHand(Hand.MAIN_HAND);
                break; // Ataca al primero en rango y se espera al siguiente tick
            }
        }
    }
}

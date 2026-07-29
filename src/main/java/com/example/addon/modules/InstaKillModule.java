package com.example.addon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class InstaKillModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> packets = sgGeneral.add(new IntSetting.Builder()
            .name("packets")
            .description("Cantidad de paquetes a enviar para forzar el daño instakill.")
            .defaultValue(50)
            .min(1)
            .sliderMax(200)
            .build()
    );

    private final Setting<Double> delay = sgGeneral.add(new DoubleSetting.Builder()
            .name("delay")
            .description("Retraso en ticks entre ataques.")
            .defaultValue(0.0)
            .min(0.0)
            .sliderMax(10.0)
            .build()
    );

    private final Setting<Boolean> autoDisable = sgGeneral.add(new BoolSetting.Builder()
            .name("auto-disable")
            .description("Desactiva el módulo automáticamente tras acertar un golpe.")
            .defaultValue(true)
            .build()
    );

    public InstaKillModule(Category category) {
        super(category, "insta-kill", "Módulo personalizado para maximizar el daño de impacto instantáneo.");
    }

    @Override
    public void onActivate() {
    }

    @Override
    public void onDeactivate() {
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;
    }
}


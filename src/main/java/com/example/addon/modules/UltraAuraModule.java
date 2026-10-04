package com.example.addon.modules;

import com.example.addon.InstaKillAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class UltraAuraModule extends Module {
    public enum Objetivo {
        MAS_CERCANO,
        VIDA_MAS_BAJA
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> rango = sgGeneral.add(new DoubleSetting.Builder()
        .name("rango")
        .description("Alcance máximo del aura en bloques.")
        .defaultValue(6)
        .min(1)
        .max(12)
        .sliderMax(12)
        .build()
    );

    private final Setting<Integer> ticksPorGolpe = sgGeneral.add(new IntSetting.Builder()
        .name("ticks-por-golpe")
        .description("Ticks entre golpe y golpe. En 1 pega en cada tick, sin espera.")
        .defaultValue(1)
        .range(1, 20)
        .build()
    );

    private final Setting<Objetivo> criterio = sgGeneral.add(new EnumSetting.Builder<Objetivo>()
        .name("criterio")
        .description("A quién ataca la aura.")
        .defaultValue(Objetivo.MAS_CERCANO)
        .build()
    );

    private final Setting<Boolean> soloMazo = sgGeneral.add(new BoolSetting.Builder()
        .name("solo-mazo")
        .description("Solo golpea si tenés mazo en la mano.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> golpeDeMazo = sgGeneral.add(new BoolSetting.Builder()
        .name("golpe-de-mazo")
        .description("Además de atacar, usa el mazo sobre la entidad para el daño y empuje extra.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> rotacionAutomatica = sgGeneral.add(new BoolSetting.Builder()
        .name("rotacion-automatica")
        .description("Mira al objetivo sin que tengas que apuntar a mano.")
        .defaultValue(true)
        .build()
    );


    private int contador = 0;

    public UltraAuraModule() {
        super(InstaKillAddon.CATEGORY, "ultra-aura", "Aura que golpea sola al objetivo, sin espera entre golpes.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        PlayerEntity objetivo = objetivo();
        if (objetivo == null) return;

        Hand mazo = manoConMazo();
        if (mazo == null && soloMazo.get()) return;

        Hand mano = mazo != null ? mazo : Hand.MAIN_HAND;

        if (rotacionAutomatica.get()) mirarA(objetivo);

        if (contador++ % ticksPorGolpe.get() != 0) return;

        mc.interactionManager.attackEntity(mc.player, objetivo);
        if (golpeDeMazo.get() && mazo != null) mc.interactionManager.interactEntity(mc.player, objetivo, mano);
        mc.player.swingHand(mano);
    }

    private PlayerEntity objetivo() {
        PlayerEntity elegido = null;
        double mejorValor = Double.MAX_VALUE;

        for (PlayerEntity jugador : mc.world.getPlayers()) {
            if (jugador == mc.player || jugador.isDead() || jugador.isCreative() || jugador.isSpectator()) continue;
            if (mc.player.distanceTo(jugador) > rango.get()) continue;

            double valor = criterio.get() == Objetivo.VIDA_MAS_BAJA
                ? jugador.getHealth() + mc.player.distanceTo(jugador) / 100
                : mc.player.distanceTo(jugador);

            if (valor < mejorValor) {
                elegido = jugador;
                mejorValor = valor;
            }
        }

        return elegido;
    }

    private Hand manoConMazo() {
        if (mc.player.getMainHandStack().isOf(Items.MACE)) return Hand.MAIN_HAND;
        if (mc.player.getOffHandStack().isOf(Items.MACE)) return Hand.OFF_HAND;
        return null;
    }

    private void mirarA(PlayerEntity objetivo) {
        Vec3d ojos = mc.player.getEyePos();
        Vec3d destino = objetivo.getEyePos();
        Vec3d diferencia = destino.subtract(ojos);

        double yaw = Math.toDegrees(Math.atan2(diferencia.z, diferencia.x)) - 90;
        double pitch = -Math.toDegrees(Math.atan2(diferencia.y, Math.hypot(diferencia.x, diferencia.z)));

        mc.player.setYaw((float) yaw);
        mc.player.setPitch((float) pitch);
    }
}
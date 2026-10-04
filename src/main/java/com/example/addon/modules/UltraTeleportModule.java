package com.example.addon.modules;

import com.example.addon.InstaKillAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

public class UltraTeleportModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> distancia = sgGeneral.add(new DoubleSetting.Builder()
        .name("distancia-maxima")
        .description("Distancia máxima del teleport en bloques.")
        .defaultValue(100)
        .min(1)
        .max(10000)
        .sliderMax(500)
        .build()
    );

    private final Setting<Boolean> teleportAutomatico = sgGeneral.add(new BoolSetting.Builder()
        .name("teleport-automatico")
        .description("Se teletransporta solo, al jugador más cercano, sin que toques nada.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> soloAlPulsar = sgGeneral.add(new BoolSetting.Builder()
        .name("solo-al-pulsar-tecla")
        .description("Con el módulo activo, solo teletransporta el tick en que pulsás la tecla del módulo.")
        .defaultValue(false)
        .build()
    );

    public UltraTeleportModule() {
        super(InstaKillAddon.CATEGORY, "ultra-teleport", "Teletransporte automático al objetivo, sin límite de distancia.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;
        if (soloAlPulsar.get() && !keybind.isPressed()) return;

        Vec3d destino = teleportAutomatico.get() ? posicionJugadorCercano() : posicionPuntoMirado();
        if (destino == null) return;

        mc.player.setPosition(destino);
        mc.player.setVelocity(Vec3d.ZERO);
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(destino.x, destino.y, destino.z, mc.player.horizontalCollision, mc.player.isOnGround()));
    }

    private Vec3d posicionPuntoMirado() {
        HitResult hit = mc.player.raycast(distancia.get(), 1f, false);
        return hit.getPos();
    }

    private Vec3d posicionJugadorCercano() {
        PlayerEntity masCercano = null;
        double mejorDistancia = distancia.get();

        for (PlayerEntity jugador : mc.world.getPlayers()) {
            if (jugador == mc.player || jugador.isDead() || jugador.isCreative() || jugador.isSpectator()) continue;

            double distanciaActual = mc.player.distanceTo(jugador);
            if (distanciaActual <= mejorDistancia) {
                masCercano = jugador;
                mejorDistancia = distanciaActual;
            }
        }

        return masCercano != null ? masCercano.getPos() : null;
    }
}
package com.example.addon.modules;

import com.example.addon.InstaKillAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class AutoBuscadorModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> distanciaBusqueda = sgGeneral.add(new DoubleSetting.Builder()
        .name("distancia-de-busqueda")
        .description("Radio en el que busca jugadores.")
        .defaultValue(128)
        .min(8)
        .max(256)
        .sliderMax(256)
        .build()
    );

    private final Setting<Double> distanciaAtaque = sgGeneral.add(new DoubleSetting.Builder()
        .name("distancia-de-ataque")
        .description("Se detiene a esta distancia del objetivo; más cerca sigue avanzando.")
        .defaultValue(3.5)
        .min(1)
        .max(12)
        .sliderMax(12)
        .build()
    );

    private final Setting<Boolean> correr = sgGeneral.add(new BoolSetting.Builder()
        .name("correr")
        .description("Corre mientras avanza hacia el objetivo.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> saltarObstaculos = sgGeneral.add(new BoolSetting.Builder()
        .name("saltar-obstaculos")
        .description("Saltea cuando choca con algo al avanzar.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> mirarAlObjetivo = sgGeneral.add(new BoolSetting.Builder()
        .name("mirar-al-objetivo")
        .description("Mantiene la cámara apuntando al objetivo.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> atacar = sgGeneral.add(new BoolSetting.Builder()
        .name("atacar")
        .description("Ataca al objetivo cuando está a tiro.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> ticksPorGolpe = sgGeneral.add(new IntSetting.Builder()
        .name("ticks-por-golpe")
        .description("Ticks entre golpe y golpe. En 1 pega en cada tick, sin espera.")
        .defaultValue(1)
        .range(1, 20)
        .build()
    );

    private int contador = 0;

    public AutoBuscadorModule() {
        super(InstaKillAddon.CATEGORY, "auto-buscador-bot", "Busca el jugador más cercano, avanza y golpea solo.");
    }

    @Override
    public void onDeactivate() {
        if (mc.player == null) return;
        soltarTeclas();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;

        PlayerEntity objetivo = objetivo();
        if (objetivo == null) {
            soltarTeclas();
            return;
        }

        double distancia = mc.player.distanceTo(objetivo);
        boolean avanzar = distancia > distanciaAtaque.get();

        if (mirarAlObjetivo.get()) mirarA(objetivo);

        mc.options.forwardKey.setPressed(avanzar);
        mc.options.backKey.setPressed(false);
        mc.options.jumpKey.setPressed(avanzar && saltarObstaculos.get() && mc.player.horizontalCollision);

        if (correr.get() && avanzar) mc.player.setSprinting(true);

        if (atacar.get() && distancia <= distanciaAtaque.get() && contador++ % ticksPorGolpe.get() == 0) {
            mc.interactionManager.attackEntity(mc.player, objetivo);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    private PlayerEntity objetivo() {
        PlayerEntity masCercano = null;
        double mejorDistancia = distanciaBusqueda.get();

        for (PlayerEntity jugador : mc.world.getPlayers()) {
            if (jugador == mc.player || jugador.isDead() || jugador.isCreative() || jugador.isSpectator()) continue;

            double distancia = mc.player.distanceTo(jugador);
            if (distancia <= mejorDistancia) {
                masCercano = jugador;
                mejorDistancia = distancia;
            }
        }

        return masCercano;
    }

    private void soltarTeclas() {
        mc.options.forwardKey.setPressed(false);
        mc.options.backKey.setPressed(false);
        mc.options.jumpKey.setPressed(false);
        mc.player.setSprinting(false);
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
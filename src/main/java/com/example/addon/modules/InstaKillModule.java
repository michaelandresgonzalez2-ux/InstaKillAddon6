package com.example.addon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

public class InstaKillModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> rangoAura = sgGeneral.add(new DoubleSetting.Builder()
        .name("rango-aura")
        .description("Rango en bloques para el golpe de mazo automático.")
        .defaultValue(4.5)
        .min(1.0)
        .sliderMax(6.0)
        .build()
    );

    private final Setting<Boolean> requiereMazo = sgGeneral.add(new BoolSetting.Builder()
        .name("requiere-mazo")
        .description("Solo golpea con mazo. Si lo tenés en el inventario, lo lleva a la mano principal.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> golpeDeMazo = sgGeneral.add(new BoolSetting.Builder()
        .name("golpe-de-mazo")
        .description("Además de atacar, usa el mazo sobre la entidad (golpe de mazo con daño y empuje extra).")
        .defaultValue(true)
        .build()
    );

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
        super(category, "instakill-y-radar", "InstaKill con mazo y radar de distancia para anárquico.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        detectarJugadoresLejanos();

        PlayerEntity objetivo = jugadorMasCercano();
        if (objetivo == null) return;

        Hand mano = manoConMazo();
        if (mano == null) {
            if (!requiereMazo.get()) {
                mano = Hand.MAIN_HAND;
            } else {
                llevarMazoAManoPrincipal();
                return;
            }
        }

        mc.interactionManager.attackEntity(mc.player, objetivo);
        if (golpeDeMazo.get()) mc.interactionManager.interactEntity(mc.player, objetivo, mano);
        mc.player.swingHand(mano);
    }

    private PlayerEntity jugadorMasCercano() {
        PlayerEntity masCercano = null;
        double mejorDistancia = rangoAura.get();

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

    private Hand manoConMazo() {
        if (mc.player.getMainHandStack().isOf(Items.MACE)) return Hand.MAIN_HAND;
        if (mc.player.getOffHandStack().isOf(Items.MACE)) return Hand.OFF_HAND;
        return null;
    }

    /** Pasa el mazo del inventario al slot de la mano principal. El mazo se usa recien al proximo tick. */
    private boolean llevarMazoAManoPrincipal() {
        FindItemResult mazo = InvUtils.find(Items.MACE);
        if (!mazo.found() || mazo.slot() == PlayerInventory.OFF_HAND_SLOT) return false;

        int slotManoPrincipal = mc.player.getInventory().selectedSlot;
        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, mazo.slot(), slotManoPrincipal, SlotActionType.SWAP, mc.player);
        return true;
    }

    private void detectarJugadoresLejanos() {
        for (PlayerEntity jugador : mc.world.getPlayers()) {
            if (jugador == mc.player || jugador.isCreative() || jugador.isSpectator()) continue;
            if (mc.player.distanceTo(jugador) > alertaDistancia.get()) continue;

            if (tickCounter >= 40) {
                info("!Alerta! Jugador detectado: " + jugador.getName().getString() + " a " + Math.round(mc.player.distanceTo(jugador)) + " bloques.");
            }
        }

        tickCounter++;
        if (tickCounter > 40) tickCounter = 0;
    }
}
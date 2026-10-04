package com.example.addon.modules;

import com.example.addon.InstaKillAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

public class UltraTotemModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> usarSiempre = sgGeneral.add(new BoolSetting.Builder()
        .name("usar-siempre")
        .description("Usa el totem apenas esté en la mano, sin esperar a tener poca vida.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> umbralVida = sgGeneral.add(new IntSetting.Builder()
        .name("umbral-vida")
        .description("Vida o menos para usar el totem. No aplica si usar-siempre está activo.")
        .defaultValue(8)
        .range(1, 20)
        .build()
    );

    private final Setting<Boolean> intercambiarManoSecundaria = sgGeneral.add(new BoolSetting.Builder()
        .name("totem-en-mano-secundaria")
        .description("Pasa el totem a la mano secundaria automáticamente cuando no lo tenés en ninguna mano.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> ticksEntreIntentos = sgGeneral.add(new IntSetting.Builder()
        .name("ticks-entre-intentos")
        .description("Ticks entre intento e intento. En 1 no hay espera entre intentos.")
        .defaultValue(1)
        .range(1, 20)
        .build()
    );

    private int contador = 0;

    public UltraTotemModule() {
        super(InstaKillAddon.CATEGORY, "ultra-totem", "Totem automático sin espera entre intentos.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null || mc.player.isDead()) return;
        if (mc.player.isUsingItem()) return;
        if (contador++ % ticksEntreIntentos.get() != 0) return;

        Hand mano = manoConTotem();
        if (mano == null) {
            if (intercambiarManoSecundaria.get()) pasarTotemAManoSecundaria();
            return;
        }

        if (!usarSiempre.get() && mc.player.getHealth() > umbralVida.get()) return;

        mc.interactionManager.interactItem(mc.player, mano);
    }

    private Hand manoConTotem() {
        if (mc.player.getMainHandStack().isOf(Items.TOTEM_OF_UNDYING)) return Hand.MAIN_HAND;
        if (mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) return Hand.OFF_HAND;
        return null;
    }

    private void pasarTotemAManoSecundaria() {
        FindItemResult totem = InvUtils.find(Items.TOTEM_OF_UNDYING);
        if (!totem.found() || totem.slot() == PlayerInventory.OFF_HAND_SLOT) return;

        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, totem.slot(), SlotUtils.OFFHAND, SlotActionType.SWAP, mc.player);
    }
}
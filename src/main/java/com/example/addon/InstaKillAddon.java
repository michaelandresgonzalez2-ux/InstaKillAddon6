package com.example.addon;

import com.example.addon.modules.AutoBuscadorModule;
import com.example.addon.modules.InstaKillModule;
import com.example.addon.modules.UltraAuraModule;
import com.example.addon.modules.UltraTeleportModule;
import com.example.addon.modules.UltraTotemModule;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstaKillAddon extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger("InstaKillAddon");
    public static final Category CATEGORY = new Category("InstaKill");

    @Override
    public void onInitialize() {
        LOG.info("Inicializando Addon InstaKill para Meteor Client...");
        Modules.get().add(new InstaKillModule(CATEGORY));
        Modules.get().add(new UltraAuraModule());
        Modules.get().add(new AutoBuscadorModule());
        Modules.get().add(new UltraTotemModule());
        Modules.get().add(new UltraTeleportModule());
    }

    @Override
    public String getPackage() {
        return "com.example.addon";
    }
}


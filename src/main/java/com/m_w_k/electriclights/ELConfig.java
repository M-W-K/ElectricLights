package com.m_w_k.electriclights;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ElectricLightsMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)


public class ELConfig {
    public static final ForgeConfigSpec SERVER_SPEC;
    public static final ServerConfig SERVER;

    static {
        {
            final Pair<ServerConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
            SERVER = pair.getLeft();
            SERVER_SPEC = pair.getRight();
        }
    }

    public static class ServerConfig
    {
        private final ForgeConfigSpec.ConfigValue<Integer> nodeConnectDist;
        private final ForgeConfigSpec.ConfigValue<Integer> minimumSwitchboardUpdateInterval;
        private final ForgeConfigSpec.ConfigValue<Integer> alternatorEnergyFactor;
        private final ForgeConfigSpec.ConfigValue<Integer> solarEnergyFactor;
        private final ForgeConfigSpec.ConfigValue<Integer> geothermalEnergyFactor;

        public ServerConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Electric Lights Configuration Settings").push("world");

            nodeConnectDist = builder.translation("nodeConnectDist").comment("The maximum connection distance for network components. Uses real distance.").defineInRange("nodeConnectDist", 16, 4, 64);
            minimumSwitchboardUpdateInterval = builder.translation("minimumSwitchboardUpdateInterval").comment("The minimum number of ticks between switchboard updates.").defineInRange("minimumSwitchboardUpdateInterval", 10, 5, 40);
            alternatorEnergyFactor = builder.translation("alternatorEnergyFactor").comment("The energy production factor of the Alternator generator.").defineInRange("alternatorEnergyFactor", 420, 60, 2040);
            solarEnergyFactor = builder.translation("solarEnergyFactor").comment("The energy production factor of the Solar generator.").defineInRange("solarEnergyFactor", 240, 60, 2040);
            geothermalEnergyFactor = builder.translation("geothermalEnergyFactor").comment("The energy production factor of the Geothermal generator.").defineInRange("geothermalEnergyFactor", 120, 60, 2040);
            builder.pop();
        }

        public int nodeConnectDistSqr() {
            return (int) Math.pow(this.nodeConnectDist.get(),2);
        }
        public int minimumSwitchboardUpdateInterval() {
            return this.minimumSwitchboardUpdateInterval.get();
        }
        public int alternatorEnergyFactor() {
            return this.alternatorEnergyFactor.get();
        }
        public int solarEnergyFactor() {
            return this.solarEnergyFactor.get();
        }
        public int geothermalEnergyFactor() {
            return this.geothermalEnergyFactor.get();
        }
    }

}

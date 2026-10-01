package com.kiancars.carmod.registry;

import com.kiancars.carmod.CarMod;
import com.kiancars.carmod.hero.HeroProgress;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CarMod.MOD_ID);

    public static final Supplier<AttachmentType<HeroProgress>> HERO_PROGRESS =
            ATTACHMENTS.register("hero_progress", () -> AttachmentType.builder(() -> HeroProgress.START)
                    .serialize(HeroProgress.CODEC)
                    .copyOnDeath()
                    .build());

    private ModAttachments() {
    }
}

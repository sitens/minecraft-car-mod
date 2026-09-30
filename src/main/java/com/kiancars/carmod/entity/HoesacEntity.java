package com.kiancars.carmod.entity;

import com.kiancars.carmod.registry.ModTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * hOesaC: a junk-food-hungry mob built on the zombie. 20 hearts, a bit faster than a zombie, doesn't burn
 * in daylight, and only goes after players who are carrying junk food —
 * without junk food he leaves you alone, even if you hit him. If he kills
 * you, he keeps your junk food (everything else drops normally) and gives
 * it back when he dies.
 */
public class HoesacEntity extends Zombie {

    private static final String STOLEN_KEY = "StolenJunkFood";

    private final List<ItemStack> stolenFood = new ArrayList<>();

    public HoesacEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        // 20 hearts, and a little faster than a zombie (0.23).
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27);
    }

    @Override
    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(3, new ZombieAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                (target, level) -> ModTags.isCarryingJunkFood(target)));
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (target instanceof Player && !ModTags.isCarryingJunkFood(target)) {
            return false;
        }
        return super.canAttack(target);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        LivingEntity target = this.getTarget();
        if (target instanceof Player && !ModTags.isCarryingJunkFood(target)) {
            this.setTarget(null);
        }
    }

    @Override
    protected boolean isSunSensitive() {
        return false;
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                  EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        // Always a grown-up hOesaC, never a baby or a chicken jockey.
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, new Zombie.ZombieGroupData(false, false));
        this.setCanPickUpLoot(false);
        return result;
    }

    public void takeJunkFood(ItemStack stack) {
        this.stolenFood.add(stack.copy());
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        for (ItemStack stack : this.stolenFood) {
            this.spawnAtLocation(level, stack);
        }
        this.stolenFood.clear();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store(STOLEN_KEY, ItemStack.CODEC.listOf(), List.copyOf(this.stolenFood));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.stolenFood.clear();
        input.read(STOLEN_KEY, ItemStack.CODEC.listOf()).ifPresent(this.stolenFood::addAll);
    }
}

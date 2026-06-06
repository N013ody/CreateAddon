package io.github.n013ody.createaddon.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * 传感器脉冲粒子
 * 用于显示传感器的活动状态
 */
public class SensorPulseParticle extends TextureSheetParticle {

    private final float baseAlpha;
    private final float pulseSpeed;

    public SensorPulseParticle(ClientLevel level, double x, double y, double z,
                                double xSpeed, double ySpeed, double zSpeed,
                                SpriteSet spriteSet, int color, float size) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);

        this.sprite = spriteSet.get(level.random);
        this.quadSize = size;
        this.lifetime = 20 + level.random.nextInt(20);

        // 设置颜色
        this.rCol = ((color >> 16) & 0xFF) / 255.0F;
        this.gCol = ((color >> 8) & 0xFF) / 255.0F;
        this.bCol = (color & 0xFF) / 255.0F;
        this.baseAlpha = ((color >> 24) & 0xFF) / 255.0F;
        this.alpha = baseAlpha;

        // 设置速度
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;

        // 设置脉冲速度
        this.pulseSpeed = 0.15F + level.random.nextFloat() * 0.1F;

        this.hasPhysics = false;
    }

    @Override
    public void tick() {
        super.tick();

        // 脉冲效果
        float pulse = (float) (0.5F + 0.5F * Math.sin(age * pulseSpeed));
        this.alpha = baseAlpha * pulse;

        // 缩小效果
        float scale = 1.0F - (float) age / lifetime;
        this.quadSize *= scale;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    /**
     * 创建传感器脉冲粒子
     */
    public static class Provider implements net.minecraft.client.particle.ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;
        private final int color;
        private final float size;

        public Provider(SpriteSet spriteSet, int color, float size) {
            this.spriteSet = spriteSet;
            this.color = color;
            this.size = size;
        }

        @Override
        public SensorPulseParticle createParticle(SimpleParticleType type, ClientLevel level,
                                                   double x, double y, double z,
                                                   double xSpeed, double ySpeed, double zSpeed) {
            return new SensorPulseParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet, color, size);
        }
    }
}

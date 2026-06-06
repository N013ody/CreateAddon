package io.github.n013ody.createaddon.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * 激光扫描粒子
 * 用于显示雷达和激光扫描效果
 */
public class LaserScanParticle extends TextureSheetParticle {

    private final float baseAlpha;
    private final float scanSpeed;

    public LaserScanParticle(ClientLevel level, double x, double y, double z,
                              double xSpeed, double ySpeed, double zSpeed,
                              SpriteSet spriteSet, int color, float size) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);

        this.sprite = spriteSet.get(level.random);
        this.quadSize = size;
        this.lifetime = 30 + level.random.nextInt(30);

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

        // 设置扫描速度
        this.scanSpeed = 0.1F + level.random.nextFloat() * 0.05F;

        this.hasPhysics = false;
    }

    @Override
    public void tick() {
        super.tick();

        // 扫描效果
        float scan = (float) (0.5F + 0.5F * Math.sin(age * scanSpeed));
        this.alpha = baseAlpha * scan;

        // 移动效果
        this.x += Math.sin(age * 0.1) * 0.01;
        this.z += Math.cos(age * 0.1) * 0.01;

        // 缩小效果
        float scale = 1.0F - (float) age / lifetime;
        this.quadSize *= scale;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    /**
     * 创建激光扫描粒子
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
        public LaserScanParticle createParticle(SimpleParticleType type, ClientLevel level,
                                                 double x, double y, double z,
                                                 double xSpeed, double ySpeed, double zSpeed) {
            return new LaserScanParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet, color, size);
        }
    }
}

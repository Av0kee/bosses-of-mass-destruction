package net.barribob.boss.particle

import net.barribob.maelstrom.static_utilities.RandomUtils
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.Camera
import net.minecraft.particle.ParticleEffect
import net.minecraft.util.math.Vec3d

class ClientParticleBuilder(private val effect: ParticleEffect) {
    private var getVel: () -> Vec3d = { Vec3d.ZERO }
    private var color: ((Float) -> Vec3d)? = null
    private var brightness: ((Float) -> Int)? = null
    private var scale: ((Float) -> Float)? = null
    private var age: (() -> Int)? = null
    private var colorVariation: Double = 0.0

    // Todo: either make this not a function, or add in velocity changes over the lifetime of a particle?
    // Right now it does function as being able to define the velocity when the builder is defined, but for the
    // most part, I think we've just been using it at particle creation time, or as a constant velocity...
    fun velocity(velocity: () -> Vec3d): ClientParticleBuilder {
        this.getVel = velocity
        return this
    }

    fun color(color: ((Float) -> Vec3d)?): ClientParticleBuilder {
        this.color = color
        return this
    }

    fun color(color: Vec3d): ClientParticleBuilder {
        this.color = { color }
        return this
    }

    fun brightness(brightness: Int): ClientParticleBuilder {
        this.brightness = { brightness }
        return this
    }

    fun scale(scale: ((Float) -> Float)?): ClientParticleBuilder {
        this.scale = scale
        return this
    }

    fun scale(scale: Float): ClientParticleBuilder {
        this.scale = { scale }
        return this
    }

    fun age(age: (() -> Int)?): ClientParticleBuilder {
        this.age = age
        return this
    }

    fun age(age: Int): ClientParticleBuilder {
        this.age = { age }
        return this
    }

    fun age(min: Int, max: Int): ClientParticleBuilder {
        this.age = { RandomUtils.range(min, max) }
        return this
    }

    fun colorVariation(variation: Double): ClientParticleBuilder {
        this.colorVariation = variation
        return this
    }

    fun build(pos: Vec3d) {
        val client = MinecraftClient.getInstance()
        val camera: Camera = client.gameRenderer.camera
        if (client != null && camera.isReady && client.particleManager != null) {
            val vel = getVel()
            val particle = client.particleManager.addParticle(effect, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z)
                ?: return

            scale?.let { particle.scale(it(0f)) }
            color?.let {
                val clr = it(0f)
                particle.setColor(clr.x.toFloat(), clr.y.toFloat(), clr.z.toFloat())
            }
            age?.let { particle.maxAge = it() }

            if (particle is SimpleParticle) {
                brightness?.let { particle.setBrightnessOverride(brightness) }
                color?.let { particle.setColorOverride(color) }
                scale?.let { particle.setScaleOverride(scale) }
                particle.setColorVariation(colorVariation)
            }
        }
    }
}
package com.mygdx.game

import com.badlogic.ashley.core.*
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.google.inject.*

//most included methods at this point is from youtube series.
class MyGdxGame : ApplicationAdapter() {
    internal lateinit var batch: SpriteBatch
    internal lateinit var p1: Texture
    internal lateinit var p2: Texture
    internal lateinit var puck: Texture
    internal val engine = Engine()
    private lateinit var injector: Injector

    //creates instance of object given a texture.
    override fun create() {
        batch = SpriteBatch()
        p1 = Texture("player1.png")
        p2 = Texture("player2.png")
        puck = Texture("puck.png")
        //injects the object into the game system
        injector  = Guice.createInjector(GameModule(this))
        injector.getInstance(Systems::class.java).list.map { injector.getInstance(it) }.forEach { system ->
            engine.addSystem(system)
        }
        createEntities()
    }

    //adds entity to game engine, sets up it's texture and transform components
    private fun createEntities() {
        val world = injector.getInstance(World::class.java)
        engine.addEntity(Entity().apply {
            add(TextureComponent(p1))
            add(TransformComponent(Vector2(5F, 5F)))

            val p1Body = world.createBody(BodyDef().apply {
                type = BodyDef.BodyType.DynamicBody
            })
            p1Body.createFixture(PolygonShape().apply {
                setAsBox(p1.width.pixelsToMeters / 2F, p1.height.pixelsToMeters / 2F)
            }, 2.0F)
            p1Body.setTransform(transform.position, 0F)
            add(PhysicsComponent(p1Body))
        })
        engine.addEntity(Entity().apply {
            add(TextureComponent(p2))
            add(TransformComponent(Vector2(9F, 5F)))
            val p2Body = world.createBody(BodyDef().apply {
                type = BodyDef.BodyType.DynamicBody
            })
            p2Body.createFixture(PolygonShape().apply {
                setAsBox(p2.width.pixelsToMeters / 2F, p2.height.pixelsToMeters / 2F)
            }, 2.0F)
            p2Body.setTransform(transform.position, 0F)
            add(PhysicsComponent(p2Body))
        })
        engine.addEntity(Entity().apply {
            add(TextureComponent(puck))
            add(TransformComponent(Vector2(7F, 5F)))
            val puckBody = world.createBody(BodyDef().apply {
                type = BodyDef.BodyType.DynamicBody
            })
            puckBody.createFixture(CircleShape().apply {
                setRadius(puck.width.pixelsToMeters / 2F)
            }, 1.0F)
            puckBody.setTransform(transform.position, 0F)
            add(PhysicsComponent(puckBody))
        })
    }

    //renters the game graphics
    override fun render() {
        Gdx.gl.glClearColor(1f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        engine.update(Gdx.graphics.deltaTime)
    }

    //delets graphics
    override fun dispose() {
        batch.dispose()
        p1.dispose()
        p2.dispose()
        puck.dispose()
    }
}

class PhysicsSynchronizationSystem : IteratingSystem(Family.all(TransformComponent::class.java, PhysicsComponent::class.java).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity.transform.position.set(entity.physics.body.position)
    }
}

class PhysicsSystem @Inject constructor(private val world: World) : EntitySystem() {
    private var accumulator = 0F
    override fun update(deltaTime: Float) {
        val frameTime = Math.min(deltaTime, 0.25F)
        accumulator += frameTime
        while (accumulator >= TIME_STEP) {
            world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS)
            accumulator -= TIME_STEP
        }
    }
    companion object {
        private val TIME_STEP = 1.0F / 300F
        private val VELOCITY_ITERATIONS = 6
        private val POSITION_ITERATIONS = 2
    }
}

class PhysicsDebugSystem @Inject constructor(private val world: World,
                                             private val camera: OrthographicCamera) : EntitySystem() {
    private val renderer = Box2DDebugRenderer()

    override fun update(deltaTime: Float) {
        renderer.render(world, camera.combined)
    }
}

//used for testing objects and features, just prints a lot of info very fast
//class SpamSystem @Inject constructor(private val spriteBatch: SpriteBatch) : EntitySystem() {
//    override fun update(deltaTime: Float) {
//        println(deltaTime.toString() + "; " + spriteBatch)
//    }
//}

//renders current view using camera and sprites able to be seen
class RenderingSystem @Inject constructor(private val batch: SpriteBatch,
                                          private val camera: OrthographicCamera) : IteratingSystem(Family.all(TransformComponent::class.java, TextureComponent::class.java).get()) {
    override fun update(deltaTime: Float) {
        batch.projectionMatrix = camera.combined //loads the cameras current matrix
        batch.begin()
        super.update(deltaTime)
        batch.end()
    }

    //process's an entity and draws it's current location
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val img = entity.texture.texture
        val position = entity.transform.position
        batch.draw(img,
                position.x - img.width.pixelsToMeters / 2F, position.y - img.height.pixelsToMeters / 2F,
                img.width.pixelsToMeters, img.height.pixelsToMeters)
    }
}

//usfull for position tracking with the camera
val Int.pixelsToMeters: Float
        get() = this / 32F

//module for game, sets up different features such as camera and systems
class GameModule(private val myGdxGame: MyGdxGame) : Module {
    override fun configure(binder: Binder) {
        binder.bind(SpriteBatch::class.java).toInstance(myGdxGame.batch)
    }

    //sets up the rendering system
    @Provides @Singleton
    fun systems() : Systems {
        return Systems(listOf(
                PhysicsSystem::class.java,
                PhysicsSynchronizationSystem::class.java,
                RenderingSystem::class.java,
                PhysicsDebugSystem::class.java
        ))
    }

    //camera used to display objects in the sim
    @Provides @Singleton
    fun camera() : OrthographicCamera {
        val viewportWidth = Gdx.graphics.width.pixelsToMeters //makes it default to see entire scene at start
        val viewportHeight = Gdx.graphics.height.pixelsToMeters
        return OrthographicCamera(viewportWidth, viewportHeight).apply {
            position.set(viewportWidth / 2F, viewportHeight / 2F, 0F)
            update()
        }
    }

    @Provides @Singleton
    fun world() : World {
        Box2D.init()
        return World(Vector2(0F, 0F), true)
    }
}

data class Systems (val list: List<Class<out EntitySystem>>)
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
import com.google.inject.*

//most included methods at this point is from youtube series.
class MyGdxGame : ApplicationAdapter() {
    internal lateinit var batch: SpriteBatch
    internal lateinit var img: Texture
    internal val engine = Engine()
    private lateinit var injector: Injector

    //creates instance of object given a texture.
    override fun create() {
        batch = SpriteBatch()
        img = Texture("badlogic.jpg")
        //injects the object into the game system
        injector  = Guice.createInjector(GameModule(this))
        injector.getInstance(Systems::class.java).list.map { injector.getInstance(it) }.forEach { system ->
            engine.addSystem(system)
        }
        createEntities()
    }

    //adds entity to game engine, sets up it' texture and transform components
    private fun createEntities() {
        engine.addEntity(Entity().apply {
            add(TextureComponent(img))
            add(TransformComponent(Vector2(1F, 1F)))
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
        img.dispose()
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
        batch.draw(img, position.x, position.y, img.width.pixelsToMeters, img.height.pixelsToMeters)
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
                RenderingSystem::class.java
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
}

data class Systems (val list: List<Class<out EntitySystem>>)
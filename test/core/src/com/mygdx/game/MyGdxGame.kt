package com.mygdx.game

import com.badlogic.ashley.core.*
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.controllers.Controller
import com.badlogic.gdx.controllers.Controllers
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef
import com.badlogic.gdx.utils.Array
import com.google.inject.*

//most included methods at this point is from youtube series.
var playerB: MutableList<Body> = mutableListOf()
var playerS: MutableList<RevoluteJoint> = mutableListOf()

class MyGdxGame : ApplicationAdapter() {
    internal lateinit var batch: SpriteBatch
    internal lateinit var p1: Texture
    internal lateinit var p2: Texture
    internal lateinit var puck: Texture
    internal lateinit var net1: Texture
    internal lateinit var net2: Texture
    internal lateinit var stick: Texture
    internal val engine = Engine()
    private lateinit var injector: Injector

    //creates instance of object given a texture.
    override fun create() {
        batch = SpriteBatch()
        p1 = Texture("player1.png")
        p2 = Texture("player2.png")
        puck = Texture("puck.png")
        net1 = Texture("net1.png")
        net2 = Texture("net2.png")
        stick = Texture("stick.png")
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
            val border = world.createBody(BodyDef().apply {
                type = BodyDef.BodyType.StaticBody
            })
            border.userData = "border"
            border.createFixture(EdgeShape().apply {
                set(Vector2(0F, 0F), Vector2(30F, 0F))
            }, 1.0F)
            border.createFixture(EdgeShape().apply {
                set(Vector2(30F, 0F), Vector2(30f, 20F))
            }, 1.0F)
            border.createFixture(EdgeShape().apply {
                set(Vector2(0F, 20F), Vector2(30F, 20F))
            }, 1.0F)
            border.createFixture(EdgeShape().apply {
                set(Vector2(0F, 20F), Vector2(0F, 0F))
            }, 1.0F)
        })
        engine.addEntity(Entity().apply {
            add(TextureComponent(p1))
            add(TransformComponent(Vector2(12F, 10F)))

            val p1Body = world.createBody(BodyDef().apply {
                type = BodyDef.BodyType.DynamicBody
            })
            p1Body.createFixture(PolygonShape().apply {
                setAsBox(p1.width.pixelsToMeters / 2F, p1.height.pixelsToMeters / 2F)
            }, 2.0F)
            p1Body.userData = "p1"
            p1Body.setTransform(transform.position, 0F)
            add(PhysicsComponent(p1Body))
            playerB.add(p1Body)
        })
        engine.addEntity(Entity().apply {
            add(TextureComponent(stick))
            add(TransformComponent(Vector2(12F, 10F)))

            val stickBody = world.createBody(BodyDef().apply {
                type = BodyDef.BodyType.DynamicBody
            })
            stickBody.createFixture(PolygonShape().apply {
                setAsBox(stick.width.pixelsToMeters / 2F, stick.height.pixelsToMeters / 2F)
            }, 0.5F)
            stickBody.userData = "p1 stick"
            stickBody.setTransform(transform.position, 0F)
            add(PhysicsComponent(stickBody))
            val joint = world.createJoint(RevoluteJointDef().apply {
                bodyA = playerB[0]
                bodyB = stickBody
                collideConnected = false
                localAnchorA.set(Vector2(0f, 0f))
                localAnchorB.set(Vector2(0f, -1f))
                enableMotor = true
                enableLimit = true
                upperAngle = 45 * MathUtils.degreesToRadians
                lowerAngle = -45 * MathUtils.degreesToRadians
            }) as RevoluteJoint
            playerS.add(joint)
        })
        engine.addEntity(Entity().apply {
            add(TextureComponent(p2))
            add(TransformComponent(Vector2(18F, 10F)))
            val p2Body = world.createBody(BodyDef().apply {
                type = BodyDef.BodyType.DynamicBody
            })
            p2Body.createFixture(PolygonShape().apply {
                setAsBox(p2.width.pixelsToMeters / 2F, p2.height.pixelsToMeters / 2F)
            }, 2.0F)
            p2Body.userData = "p2"
            p2Body.setTransform(transform.position, 0F)
            add(PhysicsComponent(p2Body))
            playerB.add(p2Body)
        })
        engine.addEntity(Entity().apply {
            add(TextureComponent(stick))
            add(TransformComponent(Vector2(18F, 10F)))

            val stickBody = world.createBody(BodyDef().apply {
                type = BodyDef.BodyType.DynamicBody
            })
            stickBody.createFixture(PolygonShape().apply {
                setAsBox(stick.width.pixelsToMeters / 2F, stick.height.pixelsToMeters / 2F)
            }, 0.5F)
            stickBody.userData = "p2 stick"
            stickBody.setTransform(transform.position, 0F)
            add(PhysicsComponent(stickBody))
            val joint = world.createJoint(RevoluteJointDef().apply {
                bodyA = playerB[1]
                bodyB = stickBody
                collideConnected = false
                localAnchorA.set(Vector2(0f, 0f))
                localAnchorB.set(Vector2(0f, -1f))
                enableMotor = true
                enableLimit = true
                upperAngle = 45 * MathUtils.degreesToRadians
                lowerAngle = -45 * MathUtils.degreesToRadians
            }) as RevoluteJoint
            playerS.add(joint)
        })
        engine.addEntity(Entity().apply {
            add(TextureComponent(puck))
            add(TransformComponent(Vector2(15F, 10F)))
            val puckBody = world.createBody(BodyDef().apply {
                type = BodyDef.BodyType.DynamicBody
            })
            puckBody.createFixture(CircleShape().apply {
                setRadius(puck.width.pixelsToMeters / 2F)
            }, 1.0F)
            puckBody.userData = "puck"
            puckBody.setTransform(transform.position, 0F)
            add(PhysicsComponent(puckBody))
            playerB.add(puckBody)
        })
        engine.addEntity(Entity().apply {
            add(TextureComponent(net1))
            add(TransformComponent(Vector2(3F, 10F)))
            val netBody = world.createBody(BodyDef().apply {
                type = BodyDef.BodyType.StaticBody
            })
            netBody.createFixture(PolygonShape().apply {
                setAsBox(net1.width.pixelsToMeters / 2F, net1.height.pixelsToMeters / 2F)
            }, 0F)
            netBody.userData = "net1 Visual"
            netBody.setTransform(transform.position, 0F)
            add(PhysicsComponent(netBody))
        })
        engine.addEntity(Entity().apply { //net1 3F, 7.5F net2 17F, 7.5F W: 0.46875 H: 2.34375
            add(TransformComponent(Vector2(3.3F, 10F)))
            val netCollide = world.createBody(BodyDef().apply {
                type = BodyDef.BodyType.StaticBody
            })
            netCollide.createFixture(PolygonShape().apply {
                setAsBox(net1.width.pixelsToMeters / 4F, net1.height.pixelsToMeters / 2.2F)
            }, 0F)
            netCollide.userData = "net1"
            netCollide.setTransform(transform.position, 0F)
            add(PhysicsComponent(netCollide))
        })
        engine.addEntity(Entity().apply {
            add(TextureComponent(net2))
            add(TransformComponent(Vector2(27F, 10F)))
            val netBody = world.createBody(BodyDef().apply {
                type = BodyDef.BodyType.StaticBody
            })
            netBody.createFixture(PolygonShape().apply {
                setAsBox(net2.width.pixelsToMeters / 2F, net2.height.pixelsToMeters / 2F)
            }, 0F)
            netBody.userData = "net2 Visual"
            netBody.setTransform(transform.position, 0F)
            add(PhysicsComponent(netBody))
        })
        engine.addEntity(Entity().apply { //net1 3F, 7.5F net2 17F, 7.5F W: 0.46875 H: 2.34375
            add(TransformComponent(Vector2(26.7F, 10F)))
            val netCollide = world.createBody(BodyDef().apply {
                type = BodyDef.BodyType.StaticBody
            })
            netCollide.createFixture(PolygonShape().apply {
                setAsBox(net2.width.pixelsToMeters / 4F, net2.height.pixelsToMeters / 2.2F)
            }, 0F)
            netCollide.userData = "net2"
            netCollide.setTransform(transform.position, 0F)
            add(PhysicsComponent(netCollide))
        })
    }

    //renters the game graphics
    override fun render() {
        Gdx.gl.glClearColor(1f, 1f, 1f, 1f)
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
    private val controlArray: Array<Controller> = Controllers.getControllers()
    private val con1: Controller = controlArray[0]
    private val con2: Controller = controlArray[1]
    private var p1Score: Int = 0
    private var p2Score: Int = 0
    override fun update(deltaTime: Float) {
        val frameTime = Math.min(deltaTime, 0.25F)
        accumulator += frameTime
        while (accumulator >= TIME_STEP) {
            world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS)
            for (contact in world.contactList) { //ball mass: 0.69029135 net1: net2:
                if ((contact.fixtureA.body.userData =="puck" || contact.fixtureB.body.userData == "puck")
                    && (contact.fixtureA.body.userData == "net1" || contact.fixtureB.body.userData == "net1")) {
                    p2Score += 1
                    Gdx.app.log("P2 Scores", "P1: " + p1Score + " P2: " + p2Score)
                    reset()
                } else if ((contact.fixtureA.body.userData == "puck" || contact.fixtureB.body.userData == "puck")
                        && (contact.fixtureA.body.userData == "net2" || contact.fixtureB.body.userData == "net2")) {
                    p1Score += 1
                    Gdx.app.log("P1 Scores", "P1: " + p1Score + " P2: " + p2Score)
                    reset()
                }
            }
            accumulator -= TIME_STEP
            playerB[0].applyForceToCenter(getDir(0),true)
            playerB[1].applyForceToCenter(getDir(1),true)
            playerB[0].applyAngularImpulse(getDir2(0), true)
            playerB[1].applyAngularImpulse(getDir2(1), true)
            if (playerB[0].angularVelocity > 5) playerB[0].angularVelocity = 5F
            else if (playerB[0].angularVelocity < -5) playerB[0].angularVelocity = -5F
            if (playerB[1].angularVelocity > 5) playerB[1].angularVelocity = 5F
            else if (playerB[1].angularVelocity < -5) playerB[1].angularVelocity = -5F
            var info = stickMove(0)
            playerS[0].motorSpeed = info.x
            playerS[0].maxMotorTorque = info.y
            info = stickMove(1)
            playerS[1].motorSpeed = info.x
            playerS[1].maxMotorTorque = info.y
        }
    }
    companion object {
        private val TIME_STEP = 1.0F / 300F
        private val VELOCITY_ITERATIONS = 6
        private val POSITION_ITERATIONS = 2
    }

    fun getDir(player: Int) : Vector2 {
        var force: Vector2 = Vector2(0F,0F)
        if (player == 0) {
            force.x = (con1.getAxis(0) * 6)
            force.y = (-con1.getAxis(1) * 6)
        } else {
            force.x = (con2.getAxis(0) * 6)
            force.y = (-con2.getAxis(1) * 6)
        }
        //Gdx.app.log("Pressed", con1.getAxis(3).toString())
        return force
    }

    fun getDir2(player: Int) : Float {
        var impulse: Float = 0F
        if (player == 0) {
            impulse = (-con1.getAxis(3) / 30F)
        } else {
            impulse = (-con2.getAxis(3) / 30F)
        }
        return impulse
    }

    fun stickMove(player: Int) : Vector2 {
        var speed = 0f
        var tourque: Float = 20f
        if (player == 0) { //360 * DEGTORAD 1 turn per second counter clockwise
            if (con1.getAxis(2) != 0F) {
                speed = (con1.getAxis(2) + 1) * 200 * MathUtils.degreesToRadians
            }
            if (con1.getAxis(5) != 0F) {
                speed += -(con1.getAxis(5) + 1) * 200 * MathUtils.degreesToRadians
            }
        } else {
            if (con2.getButton(4)) {
                speed = 200 * MathUtils.degreesToRadians
            }
            if (con2.getButton(5)) {
                speed = -200 * MathUtils.degreesToRadians
            }
        }
        return Vector2(speed, tourque)
    }

    fun reset() {
        var list = listOf<Float>(12F, 18F, 15F)
        var i = 0
        for (player in playerB) {
            player.setTransform(list[i], 10F, 0F)
            player.angularVelocity = 0F
            player.linearVelocity = Vector2(0F, 0F)
            i += 1
        }
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
        val angle = entity.physics.body.angle
        batch.draw(img,
                position.x - img.width.pixelsToMeters / 2F, position.y - img.height.pixelsToMeters / 2F,
                img.width.pixelsToMeters / 2F, img.height.pixelsToMeters / 2F, img.width.pixelsToMeters,
                img.height.pixelsToMeters, 1F, 1F, angle * MathUtils.radiansToDegrees,
                0, 0, img.width, img.height, false, false)
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
                RenderingSystem::class.java
                //PhysicsDebugSystem::class.java //used for bug testing, visualized physics
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
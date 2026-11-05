package com.acechase.kidclock.art

import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.core.graphics.PathParser

/**
 * The boy's notebook doodles: a rocket, a tank, monsters, dinosaurs, a shark, a snake and the
 * rest, each traced on the design canvas as SVG path data in a 100x100 box. Every function draws
 * one doodle in that box with the paint it is given; the caller positions, scales and turns the
 * canvas first (see DoodleNotebookFace). Fills go down before outlines, like marker over pencil.
 * Stroke widths are in box units, so they scale with the doodle.
 */
object BoyDoodles {
    const val NAVY = 0xFF2A2F45.toInt()
    const val BLUE = 0xFF2F6FB5.toInt()
    const val GREEN = 0xFF2E8B57.toInt()
    const val RED = 0xFFD9414F.toInt()
    const val ORANGE = 0xFFE8782A.toInt()
    const val PURPLE = 0xFF7C4DBD.toInt()
    const val YELLOW = 0xFFFFD23F.toInt()
    const val BROWN = 0xFF8A5A3B.toInt()
    const val WHITE = 0xFFFFFFFF.toInt()
    const val SKY = 0xFF7FC8EC.toInt()
    const val GREY = 0xFF9CA3AF.toInt()
    const val STEEL = 0xFF6B7280.toInt()

    private val rect = RectF()
    private val dash55 = DashPathEffect(floatArrayOf(5f, 5f), 0f)
    private val dash56 = DashPathEffect(floatArrayOf(5f, 6f), 0f)
    private val dash44 = DashPathEffect(floatArrayOf(4f, 4f), 0f)

    // Rocket: red body and fins, blue porthole, orange flame with a yellow core, sparkle crosses.
    private val rocketBody = path("M50 4 C70 20 72 52 62 76 H38 C28 52 30 20 50 4 Z")
    private val rocketFins = path("M38 56 L20 82 L38 78 Z M62 56 L80 82 L62 78 Z")
    private val rocketFlame = path("M40 78 C42 96 58 96 60 78")
    private val rocketCore = path("M46 80 C47 90 53 90 54 80")
    private val rocketSparks = path("M12 22 l6 6 M18 22 l-6 6 M88 34 l6 6 M94 34 l-6 6")

    fun rocket(c: Canvas, p: Paint) {
        prep(p)
        stroke(p, RED, 4f); c.drawPath(rocketBody, p); c.drawPath(rocketFins, p)
        stroke(p, BLUE, 4f); c.drawCircle(50f, 36f, 9f, p)
        fill(p, BLUE); c.drawCircle(50f, 36f, 3f, p)
        fillStroke(c, p, rocketFlame, ORANGE, ORANGE, 3f)
        fill(p, YELLOW); c.drawPath(rocketCore, p)
        stroke(p, NAVY, 2.5f); c.drawPath(rocketSparks, p)
    }

    // Three-eyed green blob with an antenna.
    private val blobBody = path("M20 60 C10 30 40 14 56 18 C80 22 92 44 84 66 C78 84 60 92 40 86 C26 82 24 72 20 60 Z")
    private val blobMouth = path("M34 66 Q54 84 76 66 M42 70 l4 8 l4 -8 M58 74 l4 8 l4 -8")
    private val blobAntenna = path("M56 18 L62 2")
    private val blobLegs = path("M40 88 l-4 10 M62 86 l4 10")

    fun blobMonster(c: Canvas, p: Paint) {
        prep(p)
        stroke(p, GREEN, 4f); c.drawPath(blobBody, p)
        eye(c, p, 38f, 46f, 9f, 40f, 47f, 4f, GREEN, 3f)
        eye(c, p, 58f, 38f, 11f, 60f, 40f, 5f, GREEN, 3f)
        eye(c, p, 74f, 52f, 7f, 75f, 53f, 3f, GREEN, 3f)
        stroke(p, GREEN, 3.5f); c.drawPath(blobMouth, p); c.drawPath(blobAntenna, p)
        fill(p, GREEN); c.drawCircle(63f, 2f, 4f, p)
        stroke(p, GREEN, 4f); c.drawPath(blobLegs, p)
    }

    // Tank firing, with an orange muzzle flash.
    private val tankBarrel = path("M62 42 H92")
    private val tankFlash = path("M92 42 l10 -9 l-4 9 l12 2 l-12 2 l4 9 Z")

    fun tank(c: Canvas, p: Paint) {
        prep(p)
        stroke(p, NAVY, 4f)
        roundRect(c, p, 10f, 48f, 70f, 20f, 8f)
        roundRect(c, p, 32f, 34f, 30f, 16f, 4f)
        stroke(p, NAVY, 3f); c.drawCircle(47f, 34f, 5f, p)
        stroke(p, NAVY, 4f); c.drawPath(tankBarrel, p)
        fillStroke(c, p, tankFlash, ORANGE, RED, 2.5f)
        stroke(p, NAVY, 4f); roundRect(c, p, 6f, 64f, 80f, 22f, 11f)
        stroke(p, NAVY, 3f)
        c.drawCircle(20f, 75f, 5f, p); c.drawCircle(38f, 75f, 5f, p); c.drawCircle(56f, 75f, 5f, p); c.drawCircle(74f, 75f, 5f, p)
    }

    // Square purple monster with spiky hair and its tongue out.
    private val squareHair = path("M28 30 l4 -14 l6 14 l5 -12 l5 12 l6 -14 l5 14 l6 -12 l6 12")
    private val squareTeeth = path("M32 68 l8 6 l8 -6 l8 6 l8 -6 l8 6")
    private val squareTongue = path("M56 73 C58 86 68 86 66 74")
    private val squareLimbs = path("M36 82 v10 M68 82 v10 M24 56 l-14 8 M80 56 l14 -6")

    fun squareMonster(c: Canvas, p: Paint) {
        prep(p)
        fill(p, WHITE); roundRect(c, p, 24f, 30f, 56f, 52f, 6f)
        stroke(p, PURPLE, 4f); roundRect(c, p, 24f, 30f, 56f, 52f, 6f)
        stroke(p, PURPLE, 3.5f); c.drawPath(squareHair, p)
        stroke(p, PURPLE, 3f); c.drawCircle(40f, 50f, 10f, p)
        fill(p, NAVY); c.drawCircle(42f, 52f, 5f, p)
        stroke(p, PURPLE, 3f); c.drawCircle(64f, 48f, 6f, p)
        fill(p, NAVY); c.drawCircle(65f, 49f, 3f, p)
        stroke(p, PURPLE, 3.5f); c.drawPath(squareTeeth, p)
        fillStroke(c, p, squareTongue, RED, RED, 2f)
        stroke(p, PURPLE, 4f); c.drawPath(squareLimbs, p)
    }

    // Dragon breathing fire. The green one (full) has two eyes, two legs and a two-tone flame;
    // the small red one at the foot of the page has one of each and a yellow flame.
    private val dragonBody = path("M20 50 C20 30 40 22 58 28 L70 18 L68 34 C78 38 80 50 72 58 L40 62 C28 62 20 58 20 50 Z")
    private val dragonSpikesFull = path("M44 26 L42 8 M58 28 L64 10 M44 60 l3 7 l3 -7 M52 60 l3 7 l3 -7")
    private val dragonSpikes = path("M44 26 L42 8 M58 28 L64 10 M44 60 l3 7 l3 -7")
    private val dragonFlame = path("M74 50 C86 40 100 44 96 56 C104 58 102 72 90 68 C84 76 72 72 74 60 Z")
    private val dragonFlameCore = path("M80 54 C88 50 94 54 92 60 C86 64 80 62 80 54 Z")

    fun dragon(c: Canvas, p: Paint, body: Int, flameFill: Int, flameLine: Int, full: Boolean) {
        prep(p)
        stroke(p, body, 4f); c.drawPath(dragonBody, p)
        fill(p, NAVY); c.drawCircle(52f, 40f, 3.5f, p)
        if (full) c.drawCircle(66f, 50f, 2f, p)
        stroke(p, body, 3.5f); c.drawPath(if (full) dragonSpikesFull else dragonSpikes, p)
        fillStroke(c, p, dragonFlame, flameFill, flameLine, 3f)
        if (full) { fill(p, YELLOW); c.drawPath(dragonFlameCore, p) }
    }

    // Red race car with blue speed lines trailing off to the left.
    private val carBody = path("M6 62 L18 44 H58 L74 62 H94 V74 H6 Z")
    private val carDetail = path("M26 46 l-4 14 h26 l-2 -14 M14 44 l-6 -8 h14")
    private val carSpeed = path("M-4 50 h-16 M0 60 h-22 M-2 70 h-14")

    fun raceCar(c: Canvas, p: Paint) {
        prep(p)
        fillStroke(c, p, carBody, WHITE, RED, 4f)
        stroke(p, RED, 3.5f); c.drawPath(carDetail, p)
        wheel(c, p, 24f, 76f, 10f, 3f)
        wheel(c, p, 72f, 76f, 10f, 3f)
        stroke(p, BLUE, 3f); c.drawPath(carSpeed, p)
    }

    // UFO beaming up a cow.
    private val ufoDome = path("M28 34 A22 18 0 0 1 72 34")
    private val ufoBeam = path("M36 52 L22 100 M64 52 L78 100")
    private val cowBits = path("M42 89 v6 M50 90 v6 M58 88 v6 M36 80 l-6 -2")

    fun ufo(c: Canvas, p: Paint) {
        prep(p)
        fill(p, WHITE); oval(c, p, 50f, 40f, 42f, 12f)
        stroke(p, BLUE, 4f); oval(c, p, 50f, 40f, 42f, 12f); c.drawPath(ufoDome, p)
        fill(p, BLUE); c.drawCircle(24f, 42f, 3f, p); c.drawCircle(50f, 46f, 3f, p); c.drawCircle(76f, 42f, 3f, p)
        stroke(p, BLUE, 2.5f); p.pathEffect = dash55; c.drawPath(ufoBeam, p); p.pathEffect = null
        fill(p, WHITE); oval(c, p, 50f, 82f, 13f, 8f)
        stroke(p, NAVY, 3f); oval(c, p, 50f, 82f, 13f, 8f)
        fill(p, WHITE); c.drawCircle(62f, 78f, 5f, p)
        stroke(p, NAVY, 3f); c.drawCircle(62f, 78f, 5f, p)
        fill(p, NAVY); c.drawCircle(46f, 80f, 3f, p); c.drawCircle(54f, 85f, 2f, p)
        stroke(p, NAVY, 2.5f); c.drawPath(cowBits, p)
    }

    // Stegosaurus with orange plates.
    private val stegoPlates = path("M22 46 l6 -14 l6 14 M34 42 l6 -16 l6 16 M48 40 l6 -16 l6 16 M62 42 l6 -14 l6 14")
    private val stegoHeadTail = path("M80 62 C92 60 96 68 90 74 C84 76 80 72 80 68 M18 66 C6 70 2 80 8 86 M8 86 l-4 -8 M8 86 l6 -6")
    private val stegoLegs = path("M34 80 v12 M46 82 v10 M58 82 v10 M68 80 v12")

    fun stegosaurus(c: Canvas, p: Paint) {
        prep(p)
        stroke(p, GREEN, 4f); oval(c, p, 50f, 62f, 34f, 20f)
        fillStroke(c, p, stegoPlates, ORANGE, GREEN, 3f)
        stroke(p, GREEN, 4f); c.drawPath(stegoHeadTail, p); c.drawPath(stegoLegs, p)
        fill(p, NAVY); c.drawCircle(88f, 66f, 2.5f, p)
    }

    // Jolly roger on a pole.
    private val rogerPole = path("M14 10 V96")
    private val rogerFlag = path("M14 14 H86 L78 40 L86 66 H14 Z")
    private val rogerBones = path("M30 56 l32 -8 M30 48 l32 8")

    fun jollyRoger(c: Canvas, p: Paint) {
        prep(p)
        stroke(p, BROWN, 4f); c.drawPath(rogerPole, p)
        fillStroke(c, p, rogerFlag, NAVY, NAVY, 3f)
        fill(p, WHITE); c.drawCircle(46f, 32f, 10f, p)
        fill(p, NAVY); c.drawCircle(42f, 31f, 2.5f, p); c.drawCircle(50f, 31f, 2.5f, p)
        fill(p, WHITE); rect.set(41f, 40f, 51f, 45f); c.drawRect(rect, p)
        stroke(p, WHITE, 4f); c.drawPath(rogerBones, p)
    }

    // Erupting volcano: lava runs, blobs in the air, two curls of smoke.
    private val volcanoCone = path("M10 88 L36 36 H64 L90 88 Z")
    private val volcanoLava = path("M40 36 C36 52 48 56 44 70 M58 36 C62 48 52 54 58 66")
    private val volcanoSmoke = path("M28 14 q-8 -10 2 -14 M74 12 q10 -8 4 -14")

    fun volcano(c: Canvas, p: Paint) {
        prep(p)
        stroke(p, BROWN, 4f); c.drawPath(volcanoCone, p)
        stroke(p, RED, 3.5f); c.drawPath(volcanoLava, p)
        fill(p, ORANGE); c.drawCircle(50f, 26f, 9f, p); c.drawCircle(65f, 16f, 6f, p)
        fill(p, RED); c.drawCircle(37f, 18f, 5f, p); c.drawCircle(50f, 8f, 4f, p)
        stroke(p, GREY, 3f); c.drawPath(volcanoSmoke, p)
    }

    // T-rex with tiny arms.
    private val trexHead = path("M56 48 C64 32 80 28 92 34 L96 44 L84 46 L88 52 L70 54 C62 54 58 56 56 60")
    private val trexBits = path("M72 52 l3 5 l3 -5 l3 5 l3 -5 M60 58 l8 2 l-3 4 M34 78 l-2 14 l8 0 M50 76 l4 14 l-8 0 M16 56 C4 54 0 70 8 74")

    fun trex(c: Canvas, p: Paint) {
        prep(p)
        stroke(p, GREEN, 4f); oval(c, p, 40f, 60f, 24f, 20f); c.drawPath(trexHead, p)
        fill(p, NAVY); c.drawCircle(84f, 38f, 2.5f, p)
        stroke(p, GREEN, 3.5f); c.drawPath(trexBits, p)
    }

    // A sword behind a shield with a lightning bolt on it.
    private val swordBlade = path("M14 86 L78 22")
    private val swordGuard = path("M68 14 l14 14 M14 86 l-8 8")
    private val shieldBody = path("M50 10 L84 22 C84 60 66 84 50 92 C34 84 16 60 16 22 Z")
    private val shieldBolt = path("M54 26 L40 50 L50 50 L46 70 L62 42 L52 42 Z")

    fun swordShield(c: Canvas, p: Paint) {
        prep(p)
        stroke(p, STEEL, 6f); c.drawPath(swordBlade, p)
        stroke(p, NAVY, 6f); c.drawPath(swordGuard, p)
        fillStroke(c, p, shieldBody, WHITE, BLUE, 4f)
        fillStroke(c, p, shieldBolt, YELLOW, ORANGE, 2f)
    }

    // Blue robot: antenna, square eyes, dial, claws and wheels.
    private val robotAntenna = path("M50 10 V2")
    private val robotFace = path("M40 31 h20 M46 28 v6 M52 28 v6 M46 38 h8 v6")
    private val robotNeedle = path("M50 58 l5 -4")
    private val robotArms = path("M26 54 h-12 v14 M14 68 l-6 8 M14 68 l6 8 M74 54 h12 v12 M86 66 l-5 7 M86 66 l5 7")

    fun robot(c: Canvas, p: Paint) {
        prep(p)
        stroke(p, BLUE, 4f); roundRect(c, p, 32f, 10f, 36f, 28f, 4f)
        stroke(p, BLUE, 3f); c.drawPath(robotAntenna, p)
        fill(p, BLUE); c.drawCircle(50f, 2f, 3f, p)
        rect.set(38f, 17f, 45f, 24f); c.drawRect(rect, p)
        rect.set(55f, 17f, 62f, 24f); c.drawRect(rect, p)
        stroke(p, BLUE, 2.5f); c.drawPath(robotFace, p)
        stroke(p, BLUE, 4f); roundRect(c, p, 26f, 44f, 48f, 34f, 4f)
        stroke(p, BLUE, 3f); c.drawCircle(50f, 58f, 7f, p)
        stroke(p, BLUE, 2.5f); c.drawPath(robotNeedle, p)
        fill(p, RED); c.drawCircle(36f, 70f, 3f, p)
        fill(p, GREEN); c.drawCircle(44f, 70f, 3f, p)
        stroke(p, BLUE, 4f); c.drawPath(robotArms, p)
        c.drawCircle(38f, 86f, 7f, p); c.drawCircle(62f, 86f, 7f, p)
    }

    // Skateboard with a dashed flip arc above it.
    private val boardDeck = path("M10 60 C12 50 20 46 30 46 H70 C80 46 88 50 90 60 M10 60 l-6 -10 M90 60 l6 -10")
    private val boardTrucks = path("M28 62 v-6 M72 62 v-6")
    private val boardArc = path("M20 30 q30 -20 60 0")

    fun skateboard(c: Canvas, p: Paint) {
        prep(p)
        stroke(p, NAVY, 5f); c.drawPath(boardDeck, p)
        stroke(p, NAVY, 4f); c.drawPath(boardTrucks, p)
        fill(p, RED); c.drawCircle(28f, 68f, 6f, p); c.drawCircle(72f, 68f, 6f, p)
        stroke(p, BLUE, 3f); p.pathEffect = dash56; c.drawPath(boardArc, p); p.pathEffect = null
    }

    // Spider hanging on a thread.
    private val spiderThread = path("M50 0 V40")
    private val spiderLegs = path("M42 44 l-16 -10 l-6 8 M58 44 l16 -10 l6 8 M40 50 h-18 l-4 10 M60 50 h18 l4 10 M42 56 l-14 10 l-2 10 M58 56 l14 10 l2 10")

    fun spider(c: Canvas, p: Paint) {
        prep(p)
        stroke(p, NAVY, 2.5f); c.drawPath(spiderThread, p)
        fill(p, NAVY); c.drawCircle(50f, 50f, 10f, p); c.drawCircle(50f, 62f, 6f, p)
        stroke(p, NAVY, 3f); c.drawPath(spiderLegs, p)
        fill(p, WHITE); c.drawCircle(47f, 61f, 1.5f, p); c.drawCircle(53f, 61f, 1.5f, p)
    }

    // Submarine. Yellow with blue bubbles at the left of the page; a plain blue outline at the foot.
    private val subScope = path("M50 32 V18 H60 M90 60 l6 -8 M90 60 l6 8")

    fun submarine(c: Canvas, p: Paint, body: Int, line: Int, porthole: Int, bubbles: Boolean) {
        prep(p)
        if (body != 0) { fill(p, body); oval(c, p, 50f, 60f, 40f, 18f); roundRect(c, p, 42f, 32f, 18f, 14f, 3f) }
        stroke(p, line, 4f); oval(c, p, 50f, 60f, 40f, 18f); roundRect(c, p, 42f, 32f, 18f, 14f, 3f); c.drawPath(subScope, p)
        for (x in floatArrayOf(34f, 50f, 66f)) {
            if (porthole != 0) { fill(p, porthole); c.drawCircle(x, 60f, 5f, p) }
            stroke(p, line, 3f); c.drawCircle(x, 60f, 5f, p)
        }
        if (bubbles) {
            stroke(p, BLUE, 2f)
            c.drawCircle(72f, 24f, 3f, p); c.drawCircle(80f, 14f, 4f, p); c.drawCircle(74f, 6f, 2f, p)
        }
    }

    // The yellow POW! burst.
    private val burstStar = path("M50 10 L58 34 L82 24 L70 44 L94 52 L70 60 L82 80 L58 70 L50 94 L42 70 L18 80 L30 60 L6 52 L30 44 L18 24 L42 34 Z")

    fun powBurst(c: Canvas, p: Paint) {
        prep(p)
        fillStroke(c, p, burstStar, YELLOW, ORANGE, 3.5f)
    }

    // Red monster truck on huge wheels with flames down the side.
    private val truckBody = path("M20 50 L28 34 H64 L76 50 H90 V60 H10 V50 Z")
    private val truckWindow = path("M34 36 l-2 12 h28 l-2 -12")
    private val truckFlames = path("M30 56 l6 -6 l4 6 l4 -6 l4 6 l4 -6 l4 6")

    fun monsterTruck(c: Canvas, p: Paint) {
        prep(p)
        fillStroke(c, p, truckBody, RED, NAVY, 4f)
        fillStroke(c, p, truckWindow, WHITE, NAVY, 3f)
        stroke(p, YELLOW, 2.5f); c.drawPath(truckFlames, p)
        wheel(c, p, 28f, 70f, 18f, 6f)
        wheel(c, p, 72f, 70f, 18f, 6f)
    }

    // Shark leaping, jaws open, over a splash.
    private val sharkBody = path("M10 60 C20 36 56 26 84 36 L94 46 C80 62 50 74 22 70 C14 68 10 64 10 60 Z")
    private val sharkBits = path("M84 46 L72 56 C64 60 56 58 54 54 M58 52 l3 4 l3 -4 l3 4 l3 -4 M44 34 L50 12 L62 32 M10 60 L0 44 M10 60 L2 74 M66 44 q-2 6 0 12")
    private val sharkSplash = path("M20 86 q6 -8 12 0 q6 8 12 0 q6 -8 12 0 q6 8 12 0")

    fun shark(c: Canvas, p: Paint) {
        prep(p)
        stroke(p, BLUE, 4f); c.drawPath(sharkBody, p)
        stroke(p, BLUE, 3.5f); c.drawPath(sharkBits, p)
        fill(p, NAVY); c.drawCircle(76f, 40f, 3f, p)
        stroke(p, SKY, 3f); c.drawPath(sharkSplash, p)
    }

    // Throwing star: navy with dashed spin arcs at the right edge, a small blue one at the foot.
    private val starShape = path("M50 6 L58 42 L94 50 L58 58 L50 94 L42 58 L6 50 L42 42 Z")
    private val starArcs = path("M8 16 q-10 20 0 40 M92 84 q10 -20 0 -40")

    fun throwingStar(c: Canvas, p: Paint, color: Int, arcs: Boolean) {
        prep(p)
        fillStroke(c, p, starShape, color, color, 3f)
        fill(p, WHITE); c.drawCircle(50f, 50f, 6f, p)
        if (arcs) { stroke(p, GREY, 2.5f); p.pathEffect = dash44; c.drawPath(starArcs, p); p.pathEffect = null }
    }

    // Paper airplane with a dashed loopy trail.
    private val planeBody = path("M10 50 L92 14 L62 90 L50 56 Z M50 56 L92 14")
    private val planeTrail = path("M8 56 q-10 10 -20 4 q-10 -6 -18 2")

    fun paperAirplane(c: Canvas, p: Paint) {
        prep(p)
        fillStroke(c, p, planeBody, WHITE, NAVY, 3.5f)
        stroke(p, BLUE, 2.5f); p.pathEffect = dash55; c.drawPath(planeTrail, p); p.pathEffect = null
    }

    // Green snake with yellow stripes and a red forked tongue.
    private val snakeBody = path("M10 70 C20 40 40 40 50 60 C60 80 80 80 90 50")
    private val snakeTongue = path("M96 46 l8 -2 M104 44 l-3 3 M104 44 l-3 -3")
    private val snakeStripes = path("M24 56 l4 -6 M40 48 l4 -6 M58 68 l4 6 M74 72 l4 6")

    fun snake(c: Canvas, p: Paint) {
        prep(p)
        stroke(p, GREEN, 8f); c.drawPath(snakeBody, p)
        fill(p, GREEN); c.drawCircle(90f, 48f, 7f, p)
        fill(p, WHITE); c.drawCircle(92f, 46f, 1.8f, p)
        stroke(p, RED, 2.5f); c.drawPath(snakeTongue, p)
        stroke(p, YELLOW, 3f); c.drawPath(snakeStripes, p)
    }

    // Zap bolt.
    private val boltShape = path("M58 4 L24 54 L46 54 L36 96 L76 40 L54 40 Z")

    fun zapBolt(c: Canvas, p: Paint) {
        prep(p)
        fillStroke(c, p, boltShape, YELLOW, ORANGE, 4f)
    }

    // Baseball bat and ball.
    private val batBarrel = path("M14 86 L80 20")
    private val batGrip = path("M14 86 L28 72")
    private val ballSeams = path("M78 72 q4 6 0 12 M90 72 q-4 6 0 12")

    fun batAndBall(c: Canvas, p: Paint) {
        prep(p)
        stroke(p, BROWN, 9f); c.drawPath(batBarrel, p)
        stroke(p, NAVY, 12f); c.drawPath(batGrip, p)
        fill(p, WHITE); c.drawCircle(84f, 78f, 10f, p)
        stroke(p, NAVY, 3f); c.drawCircle(84f, 78f, 10f, p)
        stroke(p, RED, 2.5f); c.drawPath(ballSeams, p)
    }

    // One-eyed orange monster with little horns.
    private val cyclopsBody = path("M24 70 C10 50 24 20 50 22 C78 24 90 50 76 72 C66 86 36 86 24 70 Z")
    private val cyclopsBits = path("M34 66 Q50 76 66 66 M40 68 l3 6 l3 -6 M54 70 l3 6 l3 -6 M30 24 l-6 -10 M70 24 l6 -10 M36 84 l-4 10 M64 84 l4 10")

    fun cyclops(c: Canvas, p: Paint) {
        prep(p)
        stroke(p, ORANGE, 4f); c.drawPath(cyclopsBody, p)
        eye(c, p, 50f, 48f, 14f, 52f, 50f, 6f, ORANGE, 3f)
        stroke(p, ORANGE, 3.5f); c.drawPath(cyclopsBits, p)
    }

    /** A little sparkle cross drawn straight on the page at ([x],[y]), [d] wide. */
    fun cross(c: Canvas, p: Paint, x: Float, y: Float, d: Float, width: Float) {
        prep(p)
        stroke(p, NAVY, width)
        c.drawLine(x, y, x + d, y + d, p)
        c.drawLine(x + d, y, x, y + d, p)
    }

    // Helpers

    private fun prep(p: Paint) {
        p.reset(); p.isAntiAlias = true
        p.strokeCap = Paint.Cap.ROUND; p.strokeJoin = Paint.Join.ROUND
    }

    private fun stroke(p: Paint, color: Int, width: Float) {
        p.style = Paint.Style.STROKE; p.color = color; p.strokeWidth = width; p.pathEffect = null
    }

    private fun fill(p: Paint, color: Int) {
        p.style = Paint.Style.FILL; p.color = color; p.pathEffect = null
    }

    private fun fillStroke(c: Canvas, p: Paint, shape: Path, fillColor: Int, lineColor: Int, width: Float) {
        fill(p, fillColor); c.drawPath(shape, p)
        stroke(p, lineColor, width); c.drawPath(shape, p)
    }

    private fun roundRect(c: Canvas, p: Paint, x: Float, y: Float, w: Float, h: Float, r: Float) {
        rect.set(x, y, x + w, y + h); c.drawRoundRect(rect, r, r, p)
    }

    private fun oval(c: Canvas, p: Paint, cx: Float, cy: Float, rx: Float, ry: Float) {
        rect.set(cx - rx, cy - ry, cx + rx, cy + ry); c.drawOval(rect, p)
    }

    /** A white eyeball outlined in [line] with a navy pupil. */
    private fun eye(c: Canvas, p: Paint, cx: Float, cy: Float, r: Float, px: Float, py: Float, pr: Float, line: Int, width: Float) {
        fill(p, WHITE); c.drawCircle(cx, cy, r, p)
        stroke(p, line, width); c.drawCircle(cx, cy, r, p)
        fill(p, NAVY); c.drawCircle(px, py, pr, p)
    }

    /** A solid navy tyre with a white hub. */
    private fun wheel(c: Canvas, p: Paint, cx: Float, cy: Float, r: Float, hub: Float) {
        fill(p, NAVY); c.drawCircle(cx, cy, r, p)
        fill(p, WHITE); c.drawCircle(cx, cy, hub, p)
    }

    @Suppress("RestrictedApi")
    private fun path(d: String): Path = PathParser.createPathFromPathData(d)
}

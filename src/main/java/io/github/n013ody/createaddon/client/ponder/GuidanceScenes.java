package io.github.n013ody.createaddon.client.ponder;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;

import io.github.n013ody.createaddon.registry.ModBlocks;

public final class GuidanceScenes {
    private GuidanceScenes() {
    }

    public static void materialOres(SceneBuilder scene, SceneBuildingUtil util) {
        computationBase(scene, util, "material_ores", "Materials for Computation");
        scene.world().setBlock(util.grid().at(1, 1, 2), ModBlocks.TIN_ORE.get().defaultBlockState(), false);
        scene.world().setBlock(util.grid().at(2, 1, 2), ModBlocks.DEEPSLATE_TIN_ORE.get().defaultBlockState(), false);
        scene.world().setBlock(util.grid().at(3, 1, 2), ModBlocks.DEEPSLATE_TUNGSTEN_ORE.get().defaultBlockState(), false);
        scene.world().showSection(util.select().fromTo(1, 1, 2, 3, 1, 2), Direction.DOWN);
        scene.idle(20);
        scene.overlay().showText(70)
                .text("Tin starts the early machine chain; tungsten is a deeper, later material.")
                .pointAt(util.vector().centerOf(2, 1, 2))
                .placeNearTarget();
        scene.idle(80);
        scene.overlay().showText(70)
                .colored(PonderPalette.GREEN)
                .text("These materials lead into semiconductor tools and precision computation parts.")
                .pointAt(util.vector().centerOf(3, 1, 2))
                .placeNearTarget();
        scene.idle(80);
        finish(scene);
    }

    public static void semiconductorMachines(SceneBuilder scene, SceneBuildingUtil util) {
        computationBase(scene, util, "semiconductor_machines", "Processing Wafers");
        scene.world().setBlock(util.grid().at(0, 1, 2), ModBlocks.CRYSTAL_PULL_FURNACE.get().defaultBlockState(), false);
        scene.world().setBlock(util.grid().at(1, 1, 2), ModBlocks.WAFER_SAW.get().defaultBlockState(), false);
        scene.world().setBlock(util.grid().at(2, 1, 2), ModBlocks.WAFER_POLISHER.get().defaultBlockState(), false);
        scene.world().setBlock(util.grid().at(3, 1, 2), ModBlocks.OXIDATION_DIFFUSION_FURNACE.get().defaultBlockState(), false);
        scene.world().setBlock(util.grid().at(4, 1, 2), ModBlocks.PHOTOLITHOGRAPHY_TABLE.get().defaultBlockState(), false);
        scene.world().showSection(util.select().fromTo(0, 1, 2, 4, 1, 2), Direction.DOWN);
        scene.idle(20);
        scene.overlay().showText(75)
                .text("Raw silicon becomes a boule, then wafers, then polished and prepared layers.")
                .pointAt(util.vector().centerOf(1, 1, 2))
                .placeNearTarget();
        scene.idle(85);
        scene.overlay().showText(75)
                .colored(PonderPalette.BLUE)
                .text("Some machines process instantly today; timed machines store input, animate, then output.")
                .pointAt(util.vector().centerOf(2, 1, 2))
                .placeNearTarget();
        scene.idle(85);
        scene.overlay().showText(75)
                .colored(PonderPalette.GREEN)
                .text("Later calibration cards should unlock better yield, stability and advanced recipes.")
                .pointAt(util.vector().centerOf(4, 1, 2))
                .placeNearTarget();
        scene.idle(85);
        finish(scene);
    }

    public static void differenceEngine(SceneBuilder scene, SceneBuildingUtil util) {
        computationBase(scene, util, "difference_engine", "Printing Processing Tables");
        scene.world().setBlock(util.grid().at(1, 1, 1), ModBlocks.FUNCTION_SETTING_DRUM.get().defaultBlockState(), false);
        scene.world().setBlock(util.grid().at(1, 1, 3), ModBlocks.DIFFERENCE_REGISTER_COLUMN.get().defaultBlockState(), false);
        scene.world().setBlock(util.grid().at(2, 1, 3), ModBlocks.DIFFERENCE_REGISTER_COLUMN.get().defaultBlockState(), false);
        scene.world().setBlock(util.grid().at(3, 1, 2), ModBlocks.TABLE_PRINTER.get().defaultBlockState(), false);
        scene.world().showSection(util.select().fromTo(1, 1, 1, 3, 1, 3), Direction.DOWN);
        scene.idle(20);
        scene.overlay().showText(75)
                .text("Place at least one function drum and two register columns within 4 blocks of a table printer.")
                .pointAt(util.vector().centerOf(2, 1, 2))
                .placeNearTarget();
        scene.idle(85);
        scene.overlay().showText(75)
                .colored(PonderPalette.BLUE)
                .text("Connect shaft power to the two axis-end faces. The other faces are for right-click use.")
                .pointAt(util.vector().centerOf(3, 1, 2))
                .placeNearTarget();
        scene.idle(85);
        scene.overlay().showText(75)
                .colored(PonderPalette.GREEN)
                .text("Right-click with blank table paper, then empty-hand right-click when the table is ready.")
                .pointAt(util.vector().topOf(3, 1, 2))
                .placeNearTarget();
        scene.idle(85);
        scene.overlay().showText(75)
                .colored(PonderPalette.BLUE)
                .text("More components and higher RPM work faster, but high speed needs more stress capacity.")
                .pointAt(util.vector().centerOf(2, 1, 3))
                .placeNearTarget();
        scene.idle(85);
        finish(scene);
    }

    public static void analyticalEngine(SceneBuilder scene, SceneBuildingUtil util) {
        computationBase(scene, util, "analytical_engine", "Compiling Calibration Cards");
        scene.world().setBlock(util.grid().at(1, 1, 1), ModBlocks.PUNCHED_CARD_READER.get().defaultBlockState(), false);
        scene.world().setBlock(util.grid().at(1, 1, 3), ModBlocks.MILL_ARITHMETIC_BLOCK.get().defaultBlockState(), false);
        scene.world().setBlock(util.grid().at(2, 1, 3), ModBlocks.STORE_MEMORY_COLUMN.get().defaultBlockState(), false);
        scene.world().setBlock(util.grid().at(3, 1, 2), ModBlocks.MECHANICAL_PRINTER.get().defaultBlockState(), false);
        scene.world().showSection(util.select().fromTo(1, 1, 1, 3, 1, 3), Direction.DOWN);
        scene.idle(20);
        scene.overlay().showText(75)
                .text("Place at least one card reader, one Mill and one Store within 4 blocks of a mechanical printer.")
                .pointAt(util.vector().centerOf(2, 1, 2))
                .placeNearTarget();
        scene.idle(85);
        scene.overlay().showText(75)
                .colored(PonderPalette.BLUE)
                .text("Connect shaft power to the two axis-end faces. The other faces are for right-click use.")
                .pointAt(util.vector().centerOf(3, 1, 2))
                .placeNearTarget();
        scene.idle(85);
        scene.overlay().showText(75)
                .colored(PonderPalette.GREEN)
                .text("Right-click with a processing curve table, then empty-hand right-click when the card is ready.")
                .pointAt(util.vector().topOf(3, 1, 2))
                .placeNearTarget();
        scene.idle(85);
        scene.overlay().showText(75)
                .colored(PonderPalette.BLUE)
                .text("More readers, Mills and Stores raise computation units; higher RPM also raises stress demand.")
                .pointAt(util.vector().centerOf(2, 1, 3))
                .placeNearTarget();
        scene.idle(85);
        finish(scene);
    }

    public static void testBlock(SceneBuilder scene, SceneBuildingUtil util) {
        computationBase(scene, util, "testblock", "Kinetic Test Block");
        scene.world().setBlock(util.grid().at(2, 1, 2), ModBlocks.TESTBLOCK.get().defaultBlockState(), false);
        scene.world().showSection(util.select().position(2, 1, 2), Direction.DOWN);
        scene.idle(20);
        scene.overlay().showText(70)
                .text("A development kinetic block kept in the creative tab for local mechanical tests.")
                .pointAt(util.vector().centerOf(2, 1, 2))
                .placeNearTarget();
        scene.idle(80);
        finish(scene);
    }

    public static void guidanceComputer(SceneBuilder scene, SceneBuildingUtil util) {
        baseScene(scene, util, "guidance_computer", "Using a Guidance Computer");
        scene.overlay().showText(70)
                .text("The Guidance Computer scans nearby sensor blocks and subscribes to their IValueProvider outputs.")
                .pointAt(util.vector().centerOf(2, 1, 2))
                .placeNearTarget();
        scene.idle(80);
        scene.overlay().showText(80)
                .colored(PonderPalette.GREEN)
                .text("A terrain-following loop can combine altitude, bearing, drift, closing speed and stress into rudder, elevator and throttle commands.")
                .pointAt(util.vector().centerOf(3, 1, 2))
                .placeNearTarget();
        scene.idle(90);
        finish(scene);
    }

    public static void relativeBearing(SceneBuilder scene, SceneBuildingUtil util) {
        sensorScene(scene, util, "relative_bearing_sensor", "Relative Bearing Sensor",
                "Compares the craft's current heading with the target line-of-sight.",
                "Output is yaw error in degrees: -180 to +180, where 0 means directly aligned.",
                "Use it for automatic rudders, turret drives and antenna pointing.");
    }

    public static void closingSpeed(SceneBuilder scene, SceneBuildingUtil util) {
        sensorScene(scene, util, "closing_speed_sensor", "Closing Speed Sensor",
                "Projects relative velocity onto the line-of-sight to a target.",
                "Positive values mean the target is closing; negative values mean separation is increasing.",
                "Use it for terminal braking, fuze timing and docking approach governors.");
    }

    public static void terrainAltimeter(SceneBuilder scene, SceneBuildingUtil util) {
        sensorScene(scene, util, "terrain_altimeter", "Terrain Altimeter",
                "Casts a mechanical sounding ray downward from the instrument body.",
                "Terrain mode measures clearance to the first collision below; sea-level mode reports height above sea level.",
                "Use it for terrain following, sea skimming and safe hover limits.");
    }

    public static void inertialDrift(SceneBuilder scene, SceneBuildingUtil util) {
        sensorScene(scene, util, "inertial_drift_sensor", "Inertial Drift Sensor",
                "Compares desired heading with the actual horizontal displacement direction.",
                "The output is a signed sideslip angle, useful even when the craft points one way and slides another.",
                "Use it to damp airship drift, ship skid and lateral overshoot.");
    }

    public static void lockOnProbability(SceneBuilder scene, SceneBuildingUtil util) {
        sensorScene(scene, util, "lock_on_probability_sensor", "Lock-on Probability Sensor",
                "Combines distance, angular error and lateral target speed into a single fire-control score.",
                "The output ranges from 0 to 1 and deliberately remains a probability, not a hard yes/no switch.",
                "Use it to gate launchers, cannons and interceptor logic.");
    }

    public static void structuralStress(SceneBuilder scene, SceneBuildingUtil util) {
        sensorScene(scene, util, "structural_stress_sensor", "Structural Stress Sensor",
                "Watches kinetic network load, linear speed, shaft speed and acceleration spikes.",
                "The output is a 0 to 1 danger level, independent from ordinary speedometers.",
                "Use it for overload protection, automatic throttling and safe-mode cutoffs.");
    }

    public static void relativeVelocityVector(SceneBuilder scene, SceneBuildingUtil util) {
        sensorScene(scene, util, "relative_velocity_vector_sensor", "Relative Velocity Vector Sensor",
                "Exposes the complete target velocity minus craft velocity as a Vec3.",
                "Unlike closing speed, this preserves lateral and vertical components for advanced interception math.",
                "Use it for proportional navigation, lead pursuit and docking controllers.");
    }

    public static void laserRangeFinder(SceneBuilder scene, SceneBuildingUtil util) {
        sensorScene(scene, util, "laser_range_finder", "Laser Range Finder",
                "Fires a beam along the sensor axis whose range is proportional to input rotational speed.",
                "At maximum Create speed (256 RPM) the beam reaches 128 blocks. Slower speeds shorten the reach.",
                "Use it for terrain profiling, altitude hold and multi-sensor closing-speed validation.");
    }

    public static void radarIndexer(SceneBuilder scene, SceneBuildingUtil util) {
        sensorScene(scene, util, "radar_indexer", "Radar Indexer",
                "Rotating-dish scanner that sweeps the area for entities or block coordinates.",
                "Default mode returns the nearest living entity position as a Vec3. Right-click to cycle modes.",
                "Use it for radar-guided lock-on, waypoint tracking and area surveillance.");
    }

    public static void thresholdController(SceneBuilder scene, SceneBuildingUtil util) {
        baseScene(scene, util, "threshold_controller", "Threshold Controller");
        scene.world().setBlock(util.grid().at(4, 1, 2), ModBlocks.THRESHOLD_CONTROLLER.get().defaultBlockState(), false);
        scene.world().showSection(util.select().position(4, 1, 2), Direction.DOWN);
        scene.idle(20);
        scene.overlay().showText(70)
                .text("Bind or cycle to a nearby sensor, then compare its reading with a threshold.")
                .pointAt(util.vector().centerOf(4, 1, 2))
                .placeNearTarget();
        scene.idle(80);
        scene.overlay().showText(70)
                .colored(PonderPalette.GREEN)
                .text("When the condition is true, the controller outputs full redstone strength.")
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 2), Direction.UP))
                .placeNearTarget();
        scene.idle(80);
        finish(scene);
    }

    public static void rotaryFrame(SceneBuilder scene, SceneBuildingUtil util) {
        computationBase(scene, util, "rotary_frame", "Rotary Frame");
        scene.world().setBlock(util.grid().at(2, 1, 2), ModBlocks.ROTARY_FRAME.get().defaultBlockState(), false);
        scene.world().showSection(util.select().position(2, 1, 2), Direction.DOWN);
        scene.idle(20);
        scene.overlay().showText(70)
                .text("Right-click with a block to use that block as the framed visual insert.")
                .pointAt(util.vector().centerOf(2, 1, 2))
                .placeNearTarget();
        scene.idle(80);
        scene.overlay().showText(70)
                .colored(PonderPalette.BLUE)
                .text("Empty-hand clicks rotate yaw; sneaking rotates pitch for angled instruments.")
                .pointAt(util.vector().topOf(2, 1, 2))
                .placeNearTarget();
        scene.idle(80);
        finish(scene);
    }

    private static void sensorScene(SceneBuilder scene, SceneBuildingUtil util, String id, String title,
                                    String summary, String output, String use) {
        baseScene(scene, util, id, title);
        scene.overlay().showOutlineWithText(util.select().position(2, 1, 2), 60)
                .text(summary)
                .pointAt(util.vector().centerOf(2, 1, 2))
                .placeNearTarget();
        scene.idle(70);
        scene.overlay().showText(70)
                .colored(PonderPalette.BLUE)
                .text(output)
                .pointAt(util.vector().blockSurface(util.grid().at(2, 1, 2), Direction.UP))
                .placeNearTarget();
        scene.idle(80);
        scene.overlay().showText(70)
                .colored(PonderPalette.GREEN)
                .text(use)
                .pointAt(util.vector().centerOf(3, 1, 2))
                .placeNearTarget();
        scene.idle(80);
        finish(scene);
    }

    private static void baseScene(SceneBuilder scene, SceneBuildingUtil util, String id, String title) {
        scene.title(id, title);
        scene.configureBasePlate(0, 0, 5);
        scene.world().setBlocks(util.select().layer(0), Blocks.ANDESITE.defaultBlockState(), false);
        scene.world().setBlock(util.grid().at(1, 1, 2), ModBlocks.GUIDANCE_COMPUTER.get().defaultBlockState(), false);
        scene.world().setBlock(util.grid().at(2, 1, 2), ModBlocks.TERRAIN_ALTIMETER.get().defaultBlockState(), false);
        scene.world().setBlock(util.grid().at(3, 1, 2), ModBlocks.RELATIVE_BEARING_SENSOR.get().defaultBlockState(), false);
        scene.showBasePlate();
        scene.idle(5);
        scene.world().showSection(util.select().fromTo(1, 1, 2, 3, 1, 2), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showControls(util.vector().topOf(2, 1, 2), Pointing.DOWN, 40).whileSneaking();
        scene.idle(20);
    }

    private static void computationBase(SceneBuilder scene, SceneBuildingUtil util, String id, String title) {
        scene.title(id, title);
        scene.configureBasePlate(0, 0, 5);
        scene.world().setBlocks(util.select().layer(0), Blocks.ANDESITE.defaultBlockState(), false);
        scene.showBasePlate();
        scene.idle(5);
        scene.overlay().showControls(util.vector().topOf(2, 1, 2), Pointing.DOWN, 40).whileSneaking();
        scene.idle(10);
    }

    private static void finish(SceneBuilder scene) {
        scene.markAsFinished();
        scene.idle(10);
    }
}


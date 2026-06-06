import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const root = path.resolve(__dirname, '..');
const read = (file) => fs.readFileSync(path.join(root, file), 'utf8');
const exists = (file) => fs.existsSync(path.join(root, file));
const assert = (condition, message) => {
  if (!condition) throw new Error(message);
};

const blocks = [
  'function_setting_drum',
  'difference_register_column',
  'table_printer',
  'punched_card_reader',
  'mill_arithmetic_block',
  'store_memory_column',
  'mechanical_printer',
];

const items = [
  'blank_table_paper',
  'basic_processing_curve_table',
  'blank_punched_card',
  'machine_calibration_card',
];

const modBlocks = read('src/main/java/io/github/n013ody/createaddon/registry/ModBlocks.java');
const modBlockEntities = read('src/main/java/io/github/n013ody/createaddon/registry/ModBlockEntities.java');
const creativeTabs = read('src/main/java/io/github/n013ody/createaddon/registry/ModCreativeTabs.java');
const client = read('src/main/java/io/github/n013ody/createaddon/client/CreateAddonClient.java');

for (const id of blocks) {
  const constName = id.toUpperCase();
  assert(modBlocks.includes(`"${id}"`), `ModBlocks missing ${id}`);
  assert(creativeTabs.includes(`${constName}_ITEM`), `Creative tab missing ${id}`);
  assert(exists(`src/main/resources/assets/createaddon/blockstates/${id}.json`), `Missing blockstate for ${id}`);
  assert(exists(`src/main/resources/assets/createaddon/models/block/${id}.json`), `Missing block model for ${id}`);
  assert(exists(`src/main/resources/assets/createaddon/models/item/${id}.json`), `Missing item model for ${id}`);
  assert(exists(`src/main/resources/data/createaddon/loot_table/blocks/${id}.json`), `Missing loot table for ${id}`);
  assert(exists(`src/main/resources/data/createaddon/recipe/${id}.json`), `Missing recipe for ${id}`);
}

for (const id of items) {
  const constName = id.toUpperCase();
  assert(modBlocks.includes(`"${id}"`), `ModBlocks missing item ${id}`);
  assert(creativeTabs.includes(`${constName}.get()`), `Creative tab missing item ${id}`);
  assert(exists(`src/main/resources/assets/createaddon/models/item/${id}.json`), `Missing item model for ${id}`);
}

assert(modBlockEntities.includes('COMPUTING_MACHINE'), 'Computing machines must have their own kinetic block entity type');
assert(modBlockEntities.includes('TABLE_PRINTER.get()'), 'Table printer must be a computing-machine block entity host');
assert(modBlockEntities.includes('MECHANICAL_PRINTER.get()'), 'Mechanical printer must be a computing-machine block entity host');

const computingBlock = read('src/main/java/io/github/n013ody/createaddon/content/computation/ComputingAssemblyBlock.java');
assert(computingBlock.includes('countNearby'), 'Computing assembly must scan nearby components');
assert(computingBlock.includes('requiredComponentCount'), 'Computing assembly must enforce component counts');
assert(computingBlock.includes('computationUnits'), 'Computing assembly components must expose computation units');
assert(computingBlock.includes('adjustedProcessingTicks'), 'Computing assembly must scale processing time from component capacity');
assert(computingBlock.includes('RotatedPillarKineticBlock'), 'Computing assembly must accept Create shaft input');
assert(computingBlock.includes('getSpeed()'), 'Computing assembly must use kinetic speed when starting work');
assert(computingBlock.includes('tryInsert'), 'Computing assembly must use timed processing, not instant output');

const computingMachine = read('src/main/java/io/github/n013ody/createaddon/content/computation/ComputingMachineBlockEntity.java');
assert(computingMachine.includes('extends KineticBlockEntity'), 'Computing machine block entity must join Create kinetic networks');
assert(computingMachine.includes('calculateStressApplied'), 'Computing machine must report stress impact');
assert(computingMachine.includes('isOverStressed'), 'Computing machine must respect overstressed networks');
assert(computingMachine.includes('MINIMUM_OPERATING_SPEED'), 'Computing machine must define a minimum operating speed');
assert(computingMachine.includes('dropContents'), 'Computing machine must return stored input/output when broken');
assert(computingBlock.includes('machine.dropContents'), 'Computing assembly block must drop stored contents before block entity removal');

const computingBlockItem = read('src/main/java/io/github/n013ody/createaddon/content/computation/ComputingBlockItem.java');
assert(!computingBlockItem.includes('appendHoverText'),
  'Computing block items must rely on Create tooltip registration instead of manually appending hover text');
for (const id of blocks) {
  const constantName = `${id.toUpperCase()}_ITEM`;
  assert(client.includes(`registerBlockTooltip(ModBlocks.${constantName}.get(), "block.createaddon.${id}")`),
    `${id} must explicitly use the block tooltip key`);
}

for (const id of ['table_printer', 'mechanical_printer']) {
  const blockstate = read(`src/main/resources/assets/createaddon/blockstates/${id}.json`);
  const model = read(`src/main/resources/assets/createaddon/models/block/${id}.json`);
  assert(blockstate.includes('axis=x'), `${id} blockstate must support x axis`);
  assert(blockstate.includes('axis=y'), `${id} blockstate must support y axis`);
  assert(blockstate.includes('axis=z'), `${id} blockstate must support z axis`);
  assert(model.includes('minecraft:block/cube'), `${id} model must use per-face textures`);
  assert(model.includes(`${id}_end`), `${id} model must mark kinetic end faces`);
  assert(model.includes(`${id}_side`), `${id} model must mark operation side faces`);
  assert(model.includes(`${id}_status`), `${id} model must mark a status face`);
  assert(exists(`src/main/resources/assets/createaddon/textures/block/${id}_end.png`), `${id} missing end-face texture`);
  assert(exists(`src/main/resources/assets/createaddon/textures/block/${id}_side.png`), `${id} missing side-face texture`);
  assert(exists(`src/main/resources/assets/createaddon/textures/block/${id}_status.png`), `${id} missing status-face texture`);
}

const lang = JSON.parse(read('src/main/resources/assets/createaddon/lang/en_us.json'));
for (const id of blocks) assert(lang[`block.createaddon.${id}`], `Missing English block lang for ${id}`);
for (const id of items) assert(lang[`item.createaddon.${id}`], `Missing English item lang for ${id}`);
assert(lang['message.createaddon.computation.missing_components'], 'Missing missing-components message');
assert(lang['message.createaddon.computation.started_table'], 'Missing difference engine start message');
assert(lang['message.createaddon.computation.started_card'], 'Missing analytical engine start message');
assert(lang['message.createaddon.computation.no_kinetic_power'], 'Missing no-kinetic-power message');
assert(lang['message.createaddon.computation.overstressed'], 'Missing overstressed message');
assert(lang['block.createaddon.table_printer.tooltip.condition4'], 'Table printer tooltip must explain start/retrieval');
assert(lang['block.createaddon.mechanical_printer.tooltip.condition4'], 'Mechanical printer tooltip must explain start/retrieval');
assert(lang['block.createaddon.table_printer.tooltip.condition5'], 'Table printer tooltip must explain face roles');
assert(lang['block.createaddon.mechanical_printer.tooltip.condition5'], 'Mechanical printer tooltip must explain face roles');
assert(lang['block.createaddon.function_setting_drum.tooltip.behaviour1'], 'Function drum tooltip must explain placement/use');
assert(lang['block.createaddon.mill_arithmetic_block.tooltip.behaviour1'], 'Mill tooltip must explain placement/use');
assert(lang['tooltip.createaddon.hold_shift'], 'Missing generic Shift hover prompt');
assert(lang['tooltip.createaddon.hold_w'], 'Missing generic Ponder hover prompt');
assert(client.includes('registerBlockTooltip(ModBlocks.TABLE_PRINTER_ITEM.get(), "block.createaddon.table_printer")'),
  'Table printer item must explicitly use the block tooltip key');
assert(client.includes('registerBlockTooltip(ModBlocks.MECHANICAL_PRINTER_ITEM.get(), "block.createaddon.mechanical_printer")'),
  'Mechanical printer item must explicitly use the block tooltip key');

console.log('computation engine content validated');

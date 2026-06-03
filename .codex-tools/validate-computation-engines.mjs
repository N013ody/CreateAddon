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

assert(modBlockEntities.includes('TABLE_PRINTER.get()'), 'Table printer must be a chip-machine block entity host');
assert(modBlockEntities.includes('MECHANICAL_PRINTER.get()'), 'Mechanical printer must be a chip-machine block entity host');

const computingBlock = read('src/main/java/io/github/n013ody/createaddon/content/computation/ComputingAssemblyBlock.java');
assert(computingBlock.includes('countNearby'), 'Computing assembly must scan nearby components');
assert(computingBlock.includes('requiredComponentCount'), 'Computing assembly must enforce component counts');
assert(computingBlock.includes('tryInsert'), 'Computing assembly must use timed processing, not instant output');

const lang = JSON.parse(read('src/main/resources/assets/createaddon/lang/en_us.json'));
for (const id of blocks) assert(lang[`block.createaddon.${id}`], `Missing English block lang for ${id}`);
for (const id of items) assert(lang[`item.createaddon.${id}`], `Missing English item lang for ${id}`);
assert(lang['message.createaddon.computation.missing_components'], 'Missing missing-components message');
assert(lang['message.createaddon.computation.started_table'], 'Missing difference engine start message');
assert(lang['message.createaddon.computation.started_card'], 'Missing analytical engine start message');

console.log('computation engine content validated');

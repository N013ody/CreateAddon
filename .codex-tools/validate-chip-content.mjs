import fs from 'node:fs';
import path from 'node:path';

const root = process.cwd();
const failures = [];

const machines = [
  'crystal_pull_furnace',
  'wafer_saw',
  'wafer_polisher',
  'oxidation_diffusion_furnace',
  'photolithography_table',
  'packaging_test_bench'
];
const items = [
  'silica_dust',
  'raw_silicon',
  'silicon_boule',
  'rough_wafer',
  'polished_wafer',
  'oxidized_wafer',
  'photoresist_wafer',
  'etched_wafer',
  'doped_wafer',
  'metallized_wafer',
  'finished_wafer',
  'bare_logic_die',
  'ceramic_chip_package',
  'basic_logic_chip'
];

function file(...parts) {
  return path.join(...parts).replaceAll('\\', '/');
}

function full(rel) {
  return path.join(root, rel);
}

function assert(condition, message) {
  if (!condition) failures.push(message);
}

function exists(rel) {
  return fs.existsSync(full(rel));
}

function read(rel) {
  if (!exists(rel)) {
    failures.push(`missing ${rel}`);
    return '';
  }
  return fs.readFileSync(full(rel), 'utf8');
}

function json(rel) {
  const text = read(rel);
  if (!text) return undefined;
  try {
    return JSON.parse(text);
  } catch (error) {
    failures.push(`invalid JSON ${rel}: ${error.message}`);
    return undefined;
  }
}

function includes(rel, values) {
  const text = read(rel);
  for (const value of values) {
    assert(text.includes(value), `${rel} should include ${value}`);
  }
}

function assertPng(rel) {
  if (!exists(rel)) {
    failures.push(`missing ${rel}`);
    return;
  }
  const bytes = fs.readFileSync(full(rel));
  assert(bytes.subarray(0, 8).toString('hex') === '89504e470d0a1a0a', `${rel} should be PNG`);
}

const modBlocks = read('src/main/java/io/github/n013ody/createaddon/registry/ModBlocks.java');
const creativeTabs = read('src/main/java/io/github/n013ody/createaddon/registry/ModCreativeTabs.java');

for (const name of machines) {
  const constant = name.toUpperCase();
  assert(modBlocks.includes(`"${name}"`), `ModBlocks should register ${name}`);
  assert(modBlocks.includes(constant), `ModBlocks should expose ${constant}`);
  assert(creativeTabs.includes(`${constant}_ITEM`), `creative tab should include ${name}`);
  assert(exists(file('src/main/resources/assets/createaddon/blockstates', `${name}.json`)), `missing blockstate for ${name}`);
  assert(exists(file('src/main/resources/assets/createaddon/models/block', `${name}.json`)), `missing block model for ${name}`);
  assert(exists(file('src/main/resources/assets/createaddon/models/item', `${name}.json`)), `missing block item model for ${name}`);
  assertPng(file('src/main/resources/assets/createaddon/textures/block', `${name}.png`));
  includes(file('src/main/resources/data/createaddon/loot_table/blocks', `${name}.json`), [`createaddon:${name}`]);
}

for (const name of items) {
  const constant = name.toUpperCase();
  assert(modBlocks.includes(`"${name}"`), `ModBlocks should register ${name}`);
  assert(modBlocks.includes(constant), `ModBlocks should expose ${constant}`);
  assert(creativeTabs.includes(`${constant}.get()`), `creative tab should include ${name}`);
  assert(exists(file('src/main/resources/assets/createaddon/models/item', `${name}.json`)), `missing item model for ${name}`);
  assertPng(file('src/main/resources/assets/createaddon/textures/item', `${name}.png`));
}

const pickaxe = json('src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json');
for (const name of machines) {
  assert((pickaxe?.values ?? []).includes(`createaddon:${name}`), `pickaxe tag should include ${name}`);
}

for (const [locale, expected] of [
  ['en_us', ['Crystal Pull Furnace', 'Wafer Saw', 'Wafer Polisher', 'Oxidation Diffusion Furnace', 'Photolithography Table', 'Packaging Test Bench', 'Basic Logic Chip']],
  ['zh_cn', ['单晶拉制炉', '晶圆切片机', '晶圆抛光机', '氧化扩散炉', '光刻工作台', '封装测试台', '基础逻辑芯片']]
]) {
  const lang = json(file('src/main/resources/assets/createaddon/lang', `${locale}.json`));
  const values = Object.values(lang ?? {});
  for (const value of expected) {
    assert(values.includes(value), `${locale}.json should include ${value}`);
  }
}

const recipes = [
  ['silica_dust_from_quartz', 'create:crushing', 'minecraft:quartz', 'createaddon:silica_dust'],
  ['raw_silicon_from_smelting_silica_dust', 'minecraft:smelting', 'createaddon:silica_dust', 'createaddon:raw_silicon'],
  ['silicon_boule', 'minecraft:crafting_shaped', 'createaddon:raw_silicon', 'createaddon:silicon_boule'],
  ['rough_wafer', 'minecraft:crafting_shaped', 'createaddon:silicon_boule', 'createaddon:rough_wafer'],
  ['polished_wafer', 'create:sandpaper_polishing', 'createaddon:rough_wafer', 'createaddon:polished_wafer'],
  ['oxidized_wafer', 'minecraft:smelting', 'createaddon:polished_wafer', 'createaddon:oxidized_wafer'],
  ['photoresist_wafer', 'minecraft:crafting_shaped', 'createaddon:oxidized_wafer', 'createaddon:photoresist_wafer'],
  ['etched_wafer', 'minecraft:crafting_shaped', 'createaddon:photoresist_wafer', 'createaddon:etched_wafer'],
  ['doped_wafer', 'minecraft:smelting', 'createaddon:etched_wafer', 'createaddon:doped_wafer'],
  ['metallized_wafer', 'minecraft:crafting_shaped', 'createaddon:doped_wafer', 'createaddon:metallized_wafer'],
  ['finished_wafer', 'minecraft:crafting_shaped', 'createaddon:metallized_wafer', 'createaddon:finished_wafer'],
  ['bare_logic_die', 'minecraft:crafting_shaped', 'createaddon:finished_wafer', 'createaddon:bare_logic_die'],
  ['ceramic_chip_package', 'minecraft:crafting_shaped', 'minecraft:brick', 'createaddon:ceramic_chip_package'],
  ['basic_logic_chip', 'minecraft:crafting_shaped', 'createaddon:bare_logic_die', 'createaddon:basic_logic_chip']
];

for (const [name, type, ingredient, result] of recipes) {
  const recipe = json(file('src/main/resources/data/createaddon/recipe', `${name}.json`));
  assert(recipe?.type === type, `${name} should use ${type}`);
  assert(JSON.stringify(recipe ?? {}).includes(ingredient), `${name} should consume ${ingredient}`);
  assert(JSON.stringify(recipe ?? {}).includes(result), `${name} should produce ${result}`);
}

if (failures.length) {
  console.error(`Chip content validation failed (${failures.length} issues):`);
  for (const failure of failures) console.error(`- ${failure}`);
  process.exit(1);
}

console.log('Chip content validation passed');

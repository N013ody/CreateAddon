import fs from 'node:fs';
import path from 'node:path';

const root = process.cwd();
const failures = [];

const oreBlocks = ['tin_ore', 'deepslate_tin_ore', 'deepslate_tungsten_ore'];
const materialItems = ['raw_tin', 'tin_ingot', 'raw_tungsten', 'tungsten_ingot'];

function rel(...parts) {
  return path.join(...parts).replaceAll('\\', '/');
}

function full(file) {
  return path.join(root, file);
}

function fail(message) {
  failures.push(message);
}

function assert(condition, message) {
  if (!condition) {
    fail(message);
  }
}

function exists(file) {
  return fs.existsSync(full(file));
}

function read(file) {
  try {
    return fs.readFileSync(full(file), 'utf8');
  } catch {
    fail(`missing ${file}`);
    return '';
  }
}

function parseJson(file) {
  const text = read(file);
  if (!text) {
    return undefined;
  }

  try {
    return JSON.parse(text);
  } catch (error) {
    fail(`invalid JSON ${file}: ${error.message}`);
    return undefined;
  }
}

function listJsonFiles(dir) {
  if (!fs.existsSync(dir)) {
    return [];
  }

  const entries = fs.readdirSync(dir, { withFileTypes: true });
  return entries.flatMap((entry) => {
    const child = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      return listJsonFiles(child);
    }
    return entry.isFile() && entry.name.endsWith('.json') ? [child] : [];
  });
}

function assertJsonIncludes(file, values) {
  const json = parseJson(file);
  const text = JSON.stringify(json ?? {});
  for (const value of values) {
    assert(text.includes(value), `${file} should reference ${value}`);
  }
  return json;
}

function assertTagContains(file, values) {
  const json = parseJson(file);
  const tagValues = json?.values ?? [];
  for (const value of values) {
    assert(tagValues.includes(value), `${file} should contain ${value}`);
  }
}

function assertPng(file) {
  if (!exists(file)) {
    fail(`missing ${file}`);
    return;
  }

  const bytes = fs.readFileSync(full(file));
  const signature = '89504e470d0a1a0a';
  assert(bytes.subarray(0, 8).toString('hex') === signature, `${file} should be a PNG file`);
}

for (const jsonFile of listJsonFiles(full('src/main/resources'))) {
  const projectRel = path.relative(root, jsonFile).replaceAll('\\', '/');
  parseJson(projectRel);
}

const modBlocks = read('src/main/java/io/github/n013ody/createaddon/registry/ModBlocks.java');
for (const name of oreBlocks) {
  assert(modBlocks.includes(`"${name}"`), `ModBlocks should register ${name}`);
  assert(modBlocks.includes(name.toUpperCase()), `ModBlocks should expose ${name.toUpperCase()}`);
}
for (const name of materialItems) {
  assert(modBlocks.includes(`"${name}"`), `ModBlocks should register ${name}`);
  assert(modBlocks.includes(name.toUpperCase()), `ModBlocks should expose ${name.toUpperCase()}`);
}

const creativeTabs = read('src/main/java/io/github/n013ody/createaddon/registry/ModCreativeTabs.java');
for (const name of [...oreBlocks, ...materialItems]) {
  assert(creativeTabs.includes(name.toUpperCase()), `creative tab should include ${name}`);
}

for (const name of oreBlocks) {
  assert(exists(rel('src/main/resources/assets/createaddon/blockstates', `${name}.json`)), `missing blockstate for ${name}`);
  assert(exists(rel('src/main/resources/assets/createaddon/models/block', `${name}.json`)), `missing block model for ${name}`);
  assert(exists(rel('src/main/resources/assets/createaddon/models/item', `${name}.json`)), `missing block item model for ${name}`);
  assertPng(rel('src/main/resources/assets/createaddon/textures/block', `${name}.png`));
  assertJsonIncludes(rel('src/main/resources/data/createaddon/loot_table/blocks', `${name}.json`), [
    `createaddon:${name}`,
    name.includes('tungsten') ? 'createaddon:raw_tungsten' : 'createaddon:raw_tin',
    'minecraft:silk_touch',
    'minecraft:fortune'
  ]);
}

for (const name of materialItems) {
  assert(exists(rel('src/main/resources/assets/createaddon/models/item', `${name}.json`)), `missing item model for ${name}`);
  assertPng(rel('src/main/resources/assets/createaddon/textures/item', `${name}.png`));
}

assertTagContains('src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json', oreBlocks.map((name) => `createaddon:${name}`));
assertTagContains('src/main/resources/data/minecraft/tags/block/needs_stone_tool.json', ['createaddon:tin_ore', 'createaddon:deepslate_tin_ore']);
assertTagContains('src/main/resources/data/minecraft/tags/block/needs_diamond_tool.json', ['createaddon:deepslate_tungsten_ore']);

assertJsonIncludes('src/main/resources/data/createaddon/worldgen/configured_feature/ore_tin.json', [
  'minecraft:ore',
  'createaddon:tin_ore',
  'createaddon:deepslate_tin_ore',
  'minecraft:stone_ore_replaceables',
  'minecraft:deepslate_ore_replaceables'
]);
assertJsonIncludes('src/main/resources/data/createaddon/worldgen/configured_feature/ore_tungsten.json', [
  'minecraft:ore',
  'createaddon:deepslate_tungsten_ore',
  'minecraft:deepslate_ore_replaceables'
]);

const tinPlacement = assertJsonIncludes('src/main/resources/data/createaddon/worldgen/placed_feature/ore_tin.json', [
  'createaddon:ore_tin',
  'minecraft:count',
  'minecraft:in_square',
  'minecraft:height_range',
  'minecraft:biome'
]);
assert(tinPlacement?.placement?.[0]?.count === 10, 'tin ore should generate 10 veins per chunk');
assert(tinPlacement?.placement?.[2]?.height?.min_inclusive?.absolute === -16, 'tin ore should start at Y -16');
assert(tinPlacement?.placement?.[2]?.height?.max_inclusive?.absolute === 96, 'tin ore should end at Y 96');

const tungstenPlacement = assertJsonIncludes('src/main/resources/data/createaddon/worldgen/placed_feature/ore_tungsten.json', [
  'createaddon:ore_tungsten',
  'minecraft:count',
  'minecraft:in_square',
  'minecraft:height_range',
  'minecraft:biome'
]);
assert(tungstenPlacement?.placement?.[0]?.count === 3, 'tungsten ore should generate 3 veins per chunk');
assert(tungstenPlacement?.placement?.[2]?.height?.min_inclusive?.absolute === -64, 'tungsten ore should start at Y -64');
assert(tungstenPlacement?.placement?.[2]?.height?.max_inclusive?.absolute === -8, 'tungsten ore should end at Y -8');

for (const [name, feature] of [['tin', 'ore_tin'], ['tungsten', 'ore_tungsten']]) {
  const modifier = parseJson(rel('src/main/resources/data/createaddon/neoforge/biome_modifier', `add_${feature}.json`));
  assert(modifier?.type === 'neoforge:add_features', `${name} biome modifier should use neoforge:add_features`);
  assert(modifier?.biomes === '#c:is_overworld', `${name} biome modifier should target #c:is_overworld`);
  assert(modifier?.features === `createaddon:${feature}`, `${name} biome modifier should inject createaddon:${feature}`);
  assert(modifier?.step === 'underground_ores', `${name} biome modifier should run in underground_ores`);
}

const recipeExpectations = [
  ['tin_ingot_from_smelting_raw_tin', 'minecraft:smelting', 'createaddon:raw_tin', 'createaddon:tin_ingot'],
  ['tin_ingot_from_blasting_raw_tin', 'minecraft:blasting', 'createaddon:raw_tin', 'createaddon:tin_ingot'],
  ['tin_ingot_from_smelting_tin_ore', 'minecraft:smelting', 'createaddon:tin_ore', 'createaddon:tin_ingot'],
  ['tin_ingot_from_blasting_tin_ore', 'minecraft:blasting', 'createaddon:tin_ore', 'createaddon:tin_ingot'],
  ['tin_ingot_from_smelting_deepslate_tin_ore', 'minecraft:smelting', 'createaddon:deepslate_tin_ore', 'createaddon:tin_ingot'],
  ['tin_ingot_from_blasting_deepslate_tin_ore', 'minecraft:blasting', 'createaddon:deepslate_tin_ore', 'createaddon:tin_ingot'],
  ['tungsten_ingot_from_smelting_raw_tungsten', 'minecraft:smelting', 'createaddon:raw_tungsten', 'createaddon:tungsten_ingot'],
  ['tungsten_ingot_from_blasting_raw_tungsten', 'minecraft:blasting', 'createaddon:raw_tungsten', 'createaddon:tungsten_ingot'],
  ['tungsten_ingot_from_smelting_deepslate_tungsten_ore', 'minecraft:smelting', 'createaddon:deepslate_tungsten_ore', 'createaddon:tungsten_ingot'],
  ['tungsten_ingot_from_blasting_deepslate_tungsten_ore', 'minecraft:blasting', 'createaddon:deepslate_tungsten_ore', 'createaddon:tungsten_ingot']
];

for (const [file, type, ingredient, result] of recipeExpectations) {
  const recipe = parseJson(rel('src/main/resources/data/createaddon/recipe', `${file}.json`));
  assert(recipe?.type === type, `${file} should have type ${type}`);
  assert(recipe?.ingredient?.item === ingredient, `${file} should consume ${ingredient}`);
  assert(recipe?.result?.id === result, `${file} should produce ${result}`);
}

for (const [locale, names] of [
  ['en_us', ['Tin Ore', 'Deepslate Tin Ore', 'Deepslate Tungsten Ore', 'Raw Tin', 'Tin Ingot', 'Raw Tungsten', 'Tungsten Ingot']],
  ['zh_cn', ['锡矿石', '深层锡矿石', '深层钨矿石', '粗锡', '锡锭', '粗钨', '钨锭']]
]) {
  const lang = parseJson(rel('src/main/resources/assets/createaddon/lang', `${locale}.json`));
  for (const value of names) {
    assert(Object.values(lang ?? {}).includes(value), `${locale}.json should include ${value}`);
  }
}

if (failures.length) {
  console.error(`Ore content validation failed (${failures.length} issue${failures.length === 1 ? '' : 's'}):`);
  for (const failure of failures) {
    console.error(`- ${failure}`);
  }
  process.exit(1);
}

console.log('Ore content validation passed');

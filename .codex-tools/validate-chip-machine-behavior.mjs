import fs from 'node:fs';
import path from 'node:path';

const root = process.cwd();
const failures = [];

function rel(...parts) {
  return path.join(...parts).replaceAll('\\', '/');
}

function full(file) {
  return path.join(root, file);
}

function read(file) {
  if (!fs.existsSync(full(file))) {
    failures.push(`missing ${file}`);
    return '';
  }
  return fs.readFileSync(full(file), 'utf8');
}

function assert(condition, message) {
  if (!condition) failures.push(message);
}

function parseJson(file) {
  const text = read(file);
  if (!text) return undefined;
  try {
    return JSON.parse(text);
  } catch (error) {
    failures.push(`invalid JSON ${file}: ${error.message}`);
    return undefined;
  }
}

const modBlocks = read('src/main/java/io/github/n013ody/createaddon/registry/ModBlocks.java');
assert(modBlocks.includes('WaferSawBlock'), 'ModBlocks should import/use WaferSawBlock');
assert(modBlocks.includes('PhotolithographyTableBlock'), 'ModBlocks should import/use PhotolithographyTableBlock');
assert(modBlocks.includes('CrystalPullFurnaceBlock'), 'ModBlocks should import/use CrystalPullFurnaceBlock');
assert(modBlocks.includes('WaferPolisherBlock'), 'ModBlocks should import/use WaferPolisherBlock');
assert(modBlocks.includes('OxidationDiffusionFurnaceBlock'), 'ModBlocks should import/use OxidationDiffusionFurnaceBlock');
assert(modBlocks.includes('new WaferSawBlock'), 'wafer_saw should be registered as WaferSawBlock');
assert(modBlocks.includes('new PhotolithographyTableBlock'), 'photolithography_table should be registered as PhotolithographyTableBlock');
assert(modBlocks.includes('new CrystalPullFurnaceBlock'), 'crystal_pull_furnace should be registered as CrystalPullFurnaceBlock');
assert(modBlocks.includes('new WaferPolisherBlock'), 'wafer_polisher should be registered as WaferPolisherBlock');
assert(modBlocks.includes('new OxidationDiffusionFurnaceBlock'), 'oxidation_diffusion_furnace should be registered as OxidationDiffusionFurnaceBlock');

const waferSaw = read('src/main/java/io/github/n013ody/createaddon/content/semiconductor/WaferSawBlock.java');
assert(waferSaw.includes('useItemOn'), 'WaferSawBlock should implement right-click item interaction');
assert(waferSaw.includes('SILICON_BOULE'), 'WaferSawBlock should consume silicon boule');
assert(waferSaw.includes('ROUGH_WAFER'), 'WaferSawBlock should output rough wafers');
assert(waferSaw.includes('4'), 'WaferSawBlock should output four rough wafers from one boule');
assert(waferSaw.includes('message.createaddon.machine.wafer_saw.sliced'), 'WaferSawBlock should show a sliced message');

const lithography = read('src/main/java/io/github/n013ody/createaddon/content/semiconductor/PhotolithographyTableBlock.java');
assert(lithography.includes('useItemOn'), 'PhotolithographyTableBlock should implement right-click item interaction');
assert(lithography.includes('OXIDIZED_WAFER'), 'PhotolithographyTableBlock should consume oxidized wafer');
assert(lithography.includes('PHOTORESIST_WAFER'), 'PhotolithographyTableBlock should output/consume photoresist wafer');
assert(lithography.includes('ETCHED_WAFER'), 'PhotolithographyTableBlock should output etched wafer');
assert(lithography.includes('message.createaddon.machine.photolithography_table.coated'), 'PhotolithographyTableBlock should show a coating message');
assert(lithography.includes('message.createaddon.machine.photolithography_table.exposed'), 'PhotolithographyTableBlock should show an exposure message');

const crystalPull = read('src/main/java/io/github/n013ody/createaddon/content/semiconductor/CrystalPullFurnaceBlock.java');
assert(crystalPull.includes('RAW_SILICON'), 'CrystalPullFurnaceBlock should consume raw silicon');
assert(crystalPull.includes('SILICON_BOULE'), 'CrystalPullFurnaceBlock should output silicon boule');
assert(crystalPull.includes('triggerAnimation'), 'CrystalPullFurnaceBlock should trigger animation');
assert(crystalPull.includes('message.createaddon.machine.crystal_pull_furnace.grown'), 'CrystalPullFurnaceBlock should show a growth message');

const polisher = read('src/main/java/io/github/n013ody/createaddon/content/semiconductor/WaferPolisherBlock.java');
assert(polisher.includes('ROUGH_WAFER'), 'WaferPolisherBlock should consume rough wafer');
assert(polisher.includes('POLISHED_WAFER'), 'WaferPolisherBlock should output polished wafer');
assert(polisher.includes('tryInsert'), 'WaferPolisherBlock should insert rough wafers into the machine instead of crafting instantly');
assert(polisher.includes('tryExtract'), 'WaferPolisherBlock should extract finished wafers after processing');
assert(!polisher.includes('completeProcess('), 'WaferPolisherBlock should not complete processing immediately on right click');

const oxidation = read('src/main/java/io/github/n013ody/createaddon/content/semiconductor/OxidationDiffusionFurnaceBlock.java');
assert(oxidation.includes('POLISHED_WAFER'), 'OxidationDiffusionFurnaceBlock should consume polished wafer');
assert(oxidation.includes('OXIDIZED_WAFER'), 'OxidationDiffusionFurnaceBlock should output oxidized wafer');
assert(oxidation.includes('triggerAnimation'), 'OxidationDiffusionFurnaceBlock should trigger animation');
assert(oxidation.includes('message.createaddon.machine.oxidation_diffusion_furnace.oxidized'), 'OxidationDiffusionFurnaceBlock should show an oxidation message');

for (const locale of ['en_us', 'zh_cn']) {
  const lang = parseJson(rel('src/main/resources/assets/createaddon/lang', `${locale}.json`));
  const keys = Object.keys(lang ?? {});
  for (const key of [
    'message.createaddon.machine.wafer_saw.sliced',
    'message.createaddon.machine.photolithography_table.coated',
    'message.createaddon.machine.photolithography_table.exposed',
    'message.createaddon.machine.crystal_pull_furnace.grown',
    'message.createaddon.machine.wafer_polisher.polished',
    'message.createaddon.machine.oxidation_diffusion_furnace.oxidized'
  ]) {
    assert(keys.includes(key), `${locale}.json should contain ${key}`);
  }
}

if (failures.length) {
  console.error(`Chip machine behavior validation failed (${failures.length} issues):`);
  for (const failure of failures) console.error(`- ${failure}`);
  process.exit(1);
}

console.log('Chip machine behavior validation passed');

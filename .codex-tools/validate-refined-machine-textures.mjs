import fs from 'node:fs';
import path from 'node:path';

const root = process.cwd();

const machines = [
  'crystal_pull_furnace',
  'wafer_saw',
  'wafer_polisher',
  'oxidation_diffusion_furnace',
  'photolithography_table',
  'packaging_test_bench'
];

const requiredSlots = ['top', 'front', 'detail'];

function fail(message) {
  console.error(message);
  process.exitCode = 1;
}

function readJson(rel) {
  const file = path.join(root, rel);
  if (!fs.existsSync(file)) {
    fail(`Missing ${rel}`);
    return undefined;
  }
  return JSON.parse(fs.readFileSync(file, 'utf8'));
}

function assertPng(rel) {
  const file = path.join(root, rel);
  if (!fs.existsSync(file)) {
    fail(`Missing ${rel}`);
    return;
  }
  const signature = fs.readFileSync(file).subarray(0, 8).toString('hex');
  if (signature !== '89504e470d0a1a0a') {
    fail(`${rel} is not a PNG`);
  }
}

for (const machine of machines) {
  const model = readJson(`src/main/resources/assets/createaddon/models/block/${machine}.json`);
  if (!model) continue;

  for (const slot of requiredSlots) {
    const expected = `createaddon:block/${machine}_${slot}`;
    if (model.textures?.[slot] !== expected) {
      fail(`${machine} texture slot ${slot} should be ${expected}`);
    }
    assertPng(`src/main/resources/assets/createaddon/textures/block/${machine}_${slot}.png`);
  }

  const customTextureRefs = JSON.stringify(model.textures ?? {}).match(/createaddon:block\//g) ?? [];
  if (customTextureRefs.length < 4) {
    fail(`${machine} needs at least four custom texture references`);
  }

  const elementNames = (model.elements ?? []).map((element) => element.name ?? '').join(' ');
  if (!/panel|stage|head|tube|blade|probe|socket|tray|mouth|lens/i.test(elementNames)) {
    fail(`${machine} needs named functional elements, not only casing boxes`);
  }
}

if (process.exitCode) {
  process.exit();
}

console.log('Refined machine texture validation passed');

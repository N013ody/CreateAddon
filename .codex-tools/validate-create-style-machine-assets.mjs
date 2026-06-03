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

for (const machine of machines) {
  const rel = `src/main/resources/assets/createaddon/models/block/${machine}.json`;
  const model = readJson(rel);
  if (!model) continue;

  if (model.parent === 'minecraft:block/cube_all') {
    fail(`${machine} still uses cube_all`);
  }

  if (!Array.isArray(model.elements) || model.elements.length < 4) {
    fail(`${machine} needs at least 4 model elements`);
  }

  const textureText = JSON.stringify(model.textures ?? {});
  const usesCreateTexture = textureText.includes('create:block/');
  const usesPolisherAtlas = machine === 'wafer_polisher' && textureText.includes('createaddon:block/wafer_polisher_sheet');
  if (machine === 'wafer_polisher') {
    if (usesCreateTexture) {
      fail(`${machine} must not reuse Create block textures after the custom texture redesign`);
    }
    for (const slot of ['body', 'frame', 'drive', 'chain', 'top', 'front', 'detail', 'accent', 'sheet']) {
      if (!model.textures?.[slot]?.startsWith('createaddon:block/wafer_polisher_')) {
        fail(`${machine} texture slot ${slot} should use a custom wafer_polisher texture`);
      }
    }
  } else if (!usesCreateTexture && !usesPolisherAtlas) {
    fail(`${machine} must reuse Create block textures or provide an approved custom atlas`);
  }

  const customTextureCount = (textureText.match(/createaddon:block\//g) ?? []).length;
  if (machine === 'wafer_polisher' && customTextureCount < 9) {
    fail(`${machine} needs the full custom wafer polisher texture set`);
  } else if (machine !== 'wafer_polisher' && customTextureCount < 1) {
    fail(`${machine} needs at least one custom CreateAddon detail texture`);
  }
}

if (process.exitCode) {
  process.exit();
}

console.log('Create-style machine asset validation passed');

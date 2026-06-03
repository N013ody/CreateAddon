import fs from 'node:fs';
import path from 'node:path';

const modelPath = path.join(process.cwd(), 'src/main/resources/assets/createaddon/models/block/wafer_polisher.json');
const model = JSON.parse(fs.readFileSync(modelPath, 'utf8'));
const elements = model.elements ?? [];
const failures = [];

function requireElement(name) {
  if (!elements.some((element) => element.name === name)) {
    failures.push(`missing design element ${name}`);
  }
}

function textureRefs() {
  return JSON.stringify(model.textures ?? {});
}

function assertPngSize(rel, expectedWidth, expectedHeight) {
  const file = path.join(process.cwd(), rel);
  if (!fs.existsSync(file)) {
    failures.push(`missing texture ${rel}`);
    return;
  }

  const data = fs.readFileSync(file);
  if (data.subarray(0, 8).toString('hex') !== '89504e470d0a1a0a') {
    failures.push(`${rel} is not a PNG`);
    return;
  }

  const width = data.readUInt32BE(16);
  const height = data.readUInt32BE(20);
  if (width !== expectedWidth || height !== expectedHeight) {
    failures.push(`${rel} should be ${expectedWidth}x${expectedHeight}, found ${width}x${height}`);
  }
}

function overlapAmount(a, b, axis) {
  const min = Math.max(a.from[axis], b.from[axis]);
  const max = Math.min(a.to[axis], b.to[axis]);
  return max - min;
}

for (let i = 0; i < elements.length; i++) {
  const element = elements[i];
  if (element.from.some((value) => value < 0) || element.to.some((value) => value > 16)) {
    failures.push(`${element.name ?? i} extends outside the normal 16x16x16 block footprint`);
  }
  if (element.from.some((value, axis) => value >= element.to[axis])) {
    failures.push(`${element.name ?? i} has invalid from/to bounds`);
  }

  for (let j = i + 1; j < elements.length; j++) {
    const a = elements[i];
    const b = elements[j];
    const x = overlapAmount(a, b, 0);
    const y = overlapAmount(a, b, 1);
    const z = overlapAmount(a, b, 2);
    if (x > 0 && y > 0 && z > 0) {
      failures.push(`${a.name ?? i} overlaps ${b.name ?? j} by ${x}x${y}x${z}`);
    }
  }
}

for (const name of [
  'low_graphite_base',
  'compact_precision_body',
  'front_wafer_drawer',
  'left_custom_drive_box',
  'right_slurry_service_box',
  'rear_left_press_column',
  'rear_right_press_column',
  'overhead_pressure_beam',
  'round_polishing_platen',
  'raised_wafer_stage',
  'pressure_head_carriage',
  'small_pressure_pad'
]) {
  requireElement(name);
}

if (elements.some((element) => /bullseye|target|big_disc|black_white|arch|corner_/i.test(element.name ?? ''))) {
  failures.push('model should not use old bullseye, arch, or decorative corner patterns');
}

if (textureRefs().includes('create:block/')) {
  failures.push('wafer polisher should use fully custom CreateAddon textures, not Create block texture reuse');
}

for (const texture of [
  'createaddon:block/wafer_polisher_body',
  'createaddon:block/wafer_polisher_frame',
  'createaddon:block/wafer_polisher_drive',
  'createaddon:block/wafer_polisher_chain',
  'createaddon:block/wafer_polisher_top',
  'createaddon:block/wafer_polisher_front',
  'createaddon:block/wafer_polisher_detail',
  'createaddon:block/wafer_polisher_accent'
]) {
  if (!textureRefs().includes(texture)) {
    failures.push(`model should reference ${texture}`);
  }
}

if (!textureRefs().includes('createaddon:block/wafer_polisher_sheet')) {
  failures.push('model should use a dedicated wafer_polisher_sheet texture atlas');
}

if (elements.length < 10 || elements.length > 16) {
  failures.push(`model should keep a clear low-element silhouette with 10-16 cuboids, found ${elements.length}`);
}

if ((model.credit ?? '').includes('v7') === false || (model.credit ?? '').includes('white minimalist 32px texture') === false) {
  failures.push('model credit should identify the v7 white minimalist 32px texture redesign');
}

const beam = elements.find((element) => element.name === 'overhead_pressure_beam');
if (beam && (beam.from[0] < 5 || beam.to[0] > 11 || beam.from[1] < 12 || beam.to[1] > 14)) {
  failures.push('overhead pressure beam should stay compact after screenshot review');
}

if (elements.some((element) => /lamp_strip|slurry_pipe/i.test(element.name ?? ''))) {
  failures.push('minimalist wafer polisher should not keep decorative lamp strips or slurry pipes as separate cuboids');
}

for (const name of [
  'wafer_polisher_body',
  'wafer_polisher_frame',
  'wafer_polisher_drive',
  'wafer_polisher_chain',
  'wafer_polisher_top',
  'wafer_polisher_front',
  'wafer_polisher_detail',
  'wafer_polisher_accent',
  'wafer_polisher_sheet'
]) {
  assertPngSize(`src/main/resources/assets/createaddon/textures/block/${name}.png`, 32, 32);
}

if (failures.length) {
  console.error('Wafer polisher model validation failed:');
  for (const failure of failures) console.error(`- ${failure}`);
  process.exit(1);
}

console.log('Wafer polisher model validation passed');

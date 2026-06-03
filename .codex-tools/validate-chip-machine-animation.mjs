import fs from 'node:fs';
import path from 'node:path';

const root = process.cwd();

function fail(message) {
  console.error(message);
  process.exitCode = 1;
}

function read(rel) {
  const file = path.join(root, rel);
  if (!fs.existsSync(file)) {
    fail(`Missing ${rel}`);
    return '';
  }
  return fs.readFileSync(file, 'utf8');
}

const blockEntity = read('src/main/java/io/github/n013ody/createaddon/content/semiconductor/ChipMachineBlockEntity.java');
const renderer = read('src/main/java/io/github/n013ody/createaddon/client/ChipMachineBlockEntityRenderer.java');
const client = read('src/main/java/io/github/n013ody/createaddon/client/CreateAddonClient.java');
const blockEntities = read('src/main/java/io/github/n013ody/createaddon/registry/ModBlockEntities.java');
const blocks = read('src/main/java/io/github/n013ody/createaddon/registry/ModBlocks.java');
const baseBlock = read('src/main/java/io/github/n013ody/createaddon/content/semiconductor/ChipMachineBlock.java');

if (!baseBlock.includes('implements IBE<ChipMachineBlockEntity>')) {
  fail('ChipMachineBlock should provide the shared chip machine block entity');
}

for (const token of ['inputStack', 'outputStack', 'processingTicks', 'processingTicksTotal', 'getProcessingProgress']) {
  if (!blockEntity.includes(token)) {
    fail(`ChipMachineBlockEntity should expose timed processing state for animation: ${token}`);
  }
}

if (!blockEntities.includes('CHIP_MACHINE')) {
  fail('ModBlockEntities should register CHIP_MACHINE');
}

if (!client.includes('ChipMachineBlockEntityRenderer')) {
  fail('CreateAddonClient should register the chip machine renderer');
}

for (const token of ['renderWaferPolisher', 'itemRenderer', 'getProcessingProgress', 'Axis.YP.rotationDegrees', 'renderStatic']) {
  if (!renderer.includes(token)) {
    fail(`ChipMachineBlockEntityRenderer missing stable wafer polisher animation token: ${token}`);
  }
}

if (renderer.includes('RenderType.translucent()')) {
  fail('ChipMachineBlockEntityRenderer should not use translucent coplanar quads; they flicker against block faces');
}

for (const className of ['CrystalPullFurnaceBlock', 'WaferPolisherBlock', 'OxidationDiffusionFurnaceBlock']) {
  if (!blocks.includes(className)) {
    fail(`ModBlocks should use ${className}`);
  }
}

const polisher = read('src/main/java/io/github/n013ody/createaddon/content/semiconductor/WaferPolisherBlock.java');
for (const token of ['tryInsert', 'tryExtract', 'POLISHING_TICKS']) {
  if (!polisher.includes(token)) {
    fail(`WaferPolisherBlock should drive the timed renderer with real processing: ${token}`);
  }
}

if (process.exitCode) {
  process.exit();
}

console.log('Chip machine animation validation passed');

import fs from 'node:fs';
import path from 'node:path';

const root = process.cwd();

function read(rel) {
  return fs.readFileSync(path.join(root, rel), 'utf8');
}

function fail(message) {
  console.error(message);
  process.exitCode = 1;
}

const block = read('src/main/java/io/github/n013ody/createaddon/content/semiconductor/WaferPolisherBlock.java');
const blockEntity = read('src/main/java/io/github/n013ody/createaddon/content/semiconductor/ChipMachineBlockEntity.java');
const renderer = read('src/main/java/io/github/n013ody/createaddon/client/ChipMachineBlockEntityRenderer.java');

if (block.includes('completeProcess(')) {
  fail('WaferPolisherBlock must not directly complete processing on right click');
}

for (const token of ['tryInsert', 'tryExtract']) {
  if (!block.includes(token)) {
    fail(`WaferPolisherBlock should use block-entity ${token} interaction`);
  }
}

for (const token of [
  'inputStack',
  'outputStack',
  'processingTicks',
  'processingTicksTotal',
  'tick()',
  'finishProcessing',
  'getProcessingProgress'
]) {
  if (!blockEntity.includes(token)) {
    fail(`ChipMachineBlockEntity missing timed processing token: ${token}`);
  }
}

for (const token of ['write(', 'read(', 'sendData()', 'setChanged()']) {
  if (!blockEntity.includes(token)) {
    fail(`ChipMachineBlockEntity should persist/sync state via ${token}`);
  }
}

if (renderer.includes('RenderType.translucent()')) {
  fail('ChipMachineBlockEntityRenderer must not use translucent coplanar quads for machine animation');
}

for (const token of ['itemRenderer', 'getProcessingProgress', 'Axis.YP.rotationDegrees', 'renderStatic']) {
  if (!renderer.includes(token)) {
    fail(`ChipMachineBlockEntityRenderer missing stable item/progress animation token: ${token}`);
  }
}

if (process.exitCode) {
  process.exit();
}

console.log('Wafer polisher timed processing validation passed');
